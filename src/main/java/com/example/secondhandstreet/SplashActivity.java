package com.example.secondhandstreet;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.secondhandstreet.Utils.DisplayUtil;
import com.facebook.drawee.view.SimpleDraweeView;

import java.lang.ref.WeakReference;

/**
 * Created by huangxueqin on 15-4-13.
 * 该Activity是系统启动欢迎界面的Activity， 欢迎界面持续的时间由
 * @see #MIN_SPLASH_SCREEN_TIME 变量控制
 * 在等待的同时加载系统数据库的缓存
 * @see CacheManager getIntance();
 */
public class SplashActivity extends Activity {
    static long MIN_SPLASH_SCREEN_TIME = 2000;
    Uri logo = Uri.parse("res://com.example.secondhandstreet/" + R.drawable.logo);
    Handler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_splash);
        SimpleDraweeView logoImg = (SimpleDraweeView)findViewById(R.id.logo);
        logoImg.setImageURI(logo);
        int screenWidth = DisplayUtil.getScreenSize(this).x;
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) logoImg.getLayoutParams();
        lp.width=screenWidth/2;
        lp.height = screenWidth/2;
        logoImg.setLayoutParams(lp);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }, MIN_SPLASH_SCREEN_TIME);
        CacheManager.getInstance();
    }

    private UserInfo getUserInfoFromServer(String id) {
        return UserInfo.sample;
    }

    private void onInitCompleted() {
        this.finish();
    }

}
