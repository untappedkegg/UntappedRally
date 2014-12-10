package com.keggemeyer.rallyamerica.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.keggemeyer.rallyamerica.AppState;
import com.keggemeyer.rallyamerica.R;

public abstract class BaseWebView extends BaseFragment {

    protected WebView mWebView;
    protected String link;

	 /* ----- LIFECYCLE METHODS ----- */

    /**
     * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        link = getArguments().getString(AppState.KEY_ARGS);
    }

    /**
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.generic_webview, null);

        progressBar = (ProgressBar) view.findViewById(R.id.web_view_progress);
        mWebView = (WebView) view.findViewById(R.id.web_view);
        WebSettings websettings = mWebView.getSettings();
        websettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        websettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        websettings.setBuiltInZoomControls(true);
        websettings.setDisplayZoomControls(false);
        //      websettings.setLoadWithOverviewMode(true);
        //      websettings.setSupportZoom(true);
        //        websettings.setUseWideViewPort(false);
        //        mWebView.setInitialScale(100);
        //        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setBackgroundColor(this.getResources().getColor(R.color.ActionBar));
        mWebView.setInitialScale(this.getScale());

        return view;
    }


    /**
     * @see android.support.v4.app.Fragment#onStart(android.app.Activity)
     */
    @Override
    public void onStart() {
        super.onStart();
        if (dataFetched) {
            showPage();
            setProgressBarVisibility(View.GONE);
        } else if (fetchOnCreate) {
            fetchData();
            dataFetched = true;
            startRequery();
            showPage();
        } else {
            setProgressBarVisibility(View.GONE);
            //			setEmptyText();
            //			finishedLoading();
        }
    }

    protected void updateArgs(String args, String query) {
        this.link = args;
    }

    protected abstract void fetchData();

    protected abstract void showPage();

    protected int getScale() {
        Display display = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        //	    int width = display.getWidth();
        //	    int PIC_WIDTH= mWebView.getRight()-mWebView.getLeft();
        Double val = (double) display.getWidth() / AppState.screenWidth(getActivity());
        val *= 100d;
        return val.intValue();
    }

}
