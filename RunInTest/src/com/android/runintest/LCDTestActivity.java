package com.android.runintest;

import java.io.IOException;
import java.io.RandomAccessFile;

import com.huiye.gl.GLTutorialCube;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.view.KeyEvent;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.provider.Settings;
import java.util.ArrayList;
import java.util.List;
/* < 0077092 xuyinwen 20151121 begin */
import android.media.AudioSystem;
/* 0077092 xuyinwen 20151121 end > */

public class LCDTestActivity extends BaseActivity {

    private static final String TAG = "LCDTestActivity";
    private LCDTestActivity lCDTestActivity;

    private AnimationDrawable mColorFrameAnimation;

    private AnimationDrawable mFrameAnimation;

    private ImageView mColorImage;

    private ImageView mFrameImage;

    private View mClubeGLView;

    private static final int DELAY_MILLIS = 20;

    private static final int STOP_COLOR_ANIMATION_TIME = 1 * 60 * 1000;

    private static final int STOP_FRAME_ANIMATION_TIME = 1 * 60 * 1000;

    private static final int STOP_CUBE_ANIMATION_TIME = 1 * 60 * 1000;

    private static final int PLAY_MODE_CHANGE_TIME = 10 * 1000;

    private static final int START_COLOR_ANIMATION = 0;

    private static final int STOP_COLOR_ANIMATION = 1;

    private static final int START_OPGL_ANIMATION = 2;

    private static final int STOP_OPGL_ANIMATION = 3;

    private static final int MUSIC_RECEIVER_PLAY = 0;

    private static final int MUSIC_SPEAKER_PLAY = 1;
    
    private static final int LED_GREEN_BRIGHT = 0;
    
    private static final int LED_RED_BRIGHT = 1;
    
    private static final int CHANE_LED_COLOR_TIME = 2 * 1000;

    private AudioManager audioManager;

    private MediaPlayer mpMediaPlayer;

    private Vibrator vibrator;

    private SharedPreferences mSharedPreferences = null;

    private static boolean mLCDTest = true;

    private static boolean mAudioTest = true;
    
    private int mCurrentBrightness;
    
    private boolean isAutoBrightness;

    private static final String BATTERY_CHARGING_STATUS = "Charging";

    private static final String BATTERY_FULL_STATUS = "Full";

    //private final List<CPUTestThread> mIsnterruptThreads = new ArrayList<CPUTestThread>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        lCDTestActivity = new LCDTestActivity();
        lCDTestActivity.isMonkeyRunning(TAG, "onCreate", LCDTestActivity.this);
        LogRuningTest.printInfo(TAG, "start LCDTestActivity", this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
        setContentView(R.layout.activity_lcd_test);
        isAutoBrightness = BrightnessTools.isAutoBrightness(getContentResolver());
        if (isAutoBrightness) {
            BrightnessTools.stopAutoBrightness(this);
        }
        mCurrentBrightness = BrightnessTools.getScreenBrightness(this);  
        BrightnessTools.setBrightness(this, 255); 
        mSharedPreferences = this.getSharedPreferences("runintest", Activity.MODE_PRIVATE);
        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        initColorAnimation();
        mAnimationHandler.sendEmptyMessageDelayed(START_COLOR_ANIMATION, DELAY_MILLIS);
        mLedColorHandler.sendEmptyMessageDelayed(LED_GREEN_BRIGHT, DELAY_MILLIS);
        mMusicHandler.sendEmptyMessageDelayed(MUSIC_SPEAKER_PLAY, PLAY_MODE_CHANGE_TIME);
    }

    @Override
    protected void onResume() {
        super.onResume();
        lCDTestActivity.isMonkeyRunning(TAG, "onResume", LCDTestActivity.this);
    }

    private void initColorAnimation() {
        mColorImage = (ImageView) findViewById(R.id.color_image);

        mColorImage.setBackgroundResource(R.anim.lcd_color_frame_animation);

        mColorFrameAnimation = (AnimationDrawable) mColorImage.getBackground();

        mClubeGLView = new GLTutorialCube(this);
    }

    private void initAnimation() {
        mFrameImage = (ImageView) findViewById(R.id.frame_image);

        mFrameImage.setBackgroundResource(R.anim.lcd_frame_animation);

        mFrameAnimation = (AnimationDrawable) mFrameImage.getBackground();
    }

    private void startFrameAnimation() {
        if (mFrameAnimation != null && !mFrameAnimation.isRunning()) {
            mFrameAnimation.start();
        }
    }

