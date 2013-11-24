package com.google.cloud.backend.android.sample.guestbook.utils;

import java.io.File;
import java.io.FileNotFoundException;

import android.content.Context;
import android.os.Environment;

public class HackUtils {
	public static final String DIR_NAME = "hackcachedir";

	public static final File getCacheFolder(Context c)
			throws FileNotFoundException {
		// check sd card presence and write permissions
		File dir = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState()) && c.getExternalCacheDir() != null) {
			dir = new File(c.getExternalCacheDir().getPath() + File.separator
					+ DIR_NAME);
			if (!dir.exists())
				dir.mkdir();
		} else if (c.getCacheDir() != null) {
			dir = new File(c.getCacheDir().getPath() + File.separator
					+ DIR_NAME);
			if (!dir.exists())
				dir.mkdir();
		} else
			throw new FileNotFoundException(
					"the current device does not have a valid SD card or internal storage space with writting permissions");

		return dir;

	}
}
