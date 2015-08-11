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
 * Created by huangxueqin on 15-4-27.
 */
public class RegisterActivity extends ActionBarActivity implements View.OnClickListener{
    private static final String REGISTER_PROMPT = "正在注册..";

    private SimpleDraweeView mAvatar;
    private EditText mUsername, mPassword, mPhone, mQQ;
    private Button mRegisterButton;
    private ProgressDialog mWaitDialog;

    private boolean mIsRegistering;
    private boolean mRegisterSuccess;
    private UserInfo mUserInfo;
    private RegisterTask mRegisterTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
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
        mUsername = (EditText) findViewById(R.id.register_username);
        mPassword = (EditText) findViewById(R.id.register_password);
        mPhone = (EditText) findViewById(R.id.register_phone);
        mQQ = (EditText) findViewById(R.id.register_qq);
        mRegisterButton = (Button) findViewById(R.id.btn_register);
        mRegisterButton.setOnClickListener(this);
    }

    private void startRegister() {
        String username = mUsername.getText().toString();
        String password = mPassword.getText().toString();
        String phone = mPhone.getText().toString();
        String qq = mQQ.getText().toString();
        if(username == null || password == null || phone == null || qq == null) {
            Toast.makeText(this, "信息不完整", Toast.LENGTH_SHORT).show();
        }
        else {
            mRegisterTask = new RegisterTask();
            mRegisterTask.execute(Settings.REGISTER_URL, username, password, phone, qq);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_register:
                startRegister();
                break;
        }
    }

    private void finishActivity() {
        if(mRegisterSuccess) {
            this.setResult(MainActivity.RESULT_OK);
        }
        finish();
    }

    @Override
    protected void onPause() {
        SharedPreferences sp = getSharedPreferences(Settings.SP_NAME, MODE_PRIVATE);
        sp.edit().putBoolean(Settings.SP_LOGIN_STATE, mRegisterSuccess).commit();
        if(mRegisterSuccess) {
            sp.edit().putString(Settings.SP_USER_ID, mUserInfo.id).commit();
        }
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if(mIsRegistering) {
                mRegisterTask.cancel(true);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            if(mIsRegistering) {
                mRegisterTask.cancel(true);
                mRegisterSuccess = false;
            }
            finishActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class RegisterTask extends AsyncTask<String, Void, UserInfo> {
        @Override
        protected void onCancelled() {
            super.onCancelled();
            mIsRegistering = false;
            mRegisterSuccess = false;
            mUserInfo = null;
            mWaitDialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mWaitDialog.setMessage(REGISTER_PROMPT);
            mWaitDialog.show();
            mIsRegistering = true;
        }

        @Override
        protected void onPostExecute(UserInfo userInfo) {
            super.onPostExecute(userInfo);
            mIsRegistering = false;
            mWaitDialog.dismiss();
            if(userInfo != null) {
                mUserInfo = userInfo;
                CacheManager.switchUser(userInfo);
                mRegisterSuccess = true;
                finishActivity();
            }
            else {
                Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                mUserInfo = null;
                mRegisterSuccess = false;
            }
        }

        @Override
        protected UserInfo doInBackground(String... params) {
            String url = params[0];
            String username = params[1];
            String password = params[2];
            String phone = params[3];
            String qq = params[4];
            JSONObject data = new JSONObject();
            try {
                data.put(Settings.JSON_KEY_USERNAME, username);
                data.put(Settings.JSON_KEY_PASSWORD, password);
                data.put(Settings.JSON_KEY_PHONE, phone);
                data.put(Settings.JSON_KEY_QQ, qq);
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
