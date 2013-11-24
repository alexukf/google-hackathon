/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cloud.backend.android.sample.guestbook;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.PlusClient;
import com.google.cloud.backend.android.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class SignInActivity extends HackAbstractActivity implements
		OnClickListener, PlusClient.ConnectionCallbacks,
		PlusClient.OnConnectionFailedListener,
		PlusClient.OnAccessRevokedListener {

	public static final int DIALOG_GET_GOOGLE_PLAY_SERVICES = 1;

	public static final int REQUEST_CODE_SIGN_IN = 1;
	public static final int REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES = 2;

	// private TextView mSignInStatus;
	private PlusClient mPlusClient;
	private SignInButton mSignInButton;
	// private View mSignOutButton;
	// private View mRevokeAccessButton;
	private ConnectionResult mConnectionResult;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sign_in_activity);

		mPlusClient = new PlusClient.Builder(this, this, this).setActions(
				MomentUtil.ACTIONS).build();

		// mSignInStatus = (TextView) findViewById(R.id.sign_in_status);
		mSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
		mSignInButton.setOnClickListener(this);
		getSupportActionBar().hide();
		// mSignOutButton = findViewById(R.id.sign_out_button);
		// mSignOutButton.setOnClickListener(this);
		// mRevokeAccessButton = findViewById(R.id.revoke_access_button);
		// mRevokeAccessButton.setOnClickListener(this);
	}

	@Override
	public void onStart() {
		super.onStart();
		showDialog(HackConstants.WAIT_DIALOG_TAG, 0, R.string.signed_in_status);
		mPlusClient.connect();
	}

	@Override
	public void onStop() {
		mPlusClient.disconnect();
		super.onStop();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.sign_in_button:
			showDialog(HackConstants.WAIT_DIALOG_TAG, 0,
					R.string.signed_in_status);
			int available = GooglePlayServicesUtil
					.isGooglePlayServicesAvailable(this);
			if (available != ConnectionResult.SUCCESS) {
				showDialog(DIALOG_GET_GOOGLE_PLAY_SERVICES);
				return;
			}

			try {
				mConnectionResult.startResolutionForResult(this,
						REQUEST_CODE_SIGN_IN);
			} catch (IntentSender.SendIntentException e) {
				// Fetch a new result to start.
				mPlusClient.connect();
			}
			break;
		// case R.id.sign_out_button:
		// if (mPlusClient.isConnected()) {
		// mPlusClient.clearDefaultAccount();
		// mPlusClient.disconnect();
		// mPlusClient.connect();
		// }
		// break;
		// case R.id.revoke_access_button:
		// if (mPlusClient.isConnected()) {
		// mPlusClient.revokeAccessAndDisconnect(this);
		// updateButtons(false /* isSignedIn */);
		// }
		// break;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id != DIALOG_GET_GOOGLE_PLAY_SERVICES) {
			return super.onCreateDialog(id);
		}

		int available = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (available == ConnectionResult.SUCCESS) {
			return null;
		}
		if (GooglePlayServicesUtil.isUserRecoverableError(available)) {
			return GooglePlayServicesUtil.getErrorDialog(available, this,
					REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES);
		}
		return new AlertDialog.Builder(this)
				.setMessage(R.string.plus_generic_error).setCancelable(true)
				.create();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_SIGN_IN
				|| requestCode == REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES) {
			if (resultCode == RESULT_OK && !mPlusClient.isConnected()
					&& !mPlusClient.isConnecting()) {
				// This time, connect should succeed.
				mPlusClient.connect();
			}
		}
	}

	@Override
	public void onAccessRevoked(ConnectionResult status) {
		if (!status.isSuccess()) {
			// mSignInStatus.setText(R.string.revoke_access);
			mPlusClient.disconnect();
		}
		mPlusClient.connect();
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// String currentPersonName = mPlusClient.getCurrentPerson() != null ?
		// mPlusClient
		// .getCurrentPerson().getDisplayName()
		// : getString(R.string.unknown_person);
		// mSignInStatus.setText(getString(R.string.signed_in_status,
		// currentPersonName));
		removeDialog(HackConstants.WAIT_DIALOG_TAG);
		updateButtons(true /* isSignedIn */);

		// advance to the next activity
		final Intent i = new Intent(this, MainActivity.class);
		finish();
		startActivity(i);
	}

	@Override
	public void onDisconnected() {
		// mSignInStatus.setText(R.string.loading_status);
		// mPlusClient.connect();
		updateButtons(false /* isSignedIn */);
		removeDialog(HackConstants.WAIT_DIALOG_TAG);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		mConnectionResult = result;
		updateButtons(false /* isSignedIn */);

		removeDialog(HackConstants.WAIT_DIALOG_TAG);
		Toast.makeText(this, R.string.connection_problem, Toast.LENGTH_LONG)
				.show();
	}

	private void updateButtons(boolean isSignedIn) {
		if (!isSignedIn) {
			mSignInButton.setVisibility(View.VISIBLE);
			mSignInButton.setEnabled(true);
		} else {
			mSignInButton.setVisibility(View.INVISIBLE);
			mSignInButton.setEnabled(false);

		}
		// else {
		// if (mConnectionResult == null) {
		// // Disable the sign-in button until onConnectionFailed is called
		// // with result.
		// mSignInButton.setVisibility(View.INVISIBLE);
		// mSignInStatus.setText(getString(R.string.loading_status));
		// } else {
		// // Enable the sign-in button since a connection result is
		// // available.
		// mSignInButton.setVisibility(View.VISIBLE);
		// mSignInStatus.setText(getString(R.string.signed_out_status));
		// }
		//
		// mSignOutButton.setEnabled(false);
		// mRevokeAccessButton.setEnabled(false);
		// }
	}
}
