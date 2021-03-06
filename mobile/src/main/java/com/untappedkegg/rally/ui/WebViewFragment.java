package com.untappedkegg.rally.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;

public class WebViewFragment extends Fragment {

    private WebView mWebView;
    private String url;
    private boolean isRestarting = false;

    /* ----- LIFECYCLE METHODS ----- */
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);


        Bundle bundle = getArguments();

        url = bundle.getString(AppState.KEY_URL);
        //			url = "http://wrc.com/fanzone/wrc-live/";
        if (TextUtils.isEmpty(url)) {
            url = "";
            Log.d("WebViewFragment", "URL is empty");
        }
        Log.d("WebViewFragment", "URL = " + url);

    }

    @SuppressLint("SetJavaScriptEnabled")
    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater mInflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        RelativeLayout view = (RelativeLayout) mInflater.inflate(R.layout.generic_webview, null);


        mWebView = (WebView) view.findViewById(R.id.web_view);
        WebSettings websettings = mWebView.getSettings();
        // causes slow rendering but is necessary for iRally
        websettings.setJavaScriptEnabled(true);
        websettings.setUseWideViewPort(true);
        if (isRestarting) {
            websettings.setCacheMode(WebSettings.LOAD_CACHE_ONLY);
        } else {
            isRestarting = true;
        }
        mWebView.setWebChromeClient(new WebChromeClient());

        mWebView.loadUrl(url);

        return view;

    }
}
