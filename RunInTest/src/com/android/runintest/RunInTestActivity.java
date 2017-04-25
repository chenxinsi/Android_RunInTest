package com.android.runintest;

import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.InputType;
import android.text.method.NumberKeyListener;
import android.view.View;


import android.widget.EditText;
import android.os.UserHandle;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.app.AlertDialog;
import com.android.internal.widget.LockPatternUtils;
import com.hymost.util.CommonUtil;


public class RunInTestActivity extends BaseActivity{

    private static final String TAG = "RunInTestActivity";

    private static int mRebootCount = 0 ;

    private static SharedPreferences mSharedPreferences = null;

    private LockPatternUtils mLockPatternUtils;
    
    private static int runTimes ;

    private  EditText inputRebootCount ;

    private class CancelTestingListener implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int whichButton) {
            dialog.dismiss();
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean("startRunInTest", false);
            editor.commit();
            Intent startBattaryService = new Intent(RunInTestActivity.this, TestService.class);
            RunInTestActivity.this.stopService(startBattaryService);
            RunInTestActivity.this.finish();
        }
    }


    private class CheckStartListener implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int whichButton) {
            dialog.dismiss();
            if(inputRebootCount.getText().toString() == null || 
 			       inputRebootCount.getText().toString().length() == 0 ){
            	runTimes = 1;
		 	}else{
		 		runTimes = Integer.parseInt(inputRebootCount.getText().toString());
		 	}
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putInt("Run_Times", runTimes);
            editor.commit();
            goToRebootTestActivity();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_in_test);
        LogRuningTest.printDebug(TAG, "onCreate  start  RunInTestActivity ", this);
        mSharedPreferences = this.getSharedPreferences("runintest", Activity.MODE_PRIVATE);
        mLockPatternUtils = new LockPatternUtils(this);
        initRunInTest();
        unLock();
        CommonUtil.startTestService(this, TAG);
        
        inputRebootCount = new EditText(RunInTestActivity.this);
        inputRebootCount.setFocusable(true);
        inputRebootCount.setText("10");
        inputRebootCount.setVisibility(View.INVISIBLE);
        inputRebootCount.setKeyListener(new NumberKeyListener() {

            @Override
            public int getInputType() {
                return InputType.TYPE_CLASS_PHONE;
            }

            @Override
            protected char[] getAcceptedChars() {
                return new char[]{'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
            }
        });
        
            AlertDialog checkStartTest = new AlertDialog.Builder(RunInTestActivity.this)
                    .setTitle(R.string.check_title)
                    .setView(inputRebootCount)
                    .setCancelable(false)
                    .setMessage(R.string.check_body)
                    .setPositiveButton(R.string.no, new CheckStartListener())
                    .setNegativeButton(R.string.yes, new CancelTestingListener())
                    .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CommonUtil.acquireWakeLock(TAG, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        CommonUtil.releaseWakeLock(TAG, this);
    }

    private void unLock() {
        LogRuningTest.printInfo(TAG, "UnLock screen ", this);
        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
        KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("");
        keyguardLock.disableKeyguard();
        mLockPatternUtils.setLockScreenDisabled(true, UserHandle.myUserId());
        Intent intent = new Intent("com.tcl.mie.launcher3.action.unlock");
        sendBroadcast(intent);
    }

    private void goToRebootTestActivity(){
    	Intent intent = new Intent(TestService.ACTION_REBOOT_TEST);
    	sendBroadcast(intent);
    }

    public void initRunInTest() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("startRunInTest", true);
        if (mRebootCount == 0) {
            editor.putInt("testLoopCount", 1);
            editor.putInt("currentRebootCount", 0);
            editor.putBoolean("reboot_test", true);
            editor.putBoolean("LCD_test", true);
            editor.putBoolean("TP_test", true);
            editor.putBoolean("audio_test", true);
            editor.putBoolean("integrated_test", true);
            editor.putBoolean("Bluetooth", true);
            editor.putBoolean("Wifi", true);
            editor.putBoolean("GPS", true);
            editor.putBoolean("Gravity", true);
            editor.putBoolean("Light", true);
            editor.putBoolean("Keypad", true);
            editor.putBoolean("Camera", true);
            editor.putBoolean("MIC", true);
            editor.putBoolean("Proximity", true);
            editor.putBoolean("video_test", true);
            editor.putBoolean("camera_test", true);
            editor.putBoolean("EMMC_test", true);
            editor.putBoolean("batteryLevel_test", true);
            editor.putBoolean("NFC_test", true);
            editor.putBoolean("FPS_test", true);
            editor.putBoolean("DDR_test", true);
            editor.putBoolean("rebootStatus", false);
            editor.putBoolean("vibrator_test",true);
            editor.putBoolean("audio_mic_test", true);
            editor.putBoolean("charge_test", true);
            editor.putBoolean("lightsenor_test",true);
            LogRuningTest.CreateText(this);
        }
        editor.commit();
    }

}
