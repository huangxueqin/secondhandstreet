package com.example.secondhandstreet;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.renderscript.Sampler;

import com.example.secondhandstreet.Utils.LogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangxueqin on 15-4-14.
 * 该类用于处理系统的缓存,其工作原理是新建了一个用户资料的数据库
 * 其中包含了一个默认的游客用户，用于非登录状态时的访问。
 * 同时它为每个登录的用户新建了一个独立的数据库，用户存放用户的浏览数据缓存
 * 目前缓存的类型包括：最新商品列表缓存，推荐列表缓存和搜索历史缓存
 * 当断网时为了软件的显示效果，系统为展示前一次浏览数据的缓存
 *
 * 同时该类还处理用户切换登录用户所需要处理的事件。通过
 * @see #registerForUserSwitch 方法来注册所需的操作
 */
public class CacheManager {
    static List<ItemInfo> sRecommendCache = new ArrayList<>();
    static List<ItemInfo> sNewestCache = new ArrayList<>();
    static List<SearchManager.SearchRecord> sSearchHistory = new ArrayList<>();
    static CacheManager sGuestCache;

    public static Uri DEFAULT_AVATAR_URI = Uri.parse("res://com.example.secondhandstreet/" + R.drawable.default_avatar);
    public static Uri DEFAULT_AVATAR_URI2 = Uri.parse("res://com.example.secondhandstreet/" + R.drawable.default_avatar2);
    public static Uri loadingGif = Uri.parse("res://com.example.secondhandstreet/" + R.drawable.loading);
    public static Uri loadingGifBig = Uri.parse("res://com.example.secondhandstreet/" + R.drawable.loading_big);

    private static String CACHE_IMG_DIR = "/shs/thumbnails/";

    private static CacheManager sInstance;
    private static Context sContext;
    private static List<UserSwitchedCallback> sCallbacks;
    private static UserAccountHelper sUserAccountHelper;

    private Context mContext;
    private UserInfo mUserInfo;
    private UserDataHelper mUserDataHelper;
    private Uri mCachedAvatarUri;
    private List<SearchManager.SearchRecord> searchCache;
    private List<ItemInfo> recommCache;
    private List<ItemInfo> newestCache;

    public interface UserSwitchedCallback {
        void onUserSwitched();
    }

    private CacheManager(Context context, UserInfo info) {
        mContext = context;
        mUserDataHelper = new UserDataHelper(mContext, info.id);
        mUserInfo = info;
        // add an SAMPLE_ID for test
        if (info == UserInfo.sample) {
            mCachedAvatarUri = DEFAULT_AVATAR_URI;
        } else {
            mCachedAvatarUri = getCachedAvatarFromSdcard(info.id);
        }
        recommCache = mUserDataHelper.getAllRecommendCaches();
        newestCache = mUserDataHelper.getAllNewestCaches();
        searchCache = mUserDataHelper.getAllHistoryRecord();
    }

    public static synchronized void initialize(Context context) {
        sContext = context;
        sCallbacks = new ArrayList<>();
        sUserAccountHelper = new UserAccountHelper(sContext);
        sGuestCache = new CacheManager(sContext, UserInfo.sample);
    }

    public static Context getApplicatioinContext() {
        return sContext;
    }

    public static CacheManager getInstance() {
        return sInstance;
    }

    public static synchronized void switchToGuestUser() {
        sInstance = sGuestCache;
        for(UserSwitchedCallback callback : sCallbacks) {
            callback.onUserSwitched();
        }
    }

    public static UserInfo getCachedUserInfo(String userId) {
        return sUserAccountHelper.getUserInfo(userId);
    }

    public static synchronized void switchUser(UserInfo info) {
        sInstance.sUserAccountHelper.addOrUpdateUserInfo(info);
        sInstance = new CacheManager(sContext, info);
        for(UserSwitchedCallback callback : sCallbacks) {
            callback.onUserSwitched();
        }
    }

    public UserInfo getUserInfo() {
        return mUserInfo;
    }

    public synchronized void updateUserInfo(UserInfo newInfo) {
        String id = mUserInfo.id;
        sUserAccountHelper.updateUserInfo(id, newInfo);
        mUserInfo = newInfo;
        for(UserSwitchedCallback callback : sCallbacks) {
            callback.onUserSwitched();
        }
    }

    public synchronized void updateUserInfo(UserInfo newInfo, Bitmap newAvatar) {
        String id = mUserInfo.id;
        updateUserInfo(newInfo);
        if(newAvatar != null) {
            writeAvatarCache(newAvatar, id);
            mCachedAvatarUri = getCachedAvatarFromSdcard(id);
        }
    }

    public synchronized void writeRecommendCache(List<ItemInfo> items) {
        mUserDataHelper.updateItemsInRecommendCache(items);
    }

    public synchronized void writeNewestCache(List<ItemInfo> items) {
        mUserDataHelper.updateItemsInNewestCache(items);
    }

    public static void registerForUserSwitch(UserSwitchedCallback callback) {
        for(UserSwitchedCallback c : sCallbacks) {
            if(c == callback) {
                return;
            }
        }
        sCallbacks.add(callback);
    }

    public static void unRegisterForUserSwitch(UserSwitchedCallback callback) {
        for(UserSwitchedCallback c : sCallbacks) {
            if(c == callback) {
                sCallbacks.remove(c);
                return;
            }
        }
    }

    public Uri getCachedAvatarUri() {
        return mCachedAvatarUri;
    }

    public List<SearchManager.SearchRecord> getSearchHistory() {
        return searchCache;
    }

    public List<ItemInfo> getRecommendCache() {
        return recommCache;
    }

    public List<ItemInfo> getNewestCache() {
        return newestCache;
    }

    private static  Uri getCachedAvatarFromSdcard(String userId) {
        if(!isExternalStorageReadable()) {
            return null;
        }
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        File cacheDir = new File(root + CACHE_IMG_DIR);
        if(!cacheDir.exists()) cacheDir.mkdirs();
        String avatarCacheName = getCacheAvatarName(userId);
        File cachedAvatar = new File(cacheDir, avatarCacheName);
        if(cachedAvatar.exists()) {
            return Uri.fromFile(cachedAvatar);
        }
        else {
            return null;
        }
    }

    private static void writeAvatarCache(Bitmap avatar, String userId) {
        if(!isExternalStorageWritable()) {
            return ;
        }
        LogUtil.logd("writeAvatar to sdcard");
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        File cacheDir = new File(root + CACHE_IMG_DIR);
        if(!cacheDir.exists()) cacheDir.mkdirs();
        String avatarCacheName = getCacheAvatarName(userId);
        File cachedAvatar = new File(cacheDir, avatarCacheName);
        if(cachedAvatar.exists()) cachedAvatar.delete();
        try {
            FileOutputStream out = new FileOutputStream(cachedAvatar);
            avatar.compress(Bitmap.CompressFormat.JPEG, 100, out);
            avatar.recycle();
            out.flush();
            out.close();
        } catch (Exception e) {
            LogUtil.logd("saveFile failed");
            e.printStackTrace();
        }
    }



    public static String getCacheAvatarName(String id) {
        return "avatar" + id + ".jpg";
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

}
