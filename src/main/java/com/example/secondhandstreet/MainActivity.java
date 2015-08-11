package com.example.secondhandstreet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.secondhandstreet.Utils.NetworkUtils;
import com.example.secondhandstreet.Utils.Utilities;
import com.example.secondhandstreet.classify.CategoryFragment;
import com.example.secondhandstreet.discovery.DiscoveryFragment;
import com.example.secondhandstreet.home.HomeFragment;
import com.example.secondhandstreet.profile.ProfileFragment;
import com.example.secondhandstreet.publish.PublishItemActivity;
import com.example.secondhandstreet.publish.PublishMessageActivity;

/**
 * @author huangxueqin
 * @version 1.0
 *
 * 这是软件的主Activity——即欢迎界面过后启动的Activity
 * 该类创建了系统的主界面，主要包括：
 * 1. 软件底部的5个tab {首页，分类，发布，求购，我的}
 * 2. 初始化与5个tab相对应的5个页面，分别是
 *    a. HomeFragment -----首页
 *    b. CategoryFragment -----分类
 *    c. PublishItemActivity, PublishMessageActivity -----发布
 *    d. DiscoveryFragment -----求购
 *    e. ProfileFragment -----我的
 * 该类首先初始化了这些Fragment和Activity， 同时检查了是否已经登录
 * 并由登录与否处理这些页面的加载与跳转
 */

public class MainActivity extends ActionBarActivity implements View.OnClickListener{
    private static final String TAG = "HXQ_TAG";

    private static final int REQUEST_REGISTER = 99;
    private static final int REQUEST_LOGIN = 100;

    private static final int HOME_TAB = 0;
    private static final int DISCOVER_TAB = 1;
    private static final int PUBLISH_TAB = 2;
    private static final int CLASSIFY_TAB = 3;
    private static final int PROFILE_TAB = 4;

    private Fragment mHomeFragment, mCategoryFragment, mDiscoverFragment, mProfileFragment;
    private TabIndicatorLayout mTabs;
    private Toolbar mToolbar;

