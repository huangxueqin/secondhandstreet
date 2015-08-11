package com.example.secondhandstreet.publish;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.secondhandstreet.CacheManager;
import com.example.secondhandstreet.ItemInfo;
import com.example.secondhandstreet.R;
import com.example.secondhandstreet.Settings;
import com.example.secondhandstreet.UserInfo;
import com.example.secondhandstreet.Utils.LogUtil;
import com.example.secondhandstreet.Utils.Utilities;
import com.example.secondhandstreet.Utils.FileUtil;
import com.example.secondhandstreet.Utils.NetworkUtils;
import com.example.secondhandstreet.classify.CategoryFragment;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangxueqin on 15-4-12.
 * 发布商品的界面
 */
public class PublishItemActivity extends ActionBarActivity implements View.OnClickListener{
    public static final String KEY_PIP_URL = "image_url";
    private static final int REQUEST_TAKE_PIC = 100;

    private static final int REQUEST_PREVIEW = 101;
    public static final int RESULT_SET_COVER = 102;
    public static final int RESULT_DELETE = 103;

    public static final String KEY_MAX_PIC = "max_pic";
    public static final String KEY_PIC_URIS = "pic_uris";
    public static final int MAX_PIC_NUM = 4;

    TextView mConfirm;
    EditText mItemTitle, mItemPrice, mItemContent, mItemLocation;
    View mChooseCategory;
    TextView mItemCategory;
    int mCategoryIndex = -1;
    SimpleImageGrid mImageGrid;
    GridAdapter mAdapter;
    ProgressDialog mWaitDialog;

