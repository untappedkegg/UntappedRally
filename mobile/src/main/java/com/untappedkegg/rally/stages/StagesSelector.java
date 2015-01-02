package com.untappedkegg.rally.stages;

import android.app.Activity;
import android.app.SearchManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.ImageButton;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.event.DbEvent;
import com.untappedkegg.rally.ui.BaseSelector;

public class StagesSelector extends BaseSelector {
    /*----- VARIABLES -----*/
    private short curStage;
    private short maxStage;
    private String link;
    // eventCode @ 5, year @ 4
    private String[] linkPts;


    /**
     * (non-Javadoc)
     *
     * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        query = getArguments().getString(SearchManager.QUERY);
        curStage = Short.parseShort(query);
        args = link = getArguments().getString(AppState.KEY_ARGS);
        linkPts = link.split("/");
        DbEvent.open();
        maxStage = DbEvent.getMaxStageNumber(linkPts[4], linkPts[5]);
        DbEvent.close();
    }

    /* (non-Javadoc)
     * @see com.untappedkegg.rally.ui.BaseSelector#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            curStage = savedInstanceState.getShort("curStage", curStage);
        }
        query = String.valueOf(curStage);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putShort("curStage", curStage);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected Fragment getDataFragment() {
        return new StagesResults();
    }

    @Override
    protected void onNextButtonClick() {
        if (curStage < maxStage) {
            curStage++;
            this.updateArgs(args, String.valueOf(curStage));
        }

    }

    @Override
    protected void onPreviousButtonClick() {
        if (curStage > 1) {
            curStage--;

            this.updateArgs(args, String.valueOf(curStage));
        }

    }

    @Override
    protected String getSelectorTitleText() {
        return DbEvent.getStageName(linkPts[4], linkPts[5], curStage);
    }

    @Override
    protected void disableButtons() {
        query = String.valueOf(curStage);
        final ImageButton prevBtn = (ImageButton) getActivity().findViewById(getPreviousButtonId());
        final ImageButton nextBtn = (ImageButton) getActivity().findViewById(getNextButtonId());
        if (curStage <= 1) {
            prevBtn.setEnabled(false);
        } else if (curStage == maxStage) {
            nextBtn.setEnabled(false);
        } else {
            prevBtn.setEnabled(true);
            nextBtn.setEnabled(true);
        }

    }

    @Override
    protected boolean shouldRequery() {
        return false;
    }

}
