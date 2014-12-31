package com.untappedkegg.rally.event;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.data.NewDataFetcher;
import com.untappedkegg.rally.schedule.DbSchedule;
import com.untappedkegg.rally.ui.BaseDetails;
import com.untappedkegg.rally.ui.loaders.DetailsAdapter;
import com.untappedkegg.rally.util.CommonIntents;
import com.untappedkegg.rally.util.DateManager;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;

public class EventDetails extends BaseDetails implements NewDataFetcher.Callbacks /*implements OnClickListener */ {
    private static String eventId = "";
    private static String eventName;
    private Callbacks callback;
    private boolean isFinished = false;
    private final DisplayImageOptions eventOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.ic_launcher_large) // resource or drawable
            .showImageForEmptyUri(R.drawable.ic_launcher_large) // resource or drawable
            .showImageOnFail(R.drawable.ic_launcher_large) // resource or drawable
            .cacheInMemory(true).cacheOnDisk(true)
            .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .displayer(new FadeInBitmapDisplayer(750, true, true, false))
            .build();

    //	private Button addToCalBtn, webSiteBtn, raWebsiteBtn;
    /*----- LIFECYCLE METHODS -----*/
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callback = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement " + ((Object) this).getClass().getCanonicalName() + ".Callbacks");
        }
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        eventId = getArguments().getString(AppState.KEY_ARGS);
        eventName = getArguments().getString(SearchManager.QUERY);
        isFinished = DbSchedule.isEventFinished(eventId);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.callback = (Callbacks) getActivity();
        //		addToCal = (Button) getActivity().findViewById(R.id.events_cal_button);
        //		webSite = (Button) getActivity().findViewById(R.id.events_web_button);
        //		addToCal.setOnClickListener(this);
        //		webSite.setOnClickListener(this);
    }


    /**
     * @see android.support.v4.app.Fragment#onCreateOptionsMenu(android.view.Menu, android.view.MenuInflater)
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(!isFinished)
        inflater.inflate(R.menu.detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_to_cal) {
            final String startDate = ((TextView) getActivity().findViewById(R.id.events_start)).getText().toString();
            final String endDate = ((TextView) getActivity().findViewById(R.id.events_end)).getText().toString();
            final String location = ((TextView) getActivity().findViewById(R.id.events_location)).getText().toString();

            try {
                CommonIntents.addRallyToCalendar(getActivity(), eventName, DateManager.ISO8601_DATEONLY.parse(startDate), DateManager.add(Calendar.DAY_OF_MONTH, DateManager.ISO8601_DATEONLY.parse(endDate), 1), location);
                return true;
            } catch (ParseException e) {
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.calendar_error), Toast.LENGTH_LONG).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }


    /*----- INHERITED METHODS -----*/
    @Override
    protected Cursor loadCursor() {
        return DbEvent.fetchDetails(eventId);
    }

    @Override
    protected DetailsAdapter createAdapter() {
        String[] from = new String[]{DbSchedule.SCHED_SHORT_CODE, DbSchedule.SCHED_START_DATE, DbSchedule.SCHED_END_DATE, DbSchedule.SCHED_SERIES, DbSchedule.SCHED_SITE, DbSchedule.SCHED_SEQ, DbSchedule.SCHED_EVT_SITE, DbSchedule.SCHED_ID, DbSchedule.SCHED_START_DATE, DbSchedule.SCHED_END_DATE, DbSchedule.SCHED_LOC, DbSchedule.SCHED_DESCR};
        int[] to = new int[]{R.id.events_image, R.id.events_start, R.id.events_end, R.id.events_series, R.id.events_website, R.id.events_round, R.id.events_evt_website, R.id.events_actions_table, R.id.events_start_human, R.id.events_end_human, R.id.events_location, R.id.events_descr};

        return new DetailsAdapter(getActivity(), null, from, to, getDetailsContainerId(), R.layout.event_details);
    }

    //	@Override
    //	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    //		super.onLoadFinished(loader, cursor);
    //	}

    @Override
    protected ViewBinder getViewBinder() {
        return new NewEventViewBinder();
    }

    @Override
    public void fetchData() {
        EventFetcher.getInstance().startDetails(this, DbSchedule.fetchEventRA_link(eventId));
    }

    @Override
    protected String getTitleText() {
        return eventName;
    }

    @SuppressLint("NewApi")
    public class NewEventViewBinder implements ViewBinder {

        private final Context ctx = getActivity();

        @Override
        public boolean setViewValue(View v, Cursor c, int columnIndex) {

            String uri = "";
            try {
                uri = c.getString(columnIndex);
            } catch (Exception e) {
                //				e.printStackTrace();

            }
            switch (v.getId()) {

                case R.id.events_image:
                    final ImageView imageView = (ImageView) v.findViewById(R.id.events_image);
                    //				ImageLoader.getInstance().displayImage(uri, imageView);
                    if (!AppState.isNullOrEmpty(uri)) {
//                        Context context = imageView.getContext();
                        imageView.setAdjustViewBounds(true);

                        if (uri.contains("100AW")) {
                            uri = "ra" + uri;
                        }
                        uri = uri + "_large";
                        //					int id = context.getResources().getIdentifier(uri.toLowerCase(Locale.US), "drawable", context.getPackageName());
                        //					if (id != 0) {
                        //						imageView.setImageResource(id);
                        //					} else {
                        //						imageView.setImageResource(R.drawable.ra_large);
                        //					}
                        ImageLoader.getInstance().displayImage(AppState.EGG_DRAWABLE + uri.toLowerCase(Locale.US), imageView, eventOptions);
                        imageView.setVisibility(View.VISIBLE);
                    }
                    return true;
                case R.id.events_location:
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CommonIntents.getDirections(getActivity(), ((TextView) v).getText().toString());
                        }
                    });
                    ((TextView) v).setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
                    return false;
                case R.id.events_round:
                    if (c.getString(c.getColumnIndexOrThrow(DbSchedule.SCHED_SERIES)).equalsIgnoreCase("regional")) {
                        ((View) v.getParent()).setVisibility(View.GONE);
                        return true;
                    }
                    return false;
                //                case R.id.events_series:
                //				if (uri.equalsIgnoreCase("regional")) {
                //					getActivity().findViewById(R.id.events_round).setVisibility(View.GONE);
                //                    getActivity().findViewById(R.id.events_round_text).setVisibility(View.GONE);
                //				}
                //                return false;

                case R.id.events_start_human:
                case R.id.events_end_human:
                    TextView tv = (TextView) v;
                    try {
                        tv.setText(DateManager.parseFromDb(uri, DateManager.DATEONLY_HUMAN_READABLE));
                    } catch (ParseException e) {
                        tv.setText(uri);
                        e.printStackTrace();
                    }
                    return true;
                case R.id.events_actions_table:

                    final LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View newRow = inflater.inflate(R.layout.event_details_row, null);
                    newRow.findViewById(R.id.event_details_list_right_arrow).setVisibility(View.GONE);
                    ((ViewGroup) v).addView(newRow);
                    // Stages
                    newRow = inflater.inflate(R.layout.event_details_row, null);
                    ((TextView) newRow.findViewById(R.id.event_details_list_title)).setText(R.string.events_stages);

                    newRow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            callback.selectStages(((TextView) getActivity().findViewById(R.id.events_evt_website)).getText().toString());
                        }
                    });
                    ((ViewGroup) v).addView(newRow);

                    // Results
                    //				newRow = inflater.inflate(R.layout.event_details_row, null);
                    //				((TextView) newRow.findViewById(R.id.event_details_list_title)).setText(R.string.events_results);
                    //
                    //				newRow.setOnClickListener(new View.OnClickListener() {
                    //					@Override
                    //					public void onClick(View v) {
                    //						callback.selectResults(((TextView) getActivity().findViewById(R.id.events_evt_website)).getText().toString());
                    //						}
                    //					});
                    //				((ViewGroup) v).addView(newRow);

                    //Photos
                    if (isFinished) {
                        newRow = inflater.inflate(R.layout.event_details_row, null);
                        ((TextView) newRow.findViewById(R.id.event_details_list_title)).setText(R.string.events_photos);
                        newRow.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                callback.selectPhotos(((TextView) getActivity().findViewById(R.id.events_evt_website)).getText().toString());
                            }
                        });
                        ((ViewGroup) v).addView(newRow);
                    }

                    // Event Website
                    if (!AppState.isNullOrEmpty(c.getString(c.getColumnIndex(DbSchedule.SCHED_EVT_SITE)))) {
                        newRow = inflater.inflate(R.layout.event_details_row, null);
                        ((TextView) newRow.findViewById(R.id.event_details_list_title)).setText(R.string.event_website);
                        ((TextView) newRow.findViewById(R.id.event_details_list_uri)).setText(c.getString(c.getColumnIndex(DbSchedule.SCHED_EVT_SITE)));
                        newRow.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CommonIntents.openUrl(getActivity(), ((TextView) getActivity().findViewById(R.id.events_evt_website)).getText().toString());
                            }
                        });
                        ((ViewGroup) v).addView(newRow);
                    }

                    if (!AppState.isNullOrEmpty(c.getString(c.getColumnIndex(DbSchedule.SCHED_SITE)))) {
                        newRow = inflater.inflate(R.layout.event_details_row, null);

                        ((TextView) newRow.findViewById(R.id.event_details_list_title)).setText(R.string.website);
                        newRow.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CommonIntents.openUrl(getActivity(), ((TextView) getActivity().findViewById(R.id.events_website)).getText().toString());
                            }
                        });

                        ((ViewGroup) v).addView(newRow);
                    }
                    //Calendar
                    //				((TextView) newRow.findViewById(R.id.event_details_list_title)).setText(R.string.add_to_cal);
                    //				((TextView) newRow.findViewById(R.id.event_details_list_uri)).setText(c.getString(c.getColumnIndex(DbSchedule.SCHED_SITE)));
                    //				newRow.setOnClickListener(new View.OnClickListener() {
                    //					@Override
                    //					public void onClick(View v) {
                    //						final String startDate = ((TextView) getActivity().findViewById(R.id.events_start)).getText().toString();
                    //						final String endDate = ((TextView) getActivity().findViewById(R.id.events_end)).getText().toString();
                    //
                    //						try {
                    //							CommonIntents.addRallyToCalendar(getActivity(), eventName, DateManager.ISO8601_DATEONLY.parse(startDate), DateManager.add(Calendar.DAY_OF_MONTH, DateManager.ISO8601_DATEONLY.parse(endDate), 1) );
                    //
                    //						} catch (ParseException e) {
                    //
                    //							Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.calendar_error), Toast.LENGTH_LONG).show();
                    //						}
                    //					}
                    //				});
                    //				((ViewGroup) v).addView(newRow);
                    return true;
                //			case R.id.sched_status:
                //				TextView tv = (TextView) v;
                //				int status = DateManager.todayIsBetween(c.getString(c.getColumnIndex(DbSchedule.SCHED_START_DATE)), c.getString(c.getColumnIndex(DbSchedule.SCHED_END_DATE)));
                //				if (status == 0) {
                //					tv.setText("Upcoming");
                //					tv.setTextColor(AppState.getApplication().getResources().getColor(R.color.LightBlue));
                //					return true;
                //				} else if (status == 1) {
                //					//currently running
                //					tv.setText("Live!");
                //					tv.setTextColor(AppState.getApplication().getResources().getColor(R.color.light_green));
                //					return true;
                //				} else {
                //					//completed
                //					tv.setText("Complete");
                //					tv.setTextColor(AppState.getApplication().getResources().getColor(R.color.light_red));
                //				}
                //				return true;
                //			case R.id.sched_date:
                //				((TextView) v).setText(uri + " " + c.getString(c.getColumnIndex(DbSchedule.SCHED_START_DATE)).substring(0, 4));
                //				return true;
                default:
                    return false;
            }
        }

    }

    @Override
    protected boolean shouldRequery() {
        return EventFetcher.getInstance().isRunning();
    }

    @Override
    public void onDataFetchComplete(Throwable away, String function) {
        try {
            loadData();
        } catch (Exception e) {
            //Not attached
        }
    }


    public interface Callbacks {
        public void selectPhotos(String link);

        public void selectStages(String link);

        public void selectResults(String link);
    }

}
