package com.camera.cameratest;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

//import android.media.MediaScanner;

public class FileUtil {
    private static final String TAG = "FileUtil";
    private static final File parentPath = Environment
            .getExternalStorageDirectory();
    public static String storagePath = "";
    private static final String DST_FOLDER_NAME = "PlayCamera";
    private static String[] mExternalStoragePaths;
    public static final String EXTERNAL_VOLUME = "external";
    public static Context mContext = null;

    /**
     * 初始化保存路径
     * 
     * @return
     */
    private static String initPath() {
        if (storagePath.equals("")) {
            storagePath = parentPath.getAbsolutePath() + "/" + DST_FOLDER_NAME;
            File f = new File(storagePath);
            if (!f.exists()) {
                f.mkdir();
            }
        }
        return storagePath;
    }

    public static void setContext(Context context) {
        mContext = context;
    }

    /**
     * 保存Bitmap到sdcard
     * 
     * @param b
     */
    public static void saveBitmap(Bitmap b) {

        String path = initPath();
        long dataTake = System.currentTimeMillis();
        final String jpegName = path + "/" + dataTake + ".jpg";
        Log.i(TAG, "saveBitmap:jpegName = " + jpegName);
        try {
            FileOutputStream fout = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            Log.i(TAG, "saveBitmap成功");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.i(TAG, "saveBitmap:失败");
            e.printStackTrace();
        }
        File jpgFile = new File(jpegName);

    }

    public static void DeleteFile(File file) {
        if(file.isFile()){
            file.delete();
            return;
        }
        if(file.isDirectory()){
            File[] childFile = file.listFiles();
            if(childFile == null || childFile.length == 0){
                file.delete();
                return;
            }
            for(File f : childFile){
                DeleteFile(f);
            }
            file.delete();
        }
    }
}
