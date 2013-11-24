package com.google.cloud.backend.android.sample.guestbook;

public class HackConstants {
	public static final int LOW_DPI_STATUS_BAR_HEIGHT = 40;
	public static final int MEDIUM_DPI_STATUS_BAR_HEIGHT = 65;
	public static final int HIGH_DPI_STATUS_BAR_HEIGHT = 98;
	public static final int XHIGH_DPI_STATUS_BAR_HEIGHT = 130;

	public static long REQUIRED_AVAILABLE_SPACE = 600;
	public static int REQUIRED_STORAGE_SPACE = 300 * 1024 * 1024;
	public static boolean SUPPORTS_FROYO = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO;
	public static boolean SUPPORTS_GINGERBREAD = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD;
	public static boolean SUPPORTS_HONEYCOMB = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB;
	public static boolean SUPPORTS_ICE_CREAM_SANDWICH = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
	public static boolean SUPPORTS_JELLYBEAN = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN;
	public static boolean SUPPORTS_KIT_KAT = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT;
	// dialog tags
	public static final String ERROR_DIALOG_TAG = "error_dialog";
	public static final String WAIT_DIALOG_TAG = "wait_dialog";

	// location constants
	public static final long LOC_UPDATE_INTERVAL = 5 * 1000 * 60;
	public static final long LOC_UPDATE_LOW_POWER_INTERVAL = 10 * 1000 * 60;
	public static final long LOC_UPDATE_CRITICAL_POWER_INTERVAL = 20 * 1000 * 60;
	public static final long LOC_UPDATE_FASTEST_INTERVAL =   1000 * 20;

	// prefs keys
	public static final String IS_CHARGING = "is_charging";
	public static final String BATTERY_LEVEL = "battery_level";
}
