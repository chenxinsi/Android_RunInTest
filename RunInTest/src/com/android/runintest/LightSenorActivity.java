package com.android.runintest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.hardware.*;
import android.view.KeyEvent;

public class LightSenorActivity extends BaseActivity{

    private String TAG = "LightSenorActivity";
    private LightSenorActivity lightSenorActivity;
    private Sensor mLightSensor;
    private final SensorEventListener mLightSensorListener;
    private SharedPreferences mSharedPreferences = null;
    private SensorManager mSensorManager;
    private TextView mtv_Light_info;
    private String strLightinfo;
    private boolean minValue,maxValue,flag;
    private static  boolean mLightSenor  = true;
    private int delayTime = 3 * 1000;


    public LightSenorActivity() {
        mLightSensorListener = new SensorListener();
    }

    private void initAllControl() {
        strLightinfo = getString(R.string.test_lightsensor_info);
        mtv_Light_info = (TextView) findViewById(R.id.tv_light_senor_info);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        lightSenorActivity = new LightSenorActivity();
        lightSenorActivity.isMonkeyRunning(TAG, "onCreate", LightSenorActivity.this);
        setContentView(R.layout.lightsenor_test);
        mSharedPreferences = this.getSharedPreferences("runintest", Activity.MODE_PRIVATE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initAllControl();
        TextView textview = mtv_Light_info;
        String s = strLightinfo;
        textview.setText(s);
        SensorManager sensormanager = (SensorManager) getSystemService("sensor");
        mSensorManager = sensormanager;
        Sensor sensor = mSensorManager.getDefaultSensor(5);
        mLightSensor = sensor;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                goToDDRTestActivity();
                //gotoRebootOrShowResultTestActivity();
            }
        }, 5*1000);
    }

    protected void onResume() {
        super.onResume();
        lightSenorActivity.isMonkeyRunning(TAG, "onResume", LightSenorActivity.this);
        minValue =false;
        maxValue = false;
        flag = mSensorManager.registerListener(mLightSensorListener,
                mLightSensor, 3);

        String s = (new StringBuilder()).append("bSucceed is ").append(flag)
                .toString();
    }

    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(mLightSensorListener);
    }

    class SensorListener implements SensorEventListener {

        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        public void onSensorChanged(SensorEvent sensorevent) {
            TextView textview = mtv_Light_info;
            StringBuilder stringbuilder = new StringBuilder();
            String s = strLightinfo;
            StringBuilder stringbuilder1 = stringbuilder.append(s).append("\n");
            float f = sensorevent.values[0];
            String s1 = stringbuilder1.append(f).toString();
            textview.setText(s1);
           if(f != 0 || f == 0){
               mLightSenor = true;
           }else{
               mLightSenor = false;
           }
            LogRuningTest.printDebug(TAG, "mLightSenor = " + mLightSenor, LightSenorActivity.this);
        }
    }

    private void goToDDRTestActivity(){
        LogRuningTest.printInfo(TAG, "goToDDRTestActivity ", this);
        SharedPreferences.Editor editor =  mSharedPreferences.edit();
        if(!mLightSenor){
            editor.putBoolean("lightsenor_test", mLightSenor);
            LogRuningTest.printInfo(TAG, "goToDDRTestActivity  mLightSenor= " + mLightSenor, this);
        }
        editor.commit();
        Intent intent = new Intent(TestService.ACTION_DDR_TEST);
        sendBroadcast(intent);
        finish();
    }

    private void gotoRebootOrShowResultTestActivity() {
        LogRuningTest.printInfo(TAG, "send broadcast", this);
        SharedPreferences.Editor editor =mSharedPreferences.edit();
        editor.putBoolean("rebootStatus", false);
        if (!mLightSenor) {
            editor.putBoolean("lightsenor_test", mLightSenor);
        }
        LogRuningTest.printDebug(TAG, "lightsenor_test = " + mLightSenor, this);
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