    private boolean mHasLogin = false;
    private int mCurrentDisplayTab = -1;
    private boolean showProfileFragment = false;
    private boolean needSplashScreen = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initToolbar();
        initCacheAndFragments();
    }

    // 初始化系统的缓存和底部4个tab所指示的Fragment
    private void initCacheAndFragments() {
        SharedPreferences sp = getSharedPreferences(Settings.SP_NAME, MODE_PRIVATE);
        mHasLogin = sp.getBoolean(Settings.SP_LOGIN_STATE, false);
        String userId = null;
        if(mHasLogin) {
            userId = sp.getString(Settings.SP_USER_ID, null);
        }
        if(userId == null) {
            CacheManager.switchToGuestUser();
        }
        else {
            UserInfo user = CacheManager.getCachedUserInfo(userId);
            if(user != null) {
                CacheManager.switchUser(user);
            }
            else {
                CacheManager.switchToGuestUser();
                mHasLogin = false;
            }
        }
        initBottomTabs();
        initFragments();
        switchToFragment(HOME_TAB);
    }

    // 初始化Toolbar，隐藏Toolbar自带的标题
    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.id_toolbar);
        if(mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    // 初始化底部的5个tab
    private void initBottomTabs() {
        mTabs = (TabIndicatorLayout) findViewById(R.id.bottom_tabs);
        mTabs.addView(createTabIndicator(R.drawable.indicator_home, HOME_TAB));
        mTabs.addView(createTabIndicator(R.drawable.indicator_classify, CLASSIFY_TAB));
        mTabs.addView(createTabIndicator(R.drawable.indicator_publish, PUBLISH_TAB));
        mTabs.addView(createTabIndicator(R.drawable.indicator_discover, DISCOVER_TAB));
        mTabs.addView(createTabIndicator(R.drawable.indicator_profile, PROFILE_TAB));
    }

    private TabIndicator createTabIndicator(int imgRes, int type) {
        TabIndicator indicator = new TabIndicator(this);
        indicator.setImageResource(imgRes);
        indicator.setType(type);
        indicator.setOnClickListener(this);
        return indicator;
    }

    private void initFragments() {
        mHomeFragment = new HomeFragment();
        mCategoryFragment = new CategoryFragment();
        mDiscoverFragment = new DiscoveryFragment();
        mProfileFragment = new ProfileFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(!mHomeFragment.isAdded()) {
            transaction.add(R.id.container, mHomeFragment);
        }
        if(!mDiscoverFragment.isAdded()) {
            transaction.add(R.id.container, mDiscoverFragment);
        }
        if(!mCategoryFragment.isAdded()) {
            transaction.add(R.id.container, mCategoryFragment);
        }
        if(!mProfileFragment.isAdded()) {
            transaction.add(R.id.container, mProfileFragment);
        }
        hideAllFragment(transaction);
        transaction.commit();
    }

    private void hideAllFragment(FragmentTransaction transaction) {
        transaction.hide(mHomeFragment);
        transaction.hide(mDiscoverFragment);
        transaction.hide(mCategoryFragment);
        transaction.hide(mProfileFragment);
    }

    // 处理点击某个tab跳转到相应的Fragment
    private void switchToFragment(int type) {
        mTabs.switchToTabOfType(type);
        Fragment fragment = getFragmentByTabType(type);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideAllFragment(transaction);
        transaction.show(fragment);
        transaction.commit();
        mCurrentDisplayTab = type;
        // do something, like change toolbar icon when switch to a new fragment
        switch(type) {
            case HOME_TAB:
                invalidateOptionsMenu();
                break;
            case CLASSIFY_TAB:
                invalidateOptionsMenu();
                break;
            case DISCOVER_TAB:
                invalidateOptionsMenu();
                break;
            case PROFILE_TAB:
                invalidateOptionsMenu();
                break;
        }
    }

    // 通过名字获取相应的Fragment对象
    Fragment getFragmentByTabType(int type) {
        Fragment attachedFragment = null;
        switch(type) {
            case HOME_TAB:
                attachedFragment = mHomeFragment;
                break;
            case CLASSIFY_TAB:
                attachedFragment = mCategoryFragment;
                break;
            case DISCOVER_TAB:
                attachedFragment = mDiscoverFragment;
                break;
            case PROFILE_TAB:
                attachedFragment = mProfileFragment;
                break;
        }
        return attachedFragment;
    }

    private void showContextMenu(View v) {
        registerForContextMenu(v);
        openContextMenu(v);
        unregisterForContextMenu(v);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(NetworkUtils.getNetWorkState(this) == NetworkUtils.NET_STATE_OFFLINE) {
            Toast.makeText(this, "您处于离线状态，软件功能不能使用", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                Intent login = new Intent(this, LoginActivity.class);
                startActivityForResult(login, REQUEST_LOGIN);
                break;
            case 1:
                Intent register = new Intent(this, RegisterActivity.class);
                startActivityForResult(register, REQUEST_REGISTER);
                break;
            case 2:
                Intent publishMessageIntent = new Intent(this, PublishMessageActivity.class);
                startActivity(publishMessageIntent);
                break;
            case 3:
                Intent publishIntent = new Intent(this, PublishItemActivity.class);
                startActivity(publishIntent);
                break;
        }
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if(!mHasLogin) {
            menu.setHeaderTitle(R.string.main_context_menu_title);
            menu.add(0, 0, 0, R.string.main_context_menu_login);
            menu.add(0, 1, 0, R.string.main_context_menu_register);
        }
        else {
            menu.setHeaderTitle(R.string.main_context_menu_title_login);
            menu.add(0, 3, 0, R.string.main_context_menu_publish_item);
            menu.add(0, 2, 0, R.string.main_context_menu_publish_message);
        }
    }

    @Override
    public void onClick(View v) {
        if(v instanceof TabIndicator) {
            TabIndicator tab = (TabIndicator) v;
            int type = tab.getType();
            if(type == PUBLISH_TAB) {
                showContextMenu(tab);
            }
            else if(!mHasLogin && type == PROFILE_TAB){
                showContextMenu(tab);
            }
            else {
                switchToFragment(type);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_LOGIN:
                if(resultCode == RESULT_OK) {
                    mHasLogin = true;
                    showProfileFragment = true;
                }
                break;
            case REQUEST_REGISTER:
                if(resultCode == RESULT_OK) {
                    mHasLogin = true;
                    showProfileFragment = true;
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        SharedPreferences sp = getSharedPreferences(Settings.SP_NAME, MODE_PRIVATE);
//        if(needSplashScreen) {
//            needSplashScreen = false;
//            startActivity(new Intent(this, SplashActivity.class));
//        }
        if(mHasLogin && showProfileFragment) {
            switchToFragment(PROFILE_TAB);
            showProfileFragment = false;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean result = super.onPrepareOptionsMenu(menu);
        MenuItem modify = menu.findItem(R.id.action_modify_profile);
        if(modify != null) {
            modify.setVisible(mCurrentDisplayTab == PROFILE_TAB);
        }
        menu.findItem(R.id.action_search).setVisible(mCurrentDisplayTab != PROFILE_TAB);
        menu.findItem(R.id.action_logout).setVisible(mHasLogin);
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // 处理Toolbar的菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id) {
            case R.id.action_settings:
                return true;
            case R.id.action_search:
                Intent searchIntent = new Intent(this, SearchActivity.class);
                startActivity(searchIntent);
                return true;
            case R.id.action_logout:
                logout();
                return true;
            case R.id.action_about:
                showAboutDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // 处理About菜单事件
    private void showAboutDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.about_dialog, null);
        new AlertDialog.Builder(this)
                .setTitle(R.string.action_about)
                .setView(dialogView)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    // 处理登出事件
    private void logout() {
        mHasLogin = false;
        ProgressDialog waitDialog = Utilities.getProgressDialog(this);
        waitDialog.setMessage("Logout...");
        waitDialog.show();
        CacheManager.switchToGuestUser();
        if (mCurrentDisplayTab == PROFILE_TAB) {
            switchToFragment(HOME_TAB);
        }
        waitDialog.dismiss();
        Toast.makeText(this, "已登出", Toast.LENGTH_SHORT).show();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_placeholder, container, false);
            return rootView;
        }
    }
}
