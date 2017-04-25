package com.android.runintest;

import java.io.BufferedWriter;  
import java.io.File;
import java.io.FileWriter;  
import java.io.IOException;  
import android.os.Environment;
import android.util.Log;
import java.text.SimpleDateFormat;
import android.os.FileUtils;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.Context;
import android.text.TextUtils;

public class LogRuningTest{

    private static final String TAG = "LogRuningTest";

    private static final String FILE_DIR = "/sdcard/aging";
    private static final String FILE_NAME = "Log_runningtest.txt";

    private LogRuningTest() {}

    public static String CreateText(Context context) {
        File logDir = new File(FILE_DIR, "log");
        SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd");
        String dataTime = tempDate.format(new java.util.Date()).toString();
        if (!logDir.exists()) {
            try {
                logDir.mkdirs();
                FileUtils.setPermissions(logDir.getPath(), 0777, -1, -1);
            } catch (Exception e) {
                return null;
            }
        }
        File runingTestIIDir = new File(logDir, "RunningTestII");
        if (!runingTestIIDir.exists()) {
            try {
                runingTestIIDir.mkdirs();
                FileUtils.setPermissions(runingTestIIDir.getPath(), 0777, -1, -1);
            } catch (Exception e) {
                return null;
            }
        }

        String fileName = runingTestIIDir + "/" + dataTime + FILE_NAME;
        
        File dir = new File(fileName);
        if (!dir.exists()) {
            try {
                dir.createNewFile();
                FileUtils.setPermissions(dir.getPath(), 0777, -1, -1);
                SharedPreferences sharedPreferences = context.getSharedPreferences("runintest", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("fileLogDir", fileName);
                editor.commit();
            } catch (Exception e) {
                return null;
            }
        }
        return fileName;
    }

    private static void write2File(String msg, Context context) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        SharedPreferences sharedPreferences = context.getSharedPreferences("runintest", Activity.MODE_PRIVATE);
        String fileName = sharedPreferences.getString("fileLogDir", "");
        try {
            if (TextUtils.isEmpty(fileName)) {
                fileName = CreateText(context);
            }
            if (TextUtils.isEmpty(fileName)) {
                Log.e(TAG, "Log file can not be create");
                return;
            }
            fw = new FileWriter(fileName, true);
            bw = new BufferedWriter(fw);
            bw.write(msg + "\n");
            bw.newLine();  
            bw.flush();
            bw.close();  
            fw.close();  
        } catch (IOException e) {  
            // TODO Auto-generated catch block   
            e.printStackTrace();  
            try {
                /* < 0069335 xuyinwen 20150831 begin */
                if (bw!=null) {
                    bw.close();
                }
                if (fw!=null) {
                    fw.close();
                }
                /* 0069335 xuyinwen 20150831 end > */
            } catch (IOException e1) {  
                // TODO Auto-generated catch block   
            }  
        }
    }

    public static void printVerbose(String tag, String msg, Context context) {  
        SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
        String datetime = tempDate.format(new java.util.Date()).toString();  
        String myreadline = datetime + "    v    " + tag + "    " + msg;
        write2File(myreadline, context);
        Log.v(tag, msg);
    }

    public static void printDebug(String tag, String msg, Context context) {  
        SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
        String datetime = tempDate.format(new java.util.Date()).toString();  
        String myreadline = datetime + "    d  " + tag + "    " + msg;
        write2File(myreadline, context);
        Log.d(tag, msg);
    }

    public static void printInfo(String tag, String msg, Context context) {  
        SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
        String datetime = tempDate.format(new java.util.Date()).toString();  
        String myreadline = datetime + "    i    " + tag + "    " + msg;
        write2File(myreadline, context);
        Log.i(tag, msg);
    }

    public static void printWaring(String tag, String msg, Context context) {  
        SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
        String datetime = tempDate.format(new java.util.Date()).toString();  
        String myreadline = datetime + "    w    " + tag + "    " + msg;
        write2File(myreadline, context);
        Log.w(tag, msg);
    }

    public static void printError(String tag, String msg, Context context) {  
        SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
        String datetime = tempDate.format(new java.util.Date()).toString();  
        String myreadline = datetime + "    e    " + tag + "    " + msg;
        write2File(myreadline, context);
        Log.e(tag, msg);
    }
}

