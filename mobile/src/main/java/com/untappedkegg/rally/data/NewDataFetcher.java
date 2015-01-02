package com.untappedkegg.rally.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.util.Log;

import com.untappedkegg.rally.AppState;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ConcurrentModificationException;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

public class NewDataFetcher {
    /* ----- CONSTANTS ----- */
    private static final String LOG_TAG = NewDataFetcher.class.getSimpleName();

	/* ----- CONSTRUCTORS ----- */

    /**
     * Does not allow outside direct instantiation.
     */
    private NewDataFetcher() {
    }

	/* ----- CUSTOM METHODS ----- */
    // HTTP Methods

    /**
     * <p>Performs the HTTP GET operation.  If the credentials aren't null, then Basic Authentication is used to send them.</p>
     * <p/>
     * <p>Note that this works for both http and https connections.</p>
     *
     * @param link        the url to perform the get on
     * @param credentials the username and password credentials to use
     * @return the open https connection
     * @throws IOException
     */
    public static HttpURLConnection get(String link, UsernamePasswordCredentials credentials) throws IOException {
        //		startDns();
        Log.d(LOG_TAG, String.format("Retrieving from %s with credentials %s.", link, credentials));

        final URL url = new URL(link);
        HttpURLConnection connection = null;

        if (link.startsWith("https")) {
            try {
                final SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, null, new java.security.SecureRandom());
                connection = (HttpsURLConnection) url.openConnection();
                ((HttpsURLConnection) connection).setSSLSocketFactory(context.getSocketFactory());
            } catch (NoSuchAlgorithmException e) {
                if (AppState.DEBUG) {
                    e.printStackTrace();
                }
            } catch (KeyManagementException e) {
                if (AppState.DEBUG) {
                    e.printStackTrace();
                }
            }
        } else {
            connection = (HttpURLConnection) url.openConnection();
        }

