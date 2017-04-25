package com.android.runintest;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;
import android.app.Activity;
import android.os.SystemClock;

public class RunInTestReceiver extends BroadcastReceiver {

    private static final String TAG = "RunInTestReceiver";
    private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        if (intent.getAction().equals(ACTION_BOOT)) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("runintest", Activity.MODE_PRIVATE);
            boolean runInTestStarted = sharedPreferences.getBoolean("startRunInTest", false);
            boolean ddrTestStarted = sharedPreferences.getBoolean("ddrTestStarted", false);
            /* < 0071219 xuyinwen 20150918 begin */
            long rebootTime = SystemClock.elapsedRealtime();
            LogRuningTest.printInfo(TAG, "rebootTime " + rebootTime, context);
            /* 0071219 xuyinwen 20150918 end > */
            LogRuningTest.printInfo(TAG, "runInTestStarted " + runInTestStarted, context);

            if (runInTestStarted) {
                Intent startRebootTestActivity = new Intent(context, RebootTestActivity.class);
                startRebootTestActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(startRebootTestActivity);
            }
        }
    }
	

}
