package com.android.runintest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;
import android.util.Log;

public class RuningTestConfig {

    public static String CHARGING_ENABLE_PATCH = "sys/class/power_supply/battery/charging_enabled";
    public static String CHARGING_ENABLE = "1";
    public static String CHARGING_DISABLE = "0";
    /*
     * /sys/gtp_test/opentest --------解析出“SUCCEED"为成功 /sys/gtp_test/shorttest
     * --------解析出“PASS”为成功
     */
    public static String readFile(String filePath) {
        String res = "";
        BufferedReader br;
        try {
             br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File(filePath))));
            String str = null;
            while ((str = br.readLine()) != null) {
                /* < 0072103 xuyinwen 20150922 begin */
                res += str;
                /* 0072103 xuyinwen 20150922 end > */
            }
            br.close();
        } catch (Exception e) {
            return Log.getStackTraceString(e);
        }
        return res;
    }

    public static void writeToFile(String patch, String value, String TAG,
            Context context) {
        String exception = null;
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(patch);
            outStream.write(value.getBytes());

        } catch (FileNotFoundException e) {
            exception = Log.getStackTraceString(e);
        } catch (IOException e) {
            exception = Log.getStackTraceString(e);
        }finally {
            try {
                if(null != outStream) {
                    outStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (null != exception) {
            LogRuningTest.printDebug(TAG, "result:writeToFile failed", context);
            LogRuningTest.printError(TAG, "reason:" + exception, context);
        }
    }

}
