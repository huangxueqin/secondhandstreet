package com.example.secondhandstreet;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.secondhandstreet.Utils.LogUtil;
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
 * Created by huangxueqin on 15-4-15.
 */

public class SearchActivity extends ActionBarActivity {
    private static final int REQUEST_NONE = 100;
    private static final int REQUEST_SEARCH = 101;
    private static final int REQUEST_LOAD_MORE = 102;

    EditText mSearchBox;
    ListView mHistoryList;
    ListView mItemList;
    SimpleItemInfoListAdapter mAdapter;
    ProgressDialog mWaitDialog;

    private SimpleDraweeView mLoadingIndicator;

    SearchManager mSearchManager;
    String mKeyWord;
    boolean isSearching = false;
    boolean isLoadMore = false;
    int requestType = REQUEST_NONE;
    int mCurrentPage = -1;
    int mTotalPage;
    SearchTask mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initToolbar();
        mItemList = (ListView) findViewById(R.id.item_list);
        mAdapter = new SimpleItemInfoListAdapter(this, null);
        mItemList.setAdapter(mAdapter);
        mItemList.setOnScrollListener(mListOnScrollListener);

        mWaitDialog = Utilities.getProgressDialog(this);

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(CacheManager.loadingGif).build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setAutoPlayAnimations(true)
                .build();
        mLoadingIndicator = (SimpleDraweeView) findViewById(R.id.load_indicator);
        mLoadingIndicator.setController(controller);

        mSearchBox = (EditText) findViewById(R.id.search_box);
        mSearchBox.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mSearchBox.setOnEditorActionListener(mSearchListener);
        mSearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String newKeyword = mSearchBox.getText().toString().trim();

                if(newKeyword != null && newKeyword.length() > 0 && (mKeyWord == null || !newKeyword.equals(mKeyWord))) {
                    mKeyWord = newKeyword;
                    startSearch();
                    mAdapter.removeAllData();
                }
                else if(newKeyword.length() == 0 || newKeyword == null) {
                    if(mTask != null) {
                        mTask.cancel(true);
                    }
                    mAdapter.removeAllData();
                    mKeyWord = null;
                }

            }
        });
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void startSearch() {
        if(mKeyWord != null && mKeyWord.length() > 0) {
            if(isLoadMore || isSearching) {
                if(mTask != null) {
                    mTask.cancel(true);
                }
                mLoadingIndicator.setVisibility(View.GONE);
                isLoadMore = false;
            }
            requestType = REQUEST_SEARCH;
            mTask = new SearchTask();
            mTask.execute(mKeyWord, "1");
        }
    }

    private boolean isOffline() {
        return NetworkUtils.getNetWorkState(CacheManager.getApplicatioinContext()) == NetworkUtils.NET_STATE_OFFLINE;
    }

    private void loadMore() {
        if(isOffline() || mTotalPage == 0) {
            return;
        }

        if(!isSearching && !isLoadMore && mCurrentPage < mTotalPage-1) {
            requestType = REQUEST_LOAD_MORE;
            mTask = new SearchTask();
            mTask.execute(mKeyWord, ""+(mCurrentPage+2));
        }
    }

    private EditText.OnEditorActionListener mSearchListener = new EditText.OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            LogUtil.logd("actionId = " + actionId);
            if(actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                mKeyWord = mSearchBox.getText().toString();
                if(mKeyWord != null && mKeyWord.length() > 0) {
                    startSearch();
                    return true;
                }
            }
            return false;
        }
    };

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.empty_menu, menu);
        menu.findItem(R.id.action_settings).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {;
        Log.d("-------->", "home button clicked");
        switch(item.getItemId()) {
            case android.R.id.home:

                if(isSearching || isLoadMore) {
                    mTask.cancel(true);
                }
                SearchActivity.this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
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

    private class SearchTask extends AsyncTask<String, Void, List<ItemInfo>> {
        int currentPage;
        int totalPage;
        @Override
        protected List<ItemInfo> doInBackground(String... params) {
            String keyword = params[0];
            String page = params[1];
            String url = Settings.SEARCH_KEYWORD_URL + "?page=" + page;
            JSONObject data = new JSONObject();
            try {
                data.put("title", keyword);
                JSONObject response = NetworkUtils.getResponseByHttpPost(url, data);
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
        protected void onCancelled() {
            super.onCancelled();
            if(requestType == REQUEST_LOAD_MORE) {
                isLoadMore = false;
            }
            else if(requestType == REQUEST_SEARCH) {
                isSearching = false;
//                mWaitDialog.dismiss();
                mLoadingIndicator.setVisibility(View.GONE);
            }
        }

        @Override
        protected void onPostExecute(List<ItemInfo> itemInfos) {
            super.onPostExecute(itemInfos);
            if(itemInfos != null) {
                if(requestType == REQUEST_LOAD_MORE) {
                    mAdapter.addData(itemInfos);
                    mCurrentPage = currentPage-1;
                    mTotalPage = totalPage;
                    isLoadMore = false;
                }
                else if(requestType == REQUEST_SEARCH) {
                    mCurrentPage = 0;
                    mTotalPage = totalPage;
                    mAdapter.replaceData(itemInfos);
                    isSearching = false;
//                    mWaitDialog.dismiss();
                    mLoadingIndicator.setVisibility(View.GONE);
                }
            }
            else {
                Toast.makeText(SearchActivity.this, "未找到符合要求的商品", Toast.LENGTH_SHORT);
                onCancelled();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(requestType == REQUEST_LOAD_MORE) {
                isLoadMore = true;
            }
            else if(requestType == REQUEST_SEARCH) {
                isSearching = true;
//                mWaitDialog.show();
                mLoadingIndicator.setVisibility(View.VISIBLE);
            }
        }
    }


    private void initHistoryList() {
        mSearchManager = new SearchManager(getApplicationContext());
        mHistoryList = (ListView) findViewById(R.id.history_list);
        mHistoryList.setOnItemClickListener(mListItemClickListener);
        List<SearchManager.SearchRecord> records = mSearchManager.getSearchHistory();
        if(records.size() != 0) {
            mHistoryList.setVisibility(View.VISIBLE);
            mHistoryList.setAdapter(new SearchListAdapter(this, records));
        }
    }

    private ListView.OnItemClickListener mListItemClickListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        }
    };

    private class SearchListAdapter extends BaseAdapter{
        private static final int MAX_LIST_NUM = 5;
        Context mContext;
        List<SearchManager.SearchRecord> mData;

        public SearchListAdapter(Context context, List<SearchManager.SearchRecord> data) {
            mContext = context;
            mData  = new ArrayList<>(data);
        }
        @Override
        public int getCount() {
            return Math.min(mData.size(), MAX_LIST_NUM);
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder = convertView == null ? null : (Holder) convertView.getTag();
            if(convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.search_history_list_item, parent, false);
                holder = new Holder(convertView);
                convertView.setTag(holder);
            }
            holder.content.setText(mData.get(position).content);
            return convertView;
        }

        private class Holder {
            public TextView content;
            public Holder(View rootView) {
                content = (TextView) rootView.findViewById(R.id.content);
            }
        }
    }
}
