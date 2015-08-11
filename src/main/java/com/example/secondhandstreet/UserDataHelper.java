package com.example.secondhandstreet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangxueqin on 15-4-13.
 */
public class UserDataHelper extends SQLiteOpenHelper{
    private static final int DB_VERSION = 1;

    private static final int MAX_ITEM_INFO_RECORD_NUM = 20;
    private static final String TABLE_RECOMMEND_ITEM = "recommend_item_table";
    private static final String TABLE_NEWEST_ITEM = "newest_item_table";
    private static final String COLUMN_ITEM_ID = "itemid";
    private static final String COLUMN_ITEM_NAME = "itemname";
    private static final String COLUMN_ITEM_PRICE = "itemprice";
    private static final String COLUMN_ITEM_FRONT_COVER_IMG = "itemfrontcoverimg";
    private static final String COLUMN_ITEM_LOCATION = "itemlocation";

    private static final int MAX_HISTORY_RECORD_NUM = 100;
    private static final String TABLE_SEARCH_HISTORY = "search_history_table";
    private static final String COLUMN_SEARCH_ID = "_id";
    private static final String COLUMN_SEARCH_CONTENT = "content";
    private static final String COLUMN_SEARCH_DATA_YEAR = "year";
    private static final String COLUMN_SEARCH_DATA_MONTH = "month";
    private static final String COLUMN_SEARCH_DATA_DAY = "day";
    private static final String ORDER_BY_DATE = "order by " +
            COLUMN_SEARCH_DATA_YEAR + ", " +
            COLUMN_SEARCH_DATA_MONTH + ", " +
            COLUMN_SEARCH_DATA_DAY + " DESC";

    private Context mContext;

    public UserDataHelper(Context context, String userId) {
        super(context, UserAccountHelper.getUserDataDb(userId), null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NEWEST_ITEM +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_ITEM_ID + " TEXT," + //UNIQUE ON CONFLICT REPLACE," +
                COLUMN_ITEM_NAME + " TEXT," +
                COLUMN_ITEM_PRICE + " TEXT," +
                COLUMN_ITEM_FRONT_COVER_IMG + " TEXT," +
                COLUMN_ITEM_LOCATION + " TEXT" +
                ");");

        db.execSQL("CREATE TABLE " + TABLE_RECOMMEND_ITEM +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_ITEM_ID + " TEXT," + // UNIQUE ON CONFLICT REPLACE," +
                COLUMN_ITEM_NAME + " TEXT," +
                COLUMN_ITEM_PRICE + " TEXT," +
                COLUMN_ITEM_LOCATION + " TEXT," +
                COLUMN_ITEM_FRONT_COVER_IMG + " TEXT" +
                ");");

        db.execSQL("CREATE TABLE " + TABLE_SEARCH_HISTORY +
                "(" +
                COLUMN_SEARCH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_SEARCH_CONTENT + " TEXT," + // UNIQUE ON CONFLICT REPLACE," +
                COLUMN_SEARCH_DATA_YEAR + " INTEGER," +
                COLUMN_SEARCH_DATA_MONTH + " INTEGER," +
                COLUMN_SEARCH_DATA_DAY + " INTEGER" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void clearSearchHistoryCache() {
        deleteAllRowsInTable(TABLE_SEARCH_HISTORY);
    }

    public void addSearchHistories(List<SearchManager.SearchRecord> records) {
        SQLiteDatabase db = getWritableDatabase();

        // make db no bigger than MAX_HISTORY_RECORD_NUM
        Cursor c = db.rawQuery("select * from " + TABLE_SEARCH_HISTORY +
                ORDER_BY_DATE, null);
        int count = c.getCount();
        if(count + records.size() > MAX_HISTORY_RECORD_NUM) {
            int deleteNum = count + records.size() - MAX_HISTORY_RECORD_NUM;
            c.moveToLast();
            for(int i = 0; i<deleteNum; i++) {
                int id = c.getInt(c.getColumnIndex(COLUMN_SEARCH_ID));
                db.delete(TABLE_SEARCH_HISTORY, COLUMN_SEARCH_ID + " = " + id, null);
            }
        }

        for(SearchManager.SearchRecord record : records) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_SEARCH_CONTENT, record.content);
            values.put(COLUMN_SEARCH_DATA_YEAR, record.year);
            values.put(COLUMN_SEARCH_DATA_MONTH, record.month);
            values.put(COLUMN_SEARCH_DATA_DAY, record.day);
            db.insert(TABLE_SEARCH_HISTORY, null, values);
        }
        db.close();
    }

