package com.android.runintest;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.app.Activity;
import android.os.SystemClock;

public class RunInTestReceiver extends BroadcastReceiver {

    private static final String TAG = "RunInTestReceiver";
    private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_BOOT)) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("runintest", Activity.MODE_PRIVATE);
            boolean runInTestStarted = sharedPreferences.getBoolean("startRunInTest", false);
            long rebootTime = SystemClock.elapsedRealtime();
            LogRuningTest.printInfo(TAG, "rebootTime " + rebootTime, context);
            LogRuningTest.printInfo(TAG, "runInTestStarted " + runInTestStarted, context);

            if (runInTestStarted) {
                Intent startRebootTestActivity = new Intent(context, RebootTestActivity.class);
                startRebootTestActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(startRebootTestActivity);
            }
        }
    }
}
