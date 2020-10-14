package com.bixin.bxfm.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.bixin.bxfm.view.activity.FmApplication;

/**
 * @author Altair
 * @date :2019.12.31 上午 10:13
 * @description:
 */
public class SharePreferenceTool {
    private static SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    private static class SingletonHolder {
        private static final SharePreferenceTool INSTANCE = new SharePreferenceTool();
    }

    public static SharePreferenceTool getInstance() {
        return SingletonHolder.INSTANCE;
    }


    public SharePreferenceTool() {
        mSharedPreferences = FmApplication.getInstance().getSharedPreferences("BX_FM",
                Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
    }


    public void saveInt(int value) {
        mEditor.putInt("FM_VALUE", value);
        mEditor.apply();
    }

    public int getInt() {
        return mSharedPreferences.getInt("FM_VALUE", 98000);
    }

    public void saveString(String value) {
        mEditor.putString("FM_VALUE", value);
        mEditor.apply();
    }

    public void saveString(String key, String value) {
        mEditor.putString(key, value);
        mEditor.apply();
    }
}
