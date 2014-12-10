package com.keggemeyer.rallyamerica.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.keggemeyer.rallyamerica.AppState;
import com.keggemeyer.rallyamerica.R;
import com.keggemeyer.rallyamerica.data.BaseDbAccessor;
import com.keggemeyer.rallyamerica.event.EventActivity;
import com.keggemeyer.rallyamerica.event.EventDetails;
import com.keggemeyer.rallyamerica.schedule.DbSchedule;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.util.Locale;

/**
 * Helper class for showing and canceling event live
 * notifications.
 * <p/>
 * This class makes heavy use of the {@link NotificationCompat.Builder} helper
 * class to create notifications in a backward-compatible way.
 */
public class EventLiveNotification {
    /**
     * The unique identifier for this type of notification.
     */
    private static final String NOTIFICATION_TAG = "EventLive";

    /**
     * Shows the notification, or updates a previously shown notification of
     * this type, with the given parameters.
     * <p/>
     * TODO: Customize this method's arguments to present relevant content in
     * the notification.
     * <p/>
     * TODO: Customize the contents of this method to tweak the behavior and
     * presentation of event live notifications. Make
     * sure to follow the
     * <a href="https://developer.android.com/design/patterns/notifications.html">
     * Notification design guidelines</a> when doing so.
     *
     * @see #cancel(Context)
     */
    public static void notify(final Context context, final int number) {
        final Resources res = context.getResources();

        //Get Data
        BaseDbAccessor.open();
        Cursor c = DbSchedule.fetchNextEvent();
        if (!c.moveToFirst()) {
            c.close();
            BaseDbAccessor.close();
            return;
        }
        final String eventName = c.getString(c.getColumnIndex(DbSchedule.SCHED_TITLE));
        final int eventId = c.getInt(c.getColumnIndex(DbSchedule.SCHED_ID));
        final String startDate = c.getString(c.getColumnIndex(DbSchedule.SCHED_START_DATE));
        final String endDate = c.getString(c.getColumnIndex(DbSchedule.SCHED_END_DATE));
        final String fromTo = c.getString(c.getColumnIndex(DbSchedule.SCHED_FROM_TO));
        final String website = c.getString(c.getColumnIndex(DbSchedule.SCHED_SITE));
        String uri = c.getString(c.getColumnIndex(DbSchedule.SCHED_SHORT_CODE)).toLowerCase(Locale.US);
        c.close();
        BaseDbAccessor.close();

        if (uri.contains("100aw")) {
            uri = "ra" + uri;
        }
        final String imgUrl = AppState.EGG_DRAWABLE + uri + "_large";
        //        if (DateManager.todayIsBetween(startDate, endDate) != 1) {
        //            return;
        //        }
        // This image is used as the notification's large icon (thumbnail).
        // TODO: Remove this if your notification has no relevant thumbnail.
        final Bitmap picture = ImageLoader.getInstance().loadImageSync(imgUrl, new ImageSize(128, 128));//BitmapFactory.decodeResource(res, R.drawable.example_picture);
        final Intent intent = new Intent(AppState.getApplication(), EventActivity.class);
        intent.putExtra(AppState.KEY_URI, EventDetails.class.getName());
        intent.putExtra(AppState.KEY_ARGS, String.valueOf(eventId));
        intent.putExtra(SearchManager.QUERY, eventName);

        //        final String ticker = exampleString;
        final String title = res.getString(R.string.event_live_notification_title_template, eventName);
        //        final String text = res.getString(R.string.event_live_notification_placeholder_text_template, exampleString);
        //        long diff = System.currentTimeMillis();
        //        try {
        //            diff = DateManager.parse(startDate, DateManager.ISO8601_DATEONLY).getTime();
        //        } catch (ParseException e) {
        ////            diff = 0;
        //            e.printStackTrace();
        //        }

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)

                // Set appropriate defaults for the notification light, sound,
                // and vibration.
                .setDefaults(Notification.DEFAULT_ALL)

                        // Set required fields, including the small icon, the
                        // notification title, and text.
                .setSmallIcon(R.drawable.ic_launcher).setContentTitle(title)
                        //                .setContentText(text)

                        // All fields below this line are optional.

                        // Use a default priority (recognized on devices running Android
                        // 4.1 or later)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

                        // Provide a large icon, shown with the notification in the
                        // notification drawer on devices running Android 3.0 or later.
                .setLargeIcon(picture)

                        // Set ticker text (preview) information for this notification.
                        //                .setTicker(ticker)

                        // Show a number. This is useful when stacking notifications of
                        // a single type.
                .setNumber(number)

                        // If this notification relates to a past or upcoming event, you
                        // should set the relevant time information using the setWhen
                        // method below. If this call is omitted, the notification's
                        // timestamp will by set to the time at which it was shown.
                        // TODO: Call setWhen if this notification relates to a past or
                        // upcoming event. The sole argument to this method should be
                        // the notification timestamp in milliseconds.
                        //.setWhen(...)


                        //                .setWhen(diff)
                        // Set the pending intent to be initiated when the user touches
                        // the notification.
                .setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)).setContentText(fromTo)
                        // Show expanded text content on devices running Android 4.1 or
                        // later.
                .setStyle(new NotificationCompat.BigTextStyle().bigText(eventName + "\n" + fromTo).setBigContentTitle(title)
                        /*.setSummaryText(eventName + "\n" + fromTo)*/)

                        // Example additional actions for this notification. These will
                        // only show on devices running Android 4.1 or later, so you
                        // should ensure that the activity in this notification's
                        // content intent provides access to the same actions in
                        // another way.
                .addAction(R.drawable.ic_action_share, res.getString(R.string.action_share), PendingIntent.getActivity(context, 0, Intent.createChooser(new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, res.getString(R.string.event_live_notification_send_text, eventName)), res.getString(R.string.event_live_notification_share_chooser_text)), PendingIntent.FLAG_UPDATE_CURRENT)).addAction(R.drawable.ic_action_web_site, res.getString(R.string.action_website), PendingIntent.getActivity(context, 0, Intent.createChooser(new Intent(Intent.ACTION_VIEW, Uri.parse(website)), res.getString(R.string.website)), PendingIntent.FLAG_UPDATE_CURRENT))

                        // Automatically dismiss the notification when it is touched.
                .setAutoCancel(true);

        notify(context, builder.build());
    }

    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_TAG, 0, notification);
    }

    /**
     * Cancels any notifications of this type previously shown using
     * {@link #notify(Context, String, int)}.
     */
    public static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_TAG, 0);
    }
}
