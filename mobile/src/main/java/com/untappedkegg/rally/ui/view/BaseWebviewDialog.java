package com.untappedkegg.rally.ui.view;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BaseWebviewDialog#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BaseWebviewDialog extends DialogFragment {
    private String link;
    private int title;
    private ProgressBar progressBar;
    private WebView webView;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BaseWebviewDialog.
     */
    public static BaseWebviewDialog newInstance(final String link, final int title) {
        BaseWebviewDialog fragment = new BaseWebviewDialog();
        Bundle args = new Bundle();
        args.putString(AppState.KEY_URL, link);
        args.putInt(AppState.KEY_ARGS, title);
        fragment.setArguments(args);
        return fragment;
    }

    public BaseWebviewDialog() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            link = getArguments().getString(AppState.KEY_URL);
            title = getArguments().getInt(AppState.KEY_ARGS);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.generic_webview, container);

        webView = (WebView) view.findViewById(R.id.web_view);
        progressBar = (ProgressBar) view.findViewById(R.id.web_view_progress);
        progressBar.setVisibility(View.VISIBLE);

        webView.loadUrl(link);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(getDialog() != null)
        getDialog().setTitle(title);
    }
}
