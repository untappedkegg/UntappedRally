package com.untappedkegg.rally.stages;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.os.Bundle;
import android.view.View;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.data.NewDataFetcher;
import com.untappedkegg.rally.event.DbEvent;
import com.untappedkegg.rally.schedule.DbSchedule;
import com.untappedkegg.rally.ui.BaseWebView;

public class StagesResults extends BaseWebView implements NewDataFetcher.Callbacks {
    /*----- VARIABLES -----*/
    private short curStage;
    private final StagesFetcher fetcher = StagesFetcher.getInstance();
    private String eventCode;
    private short year;
    private boolean isFinished;

    /**
     * (non-Javadoc)
     *
     * @see com.untappedkegg.rally.ui.BaseWebView#onAttach(android.app.Activity)
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        curStage = Short.parseShort(getArguments().getString(SearchManager.QUERY));
        final String[] linkPts = link.split("/");
        eventCode = linkPts[5];
        year = Short.parseShort(linkPts[4]);
        isFinished = DbSchedule.isEventFinished(eventCode, year);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            curStage = savedInstanceState.getShort("curStage", curStage);
        }

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putShort("curStage", curStage);
        super.onSaveInstanceState(outState);
    }

    /*----- INHERITED METHODS -----*/
    @Override
    protected void fetchData() {
        fetcher.startStageResults(this, getFullLink(), eventCode, curStage, year);

    }

    @Override
    protected void showPage() {
        mWebView.loadUrl("about:blank");
        if (this.shouldRequery()) {
            mWebView.loadData(getResources().getString(R.string.stage_results_format, getResources().getString(R.string.stage_results_loading)), "text/html", "UTF-8");
        } else {
            DbEvent.open();
            mWebView.loadData(DbEvent.fetchStageResults(eventCode, year, curStage), "text/html", "UTF-8");
            DbEvent.close();
        }
    }

    @Override
    protected boolean shouldRequery() {
        return fetcher.isRunning();
    }

    @SuppressLint("NewApi")
    protected void updateArgs(String args, String query) {
        super.updateArgs(args, query);
        this.dataFetched = false;
        curStage = Short.parseShort(query);

        fetchData();
        showPage();
        startRequery();
    }

    @Override
    public void onDataFetchComplete(Throwable throwable, String key) {
        DbEvent.open();
        mWebView.loadData(DbEvent.fetchStageResults(eventCode, year, curStage), "text/html", "UTF-8");
        DbEvent.close();
        this.setProgressBarVisibility(View.GONE);
    }

    /*----- CUSTOM METHODS -----*/
    private String getFullLink() {
        if (!isFinished) {
            return link + String.format(AppState.FUNC_STAGE_TIMES, curStage);
        } else {
            return link + String.format(AppState.FUNC_STAGE_RESULTS, curStage);
        }
    }

}