    public List<SearchManager.SearchRecord> getAllHistoryRecord() {
        List<SearchManager.SearchRecord> records = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select * from " + TABLE_SEARCH_HISTORY, null);
        if(c.moveToFirst()) {
            while(!c.isAfterLast()) {
                SearchManager.SearchRecord record = new SearchManager.SearchRecord();
                record.content = c.getString(c.getColumnIndex(COLUMN_SEARCH_CONTENT));
                record.year = c.getInt(c.getColumnIndex(COLUMN_SEARCH_DATA_YEAR));
                record.month = c.getInt(c.getColumnIndex(COLUMN_SEARCH_DATA_MONTH));
                record.day = c.getInt(c.getColumnIndex(COLUMN_SEARCH_DATA_DAY));
                records.add(record);
                c.moveToNext();
            }
        }
        c.close();
        db.close();
        return records;
    }

    public void updateItemsInRecommendCache(List<ItemInfo> infos) {
        deleteAllRowsInTable(TABLE_RECOMMEND_ITEM);
        addItemInfos(infos, TABLE_RECOMMEND_ITEM);
    }

    public void updateItemsInNewestCache(List<ItemInfo> infos) {
        deleteAllRowsInTable(TABLE_NEWEST_ITEM);
        addItemInfos(infos, TABLE_NEWEST_ITEM);
    }

    public void addItemsToRecommendCache(List<ItemInfo> infos) {
        addItemInfos(infos, TABLE_RECOMMEND_ITEM);
    }

    public void addItemsToNewestCache(List<ItemInfo> infos) {
        addItemInfos(infos, TABLE_NEWEST_ITEM);
    }

    public List<ItemInfo> getAllRecommendCaches() {
        return getAllItemInfos(TABLE_RECOMMEND_ITEM);
    }

    public List<ItemInfo> getAllNewestCaches() {
        return getAllItemInfos(TABLE_NEWEST_ITEM);
    }

    private void addItemInfos(List<ItemInfo> infoList, String tableName) {
        SQLiteDatabase db = getWritableDatabase();
        int count = 0;
        for(ItemInfo info : infoList) {
            if(count > MAX_ITEM_INFO_RECORD_NUM) break;
            count++;
            ContentValues values = new ContentValues();
            values.put(COLUMN_ITEM_ID, info.id);
            values.put(COLUMN_ITEM_NAME, info.name);
            values.put(COLUMN_ITEM_PRICE, info.price);
            values.put(COLUMN_ITEM_LOCATION, info.location);
            values.put(COLUMN_ITEM_FRONT_COVER_IMG, info.frontCoverImgUrl);
            db.insert(tableName, null, values);
        }
        db.close();
    }

    private List<ItemInfo> getAllItemInfos(String tableName) {
        ArrayList<ItemInfo> infos = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select * from " + tableName, null);
        if(c.moveToFirst()) {
            while(!c.isAfterLast()) {
                ItemInfo info = new ItemInfo();
                info.id = c.getString(c.getColumnIndex(COLUMN_ITEM_ID));
                info.name = c.getString(c.getColumnIndex(COLUMN_ITEM_NAME));
                info.price = c.getString(c.getColumnIndex(COLUMN_ITEM_PRICE));
                info.location = c.getString(c.getColumnIndex(COLUMN_ITEM_LOCATION));
                info.frontCoverImgUrl = c.getString(c.getColumnIndex(COLUMN_ITEM_FRONT_COVER_IMG));
                infos.add(info);
                c.moveToNext();
            }
        }
        c.close();
        db.close();
        return infos;
    }

    private void deleteAllRowsInTable(String tableName) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(tableName, "1", null);
        db.close();
    }
}
