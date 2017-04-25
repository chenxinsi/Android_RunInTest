package com.android.runintest;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import android.content.Context;
import android.util.Log;
import android.os.ServiceManager;
import android.os.RemoteException;
import com.android.internal.telephony.ITelephony;
import android.content.SharedPreferences;
import android.app.Activity;
import android.text.TextUtils;

import com.qualcomm.qcnvitems.QcNvItems;

public class I2CTest {

    private static final String TAG = "I2CTest";

    private static final String TP_PROBE_DIR = "/sys/class/assist/ctp/exist";

    private static final String GRAVITY_PROBE_DIR = "/sys/class/assist/accel/exist";

    private static final String LIGHT_PROBE_DIR = "/sys/class/assist/alsps/exist";

    private static final String REAR_CAMERA_PROBE_DIR = "/sys/class/camera/rear_camera/probe";

    private static final String FRONT_CAMERA_PROBE_DIR = "/sys/class/camera/front_camera/probe";

    private static String[] mDir = {TP_PROBE_DIR, GRAVITY_PROBE_DIR, LIGHT_PROBE_DIR,
                                REAR_CAMERA_PROBE_DIR, FRONT_CAMERA_PROBE_DIR};

    private static String[] mI2C = {"TP_", "GRAVITY_", "LIGHT_", "REAR_CAMERA_", "FRONT_CAMERA_"};

    public static final int NV_2499_SIZE = 128;
    
    public static final int I2C_SUCCESS = 1;

    public static final int I2C_FAIL = 0;

    public static final byte NV_UNTEST = 'U';

    public static final byte NV_PASS = 'P';

    public static final byte NV_FAIL = 'F';

    public static final int I2C_TEST_TOTAL = 5;

    public static final int FIRST_I2C_NV = NV_2499_SIZE - I2C_TEST_TOTAL;

    public static final int MMI_NV = 3;

    /* < 0069129 xuyinwen 20150828 begin */
    public static final int RUNINTIMES_NV = 61;

    public static final byte TEST_ONCE = '1';

    public static final byte TEST_SEC = '2';

    public static final byte TEST_THR = '3';
    /* 0069129 xuyinwen 20150828 end > */

    private static SharedPreferences mSharedPreferences = null;

    private static QcNvItems mNv = null;

    public I2CTest(Context context) {
        mNv = new QcNvItems(context);
    }

    public static int[] readI2CStatus(Context context) {
        int[] I2CStatus = new int[5];
        DataInputStream input = null;
        
        try {
            for (int i=0; i<5; i++) {
                input = new DataInputStream(
                    new BufferedInputStream(new FileInputStream(mDir[i])));
                String status = input.readLine();
                I2CStatus[i] = Integer.valueOf(status);
                LogRuningTest.printInfo(TAG, mDir[i] + " " + I2CStatus[i], context);
                input.close();
            }
        } catch (FileNotFoundException fnf) {
            LogRuningTest.printDebug(TAG, "result:read file of I2C test failed", context);
            LogRuningTest.printError(TAG, "reason:" + Log.getStackTraceString(fnf), context);
            try {
                if (null != input) {
                    input.close();
                }
            } catch (IOException e1) {
            }
            return null;
        } catch (IOException e) {
            LogRuningTest.printDebug(TAG, "result:read file of I2C test failed", context);
            LogRuningTest.printError(TAG, "reason:" + Log.getStackTraceString(e), context);
            try {
                if (null != input) {
                    input.close();
                }
            } catch (IOException e1) {
            }
            return null;
        }
        return I2CStatus;
    }

    public static byte[] resetI2CNv(Context context) {
        byte[] info = new byte[I2CTest.NV_2499_SIZE];
        if (null == mNv) {
            mNv = new QcNvItems(context);
        }

        try {
            info = mNv.getNvFactoryData3IByte();
        } catch (Exception e) {
            LogRuningTest.printDebug(TAG, "result:reset Nv failed", context);
            LogRuningTest.printError(TAG, "reason:" + Log.getStackTraceString(e), context);
            return null;
        }

        if (null == info) {
            LogRuningTest.printDebug(TAG, "result:reset Nv failed", context);
            LogRuningTest.printError(TAG, "reason:info is null", context);
            return null;
        }

        for (int i=0; i<5; i++) {
            info[FIRST_I2C_NV+i] = NV_UNTEST;
        }

        try {
            mNv.setNvFactoryData3IByte(info);
        } catch (Exception e) {
            LogRuningTest.printDebug(TAG, "result:reset  Nv failed", context);
            LogRuningTest.printError(TAG, "reason:" + Log.getStackTraceString(e), context);
            return null;
        }
        LogRuningTest.printDebug(TAG, "result:reset  Nv success", context);
        return info;
    }

    /* < 0070746 xuyinwen 20150916 begin */
    public static boolean saveReslut(boolean isPass, Context context) {
        byte[] info = new byte[I2CTest.NV_2499_SIZE];
        if (null == mNv) {
            mNv = new QcNvItems(context);
        }
        try {
            info = mNv.getNvFactoryData3IByte();
        } catch (Exception e) {
            LogRuningTest.printDebug(TAG, "result:read Nv failed", context);
            LogRuningTest.printError(TAG, "reason:" + Log.getStackTraceString(e), context);
            return false;
        }

        if (null!=info && info.length>17) {
            LogRuningTest.printDebug(TAG, " info[16]" +  info[16], context);
            /* < 0070677 xuyinwen 20150915 begin */
            LogRuningTest.printDebug(TAG, " info[RUNINTIMES_NV]" +  info[RUNINTIMES_NV], context);
            /* 0070677 xuyinwen 20150915 end > */
            info[16] = isPass?NV_PASS:NV_FAIL;
            /* < 0069129 xuyinwen 20150828 begin */
            info[RUNINTIMES_NV] = info[RUNINTIMES_NV]==0?TEST_ONCE
                :info[RUNINTIMES_NV]==TEST_ONCE?TEST_SEC
                :TEST_THR;
            /* 0069129 xuyinwen 20150828 end > */
        }
        try {
            mNv.setNvFactoryData3IByte(info);
        } catch (Exception e) {
            LogRuningTest.printDebug(TAG, "result:save Reslut NV failed", context);
            LogRuningTest.printError(TAG, "reason:" + Log.getStackTraceString(e), context);
            return false;
        }

        try {
            info = mNv.getNvFactoryData3IByte();
            LogRuningTest.printDebug(TAG, " info[16]" +  info[16], context);
            /* < 0070677 xuyinwen 20150915 begin */
            LogRuningTest.printDebug(TAG, " info[RUNINTIMES_NV]" +  info[RUNINTIMES_NV], context);
            /* 0070677 xuyinwen 20150915 end > */
        } catch (Exception e) {
            LogRuningTest.printDebug(TAG, "result:read Nv failed", context);
            LogRuningTest.printError(TAG, "reason:" + Log.getStackTraceString(e), context);
        }
        return true;
    }
    /* 0070746 xuyinwen 20150916 end > */
}


