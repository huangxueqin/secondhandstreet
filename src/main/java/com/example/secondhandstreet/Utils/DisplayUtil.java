package com.example.secondhandstreet.Utils;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by huangxueqin on 15-4-21.
 */

public class DisplayUtil {
    public static int dip2px(Context context, float dipValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int)(density * dipValue);
    }

    public static int px2dip(Context context, float pxValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int)(pxValue / density + 0.5);
    }

    public static Point getScreenSize(Context context) {
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    /**
     *
     * @param context
     * @return  screenHeight / screenWidth
     */
    public float getScreenRatio(Context context) {
        Point screenSize = getScreenSize(context);
        float w = screenSize.x;
        float h = screenSize.y;
        return h / w;
    }
}
