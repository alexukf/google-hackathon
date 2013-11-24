package com.google.cloud.backend.android.sample.guestbook.services;



import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.*;
import android.preference.PreferenceManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.cloud.backend.android.sample.guestbook.HackConstants;

/**
 * Created by Marius on 5/17/13.
 */
public class LocationsServices extends Service implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {
	public static final int REGISTER_MESSENGER = 1;
	public static final int DISMISS_MESSENGER = 2;
	public static final int UPDATE_LOCATION = 3;
	public static final int SCHEDULE_UPDATE = 4;
	public static final int REMOVE_LOCATION_UPDATE = 6;

	public static final String ON_LOCATION_UPDATED = "onLocationUpdated";
	public static final String ON_LOCATION_UPDATED_ERROR = "onLocationUpdatedError";

	public static final String GEOFENCE_UPDATE = "geofence_update";

	public static final String LATITUDE = "lat";
	public static final String LONGITUDE = "long";

	private Messenger clientMessenger;
	private Messenger serviceMessenger = new Messenger(new ServiceHandler());
	private LocationClient locationClient;

	// private boolean locationWasRequested = false;
	private int serviceID = 0;

	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(HackConstants.LOC_UPDATE_INTERVAL)
			// 5 minutes
			.setFastestInterval(HackConstants.LOC_UPDATE_FASTEST_INTERVAL) // 10
																			// seconds
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	public LocationsServices() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		if (serviceMessenger != null)
			return serviceMessenger.getBinder();
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		serviceID = startId;
		connectClient();
		return START_REDELIVER_INTENT;
	}

	private void connectClient() {
		if (locationClient != null) {
			if (locationClient.isConnected()) {
				requestLocation();
			} else if (!locationClient.isConnecting())
				locationClient.connect();
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		boolean toReturn = super.onUnbind(intent);
		// disconnect and remove location updates listener
		if (locationClient != null) {
			if (locationClient.isConnected()) {
				locationClient.removeLocationUpdates(this);

			} else if (locationClient.isConnecting())
				locationClient.disconnect();
		}
		return toReturn;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		locationClient = new LocationClient(getBaseContext(), this, this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// disconnect and remove location updates listener
		if (locationClient != null && locationClient.isConnected()) {
			locationClient.removeLocationUpdates(this);
			locationClient.disconnect();
		} else if (locationClient != null && locationClient.isConnecting())
			locationClient.disconnect();

		locationClient = null;
		serviceMessenger = null;
		clientMessenger = null;
	}

	@Override
	public void onConnected(Bundle bundle) {
		requestLocation();
	}

	@Override
	public void onDisconnected() {
		if (serviceID != 0)
			stopSelf(serviceID);
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Intent i = new Intent(ON_LOCATION_UPDATED_ERROR);
		sendBroadcast(i);

		connectClient();
	}

	@Override
	public void onLocationChanged(Location location) {
		// update the current location through the current clientMessenger
		Bundle b = new Bundle();
		b.putDouble(LATITUDE, location.getLatitude());
		b.putDouble(LONGITUDE, location.getLongitude());
		if (clientMessenger != null) {
			try {
				final Message m = Message.obtain(null, UPDATE_LOCATION);
				m.setData(b);
				clientMessenger.send(m);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		Intent i = new Intent(ON_LOCATION_UPDATED);
		sendBroadcast(i);

	}

	private void requestLocation() {
		if (locationClient != null && locationClient.isConnected()) {

			locationClient.removeLocationUpdates(this);
			// get the current battery status and level in order to set the
			// request priority
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());

			boolean isCharging = prefs.getBoolean(HackConstants.IS_CHARGING,
					false);
			float batteryLevel = prefs.getFloat(HackConstants.BATTERY_LEVEL,
					50f);

			if (isCharging) {
				REQUEST.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
			} else {
				if (batteryLevel > 50f) {
					REQUEST.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
					REQUEST.setFastestInterval(HackConstants.LOC_UPDATE_FASTEST_INTERVAL);
					REQUEST.setInterval(HackConstants.LOC_UPDATE_INTERVAL);
				} else if (batteryLevel > 20f) {
					REQUEST.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
					REQUEST.setFastestInterval(HackConstants.LOC_UPDATE_FASTEST_INTERVAL);
					REQUEST.setInterval(HackConstants.LOC_UPDATE_LOW_POWER_INTERVAL);

				} else {
					REQUEST.setPriority(LocationRequest.PRIORITY_NO_POWER);
					REQUEST.setFastestInterval(HackConstants.LOC_UPDATE_FASTEST_INTERVAL);
					REQUEST.setInterval(HackConstants.LOC_UPDATE_CRITICAL_POWER_INTERVAL);

				}
			}
			locationClient.requestLocationUpdates(REQUEST,
					LocationsServices.this);
		}
	}

	@SuppressLint("HandlerLeak")
	private class ServiceHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REGISTER_MESSENGER:
				clientMessenger = msg.replyTo;
				break;
			case DISMISS_MESSENGER:
				clientMessenger = null;
				break;
			case SCHEDULE_UPDATE:
				if (locationClient != null)
					if (locationClient.isConnected())
						requestLocation();
					else if (!locationClient.isConnecting()) {
						locationClient.connect();
					}
				break;
			case REMOVE_LOCATION_UPDATE:
				if (locationClient != null) {
					if (locationClient.isConnected())
						locationClient
								.removeLocationUpdates(LocationsServices.this);
				}
				break;

			}
		}
	}

}
