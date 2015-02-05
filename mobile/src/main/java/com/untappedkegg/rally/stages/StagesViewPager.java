package com.untappedkegg.rally.stages;

import android.app.Activity;
import android.app.SearchManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.data.BaseDbAccessor;
import com.untappedkegg.rally.event.DbEvent;
import com.untappedkegg.rally.schedule.DbSchedule;
import com.untappedkegg.rally.ui.ViewPagerFragment;


public class StagesViewPager extends ViewPagerFragment implements AdapterView.OnItemSelectedListener {

    /*-----VARIABLES -----*/
    private short curStage;
    private String link;
    // eventCode @ 5, year @ 4
    private String[] linkPts;
    private String query;
    private boolean isFinished;
    private Spinner stagesSpinner;

    public StagesViewPager() {
        // Set to true so that the Spinner updates both child Fragments
        updateAllChildren = true;

    }

    /*----- LIFECYCLE METHODS -----*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseDbAccessor.open();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        query = getArguments().getString(SearchManager.QUERY);
        curStage = Short.parseShort(query);
        link = getArguments().getString(AppState.KEY_ARGS);
        linkPts = link.split("/");

        final String[] linkPts = link.split("/");
        isFinished = DbSchedule.isEventFinished(linkPts[5], Short.parseShort(linkPts[4]));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.stages_viewpager, container, false);
        stagesSpinner = (Spinner) v.findViewById(R.id.stages_results_spinner);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            curStage = savedInstanceState.getShort("curStage", curStage);
        }
        query = String.valueOf(curStage);

        setUpSpinner();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putShort("curStage", curStage);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BaseDbAccessor.close();
    }

    /*----- INHERITED METHODS -----*/
    /*-- VIEWPAGERFRAGMENT --*/
    @Override
    protected int getInitialPageNumber() {
        return isFinished ? 1 : 0;
    }

    @Override
    protected Fragment getItems(int position) {
        Bundle bundle = new Bundle();
        bundle.putString(AppState.KEY_ARGS, link);
        bundle.putString(SearchManager.QUERY, query);
        Fragment fragment = new StagesResults();
        switch (position) {
            case 0:
                bundle.putBoolean("isFinished", false);
                break;
            default:
                bundle.putBoolean("isFinished", true);
                break;
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int getNumPages() {
        return 2;
    }

    @Override
    protected CharSequence getPageTitles(int position) {
        switch(position) {
            case 0:
                return getString(R.string.stages_times);
            default:
                return getString(R.string.stages_results);
        }
    }

    /*-- SPINNER --*/
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        this.curStage = (short) (position + 1);
        this.updateArgs(link, String.valueOf(curStage), true);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {  }

    public void updateChildArgs(String stageNo) {
        this.curStage = Short.valueOf(stageNo);
        if (this.stagesSpinner.getCount() == 0) {
            setUpSpinner();
        } else {
            stagesSpinner.setSelection(curStage - 1);
        }
        this.updateArgs(link, stageNo, true);
    }

    /*----- CUSTOM METHODS -----*/
    private void setUpSpinner() {
        final String[] stages = DbEvent.getStageNamesForEvent(linkPts[4], linkPts[5]);

        //Spinners
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, stages);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stagesSpinner.setAdapter(adapter);
        stagesSpinner.setSelection(curStage - 1);
        stagesSpinner.setOnItemSelectedListener(this);

    }


}
