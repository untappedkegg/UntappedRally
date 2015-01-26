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

/**
 * Created by UntappedKegg on 1/21/15.
 */
public class ActivityStages extends EventActivity implements EventStages.Callbacks {

    /*----- VARIABLES -----*/
    private boolean isTablet = false;
//    private String link;

//    /*----- LIFECYCLE METHODS -----*/
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        link = DbSchedule.fetchEventRA_link(curId);
//        isTablet = ScreenUtils.isLargeTablet(this);
//    }

    /*----- INHERITED METHODS -----*/
    // BaseContainer
    @SuppressWarnings("unchecked")
    @Override
    public void selectContent(String uri, String args, String query, int id, boolean addToBackStack) {
//        if (!AppState.isNullOrEmpty(uri) && !restarting) {
//            super.selectContent(uri, args, query, id, addToBackStack);
//            Log.w(LOG_TAG, "Calling super.SelectContent(..)");
//        } else {

            Fragment fragment = new EventStages();

            final String link = DbSchedule.fetchEventRA_link(curId);

            // Add args bundle to fragment
//            if (!AppState.isNullOrEmpty(args) || !AppState.isNullOrEmpty(query)) {
                Bundle bundle = new Bundle();
                bundle.putString(AppState.KEY_ARGS, link);

                if (!AppState.isNullOrEmpty(query)) bundle.putString(SearchManager.QUERY, query);
                bundle.putInt(AppState.KEY_ID, id);
                fragment.setArguments(bundle);

//            }

            attachFragment(fragment, addToBackStack, ((Object) fragment).getClass().getCanonicalName(), R.id.main_container);
            if (findViewById(R.id.second_container) != null) {
                try {
                    Fragment resultsFragment = new StagesViewPager();
                    Bundle resultsBundle = new Bundle();
                    resultsBundle.putString(AppState.KEY_ARGS, link);
                    resultsBundle.putString(SearchManager.QUERY, "1");
                    resultsBundle.putInt(AppState.KEY_ID, id);

                    resultsFragment.setArguments(resultsBundle);
                    attachFragment(resultsFragment, false, ((Object) resultsFragment).getClass().getCanonicalName(), R.id.second_container);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

//            if (/*isEventStarted &&*/ ((getResources().getConfiguration().screenLayout &  Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE &&
//                    getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)) {
//                findViewById(R.id.second_container).setVisibility(View.VISIBLE);
//            }
//        }

    }

    @Override
    protected int getContentLayout() {
        return R.layout.generic_dual_pane_layout;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        final View secondContainer = findViewById(R.id.second_container);
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE && secondContainer != null) {
//            getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentById(R.id.main_container)).commit();

            Fragment fragment = new EventStages();

            final String link = DbSchedule.fetchEventRA_link(curId);

            // Add args bundle to fragment
//            if (!AppState.isNullOrEmpty(args) || !AppState.isNullOrEmpty(query)) {
            Bundle bundle = new Bundle();
            bundle.putString(AppState.KEY_ARGS, link);

            if (!AppState.isNullOrEmpty(curQuery)) bundle.putString(SearchManager.QUERY, curQuery);
            bundle.putInt(AppState.KEY_ID, curId);
            fragment.setArguments(bundle);

//            }

            attachFragment(fragment, false, ((Object) fragment).getClass().getCanonicalName(), R.id.main_container);

//                secondContainer.setVisibility(View.GONE);
//        } else if (isTablet && newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            secondContainer.setVisibility(View.VISIBLE);
        }
//
    }

    @Override
    public void selectStageDetail(String link, String stageNo) {
//        this.selectContent(StagesSelector.class.getName(), link, stageNo);
        super.selectContent(StagesViewPager.class.getName(), link, stageNo, 0, true);
    }

    @Override
    public void updateStageResults(String stageNo) {
        try {
            ((StagesViewPager) getSupportFragmentManager().findFragmentById(R.id.second_container)).updateChildArgs(stageNo);
        } catch (Exception e ) {
            // Not found
        }
    }

}
