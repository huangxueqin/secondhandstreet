package com.example.secondhandstreet.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by huangxueqin on 15-4-23.
 */
public class CameraDrawingView extends View {
    private int[] mFocusPoint = new int[2];
    private Rect mRect;
    private Paint mPaint;
    private boolean mHasFocus = false;

    private static final int sRectSize = 100;

    public CameraDrawingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraDrawingView(Context context) {
        this(context, null);
    }

    public CameraDrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(1);
        mPaint.setColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mHasFocus) {
            int left = mFocusPoint[0];
            int top = mFocusPoint[1];
            mRect.set(left-sRectSize, top-sRectSize, left + sRectSize, top + sRectSize);
            canvas.drawRect(mRect, mPaint);
        }
    }

    public void setFocusPoint(int x, int y) {
        mFocusPoint[0] = x;
        mFocusPoint[1] = y;
        mHasFocus = true;
        invalidate();
    }

    public void clearFocus() {
        mHasFocus = false;
        invalidate();
    }
}