    private void startColorAnimation() {
        if (mColorFrameAnimation != null && !mColorFrameAnimation.isRunning()) {
            mColorFrameAnimation.start();
        }
    }

    private void stopColorAnimation() {
        if (mColorFrameAnimation != null && mColorFrameAnimation.isRunning()) {
            mColorFrameAnimation.stop();
            mColorFrameAnimation = null;
            mColorImage.setVisibility(View.GONE);
            if(mLedColorHandler != null){
                mLedColorHandler.removeCallbacksAndMessages(null);
            }
        }
        /* < 0071429 xuyinwen 20150922 begin */
        /* < 0071429 xuyinwen 20150921 begin */
        String batteryStatus = RuningTestConfig.readFile("/sys/class/power_supply/battery/status");
        LogRuningTest.printInfo(TAG, "batteryStatus = " + batteryStatus, this);
        if (BATTERY_FULL_STATUS.equals(batteryStatus)) {
            SystemProperties.set("ctl.start", "set_greenled");
        } else if (BATTERY_CHARGING_STATUS.equals(batteryStatus)) {
            SystemProperties.set("ctl.start", "set_redled");
        } else {
            SystemProperties.set("ctl.start", "set_closered");
            SystemProperties.set("ctl.start", "set_closegreen");
        }
        /* 0071429 xuyinwen 20150921 end > */
        /* 0071429 xuyinwen 20150922 end > */
    }

    
    Handler mLedColorHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case LED_GREEN_BRIGHT:
                LogRuningTest.printInfo(TAG, "LED_GREEN_BRIGHT", LCDTestActivity.this);
                SystemProperties.set("ctl.start", "set_greenled");
                //RuningTestConfig.writeToFile(RuningTestConfig.LED_PATCH
                //       , RuningTestConfig.LED_GREEN_BRIGHT, TAG, LCDTestActivity.this);
                mLedColorHandler.sendEmptyMessageDelayed(LED_RED_BRIGHT, CHANE_LED_COLOR_TIME);
                break;
            case LED_RED_BRIGHT:
                LogRuningTest.printInfo(TAG, "LED_RED_BRIGHT", LCDTestActivity.this);
                SystemProperties.set("ctl.start", "set_redled");
                //RuningTestConfig.writeToFile(RuningTestConfig.LED_PATCH
                //        , RuningTestConfig.LED_RED_BRIGHT, TAG, LCDTestActivity.this);
                mLedColorHandler.sendEmptyMessageDelayed(LED_GREEN_BRIGHT, CHANE_LED_COLOR_TIME);
                break;
            }
            super.handleMessage(msg);
        }
    };
    
    Handler mAnimationHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case START_COLOR_ANIMATION:
                LogRuningTest.printInfo(TAG, "START_COLOR_ANIMATION", LCDTestActivity.this);
                playVibrate();
                startColorAnimation();
               //playMusic();
                mAnimationHandler.sendEmptyMessageDelayed(STOP_COLOR_ANIMATION,
                            STOP_COLOR_ANIMATION_TIME);
                break;
            case STOP_COLOR_ANIMATION:
                LogRuningTest.printInfo(TAG, "STOP_COLOR_ANIMATION", LCDTestActivity.this);
                stopColorAnimation();
                SharedPreferences.Editor lcd_editor = mSharedPreferences.edit();
                if (!mLCDTest) {
                	lcd_editor.putBoolean("LCD_test", mLCDTest);
                }
                lcd_editor.commit();
                gotoTPTestActivity();
                finish();
                /*initAnimation();
                startFrameAnimation();
                mAnimationHandler.sendEmptyMessageDelayed(START_OPGL_ANIMATION,
                            STOP_FRAME_ANIMATION_TIME);*/
                break;
            case START_OPGL_ANIMATION:
                LogRuningTest.printInfo(TAG, "START_OPGL_ANIMATION", LCDTestActivity.this);
                setContentView(mClubeGLView);
                mAnimationHandler.sendEmptyMessageDelayed(STOP_OPGL_ANIMATION,
                            STOP_CUBE_ANIMATION_TIME);
            break;
            case STOP_OPGL_ANIMATION:
                LogRuningTest.printInfo(TAG, "go to integrated test", LCDTestActivity.this);
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                /* < 0067632 xuyinwen 20150813 begin */
                if (!mAudioTest) {
                    editor.putBoolean("audio_test", mAudioTest);
                }
                if (!mLCDTest) {
                    editor.putBoolean("LCD_test", mLCDTest);
                }
                /* 0067632 xuyinwen 20150813 end > */
                editor.commit();
                gotoTPTestActivity();
                finish();
                break;
            }
            super.handleMessage(msg);
        }
    };

    Handler mMusicHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MUSIC_SPEAKER_PLAY:
                LogRuningTest.printInfo(TAG, "MUSIC_SPEAKER_PLAY", LCDTestActivity.this);
                /* < 0077092 xuyinwen 20151121 begin */
                audioManager.setMode(AudioSystem.MODE_NORMAL);
                /* 0077092 xuyinwen 20151121 end > */
                mMusicHandler.sendEmptyMessageDelayed(MUSIC_RECEIVER_PLAY,
                            PLAY_MODE_CHANGE_TIME);
                break;
            case MUSIC_RECEIVER_PLAY:
                LogRuningTest.printInfo(TAG, "MUSIC_RECEIVER_PLAY", LCDTestActivity.this);
                /* < 0077092 xuyinwen 20151121 begin */
                audioManager.setMode(AudioSystem.MODE_IN_CALL);
                /* 0077092 xuyinwen 20151121 end > */
                mMusicHandler.sendEmptyMessageDelayed(MUSIC_SPEAKER_PLAY,
                            PLAY_MODE_CHANGE_TIME);
                break;
            }
            super.handleMessage(msg);
        }
    };

    private void gotoTPTestActivity() {
        Intent intent = new Intent(TestService.ACTION_TP_TEST);
        sendBroadcast(intent);
    }

    private void playMusic() {
        audioManager.setMode(AudioManager.MODE_IN_CALL);

        mpMediaPlayer = new MediaPlayer();
        AssetManager am = getAssets();
        String exception = null;
        try {
            mpMediaPlayer.setDataSource(am.openFd("Mp3.mp3").getFileDescriptor());
            mpMediaPlayer.prepare();
            mpMediaPlayer.setLooping(true);
            mpMediaPlayer.start();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            exception = Log.getStackTraceString(e);
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            exception = Log.getStackTraceString(e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            exception = Log.getStackTraceString(e);
        }

        if (null != exception) {
            LogRuningTest.printDebug(TAG, "result:LCD test failed", this);
            LogRuningTest.printError(TAG, "reason:" + exception, this);
            mAudioTest = false;
        }
    }

    private void stopMusic() {
        if (mpMediaPlayer != null) {
            mpMediaPlayer.stop();
            mpMediaPlayer = null;
        }
    }
    
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        /* < 0078018 xuyinwen 20151210 begin */
        LogRuningTest.printDebug(TAG, "onDestroy:" + Log.getStackTraceString(new Throwable()), this);
        /* 0078018 xuyinwen 20151210 end >*/
        if (isAutoBrightness) {
            BrightnessTools.startAutoBrightness(this);
        }
        BrightnessTools.setBrightness(this, mCurrentBrightness);
        stopMusic();
        stopVibrate();
        removeHandler();
        /* < 0071429 xuyinwen 20150922 begin */
        stopColorAnimation();
        /* 0071429 xuyinwen 20150922 end > */
    }

    private void playVibrate() {
        vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        long al[] = { 0, 2000L, 1000l, 2000l, 1000l };
        vibrator.vibrate(al, 0);
    }

    private void stopVibrate() {
    	/* < 0078018 xuyinwen 20151210 begin */
        if (null != vibrator) {
            vibrator.cancel();
        }
        /* 0078018 xuyinwen 20151210 end > */
    }

    private void removeHandler() {
        mMusicHandler.removeCallbacksAndMessages(null);
        mAnimationHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                /* < 0068265 xuyinwen 20150819 begin */
                /* < 0067427 xuyinwen 20150811 begin */
                LogRuningTest.printInfo(TAG, "User click KEYCODE_BACK. Ignore it.", this);
                /* 0067427 xuyinwen 20150811 end > */
                return false;
            default:
                break;
                /* 0068265 xuyinwen 20150819 end > */
        }
        return super.onKeyDown(keyCode, event);
    }

    private void saveScreenBrightness(int paramInt) {
        try {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, paramInt);
        } catch (Exception localException) {
            mLCDTest = false;
            LogRuningTest.printDebug(TAG, "result:LCD test failed", this);
            LogRuningTest.printError(TAG, "reason:" + Log.getStackTraceString(localException), this);
        }
    }

    private int getScreenBrightness() {
        int screenBrightness=255;
        try {
            screenBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception localException) {
            mLCDTest = false;
            LogRuningTest.printDebug(TAG, "result:LCD test failed", this);
            LogRuningTest.printError(TAG, "reason:" + Log.getStackTraceString(localException), this);
        }
        return screenBrightness;
    }
}
