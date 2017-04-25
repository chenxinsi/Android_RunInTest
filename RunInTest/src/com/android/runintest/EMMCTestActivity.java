package com.android.runintest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;  

import android.util.Log;
import android.os.Handler;
import android.os.Message;
import android.content.Intent;
import android.os.PowerManager;
import android.content.SharedPreferences;
/* < 0067427 xuyinwen 20150811 begin */
import android.view.KeyEvent;
/* 0067427 xuyinwen 20150811 end > */

public class EMMCTestActivity extends BaseActivity{

    private static final String TAG = "EMMCTestActivity";
    private EMMCTestActivity eMMCTestActivity;

    private static final String FILE_DIR = "/storage/emulated/0/";
    private static final String FILE_NAME = "emmc.txt";

    private static boolean mStorageState = false;

    private static List<int[]> mFillData = new ArrayList<int[]>();
    private static List<int[]> mReadData = new ArrayList<int[]>();

    private static final int FILL_DATA_SIZE = 0x80;

    private static final int TEST_TIME = 20 * 60 * 1000;

    private static final int TEST_TIMES = 50;

    private static final int EMMC_TEST_START = 0;

    private static Long mTestTime = 0L;

    private static int mTestLoopCount = 0;

    private PowerManager mPowerManager;

    private boolean mTestSuccess = true;

    private SharedPreferences mSharedPreferences;

    private PowerManager.WakeLock mWl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        eMMCTestActivity = new EMMCTestActivity();
        eMMCTestActivity.isMonkeyRunning(TAG, "onCreate", EMMCTestActivity.this);
        mTestTime = System.currentTimeMillis();
        mSharedPreferences = this.getSharedPreferences("runintest", Activity.MODE_PRIVATE);
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWl = mPowerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                PowerManager.ON_AFTER_RELEASE, "bright");
        mStorageState = checkExternalStorageState();
        if (mStorageState) {
            mEMMCHandler.sendEmptyMessageDelayed(EMMC_TEST_START, 0);
        } else {
            mTestSuccess = false;
            //gotoRebootOrShowResultTestActivity();
            mGoToLightSensorActivity();
        }
    }

    public void onResume() {
        super.onResume();
        eMMCTestActivity.isMonkeyRunning(TAG, "onResume", EMMCTestActivity.this);
        if (mWl != null) {
            mWl.acquire();
        }
    }

    Handler mEMMCHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case EMMC_TEST_START:
                startTest();
                break;
            }
            super.handleMessage(msg);
        }
    };

    private void startTest() {
        mTestLoopCount++;
        initFillData();
        createFile();
        mTestSuccess = write2File();
        mTestSuccess = readAndCompareData();
        releaseData();
        nextLoop();
    }

    private static void releaseData() {
        mFillData.clear();
        mReadData.clear();
    }

    private void nextLoop() {
        Long testTime = System.currentTimeMillis() - mTestTime;
        if (true == mTestSuccess && testTime < TEST_TIME && mTestLoopCount < TEST_TIMES) {
            mEMMCHandler.sendEmptyMessageDelayed(EMMC_TEST_START, 0);
        } else {
        	//gotoRebootOrShowResultTestActivity();
            mGoToLightSensorActivity();
        }
    }

	
    private void gotoRebootOrShowResultTestActivity() {
        LogRuningTest.printInfo(TAG, "send broadcast", this);
        SharedPreferences.Editor editor =mSharedPreferences.edit();
        editor.putBoolean("rebootStatus", false);
        if (!mTestSuccess) {
            editor.putBoolean("EMMC_test", mTestSuccess);
        }
        editor.commit();
        
        if(mSharedPreferences.getInt("currentRebootCount", 0) != mSharedPreferences.getInt("Run_Times",0) ){
        	Intent intent = new Intent(TestService.ACTION_REBOOT_TEST);
            sendBroadcast(intent);
        }else{
        	Intent intent = new Intent(TestService.ACTION_SHOW_RESULT);
            sendBroadcast(intent);
        }
        
        finish();
    }

    /*baiwenxin 20161207 Aging add ddr test.  begin*/
    private void mGoToLightSensorActivity(){
        LogRuningTest.printInfo(TAG, "mGoToLightSensorActivity ", this);
        SharedPreferences.Editor editor =  mSharedPreferences.edit();
        if(!mTestSuccess){
            editor.putBoolean("EMMC_test", mTestSuccess);
        }
        editor.commit();
        Intent intent = new Intent(TestService.ACTION_LIGHTSENOR_TEST);
        sendBroadcast(intent);
        finish();
    }

    /*baiwenxin 20161207 Aging add ddr test.  end*/

    private static void initFillData() {
        for (int i=0; i< 11; i++) {
            int dataSize = FILL_DATA_SIZE*(int)Math.pow(2, i);
            int[] fillData = new int[dataSize];
            int[] readData = new int[dataSize];
            for (int j=0; j<dataSize; j++) {
                fillData[j] = j;
            }
            mFillData.add(fillData);
            mReadData.add(readData);
        }
    }

    private boolean checkExternalStorageState() {
        String extStorageState = Environment.getExternalStorageState();
        LogRuningTest.printInfo(TAG, "checkExternalStorageState extStorageState is " + extStorageState, this);
        if (extStorageState.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    private static void createFile() {
        File dir = new File(FILE_DIR + FILE_NAME);

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

    private boolean readAndCompareData() {
        DataInputStream input = null;
        try {
            input = new DataInputStream(
                new BufferedInputStream(new FileInputStream(FILE_DIR + FILE_NAME)));
            for (int i=0; i<mReadData.size(); i++) {
                for (int j=0; j<mReadData.get(i).length; j++) {
                    mReadData.get(i)[j] = input.readInt();
                }
            }

        } catch (FileNotFoundException fnf) {
            return false;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if(null != input) {
                    input.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (int i=0; i<mReadData.size(); i++) {
            for (int j=0; j<mReadData.get(i).length; j++) {
                if (mReadData.get(i)[j] != mFillData.get(i)[j]) {
                    LogRuningTest.printDebug(TAG, "result:data compare failed", this);
                    return false;
                }
            }
        }
        LogRuningTest.printDebug(TAG, "result:data compare success", this);
        return true;
    }

    private static boolean write2File() {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(
                            new BufferedOutputStream(new FileOutputStream(FILE_DIR + FILE_NAME, true)));
            for (int i=0; i<mFillData.size(); i++) {
                for (int j=0; j<mFillData.get(i).length; j++) {
                    out.writeInt(mFillData.get(i)[j]);
                }
            }
        } catch (FileNotFoundException fnf) {
            return false;
        } catch (IOException e) {  
            // TODO Auto-generated catch block   
            e.printStackTrace();
            return false;
        } finally {
            try {
                if(null != out) {
                    out.flush();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mStorageState) {
            File file = new File(FILE_DIR + FILE_NAME);
            if (file.exists()) {
                file.delete();
            }
        }
        removeHandler();
        if (mWl != null) {
            mWl.release();
        }
    }

    private void removeHandler() {
        mEMMCHandler.removeCallbacksAndMessages(null);
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
