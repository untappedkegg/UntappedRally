package com.untappedkegg.rally.stages;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.data.DbUpdated;
import com.untappedkegg.rally.data.NewDataFetcher;
import com.untappedkegg.rally.event.DbEvent;
import com.untappedkegg.rally.ui.SectionList;
import com.untappedkegg.rally.util.DateManager;


public final class EventStages extends SectionList implements NewDataFetcher.Callbacks {

    /*----- VARIABLES -----*/
    private String link;
    private String[] linkPts;
    private Callbacks callbacks;

    /*----- LIFECYCLE METHODS -----*/
    @Override
    public void onAttach(Context activity) {
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

    @Override
    public void onResume() {
        super.onResume();
        Tracker mTracker = AppState.getDefaultTracker();
        mTracker.setScreenName("Stages");
        mTracker.setPage(linkPts[5] + " " + linkPts[4]);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    /*----- INHERITED METHODS -----*/

    @Override
    public void fetchData() {
        DbUpdated.open();
        if (DateManager.timeBetweenInDays(DbUpdated.lastUpdated_by_Source(AppState.MOD_STAGES + linkPts[5] + linkPts[4])) > AppState.STAND_UPDATE_DELAY) {
            StagesFetcher.getInstance().startAll(this, link, linkPts[4]);
            progressBar.setVisibility(View.VISIBLE);
        }
        loadList();

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
    protected SimpleCursorAdapter.ViewBinder getViewBinder() {
        return super.getViewBinder();
    }

    @Override
    protected boolean shouldRequery() {
        return StagesFetcher.getInstance().isRunning();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (!adapter.isSection(position)) {
            if (progressBar.isShown()) {
                Toast.makeText(getActivity(), R.string.just_a_moment, Toast.LENGTH_SHORT).show();
            } else {
                    final String stageId = ((TextView) v.findViewById(R.id.stages_id)).getText().toString();

                    if (getActivity().findViewById(R.id.second_container) != null ) {
                        callbacks.updateStageResults(stageId);
                    } else {
                        callbacks.selectStageDetail(link, stageId);
                    }
                }
            }

    }

    @Override
    public void onDataFetchComplete(Throwable throwable, String key) {
        try {
            loadList();
        } catch (Exception ignored) { }

        DbUpdated.open();
        DbUpdated.updated_insert(AppState.MOD_STAGES + linkPts[5] + linkPts[4]);
        DbUpdated.close();
    }

    @Override
    protected String getSectionField() {
        return DbEvent.STAGES_HEADER;
    }

    public interface Callbacks {
        void selectStageDetail(String link, String stageNo);
        void updateStageResults(String stageNo);
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
