package com.untappedkegg.rally.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.util.Log;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.BuildConfig;
import com.untappedkegg.rally.schedule.SAXSchedule;
import com.untappedkegg.rally.util.DateManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParserFactory;

public class DataFetcher {
    /* CONSTANTS */
    private static final String LOG_TAG = DataFetcher.class.getSimpleName();

    /* VARIABLES */
    private static final DataFetcher instance = new DataFetcher(AppState.getApplication());
    private static Context ctx;

    private AsyncTask<Void, Void, Throwable> scheduleThread;
    private AsyncTask<Void, Void, Throwable> standingsThread;

    /* CONSTRUCTORS */
    private DataFetcher(Context ctx) {
        DataFetcher.ctx = ctx;
    }

    public static DataFetcher getInstance() {
        return instance;
    }

    /* CUSTOM METHODS */

    public static String readStream(InputStream stream) throws IOException {
        InputStreamReader isr = new InputStreamReader(stream);
        BufferedReader br = new BufferedReader(isr, 8192);

        try {
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = br.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } finally {
            br.close();
            isr.close();
            stream.close();
        }
    }

    public static boolean isInternetConnected() {
        ConnectivityManager conMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isAvailable() && conMgr.getActiveNetworkInfo().isConnected());
    }

    // Parser Control Methods //
    // AsyncTask Methods
    private static <P, R> AsyncTask<Void, P, R> executeParallel(AsyncTask<Void, P, R> task) {
        return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    //Overview Standing
    public boolean standings_isRunning() {
        return standingsThread != null && standingsThread.getStatus() != Status.FINISHED;
    }

    public void standings_start(Callbacks callback, String link, String fileName) {
        if (link == null) {
            link = String.format(AppState.RA_STANDINGS, 1, 0, Calendar.getInstance().get(Calendar.YEAR));
        }
        standingsThread = executeParallel(new StandingsParser(callback, link, AppState.FUNC_RA_STAND, fileName));
    }


    public void standings_interrupt() {
        if (standingsThread != null) standingsThread.cancel(true);
    }

    //Schedule
    public boolean sched_isRunning() {
        return scheduleThread != null && scheduleThread.getStatus() != Status.FINISHED;
    }

    public void sched_start(Callbacks callback, boolean isOverride) {
        if (!sched_isRunning()) {
            scheduleThread = executeParallel(new ScheduleParser(callback, isOverride));
        }
    }

    public void sched_interrupt() {
        if (scheduleThread != null) scheduleThread.cancel(true);
    }


    /* ----- NESTED INTERFACES ----- */
    public interface Callbacks {
        void onDataFetchComplete(Throwable throwable, String parser);
    }

	/* ----- NESTED CLASSES ----- */
    // Parsers

    private static class ScheduleParser extends AsyncTask<Void, Void, Throwable> {
        private final String uri = AppState.MOD_SCHED;
        private final boolean isOverride;
        private final Callbacks callback;

        public ScheduleParser(Callbacks callback, boolean isOverride) {
            this.callback = callback;
            this.isOverride = isOverride;
        }

        @Override
        protected Throwable doInBackground(Void... arg0) {
            DbUpdated.open();
            if (isOverride || DateManager.timeBetweenInDays(DbUpdated.lastUpdated_by_Source(AppState.MOD_SCHED)) > AppState.CAL_UPDATE_DELAY) {

                try {
                    HttpURLConnection conn = NewDataFetcher.get(AppState.EGG_CAL_XML, null);
                    if (conn.getResponseCode() == 200) {
                        SAXParserFactory.newInstance().newSAXParser().parse(conn.getInputStream(), new SAXSchedule());
                        DbUpdated.updated_insert(AppState.MOD_SCHED);
                    } else {
                        conn.disconnect();
                    }

                } catch (Exception e) {
                    Log.d(LOG_TAG, e.toString());
                    Log.w(LOG_TAG, "Error retrieving from: " + AppState.EGG_CAL_XML);
                    return e;
                } finally {
                    DbUpdated.close();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Throwable result) {
            Log.d(LOG_TAG, "Generic Parsing finished");
            callback.onDataFetchComplete(result, uri);
            if ( result == null ) {
                AppState.setNextNotification();
            }
        }
    }

    private static class StandingsParser extends AsyncTask<Void, Void, Throwable> {
        private final String function;
        private final Callbacks callback;
        private final String link;
        private final String fileName;

        public StandingsParser(Callbacks callback, String link, String function, String fileName) {
            this.callback = callback;
            this.function = function;
            this.link = link;
            this.fileName = fileName;
        }

        @Override
        protected Throwable doInBackground(Void... arg0) {

            String table = String.format("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n<meta name=\"viewport\" content=\"initial-scale=1.0\">\n<meta charset=\"utf-8\">%s\n</head>\n<body text=\"#ffffff\" text-align:center;\">", AppState.RALLY_AMERICA_CSS);
            try {
                Pattern pattern = Pattern.compile("<table(.*?)</table>", Pattern.CASE_INSENSITIVE);
                //					Matcher matcher = pattern.matcher(readStream(doGet(link)));
                Matcher matcher = pattern.matcher(readStream(NewDataFetcher.get(link, null).getInputStream()));
                if (matcher.find()) {
                    //						if (function.equalsIgnoreCase(AppState.FUNC_RA_STAND)) {
                    //							table += "<h3 align=\"center\">Rally America Standings</h3>\n";
                    table += matcher.group(0).replaceAll("<a href=\"/champ_standings", String.format("<a href=\"%s/champ_standings", AppState.RA_BASE_URL));
                    //to remove the 'Detail' hyperlink uncomment this line
                    //							table += matcher.group(0).replaceAll("<td><a href=\"/champ_standings.*?</a></td>", "").replaceAll("<td>Detail</td>", "");
                    //						} else {
                    //							table += "<h3 align=\"center\">WRC Standings</h3>\n";
                    //							table +=  matcher.group(0).replaceAll("href=\"/en/wrc/drivers.*?\"", "");
                    //						}
                }
                table += "</body>" + "</html>";
                //						SAXParserFactory.newInstance().newSAXParser().parse(doGet(task.link), task.sax);
                final File file = ctx.getFileStreamPath(fileName);
                if (file.exists()) {
                    ctx.deleteFile(fileName);
                }
                // Otherwise, creates a file to store the standings
                if (BuildConfig.DEBUG)
                Log.i(LOG_TAG, "Writing to file: " + fileName);
                final FileOutputStream outputStream = ctx.openFileOutput(fileName, Context.MODE_PRIVATE);
                outputStream.write(table.getBytes());


            } catch (Exception e) {
                if (BuildConfig.DEBUG)
                e.printStackTrace();
                return e;
            }

            return null;
        }

        private void removeOldFiles() {
             final String [] fileParts = fileName.split("_");
            final int year = Short.parseShort(fileParts[fileParts.length -1]) - 6;
        }

        @Override
        protected void onPostExecute(Throwable result) {
            Log.d(LOG_TAG, "Generic Parsing finished");
            callback.onDataFetchComplete(result, function);
        }
    }

}
