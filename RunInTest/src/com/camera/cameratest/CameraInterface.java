package com.camera.cameratest;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.android.runintest.LogRuningTest;
import com.android.runintest.TestService;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;  
import android.graphics.BitmapFactory;  
import android.graphics.PixelFormat;  
import android.hardware.Camera;  
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;  
import android.hardware.Camera.ShutterCallback;  
import android.hardware.Camera.Size;  
import android.media.CameraProfile;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.util.Log;  
import android.view.SurfaceHolder;  
import android.content.SharedPreferences;
import android.app.Activity;

/* < 0075134 xuyinwen 20151102 begin */
public class CameraInterface {
    private static final String TAG = "CameraInterface";
    private Camera mCamera;
    private Camera.Parameters mParams;
    private boolean isPreviewing = false;
    private float mPreviwRate = -1f;
    private static int mCameraId = 0;
    private static CameraInterface mCameraInterface;
    private static int SWITCH_CAMERA = 1;
    private static int STOP_CAMERA =2;
    /* < 0077694 xuyinwen 20151202 begin */
    private static int OPEN_CAMERA =3;
    /* 0077694 xuyinwen 20151202 end > */
    private static int TAKE_PHONES = 4;
    
    private Context mContext = null;
    private CamOpenOverCallback  mCallback = null;
    private Handler mHandler = null;
    private static int currentZoom = 0;
    private boolean mTestSuccess = true;
    private int SWITCH_CAMERA_DELAY_TIME = 5000;
    /* < 0078018 xuyinwen 20151215 begin */
    /* < 0077694 xuyinwen 20151202 begin */
    private int START_CAMERA_DELAY_TIME = 5000;
    /* 0077694 xuyinwen 20151202 end > */
    private int mFailCount = 0;
    /* 0078018 xuyinwen 20151215 end > */
    private boolean mTakeingPicture = false;
    
    private int mSwitchId = 0;

    public interface CamOpenOverCallback {
        /* < 0078018 xuyinwen 20151208 begin */
        public void cameraHasOpened(boolean needUpdateZoom);
        /* 0078018 xuyinwen 20151208 end > */
    }

