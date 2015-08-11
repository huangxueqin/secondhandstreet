package com.example.secondhandstreet.discovery;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.secondhandstreet.CacheManager;
import com.example.secondhandstreet.ItemInfo;
import com.example.secondhandstreet.R;
import com.example.secondhandstreet.Settings;
import com.example.secondhandstreet.UserInfo;
import com.example.secondhandstreet.Utils.DisplayUtil;
import com.example.secondhandstreet.Utils.LogUtil;
import com.example.secondhandstreet.Utils.NetworkUtils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangxueqin on 15-4-10.
 * 求购Tab所指示的页面
 */
public class DiscoveryFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private View mNoData;
    private RecyclerView mRecList;
    private SwipeRefreshLayout mRefreshIndicator;
    private List<ItemInfo> mListInfos;
    private SimpleCardsAdapter mRecListAdapter;
    private ProgressDialog mWaitDialog;
    private Dialog popupMenu;
    private UserInfo mClickedUserInfo = null;

    private SimpleDraweeView mLoadingIndicator;

    private static final int REQUEST_TYPE_NONE = 99;
    private static final int REQUEST_TYPE_REFRESH = 100;
    private static final int REQUEST_TYPE_LOADMORE = 101;
    private int mRequestType = REQUEST_TYPE_NONE;
    private GetDiscoveryListTask mTask;
    private boolean mIsLoadingData;
    private int mCurrentPage = -1;
    private int mTotalPage = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NetworkUtils.registerForNetworkChange(mNetworkChangeCallback);
        mRecListAdapter = new SimpleCardsAdapter(getActivity(), null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_discovery, container, false);

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(CacheManager.loadingGif).build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setAutoPlayAnimations(true)
                .build();
        mLoadingIndicator = (SimpleDraweeView) rootView.findViewById(R.id.load_indicator);
        mLoadingIndicator.setController(controller);

        mRecList = (RecyclerView) rootView.findViewById(R.id.recycler_list);
        mRecList.addItemDecoration(new SimpleItemDecoration(getActivity()));
        mRecList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecList.setAdapter(mRecListAdapter);
        mRecList.setOnScrollListener(new SimpleOnScrollListener(mRecList.getLayoutManager()));
        mRefreshIndicator = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_layout);
        mRefreshIndicator.setOnRefreshListener(this);
        setupUnHappyFace(rootView);
        setupPopMenu(inflater);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isOffline()) {
                    mNoData.setVisibility(View.VISIBLE);
                    Toast.makeText(getActivity(), "离线中，无法获取求购信息", Toast.LENGTH_SHORT).show();
                }
                else {
                    refreshData();
                }
            }
        }, 500);

        return rootView;
    }

    // 初始化获取求购者联系方式的弹窗
    private void setupPopMenu(LayoutInflater inflater) {
        View popMenuView = inflater.inflate(R.layout.contact_menu_layout, null);
        View child1 = popMenuView.findViewById(R.id.contact_phone);
        View child2 = popMenuView.findViewById(R.id.contact_qq);
        child1.setOnClickListener(popupMenuItemClickListener);
        child2.setOnClickListener(popupMenuItemClickListener);
        popupMenu = new Dialog(getActivity());
        popupMenu.requestWindowFeature(Window.FEATURE_NO_TITLE);
        popupMenu.setContentView(popMenuView);
        Window window = popupMenu.getWindow();
        window.setBackgroundDrawableResource(R.drawable.popupwindow_background);
        WindowManager.LayoutParams wlp = window.getAttributes();
        int screenWidth = DisplayUtil.getScreenSize(getActivity()).x;
        wlp.width = screenWidth;
        wlp.gravity = Gravity.BOTTOM;
        window.setAttributes(wlp);
        window.getAttributes().windowAnimations = R.style.popupmenu;
        popupMenu.setCancelable(true);

    }

    private void setupUnHappyFace(View rootView) {
        mNoData = rootView.findViewById(R.id.no_data);
        SimpleDraweeView sdv = (SimpleDraweeView) mNoData.findViewById(R.id.image);
        sdv.setImageURI(Uri.parse("res://com.example.secondhandstreet/" + R.drawable.unhappyface));
    }

    private View.OnClickListener popupMenuItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.contact_phone:
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + mClickedUserInfo.phone));
                    startActivity(intent);
                    break;
                case R.id.contact_qq:
                    ClipboardManager clipboard = (ClipboardManager)
                            CacheManager.getApplicatioinContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", mClickedUserInfo.qq);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(CacheManager.getApplicatioinContext(), "qq号已经复制进剪贴板", Toast.LENGTH_SHORT).show();
                    break;
            }
            mClickedUserInfo = null;
            popupMenu.dismiss();
        }
    };

    private boolean isOffline() {
        return NetworkUtils.getNetWorkState(CacheManager.getApplicatioinContext()) == NetworkUtils.NET_STATE_OFFLINE;
    }

    private NetworkUtils.NetworkChangeCallback mNetworkChangeCallback = new NetworkUtils.NetworkChangeCallback() {
        @Override
        public void onNetworkConnected() {
            if(mRecListAdapter.getItemCount() == 0) {
                LogUtil.logd("-------------------> net work alive again");
                refreshData();
            }
        }

        @Override
        public void onNetworkBreak() {
        }
    };

    private void refreshData() {
        if(mRequestType != REQUEST_TYPE_REFRESH) {
            if(mIsLoadingData) {
                mTask.cancel(true);
                mIsLoadingData = false;
            }
            mRequestType = REQUEST_TYPE_REFRESH;
            mTask = new GetDiscoveryListTask();
            mTask.execute(Settings.DISCOVERY_URL);
        }
    }

    private void loadMoreData() {
        if(isOffline() || mTotalPage == 0) {
            return;
        }

        if(mRequestType == REQUEST_TYPE_NONE && mCurrentPage < mTotalPage -1) {
            LogUtil.logd("currentPage = " + mCurrentPage + ", totalPage = " + mTotalPage);

            mLoadingIndicator.setVisibility(View.VISIBLE);

            mRequestType = REQUEST_TYPE_LOADMORE;
            String url = Settings.DISCOVERY_URL + "?page=" + (mCurrentPage+2);
            mTask = new GetDiscoveryListTask();
            mTask.execute(url);
        }
        if(mCurrentPage == mTotalPage-1) {
            Toast.makeText(CacheManager.getApplicatioinContext(), "没有更多了", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRefresh() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                LogUtil.logd("Discovery onRefresh");
                refreshData();
            }
        });
    }

    private class GetDiscoveryListTask extends AsyncTask<String, Void, List<DiscoveryInfo>> {
        int totalPage;
        int currentPage;

        @Override
        protected List<DiscoveryInfo> doInBackground(String... params) {
            String url = params[0];
            JSONObject response = NetworkUtils.getResponseByHttpGet(url);
            if (response != null && !response.has(Settings.JSON_KEY_ERROR)) {
                List<DiscoveryInfo> itemList = new ArrayList<>();
                try {
                    JSONArray items = response.getJSONArray("list");
                    int length = items.length();
                    LogUtil.logd("list length = " + length);
                    for (int i = 0; i < length; i++) {
                        DiscoveryInfo item = DiscoveryInfo.obtainInstance(items.optJSONObject(i));
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

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mIsLoadingData = false;
            if(mRequestType == REQUEST_TYPE_REFRESH) {
                mRefreshIndicator.setRefreshing(false);
            }
            mLoadingIndicator.setVisibility(View.GONE);
            mRequestType = REQUEST_TYPE_NONE;
        }

        @Override
        protected void onPostExecute(List<DiscoveryInfo> discoveryInfos) {
            super.onPostExecute(discoveryInfos);
            mIsLoadingData = false;
            if(discoveryInfos == null) {
                if(mRequestType == REQUEST_TYPE_REFRESH) {
                    mRefreshIndicator.setRefreshing(false);
                    mNoData.setVisibility(View.VISIBLE);
                }
                else {
                    Toast.makeText(CacheManager.getApplicatioinContext(), "加载求购信息失败", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                mNoData.setVisibility(View.GONE);
                LogUtil.logd("on Post Execute Running");
                if (mRequestType == REQUEST_TYPE_REFRESH) {
                    mTotalPage = totalPage;
                    mCurrentPage = 0;
                    mRecListAdapter.replaceAllData(discoveryInfos);
                    mRefreshIndicator.setRefreshing(false);
                } else if (mRequestType == REQUEST_TYPE_LOADMORE) {
                    mTotalPage = totalPage;
                    mCurrentPage = currentPage-1;
                    mRecListAdapter.addData(discoveryInfos);
                }
            }
            mLoadingIndicator.setVisibility(View.GONE);
            mRequestType = REQUEST_TYPE_NONE;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mIsLoadingData = true;
            if(mRequestType == REQUEST_TYPE_REFRESH) {
                mRefreshIndicator.setRefreshing(true);
            }
        }
    }

    private class SimpleCardsAdapter extends RecyclerView.Adapter<SimpleCardsAdapter.ListItemHolder> {
        Context mContext;
        List<DiscoveryInfo> mData;
        public SimpleCardsAdapter(Context context, List<DiscoveryInfo> data) {
            mContext = context;
            if (data == null) {
                mData = new ArrayList<>();
            } else {
                mData = new ArrayList<>(data);
            }
            ;
        }

        public void addData(List<DiscoveryInfo> data) {
            if (data != null) {
                mData.addAll(data);
            }
            this.notifyDataSetChanged();
        }

        public void replaceAllData(List<DiscoveryInfo> data) {
            if (data != null) {
                mData = new ArrayList<>(data);
            } else {
                mData = new ArrayList<>();
            }
            notifyDataSetChanged();
        }

        @Override
        public ListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.dicovery_card, parent, false);
            v.setOnClickListener(mCardOnClickListener);
            return new ListItemHolder(v);
        }

        @Override
        public void onBindViewHolder(ListItemHolder holder, int position) {
            DiscoveryInfo info = mData.get(position);
            holder.rootView.setTag(mData.get(position));

            holder.content.setText(info.content);
            holder.username.setText(info.user.username);
            holder.avatar.setImageURI(Uri.parse(info.user.avatarImg));
            holder.date.setText(info.addTime == null ? "" : info.addTime);
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        class ListItemHolder extends RecyclerView.ViewHolder {
            View rootView;
            SimpleDraweeView avatar;
            TextView content;
            TextView username;
            TextView date;

            public ListItemHolder(View itemView) {
                super(itemView);
                rootView = itemView;
                avatar = (SimpleDraweeView) itemView.findViewById(R.id.avatar);
                username = (TextView) itemView.findViewById(R.id.username);
                content = (TextView) itemView.findViewById(R.id.content);
                date = (TextView) itemView.findViewById(R.id.date);
            }
        }

        private View.OnClickListener mCardOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag() != null) {
                    DiscoveryInfo info = (DiscoveryInfo) v.getTag();
                    mClickedUserInfo = info.user;
                    popupMenu.show();
                }
            }
        };
    }

    private class SimpleItemDecoration extends RecyclerView.ItemDecoration {
        int spacing = 16;
        public SimpleItemDecoration(Context context) {
            spacing = context.getResources().getDimensionPixelSize(R.dimen.card_spacing);
        }
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            LinearLayoutManager manager = (LinearLayoutManager) parent.getLayoutManager();
            int count = manager.getItemCount();
            int position = manager.getPosition(view);
            outRect.set(spacing, spacing/2, spacing, spacing/2);
            if(position == 0) {
                outRect.top += spacing / 2;
            }
            if(position == count - 1) {
                outRect.bottom += spacing / 2;
            }
        }
    }

    public static class DiscoveryInfo {
        private static final String JSON_KEY_CONTENT = "content";
        private static final String JSON_KEY_USER = "userinfo";
        private static final String JSON_KEY_ADD_TIME = "addtime";
        private static final String JSON_KEY_PRICE = "price";
        String content;
        String addTime;
        String price;
        UserInfo user;

        public static final DiscoveryInfo sample = new DiscoveryInfo();
        static {
            sample.content = "Hello";
            sample.user = UserInfo.sample;
        }

        public static DiscoveryInfo obtainInstance(JSONObject jsonObject) {
            DiscoveryInfo info = new DiscoveryInfo();
            try {
                info.content = jsonObject.getString(JSON_KEY_CONTENT);
                if(jsonObject.has(JSON_KEY_PRICE)) {
                    info.price = jsonObject.getString(JSON_KEY_PRICE);
                }
                if(jsonObject.has(JSON_KEY_ADD_TIME)) {
                    info.addTime = jsonObject.getString(JSON_KEY_ADD_TIME);
                }
                info.user = UserInfo.obtainInstance(jsonObject.getJSONObject(JSON_KEY_USER));
                if(info.user == null) {
                    info = null;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                info = null;
            }
            return info;
        }
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
            LogUtil.logd("totalCount = " + totalCount + ", visibleCount = " + visibleCount);
            if(newState == RecyclerView.SCROLL_STATE_IDLE && firstVisibleItem + visibleCount >= totalCount) {
                LogUtil.logd("on Bottom");
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
}
