package com.untappedkegg.rally.util;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.provider.ContactsContract;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.data.DataFetcher;
import com.untappedkegg.rally.ui.BaseWebView;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CommonIntents {
    /* ----- CUSTOM METHODS ----- */
    /*----- General Intents -----*/
    public static void sendEmail(Context ctx, String emailAddress, String subject, String message) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", emailAddress, null));
        if (!AppState.isNullOrEmpty(subject)) {
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        if (!AppState.isNullOrEmpty(message)) {
            emailIntent.putExtra(Intent.EXTRA_TEXT, message);
        }
        ctx.startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    public static void makeCall(Context ctx, String phoneNumber) {
        Intent phoneIntent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("tel", phoneNumber, null));
        ctx.startActivity(phoneIntent);
    }

//    @SuppressWarnings("deprecation")
    public static void openImage(Context ctx, String imageUri) {
        File img = ImageLoader.getInstance().getDiskCache().get(imageUri);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
        if (img == null) {
            intent.setDataAndType(Uri.parse(imageUri), "image/jpeg");
        } else {
            intent.setDataAndType(Uri.fromFile(img), "image/jpeg");
        }
        try {
            ctx.startActivity(intent);
        } catch (Exception e) {
            //No Available recievers
        }
    }

    public static void getDirections(Context ctx, String address) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("http://maps.google.com/maps?daddr=%s", address)));
        ctx.startActivity(intent);
    }

    public static void getDirections(Context ctx, double latitude, double longitude) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("http://maps.google.com/maps?daddr=%s,%s", latitude, longitude)));
        ctx.startActivity(intent);
    }

	/*----- Web Intents -----*/

    /**
     * Starts a general browser {@link Intent} to open the {@code url}
     *
     * @param ctx The context used to youTubeStart the {@link Intent}
     * @param url The URL to open
     */
    public static void openUrl(Context ctx, String url) {
        if (!AppState.isNullOrEmpty(url) && (url.startsWith("http://") || url.startsWith("https://"))) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            ctx.startActivity(intent);
        } else {
            DialogManager.raiseUIError(ctx, ctx.getResources().getString(R.string.oops), ctx.getString(R.string.invalid_url), false);
        }
    }

    /**
     * Tries to open the {@code url} in the stock android browser, failing that
     * it tries to open in Chrome (which is the new stock Browser), failing both
     * starts a general browser {@link Intent}
     *
     * @param ctx The context used to youTubeStart the {@link Intent}
     * @param url The URL to open
     */
    public static void openUrlInStockBrowser(Context ctx, String url) {
        if (!AppState.isNullOrEmpty(url)) {
            try {
                String packageName = "com.android.browser";
                String className = "com.android.browser.BrowserActivity";
                Intent internetIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                internetIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                internetIntent.setClassName(packageName, className);
                ctx.startActivity(internetIntent);
            } catch (Exception e) {
                try {
                    Intent i = new Intent("android.intent.action.MAIN");
                    i.setComponent(ComponentName.unflattenFromString("com.android.chrome/com.android.chrome.Main"));
                    i.addCategory("android.intent.category.LAUNCHER");
                    i.setData(Uri.parse(url));
                    ctx.startActivity(i);
                } catch (Exception d) {
                    openUrl(ctx, url);
                }
            }
        } else {
            DialogManager.raiseUIError(ctx, ctx.getResources().getString(R.string.oops), ctx.getString(R.string.invalid_url), false);
        }
    }

    public static void openWebView(Context ctx, String url) {
        if (!AppState.isNullOrEmpty(url)) {
            Intent intent = new Intent(AppState.getApplication(), BaseWebView.class);
            intent.putExtra(AppState.KEY_URL, url);

            ctx.startActivity(intent);

        } else {
            DialogManager.raiseUIError(ctx, ctx.getResources().getString(R.string.oops), ctx.getString(R.string.invalid_url), false);
        }
    }

	/*----- Fragment and Container -----*/

    public static void startNewContainer(Context ctx, String fragment, String args, Class<?> ContainerClass, String query) {
        Intent intent = new Intent(AppState.getApplication(), ContainerClass);
        intent.putExtra(AppState.KEY_URI, fragment);
        intent.putExtra(AppState.KEY_ARGS, args);
        intent.putExtra(SearchManager.QUERY, query);
        //		intent.putExtra(KEY_RESTARTING, url);

        ctx.startActivity(intent);
    }

    public static void startNewContainer(Context ctx, String fragment, Class<?> ContainerClass, Bundle bundle) {
        Intent intent = new Intent(AppState.getApplication(), ContainerClass);
        intent.putExtra(AppState.KEY_URI, fragment);
        intent.putExtra(AppState.KEY_BUNDLE, bundle);

        ctx.startActivity(intent);
    }

    /*----- Calendar -----*/
    public static void addToContacts(Context ctx, String name, String email, String phone, String address, String department, String title) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        intent.putExtra(ContactsContract.Intents.Insert.NAME, name);
        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, email);
        intent.putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, phone);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
        intent.putExtra(ContactsContract.Intents.Insert.POSTAL, address);
        intent.putExtra(ContactsContract.Intents.Insert.POSTAL_TYPE, ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK);
        intent.putExtra(ContactsContract.Intents.Insert.COMPANY, department);
        intent.putExtra(ContactsContract.Intents.Insert.JOB_TITLE, title);
        ctx.startActivity(intent);
    }

    /**
     * For non-recurring events.
     *
     * @param ctx
     * @param title
     * @param location
     * @param description
     * @param startTime
     * @param endTime
     */
    public static void addToCalendar(Context ctx, String title, String location, String description, Date startTime, Date endTime) {
        addToCalendar(ctx, title, location, description, startTime, endTime, null);
    }

    /**
     * For recurring events.
     *
     * @param ctx
     * @param title
     * @param location
     * @param description
     * @param startTime   The youTubeStart time on the first day.
     * @param endTime     The end time on the first day.
     * @param rrule       The special string used to convey the recurrence rules to the calendar.  Follows RFC 5545 specification (http://tools.ietf.org/html/rfc5545#section-3.8.5.3).
     */
    @SuppressLint("NewApi")
    public static void addToCalendar(Context ctx, String title, String location, String description, Date startTime, Date endTime, String rrule) {
        if (endTime == null) {
            endTime = DateManager.add(Calendar.HOUR_OF_DAY, startTime, 1);
        }

        try {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                Intent intent = new Intent(Intent.ACTION_EDIT).setType("vnd.android.cursor.item/event").putExtra("title", title).putExtra("eventLocation", location).putExtra("description", description).putExtra("beginTime", startTime.getTime()).putExtra("endTime", endTime.getTime());
                if (!AppState.isNullOrEmpty(rrule)) {
                    intent.putExtra("rrule", rrule);
                }
                ctx.startActivity(intent);
            } else {
                Intent intent = new Intent(Intent.ACTION_INSERT).setData(Events.CONTENT_URI).putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime.getTime()).putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTime()).putExtra(Events.TITLE, title).putExtra(Events.DESCRIPTION, description).putExtra(Events.EVENT_LOCATION, location).putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);
                if (!AppState.isNullOrEmpty(rrule)) {
                    intent.putExtra(Events.RRULE, rrule);
                }
                ctx.startActivity(intent);
            }
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(ctx, R.string.no_calendar, Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("NewApi")
    public static void addRallyToCalendar(Context ctx, String title, Date startTime, Date endTime, String location) {
        if (endTime == null) {
            endTime = DateManager.add(Calendar.HOUR_OF_DAY, startTime, 1);
        }

        try {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                Intent intent = new Intent(Intent.ACTION_EDIT).setType("vnd.android.cursor.item/event")
                        .putExtra("title", title)
                        .putExtra("eventLocation", location)
                        .putExtra("beginTime", startTime.getTime())
                        .putExtra("endTime", endTime.getTime())
                        .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
                ctx.startActivity(intent);
            } else {
                Intent intent = new Intent(Intent.ACTION_INSERT).setData(Events.CONTENT_URI)
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime.getTime())
                        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTime())
                        .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
                        .putExtra(Events.EVENT_LOCATION, location)
                        .putExtra(Events.TITLE, title)
                        .putExtra(Events.AVAILABILITY, Events.AVAILABILITY_FREE);
                ctx.startActivity(intent);
            }
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(ctx, R.string.no_calendar, Toast.LENGTH_LONG).show();
        }
    }

	/* ----- File I/O -----*/

    public static boolean fileExists(Context ctx, String fileName) {
        try {
            return ctx.getFileStreamPath(fileName).exists();
        } catch (Exception e) {
            return false;
        }
    }

    public static String readFile(Context ctx, String fileName) {
        String fileContents = null;
        if (fileExists(ctx, fileName)) {
            try {
                //				InputStream inputStream = ctx.openFileInput(AppState.FUNC_RA_STAND);
                fileContents = DataFetcher.readStream(ctx.openFileInput(fileName));
            } catch (Exception e) {
                fileContents = "File Not Found";
            }
        }
        return fileContents;

    }

    /*----- Share Intents -----*/
    public static Intent getShareIntent(String type, String subject, String text)
    {
        boolean found = false;
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");

        // gets the list of intents that can be loaded.
        List<ResolveInfo> resInfo = AppState.getApplication().getPackageManager().queryIntentActivities(share, 0);
        System.out.println("resinfo: " + resInfo);
        if (!resInfo.isEmpty()){
            for (ResolveInfo info : resInfo) {
                if (info.activityInfo.packageName.toLowerCase().contains(type) ||
                        info.activityInfo.name.toLowerCase().contains(type) ) {
                    share.putExtra(Intent.EXTRA_SUBJECT,  subject);
                    share.putExtra(Intent.EXTRA_TEXT,     text);
                    share.setPackage(info.activityInfo.packageName);
                    found = true;
                    break;
                }
            }
            if (found)
                return share;
        }
        return null;
    }
}
