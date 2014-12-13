package com.cvook.coffeecounter;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.List;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {
    private final static String STOREDUUID = "uid";
    private String userId;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String url = "http://cc.cvook.com/cc.html?";
        userId = PreferenceManager.getDefaultSharedPreferences(this).getString(STOREDUUID, null);
        if(userId == null || userId.length() == 0){
            userId = UUID.randomUUID().toString();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            Editor editor = preferences.edit();
            editor.putString(STOREDUUID, userId);
            editor.commit();
        }
        url += userId;

        try {
            Uri data = getIntent().getData();
            if (data != null) {
                String scheme = data.getScheme();
                String host = data.getHost();
                List<String> params = data.getPathSegments();
                String first = params.get(0);
                String second = params.get(1);
                // ((TextView) findViewById(R.id.infotext)).setText(scheme +
                // "---" + host + "---" + first + "---" + second);
            }
        } catch (Exception e) {
            Log.e("CC", "ERROR:", e);
        }
        WebView webView = (WebView) findViewById(R.id.webView);
        webView.loadData("<html><body style='background-color:#000'></body></html>", "text/html", "UTF-8");
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        WebViewClient client = new CCWebViewClient();
        webView.setWebViewClient(client);
        webView.addJavascriptInterface(new CoffeeCounterJSBridge(this), "CCJSBridge");

        webView.loadUrl(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }else if(id == R.id.action_refresh){
            WebView webView = (WebView) findViewById(R.id.webView);
            webView.clearCache(true);
            webView.reload();
        }
        return super.onOptionsItemSelected(item);
    }


    class CCWebViewClient extends WebViewClient{

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            if(errorCode == ERROR_HOST_LOOKUP || errorCode == ERROR_CONNECT){
                view.loadUrl("file:///android_asset/error/networkerror.html");
                return;
            }
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

    }
}