package com.untappedkegg.rally.util;

import android.text.format.DateUtils;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.BuildConfig;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Thread-safe wrapper for the Calendar and DateFormat classes.
 */
public class DateManager {
    // See link for help on format string syntax
    // http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html

    /* ----- FORMATTER CONSTANTS ----- */
    //User preferred time format
    static final Locale locale = AppState.localeUser;

    public static final DateFormat iso8602 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    public static final DateFormat bestOfRally = new SimpleDateFormat("EEE, dd.MM.yyyy HH:mm:ss", Locale.US);

    // ISO8601 - used inside the local database, because it is lexically sortable.
    public static final DateFormat DATABASE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    public static final DateFormat YEAR = new SimpleDateFormat("yyyy", Locale.US);

    public static final DateFormat dateonly = new SimpleDateFormat("MM/dd/yyyy", locale);
    public static final DateFormat timeonly = new SimpleDateFormat("h:mm a", locale);
    public static final DateFormat SCHED_DATE = new SimpleDateFormat("yyyy-MMM-dd HH:mm", Locale.US);

    public static final DateFormat ISO8601_DATEONLY = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    public static final DateFormat ISO8601_TIMEONLY = new SimpleDateFormat("HH:mm:ss", locale);
    public static final DateFormat GOMIZZOU = new SimpleDateFormat("MM/dd/yyyy hh:mm a", locale);
    public static final DateFormat GOMIZZOU_DATEONLY = new SimpleDateFormat("M/dd/yyyy", locale);
    public static final DateFormat GOMIZZOU_TIMEONLY = new SimpleDateFormat("h:mm a", locale);
    public static final DateFormat DATEONLY_HUMAN_READABLE = new SimpleDateFormat("EEEE MMM d, yyyy", locale);
    public static final DateFormat DAYONLY_HUMAN_READABLE = new SimpleDateFormat("EEE, MMMM d", locale);
    public static final DateFormat FULL_HUMAN_READABLE = new SimpleDateFormat("EEEE MMM d, yyyy hh:mm a", locale);
    public static final DateFormat RALLY_AMERICA = new SimpleDateFormat("MMMM dd, yyyy", locale);
    public static final DateFormat SCHED_FROM = new SimpleDateFormat("MMM dd", locale);
    public static final DateFormat SCHED_TO = new SimpleDateFormat("MMM dd, yyyy", locale);
    public static final DateFormat DAY_OF_WEEK = new SimpleDateFormat("EEEE", locale);
    public static final DateFormat DAY_AND_DATE = new SimpleDateFormat("EEE, MMM d, yyyy", locale);

    public static final DateFormat RSS_DATE = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", locale);
    public static final DateFormat RSS_DATE_OFFSET = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss ZZZZZ", locale);
    public static final DateFormat RSS_DATE_TIMEZONE = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss zzz", locale);
    public static final DateFormat RSS_DATE_RA = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", locale);



	/* ----- FORMATTING & PASRING METHODS ----- */

    /**
     * <p>Parses the date string to a date object using the given date format.</p>
     *
     * @param date       the string to be parsed
     * @param dateFormat the format of the text to be parsed
     * @return the parsed Date object
     * @throws ParseException
     */
    public static Date parse(String date, DateFormat dateFormat) throws ParseException {
        synchronized (dateFormat) {
            return dateFormat.parse(date.trim());
        }
    }

    public static String formatForDatabase(Date date) {
        synchronized (DATABASE) {
            return DATABASE.format(date);
        }
    }

    public static String formatForGoMizzou(Date date) {
        synchronized (GOMIZZOU) {
            return GOMIZZOU.format(date);
        }
    }

    public static int timeBetweenInMinutes(long prevDate) {
        //		if (AppState.isNullOrEmpty(String.valueOf(prevDate))) {
        //			return AppState.RSS_UPDATE_DELAY +1;
        //		}

        long diff = System.currentTimeMillis() - prevDate;
        diff /= DateUtils.MINUTE_IN_MILLIS;
        //		int min = diff % 60;
        //		Log.d("DateManager Diff", "Difference in Minutes = " + diff);
        return (int) diff;
    }

    public static int timeBetweenInDays(long prevDate) {

        long diff = System.currentTimeMillis() - prevDate;
        diff /= DateUtils.DAY_IN_MILLIS;
        //		int min = diff % 60;
        //		Log.d("DateManager Diff", "Difference in Days = " + diff);
        return (int) diff;
    }

    // NOTE: not internationalized
    public static String formatAsRelativeTime(Date date) {
        StringBuffer retString = new StringBuffer();
        //		int diff = (int) ((now().getTime() - date.getTime()) / 1000);
        int diff = (int) ((System.currentTimeMillis() - date.getTime()) / 1000);
        String direction = " ago";

        if (diff < 0) {
            diff = -diff;
            direction = " from now";
        }

        int sec = diff % 60;
        diff /= 60;
        int min = diff % 60;
        diff /= 60;
        int hrs = diff % 24;
        diff /= 24;
        int days = diff % 30;
        diff /= 30;
        int months = diff % 12;
        diff /= 12;
        int years = diff;

        if (years > 0) {
            retString.append(formatUnit(years, "year"));
            if (years <= 6 && months > 0) {
                retString.append(" and ");
                retString.append(formatUnit(months, "month"));
            }
        } else if (months > 0) {
            retString.append(formatUnit(months, "month"));
        } else if (days > 0) {
            retString.append(formatUnit(days, "day"));
        } else if (hrs > 0) {
            retString.append(formatUnit(hrs, "hour"));
        } else if (min > 0) {
            retString.append(formatUnit(min, "minute"));
        } else {
            retString.append("about ");
            retString.append(formatUnit(sec, "second"));
        }

        retString.append(direction);
        return retString.toString();
    }

