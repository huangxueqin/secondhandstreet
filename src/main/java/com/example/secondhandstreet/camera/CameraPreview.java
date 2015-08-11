package com.example.secondhandstreet.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.secondhandstreet.Utils.LogUtil;

/**
 * Created by huangxueqin on 15-4-21.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{
    private Context mContext;
    private SurfaceHolder mSurfaceHolder;
    private CameraInterface mCameraInterface;
    private CameraInterface.CameraOpenOverCallback mCameraOpenOverCallback;

    private boolean mDrawingViewSet = false;
    private CameraDrawingView mDrawingView;
    private Rect mFocusRect = new Rect();
    private static final int sFocusAreaSize = 100;

    private Handler mHandler;

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHandler = new Handler(Looper.getMainLooper());
        mContext = context;
        mSurfaceHolder = super.getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(this);
        mCameraInterface = new CameraInterface();
    }

    public void setCameraOpenOverCallback(CameraInterface.CameraOpenOverCallback callback) {
        mCameraOpenOverCallback = callback;
    }

    public interface OnFocusCallback {
        void onFocusCompleted(boolean success);
    }

    public void setDrawingView(CameraDrawingView cameraDrawingView) {
        mDrawingViewSet = true;
        mDrawingView = cameraDrawingView;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCameraInterface.openCamera(mCameraOpenOverCallback);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        int prevWidth = CameraPreview.this.getMeasuredWidth();
        int prevHeight = CameraPreview.this.getMeasuredHeight();
        mCameraInterface.setupCameraParams(holder, prevWidth, prevHeight);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCameraInterface.releaseCamera();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            int touchX = (int) event.getX();
            int touchY = (int) event.getY();

            // should get attention here, for api 's coordinates is horizontal
            int focusLeft = touchY - sFocusAreaSize;
            int focusRight = focusLeft + sFocusAreaSize*2;
            int focusTop = getWidth() - touchX - sFocusAreaSize;
            int focusBottom = focusTop + sFocusAreaSize*2;
            mFocusRect.set(focusLeft*2000/getWidth() - 1000,
                    focusTop*2000/getHeight() - 1000,
                    focusRight*2000/getWidth() - 1000,
                    focusBottom*2000/getHeight() - 1000);

            LogUtil.logd("width = " + getWidth() + ", height = " + getHeight());
            LogUtil.logd(mFocusRect.toString());

            doTouchFocus(mFocusRect, mFocusCallback);
            if(mDrawingViewSet) {
                mDrawingView.setFocusPoint(touchX, touchY);
            }
        }
        return super.onTouchEvent(event);
    }

    private OnFocusCallback mFocusCallback = new OnFocusCallback() {
        @Override
        public void onFocusCompleted(boolean success) {
            LogUtil.logd("focus + " + success);
            if(mDrawingViewSet) {
                mDrawingView.clearFocus();
            }
        }
    };

    private void doTouchFocus(Rect focusRect, OnFocusCallback focusCallback) {
        mCameraInterface.doTouchFocus(focusRect, focusCallback);
    }

    public void startPreview() {
        if(mCameraInterface != null) {
            mCameraInterface.startPreview();
        }
    }

    public void stopPreview() {
        if(mCameraInterface != null) {
            mCameraInterface.stopPreview();
        }
    }

    public void takePicture(Camera.PictureCallback callback) {
        mCameraInterface.takePicture(callback);
    }

    public SurfaceHolder getHolder() {
        return mSurfaceHolder;
    }
}