    private CameraInterface(Context context) {
        mContext = context;
        mHandler =new Handler(mContext.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                /* < 0077694 xuyinwen 20151202 begin */
            	    //执行拍照动作
            	if (msg.what == SWITCH_CAMERA) {
                    doTakePicture();
                    //停止摄像，并删除照片文件，然后跳转Audio测试
                } else if (msg.what == STOP_CAMERA) {
                    FileUtil.DeleteFile(new File(FileUtil.storagePath));
                    gotoAudioTestActivity(mContext);
                    //打开摄像
                } else if (msg.what == OPEN_CAMERA) {
                	if(CameraActivity.count < CameraActivity.REAR_CAMERA){
                		mSwitchId = 0 ;
                	}else{
                		mSwitchId = 1;
                	}
                    /* < 0078018 xuyinwen 20151208 begin */
                    if (CamParaUtil.CameraNumber()>1) {
                        /* < 0078018 xuyinwen 20151215 begin */
                    	
                        if (mTestSuccess) {
                            if (mCameraId == 0) {
                                doOpenCamera(mCallback, mSwitchId, false);
                                mHandler.sendEmptyMessageDelayed(SWITCH_CAMERA, SWITCH_CAMERA_DELAY_TIME);
                            } else {
                                doOpenCamera(mCallback, mSwitchId, false);
                                updateCameraParametersZoom();
                            }
                        } else {
                            if (mCameraId == 0) {
                                doOpenCamera(mCallback, 0, false);
                                updateCameraParametersZoom();
                            } else {
                                doOpenCamera(mCallback, 1, false);
                                mHandler.sendEmptyMessageDelayed(SWITCH_CAMERA, SWITCH_CAMERA_DELAY_TIME);
                            }
                        }
                        /* 0078018 xuyinwen 20151215 end > */
                    } else {
                        doOpenCamera(mCallback, 0, false);
                        updateCameraParametersZoom();
                    }
                    /* 0078018 xuyinwen 20151208 end > */
                }
                /* 0077694 xuyinwen 20151202 end > */
                super.handleMessage(msg);
            }
        };
    }

    public void gotoAudioTestActivity(Context context){
    	LogRuningTest.printInfo(TAG, "send broadcast", context);
    	SharedPreferences sharedPreferences = context.getSharedPreferences("runintest", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (!mTestSuccess) {
            editor.putBoolean("camera_test", mTestSuccess);
        }
        editor.commit();
        Intent intent = new Intent(TestService.ACTION_AUDIO_TEST);
        context.sendBroadcast(intent);
        CameraActivity.instance.finish();
    }
   /* public void gotoEMMCActivity(Context context){
        LogRuningTest.printInfo(TAG, "send broadcast", context);
        SharedPreferences sharedPreferences = context.getSharedPreferences("runintest", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
         < 0067632 xuyinwen 20150813 begin 
        if (!mTestSuccess) {
            editor.putBoolean("camera_test", mTestSuccess);
        }
         0067632 xuyinwen 20150813 end > 
        editor.commit();
        Intent intent = new Intent(TestService.ACTION_EMMC_TEST);
        context.sendBroadcast(intent);
        CameraActivity.instance.finish();
    }*/
    
    public static synchronized CameraInterface getInstance(Context context){  
        if(mCameraInterface == null){  
            mCameraInterface = new CameraInterface(context);  
        }  
        return mCameraInterface;  
    }  
    /**打开Camera 
     * @param callback 
     */  
    /* < 0078018 xuyinwen 20151208 begin */
    public void doOpenCamera(CamOpenOverCallback callback,int cameraId, boolean needUpdateZoom){
        LogRuningTest.printInfo(TAG, "Camera open....", mContext);
        mCallback = callback;
        mCameraId = cameraId;
        Log.d("xinsi","cameraId:"+cameraId);
        try {
            mCamera = Camera.open(cameraId);
        } catch (RuntimeException e) {
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
            }
            mTestSuccess = false;
            LogRuningTest.printDebug(TAG, "result:open camera test failed", mContext);
            LogRuningTest.printError(TAG, "reason:" + Log.getStackTraceString(e), mContext);
        }
        LogRuningTest.printInfo(TAG, "Camera open over....", mContext);
        callback.cameraHasOpened(needUpdateZoom);
    }
    /* 0078018 xuyinwen 20151208 end > */
    public void updateCameraParametersZoom() {
        // Set zoom.
        int minZoomValue = 0;
        int maxZoomValue = 0;
        if (mCamera !=null) {
            mParams = mCamera.getParameters();
            if (mParams != null && mParams.isZoomSupported()) {
                maxZoomValue = mParams.getMaxZoom();
                if (currentZoom == minZoomValue) {
                    LogRuningTest.printInfo(TAG, "minZoomValue:" + minZoomValue, mContext);
                    mParams.setZoom(maxZoomValue);
                    currentZoom = maxZoomValue;
                } else {
                    LogRuningTest.printInfo(TAG, "maxZoomValue:" + maxZoomValue, mContext);
                    mParams.setZoom(minZoomValue);
                    currentZoom = minZoomValue;
                }
            }

            List<String> supportedFlash = mParams.getSupportedFlashModes();
            if (CamParaUtil.isSupported(supportedFlash)) {
                mParams.setFlashMode("on");
                //是否支持闪光灯
            }

            try {
                mCamera.setParameters(mParams);
            } catch (RuntimeException e) {
                mTestSuccess = false;
                LogRuningTest.printDebug(TAG, "result:camera test failed", mContext);
                LogRuningTest.printError(TAG, "reason:" + Log.getStackTraceString(e), mContext);
            }

        }
        /* < 0078018 xuyinwen 20160108 begin */
        mHandler.sendEmptyMessageDelayed(SWITCH_CAMERA, SWITCH_CAMERA_DELAY_TIME);
        /* 0078018 xuyinwen 20160108 end > */
    }
    /**开启预览 
     * @param holder 
     * @param previewRate 
     */  
    public void doStartPreview(SurfaceHolder holder, float previewRate) {
        LogRuningTest.printInfo(TAG, "doStartPreview....", mContext);
        if (isPreviewing) {
            mCamera.stopPreview();
            return;
        }

        if (mCamera != null) {
            mParams = mCamera.getParameters();
            mParams.setPictureFormat(PixelFormat.JPEG);//设置拍照后存储的图片格式  
            CamParaUtil.getInstance().printSupportPictureSize(mParams);
            CamParaUtil.getInstance().printSupportPreviewSize(mParams);
            //设置PreviewSize和PictureSize
            Size pictureSize = CamParaUtil.getInstance().getPropPictureSize(
                    mParams.getSupportedPictureSizes(),previewRate, 800);
            mParams.setPictureSize(pictureSize.width, pictureSize.height);
            Size previewSize = CamParaUtil.getInstance().getPropPreviewSize(
                    mParams.getSupportedPreviewSizes(), previewRate, 800);
            mParams.setPreviewSize(previewSize.width, previewSize.height);

            mCamera.setDisplayOrientation(90);

            CamParaUtil.getInstance().printSupportFocusMode(mParams);
            List<String> focusModes = mParams.getSupportedFocusModes();
            if(focusModes.contains("continuous-video")){
                mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }

            try {
                mCamera.setParameters(mParams);
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();//开启预览  
            } catch (IOException e) {
                // TODO Auto-generated catch block  
                String exception = Log.getStackTraceString(e);
                LogRuningTest.printDebug(TAG, "result:camera test failed", mContext);
                LogRuningTest.printError(TAG, "reason:" + exception, mContext);
                mTestSuccess = false;
            } catch (RuntimeException e) {
                mTestSuccess = false;
                LogRuningTest.printDebug(TAG, "result:camera test failed", mContext);
                LogRuningTest.printError(TAG, "reason:" + Log.getStackTraceString(e), mContext);
            }

            isPreviewing = true;
            mPreviwRate = previewRate;

            mParams = mCamera.getParameters(); //重新get一次  
        }
    }
    /** 
     * 停止预览，释放Camera 
     */  
    public void doStopCamera() {
        LogRuningTest.printInfo(TAG, "doStopCamera", mContext);
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);  
            mCamera.stopPreview();   
            isPreviewing = false;   
            mPreviwRate = -1f;  
            mCamera.release();  
            mCamera = null;       
        }  
    }  
    /** 
     * 拍照 
     */  
    public void doTakePicture() {
        LogRuningTest.printInfo(TAG, "doTakePicture", mContext);
        if (isPreviewing && !mTakeingPicture && (mCamera != null) && CameraActivity.count<(CameraActivity.REAR_CAMERA +CameraActivity.FRONT_CAMERA)) {
            try {
                mTakeingPicture = true;
                mCamera.takePicture(mShutterCallback, null, mJpegPictureCallback);
                /* < 0078018 xuyinwen 20151215 begin */
                CameraActivity.count +=1;
                Intent updatecount = new Intent("update.count");
                mContext.sendBroadcast(updatecount);
                /* 0078018 xuyinwen 20151215 end > */
            } catch (RuntimeException e) {
                mTestSuccess = false;
                LogRuningTest.printDebug(TAG, "result:camera test failed", mContext);
                LogRuningTest.printError(TAG, "reason:" + Log.getStackTraceString(e), mContext);
                doStopCamera();
                /* < 0078018 xuyinwen 20151215 begin */
                mFailCount++;
                if (mFailCount >= 3) {
                    mHandler.sendEmptyMessageDelayed(STOP_CAMERA, 1000);
                } else {
                    if (CameraActivity.count <(CameraActivity.REAR_CAMERA +CameraActivity.FRONT_CAMERA)) {
                        mTakeingPicture = false;
                        mHandler.sendEmptyMessageDelayed(OPEN_CAMERA, START_CAMERA_DELAY_TIME);
                    } else {
                        mHandler.sendEmptyMessageDelayed(STOP_CAMERA, 1000);
                    }
                }
                /* 0078018 xuyinwen 20151215 end > */
            }
        }
    }  
  
    /*为了实现拍照的快门声音及拍照保存照片需要下面三个回调变量*/  
    ShutterCallback mShutterCallback = new ShutterCallback() {
    //快门按下的回调，在这里我们可以设置类似播放“咔嚓”声之类的操作。默认的就是咔嚓。  
        public void onShutter() {
            // TODO Auto-generated method stub  
            LogRuningTest.printInfo(TAG, "mShutterCallback...", mContext);
        }
    };

    PictureCallback mJpegPictureCallback = new PictureCallback() {
    //对jpeg图像数据的回调,最重要的一个回调  
        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub  
            LogRuningTest.printInfo(TAG, "myJpegCallback:onPictureTaken...", mContext);
            Bitmap b = null;
            if (null != data) {
                b = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图  
                isPreviewing = false;
            }

            //保存图片到sdcard  
            if (null != b) {
                //设置FOCUS_MODE_CONTINUOUS_VIDEO)之后，myParam.set("rotation", 90)失效。  
                //图片竟然不能旋转了，故这里要旋转下  
                Bitmap rotaBitmap = ImageUtil.getRotateBitmap(b, 90.0f);
                FileUtil.saveBitmap(rotaBitmap);
            }

            /* < 0077694 xuyinwen 20151202 begin */
            doStopCamera();
            /* < 0078018 xuyinwen 20151215 begin */
            mFailCount = 0;
            mTestSuccess = true;
            /* 0078018 xuyinwen 20151215 end > */
            if (CameraActivity.count <(CameraActivity.REAR_CAMERA +CameraActivity.FRONT_CAMERA)) {
                mTakeingPicture = false;
                mHandler.sendEmptyMessageDelayed(OPEN_CAMERA, START_CAMERA_DELAY_TIME);
            } else {
                mHandler.sendEmptyMessageDelayed(STOP_CAMERA, 1000);
            }
            /* 0077694 xuyinwen 20151202 end > */
        }
    };
}
/* 0075134 xuyinwen 20151102 end > */
