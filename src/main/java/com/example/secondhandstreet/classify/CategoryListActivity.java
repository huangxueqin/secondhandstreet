package com.example.secondhandstreet.classify;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.secondhandstreet.CacheManager;
import com.example.secondhandstreet.ItemInfo;
import com.example.secondhandstreet.R;
import com.example.secondhandstreet.Settings;
import com.example.secondhandstreet.SimpleItemInfoListAdapter;
import com.example.secondhandstreet.Utils.NetworkUtils;
import com.example.secondhandstreet.Utils.Utilities;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangxueqin on 15-4-30.
 */
public class CategoryListActivity extends ActionBarActivity{
    private SimpleDraweeView mLoadingIndicator;
    private TextView mTitle;
    private ListView mList;
    private SimpleItemInfoListAdapter mAdapter;
    private int mCategoryId;
    private String mCategoryName;
    private int mCurrentPage;
    private int mTotalPage;
    private GetCategoryListTask mTask;
    private boolean isLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);
        initToolbar();
        mList = (ListView) findViewById(R.id.list);
        mAdapter = new SimpleItemInfoListAdapter(this, null);
        mList.setAdapter(mAdapter);
        mList.setOnScrollListener(mListOnScrollListener);

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(CacheManager.loadingGif).build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setAutoPlayAnimations(true)
                .build();
        mLoadingIndicator = (SimpleDraweeView) findViewById(R.id.load_indicator);
        mLoadingIndicator.setController(controller);

        Intent data = getIntent();
        mCategoryId = data.getIntExtra(CategoryFragment.KEY_CATEGORY_ID, -1);
        mCategoryName = data.getStringExtra(CategoryFragment.KEY_CATEGORY_NAME);
        mTitle.setText(mCategoryName);
        mTask = new GetCategoryListTask();
        mTask.execute(1);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            mTitle = (TextView) findViewById(R.id.title);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            if(isLoading) {
                mTask.cancel(true);
            }
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isOffline() {
        return NetworkUtils.getNetWorkState(CacheManager.getApplicatioinContext()) == NetworkUtils.NET_STATE_OFFLINE;
    }


    private void loadMore() {
        if(isOffline() || mTotalPage == 0) {
            return;
        }

        if(!isLoading && mCurrentPage < mTotalPage-1) {
            mTask = new GetCategoryListTask();
            mTask.execute(mCurrentPage + 2);
        }
        if(mCurrentPage == mTotalPage-1) {
            Toast.makeText(this, "没有更多了", Toast.LENGTH_SHORT).show();
        }
    }

    private ListView.OnScrollListener mListOnScrollListener  = new ListView.OnScrollListener(){
        int currentFirstVisibleItems;
        int currentVisibleItems;
        int currentTotalItem;
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if(scrollState == SCROLL_STATE_IDLE && currentFirstVisibleItems + currentVisibleItems >= currentTotalItem) {
                loadMore();
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            currentFirstVisibleItems = firstVisibleItem;
            currentVisibleItems = visibleItemCount;
            currentTotalItem = totalItemCount;
        }
    };

    private class GetCategoryListTask extends AsyncTask<Integer, Void, List<ItemInfo>> {
        private int totalPage;
        private int currentPage;

        @Override
        protected List<ItemInfo> doInBackground(Integer... params) {
            int page = params[0];
            String url = Settings.CATEGORY_URL + "?class=" + mCategoryId + "&page=" + page;
            JSONObject response = NetworkUtils.getResponseByHttpGet(url);
            if(response != null && !response.has(Settings.JSON_KEY_ERROR)) {
                List<ItemInfo> itemList = new ArrayList<>();
                try {
                    JSONArray list = response.getJSONArray("list");
                    JSONObject meta = response.getJSONObject("meta");
                    totalPage = meta.getInt("totalpage");
                    currentPage = meta.getInt("nowpage");
                    int length = list.length();
                    for(int i = 0; i < length; i++) {
                        ItemInfo item = ItemInfo.obtainInstance(list.optJSONObject(i));
                        if(item != null) {
                            itemList.add(item);
                        }
                    }
                } catch (JSONException e) {
                    itemList = null;
                    e.printStackTrace();
                }
                return itemList;
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            isLoading = false;
            mLoadingIndicator.setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(List<ItemInfo> itemInfos) {
            super.onPostExecute(itemInfos);
            isLoading = false;
            if(itemInfos != null) {
                mTotalPage = totalPage;
                mCurrentPage = currentPage-1;
                mAdapter.addData(itemInfos);
            }
            else {
                Toast.makeText(CategoryListActivity.this, "没有数据了", Toast.LENGTH_SHORT).show();
            }
            mLoadingIndicator.setVisibility(View.GONE);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isLoading = true;
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }
    }
}
