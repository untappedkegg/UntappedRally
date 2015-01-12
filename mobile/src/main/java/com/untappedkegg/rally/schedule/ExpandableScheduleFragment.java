package com.untappedkegg.rally.schedule;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.data.DataFetcher;
import com.untappedkegg.rally.event.EventDetails;
import com.untappedkegg.rally.interfaces.Refreshable;
import com.untappedkegg.rally.ui.ExpandableList;

public class ExpandableScheduleFragment extends ExpandableList implements DataFetcher.Callbacks, /*OnClickListener,*/ AdapterView.OnItemLongClickListener, Refreshable/*, OnMenuItemClickListener*/ {

    private Callbacks callback;
    private boolean isHomeFragment;
    private View menuView = null;

    public ExpandableScheduleFragment() {
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
            callback = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ScheduleFragment.Callbacks.");
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            isHomeFragment = savedInstanceState.getBoolean("isHomeFragment");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView.setOnItemLongClickListener(this);
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
            return DbSchedule.fetchSections();
        }

    }

    @Override
    protected SimpleCursorTreeAdapter createCursorAdapter() {
        final String[] groupFrom = new String[]{DbSchedule.SCHED_YEAR_ACTUAL};
        final int[] groupTo = new int[]{R.id.generic_section_list_header_textview};
        String[] from = new String[]{DbSchedule.SCHED_ID, DbSchedule.SCHED_FROM_TO, DbSchedule.SCHED_SHORT_CODE, DbSchedule.SCHED_EVT_SITE};
        int[] to = new int[]{R.id.sched_id, R.id.sched_date, R.id.sched_icon, R.id.sched_website};
        return new ScheduleTreeCursorAdapter(getActivity(), null, R.layout.generic_section_list_header, groupFrom, groupTo, R.layout.schedule_row, from, to);
    }


    @Override
    protected boolean shouldRequery() {
        return DataFetcher.getInstance().sched_isRunning();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        final int eventId = Integer.parseInt(((TextView) v.findViewById(R.id.sched_id)).getText().toString());
        final String eventName = ((TextView) v.findViewById(R.id.sched_title)).getText().toString();
        callback.showEventDetail(EventDetails.class.getName(), eventName, eventId);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if(!"header".equals(view.getTag())) {
            view.findViewById(R.id.sched_menu_btn).callOnClick();
            return true;
        }
        return false;
    }

    @Override
    public void onDataFetchComplete(Throwable throwable, String parser) {

        if (parser.equals(AppState.MOD_SCHED)) {
            if (!this.isDetached() && this.isVisible()) {
                loadData();
            }
            finishedLoading();
        }
    }


    /* ----- NESTED INTERFACES ----- */
    public interface Callbacks {
        public void showEventDetail(String fragment, String eventName, int id);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        final int eventId = Integer.parseInt(((TextView) v.findViewById(R.id.sched_id)).getText().toString());
        final String eventName = ((TextView) v.findViewById(R.id.sched_title)).getText().toString();
        callback.showEventDetail(EventDetails.class.getName(), eventName, eventId);
        return true;
    }
}
