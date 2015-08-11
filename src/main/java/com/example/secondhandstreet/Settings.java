package com.example.secondhandstreet;

/**
 * Created by huangxueqin on 15-4-13.
 */
public class Settings {
    public static final String ROOT_URL = "http://120.26.115.191/seeker";
    public static final String LOGIN_URL = ROOT_URL + "/login.php";
    public static final String REGISTER_URL = ROOT_URL + "/register.php";
    public static final String NEWEST_URL = ROOT_URL + "/search_new.php";
    public static final String RECOMMEND_URL = ROOT_URL + "/search_recommend.php";
    public static final String ITEM_INFO_URL = ROOT_URL + "/search_productid.php";
    public static final String CATEGORY_URL = ROOT_URL + "/search_productclass.php";
    public static final String PUBLISH_URL = ROOT_URL + "/release.php";
    public static final String DISCOVERY_URL = ROOT_URL + "/search_seek.php";
    public static final String UPDATE_USER_INFO_URL = ROOT_URL + "/update_userinfo.php";
    public static final String SEARCH_KEYWORD_URL = ROOT_URL + "/search_producttitle.php";
    public static final String PUBLISH_MESSAGE_URL = ROOT_URL + "/seek.php";
    public static final String GET_PUBLISHED_ITEMS_URL = ROOT_URL + "/search_release_product.php";
    public static final String DELETE_ITEM_URL = ROOT_URL + "/delete_release.php";

//  json key for user info
    public static final String JSON_KEY_USER_ID = "id";
    public static final String JSON_KEY_USER_USERNAME = "name";
    public static final String JSON_KEY_USER_PHONE = "tel";
    public static final String JSON_KEY_USER_QQ = "qq";
    public static final String JSON_KEY_USER_EMAIL = "email";
    public static final String JSON_KEY_USER_AVATAR_IMG = "avatar";

//  json key for item info
    public static final String JSON_KEY_ITEM_ID = "id";
    public static final String JSON_KEY_ITEM_TITLE = "title";
    public static final String JSON_KEY_ITEM_CATEGORY = "class";
    public static final String JSON_KEY_ITEM_CONTENT = "describe";
    public static final String JSON_KEY_ITEM_PRICE = "price";
    public static final String JSON_KEY_ITEM_COVER = "cover";
    public static final String JSON_KEY_ITEM_EXTRA_IMAGE = "other_img";
    public static final String JSON_KEY_ITEM_LOCATION = "location";
    public static final String JSON_KEY_ITEM_OWNER = "userinfo";

    public static final String SP_NAME = "shs_shared_preference";
    public static final String SP_LOGIN_STATE = "login state";
    public static final String SP_USER_ID = "user_id";

//  json key for login and register
    public static final String JSON_KEY_USERNAME = "username";
    public static final String JSON_KEY_PASSWORD = "password";
    public static final String JSON_KEY_PHONE = "tel";
    public static final String JSON_KEY_QQ = "qq";
    public static final String JSON_KEY_ERROR = "error_code";
}
