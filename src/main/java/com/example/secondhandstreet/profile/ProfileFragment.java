package com.example.secondhandstreet.profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.example.secondhandstreet.CacheManager;
import com.example.secondhandstreet.ItemInfo;
import com.example.secondhandstreet.R;
import com.example.secondhandstreet.Settings;
import com.example.secondhandstreet.UserInfo;
import com.example.secondhandstreet.Utils.DisplayUtil;
import com.example.secondhandstreet.Utils.LogUtil;
import com.example.secondhandstreet.Utils.NetworkUtils;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangxueqin on 15-4-12.
 * 我的Tab所指示的页面，包含个人信息和个人发布历史
 */
public class ProfileFragment extends Fragment implements View.OnClickListener{

    private static final int REQUEST_CODE_MODIFY = 0;
    public static final int RESULT_CODE_MODIFIED = 1;

    SimpleDraweeView mAvatar;
    TextView mUsername;
    View mPublished;

    ListView swipeListView;
    SimpleSwipeListAdapter mAdapter;
    View mPrompt;
    PopupWindow mPopupWindow;
    private int popupWindowHeight;
    private boolean mIsLoading;
    private int mCurrentPage=-1;
    private int mTotalPage;

    boolean userInfoUpdated = false;
    boolean avatarChanged = false;

    private LoadMoreTask mTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        CacheManager.registerForUserSwitch(mCallback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        mAvatar = (SimpleDraweeView) rootView.findViewById(R.id.avatar);
        mUsername = (TextView) rootView.findViewById(R.id.username);
        mPublished = rootView.findViewById(R.id.published);
        mPublished.setOnClickListener(this);
        setupViewByUserInfo();
        initPopupWindow();
        return rootView;
    }

    private void initPopupWindow() {
        int screenHeight = DisplayUtil.getScreenSize(getActivity()).y;
        int avatarZone = getResources().getDimensionPixelSize(R.dimen.profile_user_info_zone_height);
        int standardToolbarHeight = DisplayUtil.dip2px(getActivity(), 48);
        int statusBarHeight = DisplayUtil.dip2px(getActivity(), 25);
        popupWindowHeight = screenHeight - avatarZone - standardToolbarHeight - statusBarHeight;

        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.user_pub_item_list, null);
        swipeListView = (ListView) rootView.findViewById(R.id.swipe_list);
        mPrompt = rootView.findViewById(R.id.prompt);

