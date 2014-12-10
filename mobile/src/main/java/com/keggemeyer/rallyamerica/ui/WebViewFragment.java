package com.keggemeyer.rallyamerica.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.keggemeyer.rallyamerica.AppState;
import com.keggemeyer.rallyamerica.R;

public class WebViewFragment extends Fragment {

    private WebView mWebView;
    private String url;
    private boolean isRestarting = false;

    /* ----- LIFECYCLE METHODS ----- */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);


        Bundle bundle = getArguments();

        url = bundle.getString(AppState.KEY_URL);
        //			url = "http://wrc.com/fanzone/wrc-live/";
        if (AppState.isNullOrEmpty(url)) {
            url = "";
            Log.d("WebViewFragment", "URL is empty");
        }
        Log.d("WebViewFragment", "URL = " + url);

    }

    @SuppressLint("SetJavaScriptEnabled")
    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater mInflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        RelativeLayout view = (RelativeLayout) mInflater.inflate(R.layout.generic_webview, null);


        mWebView = (WebView) view.findViewById(R.id.web_view);
        WebSettings websettings = mWebView.getSettings();
        // causes slow rendering but is necessary for iRally
        websettings.setJavaScriptEnabled(true);
        //	    		websettings.setLoadWithOverviewMode(true);
        websettings.setUseWideViewPort(true);
        //	    		websettings.setDefaultZoom(websettings.getDefaultZoom());
        if (isRestarting) {
            websettings.setCacheMode(WebSettings.LOAD_CACHE_ONLY);
        } else {
            //	    		websettings.setRenderPriority(RenderPriority.HIGH);
            isRestarting = true;
        }
        //	    		websettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        //	            mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient());

        mWebView.loadUrl(url);

        return view;

    }
}
