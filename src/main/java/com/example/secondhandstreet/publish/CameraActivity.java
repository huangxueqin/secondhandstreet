package com.example.secondhandstreet.publish;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.secondhandstreet.R;
import com.example.secondhandstreet.Utils.Utilities;
import com.example.secondhandstreet.Utils.FileUtil;
import com.example.secondhandstreet.camera.CameraDrawingView;
import com.example.secondhandstreet.camera.CameraPreview;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangxueqin on 15-4-17.
 */
public class CameraActivity extends Activity implements View.OnClickListener{

    private CameraPreview mCameraPreview;
    private LinearLayout mPicHolder;
    private HorizontalScrollView mPicHolderContainer;
    private ImageButton mTakePicButton;
    private View mCancel;
    private View mConfirm;

    private List<Uri> mPicUris = new ArrayList<>();
    private int maxPicNum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mCameraPreview = (CameraPreview) findViewById(R.id.camera_surface);
        CameraDrawingView cdv = (CameraDrawingView) findViewById(R.id.camera_drawing_view);
        mCameraPreview.setDrawingView(cdv);

        mTakePicButton = (ImageButton) findViewById(R.id.take_pic_button);
        mTakePicButton.setOnClickListener(this);
        mPicHolder = (LinearLayout) findViewById(R.id.pic_holder);
        mPicHolderContainer = (HorizontalScrollView) findViewById(R.id.scroll_gallery);

        mCancel = findViewById(R.id.cancel);
        mCancel.setOnClickListener(this);
        mConfirm = findViewById(R.id.confirm);
        mConfirm.setOnClickListener(this);
        mConfirm.setEnabled(false);
        Intent info = getIntent();
        maxPicNum = info.getIntExtra(PublishItemActivity.KEY_MAX_PIC, PublishItemActivity.MAX_PIC_NUM);
    }

    private void clearCapturedPictures() {
        List<Uri> uris = mPicUris;
        for(Uri uri : uris) {
            FileUtil.deleteFileByUri(uri);
        }
        uris.clear();
    }

    Camera.PictureCallback mCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap b = null;
            if(data != null) {
                b = BitmapFactory.decodeByteArray(data, 0, data.length);
                mCameraPreview.stopPreview();
            }
            if(b != null) {
                Bitmap rotatedBitmap = Utilities.getRotateBitmap(b, 90.0f);
                Uri picUri = FileUtil.savePhotos(rotatedBitmap, String.valueOf(android.os.SystemClock.uptimeMillis()));
                b.recycle();
                rotatedBitmap.recycle();
                View v = createPreviewPicView(picUri);
                mPicHolder.addView(v);
                // scroll to the right most
                mPicHolderContainer.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPicHolderContainer.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                    }
                }, 100L);

                mPicUris.add(picUri);
            }
            mCameraPreview.startPreview();
        }
    };

    private View createPreviewPicView(Uri picUri) {
        View previewPicView = LayoutInflater.from(this).inflate(R.layout.camera_preview_pic, mPicHolder, false);
        SimpleDraweeView sdv = (SimpleDraweeView) previewPicView.findViewById(R.id.preview_pic);
        sdv.setImageURI(picUri);
        View deleteIcon = previewPicView.findViewById(R.id.delete_btn);
        deleteIcon.setOnClickListener(mDeleteIconListener);
        return previewPicView;
    }

    private View.OnClickListener mDeleteIconListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            View parent = (View)v.getParent();
            int count = mPicHolder.getChildCount();
            for(int i = 0; i < count; i++) {
                View child = mPicHolder.getChildAt(i);
                if(parent == child) {
                    FileUtil.deleteFileByUri(mPicUris.get(i));
                    mPicUris.remove(i);
                    mPicHolder.removeViewAt(i);
                    break;
                }
            }
            if(mPicHolder.getChildCount() == 0) {
                mConfirm.setEnabled(false);
            }
        }
    };

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.take_pic_button:
                if(mPicHolder.getChildCount() < maxPicNum) {
                    mCameraPreview.takePicture(mCallback);
                    if(!mConfirm.isEnabled()) {
                        mConfirm.setEnabled(true);
                    }
                }
                else {
                    Toast.makeText(CameraActivity.this, "最多可以拍" + maxPicNum + "张照片", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.cancel:
                clearCapturedPictures();
                CameraActivity.this.setResult(RESULT_CANCELED);
                CameraActivity.this.finish();
                break;
            case R.id.confirm:
                List<Uri> uris = mPicUris;
                Intent data = new Intent();
                String[] uriStrings = new String[uris.size()];
                for(int i = 0; i < uris.size(); i++) {
                    uriStrings[i] = uris.get(i).toString();
                }
                data.putExtra(PublishItemActivity.KEY_PIC_URIS, uriStrings);
                CameraActivity.this.setResult(RESULT_OK, data);
                CameraActivity.this.finish();
                break;
        }
    }

    @Override
    protected void onResume() {
        mCameraPreview.startPreview();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mCameraPreview.stopPreview();
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            clearCapturedPictures();
        }
        return super.onKeyDown(keyCode, event);
    }
}
