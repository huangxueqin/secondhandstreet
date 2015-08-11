package com.example.secondhandstreet.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import com.example.secondhandstreet.Settings;
import com.example.secondhandstreet.UserInfo;
import com.example.secondhandstreet.Utils.LogUtil;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by huangxueqin on 15-4-15.
 * 该类包含系统访问网络所包含的工具，包括
 * 1. 检查系统的网络连接状况
 * 2. 发送Http Get和Post请求并将返回数据打包成JSON返回
 * 3. 注册BroadcastReceiver检测系统网络的变化，并作出处理
 */
public class NetworkUtils {
    public static final int NET_STATE_MOBILE = 100;
    public static final int NET_STATE_WIFI = 101;
    public static final int NET_STATE_OFFLINE = 102;

    private static final int REQUEST_TIME_OUT = 5000;
    private static final String lineEnd = "\r\n";
    private static final String twoHyphens = "--";
    private static final String boundary = "*****";


    public static int getNetWorkState(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if(networkInfo != null) {
            if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return NET_STATE_WIFI;
            }
            if(networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                return NET_STATE_MOBILE;
            }
        }
        return NET_STATE_OFFLINE;
    }

    public static DefaultHttpClient getThreadSafeHttpClient () {
        DefaultHttpClient client = new DefaultHttpClient();
        ClientConnectionManager mgr = client.getConnectionManager();
        HttpParams params = client.getParams();
        client = new DefaultHttpClient(
                new ThreadSafeClientConnManager(params,
                        mgr.getSchemeRegistry()), params);
        return client;
    }


    public static JSONObject uploadItemInfo(String urlStr, UserInfo userInfo,
                                            String title, String price, String location, String content, String category,
                                            List<String> files, int coverIndex) {
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();

            // Allow Inputs & Outputs
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            // Enable POST method and timeout
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(REQUEST_TIME_OUT*(files.size()+1));

            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);

            outputStream = new DataOutputStream(connection.getOutputStream());

            // write userid
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream
                    .writeBytes("Content-Disposition: form-data; name=\"userid\""
                            + lineEnd);
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(userInfo.id);
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);

            // write title
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream
                    .writeBytes("Content-Disposition: form-data; name=\"title\""
                            + lineEnd);
            outputStream.writeBytes(lineEnd);
            outputStream.write(title.getBytes("utf-8"));
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);

            // write category
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream
                    .writeBytes("Content-Disposition: form-data; name=\"class\""
                            + lineEnd);
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(category);
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);

            // write price
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream
                    .writeBytes("Content-Disposition: form-data; name=\"price\""
                            + lineEnd);
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(price);
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);

            // write location
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream
                    .writeBytes("Content-Disposition: form-data; name=\"location\""
                            + lineEnd);
            outputStream.writeBytes(lineEnd);
            outputStream.write(location.getBytes("utf-8"));
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);

            // write content
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream
                    .writeBytes("Content-Disposition: form-data; name=\"describe\""
                            + lineEnd);
            outputStream.writeBytes(lineEnd);
            outputStream.write(content.getBytes("utf-8"));
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);

            int count = 1;
            for(int i = 0; i < files.size(); i++) {
                String filename = files.get(i);
                String keyname = i == coverIndex ? "\"cover\"" : "\"pic" + (count++) + "\"";
                FileInputStream fileInputStream = new FileInputStream(filename);
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream
                        .writeBytes("Content-Disposition: form-data; name=" + keyname + ";filename=\""
                                + filename + "\"" + lineEnd);
                outputStream.writeBytes(lineEnd);

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // Read file
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(twoHyphens + boundary + twoHyphens
                        + lineEnd);

                fileInputStream.close();
            }

            // int serverResponseCode = connection.getResponseCode();
            String serverResponseMessage = connection.getResponseMessage();
            LogUtil.logd(" Server Response" + serverResponseMessage);
            outputStream.flush();

            InputStream is = connection.getInputStream();
            // retrieve the response from server
            int ch;
            StringBuffer b = new StringBuffer();
            while ((ch = is.read()) != -1) {
                b.append((char) ch);
            }
            String jsonString = b.toString();
            LogUtil.logd("response = " + jsonString);
            jsonString = jsonString.substring(jsonString.indexOf('{'), jsonString.length());
            JSONObject result = new JSONObject(jsonString);
            outputStream.close();
            return result;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject uploadUserInfoInfo(String urlStr, UserInfo userInfo, String password, Bitmap avatar) {
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();

            // Allow Inputs & Outputs
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            // Enable POST method
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(REQUEST_TIME_OUT*2);

            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);

            outputStream = new DataOutputStream(connection.getOutputStream());

            // write userid
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream
                    .writeBytes("Content-Disposition: form-data; name=\"userid\""
                            + lineEnd);
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(userInfo.id);
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);

            // write username
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream
                    .writeBytes("Content-Disposition: form-data; name=\"username\""
                            + lineEnd);
            outputStream.writeBytes(lineEnd);
            outputStream.write(userInfo.username.getBytes("utf-8"));
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);

            if(password != null) {
                // write password
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream
                        .writeBytes("Content-Disposition: form-data; name=\"password\""
                                + lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(password);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            }

            // write phone
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream
                    .writeBytes("Content-Disposition: form-data; name=\"tel\""
                            + lineEnd);
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(userInfo.phone);
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);

            // write qq
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream
                    .writeBytes("Content-Disposition: form-data; name=\"qq\""
                            + lineEnd);
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(userInfo.qq);
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);

            // write avatar
            if(avatar != null) {
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream
                        .writeBytes("Content-Disposition: form-data; name=\"avatar\"" + ";filename=\""
                                + "avatar.jpg" + "\"" + lineEnd);
                outputStream.writeBytes(lineEnd);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                avatar.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                outputStream.write(byteArray, 0, byteArray.length);

                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(twoHyphens + boundary + twoHyphens
                        + lineEnd);
            }

            // int serverResponseCode = connection.getResponseCode();
            String serverResponseMessage = connection.getResponseMessage();
            LogUtil.logd(" Server Response" + serverResponseMessage);
            outputStream.flush();

            InputStream is = connection.getInputStream();
            // retrieve the response from server
            int ch;
            StringBuffer b = new StringBuffer();
            while ((ch = is.read()) != -1) {
                b.append((char) ch);
            }
            String jsonString = b.toString();
            LogUtil.logd("response = " + jsonString);
            jsonString = jsonString.substring(jsonString.indexOf('{'), jsonString.length());
            JSONObject result = new JSONObject(jsonString);
            outputStream.close();
            return result;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject getResponseByHttpPost(String url, JSONObject data) {
        JSONObject result = null;
        HttpClient client = getThreadSafeHttpClient();
        HttpConnectionParams.setConnectionTimeout(client.getParams(), REQUEST_TIME_OUT);
        HttpConnectionParams.setSoTimeout(client.getParams(), REQUEST_TIME_OUT);
        try {
            LogUtil.logd("post url = " + url);
            HttpPost post = new HttpPost(url);
            StringEntity entity = new StringEntity(data.toString(), "UTF-8");
            entity.setContentType("application/json;charset=UTF-8");
            entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
            post.setEntity(entity);
            final HttpResponse response = client.execute(post);
            if(response != null) {
                String jsonString = EntityUtils.toString(response.getEntity());
                LogUtil.logd("response = " + jsonString);
                result = new JSONObject(jsonString);
            }
            else {
                LogUtil.logd("no response");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            client.getConnectionManager().shutdown();
        }
        return result;
    }


    public static JSONObject getResponseByHttpGet(String url) {
        JSONObject result = null;
        HttpClient client = getThreadSafeHttpClient();
        HttpConnectionParams.setConnectionTimeout(client.getParams(), REQUEST_TIME_OUT);
        HttpConnectionParams.setSoTimeout(client.getParams(), REQUEST_TIME_OUT);
        HttpGet request = new HttpGet();
        try {
            LogUtil.logd("get url = " + url);
            URI getUrl = new URI(url);
            request.setURI(getUrl);
            HttpResponse response = client.execute(request);
            if(response != null) {
                String jsonString = EntityUtils.toString(response.getEntity());
                LogUtil.logd("response = " + jsonString);
                result = new JSONObject(jsonString);
            }
            else {
                LogUtil.logd("no response");
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        client.getConnectionManager().shutdown();
        return result;
    }

    private static boolean checkResponse(JSONObject jsonObject) {
        if(jsonObject == null || jsonObject.has(Settings.JSON_KEY_ERROR)) {
            return false;
        }
        return true;
    }


    public interface NetworkChangeCallback {
        void onNetworkConnected();
        void onNetworkBreak();
    }

    private static List<NetworkChangeCallback> sNetworkChangeCallbacks = new ArrayList<>();

    public static void registerForNetworkChange(NetworkChangeCallback callback) {
        for(NetworkChangeCallback nc : sNetworkChangeCallbacks) {
            if(callback == nc) {
                return;
            }
        }
        sNetworkChangeCallbacks.add(callback);
    }

    public static void unRegisterForNetworkChange(NetworkChangeCallback callback) {
        for(NetworkChangeCallback nc : sNetworkChangeCallbacks) {
            if(callback == nc) {
                sNetworkChangeCallbacks.remove(callback);
                return;
            }
        }
    }




    public static class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(NetworkUtils.getNetWorkState(context) == NET_STATE_OFFLINE) {
                for(NetworkChangeCallback callback : sNetworkChangeCallbacks) {
                    callback.onNetworkBreak();
                }
            }
            else {
                for(NetworkChangeCallback callback : sNetworkChangeCallbacks) {
                    callback.onNetworkConnected();
                }
            }
        }
    }
}
