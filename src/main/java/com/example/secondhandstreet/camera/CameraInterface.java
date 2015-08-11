package com.example.secondhandstreet.camera;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.view.SurfaceHolder;

import com.example.secondhandstreet.Utils.FileUtil;
import com.example.secondhandstreet.Utils.LogUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangxueqin on 15-4-21.
 */
public class CameraInterface {
    private Camera mCamera;
    private boolean mIsPreviewing;

    public interface CameraOpenOverCallback {
        void onCameraOpened(boolean success);
    }

    public void openCamera(CameraOpenOverCallback callback) {
        boolean qOpened = false;
        try {
            releaseCamera();
            mCamera = Camera.open();
            qOpened = mCamera != null;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        if(callback != null) {
            callback.onCameraOpened(qOpened);
        }
    }

    public void releaseCamera() {
        if(mCamera != null) {
            stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void setupCameraParams(SurfaceHolder holder, int optWidth, int optHeight) {
        if(mCamera == null) {
            LogUtil.logd("camera is null");
            return;
        }

        stopPreview();
        Camera.Parameters cameraParams = mCamera.getParameters();
        cameraParams.setPictureFormat(PixelFormat.JPEG);

        LogUtil.logd("get Preview size : ");
        Camera.Size previewSize = getOptimalSize(cameraParams.getSupportedPreviewSizes(), optWidth, optHeight);
        if(previewSize == null) {
            LogUtil.logd("camera broken");
            return;
        }
        cameraParams.setPreviewSize(previewSize.width, previewSize.height);

        LogUtil.logd("get Picture Size : ");
        Camera.Size pictureSize = getOptimalSize(cameraParams.getSupportedPictureSizes(), optWidth, optHeight);
        if(pictureSize == null) {
            LogUtil.logd("camera broken");
            return;
        }
        cameraParams.setPictureSize(pictureSize.width, pictureSize.height);

        mCamera.setDisplayOrientation(90);
        cameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        mCamera.setParameters(cameraParams);
        try {
            mCamera.setPreviewDisplay(holder);
            startPreview();
        } catch (IOException e) {
            LogUtil.logd("set Preview failed");
            e.printStackTrace();
        }
    }

    public void startPreview() {
        if(mCamera != null && !mIsPreviewing) {
            mCamera.startPreview();
            mIsPreviewing = true;
        }
    }

    public void stopPreview() {
        if(mCamera != null && mIsPreviewing) {
            mCamera.stopPreview();
            mIsPreviewing = false;
        }
    }

    public void doTouchFocus(Rect focusRect, final CameraPreview.OnFocusCallback callback) {
        if(!mIsPreviewing) {
            callback.onFocusCompleted(false);
            return;
        }
        try {
            List<Camera.Area> focusList = new ArrayList<>();
            Camera.Area focusArea = new Camera.Area(focusRect, 1000);
            focusList.add(focusArea);

            Camera.Parameters params = mCamera.getParameters();
            if(params.getFocusMode() != Camera.Parameters.FOCUS_MODE_AUTO) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            params.setFocusAreas(focusList);
            params.setMeteringAreas(focusList);
            mCamera.setParameters(params);

            LogUtil.logd("try call auto focus");

            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    mCamera.cancelAutoFocus();
                    Camera.Parameters params = mCamera.getParameters();
                    if(params.getFocusMode() != Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) {
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                        mCamera.setParameters(params);
                    }
                    callback.onFocusCompleted(success);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void takePicture(Camera.PictureCallback pictureCallback) {
        if(mIsPreviewing && mCamera != null) {
            mCamera.takePicture(mShutterCallback, null, pictureCallback);
        }
    }

    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            LogUtil.logd("onShutter running");
        }
    };

    private Camera.Size getOptimalSize(List<Camera.Size> sizes, int w, int h) {
        if(sizes == null) {
            return null;
        }
        for(Camera.Size size : sizes) {
            LogUtil.logd(size.toString());
        }
        Camera.Size optimalSize = null;
        double aspectTolerance = 0.1;
        double targetRatio = (double)h / w;
        double minDiff = Double.MAX_VALUE;
        double targetWidth = w;

        for(Camera.Size size : sizes) {
            double ratio = (double)size.height / size.width;
            if(Math.abs(targetRatio - ratio) > aspectTolerance) continue;
            if(Math.abs(targetWidth - size.width) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(targetWidth - size.width);
            }
        }

        if(optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for(Camera.Size size : sizes) {
                if(Math.abs(targetWidth - size.width) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(targetWidth - size.width);
                }
            }
        }
        if(optimalSize == null) {
            optimalSize = sizes.get(0);
        }

        return optimalSize;
    }


}
