package com.untappedkegg.rally.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.util.Log;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.news.DbNews;
import com.untappedkegg.rally.news.SAXNews;
import com.untappedkegg.rally.schedule.SAXSchedule;
import com.untappedkegg.rally.util.DateManager;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

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

    private AsyncTask<Void, Void, Throwable> newsThread;
    private AsyncTask<Void, Void, Throwable> eventsThread;
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
    // HTTP Methods //
    public static InputStream doGet(String url) throws IOException {

        Log.i(LOG_TAG + " doGet", "Retrieving from " + url);

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        return httpClient.execute(request).getEntity().getContent();
    }

    public static InputStream doPost(String url, String postMsg) throws IOException {

        Log.i(LOG_TAG, "Retrieving from " + url + " with post arguments " + postMsg);

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost request = new HttpPost(url);
        StringEntity postEntity = new StringEntity(postMsg);
        request.setHeader("Content-Type", "application/x-www-form-urlencoded");
        postEntity.setContentType("application/x-www-form-urlencoded");
        request.setEntity(postEntity);

        return httpClient.execute(request).getEntity().getContent();
    }

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
    // global
    public void global_interrupt() {
        if (newsThread != null) newsThread.cancel(true);

        //Don't cancel the schedule thread because it is run so infrequently
        //if (scheduleThread != null)	scheduleThread.cancel(true);

    }

    // AsyncTask Methods
    private static <P, R> AsyncTask<Void, P, R> executeParallel(AsyncTask<Void, P, R> task) {
        //		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
        //			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /* ----- NESTED CLASSES ----- */
    public static class FetchTask {
        public final String link;
        //			public String function;
        //			public String query;
        public final BaseSAX sax;
        //			public int id;

        public FetchTask(String link, /*String function, String query,*/ BaseSAX sax/*, int id*/) {
            this.link = link;
            //				this.function = function;
            //				this.query = query;
            this.sax = sax;
            //				this.id = id;
        }
    }


    // News
    public boolean news_isRunning() {
        return newsThread != null && newsThread.getStatus() != Status.FINISHED;
    }

    public void news_start(String uri, Callbacks callback) {
        if (!news_isRunning()) {
            FetchTask[] tasks = new FetchTask[]{
                    //					new FetchTask(AppState.RSS_OFFICIAL_WRC, null, null, new SAXNews(AppState.SOURCE_WRC_COM), -1),
                    //					new FetchTask(AppState.RSS_BEST_OF, null, null, new SAXNews(AppState.SOURCE_BEST_OF_RALLY), -1),
                    //					new FetchTask(AppState.RSS_CITROEN, null, null, new SAXNews(AppState.SOURCE_CITROEN), -1),
                    new FetchTask(AppState.RSS_RALLY_MERICA, new SAXNews(AppState.SOURCE_RALLY_AMERICA)),
                    //Irally should go last because they have errors fairly regularly
                    new FetchTask(AppState.RSS_IRALLY, new SAXNews(AppState.SOURCE_IRALLY))};
            newsThread = executeParallel(new NewsParser(uri, callback, tasks));
        }
    }


    public void news_interrupt() {
        if (newsThread != null) newsThread.cancel(true);
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
        //		}
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
//            FetchTask[] tasks = new FetchTask[]{new FetchTask(AppState.EGG_CAL_XML, new SAXSchedule()),};
            scheduleThread = executeParallel(new ScheduleParser(callback, isOverride));
        }
    }


    public void sched_interrupt() {
        if (scheduleThread != null) scheduleThread.cancel(true);
    }

