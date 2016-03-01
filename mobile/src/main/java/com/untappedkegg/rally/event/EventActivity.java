package com.untappedkegg.rally.event;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.view.MenuItem;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.schedule.DbSchedule;
import com.untappedkegg.rally.stages.ActivityStages;
import com.untappedkegg.rally.ui.BaseContainer;
import com.untappedkegg.rally.util.DialogManager;

/**
 * @author UntappedKegg
 */
public class EventActivity extends BaseContainer implements EventDetails.Callbacks {
    protected boolean isEventStarted;
    private boolean shouldRecreate;

	/*----- LIFECYCLE METHODS -----*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        isEventStarted = !DbSchedule.isEventStarted(curId);
        shouldRecreate = getIntent().getBooleanExtra(AppState.KEY_SHOULD_RECREATE, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:

                if ( !shouldRecreate) {
                    NavUtils.navigateUpTo(this, NavUtils.getParentActivityIntent(this));
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
        if (TextUtils.isEmpty(uri)) {
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
        if (!TextUtils.isEmpty(args) || !TextUtils.isEmpty(query)) {
            Bundle bundle = new Bundle();
            if (!TextUtils.isEmpty(args)) bundle.putString(AppState.KEY_ARGS, args);
            if (!TextUtils.isEmpty(query)) bundle.putString(SearchManager.QUERY, query);
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

        Intent intent = new Intent(AppState.getApplication(), ActivityStages.class);
        intent.putExtra(AppState.KEY_URI, "");
        intent.putExtra(SearchManager.QUERY, curQuery);
        intent.putExtra(AppState.KEY_ID, curId);
        intent.putExtra(AppState.KEY_URL, link);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra(AppState.KEY_SHOULD_RECREATE, shouldRecreate);

        startActivity(intent);
    }

    @Override
    protected String getTitleText() {
        return curQuery;
    }

}

