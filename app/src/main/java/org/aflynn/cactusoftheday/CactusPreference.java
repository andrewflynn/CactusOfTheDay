package org.aflynn.cactusoftheday;

import android.content.Context;
import android.content.SharedPreferences;

public final class CactusPreference {
    private static final String PREFS_FILE = "prefs";
    private static final String KEY_RANDOM_CHOSEN_TIME_MILLIS = "random_chosen_time_millis";

    private CactusPreference() {}

    public static boolean checkRandomWindow(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        long randomChosenTimeMillis = prefs.getLong(KEY_RANDOM_CHOSEN_TIME_MILLIS, 0L);
        return System.currentTimeMillis() - randomChosenTimeMillis >
                Config.RANDOM_CACTUS_DISPLAY_WINDOW_MILLIS;
    }

    public static void clearRandomWindow(Context context) {
        setRandomWindowInternal(context, 0L);
    }

    public static void updateRandomWindow(Context context) {
        setRandomWindowInternal(context, System.currentTimeMillis());
    }

    private static void setRandomWindowInternal(Context context, long time) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        prefs.edit().putLong(KEY_RANDOM_CHOSEN_TIME_MILLIS, time).apply();
    }
}
