package com.untappedkegg.rally.social;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.data.DbUpdated;
import com.untappedkegg.rally.data.NewDataFetcher;
import com.untappedkegg.rally.data.NewDataFetcher.Callbacks;
import com.untappedkegg.rally.data.NewDataFetcher.Fetcher;
import com.untappedkegg.rally.util.DateManager;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

public class SocialFetcher implements Fetcher {


    private static final SocialFetcher instance = new SocialFetcher(AppState.getApplication());
    private static Context ctx;
    private static final String LOG_TAG = instance.getClass().getSimpleName();

    private final List<AsyncTask<Void, Integer, Throwable>> tasks = new ArrayList<AsyncTask<Void, Integer, Throwable>>();

    /* ----- CONSTRUCTORS ----- */
    private SocialFetcher(Context ctx) {
        SocialFetcher.ctx = ctx;
    }

    public static SocialFetcher getInstance() {
        return instance;
    }

    /* ----- INHERITED METHODS ----- */
    public void youTubeStart(Callbacks callback, boolean overrideCache) {
        if (!youTubeIsRunning()) {
            tasks.add(NewDataFetcher.execute(new YtTask(callback, overrideCache)));
        }

    }

    @Override
    public boolean isRunning() {
        return NewDataFetcher.listIsRunning(tasks);
    }

    public boolean youTubeIsRunning() {
        return NewDataFetcher.taskIsRunning(tasks, YtTask.class);
    }

    @Override
    public void interrupt() {
        // TODO Auto-generated method stub

    }

	/* ----- CUSTOM METHODS ----- */

    /* ----- NESTED CLASSES ----- */
    private static class YtTask extends AsyncTask<Void, Integer, Throwable> {
        private final Callbacks callback;
        private final String function = AppState.MOD_YOUTUBE;
        private final String link = AppState.YOUTUBE_RA;
        private final boolean overrideCache;

        public YtTask(Callbacks callback, boolean overrideCache) {
            this.callback = callback;
            this.overrideCache = overrideCache;
        }

        @Override
        protected Throwable doInBackground(Void... params) {
            try {
                DbUpdated.open();

                if (NewDataFetcher.isInternetConnected() && (DateManager.timeBetweenInDays(DbUpdated.lastUpdated_by_Source(AppState.MOD_YOUTUBE)) > AppState.YT_UPDATE_DELAY || overrideCache)) {
                    DbSocial.delete(DbSocial.YOUTUBE_TABLE);
                    Log.w(LOG_TAG, "Retrieving from: " + link);
                    final HttpURLConnection connection = NewDataFetcher.get(link, null);
                    if (connection.getResponseCode() == 200) {
                        try {
                            //							Log.e(getClass().getCanonicalName(), NewDataFetcher.readStream(connection.getInputStream()));
                            SAXParserFactory.newInstance().newSAXParser().parse(connection.getInputStream(), new SaxYouTube(function));
                            DbUpdated.updated_insert(AppState.MOD_YOUTUBE);
                            //							DbCommon.cache_insert(link, function, query);
                        } finally {
                            connection.disconnect();
                        }
                    }
                }
                Log.d(LOG_TAG, "Successfully finished parsing.");

                return null;
            } catch (Exception e) {
                if (AppState.DEBUG) {
                    e.printStackTrace();
                    return e;
                }
            } finally {
                DbUpdated.close();
                Log.d(this.getClass().getSimpleName(), "Exiting.");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Throwable result) {
            if (callback != null) {
                callback.onDataFetchComplete(result, function);
            }
        }
    }

}
