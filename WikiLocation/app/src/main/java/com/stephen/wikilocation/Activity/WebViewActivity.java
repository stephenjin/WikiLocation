package com.stephen.wikilocation.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.stephen.wikilocation.R;


public class WebViewActivity  extends Activity{

    private WebView webView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

        webView = (WebView) findViewById(R.id.webView);
        Bundle extras = getIntent().getExtras();
        webView.setWebViewClient(new WebViewClient());

        if(extras != null){
            String url = extras.getString("url");
            if(url != null)
                webView.loadUrl(url);
        }


    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
