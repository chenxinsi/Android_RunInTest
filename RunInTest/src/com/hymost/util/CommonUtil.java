package com.hymost.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import com.android.runintest.LogRuningTest;
import com.android.runintest.TestService;

import java.util.List;

/**
 * Created by xinsi on 17-4-19.
 */
public class CommonUtil {
    public static PowerManager.WakeLock wakeLock;

    public static Intent testService;
    //Get power lock, Keep the screen bright
    public static  void acquireWakeLock(String TAG, Context context){

        if(wakeLock == null){
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                    | PowerManager.ACQUIRE_CAUSES_WAKEUP, "wakeLock my Tag");
            if(wakeLock != null){
                LogRuningTest.printInfo(TAG, "acquireWakeLock", context);
                wakeLock.acquire();
            }
        }
    }

    //Release power lock
    public static void releaseWakeLock(String TAG, Context context){

        if(wakeLock != null && wakeLock.isHeld()){
            LogRuningTest.printInfo(TAG, "releaseWakeLock", context);
            wakeLock.release();
            wakeLock = null;
        }

    }

    //Stop TestService
    public static void stopTestService(Context mContext, String TAG){
        String className = "com.android.runintest.TestService";
        LogRuningTest.printInfo(TAG, "TestService isRunning:" +
                isRunningTestService(mContext, className), mContext);
        if(isRunningTestService(mContext, className)){
            if(testService != null) {
                mContext.stopService(testService);
                LogRuningTest.printInfo(TAG, TAG + "stop TestService", mContext);
            }
        }
    }

    //Open TestService
    public static void startTestService(Context mContext, String TAG) {

        String className = "com.android.runintest.TestService";
        LogRuningTest.printInfo(TAG, "TestService isRunning:" +
                isRunningTestService(mContext, className), mContext);
        if(!isRunningTestService(mContext, className)) {
            testService = new Intent(mContext, TestService.class);
            mContext.startService(testService);
            LogRuningTest.printInfo(TAG, TAG + "start TestService", mContext);
        }

    }

    //Determine whether the TestService service is started
    public static boolean isRunningTestService(Context mContext, String className){

        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(40);

        if (serviceList.size() <= 0) {
            return false;
        }

        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

}
