package com.example.secondhandstreet.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;

/**
 * Created by huangxueqin on 15-4-16.
 */
public class Utilities {
    public static ProgressDialog getProgressDialog(Context context) {
        ProgressDialog
        progressDialog = new ProgressDialog(context);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        return progressDialog;
    }

    public static Bitmap getRotateBitmap(Bitmap b, float rotateDegree){
        Matrix matrix = new Matrix();
        matrix.postRotate((float) rotateDegree);
        Bitmap rotaBitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, false);
        return rotaBitmap;
    }

    public static Point getImageSize(Uri bitmapUri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(bitmapUri.getPath(), options);
        Point result = new Point();
        result.x = options.outWidth;
        result.y = options.outHeight;
        return result;
    }

    public static boolean validPassword(String password) {
        if(password != null && password.length() >= 6) {
            return true;
        }
        return false;
    }

    public static  boolean validPhoneNumber(String phoneNumber) {
        if(phoneNumber != null) {
            if(phoneNumber.length() == 11) {
                for(int i = 0; i < 11; i++) {
                    char c = phoneNumber.charAt(i);
                    if(c > '9' || c < '0')
                        return false;
                }
                return true;
            }
        }
        return false;
    }

    public static  boolean validQQ(String QQ) {
        if(QQ != null) {
            for(int i = 0; i < QQ.length(); i++) {
                char c = QQ.charAt(i);
                if(c > '9' || c < '0')
                    return false;
            }
            return true;
        }
        return false;
    }
}
