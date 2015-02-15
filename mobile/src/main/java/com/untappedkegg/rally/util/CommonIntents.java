package com.untappedkegg.rally.util;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.provider.ContactsContract;
import android.widget.Toast;

import com.google.android.gms.plus.PlusShare;
import com.google.android.gms.plus.model.people.Person;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.BuildConfig;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.data.NewDataFetcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public final class CommonIntents {

    /*----- General Intents -----*/
    public static void sendEmail(final Context ctx, final String[] emailAddress, final String subject, final String message) {
        final Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this (or PayPal apparently)
        emailIntent.putExtra(Intent.EXTRA_EMAIL, emailAddress);
        if (!AppState.isNullOrEmpty(subject)) {
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        if (!AppState.isNullOrEmpty(message)) {
            emailIntent.putExtra(Intent.EXTRA_TEXT, message);
        }
        ctx.startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    public static void makeCall(final Context ctx, final String phoneNumber) {
        final Intent phoneIntent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("tel", phoneNumber, null));
        ctx.startActivity(phoneIntent);
    }

//    @SuppressWarnings("deprecation")
    public static void openImage(final Context ctx, final String imageUri) {
        final File img = ImageLoader.getInstance().getDiskCache().get(imageUri);
        final Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
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

    public static void getDirections(final Context ctx, final String address) {

        // Try using the 'proper' Google Maps URI, if that fails, try to open in a browser
        try {
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("geo:0,0?q=" + address.replaceAll(" ", "+")));
                ctx.startActivity(intent);
        }catch (Exception e) {
            // ActivityNotFoundException
            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("http://maps.google.com/maps?daddr=%s", address.replaceAll(" ", "%20"))));
            ctx.startActivity(intent);

        }
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
    public static void openUrl(final Context ctx, final String url) {
        if (!AppState.isNullOrEmpty(url) && (url.startsWith("http://") || url.startsWith("https://"))) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            ctx.startActivity(intent);
        } else {
            DialogManager.raiseUIError(ctx, ctx.getResources().getString(R.string.oops), ctx.getString(R.string.invalid_url), false);
        }
    }

    /*----- Calendar -----*/
    public static void addToContacts(final Context ctx, final String name, final String email, final String phone, final String address, final String department, final String title) {
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
    public static void addToCalendar(final Context ctx, final String title, final String location, final String description, final Date startTime, Date endTime, final String rrule) {
        if (endTime == null) {
            endTime = DateManager.add(Calendar.HOUR_OF_DAY, startTime, 1);
        }

        try {
                Intent intent = new Intent(Intent.ACTION_INSERT).setData(Events.CONTENT_URI)
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime.getTime())
                        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTime())
                        .putExtra(Events.TITLE, title).putExtra(Events.DESCRIPTION, description)
                        .putExtra(Events.EVENT_LOCATION, location)
                        .putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);
                if (!AppState.isNullOrEmpty(rrule)) {
                    intent.putExtra(Events.RRULE, rrule);
                }
                ctx.startActivity(intent);

        } catch (ActivityNotFoundException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            Toast.makeText(ctx, R.string.no_calendar, Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("NewApi")
    public static void addRallyToCalendar(final Context ctx, final String title, final Date startTime, Date endTime, final String location) {
        if (endTime == null) {
            endTime = DateManager.add(Calendar.HOUR_OF_DAY, startTime, 1);
        }

        try {
                Intent intent = new Intent(Intent.ACTION_INSERT)
                        .setData(Events.CONTENT_URI)
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime.getTime())
                        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTime())
                        .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
                        .putExtra(Events.EVENT_LOCATION, location)
                        .putExtra(Events.TITLE, title)
                        .putExtra(Events.AVAILABILITY, Events.AVAILABILITY_FREE);
                ctx.startActivity(intent);

        } catch (ActivityNotFoundException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            Toast.makeText(ctx, R.string.no_calendar, Toast.LENGTH_LONG).show();
        }
    }

	/* ----- File I/O -----*/

    public static boolean fileExists(final Context ctx, final String fileName) {
        try {
            return ctx.getFileStreamPath(fileName).exists();
        } catch (Exception e) {
            return false;
        }
    }

    public static String readFile(final Context ctx, final String fileName) {
        String fileContents = null;
        if (fileExists(ctx, fileName)) {
            try {
                fileContents = NewDataFetcher.readStream(ctx.openFileInput(fileName));
            } catch (Exception e) {
                fileContents = "File Not Found";
            }
        }
        return fileContents;

    }

    /*----- Share Intents -----*/
    public static Intent getShareIntent(final String type, final String subject, final String text)
    {
        boolean found = false;
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");

        // gets the list of intents that can be loaded.
        List<ResolveInfo> resInfo = AppState.getApplication().getPackageManager().queryIntentActivities(share, 0);
        System.out.println("resinfo: " + resInfo);
        if (!resInfo.isEmpty()){
            for (ResolveInfo info : resInfo) {
                if (info.activityInfo.packageName.toLowerCase().contains(type) || info.activityInfo.name.toLowerCase().contains(type) ) {
                    share.putExtra(Intent.EXTRA_SUBJECT,  subject);
                    if (!AppState.isNullOrEmpty(text))
                    share.putExtra(Intent.EXTRA_TEXT, text);
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

    public static void sendFeedback(final Context ctx) {
        List<Intent> targetedShareIntents = new ArrayList<Intent>();

        final String emailMsg = String.format("App Version: %s\nAndroid: %s : %s\nDevice: %s (%s)\nPlease leave the above lines for debugging purposes. Thank you!\n\n", BuildConfig.VERSION_NAME, Build.VERSION.SDK_INT, Build.VERSION.RELEASE, /*Build.FINGERPRINT,*/ Build.MODEL, Build.DEVICE);

        // Google+
        ArrayList<Person> recipients = new ArrayList<Person>();
        recipients.add(PlusShare.createPerson("109961307643513437237", BuildConfig.DEV_NAME));
        targetedShareIntents.add(new PlusShare.Builder(ctx).setType("text/plain").setRecipients(recipients).getIntent());

        // Email
        try {
            targetedShareIntents.add(CommonIntents.getShareIntent("email", "Feedback: " + ctx.getString(R.string.app_name), emailMsg).putExtra(Intent.EXTRA_EMAIL, new String[] {"UntappedKegg@gmail.com"}));
        } catch (Exception e) { }

        try {
            targetedShareIntents.add(CommonIntents.getShareIntent("gmail", "Feedback: " + ctx.getString(R.string.app_name), emailMsg).putExtra(Intent.EXTRA_EMAIL, new String[] {"UntappedKegg@gmail.com"}));
        } catch (Exception e) { }

        // Twitter
        Intent twitterIntent = CommonIntents.getShareIntent("twitter", "Untapped Rally", "@UntappedKegg ");
        if(twitterIntent != null)
            targetedShareIntents.add(twitterIntent);

        // Market
        try {
            final String mPackageName = ctx.getPackageName();
            final String installer = ctx.getPackageManager().getInstallerPackageName(mPackageName);
            Intent marketIntent = null;

            if (AppState.MARKET_GOOGLE.equalsIgnoreCase(installer)) {
                marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(AppState.APP_LINK_GOOGLE + mPackageName));
                marketIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

            } else if (AppState.MARKET_AMAZON.equalsIgnoreCase(installer)) {
                marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(AppState.APP_LINK_AMAZON + mPackageName));
                marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            if (marketIntent != null)
                targetedShareIntents.add(marketIntent);

        } catch (Exception e) { }

        if(!targetedShareIntents.isEmpty()) {
            Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), "Send Feedback via:");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
            ctx.startActivity(chooserIntent);
        } else Toast.makeText(ctx, R.string.no_apps_available, Toast.LENGTH_SHORT).show();

    }

}
