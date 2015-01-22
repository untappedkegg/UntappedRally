package com.untappedkegg.rally.stages;

import android.app.SearchManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.event.EventActivity;
import com.untappedkegg.rally.schedule.DbSchedule;
import com.untappedkegg.rally.util.ScreenUtils;

/**
 * Created by UntappedKegg on 1/21/15.
 */
public class ActivityStages extends EventActivity implements EventStages.Callbacks {

    /*----- VARIABLES -----*/
    private boolean isTablet = false;
//    private String link;

    /*----- LIFECYCLE METHODS -----*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        link = DbSchedule.fetchEventRA_link(curId);
        isTablet = ScreenUtils.isLargeTablet(this);
    }

    /*----- INHERITED METHODS -----*/
    // BaseContainer
    @SuppressWarnings("unchecked")
    @Override
    public void selectContent(String uri, String args, String query, int id, boolean addToBackStack) {
        if (!AppState.isNullOrEmpty(uri)) {
            super.selectContent(uri, args, query, id, addToBackStack);
        } else {

            Fragment fragment = new EventStages();
            Fragment resultsFragment = new StagesViewPager();
            final String link = DbSchedule.fetchEventRA_link(curId);

            // Add args bundle to fragment
//            if (!AppState.isNullOrEmpty(args) || !AppState.isNullOrEmpty(query)) {
                Bundle bundle = new Bundle();
                Bundle resultsBundle = new Bundle();
                bundle.putString(AppState.KEY_ARGS, link);
                resultsBundle.putString(AppState.KEY_ARGS, link);
                if (!AppState.isNullOrEmpty(query)) bundle.putString(SearchManager.QUERY, query);
                resultsBundle.putString(SearchManager.QUERY, "1");
                bundle.putInt(AppState.KEY_ID, id);
                resultsBundle.putInt(AppState.KEY_ID, id);
                fragment.setArguments(bundle);
                resultsFragment.setArguments(resultsBundle);
//            }

            attachFragment(fragment, addToBackStack, ((Object) fragment).getClass().getCanonicalName(), R.id.main_container);
//            if (isEventStarted)
                attachFragment(resultsFragment, false, ((Object) resultsFragment).getClass().getCanonicalName(), R.id.second_container);

            if (/*isEventStarted &&*/ ((getResources().getConfiguration().screenLayout &  Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE &&
                    getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)) {
                findViewById(R.id.second_container).setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    protected int getContentLayout() {
        return R.layout.generic_dual_pane_layout;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        final View secondContainer = findViewById(R.id.second_container);
        if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                secondContainer.setVisibility(View.GONE);
        } else if (isTablet && newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            secondContainer.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void selectStageDetail(String link, String stageNo) {
//        this.selectContent(StagesSelector.class.getName(), link, stageNo);
        this.selectContent(StagesViewPager.class.getName(), link, stageNo);
    }

    @Override
    public void updateStageResults(String stageNo) {
        ((StagesViewPager)getSupportFragmentManager().findFragmentById(R.id.second_container)).updateChildArgs(stageNo);
    }

}
