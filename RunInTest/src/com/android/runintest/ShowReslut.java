package com.android.runintest;

import com.android.internal.widget.LockPatternUtils;
import com.android.runintest.R;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.content.Intent;
import android.view.KeyEvent;
import android.graphics.Rect;
import android.os.SystemProperties;
import android.util.Log;

public class ShowReslut extends BaseActivity {
    private static final String TAG = "ShowReslut";
    private ShowReslut showReslut;
    Bitmap mBitmap;
    private boolean mIsPass = false;
    private static final int SAVE_TO_NV = 0;
    private static final int DELAY_SAVE_TIME = 10*1000;
    private PowerManager mPowerManager;
    private static SharedPreferences mSharedPreferences = null;
    /* < 0080333 xuyinwen 20160108 begin */
    private static final int MAX_BATTERY_LEVEL = 75;
    /* 0080333 xuyinwen 20160108 end > */
    private static final int MIN_BATTERY_LEVEL = 45;
    private PowerManager.WakeLock mWl;

    private LockPatternUtils mLockPatternUtils;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showReslut = new ShowReslut();
        showReslut.isMonkeyRunning(TAG, "onCreate", ShowReslut.this);
        mSharedPreferences = this.getSharedPreferences("runintest", Activity.MODE_PRIVATE);
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWl = mPowerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                PowerManager.ON_AFTER_RELEASE, "bright");
        mLockPatternUtils = new LockPatternUtils(this);
        TestService.setMaxBatteryLevel(MAX_BATTERY_LEVEL);
        TestService.setMinBatteryLevel(MIN_BATTERY_LEVEL);
        I2CTest i2c = new I2CTest(this);
        isPass();
        if (mIsPass) {
            mBitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.pass);
        } else {
            mBitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.fail);
        }

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(new DragView(this));
        /* < 0071224 xuyinwen 20150824 delete > */
        //Move handler to isPass()
    }

    private void gotoRunInReslut() {
        Intent intent = new Intent(TestService.ACTION_SHOW_RUNINTEST_RESULT);
        sendBroadcast(intent);
    }

    Handler mSaveNVHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case SAVE_TO_NV:
                LogRuningTest.printDebug(TAG, "mSaveNVHandler:mIsPass = " + mIsPass, ShowReslut.this);
                LogRuningTest.printDebug(TAG, "mSaveNVHandler:I2CTest.saveReslut(mIsPass, ShowReslut.this) = " + I2CTest.saveReslut(mIsPass, ShowReslut.this), ShowReslut.this);
                if (!I2CTest.saveReslut(mIsPass, ShowReslut.this)) {
                    LogRuningTest.printDebug(TAG, "mSaveNVHandler:mIsPass = " + mIsPass, ShowReslut.this);
                    mSaveNVHandler.sendEmptyMessageDelayed(SAVE_TO_NV, DELAY_SAVE_TIME);
                }
                break;
            }
            super.handleMessage(msg);
        }
    };

    public void onResume() {
        super.onResume();
        showReslut.isMonkeyRunning(TAG, "onResume", ShowReslut.this);
        if (mWl != null) {
            mWl.acquire();
        }
    }

    private void isPass() {
        SharedPreferences sharedPreferences = getSharedPreferences("runintest",
                Activity.MODE_PRIVATE);
        boolean test_result = sharedPreferences.getBoolean("testResult", false);
        boolean test_end = sharedPreferences.getBoolean("testEnd", false);
        if (test_end) {
            mIsPass = test_result;
            mSaveNVHandler.sendEmptyMessageDelayed(SAVE_TO_NV, DELAY_SAVE_TIME);
            return;
        }
        boolean audio_test = sharedPreferences.getBoolean("audio_test", false);
        boolean integrated_test = sharedPreferences.getBoolean("integrated_test", false);
        boolean batteryLevel_test = sharedPreferences.getBoolean("batteryLevel_test", false);
        boolean NFC_test = sharedPreferences.getBoolean("NFC_test", false);
        boolean FPS_test = sharedPreferences.getBoolean("FPS_test", false);
        boolean DDR_test = sharedPreferences.getBoolean("DDR_test", false);
        int batteryLevel = TestService.getInstance().getCurrentBattary();
        
     
        
        boolean VIBRATOR_test = sharedPreferences.getBoolean("vibrator_test", false);
      
        boolean LCD_test = sharedPreferences.getBoolean("LCD_test", false);
     
        boolean TP_test = sharedPreferences.getBoolean("TP_test", true);
     
        boolean camera_test = sharedPreferences.getBoolean("camera_test", false);
     
        boolean audio_mic_test = sharedPreferences.getBoolean("audio_mic_test", false);
     
        boolean video_test = sharedPreferences.getBoolean("video_test", false);
      
        boolean charge_test = sharedPreferences.getBoolean("charge_test", false);
       
        boolean EMMC_test = sharedPreferences.getBoolean("EMMC_test", false);
     
        boolean REBOOT_test = sharedPreferences.getBoolean("reboot_test",false);

        boolean LightSensor_test = sharedPreferences.getBoolean("lightsenor_test",false);
      
        
        LogRuningTest.printInfo(TAG, "batteryLevel = " + batteryLevel, this);
       // if (MAX_BATTERY_LEVEL<batteryLevel || batteryLevel<MIN_BATTERY_LEVEL) {
       //     batteryLevel_test = false;
       //     SharedPreferences.Editor editor = sharedPreferences.edit();
       //     editor.putBoolean("batteryLevel_test", false);
       //     editor.commit();
       // }
        if (VIBRATOR_test && LCD_test && TP_test && camera_test 
        		&& audio_mic_test && video_test && charge_test && EMMC_test && LightSensor_test && DDR_test && REBOOT_test) {
            mIsPass = true;
        } else {
            mIsPass = false;
        }
        LogRuningTest.printDebug(TAG, "ShowReslut isPass:mIsPass = " + mIsPass, this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("testResult", mIsPass);
        editor.putBoolean("testEnd", true);
        editor.commit();
        mSaveNVHandler.sendEmptyMessageDelayed(SAVE_TO_NV, DELAY_SAVE_TIME);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Settings.Global.putInt(this.getContentResolver(), "runin_testing", 0);
        SystemProperties.set("ctl.start", "charging_enable");
        Intent startBattaryService = new Intent(this, TestService.class);
        this.stopService(startBattaryService);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("startRunInTest", false);
        editor.commit();
        mLockPatternUtils.setLockScreenDisabled(false, UserHandle.myUserId());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mBitmap) {
            mBitmap.recycle();
            mBitmap = null;
        }
        if (mWl != null) {
            mWl.release();
        }
    }

    private class DragView extends View implements Runnable {
        private int mMotionX = 0;
        private int mMotionY = 0;
        private Paint paint;
        private WindowManager wm;

        private int width;
        private int height;
        private int speedX = 20;
        private int speedY = 20;
        private final int speed = 20;

        public DragView(Context context) {
            super(context);
            paint = new Paint();
            wm = (WindowManager) getContext().getSystemService(
                    Context.WINDOW_SERVICE);
            width = wm.getDefaultDisplay().getWidth();
            height = wm.getDefaultDisplay().getHeight();
            new Thread(this).start();
        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);
            canvas.drawColor(Color.BLACK);
            if (mMotionX + mBitmap.getWidth() >= width) {
                speedX = -speed;
            }
            if (mMotionX <= 0) {
                speedX = speed;
            }
            if (mMotionY + mBitmap.getHeight() >= height) {
                speedY = -speed;
            }
            if (mMotionY <= 0) {
                speedY = speed;
            }

            mMotionX += speedX;
            mMotionY += speedY;
            canvas.drawBitmap(mBitmap, mMotionX, mMotionY, paint);
            Rect targetRect = new Rect(300, 550, 450, 600);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStrokeWidth(3);
            paint.setTextSize(80);
            int battary = TestService.getInstance().getCurrentBattary();
            String testString = battary + "%";
            canvas.drawRect(targetRect, paint);
            if (battary > MAX_BATTERY_LEVEL || battary < MIN_BATTERY_LEVEL) {
                paint.setColor(Color.RED);
            } else {
                paint.setColor(Color.GREEN);
            }
            canvas.drawText(testString, targetRect.left, targetRect.bottom, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                gotoRunInReslut();
                finish();
                return true;
            } else {
                return super.onTouchEvent(ev);
            }
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
                postInvalidate();
            }
        }
    }
}
