package com.android.runintest;

import java.io.File;  
import java.io.FileOutputStream;  
import java.io.IOException;  
import java.util.Date;  
  
import android.app.Activity;  
import android.content.pm.ActivityInfo;  
import android.graphics.PixelFormat;  
import android.hardware.Camera;  
import android.os.AsyncTask;  
import android.os.Bundle;  
import android.os.Environment;  
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;  
import android.util.Log;  
import android.view.KeyEvent;  
import android.view.SurfaceHolder;  
import android.view.SurfaceView;  
import android.view.View;  
import android.view.Window;  
import android.view.View.OnClickListener;  
import android.widget.Button;  
/* < 0075134 xuyinwen 20151102 begin */
import java.lang.RuntimeException;
/* 0075134 xuyinwen 20151102 end > */

public class OpenCameraActivity extends Activity{  
  
    private final static String TAG = "CameraActivity";  
    private SurfaceView surfaceView;  
    private SurfaceHolder surfaceHolder;  
    private Camera camera;  
    private File picture;  
    private Button btnSave;  
    private final int REPEATED_PHOTO = 0;
    private final int REPEATED_PHOTO_TIME = 10 * 1000;
    private final int REPEATED_PHOTO_NUMBER = 5;
    private int mPhotoCount = 0; 
    
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);  
        setContentView(R.layout.activity_repeated_photo);  
        setupViews();  
        mRepeatedPhotoHandler.sendEmptyMessageDelayed(REPEATED_PHOTO, getIntent().getIntExtra("open_time", 10000));
    }  
      
    private void setupViews(){  
        surfaceView = (SurfaceView) findViewById(R.id.camera_preview); // Camera interface to instantiate components  
        surfaceHolder = surfaceView.getHolder(); // Camera interface to instantiate components  
        surfaceHolder.addCallback(surfaceCallback); // Add a callback for the SurfaceHolder  
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);  
         
    }  

    /* < 0075134 xuyinwen 20151102 begin */
    Handler mRepeatedPhotoHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REPEATED_PHOTO:
                    finish();
                    break;
                }
            super.handleMessage(msg);
        }
    };
    /* 0075134 xuyinwen 20151102 end > */
      
    @Override  
    public boolean onKeyDown(int keyCode, KeyEvent event) {  
        if (keyCode == KeyEvent.KEYCODE_CAMERA  
                || keyCode == KeyEvent.KEYCODE_SEARCH) {  
            return true;  
        }  
        return super.onKeyDown(keyCode, event);  
    }  
  
  
    // Photo call back  
    Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {  
        //@Override  
        public void onPictureTaken(byte[] data, Camera camera) {  
            new SavePictureTask().execute(data);  
            camera.startPreview();  
        }  
    };  
  
    // save pic  
    class SavePictureTask extends AsyncTask<byte[], String, String> {  
        @Override  
        protected String doInBackground(byte[]... params) {   
            return null;  
        }  
    }  

    /* < 0075134 xuyinwen 20151102 begin */
    // SurfaceHodler Callback handle to open the camera, off camera and photo size changes  
    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {  
  
        public void surfaceCreated(SurfaceHolder holder) {
            LogRuningTest.printInfo(TAG, "surfaceCreated", OpenCameraActivity.this);
            int cameraCount = Camera.getNumberOfCameras();
            try {
                camera = Camera.open();
                camera.setPreviewDisplay(holder);   
            } catch (RuntimeException e) {
                if (camera != null) {
                    camera.release();
                    camera = null;
                }
                IntegratedTestActivity.setCameraTest(false);
                LogRuningTest.printDebug(TAG, "result:open camera test failed", OpenCameraActivity.this);
                LogRuningTest.printError(TAG, "reason:" + Log.getStackTraceString(e), OpenCameraActivity.this);
            } catch (IOException e) {
                if (camera != null) {
                    camera.release();
                    camera = null;
                }
                IntegratedTestActivity.setCameraTest(false);
                LogRuningTest.printDebug(TAG, "result:open camera test failed", OpenCameraActivity.this);
                LogRuningTest.printError(TAG, "reason:" + Log.getStackTraceString(e), OpenCameraActivity.this);
            }
        }  
  
        public void surfaceChanged(SurfaceHolder holder, int format, int width,  
                int height) {
            LogRuningTest.printInfo(TAG, "surfaceChanged", OpenCameraActivity.this);
            if (null == camera) {
                IntegratedTestActivity.setCameraTest(false);
                LogRuningTest.printDebug(TAG, "result:camera test failed", OpenCameraActivity.this);
                LogRuningTest.printError(TAG, "reason:camera is null", OpenCameraActivity.this);
                return;
            }

            try {
                Camera.Parameters parameters = camera.getParameters();
                camera.setDisplayOrientation(0);
                camera.setParameters(parameters);
                camera.startPreview();
            } catch (RuntimeException e) {
                if (camera != null) {
                    camera.release();
                    camera = null;
                }
                IntegratedTestActivity.setCameraTest(false);
                LogRuningTest.printDebug(TAG, "result:camera test failed", OpenCameraActivity.this);
                LogRuningTest.printError(TAG, "reason:" + Log.getStackTraceString(e), OpenCameraActivity.this);
            }
        }
  
        public void surfaceDestroyed(SurfaceHolder holder) {
            LogRuningTest.printInfo(TAG, "surfaceDestroyed", OpenCameraActivity.this);
            if (null == camera) {
                IntegratedTestActivity.setCameraTest(false);
                LogRuningTest.printDebug(TAG, "result:camera test failed", OpenCameraActivity.this);
                LogRuningTest.printError(TAG, "reason:camera is null", OpenCameraActivity.this);
                return;
            }

            try {
                camera.setPreviewCallback(null);
                camera.stopPreview();
                camera.release();
                camera = null;
                /* < 0078018 xuyinwen 20151215 begin */
                IntegratedTestActivity.setCameraTest(true);
                /* 0078018 xuyinwen 20151215 end > */
            } catch (RuntimeException e) {
                if (camera != null) {
                    camera.release();
                    camera = null;
                }
                IntegratedTestActivity.setCameraTest(false);
                LogRuningTest.printDebug(TAG, "result:camera test failed", OpenCameraActivity.this);
                LogRuningTest.printError(TAG, "reason:" + Log.getStackTraceString(e), OpenCameraActivity.this);
            }
        }
    };
    /* 0075134 xuyinwen 20151102 end > */
    
    protected void onDestroy() {
    	super.onDestroy();
    	mRepeatedPhotoHandler.removeMessages(REPEATED_PHOTO);
    };
} 