/*	public void events_start(String uri, Callbacks callback) {
		eventsThread = executeParallel(new EventsParser(uri, callback));
	}*/

    /* ----- NESTED INTERFACES ----- */
    public interface Callbacks {
        public void onDataFetchComplete(Throwable throwable, String parser);
    }

	/* ----- NESTED CLASSES ----- */
    // Parsers


    private static class NewsParser extends AsyncTask<Void, Void, Throwable> {
        private final String uri;
        private final FetchTask[] tasks;
        private final Callbacks callback;

        public NewsParser(String uri, Callbacks callback, FetchTask[] tasks) {
            this.callback = callback;
            this.tasks = tasks;
            this.uri = uri;
        }

        @Override
        protected Throwable doInBackground(Void... arg0) {
            DbNews.open();


            for (FetchTask task : tasks) {
                try {
                    HttpURLConnection conn = NewDataFetcher.get(task.link, null);
                    if (conn.getResponseCode() == 200) {
                        DbNews.deleteOldItems();

                        SAXParserFactory.newInstance().newSAXParser().parse(conn.getInputStream(), task.sax);
                    }
                    //				} catch (SAXException e) {
                    //					e.printStackTrace();
                    conn.disconnect();
                } catch (Exception e) {
                    Log.d(LOG_TAG, e.toString());
                }

            }
            DbNews.close();

            return null;
        }

        @Override
        protected void onPostExecute(Throwable result) {
            Log.d(LOG_TAG, "News Parsing finished");
            callback.onDataFetchComplete(result, uri);
        }
    }

    private static class ScheduleParser extends AsyncTask<Void, Void, Throwable> {
        private final String uri = AppState.MOD_SCHED;
        private final boolean isOverride;
        private final Callbacks callback;

        public ScheduleParser(Callbacks callback, boolean isOverride) {
            this.callback = callback;
            this.isOverride = isOverride;
        }

        //		@Override
        //		protected void onPreExecute() {
        //			callback.onDataFetchComplete(null, uri);
        //		}

        @Override
        protected Throwable doInBackground(Void... arg0) {
            DbUpdated.open();
            if (DateManager.timeBetweenInDays(DbUpdated.lastUpdated_by_Source(AppState.MOD_SCHED)) > AppState.CAL_UPDATE_DELAY || isOverride) {

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
        //		private FetchTask[] tasks;
        private final Callbacks callback;
        private final String link;
        private final String fileName;

        public StandingsParser(Callbacks callback, String link, String function, String fileName) {
            this.callback = callback;
            //			this.tasks = tasks;
            this.function = function;
            this.link = link;
            this.fileName = fileName;


        }

        //		@Override
        //		protected void onPreExecute() {
        //			callback.onDataFetchComplete(null, uri);
        //		}

        @Override
        protected Throwable doInBackground(Void... arg0) {
            FileOutputStream outputStream;

            //			for(FetchTask task : tasks) {
            //				this.uri = task.function;
            String table = String.format("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n<meta name=\"viewport\" content=\"initial-scale=1.0\">\n<meta charset=\"utf-8\">%s\n</head>\n<body text=\"#ffffff\" style=\"background:%s; text-align:center;\">", AppState.RALLY_AMERICA_CSS, ctx.getResources().getString(R.color.ActionBar).replaceFirst("ff", ""));
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
                File file = ctx.getFileStreamPath(fileName);
                if (file.exists()) {
                    ctx.deleteFile(fileName);
                }
                // Otherwise, creates a file to store the feedback
                Log.i(LOG_TAG, "Writing to file: " + fileName);
                outputStream = ctx.openFileOutput(fileName, Context.MODE_PRIVATE);
                outputStream.write(table.getBytes());


            } catch (Exception e) {
                Log.d(LOG_TAG, e.toString());
                e.printStackTrace();
                //				}  finally {
                //					DbNews.close();
            }

            //			}
            return null;
        }

        @Override
        protected void onPostExecute(Throwable result) {
            Log.d(LOG_TAG, "Generic Parsing finished");
            callback.onDataFetchComplete(result, function);
        }
    }

/*	private static class EventsParser extends AsyncTask<Void, Void, Throwable> {
		private String uri = "generic_parser";
		private FetchTask[] tasks;
		private Callbacks callback;

		public EventsParser(String uri, Callbacks callback) {
			this.callback = callback;
			this.tasks = tasks;
			this.uri = uri;
		}
		@Override
		protected Throwable doInBackground(Void... arg0) {
			try {
				DbSchedule.open();
//				DbSchedule.deleteAllByUri(uri);
//					for(FetchTask task : tasks) {
						String foo = readStream(doGet("http://www.ralsys.com/irally_xml/events.php"));
						String ret = new String(foo.getBytes("ascii"), "utf-8");

						String[] entry = ret.split("<");
						int i = 0;
						int entlen = entry.length;
						while (i<entlen) {
							String[] records = entry[i].split("~");
								if (AppState.SERIES_WRC.equals(records[5]) || AppState.SERIES_RA.equals(records[5])){
									//records[0]=event_id, records[1]=event_name, records[2]=events_country
									//records[4]=Date_range(22 - 23 February), records[5]=series_id
									//records[6]=seq_num
									String series = "";
									if (AppState.SERIES_WRC.equals(records[5])) {
										series = AppState.WRC;
									} else {
										series = AppState.RA;
									}
									String[] date = records[4].split(" ");

									DbSchedule.create(records[0], records[1].replace('?', 'ñ'), records[8], records[7], records[4], records[2], series);
								Log.e(LOG_TAG, String.format("event_id=%s event_name=%s event_country=%s " +
															"date_range=%s series_id=%s seq_num=%s",
															records[0], records[1].replace('?', 'ñ'), records[2], records[4], records[5], records[6]));

							}

							i++;
						}

//					}

			} catch (Exception e) {
//				 Log.d(LOG_TAG, e.toString());
				e.printStackTrace();
			}  finally {
				DbSchedule.close();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Throwable result) {
			 Log.d(LOG_TAG, "News Parsing finished");
			callback.onDataFetchComplete(result, uri);
		}
	}*/

}
