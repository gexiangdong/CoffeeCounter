package com.cvook.coffeecounter;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.UUID;


public class MainActivity extends Activity {
    private final static String STOREDUUID = "uid";
    private final static String ADDACUPURL = "http://cc.cvook.com/addacup";
    private final static String HOMEPAGEURL = "http://cc.cvook.com/cc.html";
    private String homeUrl;
    private WebView webView;

    private long exitTime = 0;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        String version = "1.0";

        String url = HOMEPAGEURL + "?";
        String userId = MainActivity.getUserId(this);
        url += userId;
        homeUrl = url;
        try {
            Uri data = getIntent().getData();
            if (data != null) {
                Log.d("CC", "start from uri:" + data.toString());
                if(data.toString().startsWith(ADDACUPURL)){
                    //ADD A cup (omit the userId in the url parmerters, add a cup to the app user.
                    finish();
                }
            }
        } catch (Exception e) {
            Log.e("CC", "ERROR:", e);
        }
        webView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUserAgentString(webSettings.getUserAgentString() + "[CoffeeCounter APP (" + version + ")]");
        WebViewClient client = new CCWebViewClient();
        WebChromeClient chrome = new CCWebChromeClient();
        webView.setWebViewClient(client);
        webView.setWebChromeClient(chrome);
        webView.addJavascriptInterface(new CoffeeCounterJSBridge(), "CCJSBridge");

        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadData("<html><head><meta http-equiv=\"refresh\" content=\"0; url=" + homeUrl + "\" /></head><body style='background-color:#000'></body></html>", "text/html", "UTF-8");
            }
        });

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if(webView.canGoBack() && !webView.getUrl().equals(homeUrl)) {
                webView.goBack();
            }else{
                if ((System.currentTimeMillis() - exitTime) > 2000) {
                    Toast.makeText(MainActivity.this, getString(R.string.exit_hint), Toast.LENGTH_SHORT).show();
                    exitTime = System.currentTimeMillis();
                } else {
                    finish();
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
            Log.d("CC", "menu Setting clicked.");
            startSettingActivity();
            return true;
        }else if(id == R.id.action_refresh) {
            Log.d("CC", "menu Refresh clicked.");
            reloadHomePage();
        }else if(id == android.R.id.home){
            Log.d("CC", "Title clicked");
            webView.goBack();
        }else{
            Log.d("CC", "menu clicked on " + item.getItemId() + ", " + item.getTitle());
        }
        return super.onOptionsItemSelected(item);
    }

    private void reloadHomePage(){
        webView.clearCache(true);
        webView.loadUrl(homeUrl);
    }

    private void startSettingActivity(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    class CCWebChromeClient extends WebChromeClient{
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            Log.d("CC", "onProgressChanged() " + newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            MainActivity.this.setTitle(title);
            ActionBar bar = getActionBar();
            if(bar != null) {
                if(view.getUrl().equals(homeUrl)) {
                    bar.setDisplayHomeAsUpEnabled(false);
                }else{
                    bar.setDisplayHomeAsUpEnabled(true);
                }
            }
            Log.d("CC", "onReceivedTitle() " + title);
        }
    }

    class CCWebViewClient extends WebViewClient{

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if(url.equals(homeUrl)){
                view.clearHistory();
            }
            Log.d("CC", "onPageFinished() " + url);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            Log.d("CC", "onLoadResource() " + url);
        }

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

    /**
     * Methods in this classed is used for calling in Javascript of the Webview.
     */
    class CoffeeCounterJSBridge {

        /**
         * Show a toast from the web page
         */
        @JavascriptInterface
        public void showToast(final String toast) {
            webView.post(new Runnable() {
                @Override
                public void run() {
                Toast.makeText(MainActivity.this, toast, Toast.LENGTH_SHORT).show();
                }
            });
        }

        /**
         * load the Settings Activity from web page
         */
        @JavascriptInterface
        public void showSettings() {
            webView.post(new Runnable() {
                @Override
                public void run() {
                    startSettingActivity();
                }
            });
        }

        /**
         * refresh the webView from the web page
         */
        @JavascriptInterface
        public void refresh() {
            Log.d("CC", "CoffeeCounterJSBridge:refresh();");
            webView.post(new Runnable() {
                @Override
                public void run() {
                    reloadHomePage();
                }
            });
        }
    }

    public static String getUserId(Context context){
        String userId = PreferenceManager.getDefaultSharedPreferences(context).getString(STOREDUUID, null);
        if(userId == null || userId.length() == 0){
            userId = UUID.randomUUID().toString();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            Editor editor = preferences.edit();
            editor.putString(STOREDUUID, userId);
            editor.commit();
        }
        return userId;
    }
}