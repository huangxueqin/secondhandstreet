package com.example.secondhandstreet.home;

import android.content.Context;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.secondhandstreet.CacheManager;
import com.example.secondhandstreet.Utils.LogUtil;
import com.example.secondhandstreet.Utils.NetworkUtils;
import com.example.secondhandstreet.R;
import com.example.secondhandstreet.ItemInfo;
import com.example.secondhandstreet.Settings;
import com.example.secondhandstreet.SimpleItemInfoCardsAdapter;
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

import static android.support.v7.widget.RecyclerView.*;

/**
 * Created by huangxueqin on 15-4-10.
 */
public class RecommendationFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "HXQ_TAG";
    private RecyclerView mRecList;
    private SwipeRefreshLayout mRefreshIndicator;
    private List<ItemInfo> mListInfos;
    private SimpleItemInfoCardsAdapter mRecListAdapter;
    private SimpleDraweeView mLoadingIndicator;

    private static final int REQUEST_TYPE_NONE = 99;
    private static final int REQUEST_TYPE_REFRESH = 100;
    private static final int REQUEST_TYPE_LOADMORE = 101;
    private int mRequestType = REQUEST_TYPE_NONE;
    private boolean mIsLoadingData;
    private GetProductListTask mRefreshTask;
    private GetProductListTask mLoadMoreTask;
    private int mCurrentPage = -1;
    private int mTotalPage;
    private boolean mNeedWriteCache = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NetworkUtils.registerForNetworkChange(mNetworkChangeCallback);
        mRecListAdapter = new SimpleItemInfoCardsAdapter(getActivity(), CacheManager.getInstance().getRecommendCache());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home_recommentation, container, false);

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(CacheManager.loadingGif).build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setAutoPlayAnimations(true)
                .build();
        mLoadingIndicator = (SimpleDraweeView) rootView.findViewById(R.id.load_indicator);
        mLoadingIndicator.setController(controller);

        mRecList = (RecyclerView) rootView.findViewById(R.id.recycler_list);
        mRecList.setHasFixedSize(true);
        mRecList.addItemDecoration(new SimpleItemDecoration(getActivity()));
        mRecList.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mRecList.setAdapter(mRecListAdapter);
        mRecList.setOnScrollListener(new SimpleOnScrollListener(mRecList.getLayoutManager()));
        mRefreshIndicator = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_layout);
        mRefreshIndicator.setOnRefreshListener(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!isOffline()) {
                    refreshData();
                }
            }
        }, 500);
        return rootView;
    }

    private boolean isOffline() {
        return NetworkUtils.getNetWorkState(CacheManager.getApplicatioinContext()) == NetworkUtils.NET_STATE_OFFLINE;
    }

    private NetworkUtils.NetworkChangeCallback mNetworkChangeCallback = new NetworkUtils.NetworkChangeCallback() {
        @Override
        public void onNetworkConnected() {
            if(mRecListAdapter.getItemCount() == 0) {
                refreshData();
            }
        }

        @Override
        public void onNetworkBreak() {
        }
    };

    private void loadCaches() {
        mRecListAdapter.addData(CacheManager.getInstance().getNewestCache());
    }

    private void refreshData() {
        if(isOffline()) {
            mRefreshIndicator.setRefreshing(false);
            Toast.makeText(getActivity(), "离线中,无法刷新", Toast.LENGTH_SHORT).show();
        }
        if(!mIsLoadingData) {
            mIsLoadingData = true;
            mRequestType = REQUEST_TYPE_REFRESH;
            mRefreshTask = new GetProductListTask();
            mRefreshTask.execute(Settings.RECOMMEND_URL);
        }
    }

    private void loadMoreData() {
        if(isOffline() || mTotalPage == 0) {
            return;
        }

        if(!mIsLoadingData && mCurrentPage < mTotalPage -1) {
            LogUtil.logd("currentPage = " + mCurrentPage + ", totalPage = " + mTotalPage);

            mLoadingIndicator.setVisibility(View.VISIBLE);

            mIsLoadingData = true;
            mRequestType = REQUEST_TYPE_LOADMORE;
            String url = Settings.RECOMMEND_URL + "?page=" + (mCurrentPage+2);
            mLoadMoreTask = new GetProductListTask();
            mLoadMoreTask.execute(url);
        }
        if(mCurrentPage == mTotalPage-1) {
            Toast.makeText(this.getActivity(), "没有更多了", Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<ItemInfo> getListInfos() {
        String imgUrl = "http://img1.cache.netease.com/catchpic/E/E3/E31C5A84149A77B8B83F06AFF83EE0BB.jpg";
        ArrayList<ItemInfo> itemInfos = new ArrayList<>();
        for(int i = 0; i < 9; i++) {
            ItemInfo info = new ItemInfo();
            info.id = "1";
            info.frontCoverImgUrl = imgUrl;
            info.name = "Sample";
            info.price = "10";
            itemInfos.add(info);
        }
        return itemInfos;
    }

    @Override
    public void onRefresh() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                refreshData();
            }
        });
    }

    private class SimpleOnScrollListener extends RecyclerView.OnScrollListener{
        private RecyclerView.LayoutManager mManager;
        private int mType;
        private int firstVisibleItem;
        private static final int LINEAR_LAYOUT = 0;
        private static final int STAGGERED_GRID_LAYOUT = 1;
        private static final int GRID_LAYOUT = 2;
        public SimpleOnScrollListener(RecyclerView.LayoutManager manager) {
            mManager = manager;
            if(mManager instanceof LinearLayoutManager) {
                mType = LINEAR_LAYOUT;
            }
            else if(mManager instanceof StaggeredGridLayoutManager) {
                mType = STAGGERED_GRID_LAYOUT;
            }
            else if(mManager instanceof GridLayoutManager) {
                mType = GRID_LAYOUT;
            }
        }
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            switch (mType) {
                case LINEAR_LAYOUT:
                    firstVisibleItem = ((LinearLayoutManager)mManager).findFirstVisibleItemPosition();
                    break;
                case STAGGERED_GRID_LAYOUT:
                    StaggeredGridLayoutManager manager = (StaggeredGridLayoutManager)mManager;
                    int[] positions = new int[manager.getSpanCount()];
                    manager.findFirstVisibleItemPositions(positions);
                    firstVisibleItem = getMin(positions);
                    break;
                case GRID_LAYOUT:
                    firstVisibleItem = ((GridLayoutManager)mManager).findFirstVisibleItemPosition();
                    break;
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            int totalCount = mManager.getItemCount();
            int visibleCount = mManager.getChildCount();
            Log.d(TAG, "firstVisibleItem = " + firstVisibleItem + ", totalCount = " + totalCount + ", visibleCount = " + visibleCount);
            if(newState == RecyclerView.SCROLL_STATE_IDLE && firstVisibleItem + visibleCount >= totalCount) {
                Log.d(TAG, "on Bottom");
                loadMoreData();
            }
        }

        private int getMin(int[] a) {
            int min = Integer.MAX_VALUE;
            for(int i : a) {
                if(i < min) {
                    min= i;
                }
            }
            return min;
        }
    }

    private class SimpleItemDecoration extends ItemDecoration {
        int spacing = 16;
        public SimpleItemDecoration(Context context) {
            spacing = context.getResources().getDimensionPixelSize(R.dimen.card_spacing);
        }
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
            StaggeredGridLayoutManager manager = (StaggeredGridLayoutManager)parent.getLayoutManager();
            int position = manager.getPosition(view);
            int halfSpacing = spacing / 2;
            outRect.set(halfSpacing, halfSpacing, halfSpacing, halfSpacing);
            if((position & 0x1) != 0) {
                outRect.right += halfSpacing;
            }
            else {
                outRect.left += halfSpacing;
            }
            if(position == 0 || position == 1) {
                outRect.top += halfSpacing;
            }
            int count = manager.getItemCount();
            if(position == count-1) {
                outRect.bottom += halfSpacing;
            }
            if((count & 0x1) == 0) {
                if(position == count-2) {
                    outRect.bottom += halfSpacing;
                }
            }
        }
    }

    private class GetProductListTask extends AsyncTask<String, Void, List<ItemInfo>> {
        private int totalPage;
        private int currentPage;

        @Override
        protected void onCancelled() {
            super.onCancelled();

            mLoadingIndicator.setVisibility(View.GONE);

            mIsLoadingData = false;
            if (mRequestType == REQUEST_TYPE_REFRESH) {
                mRefreshIndicator.setRefreshing(false);
            }
            mRequestType = REQUEST_TYPE_NONE;
        }

        @Override
        protected void onPostExecute(List<ItemInfo> itemInfos) {
            super.onPostExecute(itemInfos);
            LogUtil.logd("on Post execute");
            mIsLoadingData = false;
            if(itemInfos == null) {
                if(mRequestType == REQUEST_TYPE_REFRESH) {
                    Toast.makeText(RecommendationFragment.this.getActivity(), "刷新推荐商品失败", Toast.LENGTH_SHORT).show();
                    mRefreshIndicator.setRefreshing(false);
                }
                else {
                    Toast.makeText(RecommendationFragment.this.getActivity(), "加载推荐商品失败", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                if (mRequestType == REQUEST_TYPE_REFRESH) {
                    mTotalPage = totalPage;
                    mCurrentPage = 0;
                    mRecListAdapter.replaceAllData(itemInfos);
                    mRefreshIndicator.setRefreshing(false);
                } else if (mRequestType == REQUEST_TYPE_LOADMORE) {
                    mTotalPage = totalPage;
                    mCurrentPage = currentPage-1;
                    mRecListAdapter.addData(itemInfos);
                }
                if(mNeedWriteCache) {
                    mNeedWriteCache = false;
                    CacheManager.getInstance().writeRecommendCache(itemInfos);
                }
            }
            mLoadingIndicator.setVisibility(View.GONE);
            mRequestType = REQUEST_TYPE_NONE;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mRequestType == REQUEST_TYPE_REFRESH) {
                mRefreshIndicator.setRefreshing(true);
            }
        }

        @Override
        protected List<ItemInfo> doInBackground(String... params) {
            String url = params[0];
            JSONObject response = NetworkUtils.getResponseByHttpGet(url);
            if (response != null && !response.has(Settings.JSON_KEY_ERROR)) {
                List<ItemInfo> itemList = new ArrayList<>();
                try {
                    JSONArray items = response.getJSONArray("list");
                    int length = items.length();
                    for (int i = 0; i < length; i++) {
                        ItemInfo item = ItemInfo.obtainInstance(items.optJSONObject(i));
                        if (item != null) {
                            itemList.add(item);
                        }
                    }
                    JSONObject meta = response.getJSONObject("meta");
                    currentPage = meta.getInt("nowpage");
                    totalPage = meta.getInt("totalpage");
                } catch (JSONException e) {
                    e.printStackTrace();
                    itemList = null;
                }
                return itemList;
            }
            return null;
        }
    }
}
