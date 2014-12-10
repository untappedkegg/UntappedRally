package com.keggemeyer.rallyamerica.stages;

import android.app.Activity;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.keggemeyer.rallyamerica.AppState;
import com.keggemeyer.rallyamerica.R;
import com.keggemeyer.rallyamerica.data.DbUpdated;
import com.keggemeyer.rallyamerica.data.NewDataFetcher;
import com.keggemeyer.rallyamerica.event.DbEvent;
import com.keggemeyer.rallyamerica.ui.BaseContainer;
import com.keggemeyer.rallyamerica.ui.SectionList;
import com.keggemeyer.rallyamerica.util.DateManager;


public class EventStages extends SectionList implements NewDataFetcher.Callbacks {

    private String link;
    private String[] linkPts;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        link = getArguments().getString(AppState.KEY_ARGS);
        linkPts = link.split("/");
    }

    @Override
    public void fetchData() {
        DbUpdated.open();
        if (DateManager.timeBetweenInDays(DbUpdated.lastUpdated_by_Source(AppState.MOD_STAGES + linkPts[5] + linkPts[4])) <= AppState.STAND_UPDATE_DELAY) {
            loadList();
        } else {
            //			if (isHomeFragment)
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
                ((BaseContainer) getActivity()).selectContent(StagesSelector.class.getName(), link, ((TextView) v.findViewById(R.id.stages_id)).getText().toString());
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
     * @see com.keggemeyer.rallyamerica.ui.BaseList#getCustomEmptyText()
     */
    @Override
    protected String getCustomEmptyText() {
        return getResources().getString(R.string.stages_unavailable);
    }

    /* (non-Javadoc)
     * @see com.keggemeyer.rallyamerica.ui.BaseList#getContactingEmptyText()
     */
    @Override
    protected String getContactingEmptyText() {
        return getResources().getString(R.string.stages_contacting);
    }


}
