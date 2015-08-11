package com.example.secondhandstreet.publish;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.secondhandstreet.CacheManager;
import com.example.secondhandstreet.R;
import com.example.secondhandstreet.Settings;
import com.example.secondhandstreet.Utils.NetworkUtils;
import com.example.secondhandstreet.Utils.Utilities;
import com.example.secondhandstreet.discovery.DiscoveryFragment;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by huangxueqin on 15-5-1.
 * 发布求购信息的界面
 */
public class PublishMessageActivity extends ActionBarActivity implements View.OnClickListener{

    private EditText mContent;
    private EditText mTitle;
    private EditText mPrice;
    private View mPublish;
    private ProgressDialog mWaitDialog;
    private String mContentStr;
    private String mTitleStr;
    private String mPriceStr;

    private boolean mIsPublishing;
    private PublishTask mTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_message);
        initToolbar();
        mWaitDialog = Utilities.getProgressDialog(this);
        mContent = (EditText) findViewById(R.id.item_content);
        mTitle  = (EditText) findViewById(R.id.item_title);
        mPrice = (EditText) findViewById(R.id.item_price);
        mPublish = findViewById(R.id.publish);
        mPublish.setOnClickListener(this);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void safeFinishActivity() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                safeFinishActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearPublishInfo() {
        mContentStr = null;
        mTitleStr = null;
        mPriceStr = null;
    }

    private boolean checkPublishInfo() {
        mContentStr = mContent.getText().toString();
        if(mContentStr == null || mContent.length() == 0) {
            mContentStr = null;
            return false;
        }
        mTitleStr = mTitle.getText().toString();
        if(mTitle == null || mContent.length() == 0) {
            mContentStr = null;
            return false;
        }
        mPriceStr = mPrice.getText().toString();
        if(mPriceStr == null || mPriceStr.length() == 0) {
            mPriceStr = null;
            return false;
        }
        return true;
    }

    private void doPublish() {
        mTask = new PublishTask();
        mTask.execute(mTitleStr, mPriceStr, mContentStr);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.publish:
                if(checkPublishInfo()) {
                    doPublish();
                }
                break;
        }
    }

    private class PublishTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            String title = params[0];
            String price = params[1];
            String content = params[2];
            String userId = CacheManager.getInstance().getUserInfo().id;
            JSONObject data = new JSONObject();
            try {
                data.put("userid", userId);
                data.put("title", title);
                data.put("price", price);
                data.put("describe", content);
                JSONObject response = NetworkUtils.getResponseByHttpPost(Settings.PUBLISH_MESSAGE_URL, data);
                if(response != null && !response.has(Settings.JSON_KEY_ERROR)) {
                    DiscoveryFragment.DiscoveryInfo info = DiscoveryFragment.DiscoveryInfo.obtainInstance(response);
                    return info != null ? 1 : -1;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return -1;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mIsPublishing = true;
            mWaitDialog.show();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            mWaitDialog.dismiss();
            mIsPublishing = false;
            if(integer > 0) {
                finish();
            }
            else {
                Toast.makeText(PublishMessageActivity.this, "发布失败", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mWaitDialog.dismiss();
            mIsPublishing = false;
        }
    }
}
