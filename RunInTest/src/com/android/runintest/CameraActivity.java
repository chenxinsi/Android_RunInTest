package com.android.runintest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.app.Activity;
import android.app.Service;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.os.Handler;
import android.os.Message;
import android.os.Environment;
import android.widget.Toast;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.TextView;
import android.text.TextPaint;
import android.util.Log;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.Button;
import android.widget.LinearLayout;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import java.io.ByteArrayInputStream;
import java.io.IOException;


public class CameraActivity extends Activity{

    private static final String TAG = "CameraActivity";
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWl;
    private Camera camera = null;
    private CameraView cv = null;
    String exception = null;
    private Camera.PictureCallback picture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            camera.stopPreview();
            try {
                FileOutputStream outSteam=null;
                outSteam=new FileOutputStream("/sdcard/aging.jpg");
                outSteam.write(data);
                outSteam.close();
                camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };
    LinearLayout l = null;
    private final int Rear_Camera = 0;
    private final int Rear_Camera_Photograph= 1;
    private final int Camera_RELEASE = 2;
    private final int Reliability_Camera_PREVIEW_TIME = 5 * 1000;
    private final int Reliability_Camera_CHANGE_TIME = 4 * 1000;
    int loop =0;
    private final int Back =30;
    private final int Front =20;
    private static boolean mTestSuccess = true;
    private SharedPreferences mSharedPreferences = null;
    private static int mCameraERRcount = 0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.camera_activity);
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWl = mPowerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                        PowerManager.ON_AFTER_RELEASE, "bright");
        l = (LinearLayout) findViewById(R.id.cameraView);
        LogRuningTest.printDebug(TAG, "CameraActivity onCreate start.", this);
        mSharedPreferences = getSharedPreferences("runintest", Activity.MODE_PRIVATE);
        mReliabilityCameraHandler.sendEmptyMessage(Rear_Camera);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (mWl != null) {
            mWl.release();
        }
        if(camera != null){
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (mWl != null) {
            mWl.acquire();
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(camera != null){
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    class CameraView extends SurfaceView {

        private SurfaceHolder holder = null;

        public CameraView(Context context) {
            super(context);
            holder = this.getHolder();

            holder.addCallback(new SurfaceHolder.Callback() {

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format,
                                           int width, int height) {
                    Camera.Parameters parameters = camera.getParameters();
                    camera.setParameters(parameters);
                    camera.startPreview();
                }

                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {
                        if (loop <30){
                            camera = Camera.open(0);
                        }else{
                            camera = Camera.open(1);
                        }
                        camera.setDisplayOrientation(90);
                        camera.setPreviewDisplay(holder);
                    } catch (IOException e) {
                        camera.release();
                        camera = null;
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    if(camera != null){
                        camera.stopPreview();
                        camera.release();
                        camera = null;
                    }
                }
            });
        }
    }

    Handler mReliabilityCameraHandler = new Handler(){
        public void handleMessage(Message msg) {
            try{
            switch (msg.what) {
                case Rear_Camera:
                    loop += 1;
                    l.removeAllViews();
                    cv = new CameraView(CameraActivity.this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.FILL_PARENT,
                            LinearLayout.LayoutParams.FILL_PARENT);
                    l.addView(cv, params);
                    mReliabilityCameraHandler.sendEmptyMessageDelayed(Rear_Camera_Photograph, Reliability_Camera_PREVIEW_TIME);
                    Log.d("bwx","loop =" + loop);
                    LogRuningTest.printDebug(TAG, "loop " + loop, CameraActivity.this);
                    LogRuningTest.printDebug(TAG, "Rear_Camera " + Rear_Camera, CameraActivity.this);
                    break;
                case Rear_Camera_Photograph:
                    camera.takePicture(null, null, picture);
                    mReliabilityCameraHandler.sendEmptyMessageDelayed(Camera_RELEASE, Reliability_Camera_CHANGE_TIME);
                    break;
                case Camera_RELEASE:
                    if(camera != null){
                        camera.stopPreview();
                        camera.release();
                        camera = null;
                    }
                    if(cv != null){
                        cv = null ;
                    }
                    if(loop <= 50){
                        mReliabilityCameraHandler.sendEmptyMessageDelayed(Rear_Camera, 3*1000);
                    }else {
                        gotoAudioTestActivity();
                    }
                    break;
            }
            super.handleMessage(msg);
            }catch (Exception e){
                exception = Log.getStackTraceString(e);
                LogRuningTest.printDebug(TAG, "takePicture fail" + exception, CameraActivity.this);
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt("CameraERRcount", ++mCameraERRcount);
                editor.commit();
                LogRuningTest.printInfo(TAG, "CameraERRcount  =" + mCameraERRcount, CameraActivity.this);
                gotoAudioTestActivity();
            }
        }
    };

    public void gotoAudioTestActivity(){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        mCameraERRcount = mSharedPreferences.getInt("CameraERRcount", 0);
        LogRuningTest.printInfo(TAG, "gotoAudioTestActivity  mCameraERRcount =" + mCameraERRcount, CameraActivity.this);
        if (mCameraERRcount >6) {
            editor.putBoolean("camera_test", false);
        }
        editor.commit();
        Intent intent = new Intent(TestService.ACTION_AUDIO_TEST);
        sendBroadcast(intent);
        finish();
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