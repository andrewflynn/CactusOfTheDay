package org.aflynn.cactusoftheday;

public final class Config {
    private Config() {}

    public static final String ENDPOINT = "https://api.flickr.com/services/rest";
    public static final String NSID = "120585395%40N06";
    public static final String LOG_TAG = "CactusOfTheDay";
    public static final String MUZEI_PKG = "net.nurik.roman.muzei";
    public static final String MUZEI_LAUNCHER_CLASS =
            "com.google.android.apps.muzei.MuzeiActivity";
    public static final String MUZEI_PLAY_STORE_URL =
            "https://play.google.com/store/apps/details?id=" + MUZEI_PKG;
}
