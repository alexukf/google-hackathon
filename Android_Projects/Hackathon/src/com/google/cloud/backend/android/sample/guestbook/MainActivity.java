package com.google.cloud.backend.android.sample.guestbook;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.google.cloud.backend.android.sample.guestbook.HackAbstractActivity.BitmapLruCache;
import com.google.cloud.backend.android.sample.guestbook.model.LocationEntity;
import com.google.cloud.backend.android.sample.guestbook.services.LocationsServices;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.plus.PlusClient;
import com.google.cloud.backend.android.CloudBackendActivity;
import com.google.cloud.backend.android.CloudCallbackHandler;
import com.google.cloud.backend.android.CloudEntity;
import com.google.cloud.backend.android.CloudQuery.Order;
import com.google.cloud.backend.android.CloudQuery.Scope;
import com.google.cloud.backend.android.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends CloudBackendActivity implements
		PlusClient.ConnectionCallbacks, PlusClient.OnConnectionFailedListener,
		PlusClient.OnAccessRevokedListener, OnMyLocationChangeListener {
	private PlusClient mPlusClient;
	private RequestQueue reqQueue;
	private BitmapLruCache lruCache;
	private ImageLoader mImgLoader;
	private Handler uiHandler;
	private TextView usernameField;
	private NetworkImageView userProfileImage;
	private GoogleMap gMap;
	private boolean serviceBinded = false;

	private Messenger activityMessenger;

	private Messenger locationService;
	private CurrentLocationSource locSource;
	private final List<LocationEntity> locations = new ArrayList<LocationEntity>();
	private boolean firstLocationsGot = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mPlusClient = new PlusClient.Builder(this, this, this).setActions(
				MomentUtil.ACTIONS).build();
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		lruCache = new BitmapLruCache(
				(int) (manager.getMemoryClass() * 1024 * 1024 / 8));

		reqQueue = Volley.newRequestQueue(getBaseContext());
		mImgLoader = new ImageLoader(reqQueue, lruCache);
		uiHandler = new Handler();

		usernameField = (TextView) findViewById(R.id.label1);
		userProfileImage = (NetworkImageView) findViewById(R.id.imageContainer);
		final SupportMapFragment f = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		if (savedInstanceState == null) {
			// we will set the Map fragment to retain instance state
			f.setRetainInstance(true);
		} else {
			// the map was already initialized
			gMap = f.getMap();
		}

		activityMessenger = new Messenger(new ActivityHandler(this));
		locSource = new CurrentLocationSource(this);
		setUpMapIfNeeded();

	}

	final CloudCallbackHandler<List<CloudEntity>> getHandler = new CloudCallbackHandler<List<CloudEntity>>() {
		@Override
		public void onComplete(List<CloudEntity> results) {
			locations.clear();
			if (results != null) {
				Iterator<CloudEntity> iterator = results.iterator();
				while (iterator.hasNext()) {
					locations.add(new LocationEntity.Builder().setEntity(
							iterator.next()).create());
				}
			}

			addMapMarkers();

			firstLocationsGot = true;
		}

		@Override
		public void onError(IOException exception) {
			Log.e("Hckathlon", exception.toString());
		}
	};

	@Override
	protected void onPostCreate() {
		super.onPostCreate();

		getCloudBackend().clearAllSubscription();
		// execute the query with the handler
		getCloudBackend().listByKind(LocationEntity.hashtag,
				CloudEntity.PROP_CREATED_AT, Order.DESC, 50,
				Scope.FUTURE_AND_PAST, getHandler);

	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onStart() {
		super.onStart();
		int available = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (available != ConnectionResult.SUCCESS) {
			showDialog(SignInActivity.DIALOG_GET_GOOGLE_PLAY_SERVICES);
		}
		mPlusClient.connect();
	}

	@Override
	public void onStop() {
		mPlusClient.disconnect();
		if (uiHandler != null)
			uiHandler.removeCallbacksAndMessages(null);
		super.onStop();
		unbindServices();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SignInActivity.REQUEST_CODE_SIGN_IN
				|| requestCode == SignInActivity.REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES) {
			if (resultCode == RESULT_OK && !mPlusClient.isConnected()
					&& !mPlusClient.isConnecting()) {
				// This time, connect should succeed.
				mPlusClient.connect();
			}
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if (lruCache != null) {
			lruCache.evictAll();
		}
	}

	// actions
	private void addMapMarkers() {
		if (gMap != null && mPlusClient != null && mPlusClient.isConnected()) {
			gMap.clear();
			Iterator<LocationEntity> iterator = locations.iterator();
			LocationEntity toIterate;
			final LatLngBounds.Builder builder = new LatLngBounds.Builder();
			String myGoogleId = null;
			if (mPlusClient.getCurrentPerson() != null)
				myGoogleId = mPlusClient.getCurrentPerson().getId();

			while (iterator.hasNext()) {
				toIterate = iterator.next();
				if (myGoogleId == null
						|| !(toIterate.getUserId().equals(myGoogleId))) {
					final LatLng point = new LatLng(toIterate.getLatitude(),
							toIterate.getLongitude());
					gMap.addMarker(new MarkerOptions().position(point).title(
							toIterate.getUserName()));

					builder.include(point);
				}

			}
			// Pan to see all markers in view.
			// Cannot zoom to bounds until the map has a size.
			final View mapView = getSupportFragmentManager().findFragmentById(
					R.id.map).getView();
			if (mapView.getViewTreeObserver().isAlive()) {
				mapView.getViewTreeObserver().addOnGlobalLayoutListener(
						new ViewTreeObserver.OnGlobalLayoutListener() {
							@SuppressLint("NewApi")
							@SuppressWarnings("deprecation")
							// We use the new method when supported
							// We check which build version we are using.
							@Override
							public void onGlobalLayout() {
								if (locations.size() > 0) {
									if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
										mapView.getViewTreeObserver()
												.removeGlobalOnLayoutListener(
														this);
									} else {
										mapView.getViewTreeObserver()
												.removeOnGlobalLayoutListener(
														this);
									}
									gMap.animateCamera(CameraUpdateFactory
											.newLatLngBounds(builder.build(),
													10));

								}

							}
						});
			}
			if (locations.size() > 0) {
				gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
						new LatLng(locations.get(0).getLatitude(), locations
								.get(0).getLongitude()), 16f));
			}
		}
	}

	private void bindServices() {
		if (locationService == null && !serviceBinded) {
			bindService(new Intent(this, LocationsServices.class),
					locationServiceConnection, BIND_AUTO_CREATE);

		}
	}

	private void unbindServices() {
		if (serviceBinded && locationService != null) {

			try {
				locationService.send(Message.obtain(null,
						LocationsServices.REMOVE_LOCATION_UPDATE));
				locationService.send(Message.obtain(null,
						LocationsServices.DISMISS_MESSENGER));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			unbindService(locationServiceConnection);
			locationService = null;
			serviceBinded = false;
		}
	}

	private void setUpMapIfNeeded() {
		if (gMap == null) {
			gMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			if (gMap != null) {
				// addMapMarkers();
			}
		}

		gMap.setMyLocationEnabled(true);
		gMap.setLocationSource(locSource);
		gMap.setOnMyLocationChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	private void signOut() {
		int available = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (available != ConnectionResult.SUCCESS) {
			showDialog(SignInActivity.DIALOG_GET_GOOGLE_PLAY_SERVICES);
			return;
		}
		if (mPlusClient != null)
			mPlusClient.disconnect();
	}

	private void navigateToActivity(Class<? extends Activity> aClass) {
		Intent i = new Intent(this, aClass);
		startActivity(i);
	}

	// handlers

	@Override
	public void onMyLocationChange(Location arg0) {
		if (!firstLocationsGot) {
			firstLocationsGot = true;
			return;
		}
		Location l = new Location("CurrentLocation");
		l.setLatitude(arg0.getLatitude());
		l.setLongitude(arg0.getLongitude());

		if (mPlusClient != null && mPlusClient.isConnected()) {
			LocationEntity locEntity = Utils.findLocationEntityByClientId(
					locations, mPlusClient.getCurrentPerson().getId());

			if (locEntity == null) {
				locEntity = new LocationEntity.Builder()
						.setUserId(mPlusClient.getCurrentPerson().getId())
						.setUserName(
								mPlusClient.getCurrentPerson().getDisplayName())
						.setLatitude(arg0.getLatitude())
						.setLongitude(arg0.getLongitude()).create();

			} else {
				locEntity.setLatitude(arg0.getLatitude());
				locEntity.setLongitude(arg0.getLongitude());
			}
			// create a response handler that will receive the
			// result or
			// an error
			CloudCallbackHandler<CloudEntity> handler = new CloudCallbackHandler<CloudEntity>() {
				@Override
				public void onComplete(final CloudEntity result) {
					LocationEntity locEntity = Utils
							.findLocationEntityByClientId(locations,
									mPlusClient.getCurrentPerson().getId());
					if (locEntity == null)
						locations.add(new LocationEntity.Builder().setEntity(
								result).create());
					Toast.makeText(MainActivity.this, "Location sent to Cloud",
							Toast.LENGTH_LONG).show();
					getCloudBackend().clearAllSubscription();
					// execute the query with the handler
					getCloudBackend().listByKind(LocationEntity.hashtag,
							CloudEntity.PROP_CREATED_AT, Order.DESC, 50,
							Scope.FUTURE_AND_PAST, getHandler);

				}

				@Override
				public void onError(final IOException exception) {
					// FhandleEndpointException(exception);
				}
			};

			// execute the insertion with the handler
			getCloudBackend().update(locEntity.getEntity(), handler);
		}

	}

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id != SignInActivity.DIALOG_GET_GOOGLE_PLAY_SERVICES) {
			return super.onCreateDialog(id);
		}

		int available = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (available == ConnectionResult.SUCCESS) {
			return null;
		}
		if (GooglePlayServicesUtil.isUserRecoverableError(available)) {
			return GooglePlayServicesUtil.getErrorDialog(available, this,
					SignInActivity.REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES);
		}
		return new AlertDialog.Builder(this)
				.setMessage(R.string.plus_generic_error).setCancelable(true)
				.create();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.sign_out_menu_item:
			signOut();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onAccessRevoked(ConnectionResult arg0) {
		navigateToActivity(SignInActivity.class);

	}

	@Override
	public void onConnected(Bundle arg0) {
		if (mPlusClient.getCurrentPerson() != null) {
			if (mPlusClient.getCurrentPerson().getDisplayName() != null) {
				final String username = String.format(Locale.getDefault(),
						getString(R.string.username_message_format),
						mPlusClient.getCurrentPerson().getDisplayName());
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						if (usernameField != null)
							usernameField.setText(username);
					}
				});
			}
			if (mPlusClient.getCurrentPerson().getImage() != null) {
				final String imageUrl = mPlusClient.getCurrentPerson()
						.getImage().getUrl();
				if (imageUrl != null && userProfileImage != null) {
					userProfileImage.setImageUrl(imageUrl, mImgLoader);
				}
			}

		}
		bindServices();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// Toast.makeText(this, R.string.connection_problem, Toast.LENGTH_LONG)
		// .show();
		navigateToActivity(SignInActivity.class);
	}

	@Override
	public void onDisconnected() {
		navigateToActivity(SignInActivity.class);
	}

	private final ServiceConnection locationServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder iBinder) {
			if (iBinder != null) {
				serviceBinded = true;
				locationService = new Messenger(iBinder);
				final Message msg = Message.obtain(null,
						LocationsServices.REGISTER_MESSENGER);
				// register the activity messenger
				try {
					msg.replyTo = activityMessenger;
					locationService.send(msg);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				// we will request a location update
				// final Message reqLocMsg = Message.obtain(null,
				// LocationsServices.SCHEDULE_UPDATE);
				final Message reqLocMsg = Message.obtain(null,
						LocationsServices.SCHEDULE_UPDATE);
				// register the activity messenger
				try {
					locationService.send(reqLocMsg);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			serviceBinded = false;
			locationService = null;

		}
	};

	private static class ActivityHandler extends Handler {
		private final WeakReference<MainActivity> aReference;

		public ActivityHandler(MainActivity a) {
			aReference = new WeakReference<MainActivity>(a);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LocationsServices.UPDATE_LOCATION:
				if (aReference != null && aReference.get() != null) {
					if (aReference.get().locSource != null)
						aReference.get().locSource.onLocationChanged(
								msg.getData().getDouble(
										LocationsServices.LATITUDE),
								msg.getData().getDouble(
										LocationsServices.LONGITUDE));

				}
				break;
			}
		}

	}

	private static class CurrentLocationSource implements LocationSource {
		private OnLocationChangedListener mListener;

		@SuppressWarnings("unused")
		private final WeakReference<MainActivity> aReference;

		public CurrentLocationSource(MainActivity a) {
			aReference = new WeakReference<MainActivity>(a);
		}

		@Override
		public void activate(OnLocationChangedListener onLocationChangedListener) {
			mListener = onLocationChangedListener;
		}

		@Override
		public void deactivate() {
			mListener = null;
		}

		public void onLocationChanged(double latitude, double longitude) {
			Location l = new Location("CurrentLocation");
			l.setLatitude(latitude);
			l.setLongitude(longitude);

			mListener.onLocationChanged(l);

		}
	}
}
