package com.google.cloud.backend.android.sample.guestbook.dialogs;


import com.google.cloud.backend.android.R;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.TextView;

public class TransparentInfoDialog extends DialogFragment {
	private static final String MESSAGE_KEY = "message";
	private TextView label;

	public static TransparentInfoDialog newInstance(int messageId) {
		TransparentInfoDialog dialog = new TransparentInfoDialog();
		Bundle arg = new Bundle();
		arg.putInt(MESSAGE_KEY, messageId);
		dialog.setArguments(arg);
		return dialog;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NO_FRAME, R.style.HackTransparentDialog);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.custom_progress_dialog,
				container, false);
		// progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
		label = (TextView) v.findViewById(R.id.label1);
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		label.setText(getString(getArguments().getInt(MESSAGE_KEY,
				R.string.empty_message)));
	}

	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		WindowManager.LayoutParams p = getDialog().getWindow().getAttributes();
		p.width = LayoutParams.MATCH_PARENT;
		p.height = LayoutParams.MATCH_PARENT;
		getDialog().getWindow().setAttributes(p);
		setCancelable(false);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		label = null;
	}

}
