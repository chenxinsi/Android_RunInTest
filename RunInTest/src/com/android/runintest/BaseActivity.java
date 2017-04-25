package com.android.runintest;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.view.KeyEvent;


public class BaseActivity extends Activity {
	
	public void isMonkeyRunning(String tag, String s, Context context) {
		//如果在跑monkey, 则finish掉activity；
        if(ActivityManager.isUserAMonkey()){
         LogRuningTest.printInfo(tag, "---isMonkeyRunning", context);
         Activity activity =(Activity)context;
         activity.finish();
         LogRuningTest.printInfo(tag, "---finish", context);
        }
   }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                LogRuningTest.printInfo("BaseActivity", "User click KEYCODE_BACK. Ignore it.", this);
                return false;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