        if (credentials != null) {
            final String user = credentials.getUserName();
            final String pass = credentials.getPassword();
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, pass.toCharArray());
                }
            });
        }

        return connection;
    }

    /**
     * <p>Performs the HTTP POST operation.  If the credentials aren't null, then Basic Authentication is used to send them.</p>
     * <p/>
     * <p>Note that this works for both http and https connections.</p>
     *
     * @param link        the url to perform the get on
     * @param message     the message to be sent
     * @param credentials the username and password credentials to use
     * @return the open https connection
     * @throws IOException
     */
    public static HttpURLConnection post(String link, String message, UsernamePasswordCredentials credentials) throws IOException {
        //		startDns();
        Log.d(LOG_TAG, String.format("Retrieving from %s with credentials %s and post arguments %s.", link, credentials, message));

        final URL url = new URL(link);
        HttpURLConnection connection = null;

        if (link.startsWith("https")) {
            try {
                final SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, null, new java.security.SecureRandom());
                connection = (HttpsURLConnection) url.openConnection();
                ((HttpsURLConnection) connection).setSSLSocketFactory(context.getSocketFactory());
            } catch (NoSuchAlgorithmException e) {
                if (AppState.DEBUG) {
                    e.printStackTrace();
                }
            } catch (KeyManagementException e) {
                if (AppState.DEBUG) {
                    e.printStackTrace();
                }
            }
        } else {
            connection = (HttpURLConnection) url.openConnection();
        }

        if (credentials != null) {
            final String user = credentials.getUserName();
            final String pass = credentials.getPassword();
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, pass.toCharArray());
                }
            });
        }

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length", "" + Integer.toString(message.getBytes().length));
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        final DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(message);
        wr.flush();
        wr.close();

        return connection;
    }

    /**
     * <p>Converts the input stream returned from the url connection to a string.</p>
     *
     * @param stream the url connection response stream
     * @return the response string
     * @throws IOException
     */
    public static String readStream(InputStream stream) throws IOException {
        final InputStreamReader isr = new InputStreamReader(stream);
        final BufferedReader br = new BufferedReader(isr, 8192);

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

    /**
     * <p>Parses a string to a JSONObject.</p>
     *
     * @param string the JSON string
     * @return the parsed JSONObject
     * @see #parseJSONData(InputStream)
     * @see #parseJSONArray(String)
     * @see #parseJSONArray(InputStream)
     */
    public static JSONObject parseJSONData(String string) {
        JSONObject JSONObject = null;
        try {
            JSONObject = new JSONObject(string);
        } catch (JSONException e) {
            if (AppState.DEBUG) {
                e.printStackTrace();
            }
        }
        return JSONObject;
    }

    /**
     * <p>Works the same as {@link #parseJSONData(String)} except that it reads the input stream to a string first.</p>
     *
     * @param stream the input stream containing the JSON string
     * @return the parsed JSON object
     * @throws IOException
     * @see #parseJSONData(String)
     * @see #parseJSONArray(String)
     * @see #parseJSONArray(InputStream)
     */
    public static JSONObject parseJSONData(InputStream stream) throws IOException {
        return parseJSONData(readStream(stream));
    }

    /**
     * <p>Parses a string to a JSONArray.</p>
     *
     * @param string the JSON string
     * @return the parsed JSONArray
     * @see #parseJSONData(String)
     * @see #parseJSONData(InputStream)
     * @see #parseJSONArray(InputStream)
     */
    public static JSONArray parseJSONArray(String string) {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(string);
        } catch (JSONException e) {
            if (AppState.DEBUG) {
                e.printStackTrace();
            }
        }
        return jsonArray;
    }

    /**
     * <p>Works the same as {@link #parseJSONArray(String)} except that it reads the input stream to a string first.</p>
     *
     * @param stream the input stream containing the JSON string
     * @return the parsed JSONArray
     * @throws IOException
     * @see #parseJSONData(String)
     * @see #parseJSONData(InputStream)
     * @see #parseJSONArray(String)
     */
    public static JSONArray parseJSONArray(InputStream stream) throws IOException {
        return parseJSONArray(readStream(stream));
    }

    public static JSONObject parseJSONData(HttpURLConnection conn) throws IOException {
        return parseJSONData(readStream(conn.getInputStream()));
    }

    public static JSONArray parseJSONArray(HttpURLConnection conn) throws IOException {
        return parseJSONArray(readStream(conn.getInputStream()));
    }


    /**
     * <p>Checks if the device has network connection.</p>
     *
     * @return {@code true} if the device is connected, {@code false} otherwise
     */
    public static boolean isInternetConnected() {
        final ConnectivityManager conMgr = (ConnectivityManager) AppState.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        return (conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isAvailable() && conMgr.getActiveNetworkInfo().isConnected());
    }

    public static boolean isWifiConnected() {
        final NetworkInfo netInfo = ((ConnectivityManager) AppState.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return netInfo.isAvailable() && netInfo.isConnected();
    }

    // AsyncTask Methods

    /**
     * <p>Forces the {@link AsyncTask} to run parallel to running tasks, no matter the api version of the device.</p>
     *
     * @param task the task to be started
     * @return running task
     */
    public static AsyncTask<Void, Integer, Throwable> execute(AsyncTask<Void, Integer, Throwable> task) {
        return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * <p>Static method that can be used by a fetcher that uses a list to keep track of the running tasks
     * to determine if any of tasks are running.</p>
     *
     * @param tasks the list of tasks to check
     * @return true if any of the tasks in the list is running, false otherwise
     */
    public static boolean listIsRunning(List<AsyncTask<Void, Integer, Throwable>> tasks) {
        boolean isRunning = false;

        try {
            for (AsyncTask<Void, Integer, Throwable> task : tasks) {
                if (task != null) {
                    if (task.getStatus() != Status.FINISHED) {
                        isRunning = true;
                    }
                }
            }
        } catch (ConcurrentModificationException e) {
            return true;
        }
        return isRunning;
    }

    /**
     * <p>Static method that can be used by a fetcher that uses a list to keep track of the running tasks
     * to determine if the specified task is running.</p>
     *
     * @param tasks the list of tasks to check
     * @return true if the task in the list is running, false otherwise
     */
    public static boolean taskIsRunning(List<AsyncTask<Void, Integer, Throwable>> tasks, Object parser) {

        try {
            for (AsyncTask<Void, Integer, Throwable> task : tasks) {
                if (task != null) {
                    if (task.getClass().isInstance(parser) && task.getStatus() != Status.FINISHED) {
                        return true;
                    }
                }
            }
        } catch (ConcurrentModificationException e) {
            return true;
        }
        return false;
    }

    /**
     * <p>Static method that can be used by a fetcher that uses a list to keep track of the running tasks to interrupt all running tasks.</p>
     *
     * @param tasks the list of tasks to interrupt
     */
    public static void listInterrupt(List<AsyncTask<Void, Integer, Throwable>> tasks) {
        for (AsyncTask<Void, Integer, Throwable> task : tasks) {
            if (task != null) {
                task.cancel(true);
            }
        }
        tasks.clear();
    }

	/* ----- NESTED INTERFACES ----- */

    /**
     * <p>Interface that a fetcher needs to implement.</p>
     *
     * @author russellja
     */
    public interface Fetcher {
        /**
         * <p>Implementation should determine if any tasks are running within the fetcher.</p>
         *
         * @return true if the fetcher is running, false otherwise
         */
        public boolean isRunning();

        /**
         * <p>Implementation should interrupt all running tasks within the fetcher.</p>
         */
        public void interrupt();
    }

    /**
     * <p>Interface that the activity calling the fetcher needs to implement.</p>
     *
     * @author russellja
     */
    public interface Callbacks {
        /**
         * <p>Tells the calling activity that the data fetch is complete.</p>
         *
         * @param throwable the thrown error
         * @param key       the key of the task that was completed
         */
        public void onDataFetchComplete(Throwable throwable, String key);
    }
}
