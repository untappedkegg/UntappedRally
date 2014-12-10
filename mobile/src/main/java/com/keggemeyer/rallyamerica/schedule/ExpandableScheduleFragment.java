package com.keggemeyer.rallyamerica.schedule;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.keggemeyer.rallyamerica.AppState;
import com.keggemeyer.rallyamerica.R;
import com.keggemeyer.rallyamerica.data.DataFetcher;
import com.keggemeyer.rallyamerica.event.EventDetails;
import com.keggemeyer.rallyamerica.event.EventPhotos;
import com.keggemeyer.rallyamerica.interfaces.Refreshable;
import com.keggemeyer.rallyamerica.stages.EventStages;
import com.keggemeyer.rallyamerica.ui.ExpandableList;
import com.keggemeyer.rallyamerica.util.CommonIntents;
import com.keggemeyer.rallyamerica.util.DateManager;

import java.text.ParseException;
import java.util.Calendar;

public class ExpandableScheduleFragment extends ExpandableList implements DataFetcher.Callbacks, OnClickListener,/*OnItemLongClickListener,*/ Refreshable, OnMenuItemClickListener {

    private Callbacks callback;
    private boolean isHomeFragment;
    private View menuView = null;

    public ExpandableScheduleFragment() {
    }

    /* ----- LIFECYCLE METHODS ----- */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //		activity.getActionBar().setDisplayShowTitleEnabled(true);
        //		getActivity().getActionBar().setTitle(R.string.app_name);
        try {
            //	    	Bundle bundle = getArguments();
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

    //    @Override
    //	public void onActivityCreated(Bundle savedState) {
    //	    super.onActivityCreated(savedState);

    //	    getListView().setOnItemLongClickListener(this);
    //	}


    /* ----- INHERITED METHODS ----- */
    //	Fragment
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("isHomeFragment", isHomeFragment);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void fetchData() {
//        DbUpdated.open();
//        if (DateManager.timeBetweenInDays(DbUpdated.lastUpdated_by_Source(AppState.MOD_SCHED)) > AppState.CAL_UPDATE_DELAY) {

            //			if (!DataFetcher.getInstance().sched_isRunning()) {
            DataFetcher.getInstance().sched_start(this, false);
            //				progressBar.setVisibility(View.VISIBLE);
            //			}
//        }
//        DbUpdated.close();
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
            //		return DbSchedule.fetch();
            return DbSchedule.fetchSections();
        }

    }

    @Override
    protected SimpleCursorTreeAdapter createCursorAdapter() {
        final String[] groupFrom = new String[]{DbSchedule.SCHED_YEAR_ACTUAL};
        final int[] groupTo = new int[]{R.id.generic_section_list_header_textview};
        String[] from = new String[]{DbSchedule.SCHED_ID, DbSchedule.SCHED_FROM_TO, DbSchedule.SCHED_SHORT_CODE, DbSchedule.SCHED_EVT_SITE};
        int[] to = new int[]{R.id.sched_id, R.id.sched_date, R.id.sched_icon, R.id.sched_website};
        //		return new WrapperExpandableListAdapter(getActivity(), R.layout.schedule_row, null, from, to, 0, this);
        return new ScheduleTreeCursorAdapter(getActivity(), null, R.layout.generic_section_list_header, groupFrom, groupTo, R.layout.schedule_row, from, to, this);
    }


    @Override
    protected boolean shouldRequery() {
        return DataFetcher.getInstance().sched_isRunning();
    }

    //    @Override
    //    protected String getSectionField() {
    //        if (isHomeFragment)
    //            return null;
    //        return DbSchedule.SCHED_YEAR_ACTUAL;
    //    }

