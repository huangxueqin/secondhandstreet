package com.example.secondhandstreet;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by huangxueqin on 15-4-10.
 */
public class MainApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
        CacheManager.initialize(this);
    }
}
