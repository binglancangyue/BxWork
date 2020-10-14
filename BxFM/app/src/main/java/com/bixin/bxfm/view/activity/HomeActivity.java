package com.bixin.bxfm.view.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bixin.bxfm.R;
import com.bixin.bxfm.model.RequestPermissionTool;
import com.bixin.bxfm.model.SharePreferenceTool;

import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.ref.WeakReference;


/**
 * @author Altair
 * @date :2020.01.06 上午 10:17
 * @description:
 */
public class HomeActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "FMHomeActivity";
    private ImageView ivIncrease;
    private ImageView ivDecrease;
    private TextView tvNumber;
    private SeekBar mSeekBar;
    private ImageView ivFmSwitch;
    private MyHandle mHandle;
    private int fmNumber;
    private SharePreferenceTool mSharePreferenceTool;
    private static final int MULTIPLE = 10;
    private static final int UPPER_LIMIT = 10800;
    private static final int LOWER_LIMIT = 7600;
    private static final int progressMaxFmValue = 3200;

    public static final String fm_power_path = "/sys/class/QN8027/QN8027/power_state";
    public static final String fm_tunetoch_path = "/sys/class/QN8027/QN8027/tunetoch";
    public static final int FM_STATE_ON = 1;
    public static final int FM_STATE_OFF = 0;
    private String[] permissions = new String[]{Manifest.permission.WRITE_SECURE_SETTINGS,
            Manifest.permission.WRITE_SETTINGS, Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.RECEIVE_BOOT_COMPLETED};
    private RequestPermissionTool requestPermissionTool;
    private AudioManager audioManager;

    private static class MyHandle extends Handler {
        private WeakReference<HomeActivity> weakReference;
        private HomeActivity mActivity;

        public MyHandle(HomeActivity homeActivity) {
            weakReference = new WeakReference<>(homeActivity);
            mActivity = weakReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            int type = msg.what;
            if (type == 1) {
                mActivity.updateFmHertzByProgress();
            }
            if (type == 2) {
                mActivity.execShell("exec system/bin/sh system/bin/fm_switch.sh");
            }
        }
    }

    public void execShell(String cmd) {
        try {
            //权限设置
            Process p = Runtime.getRuntime().exec("su");
            //获取输出流
            OutputStream outputStream = p.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            //将命令写入
            dataOutputStream.writeBytes(cmd);
            //提交命令
            dataOutputStream.flush();
            //关闭流操作
            dataOutputStream.close();
            outputStream.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        int option = window.getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        window.getDecorView().setSystemUiVisibility(option);
        window.setStatusBarColor(Color.TRANSPARENT);
//        setStatusBarVisible(false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        init();
        initView();
        initData();
    }

    private void init() {
        this.mHandle = new MyHandle(this);
        mHandle.sendEmptyMessage(2);
        this.mSharePreferenceTool = new SharePreferenceTool();
        requestPermissionTool = new RequestPermissionTool(this);
        this.audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    private void initView() {
        ivDecrease = findViewById(R.id.iv_decrease);
        ivIncrease = findViewById(R.id.iv_increase);
        tvNumber = findViewById(R.id.tv_number);
        ivFmSwitch = findViewById(R.id.iv_fm_switch);
        mSeekBar = findViewById(R.id.seek_bar_hz);
        setViewListener();
    }

    @SuppressLint("SetTextI18n")
    private void initData() {
        getWindow().getDecorView().post(() -> mHandle.post(() -> {
//            int fmValue = getSystemFmValue();
//            requestPermissionTool.initPermission(permissions, this);
            int fmValue = mSharePreferenceTool.getInt();
            int progress = getChangeProgress(fmValue);
            mSeekBar.setProgress(progress);
            setFmValue(fmValue);
            updateBtn(getFmState());
        }));
    }

    private void setViewListener() {
        ivIncrease.setOnClickListener(this);
        ivDecrease.setOnClickListener(this);
        ivFmSwitch.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mHandle.sendEmptyMessage(1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStopTrackingTouch:stop " + seekBar.getProgress());
                tuneFmValueByProgress(seekBar);
            }
        });
    }

    private void setStatusBarVisible(boolean show) {
        if (show) {
            int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            uiFlags |= 0x00001000;
            getWindow().getDecorView().setSystemUiVisibility(uiFlags);
        } else {
            int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            uiFlags |= 0x00001000;
            getWindow().getDecorView().setSystemUiVisibility(uiFlags);
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.iv_increase) {
            changeFrequencyBandByBtn(true);
        }
        if (viewId == R.id.iv_decrease) {
            changeFrequencyBandByBtn(false);
        }
        if (viewId == R.id.iv_fm_switch) {
            openOrCloseFM();
        }
    }

    private void openOrCloseFM() {
        boolean isSwitchOn = ivFmSwitch.isSelected();
        if (isSwitchOn) {
            closeFm();
        } else {
            openFm();
        }
        ivFmSwitch.setSelected(!isSwitchOn);
    }

    private void changeFrequencyBandByBtn(boolean b) {
        int progress = mSeekBar.getProgress() * MULTIPLE;
        int band;
        if (b) {
            band = progress + MULTIPLE;
            if (band > progressMaxFmValue) {
                band = progressMaxFmValue;
            }
        } else {
            band = progress - MULTIPLE;
            if (band <= 0) {
                band = 0;
            }
        }
        fmNumber = LOWER_LIMIT + band;
        mSeekBar.setProgress((fmNumber - LOWER_LIMIT) / MULTIPLE);
        setFmValue(fmNumber);
    }

    private void openFm() {
        try {
            Writer fm_power = new FileWriter(fm_power_path);
            fm_power.write("on");
            fm_power.close();
            setSpeakerphoneOn(false);
            Log.d(TAG, "openFm:on ");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "openFm: " + e.getMessage());
        }
    }

    private void closeFm() {
        try {
            Writer fm_power = new FileWriter(fm_power_path);
            fm_power.write("off");
            fm_power.flush();
            fm_power.close();
            setSpeakerphoneOn(true);
            Log.d(TAG, "closeFm:on off");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "closeFm: " + e.getMessage());
        }
    }

    private void updateFmHertzByProgress() {
        double progress = mSeekBar.getProgress();
        progress = progress / MULTIPLE;
        double number = progress + 76.0;
        tvNumber.setText(number + "");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void tuneFmValueByProgress(SeekBar seekBar) {
        int progress = seekBar.getProgress() * MULTIPLE;
        progress += LOWER_LIMIT;
        setFmValue(progress);
    }

    private void updateBtn(int status) {
        if (status == FM_STATE_ON) {
            ivFmSwitch.setSelected(true);
            openFm();
        } else if (status == FM_STATE_OFF) {
            ivFmSwitch.setSelected(false);
            closeFm();
        }
    }

    private void saveFmValue() {
        int progress = mSeekBar.getProgress() * MULTIPLE;
        progress = progress + LOWER_LIMIT;
        mSharePreferenceTool.saveInt(progress);
    }

    private int getChangeProgress(int value) {
        int seekBarProgress = value - LOWER_LIMIT;
        seekBarProgress = seekBarProgress / MULTIPLE;
        return seekBarProgress;
    }

    public int getFmState() {
        char[] buf = new char[10];
        String onOrOff;
        Reader in = null;
        try {
            in = new FileReader(fm_power_path);
            in.read(buf);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        onOrOff = new String(buf, 0, 1);
        if (onOrOff.equals("0")) {
            return 0;
        } else {
            return 1;
        }
    }

    private void setFmValue(int value) {
        try {
            Writer fmTuneTouch = new FileWriter(fm_tunetoch_path);
            fmTuneTouch.write(value + "");
            fmTuneTouch.flush();
            fmTuneTouch.close();
            mSharePreferenceTool.saveInt(value);
            Log.d(TAG, "setFmValue:value " + value);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "setFmValue: " + e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermissionDismiss = false;//有权限没有通过
        if (1000 == requestCode) {
            for (int grantResult : grantResults) {
                if (grantResult == -1) {
                    hasPermissionDismiss = true;
                }
            }
            //如果有权限没有被允许
            if (hasPermissionDismiss) {
                requestPermissionTool.showPermissionDialog();//跳转到系统设置权限页面，或者直接关闭页面，不让他继续访问
            }
        }
    }

    private void setSpeakerphoneOn(boolean b) {

//        if (audioManager != null) {
//            if (!b) {
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//            } else {
//                audioManager.setMode(AudioManager.MODE_NORMAL);
//            }
//            audioManager.setSpeakerphoneOn(b);
//            Log.d(TAG, "setSpeakerphoneOn: " + b);
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveFmValue();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandle != null) {
            mHandle.removeCallbacksAndMessages(null);
            mHandle = null;
        }
    }
}
