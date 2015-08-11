package com.example.secondhandstreet.profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.secondhandstreet.CacheManager;
import com.example.secondhandstreet.Settings;
import com.example.secondhandstreet.Utils.FileUtil;
import com.example.secondhandstreet.Utils.LogUtil;
import com.example.secondhandstreet.Utils.NetworkUtils;
import com.example.secondhandstreet.R;
import com.example.secondhandstreet.UserInfo;
import com.example.secondhandstreet.Utils.Utilities;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONObject;

import java.io.File;

/**
 * Created by huangxueqin on 15-4-13.
 */
public class ProfileModifyActivity extends ActionBarActivity implements View.OnClickListener{
    public static final String KEY_TITLE = "key_title";
    public static final String KEY_BACK_DATA = "key_data";
    public static final String KEY_REQUEST_TYPE = "key_request_type";
    private static final String DEFAULT_CONTENT = "未填写";

    public static final int REQUEST_UPDATE_USER_QQ = 97;
    public static final int REQUEST_UPDATE_USER_PHONE = 98;
    public static final int REQUEST_UPDATE_USER_NAME = 99;
    public static final int REQUEST_UPDATE_PASSWORD = 104;
    public static final int REQUEST_TAKE_PHOTO = 100;
    public static final int REQUEST_CHOOSE_FROM_SDCARD = 101;
    public static final int REQUEST_CROP_SDCARD = 102;
    public static final int REQUEST_CROP_PHOTO = 103;

    SimpleDraweeView mAvatar;
    TextView mUsername;
    TextView mPhone;
    TextView mQQ;
    TextView mPassword;

    View mItemAvatar;
    View mItemUsername;
    View mItemPhone;
    View mItemQQ;
    View mItemPassword;
    ProgressDialog mWaitDialog;

    UserInfo mUserInfo;
    String mUpdatedUsername;
    String mUpdatedPhone;
    String mUpdatedQQ;
    String mUpdatedPassword;
    Bitmap mUpdatedAvatar;
    Uri mAvatarUri;

