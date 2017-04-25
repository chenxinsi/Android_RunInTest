package com.android.runintest;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.KeyguardManager.KeyguardLock;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.os.SystemProperties;
import android.media.AudioManager;
/* < 0067427 xuyinwen 20150811 begin */
import android.view.KeyEvent;
/* 0067427 xuyinwen 20150811 end > */

public class IntegratedTestActivity extends Activity {

    private final int ENTER_INTO_DORMANCY = 0;
    private final int WAKING_UP_FROM_DORMANCY = 1;
    private final int FINISH_DORMANCY = 2;
    private final int FRIST_SLEEP_TIME = 60 * 1000;
    private final int SECOND_SLEEP_TIME = 60 * 1000;
    private final int THIRD_SLEEP_TIME = 60 * 1000;

    private final int OPEN_TIME = 2000;

    private final int NEXT_TEST_TIME = 1000;

    private final int GO_TO_CAMERA_TIME = 5 * 1000;

    private final int OPEN_BLUETOOTH = 1000;

    private final int OPEN_WIFI = 1001;

    private final int OPEN_GPS = 1002;

    private final int OPEN_GRAVITY = 1003;

    private final int OPEN_COMPASS = 1004;

    private final int OPEN_PROXIMITY = 1005;

    private final int OPEN_ENVIROMENT_LIGHT = 1006;

    private final int OPEN_KEYPAD = 1007;

    private final int CLOSE_KEYPAD = 1011;

    private final int OPEN_CAMERA = 1008;

    private final int OPEN_MIC = 1009;

    private final int GO_TO_SLEEP = 1010;

    private final int GO_TO_CAMERA = 1012;

    private final String ACTION_WAKING_UP_FROM_DORMANCY = "action_waking_up_from_dormancy";

    private final String TAG = "IntegratedTestActivity";

    private int mSleepCount = 0;

    private static final int SLEEP_TIMES = 1;

    private float mValues[];

    PowerManager mPowerManager;
    BluetoothAdapter mBluetoothAdapter;
    PowerManager.WakeLock mWakeLock;
    AlarmManager mAlarmManager;
    Intent mAlarmManagerIntent;
    PendingIntent mAlarmManagerPendingIntent;
    IntentFilter mWakingUpFilter;

    WifiManager wifiManager;
    private SensorManager mGravitySensorManager;  
    private SensorManager mCompassSensorManager; 
    private Sensor mGravitySensor;  
    private Sensor mCompassSensor;
    private SensorListener mGavitySensorListener;
    private SensorEventListener mCompassSensorListener;
    private Sensor mProimitySensor;
    private SensorEventListener mProximityListener;
    private SensorManager mProximitySensorManager;
    private Sensor mLightSensor;
    private SensorEventListener mLightSensorListener;
    private SensorManager mLightSensorManager;
    private boolean mNeedReboot;
    private int mLoopCount = 0;
    
    private final int REBOOT_COUNT = 1;
    private String configPath = "/sys/class/leds/button-backlight/brightness";

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor editor;
    

    private int mOpenCount = 0;

