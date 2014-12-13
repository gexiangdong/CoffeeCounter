package com.cvook.coffeecounter;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class CoffeeCounterJSBridge {
    Context mContext;

    /** Instantiate the interface and set the context */
    CoffeeCounterJSBridge(Context c) {
        mContext = c;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    } 
}