    boolean mIsUpdating;
    boolean mIsUpdateSuccess;
    UpdateUserInfoTask mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_profile);
        mUserInfo = CacheManager.getInstance().getUserInfo();
        initToolbar();
        initViews();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initViews() {
        mWaitDialog = Utilities.getProgressDialog(this);
        mWaitDialog.setMessage("正在更新用户信息...");

        mUsername = (TextView) findViewById(R.id.username);
        mUsername.setText(mUserInfo.username == null ? DEFAULT_CONTENT : mUserInfo.username);
        mPhone = (TextView) findViewById(R.id.phone);
        mPhone.setText(mUserInfo.phone == null ? DEFAULT_CONTENT : mUserInfo.phone);
        mQQ = (TextView) findViewById(R.id.qq);
        mQQ.setText(mUserInfo.qq == null ? DEFAULT_CONTENT : mUserInfo.qq);
        mPassword = (TextView) findViewById(R.id.password);

        mAvatar = (SimpleDraweeView) findViewById(R.id.avatar);
        setAvatarImage();

        mItemAvatar = findViewById(R.id.item_avatar);
        mItemUsername = findViewById(R.id.item_username);
        mItemPhone = findViewById(R.id.item_phone);
        mItemQQ = findViewById(R.id.item_qq);
        mItemAvatar.setOnClickListener(this);
        mItemUsername.setOnClickListener(this);
        mItemPhone.setOnClickListener(this);
        mItemQQ.setOnClickListener(this);

        mItemPassword = findViewById(R.id.item_password);
        mItemPassword.setOnClickListener(this);
    }

    private void setAvatarImage() {
        Uri cacheAvatar = CacheManager.getInstance().getCachedAvatarUri();
        if(mUserInfo.avatarImg != null) {
            mAvatar.setImageURI(Uri.parse(mUserInfo.avatarImg));
        }
        else if(cacheAvatar != null) {
            mAvatar.setImageURI(cacheAvatar);
        }
        else {
            mAvatar.setImageURI(CacheManager.DEFAULT_AVATAR_URI);
        }
    }



    private void setCropImg(Intent picdata) {
        Bundle bundle = picdata.getExtras();
        if (null != bundle) {
            Bitmap origAvatar = mUpdatedAvatar;
            mUpdatedAvatar = bundle.getParcelable("data");
            mAvatar.setImageBitmap(mUpdatedAvatar);
            if(origAvatar != null) {
                origAvatar.recycle();
            }
        }
    }

    public void cropImage(Uri uri, int requestCode) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHOOSE_FROM_SDCARD:
                if(resultCode == RESULT_OK) {
                    mAvatarUri = data.getData();
                    cropImage(mAvatarUri, REQUEST_CROP_SDCARD);
                }
                break;
            case REQUEST_TAKE_PHOTO:
                if(resultCode == RESULT_OK) {
                    cropImage(mAvatarUri, REQUEST_CROP_PHOTO);
                }
                break;
            case REQUEST_CROP_PHOTO:
                if(resultCode == RESULT_OK) {
                    setCropImg(data);
                    FileUtil.deleteFileByUri(mAvatarUri);
                    mAvatarUri = null;
                }
                break;
            case REQUEST_CROP_SDCARD:
                if(resultCode == RESULT_OK) {
                    setCropImg(data);
                }
                break;
            case REQUEST_UPDATE_USER_NAME:
                if(resultCode == RESULT_OK) {
                    if(data != null) {
                        mUpdatedUsername = data.getStringExtra(KEY_BACK_DATA);
                        mUsername.setText(mUpdatedUsername);
                    }
                }
                break;
            case REQUEST_UPDATE_USER_PHONE:
                if(resultCode == RESULT_OK) {
                    if(data != null) {
                        mUpdatedPhone = data.getStringExtra(KEY_BACK_DATA);
                        mPhone.setText(mUpdatedPhone);
                    }
                }
                break;
            case REQUEST_UPDATE_USER_QQ:
                if(resultCode == RESULT_OK) {
                    if(data != null) {
                        mUpdatedQQ = data.getStringExtra(KEY_BACK_DATA);
                        mQQ.setText(mUpdatedQQ);
                    }
                }
                break;
            case REQUEST_UPDATE_PASSWORD:
                if(resultCode == RESULT_OK) {
                    if(data != null) {
                        mUpdatedPassword = data.getStringExtra(KEY_BACK_DATA);
                        mPassword.setText(mUpdatedPassword);
                    }
                }
                break;
        }
    }

    private void clearUpdatedInfos() {
        mUpdatedPhone = null;
        mUpdatedQQ = null;
        mUpdatedUsername = null;
        mUpdatedAvatar = null;
        mUpdatedPassword = null;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getItemId() == 0) {
            LogUtil.logd("take a photo clicked");
            Intent intent = new Intent(
                    MediaStore.ACTION_IMAGE_CAPTURE);
            mAvatarUri = Uri.fromFile(new File(Environment
                    .getExternalStorageDirectory(), "avatar_"
                    + mUserInfo.id + ".png"));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mAvatarUri);
            startActivityForResult(intent, REQUEST_TAKE_PHOTO);
            return true;
        }
        else if(item.getItemId() == 1) {
            LogUtil.logd("choose from sdcard clicked");
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CHOOSE_FROM_SDCARD);
            return true;
        }
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(R.string.choose_avatar_dialog_title);
        menu.add(0, 0, 0, R.string.choose_avatar_dialog_take_photo);
        menu.add(0, 1, 0, R.string.choose_avatar_dialog_choose_from_sdcard);
    }


    private void finishActivity() {
        if(mIsUpdateSuccess) {
            Intent data = new Intent();
            data.putExtra("data", mUpdatedAvatar != null);
            this.setResult(ProfileFragment.RESULT_CODE_MODIFIED, data);
        }
        else {
            this.setResult(RESULT_CANCELED);
        }
        this.finish();
    }

    private void updateUserInfo() {
        UserInfo updatedInfo = new UserInfo();
        updatedInfo.id = mUserInfo.id;
        updatedInfo.phone = mUpdatedPhone == null ? mUserInfo.phone : mUpdatedPhone;
        updatedInfo.qq = mUpdatedQQ == null ? mUserInfo.qq : mUpdatedQQ;
        updatedInfo.username = mUpdatedUsername == null ? mUserInfo.username : mUpdatedUsername;
        mTask = new UpdateUserInfoTask();
        mTask.execute(updatedInfo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_modify_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                updateUserInfo();
                return true;
            case android.R.id.home:
                LogUtil.logd("----------->home clicked");
                if(mIsUpdating) {
                    mTask.cancel(true);
                    mIsUpdateSuccess = false;
                }
                finishActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.item_avatar:
                registerForContextMenu(mAvatar);
                openContextMenu(mAvatar);
                unregisterForContextMenu(mAvatar);
                break;
            case R.id.item_username:
                startEditTextActivity(REQUEST_UPDATE_USER_NAME);
                break;
            case R.id.item_phone:
                startEditTextActivity(REQUEST_UPDATE_USER_PHONE);
                break;
            case R.id.item_qq:
                startEditTextActivity(REQUEST_UPDATE_USER_QQ);
                break;
            case R.id.item_password:
                startEditTextActivity(REQUEST_UPDATE_PASSWORD);
                break;
        }
    }

    private void startEditTextActivity(int requestCode) {
        String title = null;
        Resources res = getResources();
        switch (requestCode) {
            case REQUEST_UPDATE_USER_NAME:
                title = res.getString(R.string.mp_update_username);
                break;
            case REQUEST_UPDATE_USER_PHONE:
                title = res.getString(R.string.mp_update_userphone);
                break;
            case REQUEST_UPDATE_USER_QQ:
                title = res.getString(R.string.mp_update_userqq);
                break;
            case REQUEST_UPDATE_PASSWORD:
                title = res.getString(R.string.mp_update_password);
                break;

        }
        Intent intent = new Intent(this, EditTextActivity.class);
        intent.putExtra(KEY_TITLE, title);
        intent.putExtra(KEY_REQUEST_TYPE, requestCode);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if(mIsUpdating) {
                mTask.cancel(true);
                mIsUpdateSuccess = false;
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private class UpdateUserInfoTask extends AsyncTask<UserInfo, Void, UserInfo> {

        @Override
        protected UserInfo doInBackground(UserInfo... params) {
            UserInfo updatedUserInfo = params[0];
            JSONObject response = NetworkUtils.uploadUserInfoInfo(Settings.UPDATE_USER_INFO_URL, updatedUserInfo, mUpdatedPassword, mUpdatedAvatar);
            if(response != null && !response.has(Settings.JSON_KEY_ERROR)) {
                UserInfo info = UserInfo.obtainInstance(response);
                return info;
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mIsUpdating = false;
            mIsUpdateSuccess = false;
            mWaitDialog.dismiss();
        }

        @Override
        protected void onPostExecute(UserInfo userInfo) {
            super.onPostExecute(userInfo);
            mIsUpdating = false;
            if(userInfo != null) {
                LogUtil.logd("update success");
                mIsUpdateSuccess = true;
                CacheManager.getInstance().updateUserInfo(userInfo, mUpdatedAvatar);
                mWaitDialog.dismiss();
                finishActivity();
            }
            else {
                Toast.makeText(ProfileModifyActivity.this, "修改信息失败", Toast.LENGTH_SHORT).show();
                mIsUpdateSuccess = false;
                mWaitDialog.dismiss();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mIsUpdating = true;
            mWaitDialog.show();
        }
    }
}
