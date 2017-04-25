package com.android.runintest;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.os.SystemProperties;

public class TestService extends Service {

    private static final String TAG = "TestService";

    private static int mBatteryLevel = -1;

    private static boolean mIsDdrTest = false;

    private static boolean mTestEnd = false;

    private static int mCurrentBatteryLevel = -1;

    private static final int COMPARE_BATTTERY = 0;

    private static final int COMPARE_BATTTERY_TIME = 10 * 1000;

    private static TestService sInstance;

    private static SharedPreferences mSharedPreferences = null;
    
    public static final String ACTION_VIBRATE_TEST = "action_vibrate_test";

    public static final String ACTION_LCD_TEST = "action_lcd_test";

    public static final String ACTION_VEDIO_TEST = "action_vedio_test";

    public static final String ACTION_INTEGRATED_TEST = "action_integrated_test";

    public static final String ACTION_TP_TEST = "action_tp_test";

    public static final String ACTION_EMMC_TEST = "action_emmc_test";

    public static final String ACTION_CAMERA_TEST = "action_carmera_test";

    public static final String ACTION_DDR_TEST = "action_ddr_test";

    public static final String ACTION_NFC_TEST = "action_nfc_test";

    public static final String ACTION_FPS_TEST = "action_fps_test";
    
    public static final String ACTION_OPEN_CAMERA = "action_open_camera";

    public static final String ACTION_SHOW_RESULT = "action_show_result";

    public static final String ACTION_SHOW_RUNINTEST_RESULT = "action_show_runintest_result";
    
    public static final String ACTION_AUDIO_TEST = "action_audio_test";
    
    public static final String ACTION_CHARGE_TEST = "action_charge_test";
    
    public static final String ACTION_REBOOT_TEST = "action_reboot_test";

    public static final String ACTION_LIGHTSENOR_TEST = "action_lightsenor_test";

    private static int mMaxBatteryLevel = 75;

