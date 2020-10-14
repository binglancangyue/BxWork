package com.bixin.bxfm.model;

//import android.car.SettingsDef;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import com.bixin.bxfm.view.activity.FmApplication;

/**
 * @author Altair
 * @date :2020.01.06 下午 04:08
 * @description:
 */
public class FmSettingsObserver extends ContentObserver {
    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public FmSettingsObserver(Handler handler) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        ContentResolver resolver = FmApplication.getInstance().getContentResolver();
//        if (Settings.Secure.getUriFor(SettingsDef.FM_CURRENT_FREQUENCY).equals(uri)) {
//            int freq = Settings.Secure.getInt(resolver, SettingsDef.FM_CURRENT_FREQUENCY, 0);
//            Log.d("FmSettingsObserver", "onChange:freq " + freq);
//        }
    }
}
