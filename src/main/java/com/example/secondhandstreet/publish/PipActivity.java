package com.example.secondhandstreet.publish;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.example.secondhandstreet.R;
import com.example.secondhandstreet.Utils.DisplayUtil;
import com.example.secondhandstreet.Utils.Utilities;
import com.facebook.drawee.view.SimpleDraweeView;

/**
 * Created by huangxueqin on 15-5-2.
 */
public class PipActivity extends ActionBarActivity implements View.OnClickListener{
    private SimpleDraweeView mImage;
    private View mDeleteButton, mSetCoverButton;
    private Uri mImageUri;
    private Point mScreenSize;
    private Point mRealSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pip);
        mImageUri = getIntent().getParcelableExtra(PublishItemActivity.KEY_PIP_URL);
        initToolbar();
        mDeleteButton = findViewById(R.id.delete_button);
        mSetCoverButton = findViewById(R.id.set_cover_button);
        mDeleteButton.setOnClickListener(this);
        mSetCoverButton.setOnClickListener(this);
        mImage = (SimpleDraweeView) findViewById(R.id.image);
        setupPreviewImage();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupPreviewImage() {
        mScreenSize = DisplayUtil.getScreenSize(this);
        mRealSize = Utilities.getImageSize(mImageUri);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)mImage.getLayoutParams();
        lp.width = mScreenSize.x;
        lp.height = mScreenSize.x;
        mImage.setLayoutParams(lp);
        mImage.setImageURI(mImageUri);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.delete_button:
                setResult(PublishItemActivity.RESULT_DELETE);
                finish();
                break;
            case R.id.set_cover_button:
                setResult(PublishItemActivity.RESULT_SET_COVER);
                finish();
                break;
        }
    }
}
