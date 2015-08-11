package com.example.secondhandstreet;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangxueqin on 15-4-15.
 */
public class SearchManager {
    private Context mContext;
    private CacheManager mCacheManager;

    public SearchManager(Context context) {
        this.mCacheManager = CacheManager.getInstance();
        mContext = context;
    }

    public List<SearchRecord> getSearchHistory() {
        return mCacheManager.getSearchHistory();
    }

    public static class SearchRecord {
        public static SearchRecord sample = new SearchRecord();
        static {
            sample.content = "sample record";
        }

        public String content;
        public int year;
        public int month;
        public int day;
    }
}
