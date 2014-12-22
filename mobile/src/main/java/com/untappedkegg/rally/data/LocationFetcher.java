package com.untappedkegg.rally.data;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.untappedkegg.rally.AppState;
//import adn.GoMizzou.util.Logger;

/**
 * <p>{@link LocationListener} implementation that lets the GooglePlayServices handle the location providers, and sends any updates to the calling activity.</p>
 * <p/>
 * <p>An activity needing location updates will need to implement {@link LocationFetcher.Callbacks} and access the methods {@link #startUpdates(Callbacks)},
 * {@link #stopUpdates(Callbacks)}, and {@link #requestUpdate()}. An activity can skip implementing the {@link LocationFetcher.Callbacks} and get the current
 * location by accessing {@link #requestLocation()}.</p>
 *
 * @author russellja
 */
public class LocationFetcher implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {
    /* ----- CONSTANTS ----- */
    private static final String LOG_TAG = LocationFetcher.class.getSimpleName();
    private static final long MIN_TIME = AppState.LOCATION_UPDATE_INTERVAL_MINUTES * 60000;

	/* ----- VARIABLES ----- */
    /**
     * The {@code LocationFetcher} instance.
     */
    private static LocationFetcher instance = new LocationFetcher(AppState.getApplication());
    /**
     * The Application context.
     */
    private Context ctx;

    private Callbacks callback;
    private int handleCount;
    private boolean updatesRequested = false;
    private boolean locationFailed = false;
    private int errorCode = 0;
    private LocationClient locationClient;
    private LocationRequest locationRequest;
    /**
     * The current location.
     */
    private Location location;

	/* ----- CONSTRUCTORS ----- */

    /**
     * Does not allow outside direct instantiation, use {@link #getInstance()}.
     */
    private LocationFetcher(Context ctx) {
        this.ctx = ctx;
    }

    /**
     * <p>Use instead of the constructor.</p>
     *
     * @return {@link #instance}
     */
    public static LocationFetcher getInstance() {
        return instance;
    }

	/* ----- INHERITED METHODS ----- */
    // GooglePlayServicesClient.ConnectionCallbacks

    /**
     * <p>Sends the location updates request and calls the {@code callback} {@link Callbacks#setLocationFailed(boolean, int)} method to let it know that
     * the connection was successful.</p>
     * <p/>
     * <p>Used in the connection between the {@code LocationFetcher} and Google Play services.  Should not be accessed by activities requesting location updates.</p>
     */
    @Override
    public void onConnected(Bundle bundle) {
        if (updatesRequested) {
            locationClient.requestLocationUpdates(locationRequest, this);
        }
        setLocationFailed(false, 0);
    }

    /**
     * <p>Calls the {@code callback} {@link Callbacks#setLocationFailed(boolean, int)} method to let it know that the connection was disconnected.</p>
     * <p/>
     * <p>Used in the connection between the {@code LocationFetcher} and Google Play services.  Should not be accessed by activities requesting location updates.</p>
     */
    @Override
    public void onDisconnected() {
        setLocationFailed(true, ConnectionResult.INTERNAL_ERROR);
    }

    // GooglePlayServicesClient.OnConnectionFailedListener

    /**
     * <p>Calls the {@code callback} {@link Callbacks#setLocationFailed(boolean, int)} method to let it know that the connection failed.</p>
     * <p/>
     * <p>Used in the connection between the {@code LocationFetcher} and Google Play services.  Should not be accessed by activities requesting location updates.</p>
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        setLocationFailed(true, result.getErrorCode());
    }

    // LocationListener

    /**
     * <p>Recieves the location update, checks to see if it's better than the current {@link #location}. If so, then it becomes the current {@link #location}
     * and is sent to the {@code callback}.</p>
     * <p/>
     * <p>Used in the connection between the {@code LocationFetcher} and Google Play services.  Should not be accessed by activities requesting location updates.</p>
     */
    @Override
    public void onLocationChanged(Location location) {
        if (isBetterLocation(location)) {
            Log.d(LOG_TAG, String.format("Location changed to lat: %s / long: %s, provider: %s.", location.getLatitude(), location.getLongitude(), location.getProvider()));
            this.location = location;
            if (callback != null) {
                callback.onLocationUpdated(location.getLatitude(), location.getLongitude());
            } else {
                Log.d(LOG_TAG, "Location callback is null.");
            }
        }
    }

	/* ----- CUSTOM METHODS ----- */

