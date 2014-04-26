package org.aflynn.cactusoftheday;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

public class CactusActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            if (getPackageManager().getApplicationInfo(Config.MUZEI_PKG, 0) != null) {
                Intent muzeiIntent = new Intent();
                muzeiIntent.setComponent(
                        new ComponentName(Config.MUZEI_PKG, Config.MUZEI_LAUNCHER_CLASS));
                startActivity(muzeiIntent);
                Analytics.track(this, Analytics.CATEGORY_USER, Analytics.ACTION_LAUNCHER);
                finish();
                return;
            }
        } catch (NameNotFoundException e) {
            // Not installed
        }

        Toast.makeText(this, R.string.please_install_muzei, Toast.LENGTH_LONG).show();
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Config.MUZEI_PLAY_STORE_URL)));
        Analytics.track(this, Analytics.CATEGORY_USER, Analytics.ACTION_PLAY_STORE_TRAMPOLINE,
                Config.MUZEI_PLAY_STORE_URL);
        finish();
    }
}