    private static boolean mBluetoothTest = true;
    private static boolean mWifiTest = true;
    private static boolean mGPSTest = true;
    private static boolean mGravityTest = true;
    //private boolean mCompassTest = true;
    private static boolean mLightTest = true;
    private static boolean mKeypadTest = true;
    private static boolean mCameraTest = true;
    private static boolean mMICTest = true;
    private static boolean mProximityTest = true;
    /* < 0078018 xuyinwen 20151215 begin */
    private static boolean mCameraTestRetry = true;
    /* 0078018 xuyinwen 20151215 end > */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        LogRuningTest.printInfo(TAG, "start integrated test", this);
        mSharedPreferences = this.getSharedPreferences("runintest", Activity.MODE_PRIVATE);
        editor = mSharedPreferences.edit();
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        mAlarmManagerIntent = new Intent(ACTION_WAKING_UP_FROM_DORMANCY);
        mAlarmManagerPendingIntent = PendingIntent.getBroadcast(this, 0, mAlarmManagerIntent, 0);
        mWakingUpFilter = new IntentFilter(ACTION_WAKING_UP_FROM_DORMANCY);
        registerReceiver(mWakingUpReceiver, mWakingUpFilter);
        goToSleep(FRIST_SLEEP_TIME);
        loadReBootCount();
        Intent intent = getIntent();
        mNeedReboot = intent.getBooleanExtra("needReboot", false);
    }

    private void goToSleep(int time) {
        mSleepCount++;
        if (mWakeLock != null) {
            mWakeLock.release();
        }
        mPowerManager.goToSleep(SystemClock.uptimeMillis());
        wakingUpFromDormancyTime(this, time);
    }

    private void wakingUpFromDormancy() {
        mWakeLock = mPowerManager.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
        if (mWakeLock != null) {
            mWakeLock.acquire();
        }
        mPowerManager.wakeUp(SystemClock.uptimeMillis());
        unLock();
    }

    private void wakingUpFromDormancyTime(Context context, int time) {
        long imeiTime = SystemClock.elapsedRealtime() + time;
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, imeiTime, mAlarmManagerPendingIntent);
        unLock();
    }

    public static void setCameraTest(boolean success) {
        mCameraTest = success;
    }

    private void deviceDetect() {
        // BT
        mTestHandler.sendEmptyMessage(OPEN_BLUETOOTH);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unregisterReceiver(mWakingUpReceiver);	
        if (mGravitySensorManager != null) {
            mGravitySensorManager.unregisterListener(mGavitySensorListener);                		
        }
        if (mCompassSensorManager != null) {
            mCompassSensorManager.unregisterListener(mCompassSensorListener);                		
        }
        if (mProximitySensorManager != null) {
            mProximitySensorManager.unregisterListener(mProximityListener);                		
        }
        if (mLightSensorManager != null) {
            mLightSensorManager.unregisterListener(mLightSensorListener);                		
        }
        if (mTestHandler != null) {
            mTestHandler.removeCallbacksAndMessages(null);    		
        }
        if (mWakeLock != null) {
            mWakeLock.release();
        }
    }

    Handler mTestHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case OPEN_BLUETOOTH:
                    LogRuningTest.printInfo(TAG, "OPEN_BLUETOOTH", IntegratedTestActivity.this);
                    if (mBluetoothAdapter == null) {
                        LogRuningTest.printDebug(TAG, "result:bluetooth test failed", IntegratedTestActivity.this);
                        LogRuningTest.printError(TAG, "reason:mBluetoothAdapter is null", IntegratedTestActivity.this);
                    } else if (mBluetoothTest && mOpenCount < 2 ) {
                        if (mBluetoothAdapter.isEnabled()) {
                            mBluetoothTest = mBluetoothAdapter.disable();
                            mTestHandler.sendEmptyMessageDelayed(OPEN_BLUETOOTH,
                                OPEN_TIME);
                            mOpenCount++;
                        } else {
                            mBluetoothTest = mBluetoothAdapter.enable();
                            mTestHandler.sendEmptyMessageDelayed(OPEN_BLUETOOTH,
                                OPEN_TIME);
                            mOpenCount++;
                        }
                    }
                    else if (mOpenCount>=2 || !mBluetoothTest) {
                        mTestHandler.sendEmptyMessageDelayed(OPEN_WIFI,
                            NEXT_TEST_TIME);
                        mOpenCount = 0;
                    }
                    break;
                case OPEN_WIFI:
                    LogRuningTest.printInfo(TAG, "OPEN_WIFI", IntegratedTestActivity.this);
                    if (wifiManager ==  null) {
                        LogRuningTest.printDebug(TAG, "result:wifi test failed", IntegratedTestActivity.this);
                        LogRuningTest.printError(TAG, "reason:wifiManager is null", IntegratedTestActivity.this);
                    } else if (mWifiTest && mOpenCount < 2 ) {
                        if (wifiManager.isWifiEnabled()) {
                            mWifiTest = wifiManager.setWifiEnabled(false);
                            mTestHandler.sendEmptyMessageDelayed(OPEN_WIFI, OPEN_TIME);
                            mOpenCount++;
                        } else {
                            mWifiTest = wifiManager.setWifiEnabled(true);
                            mTestHandler.sendEmptyMessageDelayed(OPEN_WIFI, OPEN_TIME);
                            mOpenCount++;
                        }
                    }
                    else if (mOpenCount>=2 || !mWifiTest) {
                        mTestHandler.sendEmptyMessageDelayed(OPEN_GPS,
                            NEXT_TEST_TIME);
                        mOpenCount = 0;
                    }
                    break;
                case OPEN_GPS:
                    LogRuningTest.printInfo(TAG, "OPEN_GPS", IntegratedTestActivity.this);
                    boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(
                    getContentResolver(), LocationManager.GPS_PROVIDER);
                    if (mGPSTest && mOpenCount < 2 ) {
                        if (gpsEnabled) {
                            Settings.Secure.setLocationProviderEnabled(
                                    getContentResolver(), LocationManager.GPS_PROVIDER, false);
                            mGPSTest = Settings.Secure.isLocationProviderEnabled(
                                getContentResolver(), LocationManager.GPS_PROVIDER)
                                ? false:true;
                            mTestHandler.sendEmptyMessageDelayed(OPEN_GPS, OPEN_TIME);
                            mOpenCount++;
                        } else {
                            Settings.Secure.setLocationProviderEnabled(
                            getContentResolver(), LocationManager.GPS_PROVIDER,
                                    true);
                            mGPSTest = Settings.Secure.isLocationProviderEnabled(
                                getContentResolver(), LocationManager.GPS_PROVIDER)
                                ? true:false;
                            mTestHandler.sendEmptyMessageDelayed(OPEN_GPS, OPEN_TIME);
                            mOpenCount++;
                        }
                    }
                    else if (mOpenCount>=2 || !mGPSTest) {
                        mTestHandler.sendEmptyMessageDelayed(OPEN_GRAVITY,
                            NEXT_TEST_TIME);
                        mOpenCount = 0;
                    }
                    break;
                case OPEN_GRAVITY:
                    LogRuningTest.printInfo(TAG, "OPEN_GRAVITY", IntegratedTestActivity.this);
                    mGravitySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);  
                    mGravitySensor = mGravitySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                    mGavitySensorListener  = new GravitySensListener();
                    mGravitySensorManager.registerListener(mGavitySensorListener, SensorManager.SENSOR_DELAY_NORMAL);
                    mTestHandler.sendEmptyMessageDelayed(OPEN_PROXIMITY, OPEN_TIME);
                    break;
                /*case OPEN_COMPASS:
                    //mGravitySensorManager.unregisterListener(mGavitySensorListener);
                    mCompassSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);  
                    mCompassSensor = mCompassSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                    mCompassSensorListener  = new CompassSensorListener();
                    mCompassSensorManager.registerListener(mCompassSensorListener,mCompassSensor, 
                    SensorManager.SENSOR_DELAY_NORMAL);
                    mTestHandler.sendEmptyMessageDelayed(OPEN_PROXIMITY,
                            OPEN_TIME);
                    break;*/
                case OPEN_PROXIMITY:
                    LogRuningTest.printInfo(TAG, "OPEN_PROXIMITY", IntegratedTestActivity.this);
                    mProximitySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                    mProimitySensor = mProximitySensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
                    mProximityListener = new ProximitySenListenerSenListener();
                    mProximityTest = mProximitySensorManager.registerListener(mProximityListener,
                            mProimitySensor, SensorManager.SENSOR_DELAY_NORMAL);
                    mTestHandler.sendEmptyMessageDelayed(OPEN_ENVIROMENT_LIGHT,
                            OPEN_TIME);
                    break;
                case OPEN_ENVIROMENT_LIGHT:
                    LogRuningTest.printInfo(TAG, "OPEN_ENVIROMENT_LIGHT", IntegratedTestActivity.this);
                    mLightSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                    mLightSensor = mLightSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
                    mLightSensorListener = new LightSensorListener();
                    mLightTest = mLightSensorManager.registerListener(mLightSensorListener,
                            mLightSensor, SensorManager.SENSOR_DELAY_NORMAL);
                    mTestHandler.sendEmptyMessageDelayed(OPEN_KEYPAD,
                            OPEN_TIME);
                    break;
                case OPEN_KEYPAD:
                    LogRuningTest.printInfo(TAG, "OPEN_KEYPAD", IntegratedTestActivity.this);
                    SystemProperties.set("ctl.start", "set_openlight");