    private static int mMinBatteryLevel = 45;
    
    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        initBroadcastReceiver();
        mSharedPreferences = this.getSharedPreferences("runintest", Activity.MODE_PRIVATE);
        loadSharePrefs();
        mBatteryHandler.sendEmptyMessage(COMPARE_BATTTERY);
    }

    public static TestService getInstance() {
        return sInstance;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        this.unregisterReceiver(mTestReceiver);
        mBatteryHandler.removeCallbacksAndMessages(null);
        LogRuningTest.printInfo(TAG, "onDestroy", sInstance);
        SystemProperties.set("ctl.start", "charging_enable");
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        // TODO Auto-generated method stub
        super.unbindService(conn);
    }

    private void initBroadcastReceiver() {
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryLevelFilter.addAction(ACTION_VIBRATE_TEST);
        batteryLevelFilter.addAction(ACTION_LCD_TEST);
        batteryLevelFilter.addAction(ACTION_VEDIO_TEST);
        batteryLevelFilter.addAction(ACTION_INTEGRATED_TEST);
        batteryLevelFilter.addAction(ACTION_DDR_TEST);
        batteryLevelFilter.addAction(ACTION_EMMC_TEST);
        batteryLevelFilter.addAction(ACTION_CAMERA_TEST);
        batteryLevelFilter.addAction(ACTION_NFC_TEST);
        batteryLevelFilter.addAction(ACTION_FPS_TEST);
        batteryLevelFilter.addAction(ACTION_OPEN_CAMERA);
        batteryLevelFilter.addAction(ACTION_SHOW_RESULT);
        batteryLevelFilter.addAction(ACTION_SHOW_RUNINTEST_RESULT);
        batteryLevelFilter.addAction(ACTION_TP_TEST);
        batteryLevelFilter.addAction(ACTION_AUDIO_TEST);
        batteryLevelFilter.addAction(ACTION_CHARGE_TEST);
        batteryLevelFilter.addAction(ACTION_REBOOT_TEST);
        batteryLevelFilter.addAction(ACTION_LIGHTSENOR_TEST);
        registerReceiver(mTestReceiver, batteryLevelFilter);
        LogRuningTest.printInfo(TAG, "initBroadcastReceiver", sInstance);
    }

    public void loadSharePrefs() {
        mBatteryLevel = mSharedPreferences.getInt("battleryLevel", 0);
        mIsDdrTest = mSharedPreferences.getBoolean("Ddr_test", false);
        mTestEnd = mSharedPreferences.getBoolean("testEnd", false);
        LogRuningTest.printInfo(TAG, "old battery level " + mBatteryLevel, sInstance);
    }

    public int getCurrentBattary() {
        return mCurrentBatteryLevel;
    }

    Handler mBatteryHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case COMPARE_BATTTERY:
                LogRuningTest.printInfo(TAG, "COMPARE_BATTTERY mBatteryLevel = " + mBatteryLevel, sInstance);
                LogRuningTest.printInfo(TAG, "COMPARE_BATTTERY mCurrentBatteryLevel = " + mCurrentBatteryLevel, sInstance);
                if (0 == mBatteryLevel && -1 == mCurrentBatteryLevel) {
                    mBatteryHandler.sendEmptyMessageDelayed(COMPARE_BATTTERY, COMPARE_BATTTERY_TIME);
                    break;
                } else if (-1 == mCurrentBatteryLevel) {
                    mBatteryHandler.sendEmptyMessageDelayed(COMPARE_BATTTERY, COMPARE_BATTTERY_TIME);
                    break;
                } else if (0 == mBatteryLevel) {
                    mBatteryLevel = mCurrentBatteryLevel;
                } else {
                    if (Math.abs(mCurrentBatteryLevel - mBatteryLevel) > 20 && !mIsDdrTest && !mTestEnd) {
                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putBoolean("batteryLevel_test", false);
                        editor.commit();
                        LogRuningTest.printDebug(TAG, "result:battery compare error", sInstance);
                        LogRuningTest.printError(TAG, "reason:differ more than 20", sInstance);
                    } else if (mIsDdrTest) {
                        mIsDdrTest = false;
                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putBoolean("Ddr_test", false);
                        editor.commit();
                    }
                    mBatteryLevel = mCurrentBatteryLevel;
                }
                mBatteryHandler.sendEmptyMessageDelayed(COMPARE_BATTTERY, COMPARE_BATTTERY_TIME);
                break;
            }
            super.handleMessage(msg);
        }
    };

    public static void setMaxBatteryLevel(int batteryLevel) {
        mMaxBatteryLevel = batteryLevel;
    }

    public static void setMinBatteryLevel(int batteryLevel) {
        mMinBatteryLevel = batteryLevel;
    }

    private final BroadcastReceiver mTestReceiver = new BroadcastReceiver() {

    //class BatteryReceiver extends BroadcastReceiver {
    @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            LogRuningTest.printInfo(TAG, "mTestReceiver intent =" + intent.getAction(), context);
            
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                mCurrentBatteryLevel = (level * 100) / scale;
                LogRuningTest.printInfo(TAG, "mTestReceiver mCurrentBatteryLevel  =" + mCurrentBatteryLevel, context);
                if(mCurrentBatteryLevel <= mMinBatteryLevel){
                    SystemProperties.set("ctl.start", "charging_enable");
                }
                if(mCurrentBatteryLevel >= mMaxBatteryLevel){
                    SystemProperties.set("ctl.start", "charging_disable");
                }
                String bms = RuningTestConfig.readFile("/sys/class/power_supply/bms/temp");
                String chgdisabled = RuningTestConfig.readFile(RuningTestConfig.CHARGING_ENABLE_PATCH);
                String charging_enabled = RuningTestConfig.readFile("/sys/class/power_supply/battery/charging_enabled");
                String current_now = RuningTestConfig.readFile("/sys/class/power_supply/bms/current_now");
                String voltage_ocv = RuningTestConfig.readFile("/sys/class/power_supply/bms/voltage_ocv");
                String voltage_now = RuningTestConfig.readFile("/sys/class/power_supply/battery/voltage_now");
                String usb_type = RuningTestConfig.readFile("/sys/class/power_supply/usb/type");
                LogRuningTest.printInfo(TAG, "bms =" + bms, context);
                LogRuningTest.printInfo(TAG, "chgdisabled =" + chgdisabled, context);
                LogRuningTest.printInfo(TAG, "charging_enabled =" + charging_enabled, context);
                LogRuningTest.printInfo(TAG, "current_now =" + current_now, context);
                LogRuningTest.printInfo(TAG, "voltage_ocv =" + voltage_ocv, context);
                LogRuningTest.printInfo(TAG, "voltage_now =" + voltage_now, context);
                LogRuningTest.printInfo(TAG, "usb_type =" + usb_type, context);
            } else {
                Intent intentActivity = null;
                if(ACTION_VIBRATE_TEST.equals(intent.getAction())){
                	intentActivity = new Intent(context, VibrateTestActivity.class);
                }else if (ACTION_LCD_TEST.equals(intent.getAction())) {
                    intentActivity = new Intent(context, LCDTestActivity.class);
                } else if (ACTION_VEDIO_TEST.equals(intent.getAction())) {
                    intentActivity = new Intent(context, VedioTestActivity.class);
                } else if (ACTION_INTEGRATED_TEST.equals(intent.getAction())) {
                    intentActivity = new Intent(context, IntegratedTestActivity.class);
                } else if (ACTION_EMMC_TEST.equals(intent.getAction())) {
                    intentActivity = new Intent(context, EMMCTestActivity.class);
                } else if (ACTION_CAMERA_TEST.equals(intent.getAction())) {
                    intentActivity = new Intent(context, CameraActivity.class);
                } else if (ACTION_NFC_TEST.equals(intent.getAction())) {
                    intentActivity = new Intent(context, NFCTestActivity.class);
                } else if (ACTION_FPS_TEST.equals(intent.getAction())) {
                    intentActivity = new Intent(context, FPSTestActivity.class);
                } else if (ACTION_OPEN_CAMERA.equals(intent.getAction())) {
                    intentActivity = new Intent(context, OpenCameraActivity.class);
                } else if (ACTION_SHOW_RESULT.equals(intent.getAction())) {
                    intentActivity = new Intent(context, ShowReslut.class);
                } else if (ACTION_DDR_TEST.equals(intent.getAction())) {
                    intentActivity = new Intent(context, DDRTestActivity.class);
                } else if (ACTION_SHOW_RUNINTEST_RESULT.equals(intent.getAction())) {
                    intentActivity = new Intent(context, ShowRunInReslut.class);
                } else if (ACTION_TP_TEST.equals(intent.getAction())) {
                    intentActivity = new Intent(context, TPTestActivity.class);
                } else if(ACTION_AUDIO_TEST.equals(intent.getAction())){
                	intentActivity = new Intent(context, AudioTestActivity.class);
                } else if(ACTION_CHARGE_TEST.equals(intent.getAction())){
                	intentActivity = new Intent(context, ChargeTestActivity.class);
                } else if(ACTION_REBOOT_TEST.equals(intent.getAction())){
                	intentActivity = new Intent(context, RebootTestActivity.class);
                } else if(ACTION_LIGHTSENOR_TEST.equals(intent.getAction())){
                    intentActivity = new Intent(context, LightSenorActivity.class);
                }
                boolean needReboot = intent.getBooleanExtra("needReboot", false);
                intentActivity.putExtra("needReboot", needReboot);
                intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentActivity);
            }
        }
    };

}
