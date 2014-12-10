package com.keggemeyer.rallyamerica.radio;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.keggemeyer.rallyamerica.AppState;
import com.keggemeyer.rallyamerica.R;
import com.keggemeyer.rallyamerica.data.DataFetcher;
import com.keggemeyer.rallyamerica.data.DbUpdated;
import com.keggemeyer.rallyamerica.util.DateManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

// URL = www.wrc.com/live-ticker/live_popup_text.html
public class LiveTextFragment extends Fragment implements DataFetcher.Callbacks {
    private WebView mWebView;
    private boolean isRestarting = false;
    private ProgressBar progressBar;
    //	 private static boolean fileNotFound = false;
    //	 private DataFetcher dataFetch;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater mInflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        RelativeLayout view = (RelativeLayout) mInflater.inflate(R.layout.generic_webview, null);


        mWebView = (WebView) view.findViewById(R.id.web_view);
        WebSettings websettings = mWebView.getSettings();
        //	            websettings.setLoadWithOverviewMode(true);
        websettings.setUseWideViewPort(true);
        mWebView.setInitialScale(100);

        mWebView.setWebChromeClient(new WebChromeClient());

        File file = getActivity().getFileStreamPath(AppState.FUNC_RA_STAND);
        if (file.exists() == true) {
            try {
                InputStream inputStream = getActivity().openFileInput(AppState.FUNC_RA_STAND);
                //	    		        InputStream iS = getActivity().openFileInput(AppState.FUNC_WRC_STAND);
                mWebView.loadData(/*DataFetcher.readStream(iS) +*/ DataFetcher.readStream(inputStream), "text/html", "UTF-8");

            } catch (FileNotFoundException e) {
                //	    		    	fileNotFound = true;
                //	    		       Logger.i(LOG_TAG, "File not found " + e.toString());
            } catch (IOException e) {
                //	    		    	fileNotFound = true;
                //	    		       Logger.i(LOG_TAG, "Can not read file: " + e.toString());
            }

        } else {
            //	    				DataFetcher.getInstance().standings_start(this);
        }


        return view;

    }

    //	@Override
    //	public void onAttach(Activity activity) {
    //		super.onAttach(activity);
    //
    //		try {
    ////			callbacks = (Callbacks) activity;
    //		} catch(ClassCastException e) {
    //			throw new ClassCastException(activity.toString() + " must implement " + this.getClass().getCanonicalName() + ".Callbacks");
    //		}
    //	}
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        progressBar = (ProgressBar) getActivity().findViewById(R.id.web_view_progress);
        if (DataFetcher.getInstance().standings_isRunning()) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            DbUpdated.open();
            if (DateManager.timeBetweenInDays(DbUpdated.lastUpdated_by_Source(AppState.MOD_STAND)) >= AppState.STAND_UPDATE_DELAY) {
                //			progressBar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
                progressBar.setVisibility(View.VISIBLE);
                //				DataFetcher.getInstance().standings_start(this);
                DbUpdated.close();
            }
        }
    }

    @Override
    public void onDataFetchComplete(Throwable throwable, String parser) {
        if (parser.equalsIgnoreCase(AppState.FUNC_RA_STAND) /*|| parser.equalsIgnoreCase(AppState.FUNC_WRC_STAND)*/) {
            progressBar.setVisibility(View.GONE);

            File file = getActivity().getFileStreamPath(AppState.FUNC_RA_STAND);
            if (file.exists() == true) {
                try {

                    InputStream inputStream = getActivity().openFileInput(AppState.FUNC_RA_STAND);
                    //   		        InputStream iS = getActivity().openFileInput(AppState.FUNC_WRC_STAND);
                    mWebView.loadData(/*DataFetcher.readStream(iS) +*/ DataFetcher.readStream(inputStream), "text/html", "UTF-8");
                    //   		        fileNotFound = false;
                } catch (FileNotFoundException e) {
                    //   		       Logger.i(LOG_TAG, "File not found " + e.toString());
                } catch (IOException e) {
                    //   		       Logger.i(LOG_TAG, "Can not read file: " + e.toString());
                }
                DbUpdated.open();
                DbUpdated.updated_insert(AppState.MOD_STAND);
                DbUpdated.close();

            }
        }

    }
}