    public static synchronized String[] formatAsRelativeTime(long millisUntilFinished) {

        String[] dates = {"0", "0", "0", "0"/*, "0", "0"*/};

        long time = millisUntilFinished / 1000;
        long sec = time % 60;
        //        dates[3] = sec != 1 ? sec + " seconds" : sec + " second";
        dates[3] = String.format("%02ds", sec);//sec + "s";
        time /= 60;
        long min = time % 60;
        //        dates[2] = min != 1 ? min + " minutes" : min + " minute";
        dates[2] = String.format("%02dm", min);//min + "m";
        time /= 60;
        long hrs = time % 24;
        //        dates[1] = hrs != 1 ? hrs + " hours" : hrs + " hour";
        dates[1] = String.format("%02dh", hrs);//hrs + "h";
        time /= 24;
        //        long days = time % 30;
        long days = millisUntilFinished / DateUtils.DAY_IN_MILLIS;
        //        dates[0] = days != 1 ? days + " days" : days + " day";
        dates[0] = days + "d";
        //        time /= 30;
        //        long months = time % 12;
        //        dates[1] = months != 1 ? months + " months" : months + " month";
        //        time /= 12;
        //        long years = time;
        //        dates[0] = years != 1 ? years + " years" : years + " year";
        return dates;
    }


    // NOTE: not internationalized
    private static String formatUnit(int number, String unit) {
        if (number <= 1) {
            String oneWord = (unit.equals("hour")) ? "an" : "a";
            return oneWord + " " + unit;
        }
        return number + " " + unit + "s";
    }

    public static Date parseFromDatabase(String date) throws ParseException {
        synchronized (DATABASE) {
            return DATABASE.parse(date.trim());
        }
    }

    public static Date parseFromGoMizzou(String date) throws ParseException {
        synchronized (GOMIZZOU) {
            return GOMIZZOU.parse(date.trim());
        }
    }


    /* ----- DATE MANIPUTALTION METHODS ----- */
    public static Date add(int field, Date date, int value) {
        Calendar c = Calendar.getInstance(locale);
        c.setTime(date);
        c.add(field, value);
        return c.getTime();
    }

    public static int get(int field, Date date) {
        Calendar c = Calendar.getInstance(locale);
        c.setTime(date);
        return c.get(field);
    }

    public static Date now() {
        return Calendar.getInstance(locale).getTime();
    }

    public static long nowInUTC() {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
        //		Log.d("DateManager", String.valueOf(date));
        //		return date;
        //		Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime();
        //		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        //		cal.set(year + 1900, month, day, hour, minute, second);
        //		cal.getTime().getTime();
    }

    /**
     * Returns current date in the desired format
     *
     * @param dateFormat
     * @return current date in the format as specified by {@code dateFormat}
     */
    public static String now(DateFormat dateFormat) {
        return dateFormat.format(Calendar.getInstance(locale).getTime());
    }

    public static Date stripTime(Date dt) {
        Calendar c = Calendar.getInstance(locale);
        c.setTime(dt);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    public static String format(Date date, DateFormat dateFormat, int amountToAddToCalendarField, int calendarFieldToAddTo) {
        date = add(amountToAddToCalendarField, date, calendarFieldToAddTo);

        synchronized (dateFormat) {
            return dateFormat.format(date);
        }
    }

    public static String format(Date date, DateFormat dateFormat) {

        synchronized (dateFormat) {
            return dateFormat.format(date);
        }
    }

    public static String parseFromDb(String date, DateFormat dateFormat) throws ParseException {
        synchronized (dateFormat) {
            return dateFormat.format(ISO8601_DATEONLY.parse(date));
        }
    }

    /**
     * @param start StartDate in ISO-8601
     * @param end   EndDate in ISO-8601
     * @return 0 if today is before, 1 if today is between, 2 if today is after,
     * -1 if event is Cancelled, -2 if there was an error parsing a date
     */
    public static short todayIsBetween(String start, String end) {
        if ("CANCELLED".equalsIgnoreCase(start)) return -1;

        long startTime;
        long endTime;
        long todayTime;// = nowInUTC();
        //		Log.e("DATEMANAGER", "StartTime = " + youTubeStart + " EndTime = " + end);
        try {
            startTime = ISO8601_DATEONLY.parse(start).getTime();
            endTime = ISO8601_DATEONLY.parse(end).getTime();
            todayTime = ISO8601_DATEONLY.parse(now(ISO8601_DATEONLY)).getTime();
        } catch (ParseException e) {
            //			e.printStackTrace();

            return -2;
            //            return youTubeStart == end ? (short)-1 : (short)-2;
            //			return -1;
        }
        //		long todayTime = nowInUTC();
        if (todayTime > endTime) {
            return 2;
        } else if (todayTime < startTime) {
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * @param date StartDate in ISO-8601
     * @return {@code true} if today is before, {@code false} otherwise
     */
    public static boolean todayIsBefore(String date)  {
        if ("CANCELLED".equalsIgnoreCase(date)) return false;

        try {
            return ISO8601_DATEONLY.parse(date).getTime() > ISO8601_DATEONLY.parse(now(ISO8601_DATEONLY)).getTime();
        } catch (ParseException e) {
            if (BuildConfig.DEBUG)
       			e.printStackTrace();
            return false;
        }
    }
}
