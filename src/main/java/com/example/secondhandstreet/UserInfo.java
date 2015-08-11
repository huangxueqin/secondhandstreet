package com.example.secondhandstreet;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by huangxueqin on 15-4-13.
 */
public class UserInfo implements Parcelable{
    public static final String SAMPLE_ID = "000000";
    public static final String UNINIT = "0";


    public String id;
    public String username;
    public String phone;
    public String qq;
    public String email;
    public String avatarImg;

    public UserInfo() {

    }

    public UserInfo(UserInfo info) {
        this.id = info.id;
        this.username = info.username;
        this.phone = info.phone;
        this.qq = info.qq;
        this.email = info.email;
        this.avatarImg = info.avatarImg;
    }

    public static UserInfo obtainInstance(JSONObject jsonObject) {
        UserInfo user = new UserInfo();
        try {
            user.id = jsonObject.getString(Settings.JSON_KEY_USER_ID);
            user.username = jsonObject.getString(Settings.JSON_KEY_USER_USERNAME);
            if(jsonObject.has(Settings.JSON_KEY_USER_PHONE)) {
                user.phone = jsonObject.getString(Settings.JSON_KEY_USER_PHONE);
            }
            if(jsonObject.has(Settings.JSON_KEY_USER_QQ)) {
                user.qq = jsonObject.getString(Settings.JSON_KEY_USER_QQ);
            }
            if(jsonObject.has(Settings.JSON_KEY_USER_EMAIL)) {
                user.email = jsonObject.getString(Settings.JSON_KEY_USER_EMAIL);
            }
            if(jsonObject.has(Settings.JSON_KEY_USER_AVATAR_IMG)) {
                String url = jsonObject.getString(Settings.JSON_KEY_USER_AVATAR_IMG);
                url.replace("avatars\\", "avatars");
                user.avatarImg = Settings.ROOT_URL + "/" + url;
            }
        } catch (JSONException e) {
            user = null;
            e.printStackTrace();
        }
        return user;
    }

    public static final Parcelable.Creator<UserInfo> CREATOR = new
            Parcelable.Creator<UserInfo>() {
                @Override
                public UserInfo createFromParcel(Parcel source) {
                    UserInfo info = new UserInfo();
                    info.id = source.readString();
                    info.username = source.readString();

                    String phone = source.readString();
                    info.phone = phone.equals(UNINIT) ? null : phone;

                    String qq = source.readString();
                    info.qq  = qq.equals(UNINIT) ? null : qq;

                    String email = source.readString();
                    info.email = email.equals(UNINIT) ? null : email;

                    String avatarImg = source.readString();
                    info.avatarImg = avatarImg.equals(UNINIT) ? null : email;
                    return info;
                }

                @Override
                public UserInfo[] newArray(int size) {
                    return new UserInfo[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.username);
        dest.writeString(this.phone == null ? UNINIT : this.phone);
        dest.writeString(this.qq == null ? UNINIT : this.qq);
        dest.writeString(this.email == null ? UNINIT : this.email);
        dest.writeString(this.avatarImg == null ? UNINIT : this.avatarImg);
    }

    public static final UserInfo sample = new UserInfo();
    static {
        sample.id = SAMPLE_ID;
        sample.username = "huangxueqin";
        sample.phone = "13053098264";
        sample.qq = "123456789";
        sample.email = "xhuuanniqege@gmail.com";
        sample.avatarImg = "http://img1.cache.netease.com/catchpic/E/E3/E31C5A84149A77B8B83F06AFF83EE0BB.jpg";
    }
}
