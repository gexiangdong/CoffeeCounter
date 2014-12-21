package com.cvook.coffeecounter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;



/**
 * Created by gexiangdong on 14/12/20.
 */
public class Util {
    public static final String JSONKEY = "JSON";
    private static final String URL = "http://cc.cvook.com/cc.php";
    private static Util instance;

    private Util(){

    }



    public void getRemoteJSONObjectAsync(final String action, final String userId, final Map<String, String> params, final Handler handler) {
        Runnable ra = new Runnable(){
            @Override
            public void run(){
                JSONObject json = getRemoteJSONObject(action, userId, params);
                Message msg = new Message();
                Bundle data = new Bundle();
                if(json != null){
                    data.putString(JSONKEY, json.toString());
                }
                msg.setData(data);
                handler.sendMessage(msg);
            }
        };
        new Thread(ra).start();
    }

    /**
     *
     * @param action
     * @param params
     * @return
     */
    public JSONObject getRemoteJSONObject(String action, String userId, Map<String, String> params) {
        HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 300000);
        client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 300000);

        HttpPost request = new HttpPost(URL);
        request.setHeader("User-Agent", "CoffeeCounter(Version 1)");
        HttpResponse response;

        String signature = null;
        String timestamp = String.valueOf((new Date()).getTime());
        String nonce = String.valueOf((new Random()).nextInt(Integer.MAX_VALUE));
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        if(params == null){
            params = new HashMap<String, String>();
        }
        if(params.containsKey("timestamp") || params.containsKey("nonece") ||
                params.containsKey("signature") || params.containsKey("action") ||
                params.containsKey("userid") || params.containsKey("clientVersion")){
            throw new RuntimeException("Invalid parameters, " + params);
        }
        params.put("timestamp", timestamp);
        params.put("userid", userId);
        params.put("action", action);

        String[] sortedKeys = params.keySet().toArray(new String[]{});
        Arrays.sort(sortedKeys, 0, params.size());
        for(int i=0; i<sortedKeys.length; i++){
            String v = params.get(sortedKeys[i]);
            list.add(new BasicNameValuePair(sortedKeys[i], v));
        }

        try {
            request.setEntity(new UrlEncodedFormEntity(list, HTTP.UTF_8));
            response = client.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                try {
                    JSONObject json = new JSONObject(result);
                    if(json.getInt("status") == 1010){
                        //invalid parameter
                    }else if(json.getInt("status") == 1020){
                        //invalid userId
                    }else if(json.getInt("status") == 1030){
                        //invalid clientVersion
                    }else if(json.getInt("status") == 1040){
                        //nonce / duplicated timestamp
                    }else if(json.getInt("status") == 1050){
                        //invalid signature
                    }
                    return json;
                } catch (JSONException e) {
                    Log.e("CC", "JSON ERROR" + URL + "\r\n" + result, e);
                }
            }else{
                Log.e("CC", "JSON ERROR" + URL + "  CODE=" + response.getStatusLine().getStatusCode());
            }
            return null;
        } catch (IOException e) {
            Log.e("CC", "Network error." + URL, e);
            return null;
        } catch(Exception ex){
            Log.e("CC", "Network error." + URL, ex);
            return null;
        }finally{

        }

    }


    public static synchronized Util getInstance(){
        if(instance == null){
            instance = new Util();
        }
        return instance;
    }
}
