package com.android.runintest;

import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.content.Intent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import android.os.FileUtils;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import android.util.Log;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.os.SystemProperties;
import android.provider.Settings;
import android.content.ActivityNotFoundException;
import android.os.PowerManager;

public class FPSTestActivity extends Activity{

    private static final String TAG = "FPSTestActivity";

    private static SharedPreferences mSharedPreferences = null;

    private static boolean mFPSSuccess = false;

    private static final int GO_TO_DDR = 0;
    private static final int CHECK_RESULT = 1;
    private static final int FPS_TEST = 2;

    private static final int DELAY_TIME = 10 * 1000;
    private static final int TEST_TIME = 10 * 60 * 1000;

    private static Long mTestTime = 0L;
    private PowerManager mPowerManager;

    private PowerManager.WakeLock mWl;
    private static final String mFileName = "/storage/emulated/0/FPSResult.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mSharedPreferences = this.getSharedPreferences("runintest", Activity.MODE_PRIVATE);
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWl = mPowerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                PowerManager.ON_AFTER_RELEASE, "bright");
        mTestTime = System.currentTimeMillis();
        mTestHandler.sendEmptyMessage(FPS_TEST);
    }

    private void fingerprintTest() {
        try {
            Intent intent = new Intent("android.intent.action.CIT_FINGPRINT_TEST");
            intent.putExtra("FromRunin", true);
            startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            Log.d(TAG, "the fingerprint activity is not exist");
        }
    }

    Handler mTestHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FPS_TEST:
                    Long testTime = System.currentTimeMillis() - mTestTime;
                    LogRuningTest.printDebug(TAG, "FPS_TEST   testTime  "  + testTime, FPSTestActivity.this);
                    if (testTime < TEST_TIME) {
                        fingerprintTest();
                        mTestHandler.sendEmptyMessageDelayed(FPS_TEST, DELAY_TIME);
                    } else {
                        mTestHandler.sendEmptyMessage(CHECK_RESULT);
                    }
                    break;
                case CHECK_RESULT:
                    LogRuningTest.printDebug(TAG, "CHECK_RESULT", FPSTestActivity.this);
                    checkTestResult();
                    mTestHandler.sendEmptyMessage(GO_TO_DDR);
                    break;
                case GO_TO_DDR:
                    LogRuningTest.printDebug(TAG, "GO_TO_DDR", FPSTestActivity.this);
                    gotoDDRTestActivity();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void onResume() {
        super.onResume();
        if (mWl != null) {
            mWl.acquire();
        }
    }

    public void onStop() {
        super.onStop();
        if (mWl != null) {
            mWl.release();
        }
    }

    private void gotoDDRTestActivity() {
        LogRuningTest.printInfo(TAG, "send broadcast", this);
        SharedPreferences.Editor editor =mSharedPreferences.edit();
        if (!mFPSSuccess) {
            editor.putBoolean("FPS_test", true);
        }
        editor.putBoolean("Ddr_test", true);
        editor.commit();
        Intent intent = new Intent(TestService.ACTION_DDR_TEST);
        sendBroadcast(intent);
        finish();
    }

    public void checkTestResult() {
        String res = "";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File(mFileName))));
            String str = null;
            while ((str = br.readLine()) != null) {
                res += str;
                if (str.contains("0")) {
                    mFPSSuccess= true;
                }else{
                    mFPSSuccess= false;
                    break;
                }
            }
        } catch (Exception e) {
            res += Log.getStackTraceString(e);
        }finally {
            try {
                if(null != br) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mFPSSuccess) {
            LogRuningTest.printDebug(TAG, "result:FPS test success", this);
        } else {
            LogRuningTest.printDebug(TAG, "result:FPS test fail", this);
            LogRuningTest.printError(TAG, "reason:" + res, this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                LogRuningTest.printInfo(TAG, "User click KEYCODE_BACK. Ignore it.", this);
                return false;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