    //	@Override
    //         protected SimpleCursorTreeAdapter.ViewBinder getViewBinder() {
    //             return new ScheduleViewBinder();
    //         }

//    @Override
//    protected void finishedLoading() {
//        AppState.setNextNotification();
//    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        final String eventId = ((TextView) v.findViewById(R.id.sched_id)).getText().toString();
        final String eventName = ((TextView) v.findViewById(R.id.sched_title)).getText().toString();
        callback.showEventDetail(EventDetails.class.getName(), eventId, eventName);
    }

    //    @Override
    //	public void onListItemClick(ListView l, View v, int position, long id) {
    ////        if(!adapter.isSection(position)) {
    //            final String eventId = ((TextView) v.findViewById(R.id.sched_id)).getText().toString();
    //            final String eventName = ((TextView) v.findViewById(R.id.sched_title)).getText().toString();
    //            callback.showEventDetail(EventDetails.class.getName(), eventId, eventName);
    ////        }
    //		// call back for dialog fragment which gives the option to choose from stages, stage times, itinerary
    //
    //	}

    @Override
    public void onDataFetchComplete(Throwable throwable, String parser) {

        if (parser.equals(AppState.MOD_SCHED)) {
            if (!this.isDetached() && this.isVisible()) {
                loadData();
            }
            finishedLoading();
        }
    }

    @Override
    public void onClick(View v) {
        menuView = ((View) v.getParent().getParent());
        final boolean isFinished = DbSchedule.isEventFinished(((TextView) menuView.findViewById(R.id.sched_id)).getText().toString());
        final String date = ((TextView) menuView.findViewById(R.id.sched_date)).getText().toString();
        PopupMenu popup = new PopupMenu(getActivity(), v);
        popup.inflate(R.menu.schedule);
        if (AppState.isNullOrEmpty(((TextView) menuView.findViewById(R.id.sched_website)).getText().toString())) {
            popup.getMenu().removeItem(R.id.menu_schedule_website);
        }
        if (!isFinished) {
            popup.getMenu().removeItem(R.id.menu_schedule_photos);
        }
        if ("TBD".equalsIgnoreCase(date) || "CANCELLED".equalsIgnoreCase(date) || isFinished) {
            popup.getMenu().removeItem(R.id.menu_schedule_add_to_cal);
        }
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        final String website = ((TextView) menuView.findViewById(R.id.sched_event_website)).getText().toString();
        final String eventName = ((TextView) menuView.findViewById(R.id.sched_title)).getText().toString();

        switch (item.getItemId()) {
            case R.id.menu_schedule_stages:
                callback.showEventDetail(EventStages.class.getName(), website, eventName);
                break;
            case R.id.menu_schedule_photos:
                callback.showEventDetail(EventPhotos.class.getName(), website, eventName);
                break;
            case R.id.menu_schedule_website:
                CommonIntents.openUrl(getActivity(), ((TextView) menuView.findViewById(R.id.sched_website)).getText().toString());
                break;
            case R.id.menu_schedule_event_website:
                CommonIntents.openUrl(getActivity(), website);
                break;
            case R.id.menu_schedule_add_to_cal:
                final String startDate = ((TextView) menuView.findViewById(R.id.sched_start_date)).getText().toString();
                final String endDate = ((TextView) menuView.findViewById(R.id.sched_end_date)).getText().toString();
                final String location = ((TextView) menuView.findViewById(R.id.sched_location)).getText().toString();


                try {
                    CommonIntents.addRallyToCalendar(getActivity(), eventName, DateManager.ISO8601_DATEONLY.parse(startDate), DateManager.add(Calendar.DAY_OF_MONTH, DateManager.ISO8601_DATEONLY.parse(endDate), 1), location);
                    return true;
                } catch (ParseException e) {
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.calendar_error), Toast.LENGTH_LONG).show();
                }
        }
        return true;
    }


    //	@Override
    //	public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
    //		String startDate = ((TextView) v.findViewById(R.id.start_date)).getText().toString();
    //		String endDate = ((TextView) v.findViewById(R.id.end_date)).getText().toString();
    //
    //		try {
    //			CommonIntents.addRallyToCalendar(getActivity(), ((TextView) v.findViewById(R.id.sched_title)).getText().toString(), DateManager.ISO8601_DATEONLY.parse(startDate), DateManager.add(Calendar.DAY_OF_MONTH, DateManager.ISO8601_DATEONLY.parse(endDate), 1) );
    //			return true;
    //		} catch (ParseException e) {
    //			//
    ////			e.printStackTrace();
    //			return false;
    //		}
    //
    //	}

    /* ----- NESTED INTERFACES ----- */
    public interface Callbacks {
        public void showEventDetail(String fragment, String args, String eventName);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        final String eventId = ((TextView) v.findViewById(R.id.sched_id)).getText().toString();
        final String eventName = ((TextView) v.findViewById(R.id.sched_title)).getText().toString();
        callback.showEventDetail(EventDetails.class.getName(), eventId, eventName);
        return true;
    }
}
