package com.android.runintest;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;

import com.android.internal.widget.LockPatternUtils;
import com.hymost.util.CommonUtil;

public class RebootTestActivity extends BaseActivity{
	
	private static final String TAG = "RebootTestActivity";
	
	private static  int REBOOT_COUNT = 0;
	
	private static int mRebootCount = 0 ;
	private static int mLoopCount = 0;
	private static boolean rebootStatus ;
	private static final int FIRST_TEST_LOOP = 1;
	private static final int REBOOT_TIME = 10 * 1000;
	
	private LockPatternUtils mLockPatternUtils;
	
	private static final String ACTION_REBOOT_TIME = "action_reboot_time";
	
	private static SharedPreferences mSharedPreferences = null;
	
	private static boolean mTestSuccess = true;
	
	private static int runTimes ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogRuningTest.printDebug(TAG, "onCreate  start  RebootTestActivity ", this);
		mSharedPreferences = this.getSharedPreferences("runintest", Activity.MODE_PRIVATE);
		mLockPatternUtils = new LockPatternUtils(this);
		loadRunInTest();
		unLock();
        CommonUtil.startTestService(this, TAG);
		mhandler.sendEmptyMessageDelayed(0, 5 * 1000);
	}
	
	Handler mhandler =new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(mRebootCount  <= runTimes ){
				mRebootOrgoToVibrateTestActivity();
			}
		}
	};

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

	private void mRebootOrgoToVibrateTestActivity(){
		if(!rebootStatus){
			 LogRuningTest.printDebug(TAG, "mRebootOrgoToVibrateTestActivity() isInRebootLoop()", this);
			 //rebootLoop();
		}else{
			 LogRuningTest.printDebug(TAG, "goToVibrateTestActivity()", this);
			/** judge Reboot Test is or not success
			 *  @mRebootCount  reboot times
			 *  @runTimes      the loop times
			 *  begin xinsi 2016-11-26 14:56
			**/
			SharedPreferences.Editor editor = mSharedPreferences.edit();
			editor.putBoolean("testEnd", false);
			if(mRebootCount != runTimes){
				editor.putBoolean("reboot_test",false);
			}else{
				editor.putBoolean("reboot_test",true);
			}
			editor.commit();
			/** end  xinsi**/
			 Intent intent =  new Intent(TestService.ACTION_VIBRATE_TEST);
	         sendBroadcast(intent);
	         finish();
		}
	
	}
	

	
	public boolean isInRebootLoop() {
        LogRuningTest.printInfo(TAG, "rebooted times is " + mRebootCount, this);
        return mRebootCount > REBOOT_COUNT ? false : true;
    }
	
	public void rebootLoop() {
	        saveReBootCount();
	        Intent intent = new Intent(Intent.ACTION_REBOOT);
	        intent.setAction(Intent.ACTION_REBOOT);
	        intent.putExtra("nowait", 1);
	        intent.putExtra("interval", 1);
	        intent.putExtra("window", 0);
	        sendBroadcast(intent);
	        LogRuningTest.printInfo(TAG, "First test loop, reboot test", this);
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
	
	public void loadRunInTest() {
		rebootStatus = mSharedPreferences.getBoolean("rebootStatus", false);
    	REBOOT_COUNT = mSharedPreferences.getInt("reboot_count", 0);
        mRebootCount = mSharedPreferences.getInt("currentRebootCount", 0);
        runTimes = mSharedPreferences.getInt("Run_Times",0);
        mLoopCount = mSharedPreferences.getInt("testLoopCount", 1);
        LogRuningTest.printInfo(TAG,"loadRunInTest() mRebootCount" + mRebootCount,this);
        LogRuningTest.printInfo(TAG, "loadRunInTest() runTimes" + runTimes, this);
	}
	public void saveReBootCount() {
	        int batteryLevel = TestService.getInstance().getCurrentBattary();
	        SharedPreferences.Editor editor = mSharedPreferences.edit();
	        if(mSharedPreferences.getBoolean("rebootStatus", false) == false){
	        	editor.putBoolean("rebootStatus",true);
	        }else{
	        	editor.putBoolean("rebootStatus", false);
	        }
	        editor.putBoolean("startRunInTest", true);
	        editor.putInt("currentRebootCount", ++mRebootCount);
	        editor.putInt("battleryLevel", batteryLevel);
	        editor.commit();
	        LogRuningTest.printInfo(TAG, "saveReBootCount currentRebootCount " + mRebootCount, this);
	        LogRuningTest.printInfo(TAG, "saveReBootCount battleryLevel " + batteryLevel, this);
	}

}
