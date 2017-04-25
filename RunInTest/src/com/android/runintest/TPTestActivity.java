package com.android.runintest;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.os.Bundle;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.SharedPreferences.Editor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import android.os.FileUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import android.util.Log;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.TextView;
import android.os.SystemProperties;
import android.provider.Settings;
import android.content.ActivityNotFoundException;
import android.os.PowerManager;
import android.os.Build;
import android.text.TextUtils;

public class TPTestActivity extends BaseActivity{

    private static final String TAG = "TPTestActivity";
    private TPTestActivity  tPTestActivity;
    
    TextView mTestResult;
    private static String TP_PATH = "/proc/class/ms-touchscreen-msg20xx/device/test";
    public static final byte[] LIGHTE_ON = { '1' };
    public static final byte[] LIGHTE_OFF = { '2' };
    
    private static int READ_COUNT1 = 0;
    private static int READ_COUNT2 = 0;
    
    private static int ROUND_TIMES = 3;
    private static int ROUND_COUNTS = 0;
    
    private static SharedPreferences mSharedPreferences = null;

    private PowerManager mPowerManager;

    private PowerManager.WakeLock mWl;
  
    private KeyguardManager km;
    private KeyguardLock kl;
	private static final int REQUEST_ASK = 1;
    
    private static boolean mTestSuccess = true;
	private int  passcount = 0;
	private int  failcount = 0;
	private static int mTPtestCount = 0 ;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        tPTestActivity = new TPTestActivity();
        tPTestActivity.isMonkeyRunning(TAG, "onCreate", TPTestActivity.this);
        LogRuningTest.printDebug(TAG, "satrt TPTestActivity", this);
        setContentView(R.layout.tp_test);
        mSharedPreferences = this.getSharedPreferences("runintest", Activity.MODE_PRIVATE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mTestResult = (TextView) findViewById(R.id.test_result);

        if(SystemProperties.get("ro.product.apk_tptest", "1").equals("1")){
            APKTPTest();
        }else{
            writeAndRead();
        }
    }

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
				case 0:
					LogRuningTest.printDebug(TAG, "case---->"+"0",TPTestActivity.this);
					READ_COUNT1++;
					LogRuningTest.printDebug(TAG, "readFile(TP_PATH):"+readFile(TP_PATH),TPTestActivity.this);
					if (readFile(TP_PATH)) {
						LogRuningTest.printDebug(TAG, "case---->0" + "1",TPTestActivity.this);
						writeFile(2);
						mHandler.sendEmptyMessage(1);
						mHandler.removeMessages(0);
						LogRuningTest.printDebug(TAG, "case---->0" + "1",TPTestActivity.this);
					} else {
						if (READ_COUNT1 == 10) {
							mHandler.removeMessages(0);
							mTestResult.setText(getString(R.string.tp_test_fail));
							mTestSuccess = false;
							LogRuningTest.printDebug(TAG, "case---->0" + "2",TPTestActivity.this);
							mHandler.sendEmptyMessageDelayed(2, 1000);
						} else {
							mHandler.sendEmptyMessageDelayed(0, 1000);
						}
					}
					break;
				case 1:
					LogRuningTest.printDebug(TAG, "case---->1"+"1",TPTestActivity.this);
					READ_COUNT2++;
					if(readFile(TP_PATH)){
						mHandler.removeMessages(1);
						LogRuningTest.printDebug(TAG, "case---->1"+"1",TPTestActivity.this);
						mTestResult.setText(getString(R.string.tp_test_ok));
						//baiwenxin 0100460 2017-2-27 begin
						Editor editor =  mSharedPreferences.edit();
						editor.putInt("TPtestPassCount", ++mTPtestCount);
						editor.commit();
						//baiwenxin 0100460 2017-2-27 end
					} else {
						if (READ_COUNT2 == 6) {
							LogRuningTest.printDebug(TAG, "case---->1" + "2",TPTestActivity.this);
							mTestResult.setText(getString(R.string.tp_test_fail));
							mTestSuccess = false;
							mHandler.removeMessages(1);
						} else {
							mHandler.sendEmptyMessageDelayed(1, 1000);
						}
					}
					mHandler.sendEmptyMessageDelayed(2, 3 * 1000);
					break;
				case 2:
					goToCameraTest();
					break;
				case 3:
					if(ROUND_COUNTS < ROUND_TIMES){
						writeAndRead();
					}else{
						mHandler.sendEmptyMessageDelayed(2, 5 * 1000);
					}
					break;
				default:
					break;


			}

		}

	};

	private void writeFile (int i) {
		FileOutputStream red;
		try {
			byte[] ledData = LIGHTE_ON;
			ledData = (i == 1) ? LIGHTE_ON : LIGHTE_OFF;
			red = new FileOutputStream(TP_PATH);
			red.write(ledData);
			red.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeAndRead(){
		LogRuningTest.printDebug(TAG,"TPTest writeAndRead()",this);
		ROUND_COUNTS ++;
		READ_COUNT1 = 0;
		READ_COUNT2 = 0;
		mTestResult.setText(getString(R.string.read_tp));
		writeFile(1);
		mHandler.sendEmptyMessageDelayed(0,5000);
	}

	public  boolean readFile(String filePath) {
		boolean tpRes = false;
		String res = "";
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath))));
			String str = null;
			while ((str = br.readLine()) != null) {
				res += str;
			}
			LogRuningTest.printDebug(TAG,"readFile() res:"+res,this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		/**
		 * xinsi
		 */
		tpRes = "0".equals(res);
		LogRuningTest.printDebug(TAG, "readFile tpRes = " + tpRes,TPTestActivity.this);
		return tpRes;
	}




	public void APKTPTest(){
		LogRuningTest.printDebug(TAG,"APKTPTest() start",this);
		Intent mintent = new Intent();
		mintent.setClassName("com.android.touch", "com.android.touch.main.MainActivity");
		startActivityForResult(mintent, REQUEST_ASK);
	}

    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
        tPTestActivity.isMonkeyRunning(TAG, "onResume", TPTestActivity.this);
    }

    @Override
    protected void onStop() {
    	// TODO Auto-generated method stub
    	super.onStop();
    }

    private void goToCameraTest() {
    	/**
    	 * Judge TPtest is or not success 
    	 * begin xinsi 2016-11-26 17:29
    	 **/
    	Editor editor =  mSharedPreferences.edit();
		//baiwenxin 0100460 2017-2-27 begin
		//baiwenxin 0101680 2017-3-1 begin
		if(TextUtils.equals(Build.BUILD_PRODUCT, "msm8909_x26l_cp5267") || TextUtils.equals(Build.BUILD_PRODUCT, "msm8909_x26l_cp5267c")){
			//baiwenxin 0101680 2017-3-1 end
			mTPtestCount = mSharedPreferences.getInt("TPtestPassCount", 0);
			if (mTPtestCount >= 1){
				mTestSuccess =true;
			}else{
				mTestSuccess = false;
			}
		}
        editor.putBoolean("TP_test", mTestSuccess);
    	editor.commit();
		LogRuningTest.printDebug(TAG, "goToCameraTest  mTestSuccess =" + mTestSuccess +"mTPtestCount ="+mTPtestCount,this);
		//baiwenxin 0100460 2017-2-27 end
    	Intent intent = new Intent(TestService.ACTION_CAMERA_TEST);
        sendBroadcast(intent);
        finish();
    }

    
    @Override
    protected void onDestroy() {
        super.onDestroy();
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Bundle bundle;
		if (requestCode == REQUEST_ASK) {
			if (resultCode == 1) {
				bundle = data.getExtras();
				passcount = bundle.getInt("PASS");
				failcount = bundle.getInt("FAIL");
				LogRuningTest.printDebug(TAG,"onActivityResult() passcount " + passcount +" failcount "+failcount,this);
			}
		}
		//baiwenxin 0097567 2017-02-24 begin
		if(passcount == 1 && failcount == 0){
			mTestSuccess = true;
		}else {
			mTestSuccess = false;
		}
		goToCameraTest();
		//baiwenxin 0097567 2017-02-24 end
	}
}
