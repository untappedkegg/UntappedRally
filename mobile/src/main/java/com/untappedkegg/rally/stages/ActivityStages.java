package com.untappedkegg.rally.stages;

import android.app.SearchManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.event.EventActivity;
import com.untappedkegg.rally.schedule.DbSchedule;

/**
 * Created by UntappedKegg on 1/21/15.
 */
public final class ActivityStages extends EventActivity implements EventStages.Callbacks {

    /*----- INHERITED METHODS -----*/
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }

    // BaseContainer
    @SuppressWarnings("unchecked")
    @Override
    public void selectContent(String uri, String args, String query, int id, boolean addToBackStack) {
            Fragment fragment = new EventStages();

            final String link = DbSchedule.fetchEventRA_link(curId);

                Bundle bundle = new Bundle();
                bundle.putString(AppState.KEY_ARGS, link);

                if (!AppState.isNullOrEmpty(query)) {
                    bundle.putString(SearchManager.QUERY, query);
                } else {
                    bundle.putString(SearchManager.QUERY, "1");
                }
                bundle.putInt(AppState.KEY_ID, id);
                fragment.setArguments(bundle);

            attachFragment(fragment, addToBackStack, ((Object) fragment).getClass().getCanonicalName(), R.id.main_container);
            if (findViewById(R.id.second_container) != null) {
                try {
                    Fragment resultsFragment = new StagesViewPager();
                    Bundle resultsBundle = new Bundle();
                    resultsBundle.putString(AppState.KEY_ARGS, link);
                    resultsBundle.putString(SearchManager.QUERY, "0");
                    resultsBundle.putInt(AppState.KEY_ID, id);

                    resultsFragment.setArguments(resultsBundle);
                    attachFragment(resultsFragment, false, ((Object) resultsFragment).getClass().getCanonicalName(), R.id.second_container);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

    }

    @Override
    protected int getContentLayout() {
        return R.layout.generic_dual_pane_layout;
    }

    @Override
    public void selectStageDetail(final String link, final String stageNo) {
        super.selectContent(StagesViewPager.class.getName(), link, stageNo, 0, true);
    }

    @Override
    public void updateStageResults(final String stageNo) {
        try {
            ((StagesViewPager) getSupportFragmentManager().findFragmentById(R.id.second_container)).updateChildArgs(stageNo);
        } catch (Exception e ) {
            // Not found
        }
    }

}
