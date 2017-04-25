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
import com.hymost.util.CommonUtil;

public class ShowRunInReslut extends BaseActivity {
    private static final String TAG = "ShowRunInReslut";
    private ShowRunInReslut showRunInReslut;
    private TextView mPassTextView;  
    private TextView mFailTextView; 
    private String mPassReslut = "";
    private String mFailReslut = "";
    private static SharedPreferences mSharedPreferences = null;
    private LockPatternUtils mLockPatternUtils;
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
        showRunInReslut = new ShowRunInReslut();
        showRunInReslut.isMonkeyRunning(TAG, "onCreate", ShowRunInReslut.this);
        setContentView(R.layout.show_runin_reslut);
        mSharedPreferences = this.getSharedPreferences("runintest", Activity.MODE_PRIVATE);
        mLockPatternUtils = new LockPatternUtils(this);
        showReslut();
        mFailTextView = (TextView) findViewById(R.id.showFailReslut);
        mFailTextView.setText(mFailReslut);
        mFailTextView.setTextColor(Color.RED);
        mFailTextView.getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG );
        mPassTextView = (TextView) findViewById(R.id.showPassReslut);
        mPassTextView.setText(mPassReslut);
        mPassTextView.setTextColor(Color.GREEN);
        mPassTextView.getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG );
    }

    @Override
    protected void onResume() {
        super.onResume();
        CommonUtil.acquireWakeLock(TAG, this);
        showRunInReslut.isMonkeyRunning(TAG, "onResume", ShowRunInReslut.this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        CommonUtil.startTestService(this, TAG);
        Settings.Global.putInt(this.getContentResolver(), "runin_testing", 1);
        LogRuningTest.printInfo(TAG, "onStart", this);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("startRunInTest", true);
        editor.commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CommonUtil.releaseWakeLock(TAG, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Settings.Global.putInt(this.getContentResolver(), "runin_testing", 0);
        LogRuningTest.printInfo(TAG, "onStop", this);
        SystemProperties.set("ctl.start", "charging_enable");
        CommonUtil.stopTestService(this, TAG);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("startRunInTest", false);
        editor.commit();
        mLockPatternUtils.setLockScreenDisabled(false, UserHandle.myUserId());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void showReslut() {
        SharedPreferences sharedPreferences = getSharedPreferences("runintest", Activity.MODE_PRIVATE);
        boolean test_result = sharedPreferences.getBoolean("testResult", false);
        LogRuningTest.printInfo(TAG, "testResult:" + test_result, this);
        boolean VIBRATOR_test = sharedPreferences.getBoolean("vibrator_test", false);
        LogRuningTest.printInfo(TAG, "VIBRATOR_test:" + VIBRATOR_test, this);
        boolean LCD_test = sharedPreferences.getBoolean("LCD_test", false);
        LogRuningTest.printInfo(TAG, "LCD_test:" + LCD_test, this);
        boolean TP_test = sharedPreferences.getBoolean("TP_test", true);
        LogRuningTest.printInfo(TAG, "TP_test:" + TP_test, this);
        boolean camera_test = sharedPreferences.getBoolean("camera_test", false);
        LogRuningTest.printInfo(TAG, "camera_test:" + camera_test, this);
        boolean audio_mic_test = sharedPreferences.getBoolean("audio_mic_test", false);
        LogRuningTest.printInfo(TAG, "audio_mic_test:" + audio_mic_test, this);
        boolean video_test = sharedPreferences.getBoolean("video_test", false);
        LogRuningTest.printInfo(TAG, "video_test:" + video_test, this);
        boolean charge_test = sharedPreferences.getBoolean("charge_test", false);
        LogRuningTest.printInfo(TAG, "charge_test:" + charge_test, this);
        boolean EMMC_test = sharedPreferences.getBoolean("EMMC_test", false);
        LogRuningTest.printInfo(TAG, "EMMC_test:" + EMMC_test, this);
        boolean REBOOT_test = sharedPreferences.getBoolean("reboot_test",false);
        LogRuningTest.printInfo(TAG, "REBOOT_test:" + REBOOT_test, this);
        boolean DDR_test = sharedPreferences.getBoolean("DDR_test", false);
        LogRuningTest.printInfo(TAG, "DDR_test:" + DDR_test, this);
        boolean LightSensor_test = sharedPreferences.getBoolean("lightsenor_test", false);
        LogRuningTest.printInfo(TAG, "LightSensor_test:" + LightSensor_test, this);
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
}  
