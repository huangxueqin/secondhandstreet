package com.example.secondhandstreet.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.secondhandstreet.R;
import com.example.secondhandstreet.Utils.Utilities;

/**
 * Created by huangxueqin on 15-4-24.
 */
public class EditTextActivity extends ActionBarActivity{
    EditText mInput;
    String mTitle;
    int mRequestType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_text);
        Intent data = getIntent();
        mTitle = data.getStringExtra(ProfileModifyActivity.KEY_TITLE);
        mRequestType = data.getIntExtra(ProfileModifyActivity.KEY_REQUEST_TYPE, -1);

        initToolbar();
        mInput = (EditText) findViewById(R.id.input);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            TextView title = (TextView) toolbar.findViewById(R.id.title);
            title.setText(mTitle);
        }
    }

    private boolean checkDataValidity(int requestType, String data) {
        boolean result = true;
        switch (requestType) {
            case ProfileModifyActivity.REQUEST_UPDATE_USER_PHONE:
                result = Utilities.validPhoneNumber(data);
                break;
            case ProfileModifyActivity.REQUEST_UPDATE_USER_QQ:
                result = Utilities.validQQ(data);
                break;
            case ProfileModifyActivity.REQUEST_UPDATE_PASSWORD:
                result = Utilities.validPassword(data);
                break;
        }
        return result;
    }

    private void saveData() {
        String data = mInput.getText().toString();
        if(data != null && checkDataValidity(mRequestType, data)) {
            Intent dataBack = new Intent();
            dataBack.putExtra(ProfileModifyActivity.KEY_BACK_DATA, mInput.getText().toString());
            setResult(RESULT_OK, dataBack);
            finish();
        }
        else {
            Toast.makeText(this, "数据不合法", Toast.LENGTH_SHORT).show();
        }
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
                saveData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
