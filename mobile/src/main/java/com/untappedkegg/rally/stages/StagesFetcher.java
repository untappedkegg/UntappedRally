package com.untappedkegg.rally.stages;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.data.DbUpdated;
import com.untappedkegg.rally.data.NewDataFetcher;
import com.untappedkegg.rally.data.NewDataFetcher.Callbacks;
import com.untappedkegg.rally.data.NewDataFetcher.Fetcher;
import com.untappedkegg.rally.event.DbEvent;
import com.untappedkegg.rally.util.DateManager;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StagesFetcher implements Fetcher {

    private static StagesFetcher instance = new StagesFetcher(AppState.getApplication());
    private static Context ctx;
    private static final String LOG_TAG = instance.getClass().getSimpleName();

    private final List<AsyncTask<Void, Integer, Throwable>> tasks = new ArrayList<AsyncTask<Void, Integer, Throwable>>();

    /* ----- CONSTRUCTORS ----- */
    private StagesFetcher(Context ctx) {
        StagesFetcher.ctx = ctx;
    }

    public static StagesFetcher getInstance() {
        return instance;
    }

    /* ----- INHERITED METHODS ----- */
    public void startAll(Callbacks callback, String link, String year) {
        tasks.add(NewDataFetcher.execute(new StagesParser(callback, link, year)));
    }

    public void startStageResults(Callbacks callback, String link, String eventCode, short stage, short year, boolean isResults) {
        tasks.add(NewDataFetcher.execute(new StageResultsParser(callback, link, eventCode, stage, year, isResults)));
    }

    @Override
    public boolean isRunning() {
        return NewDataFetcher.listIsRunning(tasks);
    }

    @Override
    public void interrupt() {
        NewDataFetcher.listInterrupt(tasks);

    }


    private static class StagesParser extends AsyncTask<Void, Integer, Throwable> {
        private final String function;
        private final Callbacks callback;
        private final String link;
        private final String year;

        public StagesParser(Callbacks callback, String link, String year) {
            this.callback = callback;
            this.function = "FUNC_STAGES";
            this.link = link;
            this.year = year;
        }

        @Override
        protected Throwable doInBackground(Void... arg0) {


            try {
                DbEvent.open();
                final String event = link.substring(link.lastIndexOf("/") + 1);
                DbEvent.delete_stages(event, year);

                Pattern pattern = Pattern.compile("<tr class=(.*?)</tr>", Pattern.CASE_INSENSITIVE);
                Pattern stageNum = Pattern.compile("\">([0-9]{1,2})</a>", Pattern.CASE_INSENSITIVE);
                Pattern stageTime = Pattern.compile("time\">([0-9:]{1,5})</td>", Pattern.CASE_INSENSITIVE);
                Pattern stageDist = Pattern.compile("<td>([0-9.]{1,5})</td>", Pattern.CASE_INSENSITIVE);
                Pattern stageName = Pattern.compile("</td>.*?stage/[0-9]{1,2}\">(.*?)<a></td>", Pattern.CASE_INSENSITIVE);

                Matcher matcher = pattern.matcher(NewDataFetcher.readStream(NewDataFetcher.get(link + "/stages", null).getInputStream()));

                String header = null;
                while (matcher.find()) {
                    String name = null;
                    String time = null;
                    String length = null;
                    String number = null;

                    final String find = matcher.group(0);

                    //Stage Number
                    Matcher stageMatch = stageNum.matcher(find);
                    if (stageMatch.find()) {
                        number = stageMatch.group(1);
                    }
                    // Stage Name
                    Matcher nameMatch = stageName.matcher(find);
                    if (nameMatch.find()) {
                        name = nameMatch.group(1);
                    }
                    // Stage Time
                    Matcher timeMatch = stageTime.matcher(find);
                    if (timeMatch.find()) {
                        time = timeMatch.group(1);
                    }
                    // Stage Distance
                    Matcher distMatch = stageDist.matcher(find);
                    if (distMatch.find()) {
                        length = distMatch.group(1);
                    }

                    if (AppState.isNullOrEmpty(name)) {
                        header = android.text.Html.fromHtml(find).toString();
                    } else {
                        DbEvent.stagesInsert(event, name, number, time, length, year, header);
                    }

                    //							find.replaceAll("'", "\"");
                    //							final String[] finds = matcher.group(0).replaceAll("'", "\"").split("\"");
                    //							Log.e(finds[5], finds[1].replaceAll("/assets/", String.format("%s/assets/", AppState.RA_BASE_URL)) );
                    //							DbEvent.photosInsert(finds[5], finds[1].replaceAll("/assets/", String.format("%s/assets/", AppState.RA_BASE_URL)), link.substring(link.lastIndexOf("/")+1));
                }

            } catch (Exception e) {
                Log.d(LOG_TAG, e.toString());
                e.printStackTrace();
                return e;
            } finally {
                DbEvent.close();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Throwable result) {
            Log.d(LOG_TAG, "Stages Parsing finished");
            callback.onDataFetchComplete(result, function);
        }
    }

    private static class StageResultsParser extends AsyncTask<Void, Integer, Throwable> {
        private final String function;
        private final Callbacks callback;
        private String link;
        private final short year;
        private final short curStage;
        private final String eventCode;
        private boolean isResults;

        public StageResultsParser(Callbacks callback, String link, String eventCode, short curStage, short year, boolean isResults) {
            this.callback = callback;
            this.function = "FUNC_STAGE_RESULTS";
            this.link = link;
            this.year = year;
            this.curStage = curStage;
            this.eventCode = eventCode;
            this.isResults = isResults;


        }
        @Override
        protected Throwable doInBackground(Void... arg0) {


            String table = String.format("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n<meta name=\"viewport\" content=\"initial-scale=1.0\">\n<meta charset=\"utf-8\">\n%s</head>\n<body text=\"#ffffff\" style=\"background:%s; text-align:center;\"><h3>Stage Finish Times</h3>", AppState.RALLY_AMERICA_CSS, ctx.getResources().getString(R.color.ActionBar).replaceFirst("ff", ""));
            try {
                DbEvent.open();

                if (DateManager.timeBetweenInMinutes(DbUpdated.lastUpdated_by_Source(link)) > AppState.STAGE_RESULT_DELAY) {

                    Pattern pattern = Pattern.compile("<table(.*?)</table>", Pattern.CASE_INSENSITIVE);

                    final HttpURLConnection conn = NewDataFetcher.get(link, null);
                    Matcher matcher = pattern.matcher(NewDataFetcher.readStream(conn.getInputStream()));
                    if (matcher.find()) {
                        table += matcher.group(0) + "</body>" + "</html>";
                        DbEvent.stageResultsInsert(eventCode, year, curStage, table.replaceAll("<a href=\"/driver_lookup", String.format("<a href=\"%s/driver_lookup", AppState.RA_BASE_URL)), isResults);

                    }

                    if (conn.getResponseCode() == 200) {
                        DbUpdated.updated_insert(link);
                    }
                    conn.disconnect();

                }
            } catch (Exception e) {
                Log.d(LOG_TAG, e.toString());
                e.printStackTrace();
                return e;
            } finally {
                DbEvent.close();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Throwable result) {
            Log.d(LOG_TAG, "Stages Parsing finished");
            callback.onDataFetchComplete(result, function);
        }
    }

}
