package com.untappedkegg.rally.news;

import android.os.AsyncTask;
import android.util.Log;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.data.NewDataFetcher;
import com.untappedkegg.rally.data.NewDataFetcher.Callbacks;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.SAXParserFactory;

/**
 * Created by UntappedKegg on 2/15/15.
 */
public class NewsFetcher implements NewDataFetcher.Fetcher {

    private static final NewsFetcher instance = new NewsFetcher();

    private static final String LOG_TAG = instance.getClass().getSimpleName();

    private final List<AsyncTask<Void, Integer, Throwable>> tasks = new ArrayList<AsyncTask<Void, Integer, Throwable>>();

    public static NewsFetcher getInstance() {
        return instance;
    }

    @Override
    public boolean isRunning() {
        return NewDataFetcher.listIsRunning(tasks);
    }


    @Override
    public void interrupt() {
        // TODO Auto-generated method stub

    }

    public void news_start(Callbacks callback) {
        if (!isRunning()) {
            // Delete old Items once, before fetching new ones
            DbNews.deleteOldItems();

            tasks.add(NewDataFetcher.execute(new NewsParser(callback, AppState.SOURCE_RALLY_AMERICA, AppState.RSS_RALLY_MERICA)));
            tasks.add(NewDataFetcher.execute(new NewsParser(callback, AppState.SOURCE_IRALLY, AppState.RSS_IRALLY)));

            //Fetch news for each user selected event
            final Set<String> feeds = AppState.getSettings().getStringSet("event_feeds", null);
            if (feeds != null) {
                for (String feed : feeds) {
                    tasks.add(NewDataFetcher.execute(new NewsParser(callback, feed, AppState.NEWS_MAP.get(feed))));
                }
            }
        }
    }

    /* ----- NESTED CLASSES ----- */
    // Parsers

    private static class NewsParser extends AsyncTask<Void, Integer, Throwable> {
        private final String uri;
        private final Callbacks callback;
        private final String link;

        public NewsParser(Callbacks callback, String uri, String link) {
            this.callback = callback;
            this.uri = uri;
            this.link = link;
        }

        @Override
        protected Throwable doInBackground(Void... arg0) {
            DbNews.open();

                try {
                    HttpURLConnection conn = NewDataFetcher.get(link);
                    if (conn.getResponseCode() == 200) {
                        SAXParserFactory.newInstance().newSAXParser().parse(conn.getInputStream(), new SAXNews(uri));
                    }
                    conn.disconnect();
                } catch (Exception e) {
                    Log.d(LOG_TAG, e.toString());
                    return e;
                } finally {
                    DbNews.close();
                }

            return null;
        }

        @Override
        protected void onPostExecute(Throwable result) {
            Log.d(LOG_TAG, "News Parsing finished for: " + uri);
            callback.onDataFetchComplete(result, AppState.MOD_NEWS);
        }
    }

}
