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
/* < 0071221 xuyinwen 20150918 begin */
import android.provider.Settings;
/* 0071221 xuyinwen 20150918 end > */

public class DDRTestActivity extends BaseActivity{

    private static final String TAG = "DDRTestActivity";
    private DDRTestActivity dDRTestActivity;

    private static SharedPreferences mSharedPreferences = null;

    private static File RECOVERY_DIR = new File("/cache/recovery");

    private static File COMMAND_FILE = new File(RECOVERY_DIR, "command");

    private static boolean mDDRSuccess = false;

    private static boolean mDDRTestStarted = false;

    private static final int GO_TO_RESULT = 0;

    private static final int DELAY_TIME = 5 * 1000;

    /* < 0068595 xuyinwen 20150824 begin */
    private static final int BATTERY_LEVEL = 70;
    /* 0068595 xuyinwen 20150824 end > */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        dDRTestActivity = new DDRTestActivity();
        dDRTestActivity.isMonkeyRunning(TAG, "onCreate", DDRTestActivity.this);
        Settings.Global.putInt(this.getContentResolver(), "runin_testing", 1);
        mSharedPreferences = this.getSharedPreferences("runintest", Activity.MODE_PRIVATE);
        boolean ddrTestStarted = mSharedPreferences.getBoolean("ddrTestStarted", false);
        if (!ddrTestStarted) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean("ddrTestStarted", true);
            editor.commit();
            createFile();
            write2File();
            saveBatteryLevel();
            goToRecoveryMode();
            //mTestHandler.sendEmptyMessageDelayed(GO_TO_RESULT, DELAY_TIME);
        } else {
            startTestService();
            checkTestResult();
            saveDDRFailLog();
            mTestHandler.sendEmptyMessageDelayed(GO_TO_RESULT, DELAY_TIME);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        dDRTestActivity.isMonkeyRunning(TAG, "onResume", DDRTestActivity.this);
    }

    private void saveBatteryLevel() {
        LogRuningTest.printDebug(TAG, "DDR saveBatteryLevel", this);
        int batteryLevel = TestService.getInstance().getCurrentBattary();
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("battleryLevel", batteryLevel);
        editor.commit();
        /* < 0068595 xuyinwen 20150824 begin */
        if (batteryLevel > BATTERY_LEVEL) {
            SystemProperties.set("persist.usb.chgdisabled", "1");
        } else if (batteryLevel < BATTERY_LEVEL) {
            SystemProperties.set("persist.usb.chgdisabled", "0");
        }
        /* 0068595 xuyinwen 20150824 end > */
    }

    Handler mTestHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GO_TO_RESULT:
                    LogRuningTest.printDebug(TAG, "DDR mTestHandler", DDRTestActivity.this);
                    goToShowResult();
                    break;
            }

            super.handleMessage(msg);
        }
    };

    private void startTestService() {
        LogRuningTest.printDebug(TAG, "DDR startTestService", this);
        Intent testService = new Intent(this, TestService.class);
        this.startService(testService);
        LogRuningTest.printInfo(TAG, "start TestService", this);
    }

    private void goToShowResult() {
        LogRuningTest.printInfo(TAG, "goToShowResult mDDRSuccess =" + mDDRSuccess, this);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("rebootStatus", false);
        /* < 0067632 xuyinwen 20150813 begin */
        if (!mDDRSuccess) {
            editor.putBoolean("DDR_test", mDDRSuccess);
        }
        /* 0067632 xuyinwen 20150813 end > */
        editor.commit();
        //Intent intent = new Intent(TestService.ACTION_SHOW_RESULT);
        //sendBroadcast(intent);
        LogRuningTest.printInfo(TAG, "goToShowResult currentRebootCount = " + mSharedPreferences.getInt("currentRebootCount", 0), this);
        LogRuningTest.printInfo(TAG, "goToShowResult Run_Times = " + mSharedPreferences.getInt("Run_Times",0), this);
        if(mSharedPreferences.getInt("currentRebootCount", 0) >= mSharedPreferences.getInt("Run_Times",0) ){
            Intent intent = new Intent(TestService.ACTION_SHOW_RESULT);
            sendBroadcast(intent);
        }else{
            Intent intent = new Intent(TestService.ACTION_REBOOT_TEST);
            sendBroadcast(intent);
        }
        finish();
    }

    /* < 0072103 xuyinwen 20150922 begin */
    public void saveDDRFailLog() {
        LogRuningTest.printDebug(TAG, "DDR saveDDRFailLog", this);
        if (mDDRSuccess) {
            return;
        }
        String ddrLog = RuningTestConfig.readFile("/data/ddr_log.txt");
        LogRuningTest.printInfo(TAG, "ddrLog:" + ddrLog, this);
    }
    /* 0072103 xuyinwen 20150922 end > */

    public void checkTestResult() {
        LogRuningTest.printDebug(TAG, "DDR checkTestResult", this);
        String res = "";
        BufferedReader br = null;
        try {
             br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File("/data/ddr.txt"))));
            String str = null;
            while ((str = br.readLine()) != null) {
                res += str;
                if (str.contains("pass")) {
                    mDDRSuccess = true;
                }else{
                    mDDRSuccess = false;
                    break;
                }
            }

        } catch (Exception e) {
            res += Log.getStackTraceString(e);
        } finally {
            try {
                if(null != br) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mDDRSuccess) {
            LogRuningTest.printDebug(TAG, "result:DDR test success", this);
        } else {
            LogRuningTest.printDebug(TAG, "result:DDR test fail", this);
            LogRuningTest.printError(TAG, "reason:" + res, this);
        }
    }

    public void createFile() {
        LogRuningTest.printDebug(TAG, "DDR createFile", this);
        File dir = new File("/data/ddr_parm");
        if (!dir.exists()) {
            try {
                dir.createNewFile();
                FileUtils.setPermissions(dir.getPath(), 0777, -1, -1);
            } catch (Exception e) {
                return;
            }
        }
    }

    private void write2File() {
        LogRuningTest.printDebug(TAG, "DDR write2File", this);
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter("/data/ddr_parm", false);
            bw = new BufferedWriter(fw);
            bw.write("10M");
            bw.newLine();
            bw.flush();
            bw.close();
            fw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            try {
                bw.close();
                fw.close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
            }
        }
    }

    public void goToRecoveryMode() {
        LogRuningTest.printDebug(TAG, "DDR goToRecoveryMode", this);
        String filename = "/system/etc/ddr_test.zip";
        String arg = "--ddr_tester";
        //String arg = "--update_package=" + filename +"\n";
        RECOVERY_DIR.mkdirs();
        COMMAND_FILE.delete();
        FileWriter command = null;
        try {
            command = new FileWriter(COMMAND_FILE);
            command.write(arg);
            command.write("\n");
            command.flush();
            command.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            try {
                command.close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
            }
        }
        Intent intent = new Intent(Intent.ACTION_REBOOT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Intent.EXTRA_REASON, "age-recovery");
        this.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /* < 0067427 xuyinwen 20150811 begin */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                /* < 0068265 xuyinwen 20150819 begin */
                LogRuningTest.printInfo(TAG, "User click KEYCODE_BACK. Ignore it.", this);
                return false;
            default:
                break;
                /* 0068265 xuyinwen 20150819 end > */
        }
        return super.onKeyDown(keyCode, event);
    }
    /* 0067427 xuyinwen 20150811 end > */
}
