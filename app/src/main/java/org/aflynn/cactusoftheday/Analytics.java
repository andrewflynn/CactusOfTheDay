package org.aflynn.cactusoftheday;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.Map;

public final class Analytics {
    private Analytics() {}

    public static final String CATEGORY_USER = "user";
    public static final String CATEGORY_AUTO = "auto";

    public static final String ACTION_PLAY_STORE_TRAMPOLINE = "play_store_trampoline";
    public static final String ACTION_LAUNCHER = "launcher";
    public static final String ACTION_RPC = "rpc";
    public static final String ACTION_PUBLISH_WALLPAPER = "publish_wallpaper";
    public static final String ACTION_ERROR = "error";
    public static final String ACTION_NOT_UPDATING = "not_updating";
    public static final String ACTION_ENABLED = "enabled";
    public static final String ACTION_DISABLED = "disabled";

    public static final String LABEL_SINGLE_CACTUS = "single_cactus";
    public static final String LABEL_RANDOM_CACTUS = "single_cactus";
    public static final String LABEL_NULL_RESPONSE = "null_response";
    public static final String LABEL_NETWORK = "network";

    private static final Object sLock = new Object();
    private static volatile Tracker sTracker = null;

    public static void track(Context context, String category, String action) {
        getTracker(context).send(getEvent(category, action, null /* action */, 0L /* value */));
    }

    public static void track(Context context, String category, String action, String label) {
        getTracker(context).send(getEvent(category, action, label, 0L /* value */));
    }

    public static void track(Context context, String category, String action, String label,
            long value) {
        getTracker(context).send(getEvent(category, action, label, value));
    }

    private static Map<String, String> getEvent(String category, String action, String label,
            long value) {
        HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action);
        if (label != null) {
            builder.setLabel(label);
        }
        if (value != 0L) {
            builder.setValue(value);
        }
        return builder.build();
    }

    private static Tracker getTracker(Context context) {
        if (sTracker == null) {
            synchronized (sLock) {
                if (sTracker == null) {
                    sTracker = GoogleAnalytics.getInstance(context.getApplicationContext())
                            .newTracker(R.xml.global_tracker);
                }
            }
        }
        return sTracker;
    }
}
