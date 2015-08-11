package com.example.secondhandstreet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.secondhandstreet.Utils.LogUtil;

/**
 * Created by huangxueqin on 15-4-15.
 */
public class UserAccountHelper extends SQLiteOpenHelper{
    private static final String DB_NAME = "db_user_account";
    private static final int DB_VERSION = 1;

    private static final String TABLE_USER_INFO = "user_info_table";
    private static final String COLUMN_USER_ID = "userid";
    private static final String COLUMN_USER_NAME = "username";
    private static final String COLUMN_USER_PHONE = "userphone";
    private static final String COLUMN_USER_QQ = "userqq";
    private static final String COLUMN_USER_EMAIL = "useremail";
    private static final String COLUMN_USER_AVATAR_URL = "user_avatar_url";
    private static final String COLUMN_USER_AVATAR_CACHE = "user_avatar_cache";
    private static final String COLUMN_USER_DATA_DB_NAME = "user_data_db";

    public UserAccountHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        addUserInfo(UserInfo.sample);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USER_INFO +
                "(" +
                COLUMN_USER_ID + " TEXT PRIMARY KEY, " +
                COLUMN_USER_NAME + " TEXT," +
                COLUMN_USER_PHONE + " TEXT," +
                COLUMN_USER_QQ + " TEXT," +
                COLUMN_USER_EMAIL + " TEXT," +
                COLUMN_USER_AVATAR_URL + " TEXT," +
                COLUMN_USER_AVATAR_CACHE + " TEXT," +
                COLUMN_USER_DATA_DB_NAME + " TEXT" +
                ");");
    }

    public static final String getUserDataDb(String id) {
        return "db_user_data_" + id;
    }

    public long addOrUpdateUserInfo(UserInfo info) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery("select * from " + TABLE_USER_INFO + " where " + COLUMN_USER_ID + " = ?",
                new String[]{info.id});
        boolean find = c.getCount() > 0;
        c.close();

        long result = -1;
        if(find) {
            result = updateUserInfo(info.id, info);
        }
        else {
            ContentValues values = new ContentValues();
            values.put(COLUMN_USER_ID, info.id);
            values.put(COLUMN_USER_NAME, info.username);
            values.put(COLUMN_USER_PHONE, info.phone);
            values.put(COLUMN_USER_QQ, info.qq);
            values.put(COLUMN_USER_EMAIL, info.email);
            values.put(COLUMN_USER_AVATAR_URL, info.avatarImg);
            values.put(COLUMN_USER_DATA_DB_NAME, getUserDataDb(info.id));
            result = db.insert(TABLE_USER_INFO, null, values);
        }
        db.close();
        return result;
    }

    public boolean addUserInfo(UserInfo info) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery("select * from " + TABLE_USER_INFO + " where " + COLUMN_USER_ID + " = ?",
                new String[]{info.id});
        if(c.getCount() > 0) {
            LogUtil.logd("already exist row " + info.id);
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, info.id);
        values.put(COLUMN_USER_NAME, info.username);
        values.put(COLUMN_USER_PHONE, info.phone);
        values.put(COLUMN_USER_QQ, info.qq);
        values.put(COLUMN_USER_EMAIL, info.email);
        values.put(COLUMN_USER_AVATAR_URL, info.avatarImg);
        values.put(COLUMN_USER_DATA_DB_NAME, getUserDataDb(info.id));
        long result = db.insert(TABLE_USER_INFO, null, values);
        c.close();
        db.close();
        return result != -1l;
    }

    public int updateUserInfo(String userId, UserInfo info) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        if(info.username != null) {
            values.put(COLUMN_USER_NAME, info.username);
        }
        if(info.phone != null) {
            values.put(COLUMN_USER_PHONE, info.phone);
        }
        if(info.qq != null) {
            values.put(COLUMN_USER_QQ, info.qq);
        }
        if(info.email != null) {
            values.put(COLUMN_USER_EMAIL, info.email);
        }
        if(info.avatarImg != null) {
            values.put(COLUMN_USER_AVATAR_URL, info.avatarImg);
        }
        int result = db.update(TABLE_USER_INFO, values, COLUMN_USER_ID + " = ?", new String[]{userId});
        db.close();
        return result;
    }

    public UserInfo getUserInfo(String id) {
        if(id == null) {
            return null;
        }

        UserInfo info = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = getReadableDatabase().rawQuery("select * from " + TABLE_USER_INFO + " where "+ COLUMN_USER_ID + " = ?",
                new String[]{id});
        if(c.getCount() > 0) {
            info = new UserInfo();
            int indexUserid = c.getColumnIndex(COLUMN_USER_ID);
            int indexUsername = c.getColumnIndex(COLUMN_USER_NAME);
            int indexUserphone = c.getColumnIndex(COLUMN_USER_PHONE);
            int indexUserQQ = c.getColumnIndex(COLUMN_USER_QQ);
            int indexUseremail = c.getColumnIndex(COLUMN_USER_EMAIL);
            int indexUseravatar = c.getColumnIndex(COLUMN_USER_AVATAR_URL);
            c.moveToFirst();
            info.id = c.getString(indexUserid);
            info.username = c.getString(indexUsername);
            info.phone = c.getString(indexUserphone);
            info.qq = c.getString(indexUserQQ);
            info.email = c.getString(indexUseremail);
            info.avatarImg = c.getString(indexUseravatar);
        }
        c.close();
        db.close();
        return info;
    }

    public void clearAllUserInfos() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_USER_INFO, "1", null);
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
