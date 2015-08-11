package com.example.secondhandstreet.Utils;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by huangxueqin on 15-4-21.
 */
public class FileUtil {
    private static final String APP_FOLDER = "shs";
    private static final File rootDir = Environment.getExternalStorageDirectory();
    private static final File appDir = new File(rootDir, APP_FOLDER);
    private static final File photoDir = new File(appDir, "photo");
    private static final File avatarCacheFile = new File(appDir, "thumbnails");

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static final Uri savePhotos(Bitmap b, String name) {
        if(!isExternalStorageWritable()) {
            LogUtil.logd("External Storage is not writable");
            return null;
        }
        Uri photoUri = null;
        if(!photoDir.exists()) {
            photoDir.mkdirs();
        }
        File picFile = new File(photoDir, name + ".jpg");
        if(picFile.exists()) {
            picFile.delete();
        }
        try {
            FileOutputStream fos = new FileOutputStream(picFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            fos.close();
            photoUri = Uri.fromFile(picFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return photoUri;
    }

    public static void deleteFileByUri(Uri uri) {
        File f = new File(uri.getPath());
        if(f.exists()) {
            f.delete();
        }
    }

}
