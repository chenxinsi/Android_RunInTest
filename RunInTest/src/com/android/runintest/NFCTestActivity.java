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
/* < 0067427 xuyinwen 20150811 begin */
import android.view.KeyEvent;
/* 0067427 xuyinwen 20150811 end > */
/* < 0068595 xuyinwen 20150824 begin */
import android.os.SystemProperties;
/* 0068595 xuyinwen 20150824 end > */
import android.nfc.NfcAdapter;

public class NFCTestActivity extends Activity{

    private static final String TAG = "NFCTestActivity";

    private static SharedPreferences mSharedPreferences = null;

    private static File RECOVERY_DIR = new File("/dev/nq-nci");

    private static boolean mNFCSuccess = false;
    
    private static final int START_TEST = 0;
    private static final int CHECK_RESULT = 1;
    private static final int GO_TO_RESULT = 2;

    private static final int DELAY_TIME = 10 * 1000;

    private static final int STOP_TIME = 10 * 60 * 1000;

    /* < 0068595 xuyinwen 20150824 begin */
    private static final int BATTERY_LEVEL = 70;
    /* 0068595 xuyinwen 20150824 end > */

    private static Long mTestTime = 0L;
    private NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mSharedPreferences = this.getSharedPreferences("runintest", Activity.MODE_PRIVATE);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mNfcAdapter.disable();
        mTestTime = System.currentTimeMillis();
        mTestHandler.sendEmptyMessageDelayed(START_TEST, DELAY_TIME);
    }

    private static void createFile() {
        File dir = new File("sdcard/log/RunningTestII/nfc_test.txt");

        if (dir.exists()) {
            dir.delete();
        }

        if (!dir.exists()) {  
            try {  
                dir.createNewFile();  
            } catch (Exception e) {
            }  
        }  
    }

    Handler mTestHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_TEST:
                    createFile();
                    SystemProperties.set("ctl.start", "nfc_test");
                    mTestHandler.sendEmptyMessageDelayed(CHECK_RESULT, DELAY_TIME);
                    break;
                case CHECK_RESULT:                    
                    checkTestResult();
                    Long testTime = System.currentTimeMillis() - mTestTime;
                    if (mNFCSuccess && testTime < STOP_TIME) {
                        mTestHandler.sendEmptyMessage(START_TEST);
                    } else {
                        mNfcAdapter.enable();
                        mTestHandler.sendEmptyMessageDelayed(GO_TO_RESULT, DELAY_TIME);
                    }
                    break;
                case GO_TO_RESULT:
                    goToFPSTest();
                    break;
            }

            super.handleMessage(msg);
        }
    };

    private void goToFPSTest() {
        LogRuningTest.printInfo(TAG, "send broadcast", this);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
     //   if (!mNFCSuccess) {
     //       editor.putBoolean("NFC_test", mNFCSuccess);
     //   }
     //   editor.commit();
        Intent intent = new Intent(TestService.ACTION_FPS_TEST);
        sendBroadcast(intent);
        finish();
    }

    public void checkTestResult() {
        String res = "";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File("sdcard/log/RunningTestII/nfc_test.txt"))));
            String str = null;
            while ((str = br.readLine()) != null) {
                LogRuningTest.printInfo(TAG, "STR=" + str + "   " + 
                str.length(), this);
                res += str;
            }
            if (res.equals("400003001101")) {
                mNFCSuccess = true;
            }else{
                mNFCSuccess = false;
            }
            br.close();
        } catch (Exception e) {
            res += Log.getStackTraceString(e);
        }
        if (mNFCSuccess) {
            LogRuningTest.printDebug(TAG, "result:NFC test success", this);
        } else {
            LogRuningTest.printDebug(TAG, "result:NFC test fail", this);
            LogRuningTest.printError(TAG, "reason:" + res, this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTestHandler.removeCallbacksAndMessages(null);
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
