package com.example.secondhandstreet;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.secondhandstreet.Utils.NetworkUtils;
import com.example.secondhandstreet.Utils.Utilities;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by huangxueqin on 15-4-14.
 */
public class LoginActivity extends ActionBarActivity implements View.OnClickListener{
    private static final String TAG = "HXQ_TAG";
    private static final String LOGIN_PROMPT = "正在登录..";

    SimpleDraweeView mAvatar;
    EditText mUsername, mPassword;
    Button mLogin;
    ProgressDialog mWaitDialog;

    private boolean mLoginSuccess;
    private boolean mIsLogining;
    private String mUserId;
    private LoginTask mLoginTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_login);
        initToolbar();
        initViews();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void initViews() {
        mWaitDialog = Utilities.getProgressDialog(this);
        mAvatar = (SimpleDraweeView) findViewById(R.id.avatar);
        mAvatar.setImageURI(CacheManager.DEFAULT_AVATAR_URI);

        mUsername = (EditText) findViewById(R.id.login_username);
        mPassword = (EditText) findViewById(R.id.login_password);

        mLogin = (Button) findViewById(R.id.btn_login);
        mLogin.setOnClickListener(this);
    }

    private void startLogin() {
        String username = mUsername.getText().toString();
        String password = mPassword.getText().toString();
        if(username == null || username.length() == 0) {
            Toast.makeText(this, "username can't be empty", Toast.LENGTH_SHORT).show();
        }
        else if(password == null || password.length() == 0){
            Toast.makeText(this, "password can't be empty", Toast.LENGTH_SHORT).show();
        }
        else {
            mLoginTask = new LoginTask();
            mLoginTask.execute(Settings.LOGIN_URL, username, password);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_login:
                startLogin();
                break;
        }
    }

    private void finishActivity() {
        if(mLoginSuccess) {
            this.setResult(MainActivity.RESULT_OK);
        }
        finish();
    }

    @Override
    protected void onPause() {
        SharedPreferences sp = getSharedPreferences(Settings.SP_NAME, MODE_PRIVATE);
        sp.edit().putBoolean(Settings.SP_LOGIN_STATE, mLoginSuccess).commit();
        if(mLoginSuccess) {
            sp.edit().putString(Settings.SP_USER_ID, mUserId).commit();
        }
        super.onPause();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if(mIsLogining) {
                if(mIsLogining) {
                    mLoginTask.cancel(true);
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            if(mIsLogining) {
                mLoginTask.cancel(true);
                mLoginSuccess = false;
            }
            finishActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class LoginTask extends AsyncTask<String, Void, UserInfo> {
        @Override
        protected void onCancelled() {
            super.onCancelled();
            mIsLogining = false;
            mLoginSuccess = false;
            mUserId = null;
            mWaitDialog.dismiss();
        }

        @Override
        protected void onPostExecute(UserInfo userInfo) {
            super.onPostExecute(userInfo);
            mIsLogining = false;
            mWaitDialog.dismiss();
            if(userInfo != null) {
                mUserId = userInfo.id;
                CacheManager.switchUser(userInfo);
                mLoginSuccess = true;
                finishActivity();
            }
            else {
                Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                mUserId = null;
                mLoginSuccess = false;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mWaitDialog.setMessage(LOGIN_PROMPT);
            mWaitDialog.show();
            mIsLogining = true;
        }

        @Override
        protected UserInfo doInBackground(String... params) {
            String url = params[0];
            String username = params[1];
            String password = params[2];
            JSONObject data = new JSONObject();
            try {
                data.put(Settings.JSON_KEY_USERNAME, username);
                data.put(Settings.JSON_KEY_PASSWORD, password);
                JSONObject response = NetworkUtils.getResponseByHttpPost(url, data);
                if(response != null && !response.has(Settings.JSON_KEY_ERROR)) {
                    UserInfo info = UserInfo.obtainInstance(response);
                    return info;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