    private UserInfo mUserInfo;
    private Handler mHandler;
    private boolean mIsUploading;
    private PublishTask mTask;
    private int mLastChooseImage = -1;
    private int mCoverImage = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);
        mHandler = new Handler(Looper.getMainLooper());
        mUserInfo = CacheManager.getInstance().getUserInfo();
        initToolbar();
        initViews();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            mConfirm = (TextView) toolbar.findViewById(R.id.confirm);
            mConfirm.setOnClickListener(this);
        }
    }

    private void initViews() {
        mWaitDialog = Utilities.getProgressDialog(this);

        mAdapter = new GridAdapter(this);
        mImageGrid = (SimpleImageGrid) findViewById(R.id.image_grid);
        mImageGrid.setAdapter(mAdapter);

        mItemTitle = (EditText) findViewById(R.id.item_title);
        mItemPrice = (EditText) findViewById(R.id.item_price);
        mItemContent = (EditText) findViewById(R.id.item_content);
        mItemLocation = (EditText) findViewById(R.id.item_location);
        mItemCategory = (TextView) findViewById(R.id.category);
        mChooseCategory = findViewById(R.id.choose_category);
        mChooseCategory.setOnClickListener(this);
    }

    private void showCategoryPicker() {
        final Dialog d = new Dialog(this);
        d.setTitle("选择商品类别");
        d.setContentView(R.layout.category_pcker_dialog);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.number_picker);
        Button confirm = (Button) d.findViewById(R.id.confirm);
        Button cancel = (Button) d.findViewById(R.id.cancel);
        np.setMinValue(1);
        np.setMaxValue(CategoryFragment.categoryNames.length);
        np.setDisplayedValues(CategoryFragment.categoryNames);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCategoryIndex = np.getValue();
                mItemCategory.setText(CategoryFragment.categoryNames[mCategoryIndex-1]);
                d.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id) {
            case R.id.confirm:
                String title = mItemTitle.getText().toString();
                String price = mItemPrice.getText().toString();
                String location = mItemLocation.getText().toString();
                String content = mItemContent.getText().toString();
                if(mAdapter.getAllImageUris().size() == 0) {
                    Toast.makeText(this, "至少需要一张物品照片", Toast.LENGTH_SHORT);
                }
                else if(title == null || title.length() == 0) {
                    Toast.makeText(this, "标题不能为空", Toast.LENGTH_SHORT).show();
                }
                else if(price == null || price.length() == 0) {
                    Toast.makeText(this, "价格不能为空", Toast.LENGTH_SHORT).show();
                }
                else if(location == null || location.length() == 0) {
                    Toast.makeText(this, "位置不能为空", Toast.LENGTH_SHORT).show();
                }
                else if(mCategoryIndex == -1) {
                    Toast.makeText(this, "请选择物品类别", Toast.LENGTH_SHORT).show();
                }
                else if(content == null || content.length() == 0) {
                    Toast.makeText(this, "物品详情不能为空", Toast.LENGTH_SHORT).show();
                }
                else if(mAdapter.getCount() == 1) {
                    Toast.makeText(this, "至少上传一张图片", Toast.LENGTH_SHORT).show();
                }
                else {
                    doPublish(title, price, location, content);
                }
                break;
            case R.id.choose_category:
                showCategoryPicker();
                break;
        }
    }

    private void startTakePhotoActivity() {
        Intent takePictureIntent = new Intent(this, CameraActivity.class);
        takePictureIntent.putExtra(KEY_MAX_PIC, MAX_PIC_NUM - (mAdapter.getCount()-1));
        startActivityForResult(takePictureIntent, REQUEST_TAKE_PIC);
    }

    private void onPhotoAdded(String[] addUris) {
        mAdapter.addImages(addUris);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.logd("onActivity Result running");
        if(requestCode == REQUEST_TAKE_PIC) {
            if(resultCode == RESULT_OK) {
                final String[] uris = data.getStringArrayExtra(PublishItemActivity.KEY_PIC_URIS);
                if(uris.length > 0) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onPhotoAdded(uris);
                        }
                    }, 100L);
                }
            }
            return;
        }
        else if(requestCode == REQUEST_PREVIEW) {
            LogUtil.logd("return from preview");
            if(resultCode == RESULT_SET_COVER) {
                if(mCoverImage != mLastChooseImage) {
                    mCoverImage = mLastChooseImage;
                    mAdapter.setCover(mCoverImage);
                }
            }
            else if(resultCode == RESULT_DELETE) {
                LogUtil.logd("delete Image");
                mAdapter.deleteImageOnPosition(mLastChooseImage);
                if(mLastChooseImage <= mCoverImage) {
                    mCoverImage = Math.max(0, mLastChooseImage-1);
                    mAdapter.setCover(mCoverImage);
                }
            }
            mLastChooseImage = -1;
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void doPublish(String title, String price, String location, String content) {
        mTask = new PublishTask();
        mTask.execute(title, price, location, content, String.valueOf(mCategoryIndex));
    }

    private void cancelPublish() {
        if(mIsUploading) {
            mTask.cancel(true);
            mIsUploading = false;
        }
    }

    private void finishActivity() {
        List<Uri> picUris = mAdapter.getAllImageUris();
        for(Uri uri : picUris) {
            FileUtil.deleteFileByUri(uri);
        }
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finishActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if(mIsUploading) {
                cancelPublish();
                return true;
            }
            else {
                finishActivity();
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    private class PublishTask extends AsyncTask<String, Void, ItemInfo> {

        @Override
        protected ItemInfo doInBackground(String... params) {
            String title = params[0];
            String price = params[1];
            String location = params[2];
            String content = params[3];
            String category = params[4];
            List<Uri> imageUris = mAdapter.getAllImageUris();
            List<String> files = new ArrayList<>();
            for(Uri uri : imageUris) {
                files.add(uri.getPath());
            }
            JSONObject response = NetworkUtils.uploadItemInfo(Settings.PUBLISH_URL,
                    mUserInfo, title, price, location, content, category, files, mCoverImage == -1 ? 0 : mCoverImage);
            if (response != null && !response.has(Settings.JSON_KEY_ERROR)) {
                ItemInfo itemInfo = ItemInfo.obtainInstance(response);
                return itemInfo;
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mIsUploading = true;
            mWaitDialog.show();
        }

        @Override
        protected void onPostExecute(ItemInfo itemInfo) {
            super.onPostExecute(itemInfo);
            mIsUploading = false;
            mWaitDialog.dismiss();
            if(itemInfo != null) {
                finishActivity();
            }
            else {
                Toast.makeText(PublishItemActivity.this, "发布失败，请检查网络", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mIsUploading = false;
            mWaitDialog.dismiss();
        }
    }

    private class GridAdapter extends BaseAdapter {
        private final Uri sAddImageUri = Uri.parse("res://com.example.secondhandstreet/" + R.drawable.add_image);
        int mCover = 0;
        Context mContext;
        List<Uri> mImageUris;

        public GridAdapter(Context context) {
            mContext = context;
            mImageUris = new ArrayList<>();
        }

        public GridAdapter(Context context, String[] uris) {
            mContext = context;
            mImageUris = new ArrayList<>();
            for(String uri : uris) {
                mImageUris.add(Uri.parse(uri));
            }
        }

        public GridAdapter(Context context, List<Uri> uris) {
            mContext = context;
            mImageUris = new ArrayList<>(uris);
        }

        public void setCover(int newCover) {
            if(newCover < getCount()-1) {
                mCover = newCover;
                notifyDataSetChanged();
            }
        }

        public void deleteImageOnPosition(int position) {
            if(position < mImageUris.size()) {
                FileUtil.deleteFileByUri(mImageUris.get(position));
                mImageUris.remove(position);
                notifyDataSetChanged();
            }
        }

        public void addImages(String[] uris) {
            for(String uri : uris) {
                mImageUris.add(Uri.parse(uri));
            }
            this.notifyDataSetChanged();
        }

        public List<Uri> getAllImageUris() {
            return mImageUris;
        }

        @Override
        public int getCount() {
            return mImageUris.size()+1;
        }

        @Override
        public Object getItem(int position) {
            if(position == getCount()-1) {
                return sAddImageUri;
            }
            else {
                return mImageUris.get(position);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder mViewHolder = null;
            if(convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.publish_item_image, parent, false);
                mViewHolder = new ViewHolder(convertView);
                convertView.setTag(mViewHolder);
            }
            mViewHolder = (ViewHolder) convertView.getTag();
            Uri uri = (Uri) getItem(position);
            mViewHolder.sdv.setImageURI(uri);
            mViewHolder.index = position;
            if(position == mCover && getCount() > 1) {
                mViewHolder.coverMark.setVisibility(View.VISIBLE);
            }
            else {
                mViewHolder.coverMark.setVisibility(View.INVISIBLE);
            }
            return convertView;
        }

        private class ViewHolder {
            int index;
            View rootView;
            SimpleDraweeView sdv;
            TextView coverMark;

            public ViewHolder(View rootView) {
                this.rootView = rootView;
                this.sdv = (SimpleDraweeView) rootView.findViewById(R.id.image);
                this.coverMark = (TextView) rootView.findViewById(R.id.cover_mark);
                if(this.coverMark != null) {
                    this.coverMark.setVisibility(View.INVISIBLE);
                }
                rootView.setOnClickListener(mGridImageClickedListener);
            }
        }

        View.OnClickListener mGridImageClickedListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHolder holder = (ViewHolder)v.getTag();
                int index = holder.index;
                if(index == mAdapter.getCount()-1) {
                    if(mAdapter.getCount()-1 == MAX_PIC_NUM) {
                        Toast.makeText(PublishItemActivity.this, "最多可以上传" + MAX_PIC_NUM + "张照片", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        startTakePhotoActivity();
                    }
                }
                else {
                    mLastChooseImage = index;
                    Uri uri = mAdapter.mImageUris.get(index);
                    Intent pip = new Intent(PublishItemActivity.this, PipActivity.class);
                    pip.putExtra(KEY_PIP_URL, uri);
                    startActivityForResult(pip, REQUEST_PREVIEW);
                }
            }
        };
    }
}
