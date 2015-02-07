package com.untappedkegg.rally.event;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.MenuItem;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.schedule.DbSchedule;
import com.untappedkegg.rally.stages.ActivityStages;
import com.untappedkegg.rally.stages.EventStages;
import com.untappedkegg.rally.ui.BaseContainer;
import com.untappedkegg.rally.util.DialogManager;

/**
 * @author UntappedKegg
 */
public class EventActivity extends BaseContainer implements EventDetails.Callbacks {
    protected boolean isEventStarted;

	/*----- LIFECYCLE METHODS -----*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        isEventStarted = !DbSchedule.isEventStarted(getIntent().getExtras().getInt(AppState.KEY_ID));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                final Intent parentIntent = NavUtils.getParentActivityIntent(this);
                if (!NavUtils.shouldUpRecreateTask(this, parentIntent)) {
                    NavUtils.navigateUpTo(this, parentIntent);
//                    this.finish();
                } else {
                    TaskStackBuilder.create(this).addParentStack(this).startActivities();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /* ----- INHERITED METHODS ----- */
    // BaseContainer
    @SuppressWarnings("unchecked")
    @Override
    public void selectContent(String uri, String args, String query, int id, boolean addToBackStack) {
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
            bundle.putInt(AppState.KEY_ID, id);
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

            // If screen is XLarge & Landscape
       /* if (isEventStarted && (getResources().getConfiguration().screenLayout &  Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE &&
        getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

            final View secondContainer = findViewById(R.id.second_container);
        if (secondContainer != null ) {
            secondContainer.setVisibility(View.VISIBLE);
        }
            this.selectContent(EventStages.class.getName(), link);

          final Fragment fragment = new StagesViewPager();

            Bundle bundle = new Bundle();
            bundle.putString(AppState.KEY_ARGS, link);
            bundle.putString(SearchManager.QUERY, "1");
            fragment.setArguments(bundle);
            attachFragment(fragment, false, StagesViewPager.class.getSimpleName(), R.id.second_container);

        } else {
            this.selectContent(EventStages.class.getName(), link);
        }*/
        Intent intent = new Intent(AppState.getApplication(), ActivityStages.class);
        intent.putExtra(AppState.KEY_URI, "");
        intent.putExtra(SearchManager.QUERY, curQuery);
        intent.putExtra(AppState.KEY_ID, curId);
        intent.putExtra(AppState.KEY_URL, link);

        startActivity(intent);
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

