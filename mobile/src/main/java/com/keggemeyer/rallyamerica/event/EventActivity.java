package com.keggemeyer.rallyamerica.event;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.MenuItem;

import com.keggemeyer.rallyamerica.AppState;
import com.keggemeyer.rallyamerica.R;
import com.keggemeyer.rallyamerica.stages.EventStages;
import com.keggemeyer.rallyamerica.stages.StagesSelector;
import com.keggemeyer.rallyamerica.ui.BaseContainer;
import com.keggemeyer.rallyamerica.util.DialogManager;

/**
 * @author Kyle
 */
public class EventActivity extends BaseContainer implements EventDetails.Callbacks, EventStages.Callbacks {

	/*----- LIFECYCLE METHODS -----*/
    //	@Override
    //	public void onWindowFocusChanged(boolean hasFocus) {
    //	        super.onWindowFocusChanged(hasFocus);
    //	    if (hasFocus ) {
    //	    	AppState.hideSystemUI(this);
    //	    }
    //	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                final Intent parentIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, parentIntent)) {
                    NavUtils.navigateUpTo(this, parentIntent);
                } else {
                    TaskStackBuilder.create(this).addParentStack(this).startActivities();
                }

//                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /* ----- INHERITED METHODS ----- */
    // BaseContainer
    @SuppressWarnings("unchecked")
    @Override
    public void selectContent(String uri, String args, String query, boolean addToBackStack) {
        Fragment fragment;

        // default fragment
        if (AppState.isNullOrEmpty(uri)) {
            uri = EventDetails.class.getName();
        }

        // Select appropriate fragment
        try {
            Class<Fragment> fragmentClass = (Class<Fragment>) Class.forName(uri);
            fragment = fragmentClass.newInstance();
        } catch (Exception e) { // ClassNotFound, IllegalAccess, etc.
            DialogManager.raiseUIError(this, e, false);
            e.printStackTrace();
            return;
        }

        // Add args bundle to fragment
        if (!AppState.isNullOrEmpty(args) || !AppState.isNullOrEmpty(query)) {
            Bundle bundle = new Bundle();
            if (!AppState.isNullOrEmpty(args)) bundle.putString(AppState.KEY_ARGS, args);
            if (!AppState.isNullOrEmpty(query)) bundle.putString(SearchManager.QUERY, query);
            fragment.setArguments(bundle);
        }

        attachFragment(fragment, addToBackStack, ((Object) fragment).getClass().getCanonicalName(), R.id.main_container);

    }

    @Override
    public void selectPhotos(String link) {
        this.selectContent(EventPhotos.class.getName(), link);

    }

    @Override
    public void selectStages(String link) {
        this.selectContent(EventStages.class.getName(), link);

    }

    @Override
    public void selectStageDetail(String link, String stageNo) {
        this.selectContent(StagesSelector.class.getName(), link, stageNo);
    }

    @Override
    public void selectResults(String link) {
        this.selectContent(EventStages.class.getName(), link);
    }

    @Override
    protected String getTitleText() {
        return curQuery;
    }

}
