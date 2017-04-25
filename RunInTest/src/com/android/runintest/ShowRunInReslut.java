package com.android.runintest;

import com.android.runintest.R;
import android.os.Bundle;  
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemProperties;
import android.app.Activity;  
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Html;  
import android.text.method.LinkMovementMethod;  
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
/* < 0071221 xuyinwen 20150918 begin */
import android.provider.Settings;
/* 0071221 xuyinwen 20150918 end > */
/* < 0080333 xuyinwen 20160107 begin */
import com.android.internal.widget.LockPatternUtils;
/* 0080333 xuyinwen 20160107 end > */
import android.os.UserHandle;

public class ShowRunInReslut extends BaseActivity {
    /* < 0068265 xuyinwen 20150819 begin */
    private static final String TAG = "ShowRunInReslut";
    private ShowRunInReslut showRunInReslut;
    /* 0068265 xuyinwen 20150819 end > */
    private TextView mPassTextView;  
    private TextView mFailTextView; 
    private String mPassReslut = "";
    private String mFailReslut = "";
    /* < 0077944 xuyinwen 20151207 begin */
    private PowerManager mPowerManager;
    private WakeLock mWl;
    /* 0077944 xuyinwen 20151207 end > */
    private static SharedPreferences mSharedPreferences = null;
    /* < 0080333 xuyinwen 20160107 begin */
    private LockPatternUtils mLockPatternUtils;
    /* 0080333 xuyinwen 20160107 end > */
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
        showRunInReslut = new ShowRunInReslut();
        showRunInReslut.isMonkeyRunning(TAG, "onCreate", ShowRunInReslut.this);
        setContentView(R.layout.show_runin_reslut);
        mSharedPreferences = this.getSharedPreferences("runintest", Activity.MODE_PRIVATE);
        /* < 0077944 xuyinwen 20151207 begin */
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWl = mPowerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                PowerManager.ON_AFTER_RELEASE, "bright");
        /* 0077944 xuyinwen 20151207 end > */
        /* < 0080333 xuyinwen 20160107 begin */
        mLockPatternUtils = new LockPatternUtils(this);
        /* 0080333 xuyinwen 20160107 end > */
        showReslut();
        /* < 0071221 xuyinwen 20150918 delete > */
        /* < 0068592 xuyinwen 20150824 begin */
        mFailTextView = (TextView) findViewById(R.id.showFailReslut);
        mFailTextView.setText(mFailReslut);
        mFailTextView.setTextColor(Color.RED);
        mFailTextView.getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG );
        mPassTextView = (TextView) findViewById(R.id.showPassReslut);
        mPassTextView.setText(mPassReslut);
        mPassTextView.setTextColor(Color.GREEN);
        mPassTextView.getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG );
        /* 0068592 xuyinwen 20150824 end > */
    }

    @Override
    protected void onResume() {
        super.onResume();
        showRunInReslut.isMonkeyRunning(TAG, "onResume", ShowRunInReslut.this);
    }

    /* < 0068593 xuyinwen 20150824 begin */
    private void startTestService() {
        Intent testService = new Intent(this, TestService.class);
        this.startService(testService);
        LogRuningTest.printInfo(TAG, "start TestService", this);
    }
    /* 0068593 xuyinwen 20150824 end > */

    /* < 0071221 xuyinwen 20150918 begin */
    @Override
        protected void onStart() {
            super.onStart();
            startTestService();
            /* < 0077339 xuyinwen 20150918 begin */
            Settings.Global.putInt(this.getContentResolver(), "runin_testing", 1);
            /* 0077339 xuyinwen 20150918 end > */
            LogRuningTest.printInfo(TAG, "onStart", this);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean("startRunInTest", true);
            editor.commit();
            /* < 0077944 xuyinwen 20151207 begin */
            if (mWl != null) {
                mWl.acquire();
            }
            /* 0077944 xuyinwen 20151207 end > */
        }

    @Override
    protected void onStop() {
        super.onStop();
        /* < 0077339 xuyinwen 20150918 begin */
        Settings.Global.putInt(this.getContentResolver(), "runin_testing", 0);
        /* 0077339 xuyinwen 20150918 end > */
        LogRuningTest.printInfo(TAG, "onStop", this);
        SystemProperties.set("ctl.start", "charging_enable");
        Intent startBattaryService = new Intent(this, TestService.class);
        this.stopService(startBattaryService);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("startRunInTest", false);
        editor.commit();
        /* < 0077944 xuyinwen 20151207 begin */
        if (mWl != null) {
            mWl.release();
        }
        /* 0077944 xuyinwen 20151207 end > */
        /* < 0080333 xuyinwen 20160107 begin */
        mLockPatternUtils.setLockScreenDisabled(false, UserHandle.myUserId());
        /* 0080333 xuyinwen 20160107 end > */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    /* 0071221 xuyinwen 20150918 end > */


    /* < 0068592 xuyinwen 20150824 begin */
    private void showReslut() {
        SharedPreferences sharedPreferences = getSharedPreferences("runintest", Activity.MODE_PRIVATE);
        boolean test_result = sharedPreferences.getBoolean("testResult", false);
        boolean VIBRATOR_test = sharedPreferences.getBoolean("vibrator_test", false);
        boolean LCD_test = sharedPreferences.getBoolean("LCD_test", false);
        boolean TP_test = sharedPreferences.getBoolean("TP_test", true);
        boolean camera_test = sharedPreferences.getBoolean("camera_test", false);
        boolean audio_mic_test = sharedPreferences.getBoolean("audio_mic_test", false);
        boolean video_test = sharedPreferences.getBoolean("video_test", false);
        boolean charge_test = sharedPreferences.getBoolean("charge_test", false);
        boolean EMMC_test = sharedPreferences.getBoolean("EMMC_test", false);
        boolean REBOOT_test = sharedPreferences.getBoolean("reboot_test",false);
        boolean DDR_test = sharedPreferences.getBoolean("DDR_test", false);
        boolean LightSensor_test = sharedPreferences.getBoolean("lightsenor_test", false);
        if (VIBRATOR_test && LCD_test && TP_test && camera_test && audio_mic_test
                && video_test && charge_test && EMMC_test && REBOOT_test
                && DDR_test && LightSensor_test){
            test_result =true;
        }
        if (test_result) {
            mPassReslut += "老化测试                                 成功" + "\n";
        } else {
            mFailReslut += "老化测试                                 失败" + "\n";
        }
        if(VIBRATOR_test){
        	mPassReslut += "振动测试                                成功" + "\n";
        }else{
        	mFailReslut += "振动测试                                失败" + "\n";
        }
        if (LCD_test) {
            mPassReslut += "LCD测试                                成功" + "\n";
        } else {
            mFailReslut += "LCD测试                                失败" + "\n";
        }
        if (TP_test) {
            mPassReslut += "TP测试                                  成功" + "\n";
        } else {
            mFailReslut += "TP测试                                  失败" + "\n";
        }
        if (camera_test) {
            mPassReslut += "照相测试                                成功" + "\n";
        } else {
            mFailReslut += "照相测试                                失败" + "\n";
        }
        if (audio_mic_test) {
            mPassReslut += "音频测试                                成功" + "\n";
        } else {
            mFailReslut += "音频测试                                失败" + "\n";
        }
        if (video_test) {
            mPassReslut += "视频测试                                成功" + "\n";
        } else {
            mFailReslut += "视频测试                                失败" + "\n";
        }
        if (charge_test) {
            mPassReslut += "充电测试                                成功" + "\n";
        }else{
            mFailReslut += "充电测试                                失败" + "\n";
        }
        if (EMMC_test) {
            mPassReslut += "EMMC测试                            成功" + "\n";
        } else {
            mFailReslut += "EMMC测试                            失败" + "\n";
        }
        if(REBOOT_test){
        	mPassReslut += "开关机测试                             成功" + "\n";
        }else{
        	mFailReslut += "开关机测试                             失败" + "\n";
        }
       
        /*boolean integrated_test = sharedPreferences.getBoolean("integrated_test", false);
        if (integrated_test) {
            mPassReslut += "综合测试                                成功" + "\n";
        } else {
            mFailReslut += "综合测试                                失败" + "\n";
        }*/
       /* boolean bluetooth_test = sharedPreferences.getBoolean("Bluetooth", true);
        if (bluetooth_test) {
            mPassReslut += "蓝牙测试                                成功" + "\n";
        }else{
            mFailReslut += "蓝牙测试                                失败" + "\n";
        }*/
 /*       boolean wifi_test = sharedPreferences.getBoolean("Wifi", true);
        if (wifi_test) {
            mPassReslut += "WIFI测试                                成功" + "\n";
        } else {
            mFailReslut += "WIFI测试                                失败" + "\n";
        }
        boolean gps_test = sharedPreferences.getBoolean("GPS", true);
        if (gps_test) {
            mPassReslut += "GPS测试                                成功" + "\n";
        } else {
            mFailReslut += "GPS测试                                失败" + "\n";
        }
        boolean gravity_test = sharedPreferences.getBoolean("Gravity", true);
        if (gravity_test) {
            mPassReslut += "重力感应测试                        成功" + "\n";
        } else {
            mFailReslut += "重力感应测试                        失败" + "\n";
        }
        boolean light_test = sharedPreferences.getBoolean("Light", true);
        if (light_test) {
            mPassReslut += "光感应测试                            成功" + "\n";
        }else{
            mFailReslut += "光感应测试                            失败" + "\n";
        }*/
     /*   boolean mic_test = sharedPreferences.getBoolean("MIC", true);
        if (mic_test) {
            mPassReslut += "MIC测试                                 成功" + "\n";
        } else {
            mFailReslut += "MIC测试                                 失败" + "\n";
        }
        boolean proximity_test =  sharedPreferences.getBoolean("Proximity", true);
        if (proximity_test) {
            mPassReslut += "Proximity测试                       成功" + "\n";
        } else {
            mFailReslut += "Proximity测试                       失败" + "\n";
        }
        boolean nfc_test =  sharedPreferences.getBoolean("NFC_test", true);
        if (nfc_test) {
            mPassReslut += "NFC                                          成功" + "\n";
        } else {
            mFailReslut += "NFC                                          失败" + "\n";
        }
        boolean FPS_test = sharedPreferences.getBoolean("FPS_test", false);
        if (FPS_test) {
            mPassReslut += "FPS                                          成功" + "\n";
        } else {
            mFailReslut += "FPS                                          失败" + "\n";
        }*/
        if (DDR_test) {
            mPassReslut += "DDR                                         成功" + "\n";
        } else {
            mFailReslut += "DDR                                         失败" + "\n";
        }
        if (LightSensor_test) {
            mPassReslut += "光感应测试                               成功" + "\n";
        } else {
            mFailReslut += "光感应测试                               失败" + "\n";
        }
    }
    /* 0068592 xuyinwen 20150824 end > */
}  