    /**
     * <p>Sets up the {@code callback} connection. If it is the first connection then then {@code locationClient} is set up and the connection to Google Play
     * services is opened.  Calls {@link #requestUpdate()} to send an initial update to the {@code callback}.</p>
     *
     * @param callback the calling activity
     */
    public synchronized void startUpdates(Callbacks callback) {
        try {
            this.callback = callback;
        } catch (ClassCastException e) {
            throw new ClassCastException(callback.toString() + " must implement " + Callbacks.class.toString() + ".");
        }

        if (handleCount == 0) {
            Log.d(LOG_TAG, "Starting location updates.");
            init();
            updatesRequested = true;
        }
        handleCount++;
        Log.d(LOG_TAG, String.format("Added location handle %s. Adapter now reports %d handles.", callback.toString(), handleCount));

        requestUpdate();
    }

    /**
     * <p>Removes the {@code callback} connection.  If there are no more connections then the location updates are removed from the {@code locationClient}
     * and the connection to Google Play services is closed.</p>
     *
     * @param callback the calling activity
     */
    public synchronized void stopUpdates(Callbacks callback) {
        if (this.callback != null) {
            if (this.callback.equals(callback)) {
                this.callback = null;
            }
        }

        handleCount--;
        Log.d(LOG_TAG, String.format("Removed location handle. Adapter now reports %d handles.", handleCount));

        if (handleCount == 0) {
            if (locationClient.isConnected()) {
                locationClient.removeLocationUpdates(this);
            }
            locationClient.disconnect();
            Log.d(LOG_TAG, "Connection to Google Play services closed.");
        }
    }

    /**
     * <p>Sends the {@link #location} to the {@code callback} if it has been found, or sends the {@link #locationFailed} and {@link errorCode} if the location has not been found.</p>
     */
    public void requestUpdate() {
        if (location != null && callback != null) {
            callback.onLocationUpdated(location.getLatitude(), location.getLongitude());
        } else if (location == null && callback != null) {
            callback.setLocationFailed(locationFailed, errorCode);
        }
    }

    /**
     * <p>An activity can skip implementing the {@link LocationFetcher.Callbacks} and get the last location update (or null if a location hasn't been found yet).
     * If there is an activity receiving updates via the {@link LocationFetcher.Callbacks} currently, then it will likely be up-to-date.</p>
     *
     * @return {@link #location}
     */
    public Location requestLocation() {
        return location;
    }

    /**
     * <p>Sets the member variables for failed location fetches and sends them to the callback.</p>
     *
     * @param locationFailed true if the location fetch failed, false otherwise
     * @param errorCode      the code for the error that caused the fetch to fail
     */
    public void setLocationFailed(boolean locationFailed, int errorCode) {
        Log.d(LOG_TAG, String.format("Location fetch %s with error code %d.", (locationFailed ? "failed" : "succeeded"), errorCode));

        this.locationFailed = locationFailed;
        this.errorCode = errorCode;
        if (callback != null) {
            callback.setLocationFailed(locationFailed, errorCode);
        }
    }

    /**
     * <p>Sets up the {@code locationClient} and connects to Google Play services.</p>
     */
    private void init() {
        locationClient = new LocationClient(ctx, this, this);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(MIN_TIME);

        locationClient.connect();
    }

    /**
     * <p>Checks to see if the {@code location} is better than the current {@link #location}.</p>
     *
     * @param location the location to be checked
     * @return {@code true} if the {@code location} is better or if the current {@link #location} is null, {@code false} otherwise
     */
    private boolean isBetterLocation(Location location) {
        if (this.location == null) {
            return true;
        }

        long timeDelta = location.getTime() - this.location.getTime();
        boolean isNewer = timeDelta > 0;
        boolean isSignificantlyNewer = timeDelta > MIN_TIME;
        boolean isSignificantlyOlder = timeDelta < -MIN_TIME;

        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }

        float accuracyDelta = (location.getAccuracy() - this.location.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        boolean isFromSameProvider = isSameProvider(location.getProvider(), this.location.getProvider());

        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * <p>Checks to see if the two providers are the same.</p>
     *
     * @param provider1 first provider
     * @param provider2 second provider
     * @return {@code true} if they are equal, {@code false} otherwise or if {@code provider1} is null
     */
    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    /**
     * <p>The callbacks interface to communicate with an activity requesting location updates.</p>
     *
     * @author russellja
     */
    public interface Callbacks {
        /**
         * <p>Lets the calling activity know that the location has been updated.</p>
         *
         * @param latitude  the new location latitude
         * @param longitude the new location longitude
         */
        public void onLocationUpdated(double latitude, double longitude);

        /**
         * <p>Lets the calling activity know whether the location fetch has failed .</p>
         *
         * @param locationFailed {@code true} if the location fetch failed, {@code false} if it succeeded
         * @param errorCode      the errorCode if the fetch failed, 0 if it succeeded
         */
        public void setLocationFailed(boolean locationFailed, int errorCode);
    }
}
