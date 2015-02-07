package com.untappedkegg.rally.schedule;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.data.DataFetcher;
import com.untappedkegg.rally.event.EventDetails;
import com.untappedkegg.rally.interfaces.Refreshable;
import com.untappedkegg.rally.interfaces.ScheduleItemClickReceiver;
import com.untappedkegg.rally.ui.SectionList;

public final class ScheduleFragment extends SectionList implements DataFetcher.Callbacks, Refreshable, AdapterView.OnItemLongClickListener {

    private ScheduleItemClickReceiver callback;
    private boolean isHomeFragment;

    public ScheduleFragment() {
    }

    /* ----- LIFECYCLE METHODS ----- */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            isHomeFragment = getArguments().getBoolean("isHomeFragment");
        } catch (NullPointerException e) {
            isHomeFragment = false;
        }
        try {
            callback = (ScheduleItemClickReceiver) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement " + ScheduleItemClickReceiver.class.getSimpleName());
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            isHomeFragment = savedInstanceState.getBoolean("isHomeFragment");
        }
    }

        @Override
    	public void onActivityCreated(Bundle savedState) {
    	    super.onActivityCreated(savedState);

    	    getListView().setOnItemLongClickListener(this);
    	}


    /* ----- INHERITED METHODS ----- */
    //	Fragment
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("isHomeFragment", isHomeFragment);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void fetchData() {
        DataFetcher.getInstance().sched_start(this, false);
    }

    @Override
    public void refreshData() {
        DataFetcher.getInstance().sched_start(this, true);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected Cursor loadCursor() {
        if (isHomeFragment) {
            return DbSchedule.fetchUpcoming();
        } else {
            return DbSchedule.fetch();
        }
    }

    @Override
    protected SimpleCursorAdapter createCursorAdapter() {
        String[] from = new String[]{DbSchedule.SCHED_ID, DbSchedule.SCHED_FROM_TO, DbSchedule.SCHED_SHORT_CODE, DbSchedule.SCHED_EVT_SITE};
        int[] to = new int[]{R.id.sched_id, R.id.sched_date, R.id.sched_icon, R.id.sched_website};
        return new ScheduleCursorAdaptor(getActivity(), R.layout.schedule_row, null, from, to, 0, this);
    }

    @Override
    protected boolean shouldRequeryData() {
        return DataFetcher.getInstance().sched_isRunning();
    }

    @Override
    protected String getSectionField() {
        if (isHomeFragment) return null;
        return DbSchedule.SCHED_YEAR_ACTUAL;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (!adapter.isSection(position)) {
            final int eventId = Integer.parseInt(((TextView) v.findViewById(R.id.sched_id)).getText().toString());
            final String eventName = ((TextView) v.findViewById(R.id.sched_title)).getText().toString();
            callback.showEventDetail(EventDetails.class.getName(), eventName, eventId);
        }
        // call back for dialog fragment which gives the option to choose from stages, stage times, itinerary

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if(!adapter.isSection(position)) {
            view.findViewById(R.id.sched_menu_btn).callOnClick();
            return true;
        }
        return false;
    }

    @Override
    public void onDataFetchComplete(Throwable throwable, String parser) {
        if (parser.equals(AppState.MOD_SCHED)) {
            if (!this.isDetached() && this.isVisible()) {
                loadList();
            }
        }
    }

}