//                    storeToFile(true);
//                    RuningTestConfig.writeToFile(RuningTestConfig.BRIGHTNESS_PATCH
//                            , RuningTestConfig.OPEN_KEYPAD, TAG, IntegratedTestActivity.this);
                    mTestHandler.sendEmptyMessageDelayed(CLOSE_KEYPAD,
                            OPEN_TIME);
                    break;
                case CLOSE_KEYPAD:
                    LogRuningTest.printInfo(TAG, "CLOSE_KEYPAD", IntegratedTestActivity.this);
                    SystemProperties.set("ctl.start", "set_closelight");
//                    storeToFile(false);
//                    RuningTestConfig.writeToFile(RuningTestConfig.BRIGHTNESS_PATCH
//                            , RuningTestConfig.CLOSE_KEYPAD, TAG, IntegratedTestActivity.this);
                    mTestHandler.sendEmptyMessageDelayed(OPEN_CAMERA,
                            OPEN_TIME);
                    break;
                case OPEN_CAMERA:
                    LogRuningTest.printInfo(TAG, "OPEN_CAMERA", IntegratedTestActivity.this);
                    openCameraActivity();
                    mTestHandler.sendEmptyMessageDelayed(OPEN_MIC,
                            10000);
                    break;
                case OPEN_MIC:
                    /* < 0078018 xuyinwen 20151215 begin */
                    if (!mCameraTest && mCameraTestRetry) {
                        mCameraTestRetry = false;
                        mTestHandler.sendEmptyMessageDelayed(OPEN_CAMERA,
                                OPEN_TIME);
                        return;
                    }
                    mCameraTestRetry = true;
                    /* 0078018 xuyinwen 20151215 end > */
                    LogRuningTest.printInfo(TAG, "OPEN_MIC", IntegratedTestActivity.this);
                    AudioManager audioManager =
                            (AudioManager) IntegratedTestActivity.this.getSystemService(Context.AUDIO_SERVICE);
                    boolean muted = audioManager.isMicrophoneMute();
                    audioManager.setMicrophoneMute(audioManager.isMicrophoneMute()?false:true);
                    boolean newMuted = audioManager.isMicrophoneMute();
                    if (muted == newMuted) {
                        mMICTest = false;
                        LogRuningTest.printDebug(TAG, "result:open MIC test failed", IntegratedTestActivity.this);
                    }
                    mOpenCount++;
                    if (mOpenCount>=2 || !mGPSTest) {
                        mTestHandler.sendEmptyMessageDelayed(GO_TO_SLEEP,
                            NEXT_TEST_TIME);
                        mOpenCount = 0;
                    } else {
                        mTestHandler.sendEmptyMessageDelayed(OPEN_MIC,
                            OPEN_TIME);
                    }
                    break;
                case GO_TO_SLEEP:
                    LogRuningTest.printInfo(TAG, "GO_TO_SLEEP", IntegratedTestActivity.this);
                   	if(mGravitySensorManager != null){
                		mGravitySensorManager.unregisterListener(mGavitySensorListener);                		
                	}
                	if(mCompassSensorManager != null){
                		mCompassSensorManager.unregisterListener(mCompassSensorListener);                		
                	}
                	if(mProximitySensorManager != null){
                		mProximitySensorManager.unregisterListener(mProximityListener);                		
                	}
                	if(mLightSensorManager != null){
                		mLightSensorManager.unregisterListener(mLightSensorListener);                		
                	}
                    if (mSleepCount < SLEEP_TIMES) {
                        goToSleep(SECOND_SLEEP_TIME);
                    } else if (mSleepCount == SLEEP_TIMES) {
                        goToSleep(THIRD_SLEEP_TIME);
                    }
                    break;
                case GO_TO_CAMERA:
                    Intent intent = new Intent(TestService.ACTION_CAMERA_TEST);
                    sendBroadcast(intent);
                    finish();
                    break;
            }

            super.handleMessage(msg);
        }
    };

    private void gotoVideoTestActivity() {
        Intent intent = new Intent(TestService.ACTION_VEDIO_TEST);
        sendBroadcast(intent);
        finish();
    }
    
    private void openCameraActivity() {
        Intent intent = new Intent(TestService.ACTION_OPEN_CAMERA);
        sendBroadcast(intent);
    }

    private void gotoCamerTestActivity() {
        LogRuningTest.printInfo(TAG, "send broadcast", this);
        mTestHandler.sendEmptyMessageDelayed(GO_TO_CAMERA,
                            GO_TO_CAMERA_TIME);
    }

    public boolean storeToFile(boolean open) {
        String exception = null;
        try {
            FileOutputStream outStream = new FileOutputStream(configPath);
            String keyPad = (open ? "1" : "0");
            outStream.write(keyPad.getBytes());
            outStream.close();
            return true;
        } catch (FileNotFoundException e) {
            exception = Log.getStackTraceString(e);
        } catch (IOException e) {
            exception = Log.getStackTraceString(e);
        }
        if (null != exception) {
            LogRuningTest.printDebug(TAG, "result:Keypad test failed", this);
            LogRuningTest.printError(TAG, "reason:" + exception, this);
            mKeypadTest = false;
        }
        return false;
    }

    private void unLock(){
        LogRuningTest.printInfo(TAG, "unLock", this);
        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
        KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("");
        keyguardLock.disableKeyguard();
        Intent intent = new Intent("com.tcl.mie.launcher3.action.unlock");
        sendBroadcast(intent);
    }

    /*class CompassSensorListener implements SensorEventListener {

        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        public void onSensorChanged(SensorEvent sensorevent) {
            float axisX = sensorevent.values[0];
            float axisY = sensorevent.values[1];
            float axisZ = sensorevent.values[2];
            if(axisX == 0 && axisY == 0 && axisZ == 0){
                mCompassTest = false;
            }else{
                mCompassTest = true;
            }
        }
    }*/


    private class GravitySensListener implements SensorListener {

        public void onAccuracyChanged(int i, int j) {
        }

        public void onSensorChanged(int i, float af[]) {
            mValues = af;
            float f = mValues[0];
            float f1 = mValues[1];
            float f2 = mValues[2];
            if(f == 0 && f1 == 0 && f2 == 0){
                mGravityTest = false;
            }else{
                mGravityTest = true;
            }
        }

    }

    private class ProximitySenListenerSenListener implements SensorEventListener {

        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        public void onSensorChanged(SensorEvent sensorevent) {
            float f = sensorevent.values[0];
            LogRuningTest.printInfo(TAG, "ProximitySenListenerSenListener " + f, IntegratedTestActivity.this);
        }

    }

    class LightSensorListener implements SensorEventListener {

        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        public void onSensorChanged(SensorEvent sensorevent) {
            float f = sensorevent.values[0];
            LogRuningTest.printInfo(TAG, "LightSensorListener " + f, IntegratedTestActivity.this);
        }
    }

    public void reBootCompleteRunInTest() {
        saveReBootCount();
        Intent intent = new Intent(Intent.ACTION_REBOOT);
        intent.setAction(Intent.ACTION_REBOOT);
        intent.putExtra("nowait", 1);
        intent.putExtra("interval", 1);
        intent.putExtra("window", 0);
        sendBroadcast(intent);
    }

    public void loadReBootCount() {       
        mLoopCount = mSharedPreferences.getInt("testLoopCount", 1);
    }

    /* < 0071219 xuyinwen 20150918 begin */
    public void saveTestResult() {
        int batteryLevel = TestService.getInstance().getCurrentBattary();
        boolean integratedTest = false;
        if (mBluetoothTest && mWifiTest && mGPSTest && mGravityTest && mLightTest
                && mKeypadTest && mCameraTest && mMICTest && mProximityTest) {
            integratedTest = true;
        }
        LogRuningTest.printInfo(TAG, "Bluetooth " + mBluetoothTest, this);
        LogRuningTest.printInfo(TAG, "Wifi " + mWifiTest, this);
        LogRuningTest.printInfo(TAG, "GPS " + mGPSTest, this);
        LogRuningTest.printInfo(TAG, "Gravity " + mGravityTest, this);
        LogRuningTest.printInfo(TAG, "Light " + mLightTest, this);
        LogRuningTest.printInfo(TAG, "Keypad " + mKeypadTest, this);
        LogRuningTest.printInfo(TAG, "Camera " + mCameraTest, this);
        LogRuningTest.printInfo(TAG, "MIC " + mMICTest, this);
        LogRuningTest.printInfo(TAG, "Proximity " + mProximityTest, this);
        if (!mBluetoothTest) {
            editor.putBoolean("Bluetooth", mBluetoothTest);
        }
        if (!mWifiTest) {
            editor.putBoolean("Wifi", mWifiTest);
        }
        if (!mGPSTest) {
            editor.putBoolean("GPS", mGPSTest);
        }
        if (!mGravityTest) {
            editor.putBoolean("Gravity", mGravityTest);
        }
        if (!mLightTest) {
            editor.putBoolean("Light", mLightTest);
        }
        if (!mKeypadTest) {
            editor.putBoolean("Keypad", mKeypadTest);
        }
        if (!mCameraTest) {
            editor.putBoolean("Camera", mCameraTest);
        }
        if (!mMICTest) {
            editor.putBoolean("MIC", mMICTest);
        }
        if (!mProximityTest) {
            editor.putBoolean("Proximity", mProximityTest);
        }
        /* 0067705 xuyinwen 20150813 end > */
        editor.putInt("battleryLevel", batteryLevel);
        if (!integratedTest) {
            editor.putBoolean("integrated_test", integratedTest);
        }
        /* 0067632 xuyinwen 20150813 end > */
        editor.commit();
    }
    /* 0071219 xuyinwen 20150918 end > */

    public void saveReBootCount() {
        editor.putInt("testLoopCount", ++mLoopCount);
        editor.commit();
    }

    private final BroadcastReceiver mWakingUpReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_WAKING_UP_FROM_DORMANCY.equals(intent.getAction())) {
                LogRuningTest.printInfo(TAG, "mWakingUpReceiver sleepCount =" + mSleepCount, context);            
                if (mSleepCount > SLEEP_TIMES) {
                    wakingUpFromDormancy();
                    LogRuningTest.printInfo(TAG, "mWakingUpReceiver mLoopCount =" + mLoopCount, context);
                    saveTestResult();
                    if (mNeedReboot && mLoopCount < REBOOT_COUNT) {
                        reBootCompleteRunInTest();
                    } else {
                        if (mNeedReboot) {
                            gotoCamerTestActivity();
                        } else {
                            gotoVideoTestActivity();
                        }
                    }
                } else {
                    wakingUpFromDormancy();
                    deviceDetect();
                }
            }
        }
    };

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
