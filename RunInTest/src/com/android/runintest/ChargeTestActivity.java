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
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class ChargeTestActivity extends BaseActivity {

	private String TAG = "ChargeTestActivity";
	private ChargeTestActivity chargeTestActivity;
	private PowerManager.WakeLock mWl;
	private int current_average = 0;
    private int current_voltage = 0;
    String batteryStatus = "";
    private TextView txt_descript;
    private boolean chargeSuccess = false;
    PowerManager mPowerManager;
    private static int times = 0;
    private static final int TIMES_TEST = 12;

    private static boolean mTestSuccess = true;
    private SharedPreferences mSharedPreferences = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		chargeTestActivity = new ChargeTestActivity();
		chargeTestActivity.isMonkeyRunning(TAG, "onCreate", this);
		setContentView(R.layout.charge_test);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWl = mPowerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                        PowerManager.ON_AFTER_RELEASE, "bright");
		txt_descript = (TextView) findViewById(R.id.textView1);
	}

	 private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
         public void onReceive(Context context, Intent intent) {
			 LogRuningTest.printDebug(TAG,"BroadcastReceiver mHandler.hasMessages(0) = " + mHandler.hasMessages(0),ChargeTestActivity.this);
                 String s = intent.getAction();
                         if (!mHandler.hasMessages(0)) {
                                 mHandler.sendEmptyMessage(0);
                         }
                         showChargeTestInfo(intent, s);
         }
     };

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        chargeTestActivity.isMonkeyRunning(TAG, "onCreate", this);
        if (mWl != null) {
            mWl.setReferenceCounted(false);
            mWl.acquire();
        }
        LogRuningTest.printDebug(TAG,"power------1------>"+
                readFile("/sys/class/power_supply/battery/voltage_now"),this);
        LogRuningTest.printDebug(TAG,"power------2------>"+
                readFile("/sys/class/power_supply/battery/current_now"),this);
        mSharedPreferences = this.getSharedPreferences("runintest", Activity.MODE_PRIVATE);
        IntentFilter filter = new IntentFilter("android.intent.action.BATTERY_CHANGED");
        LogRuningTest.printDebug(TAG,"onResume registerReceiver =" + filter,this);
        registerReceiver(mBatteryInfoReceiver, filter);
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mHandler.removeMessages(0);
        unregisterReceiver(mBatteryInfoReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWl != null) {
            mWl.release();
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    LogRuningTest.printDebug(TAG, " mHandler", ChargeTestActivity.this);
                    System.out.println("ddd:" + msg.what + "," + System.currentTimeMillis());
                    current_average = -Integer.parseInt(readFile("/sys/class/power_supply/battery/current_now")) / 10;
                    current_voltage = Integer.parseInt(readFile("/sys/class/power_supply/battery/voltage_now"));
                    current_average = current_average <= 0 ? 0 : current_average;
                    showChargeTestInfo(null, "");
                    LogRuningTest.printDebug(TAG, "mHandler times ="+ times +"TIMES_TEST =" + TIMES_TEST, ChargeTestActivity.this);
                    if (times < TIMES_TEST) {
                        mHandler.sendEmptyMessageDelayed(0, 5000);
                    } else {
                        mHandler.sendEmptyMessageDelayed(1, 2000);
                    }
                    break;
                case 1:
                    goToEmmcTestActivity();
                    break;
                default:
                    break;
            }
        }
    };

    private void goToEmmcTestActivity() {
        LogRuningTest.printDebug(TAG, "goToEmmcTestActivity mTestSuccess ="+mTestSuccess, ChargeTestActivity.this);
        Editor editor = mSharedPreferences.edit();
        if (!mTestSuccess) {
            editor.putBoolean("charge_test", mTestSuccess);
        }
        editor.commit();
        Intent intent = new Intent(TestService.ACTION_EMMC_TEST);
        sendBroadcast(intent);
        finish();
    }

    private String readFile(String filePath) {
        String res = "";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath))));
            String str = null;
            while ((str = br.readLine()) != null) {
                res += str;
            }
            if (br != null) {
                br.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (res == null || res.equals("")) {
            res = "0";
        }

        return res;
    }

    private void showChargeTestInfo(Intent intent, String s) {
        int i = 0, j = 0, k = 0;//, current = 0;
        times++;
        if ("android.intent.action.BATTERY_CHANGED".equals(s)) {
            i = intent.getIntExtra("plugged", 0);
            j = intent.getIntExtra("status", 1);
            k = intent.getIntExtra("temperature", 0);
            LogRuningTest.printDebug(TAG, "average=" +
                    readFile("/sys/class/power_supply/battery/current_now"), this);
            LogRuningTest.printDebug(TAG, "voltage=" +
                    readFile("/sys/class/power_supply/battery/voltage_now"), this);

            current_average = -Integer.parseInt(readFile("/sys/class/power_supply/battery/current_now")) / 10;
            current_voltage = Integer.parseInt(readFile("/sys/class/power_supply/battery/voltage_now"));
            current_average = current_average <= 0 ? 0 : current_average;
            LogRuningTest.printDebug(TAG,
                    "power------------>"
                            + current_average, this);
            if (j == 2) {
                if (i > 0) {
                    if (i == 1)
                        batteryStatus = getString(R.string.battery_info_status_charging_ac);
                    else
                        batteryStatus = getString(R.string.battery_info_status_charging_usb);
                }
            } else if (j == 3) {
                batteryStatus = getString(R.string.battery_info_status_discharging);
            } else if (j == 4) {
                batteryStatus = getString(R.string.battery_info_status_notcharging);
            } else if (j == 5) {
                batteryStatus = getString(R.string.battery_info_status_full);
            } else {
                batteryStatus = getString(R.string.battery_info_status_unknow);
            }
        }
        String s1 = batteryStatus;
        LogRuningTest.printDebug(TAG, "j=" + j, this);
        if (j == 4 || j == 3) {
            chargeSuccess = false;
            mTestSuccess = false;
            txt_descript.setText(s1);
        } else {
            chargeSuccess = true;
            mTestSuccess = true;
            s1 += "\n";
            s1 += getString(R.string.voltage_current) + current_voltage / 1000 + "mV" + "\n";
            s1 += getString(R.string.charge_current) + current_average / 100 + "mA";
            if (current_average <= 35) {
                s1 += "(" + getString(R.string.abnormal_value) + ")";
            }

				s1 += "\n\n";
				if (/*k > 45 || */current_average <= 35) {/* current <= 300 provided by driver */
					chargeSuccess = false;
					if(times>=120){
						s1 += getString(R.string.test_fail);
						mTestSuccess = false;
					}
				}else{
					mTestSuccess = true;
					chargeSuccess = true;
					s1 += getString(R.string.test_success);
				}
			}
			txt_descript.setText(s1);
		 if(mTestSuccess){
			 LogRuningTest.printDebug(TAG, "showChargeTestInfo mTestSuccess ="+ mTestSuccess,this);
			 goToEmmcTestActivity();
		 }
		}

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

}
