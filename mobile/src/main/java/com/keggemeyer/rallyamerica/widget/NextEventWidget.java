package com.keggemeyer.rallyamerica.widget;

import android.app.PendingIntent;
import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.keggemeyer.rallyamerica.AppState;
import com.keggemeyer.rallyamerica.R;
import com.keggemeyer.rallyamerica.data.BaseDbAccessor;
import com.keggemeyer.rallyamerica.data.DataFetcher;
import com.keggemeyer.rallyamerica.event.EventActivity;
import com.keggemeyer.rallyamerica.event.EventDetails;
import com.keggemeyer.rallyamerica.schedule.DbSchedule;
import com.keggemeyer.rallyamerica.util.DateManager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.text.ParseException;
import java.util.Locale;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link NextEventWidgetConfigureActivity NextEventWidgetConfigureActivity}
 */
public class NextEventWidget extends AppWidgetProvider implements DataFetcher.Callbacks {
    private static Intent intent;
    private static Context ctx;
    private static AppWidgetManager widgetManager;
    private static int[] widgetIds;

//    private static final DisplayImageOptions nextEventOptions = new DisplayImageOptions.Builder()
//            .showImageOnLoading(R.drawable.ra_large) // resource or drawable
//            .showImageForEmptyUri(R.drawable.ra_large) // resource or drawable
//            .showImageOnFail(R.drawable.ra_large) // resource or drawable
//            .cacheInMemory(true).cacheOnDisk(true)
//            .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
//            .bitmapConfig(Bitmap.Config.RGB_565)
//            .displayer(new FadeInBitmapDisplayer(750, true, true, false))
//            .build();
//    private static final DisplayImageOptions foregroundImg = new DisplayImageOptions.Builder()
//            .cacheInMemory(true)
//            .cacheOnDisk(true)
//            .imageScaleType(ImageScaleType.EXACTLY)
//            .bitmapConfig(Bitmap.Config.RGB_565)
//            .displayer(new FadeInBitmapDisplayer(750, true, true, false))
//            .build();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        this.ctx = context;
        this.widgetManager = appWidgetManager;
        this.widgetIds = appWidgetIds;
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            NextEventWidgetConfigureActivity.deleteTitlePref(context, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Ensure Data is present
        DataFetcher.getInstance().sched_start(this, false);
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        BaseDbAccessor.open();
        CharSequence widgetText = NextEventWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        // Construct the RemoteViews object
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_next_event);

        // SET UP THE VIEWS
        final Cursor c = DbSchedule.fetchNextEvent();
        if (c.moveToFirst()) {
            try {

                final String nextEventStart = c.getString(c.getColumnIndexOrThrow(DbSchedule.SCHED_START_DATE));
                final short evtStatus = DateManager.todayIsBetween(nextEventStart, c.getString(c.getColumnIndexOrThrow(DbSchedule.SCHED_END_DATE)));
                final String eventName = c.getString(c.getColumnIndexOrThrow(DbSchedule.SCHED_TITLE));
                final int eventId = c.getInt(c.getColumnIndex(DbSchedule.SCHED_ID));
                String uri = c.getString(c.getColumnIndexOrThrow(DbSchedule.SCHED_SHORT_CODE)).toLowerCase(Locale.US);
                views.setTextViewText(R.id.widget_next_event_title, eventName);
                if (uri.contains("100aw")) {
                    uri = "ra" + uri;
                }
                uri += "_large";
                File file = ImageLoader.getInstance().getDiskCache().get(uri);
                if (file != null && file.canRead()) {
                    views.setImageViewBitmap(R.id.widget_next_event_img, BitmapFactory.decodeFile(file.getAbsolutePath()));
                } else
                ImageLoader.getInstance().loadImage(AppState.EGG_DRAWABLE + uri, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
//                        views.setImageViewResource(R.id.widget_next_event_img, R.drawable.ra_large);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
//                        views.setViewVisibility(R.id.widget_next_event_img, View.GONE);
                        Log.e(this.getClass().getCanonicalName(), failReason.toString());
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        views.setImageViewBitmap(R.id.widget_next_event_img, loadedImage);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        Log.e(this.getClass().getCanonicalName(), "Image Loading CANCELLED!!");
                    }
                });
                intent = new Intent(AppState.getApplication(), EventActivity.class);
                intent.putExtra(AppState.KEY_URI, EventDetails.class.getName());
                intent.putExtra(AppState.KEY_ARGS, String.valueOf(eventId));
                intent.putExtra(SearchManager.QUERY, eventName);

                if (evtStatus == 0) {
                    final long evntTime = DateManager.parse(nextEventStart, DateManager.ISO8601_DATEONLY).getTime();
                    long diff = evntTime - System.currentTimeMillis();

                    if (diff > 0) {
                        String time = "";
                        String[] relative = DateManager.formatAsRelativeTime(diff);
                        for (short i = 0; i < 2; i++) {
                             /*if (!relative[i].startsWith("0"))*/
                            time += relative[i] + " ";
                        }
                        views.setTextViewText(R.id.widget_header_text, AppState.getApplication().getString(R.string.next_event) + ": " + time);
                        views.setTextColor(R.id.widget_header_text, context.getResources().getColor(android.R.color.white));
//                        views.setTextColor(R.id.widget_header_text, android.R.color.white);
                    } else {
                        views.setTextViewText(R.id.widget_header_text, AppState.getApplication().getString(R.string.now_live));
                        views.setTextColor(R.id.widget_header_text, context.getResources().getColor(R.color.light_green));
                    }
                } else if (evtStatus == 1) {
                    views.setTextViewText(R.id.widget_header_text, AppState.getApplication().getString(R.string.now_live));
                    views.setTextColor(R.id.widget_header_text, context.getResources().getColor(R.color.light_green));
                }

            } catch (ParseException e) {
                //                 views.setViewVisibility(R.id.next_event_img, View.GONE);
                //                 if (AppState.isNullOrEmpty(counter.getText().toString()))
                //                     counter.setVisibility(View.GONE);
                e.printStackTrace();
            } catch (CursorIndexOutOfBoundsException e) {

            } finally {
                c.close();
                BaseDbAccessor.close();
            }
        }

        views.setTextViewText(R.id.appwidget_text, widgetText);

        if (intent != null) {
            views.setOnClickPendingIntent(R.id.widget_next_event_frame, PendingIntent.getActivity(context, PendingIntent.FLAG_UPDATE_CURRENT, intent, Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        // Instruct the widget manager to update the widget
        try {
            appWidgetManager.updateAppWidget(appWidgetId, views);
        } catch (IllegalArgumentException e) {
            Toast.makeText(context, "It appears that your device has insufficient memory to handle this Widget", Toast.LENGTH_LONG).show();
            //             views.removeAllViews(R.layout.widget_next_event_frame);
        }
    }

    @Override
    public void onDataFetchComplete(Throwable throwable, String parser) {
        // There may be multiple widgets active, so update all of them
        final int N = widgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(ctx, widgetManager, widgetIds[i]);
        }
    }
}


