package com.untappedkegg.rally.stages;

import android.app.Activity;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.data.DbUpdated;
import com.untappedkegg.rally.data.NewDataFetcher;
import com.untappedkegg.rally.event.DbEvent;
import com.untappedkegg.rally.ui.SectionList;
import com.untappedkegg.rally.util.DateManager;


public class EventStages extends SectionList implements NewDataFetcher.Callbacks {

    /*----- VARIABLES -----*/
    private String link;
    private String[] linkPts;
    private Callbacks callbacks;

    /*----- LIFECYCLE METHODS -----*/
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        link = getArguments().getString(AppState.KEY_ARGS);
        linkPts = link.split("/");

        try {
            callbacks = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement " + Callbacks.class.getSimpleName());
        }
    }

    /*----- INHERITED METHODS -----*/
    @Override
    public void fetchData() {
        DbUpdated.open();
        if (DateManager.timeBetweenInDays(DbUpdated.lastUpdated_by_Source(AppState.MOD_STAGES + linkPts[5] + linkPts[4])) <= AppState.STAND_UPDATE_DELAY) {
            loadList();
        } else {
            loadList();

            StagesFetcher.getInstance().startAll(this, link, linkPts[4]);
            progressBar.setVisibility(View.VISIBLE);

        }
        DbUpdated.close();

    }

    @Override
    protected Cursor loadCursor() {
        return DbEvent.stagesSelect(linkPts[5], linkPts[4]);
    }

    @Override
    protected SimpleCursorAdapter createCursorAdapter() {
        String[] from = new String[]{DbEvent.STAGES_NAME, DbEvent.STAGES_ATC, DbEvent.STAGES_LENGTH, DbEvent.STAGES_NUMBER};
        int[] to = new int[]{R.id.stages_name, R.id.stages_atc, R.id.stages_distance, R.id.stages_id};
        return new SimpleCursorAdapter(getActivity(), R.layout.stages_row, null, from, to, 0);
    }

    @Override
    protected boolean shouldRequeryData() {
        return StagesFetcher.getInstance().isRunning();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (!adapter.isSection(position)) {
            if (progressBar.isShown()) {
                Toast.makeText(getActivity(), R.string.just_a_moment, Toast.LENGTH_SHORT).show();
            } else {
                callbacks.selectStageDetail(link, ((TextView) v.findViewById(R.id.stages_id)).getText().toString());
            }
        }
    }

    @Override
    public void onDataFetchComplete(Throwable throwable, String key) {
        try {
            loadList();
        } catch (Exception e) {
        }
        DbUpdated.open();
        DbUpdated.updated_insert(AppState.MOD_STAGES + linkPts[5] + linkPts[4]);
        DbUpdated.close();
    }

    @Override
    protected String getSectionField() {
        return DbEvent.STAGES_HEADER;
    }

    public interface Callbacks {
        public void selectStageDetail(String link, String stageNo);
    }

    /* (non-Javadoc)
     * @see com.untappedkegg.rally.ui.BaseList#getCustomEmptyText()
     */
    @Override
    protected String getCustomEmptyText() {
        return getResources().getString(R.string.stages_unavailable);
    }

    /* (non-Javadoc)
     * @see com.untappedkegg.rally.ui.BaseList#getContactingEmptyText()
     */
    @Override
    protected String getContactingEmptyText() {
        return getResources().getString(R.string.stages_contacting);
    }


}
