package com.example.secondhandstreet;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.secondhandstreet.Utils.LogUtil;
import com.example.secondhandstreet.Utils.NetworkUtils;
import com.example.secondhandstreet.Utils.Utilities;
import com.facebook.drawee.view.SimpleDraweeView;
import com.viewpagerindicator.CirclePageIndicator;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangxueqin on 15-4-24.
 */

public class ItemSpecificActivity extends ActionBarActivity implements View.OnClickListener {
    public static final String KEY_ITEM_INFO = "item_info";

    private ItemInfo mItemInfo;

    private TextView mContactSeller;
    private ProgressDialog mWaitDialog;
    private ViewPager mGallery;
    private CirclePageIndicator mIndicator;
    private TextView mTitle, mPrice, mLocation, mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_specific);
        Intent data = getIntent();
        mItemInfo = data.getParcelableExtra(KEY_ITEM_INFO);
        initToolbar();
        mGallery = (ViewPager) findViewById(R.id.gallery);
        mIndicator = (CirclePageIndicator) findViewById(R.id.gallery_indicator);
        mTitle = (TextView) findViewById(R.id.title);
        mPrice = (TextView) findViewById(R.id.price);
        mLocation = (TextView) findViewById(R.id.location);
        mContent = (TextView) findViewById(R.id.content);
        checkAndLoadItemInfo();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            mContactSeller = (TextView) findViewById(R.id.contact_seller);
            mContactSeller.setOnClickListener(this);
        }
    }

    private void updateViewsByItemInfo() {
        int imageNum = 1 + (mItemInfo.extraImageUrls == null ? 0 : mItemInfo.extraImageUrls.length);
        String[] imageUris = new String[imageNum];
        imageUris[0] = mItemInfo.frontCoverImgUrl;
        for (int i = 1; i < imageNum; i++) {
            imageUris[i] = mItemInfo.extraImageUrls[i - 1];
        }
        ImageSlidesAdapter adapter = new ImageSlidesAdapter(this, imageUris);
        mGallery.setAdapter(adapter);
        mIndicator.setViewPager(mGallery);
        mIndicator.setSnap(true);
        mTitle.setText(mItemInfo.name);
        mPrice.setText(mItemInfo.price);
        mLocation.setText(mItemInfo.location);
        mContent.setText(mItemInfo.content);
    }

    private void checkAndLoadItemInfo() {
        if (!mItemInfo.isItemInfoComplete()) {
            String itemId = mItemInfo.id;
            GetItemInfoTask task = new GetItemInfoTask();
            task.execute(itemId);
        } else {
            updateViewsByItemInfo();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(R.string.is_contact_seller);
        menu.add(0, 0, 0, R.string.is_contact_with_phone);
        menu.add(0, 1, 0, R.string.is_contact_with_qq);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + mItemInfo.owner.phone));
                startActivity(intent);
                break;
            case 1:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", mItemInfo.owner.qq);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "qq号已经复制进剪贴板", Toast.LENGTH_SHORT).show();
                break;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.contact_seller) {
            registerForContextMenu(mContactSeller);
            openContextMenu(mContactSeller);
            unregisterForContextMenu(mContactSeller);
        }
    }

    public class ImageSlidesAdapter extends PagerAdapter {
        private List<Uri> mImageUris;
        private Context mContext;

        public ImageSlidesAdapter(Context context, String[] uris) {
            mImageUris = new ArrayList<>();
            for (String uri : uris) {
                mImageUris.add(Uri.parse(uri));
            }
            mContext = context;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            SimpleDraweeView sdv = new SimpleDraweeView(mContext);
            sdv.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            LogUtil.logd(mImageUris.get(position).toString());
            sdv.setImageURI(mImageUris.get(position));
            container.addView(sdv);
            return sdv;
        }

        @Override
        public int getCount() {
            return mImageUris.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return object == view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private class GetItemInfoTask extends AsyncTask<String, Void, ItemInfo> {
        @Override
        protected ItemInfo doInBackground(String... params) {
            String itemId = params[0];
            String url = Settings.ITEM_INFO_URL + "?id=" + itemId;
            JSONObject response = NetworkUtils.getResponseByHttpGet(url);
            if (response != null && !response.has(Settings.JSON_KEY_ERROR)) {
                ItemInfo info = ItemInfo.obtainInstance(response);
                return info;
            }
            return null;
        }

        @Override
        protected void onPostExecute(ItemInfo itemInfo) {
            super.onPostExecute(itemInfo);
            if (itemInfo == null) {
                Toast.makeText(ItemSpecificActivity.this, "加载商品数据失败", Toast.LENGTH_SHORT).show();
            } else {
                mItemInfo = itemInfo;
                updateViewsByItemInfo();
            }
            mWaitDialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mWaitDialog = Utilities.getProgressDialog(ItemSpecificActivity.this);
            mWaitDialog.show();
        }
    }
}
