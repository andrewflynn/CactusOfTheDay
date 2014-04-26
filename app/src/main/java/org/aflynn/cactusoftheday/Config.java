package org.aflynn.cactusoftheday;

import java.text.SimpleDateFormat;

public final class Config {
    private Config() {}

    public static final String ENDPOINT = "https://api.flickr.com/services/rest";
    public static final String NSID = "120585395%40N06";
    //public static final String NSID = "123915748@N06"; // testcactusoftheday
    public static final String LOG_TAG = "CactusOfTheDay";
    public static final String MUZEI_PKG = "net.nurik.roman.muzei";
    public static final String MUZEI_LAUNCHER_CLASS =
            "com.google.android.apps.muzei.MuzeiActivity";
    public static final String MUZEI_PLAY_STORE_URL =
            "https://play.google.com/store/apps/details?id=" + MUZEI_PKG;

    public static final SimpleDateFormat DESCRIPTION_DATE_FORMAT =
            new SimpleDateFormat("MMMM d yyyy"); // January 31 2014
    public static final int RANDOM_CACTUS_PAGINATION = 500;

    public static final long CHECK_INTERVAL_MILLIS = 1 * 60 * 60 * 1000L; // 1 hr
    public static final int CACTUS_OF_THE_DAY_DISPLAY_WINDOW_SECS = 48 * 60 * 60; // 48 hrs
    public static final long RANDOM_CACTUS_DISPLAY_WINDOW_MILLIS = 24 * 60 * 60 * 1000L; // 24 hrs
    // TEST VALUES
    //public static final long CHECK_INTERVAL_MILLIS = 1 * 10 * 1000L; // 10 seconds
    //public static final int CACTUS_OF_THE_DAY_DISPLAY_WINDOW_SECS = 1 * 30; // 30 seconds
    //public static final long RANDOM_CACTUS_DISPLAY_WINDOW_MILLIS = 1 * 20 * 1000L; // 20 seconds
}