        mPopupWindow = new PopupWindow(rootView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(this.getActivity().getResources().getDrawable(R.drawable.popupwindow_background));
        mPopupWindow.setAnimationStyle(R.style.popupmenu);
        mPopupWindow.setHeight(popupWindowHeight);
        mAdapter = new SimpleSwipeListAdapter(this.getActivity());
        swipeListView.setAdapter(mAdapter);
        swipeListView.setOnScrollListener(mListOnScrollListener);
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

    private void loadMore() {
        if(!mIsLoading && mTotalPage > 0 && mCurrentPage < mTotalPage-1) {
            mTask = new LoadMoreTask();
            mTask.execute(mCurrentPage+2);
        }
    }

    private void setupViewByUserInfo() {
        CacheManager cacheManager = CacheManager.getInstance();
        UserInfo info = cacheManager.getUserInfo();
        LogUtil.logd("username = " + info.username);
        if(info.avatarImg != null) {
            LogUtil.logd("avatar = " + info.avatarImg);
            mAvatar.setImageURI(Uri.parse(info.avatarImg));
        }
        else if(cacheManager.getCachedAvatarUri() != null) {
            mAvatar.setImageURI(cacheManager.getCachedAvatarUri());
        }
        else {
            mAvatar.setImageURI(CacheManager.DEFAULT_AVATAR_URI);
        }
        mUsername.setText(info.username);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_MODIFY) {
            if(resultCode == RESULT_CODE_MODIFIED) {
                userInfoUpdated = true;
                avatarChanged = data.getBooleanExtra("data", false);
            }
        }
        else
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        if(userInfoUpdated) {
            CacheManager cacheManager = CacheManager.getInstance();
            mUsername.setText(cacheManager.getUserInfo().username);
            if(avatarChanged) {
                mAvatar.setImageURI(null);
                mAvatar.setImageURI(cacheManager.getCachedAvatarUri());
                avatarChanged = false;
            }
            userInfoUpdated = false;
        }
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_modify_profile) {
            Intent modifyIntent = new Intent(getActivity(), ProfileModifyActivity.class);
            startActivityForResult(modifyIntent, REQUEST_CODE_MODIFY);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.published:
                mAdapter.removeAllData();
                mCurrentPage = -1;
                mTotalPage = 0;
                mTask = new LoadMoreTask();
                mTask.execute(1);
                mPopupWindow.showAtLocation((ViewGroup) mPublished.getParent(), Gravity.BOTTOM, 0, 0);
                break;
        }
    }

    private CacheManager.UserSwitchedCallback mCallback = new CacheManager.UserSwitchedCallback() {
        @Override
        public void onUserSwitched() {
            LogUtil.logd("update info after modify");
            setupViewByUserInfo();
        }
    };

    private class DeleteTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String itemId = params[0];
            String userId = CacheManager.getInstance().getUserInfo().id;
            String url = Settings.DELETE_ITEM_URL + "?id=" + itemId + "&userid=" + userId;
            NetworkUtils.getResponseByHttpGet(url);
            return null;
        }
    }

    private class LoadMoreTask extends AsyncTask<Integer, Void, List<ItemInfo>> {
        int totalPage;
        int currentPage;
        @Override
        protected List<ItemInfo> doInBackground(Integer... params) {
            int page = params[0];
            String userid = CacheManager.getInstance().getUserInfo().id;
            String url = Settings.GET_PUBLISHED_ITEMS_URL + "?userid=" + userid + "&page=" + page;
            JSONObject data = new JSONObject();
            try {
                JSONObject response = NetworkUtils.getResponseByHttpGet(url);
                if (response != null && !response.has(Settings.JSON_KEY_ERROR)) {
                    List<ItemInfo> itemList = new ArrayList<>();
                    JSONArray list = response.getJSONArray("list");
                    JSONObject meta = response.getJSONObject("meta");
                    totalPage = meta.getInt("totalpage");
                    currentPage = meta.getInt("nowpage");
                    int length = list.length();
                    for (int i = 0; i < length; i++) {
                        ItemInfo item = ItemInfo.obtainInstance(list.optJSONObject(i));
                        if (item != null) {
                            itemList.add(item);
                        }
                    }
                    return itemList;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mIsLoading = true;
        }

        @Override
        protected void onPostExecute(List<ItemInfo> items) {
            if(items != null) {
                mPrompt.setVisibility(View.GONE);
                mAdapter.addData(items);
                mCurrentPage = currentPage-1;
                mTotalPage = totalPage;
            }
            else if(mAdapter.getCount() == 0) {
                mPrompt.setVisibility(View.VISIBLE);
            }
            mIsLoading = false;
        }
    }

    public class SimpleSwipeListAdapter extends BaseAdapter {

        private List<ItemInfo> data;
        private Context context;

        public SimpleSwipeListAdapter(Context context) {
            this.context = context;
            this.data = new ArrayList<>();
        }

        public SimpleSwipeListAdapter(Context context, List<ItemInfo> data) {
            this.context = context;
            this.data = new ArrayList<>(data);
        }

        public void addData(List<ItemInfo> data) {
            this.data.addAll(data);
            this.notifyDataSetChanged();
        }

        public void removeAllData() {
            this.data.clear();
            notifyDataSetChanged();
        }
        public void removeItem(int position) {
            if(position >= 0 && position < this.data.size()) {
                ItemInfo item = data.get(position);
                DeleteTask task = new DeleteTask();
                task.execute(item.id);
                data.remove(position);
                notifyDataSetChanged();
            }
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public ItemInfo getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ItemInfo item = getItem(position);
            final ViewHolder holder;
            if (convertView == null) {
                LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                SwipeLayout swipeLayout = (SwipeLayout) li.inflate(R.layout.user_pub_item_list_row, parent, false);
                swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
                swipeLayout.addDrag(SwipeLayout.DragEdge.Left, swipeLayout.findViewById(R.id.back));
                swipeLayout.setRightSwipeEnabled(false);
                convertView = swipeLayout;
                holder = new ViewHolder();
                holder.image = (SimpleDraweeView) convertView.findViewById(R.id.item_img);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.price = (TextView) convertView.findViewById(R.id.price);
                holder.location = (TextView) convertView.findViewById(R.id.location);
                holder.delete = (Button) convertView.findViewById(R.id.delete);
                holder.index = position;
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final SwipeLayout swipeLayout = (SwipeLayout) convertView;

            holder.image.setImageURI(Uri.parse(item.frontCoverImgUrl));
            holder.title.setText(item.name);
            holder.price.setText("￥ " + item.price);
            holder.location.setText(item.location);

            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getActivity())
                            .setMessage("将该商品从本平台移除?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ItemInfo info = mAdapter.data.get(position);
                                    mAdapter.removeItem(position);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    swipeLayout.close(true);
                                }
                            })
                            .show();
                }
            });

            return convertView;
        }

        class ViewHolder {
            int index;
            SimpleDraweeView image;
            TextView title;
            TextView price;
            TextView location;
            Button delete;
        }

    }
}
