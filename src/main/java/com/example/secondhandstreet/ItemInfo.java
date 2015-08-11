package com.example.secondhandstreet;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.secondhandstreet.Utils.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangxueqin on 15-4-10.
 */
public class ItemInfo implements Parcelable{
    private static final String UNINIT = "0";

    public static final String SAMPLE_ID = "111111";
    public static final ItemInfo sample = new ItemInfo();
    static {
        sample.id = SAMPLE_ID;
        sample.name = "sample";
        sample.price="10";
        sample.frontCoverImgUrl = "http://img1.cache.netease.com/catchpic/E/E3/E31C5A84149A77B8B83F06AFF83EE0BB.jpg";
        sample.extraImageUrls = new String[3];
        sample.content = "content";
        sample.location = "location";
        sample.owner = UserInfo.sample;
        sample.extraImageUrls[0] = "http://img1.cache.netease.com/catchpic/E/E3/E31C5A84149A77B8B83F06AFF83EE0BB.jpg";
        sample.extraImageUrls[1] = "http://img1.cache.netase.com/catchpic/E/E3/E31C5A84149A77B8B83F06AFF83EE0BB.jpg";
        sample.extraImageUrls[2] = "http://img1.cache.neteasee.com/catchpic/E/E3/E31C5A84149A77B8B83F06AFF83EE0BB.jpg";
    }

    public String id;
    public String name;
    public String category;
    public String content;
    public String price;
    public String frontCoverImgUrl;
    public String[] extraImageUrls;
    public String location;
    public UserInfo owner;

    public boolean isItemInfoComplete() {
        return content != null && owner != null;
    }

    public static ItemInfo obtainInstance(JSONObject jsonObject) {
        ItemInfo item = new ItemInfo();
        try {
            item.id = jsonObject.getString(Settings.JSON_KEY_ITEM_ID);
            if(jsonObject.has(Settings.JSON_KEY_ITEM_TITLE)) {
                item.name = jsonObject.getString(Settings.JSON_KEY_ITEM_TITLE);
            }

            if(jsonObject.has(Settings.JSON_KEY_ITEM_CATEGORY)) {
                item.category = jsonObject.getString(Settings.JSON_KEY_ITEM_CATEGORY);
            }

            if(jsonObject.has(Settings.JSON_KEY_ITEM_CONTENT)) {
                item.content = jsonObject.getString(Settings.JSON_KEY_ITEM_CONTENT);
            }
            if(jsonObject.has(Settings.JSON_KEY_ITEM_PRICE)) {
                item.price = jsonObject.getString(Settings.JSON_KEY_ITEM_PRICE);
            }
            if(jsonObject.has(Settings.JSON_KEY_ITEM_COVER)) {
                String partUrl = jsonObject.getString(Settings.JSON_KEY_ITEM_COVER);
                partUrl.replace("pics\\", "pics");
                item.frontCoverImgUrl = Settings.ROOT_URL + "/" + partUrl;
            }
            if(jsonObject.has(Settings.JSON_KEY_ITEM_EXTRA_IMAGE)) {
                JSONArray urls = jsonObject.getJSONArray(Settings.JSON_KEY_ITEM_EXTRA_IMAGE);
                int length = urls.length();
                item.extraImageUrls = new String[urls.length()];
                for(int i = 0; i < length; i++) {
                    String partUrl = urls.optString(i);
                    partUrl.replace("pics\\", "pics");
                    item.extraImageUrls[i] = Settings.ROOT_URL + "/" + partUrl;
                }
            }
            if(jsonObject.has(Settings.JSON_KEY_ITEM_LOCATION)) {
                item.location = jsonObject.getString(Settings.JSON_KEY_ITEM_LOCATION);
            }
            if(jsonObject.has(Settings.JSON_KEY_ITEM_OWNER)) {
                JSONObject userInfoObject = jsonObject.getJSONObject(Settings.JSON_KEY_ITEM_OWNER);
                item.owner = UserInfo.obtainInstance(userInfoObject);
                if(item.owner == null) {
                    return null;
                }
            }
        } catch (JSONException e) {
            item = null;
            e.printStackTrace();
        }
        return item;
    }

    public static final Parcelable.Creator<ItemInfo> CREATOR = new
            Parcelable.Creator<ItemInfo>() {
                @Override
                public ItemInfo createFromParcel(Parcel source) {
                    ItemInfo info = new ItemInfo();
                    info.id = source.readString();
                    info.name = source.readString();

                    String content = source.readString();
                    info.content = content.equals(UNINIT) ? null : content;
                    info.price = source.readString();
                    info.frontCoverImgUrl = source.readString();

                    int length = source.readInt();
                    if(length == 0) {
                        info.extraImageUrls = null;
                    }
                    else {
                        info.extraImageUrls = new String[length];
                        for(int i = 0; i < length; i ++) {
                            info.extraImageUrls[i] = source.readString();
                        }
                    }

                    info.location = source.readString();
                    if(source.readInt() != 0) {
                        info.owner = source.readParcelable(UserInfo.class.getClassLoader());
                    }
                    else {
                        info.owner = null;
                    }

                    return info;
                }

                @Override
                public ItemInfo[] newArray(int size) {
                    return new ItemInfo[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.content == null ? UNINIT : this.content);
        dest.writeString(this.price);
        dest.writeString(this.frontCoverImgUrl);
        int length = this.extraImageUrls == null ? 0 : this.extraImageUrls.length;
        dest.writeInt(length);
        for(int i = 0; i < length; i++) {
            dest.writeString(extraImageUrls[i]);
        }
        dest.writeString(this.location);
        dest.writeInt(this.owner == null ? 0 : 1);
        dest.writeParcelable(this.owner, 0);
    }
}
