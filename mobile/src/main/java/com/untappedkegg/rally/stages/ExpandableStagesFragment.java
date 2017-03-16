package com.untappedkegg.rally.stages;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.data.DbUpdated;
import com.untappedkegg.rally.data.NewDataFetcher;
import com.untappedkegg.rally.event.DbEvent;
import com.untappedkegg.rally.ui.ExpandableList;
import com.untappedkegg.rally.util.DateManager;


public final class ExpandableStagesFragment extends ExpandableList implements NewDataFetcher.Callbacks {

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
        loadData();

        DbUpdated.close();
    }

    @Override
    protected Cursor loadCursor() {
//        return DbEvent.stagesSelect(linkPts[5], linkPts[4]);
        return DbEvent.stagesHeaderSelect(linkPts[5], linkPts[4]);
    }

    @Override
    protected SimpleCursorTreeAdapter createCursorAdapter() {
        final String[] groupFrom = new String[]{DbEvent.STAGES_HEADER};
        final int[] groupTo = new int[]{R.id.generic_section_list_header_textview};
        String[] from = new String[]{DbEvent.STAGES_NAME, DbEvent.STAGES_ATC, DbEvent.STAGES_LENGTH, DbEvent.STAGES_NUMBER};
        int[] to = new int[]{R.id.stages_name, R.id.stages_atc, R.id.stages_distance, R.id.stages_id};
        return new StagesTreeCursorAdapter(getActivity(), null, R.layout.generic_section_list_header, groupFrom, groupTo, R.layout.stages_row, from, to);
    }

    @Override
    protected boolean shouldRequery() {
        return StagesFetcher.getInstance().isRunning();
    }

    @Override
    public void onDataFetchComplete(Throwable throwable, String key) {
        try {
            loadData();
        } catch (Exception ignored) { }

        DbUpdated.open();
        DbUpdated.updated_insert(AppState.MOD_STAGES + linkPts[5] + linkPts[4]);
        DbUpdated.close();
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        if (progressBar.isShown()) {
            Toast.makeText(getActivity(), R.string.just_a_moment, Toast.LENGTH_SHORT).show();
        } else {
            final String stageNum = ((TextView) v.findViewById(R.id.stages_id)).getText().toString();

            if (getActivity().findViewById(R.id.second_container) != null ) {
                callbacks.updateStageResults(stageNum);
            } else {
                callbacks.selectStageDetail(link, stageNum);
            }
        }
        return true;
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
