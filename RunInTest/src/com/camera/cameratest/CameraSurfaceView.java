package com.camera.cameratest;

/* < 0077694 xuyinwen 20151202 begin */
import com.android.runintest.LogRuningTest;
/* 0077694 xuyinwen 20151202 end > */

import android.content.Context;  
import android.graphics.PixelFormat;  
import android.util.AttributeSet;  
import android.util.Log;  
import android.view.SurfaceHolder;  
import android.view.SurfaceView;  

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {  
    private static final String TAG = "CameraSurfaceView";  
    CameraInterface mCameraInterface;  
    Context mContext;  
    /* < 0077694 xuyinwen 20151202 begin */
    SurfaceHolder mSurfaceHolder;
    private static boolean mSurfaceHasChanged = false;
    /* 0077694 xuyinwen 20151202 end > */

    public CameraSurfaceView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        // TODO Auto-generated constructor stub  
        mContext = context;  
        mSurfaceHolder = getHolder();  
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);//translucent半透明 transparent透明 
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);  
        mSurfaceHolder.addCallback(this);  
    }  
  
    @Override  
    public void surfaceCreated(SurfaceHolder holder) {  
        // TODO Auto-generated method stub  
        /* < 0077694 xuyinwen 20151202 begin */
        LogRuningTest.printInfo(TAG, "surfaceCreated....", mContext);
        /* 0077694 xuyinwen 20151202 end > */
    }  
  
    @Override  
    public void surfaceChanged(SurfaceHolder holder, int format, int width,  
            int height) {  
        // TODO Auto-generated method stub  
        /* < 0077694 xuyinwen 20151202 begin */
        mSurfaceHasChanged = true;
        LogRuningTest.printInfo(TAG, "surfaceChanged....", mContext);
        /* 0077694 xuyinwen 20151202 end > */
    }  

    /* < 0075134 xuyinwen 20151102 begin */
    @Override  
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub  
        /* < 0077694 xuyinwen 20151202 begin */
        mSurfaceHasChanged = false;
        LogRuningTest.printInfo(TAG, "surfaceDestroyed....", mContext);
        /* 0077694 xuyinwen 20151202 end > */
    }
    /* 0075134 xuyinwen 20151102 end > */

    public SurfaceHolder getSurfaceHolder() {
        return mSurfaceHolder;
    }

    /* < 0077694 xuyinwen 20151202 begin */
    public static boolean surfaceHasChanged() {
        return mSurfaceHasChanged;
    }
    /* 0077694 xuyinwen 20151202 end > */
}
