package com.android.runintest;

import java.io.IOException;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.os.SystemProperties;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import android.media.AudioManager;
/* < 0067427 xuyinwen 20150811 begin */
import android.view.KeyEvent;
/* 0067427 xuyinwen 20150811 end > */
/* < 0077092 xuyinwen 20151121 begin */
import android.media.AudioSystem;
/* 0077092 xuyinwen 20151121 end > */

public class VedioTestActivity extends Activity implements
                    OnCompletionListener, OnErrorListener, OnInfoListener,
                    OnPreparedListener, OnSeekCompleteListener, OnVideoSizeChangedListener,
                    SurfaceHolder.Callback {
    private static final String TAG = "VedioTestActivity";
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWl;
    private Display currDisplay;
    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private MediaPlayer player;
    private int vWidth, vHeight;
    Vibrator vibrator;
    private final int GO_TO_VEDIO_TEST_ACTIVITY = 0;
    private final int PLAY_VEDIO_TIME =  60 * 1000;
    private int mCurrentBrightness;
    private boolean isAutoBrightness;
    private boolean mTestSuccess = true;
    private SharedPreferences mSharedPreferences = null;
    private AudioManager audioManager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_vedio_test);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWl = mPowerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                        PowerManager.ON_AFTER_RELEASE, "bright");
        LogRuningTest.printDebug(TAG, "onCreate start VedioTestActivity", this);
        isAutoBrightness = BrightnessTools.isAutoBrightness(getContentResolver());
        mSharedPreferences = this.getSharedPreferences("runintest", Activity.MODE_PRIVATE);
        if (isAutoBrightness) {
            BrightnessTools.stopAutoBrightness(this);
        }
        mCurrentBrightness = BrightnessTools.getScreenBrightness(this);      
        BrightnessTools.setBrightness(this, 255);  
        surfaceView = (SurfaceView) this.findViewById(R.id.video_surface);
        holder = surfaceView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        /* < 0077092 xuyinwen 20151121 begin */
        audioManager.setMode(AudioSystem.MODE_NORMAL);
        /* 0077092 xuyinwen 20151121 end > */
        player = new MediaPlayer();
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setOnInfoListener(this);
        player.setOnPreparedListener(this);
        player.setOnSeekCompleteListener(this);
        player.setOnVideoSizeChangedListener(this);
        String exception = null;
        AssetManager am = getAssets();
        try {
            player.setDataSource(am.openFd("aaa.mp4").getFileDescriptor(),
                        am.openFd("aaa.mp4").getStartOffset(),
                        am.openFd("aaa.mp4").getLength());
            LogRuningTest.printDebug(TAG, "surface start called", this);
        } catch (IllegalArgumentException e) {
            exception = Log.getStackTraceString(e);
        } catch (IllegalStateException e) {
            exception = Log.getStackTraceString(e);
        } catch (IOException e) {
            exception = Log.getStackTraceString(e);
        }
        currDisplay = this.getWindowManager().getDefaultDisplay();
        playVibrate();
        mGoToChargeTestActivity.sendEmptyMessageDelayed(GO_TO_VEDIO_TEST_ACTIVITY, PLAY_VEDIO_TIME);
        if (null != exception) {
            LogRuningTest.printDebug(TAG, "result:video test failed", this);
            LogRuningTest.printError(TAG, "reason:" + exception, this);
            mTestSuccess = false;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        LogRuningTest.printInfo(TAG, "surfaceChanged called", this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        player.setDisplay(holder);
        player.prepareAsync();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        LogRuningTest.printInfo(TAG, "surfaceDestroyed called", this);
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer arg0, int arg1, int arg2) {
        LogRuningTest.printInfo(TAG, "onVideoSizeChanged called", this);
    }

    @Override
    public void onSeekComplete(MediaPlayer arg0) {
        LogRuningTest.printInfo(TAG, "onSeekComplete called", this);
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        vWidth = player.getVideoWidth();
        vHeight = player.getVideoHeight();
        if (vWidth > currDisplay.getWidth()
                    || vHeight > currDisplay.getHeight()) {
            float wRatio = (float) vWidth / (float) currDisplay.getWidth();
            float hRatio = (float) vHeight / (float) currDisplay.getHeight();

            float ratio = Math.max(wRatio, hRatio);

            vWidth = (int) Math.ceil((float) vWidth / ratio);
            vHeight = (int) Math.ceil((float) vHeight / ratio);
            surfaceView.setLayoutParams(new LinearLayout.LayoutParams(vWidth,
            vHeight));
            player.setLooping(true);
            player.start();
        }else{
            surfaceView.setLayoutParams(new LinearLayout.LayoutParams(currDisplay.getWidth(),
                    currDisplay.getHeight()));
                    player.setLooping(true);
                    player.start();
        }
    }

    @Override
    public boolean onInfo(MediaPlayer player, int whatInfo, int extra) {
        LogRuningTest.printInfo(TAG, "whatInfo " + whatInfo, this);
        switch (whatInfo) {
            case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                break;
            case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                break;
            case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                break;
            case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                break;
        }
        return false;
    }

    @Override
    public boolean onError(MediaPlayer player, int whatError, int extra) {
        LogRuningTest.printInfo(TAG, "whatError " + whatError, this);
        switch (whatError) {
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                player.reset();
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer player) {
        LogRuningTest.printInfo(TAG, "onComletion called", this);
    }

    private void playVibrate() {
        vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        long al[] = { 0, 2000L, 1000l, 2000l, 1000l };
        vibrator.vibrate(al, 0);
    }

    private void goToChargeTestActivity(){
    	/**
    	 * Judge Vedio Test is or not Success
    	 * 
    	 * 
    	**/
        LogRuningTest.printDebug(TAG, "player.getCurrentPosition():"
                         + player.getCurrentPosition() , this);
        LogRuningTest.printDebug(TAG, "PLAY_VEDIO_TIME:"
                          + PLAY_VEDIO_TIME , this);
       Editor editor = mSharedPreferences.edit();
       if(PLAY_VEDIO_TIME - player.getCurrentPosition() < 1000 || player.getCurrentPosition() -PLAY_VEDIO_TIME <1000 ){
    	   editor.putBoolean("video_test", true);
       }else{
    	   editor.putBoolean("video_test", false);
       }
       editor.commit();
    	/** end xinsi */
    	Intent intent = new Intent(TestService.ACTION_CHARGE_TEST);
        sendBroadcast(intent);
        finish();
    }
    Handler mGoToChargeTestActivity = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GO_TO_VEDIO_TEST_ACTIVITY:
                	goToChargeTestActivity();
            }
            super.handleMessage(msg);
        }
    };

    public void onResume() {
        super.onResume();
        if (mWl != null) {
            mWl.acquire();
        }
    }
   
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopVibrate();
        if (isAutoBrightness) {
            BrightnessTools.startAutoBrightness(this);
        }
        BrightnessTools.setBrightness(this, mCurrentBrightness);
        Log.d("xinsi","player.getCurrentPosition():"+player.getCurrentPosition()
        		+"  getDuration():"+player.getDuration());
        if(player != null){
            player.release();
        }
        if (mGoToChargeTestActivity != null) {
        	mGoToChargeTestActivity.removeCallbacksAndMessages(null);
        }
        if (mWl != null) {
            mWl.release();
        }
    }

    private void stopVibrate() {
        vibrator.cancel();
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
}
