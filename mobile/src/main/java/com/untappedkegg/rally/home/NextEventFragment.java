package com.untappedkegg.rally.home;


import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.BuildConfig;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.data.DataFetcher;
import com.untappedkegg.rally.interfaces.NavDrawerItemSelected;
import com.untappedkegg.rally.interfaces.Refreshable;
import com.untappedkegg.rally.interfaces.ScheduleItemClickReceiver;
import com.untappedkegg.rally.schedule.DbSchedule;
import com.untappedkegg.rally.ui.BaseFragment;
import com.untappedkegg.rally.util.DateManager;

import java.text.ParseException;
import java.util.Locale;


public final class NextEventFragment extends BaseFragment implements View.OnClickListener, DataFetcher.Callbacks, Refreshable {

    TextView counter, name;
    ImageView picture, background;
    CountDownTimer timer;
    private int eventId;
    private String eventName;
    private boolean isRequeryFinished;
    private ScheduleItemClickReceiver callback;
    private final DisplayImageOptions nextEventOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.ra_large) // resource or drawable
            .showImageForEmptyUri(R.drawable.ra_large) // resource or drawable
            .showImageOnFail(R.drawable.ra_large) // resource or drawable
            .cacheInMemory(true).cacheOnDisk(true)
            .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .displayer(new FadeInBitmapDisplayer(750, true, true, false))
            .build();

    public NextEventFragment() {
        // Required empty public constructor
    }

    /* ----- LIFECYCLE METHODS ----- */
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            callback = (ScheduleItemClickReceiver) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement " + NavDrawerItemSelected.class.getSimpleName());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isRequeryFinished = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_next_event, container, false);
        counter = (TextView) view.findViewById(R.id.next_event_count_down);
        name = (TextView) view.findViewById(R.id.next_event_name);
        picture = (ImageView) view.findViewById(R.id.next_event_img);
        background = (ImageView) view.findViewById(R.id.next_event_background);
        progressBar = (ProgressBar) view.findViewById(R.id.next_event_progress);
        view.setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // By doing this in onResume() we can update the UI
        // when the user returns, this is especially important
        // if there was a change in events since we were paused
        if (dataFetched) {
            finishRequery();
        } else if (fetchOnCreate) {
            fetchData();
            dataFetched = true;
            super.startRequery();
        } else {
            finishRequery();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(timer != null) {
            timer.cancel();
        }
    }

    private void fetchData() {
        DataFetcher.getInstance().sched_start(this, false);
    }

    /*----- INHERITED METHODS -----*/
    @Override
    public void refreshData() {
        DataFetcher.getInstance().sched_start(this, true);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected boolean shouldRequery() {
        return DataFetcher.getInstance().sched_isRunning();
    }

    @Override
    protected void finishRequery() {
        progressBar.setVisibility(View.GONE);

        final Cursor c = DbSchedule.fetchNextEvent();

        if (c.moveToFirst()) {
            try {
                ImageLoader.getInstance().displayImage(c.getString(c.getColumnIndexOrThrow(DbSchedule.SCHED_IMG)), background, nextEventOptions);
                final String nextEventStart = /*"2014-11-08"*/c.getString(c.getColumnIndexOrThrow(DbSchedule.SCHED_START_DATE));
                final short evtStatus = DateManager.todayIsBetween(nextEventStart, c.getString(c.getColumnIndexOrThrow(DbSchedule.SCHED_END_DATE)));
                eventName = c.getString(c.getColumnIndexOrThrow(DbSchedule.SCHED_TITLE));
                eventId = c.getInt(c.getColumnIndexOrThrow(DbSchedule.SCHED_ID));
                isRequeryFinished = true;

                String uri = c.getString(c.getColumnIndexOrThrow(DbSchedule.SCHED_SHORT_CODE)).toLowerCase(Locale.US);

                if (uri.contains("100aw")) {
                    uri = "ra" + uri;
                }
                uri += "_large";
                ImageLoader.getInstance().displayImage(AppState.EGG_DRAWABLE + uri, picture, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        name.setText(eventName);
                        name.setVisibility(View.VISIBLE);

                        picture.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                    }
                });

                if (evtStatus == 0) {

                    final long eventTime = DateManager.parse(nextEventStart, DateManager.ISO8601_DATEONLY).getTime();
                    if (timer != null) {
                        timer.cancel();
                        timer = null;
                    }
                    counter.setVisibility(View.VISIBLE);
                    timer = new CountDownTimer(eventTime - System.currentTimeMillis(), 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            StringBuilder time = new StringBuilder();
                            String[] relative = DateManager.formatAsRelativeTime(millisUntilFinished);

                            for (short i = 0; i < 3; i++) {
                                time.append(relative[i])
                                        .append(" ");
                            }
                            counter.setText(time.toString());
                        }

                        @Override
                        public void onFinish() {
                            counter.setText(R.string.now_live);
                            counter.setBackgroundColor(getResources().getColor(R.color.light_green));
                        }
                    };
                    timer.start();
                } else if (evtStatus == 1) {
                    counter.setText(R.string.now_live);
                    counter.setBackgroundColor(getResources().getColor(R.color.light_green));
                }
            } catch (ParseException e) {
                picture.setVisibility(View.GONE);
                if (TextUtils.isEmpty(counter.getText().toString())) {
                    counter.setVisibility(View.GONE);
                }
                if (BuildConfig.DEBUG)
                    e.printStackTrace();

            } catch (CursorIndexOutOfBoundsException e) {
                if (BuildConfig.DEBUG)
                    e.printStackTrace();


            } finally {
                c.close();
            }
        } else {
            c.close();
        }

    }

    @Override
    public void onClick(View v) {
        if (isRequeryFinished) {
            callback.showEventDetail( eventName, eventId);
        } else {
            Toast.makeText(getActivity(), R.string.just_a_moment, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDataFetchComplete(Throwable throwable, String parser) {
        if (parser.equals(AppState.MOD_SCHED)) {
            progressBar.setVisibility(View.GONE);
        }
    }

}
