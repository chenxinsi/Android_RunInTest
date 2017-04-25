package com.camera.cameratest;

import com.android.runintest.R;
import com.camera.cameratest.CameraInterface.CamOpenOverCallback;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.app.Activity;  
import android.graphics.Point;  
import android.os.Bundle;  
import android.view.Menu;  
import android.view.SurfaceHolder;  
import android.view.View;  
import android.view.View.OnClickListener;  
import android.view.ViewGroup.LayoutParams;  
import android.widget.ImageButton; 
import android.widget.TextView;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.WindowManager;
/* < 0067427 xuyinwen 20150811 begin */
import android.view.KeyEvent;
import android.content.SharedPreferences;

import com.android.runintest.BaseActivity;
import com.android.runintest.LogRuningTest;
import com.android.runintest.TestService;
/* 0067427 xuyinwen 20150811 end > */

/* < 0075134 xuyinwen 20151102 begin */
public class CameraActivity extends BaseActivity implements CamOpenOverCallback {
    private static final String TAG = "CameraActivity";
    private CameraActivity cameraActivity;
    CameraSurfaceView surfaceView = null;
    TextView displaycount;
    float previewRate = -1f;
    /* < 0077694 xuyinwen 20151202 begin */
    private static final int START_PREVIEW = 1;
    private static final int SETPARMETER = 2;
    private static final int STOP_CAMERA =3;
    public static int mWaitTimes = 0;
    public static final int MAX_WAIT_TIMES = 3;
    /* 0077694 xuyinwen 20151202 end > */
    private BroadcastReceiver cameraReceiver = null;
    private Context mContext = null;
    public static int count = 0;
    //public static final int MAX_COUNT = 120;
    public static final int MAX_COUNT = 10;
    
    public static final int FRONT_CAMERA = 20;
    
    public static final int REAR_CAMERA = 30;
    
    public static CameraActivity instance = null;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            /* < 0077694 xuyinwen 20151202 begin */
            if(msg.what == SETPARMETER) {
                CameraInterface.getInstance(CameraActivity.this).updateCameraParametersZoom();
            } else if (msg.what == START_PREVIEW) {
                if (CameraSurfaceView.surfaceHasChanged()) {
                    SurfaceHolder holder = surfaceView.getSurfaceHolder();
                    CameraInterface.getInstance(mContext).doStartPreview(holder, previewRate);
                    mHandler.sendEmptyMessageDelayed(SETPARMETER, 5000);
                } else if  (MAX_WAIT_TIMES > mWaitTimes) {
                    mHandler.sendEmptyMessageDelayed(START_PREVIEW, 1000);
                    mWaitTimes++;
                } else {
                    CameraInterface.getInstance(mContext).doStopCamera();
                    mHandler.sendEmptyMessageDelayed(STOP_CAMERA, 1000);
                }
            } else if (msg.what == STOP_CAMERA) {
                CameraInterface.getInstance(mContext).gotoAudioTestActivity(mContext);
            }
            /* 0077694 xuyinwen 20151202 end > */
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
        cameraActivity = new CameraActivity();
        cameraActivity.isMonkeyRunning(TAG, "onCreate", CameraActivity.this);
        overridePendingTransition(0, 0);
        instance = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mContext = getApplicationContext();
        registerBroadCast();
        FileUtil.setContext(getApplicationContext());

        Thread openThread = new Thread(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
                /* < 0078018 xuyinwen 20151208 begin */
                CameraInterface.getInstance(mContext).doOpenCamera(CameraActivity.this,0, true);
                /* 0078018 xuyinwen 20151208 end > */
            }
        };
        setContentView(R.layout.activity_camera);  
        initUI();  
        initViewParams();
        openThread.start();
    }  
    
    @Override
    protected void onResume() {
        super.onResume();
        cameraActivity.isMonkeyRunning(TAG, "onResume", CameraActivity.this);
    }

    private void registerBroadCast(){
        cameraReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                // TODO Auto-generated method stub
                LogRuningTest.printInfo(TAG, "action==" + arg1.getAction().toString(), mContext);
                if(arg1.getAction().equals("update.count")){
                    displaycount.setText("Count:"+count);
                    LogRuningTest.printInfo(TAG, "count:" + count, mContext);
                }
            }
        };
        IntentFilter cameraFilter = new IntentFilter();
        cameraFilter.addAction("update.count");
        registerReceiver(cameraReceiver, cameraFilter);
    }

    private void initUI(){  
        surfaceView = (CameraSurfaceView)findViewById(R.id.camera_surfaceview);  
        displaycount = (TextView)findViewById(R.id.displaycount);
        displaycount.setText("Count:"+count);
    }  
    private void initViewParams(){  
        LayoutParams params = surfaceView.getLayoutParams();  
        Point p = DisplayUtil.getScreenMetrics(this);  
        params.width = p.x;  
        params.height = p.y;  
        previewRate = DisplayUtil.getScreenRate(this); //默认全屏的比例预览  
        surfaceView.setLayoutParams(params);  
    }  
  @Override
protected void onDestroy() {
	// TODO Auto-generated method stub
	super.onDestroy();
	mHandler.sendEmptyMessage(4);
	unregisterReceiver(cameraReceiver);
}
    @Override  
    /* < 0078018 xuyinwen 20151208 begin */
    public void cameraHasOpened(boolean needUpdateZoom) {
        // TODO Auto-generated method stub  
        /* < 0077694 xuyinwen 20151202 begin */
        if (CameraSurfaceView.surfaceHasChanged()) {
            SurfaceHolder holder = surfaceView.getSurfaceHolder();
            CameraInterface.getInstance(mContext).doStartPreview(holder, previewRate);
            if (needUpdateZoom) {
                mHandler.sendEmptyMessageDelayed(SETPARMETER, 5000);
            }
        } else {
            mHandler.sendEmptyMessageDelayed(START_PREVIEW, 1000);
            mWaitTimes++;
        }
        /* 0077694 xuyinwen 20151202 end > */
    }
    /* 0078018 xuyinwen 20151208 end > */

    /* < 0067427 xuyinwen 20150811 begin */
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
    /* 0067427 xuyinwen 20150811 end > */
}
/* 0075134 xuyinwen 20151102 end > */
