package com.google.cloud.backend.android.sample.guestbook;

import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.google.cloud.backend.android.R;
import com.google.cloud.backend.android.sample.guestbook.dialogs.TransparentInfoDialog;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.LruCache;
import android.support.v7.app.ActionBarActivity;

public class HackAbstractActivity extends ActionBarActivity {
	protected CustomFragmentManager mFragmentManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mFragmentManager = new CustomFragmentManager(this);
	}

	// actions
	protected final void showDialog(String tag, int titleResId, int messageResId) {
		if (tag != null) {
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			Fragment prevFragment = getSupportFragmentManager()
					.findFragmentByTag(tag);
			if (prevFragment != null) {
				ft.remove(prevFragment);
			}
			DialogFragment dialog = null;
			if (tag.equals(HackConstants.WAIT_DIALOG_TAG)) {
				dialog = TransparentInfoDialog.newInstance(messageResId);
			}

			if (dialog != null) {
				dialog.show(ft, tag);
			}

		}
	}

	protected final void removeDialog(String tag) {
		if (tag != null) {
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			Fragment prevFragment = getSupportFragmentManager()
					.findFragmentByTag(tag);
			if (prevFragment != null) {
				ft.remove(prevFragment);
			}
			ft.commitAllowingStateLoss();
		}
	}

	// custom classes
	public final static class CustomFragmentManager {
		private ActionBarActivity activity;

		public CustomFragmentManager(ActionBarActivity a) {
			this.activity = a;
		}

		public void clearBackstack() {
			if (activity.getSupportFragmentManager().getBackStackEntryCount() > 0) {
				BackStackEntry entry = activity.getSupportFragmentManager()
						.getBackStackEntryAt(0);
				if (entry != null) {
					int id = entry.getId();
					activity.getSupportFragmentManager().popBackStackImmediate(
							id, FragmentManager.POP_BACK_STACK_INCLUSIVE);
				}
			}
		}

		/**
		 * navigate to a specific fragment
		 * 
		 * @param f
		 * @param transactionId
		 */
		public void switchToFragment(Fragment f, String transactionId,
				Boolean withAddToBackstack) {

			switchToFragment(f, transactionId, withAddToBackstack, true);

		}

		/**
		 * 
		 * @param f
		 * @param transactionId
		 * @param withAddToBackstack
		 * @param withCustomAnimation
		 */
		public void switchToFragment(Fragment f, String transactionId,
				Boolean withAddToBackstack, Boolean withCustomAnimation) {

			Fragment currentFragment = activity.getSupportFragmentManager()
					.findFragmentById(R.id.fragmentContainer);
			FragmentTransaction ft = activity.getSupportFragmentManager()
					.beginTransaction();

			// for web view based fragment we will not add an animation
			if (withCustomAnimation)
				ft.setCustomAnimations(R.anim.slide_in_right,
						R.anim.slide_out_left, R.anim.slide_in_left,
						R.anim.slide_out_right);

			if (currentFragment != null) {
				ft.replace(R.id.fragmentContainer, f);
			} else {
				ft.add(R.id.fragmentContainer, f);
			}

			if (withAddToBackstack)
				ft.addToBackStack(transactionId);

			ft.commit();

		}

	}

	public static final class BitmapLruCache extends LruCache<String, Bitmap>
			implements ImageCache {
		public BitmapLruCache(int size) {
			super(size);
		}

		@SuppressLint("NewApi")
		@Override
		protected int sizeOf(String key, Bitmap value) {
			if (HackConstants.SUPPORTS_ICE_CREAM_SANDWICH)

				return value.getByteCount();
			else
				return value.getRowBytes() * value.getHeight();
		}

		@Override
		public Bitmap getBitmap(String url) {
			return get(url);
		}

		@Override
		public void putBitmap(String url, Bitmap bitmap) {
			put(url, bitmap);
		}
	}

}
