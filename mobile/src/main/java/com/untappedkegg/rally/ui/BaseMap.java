package com.untappedkegg.rally.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.data.BaseDbAccessor;
import com.untappedkegg.rally.ui.loaders.SimpleCursorLoader;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * <p>
 * Base implementation for a {@link SupportMapFragment} that uses CursorLoaders
 * with automatic, periodic requerying to place markers on the map.
 * </p>
 *
 * @author russellja
 */
public abstract class BaseMap extends SupportMapFragment implements LoaderCallbacks<Cursor>, GoogleMap.OnInfoWindowClickListener {
    /* ----- CONSTANTS ----- */
    // private final String LOG_TAG = BaseMap.class.getSimpleName() + "(" +
    // getClass().getSimpleName() + ")@" + Integer.toHexString(hashCode());

	/* ----- VARIABLES ----- */
    /**
     * Behavior flag, used to determine if the data has been fetched.
     */
    protected boolean dataFetched = false;
    /**
     * Behavior flag, used to determine if the data should be fetched on
     * fragment creation.
     */
    protected boolean fetchOnCreate = true;
    /**
     * Behavior flag, used to determine if the cursor has been swapped in the
     * adapter.
     */
    protected volatile boolean hasSwappedCursor = true;
    protected GoogleMap map = null;
    /**
     * Hashmap of markers and associated database id, used to look up the
     * location when the marker is selected from the map.
     */
    protected final HashMap<Marker, Integer> markerMap = new HashMap<Marker, Integer>();
    protected final SparseArray<Marker> reverseMarkerMap = new SparseArray<Marker>();
    protected Thread requeryManager;

    protected ArrayList<LatLng> latLngList = new ArrayList<>();

	/* ----- CONSTRUCTORS ----- */

    /**
     * Required constructor.
     */
    public BaseMap() {
    }

	/* ----- LIFECYCLE METHODS ----- */

    /**
     * {@inheritDoc}
     * <p/>
     * <p>
     * Opens the database, tracks the fragment in analytics, and tries to get
     * the variables saved to the savedInstanceState. Subclass should override
     * to get any other variables saved.
     * </p>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseDbAccessor.open();

        dataFetched = (savedInstanceState != null) ? savedInstanceState.getBoolean("dataFetched") : dataFetched;
        fetchOnCreate = (savedInstanceState != null) ? savedInstanceState.getBoolean("fetchOnCreate") : fetchOnCreate;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>
     * Gets the layout created in the super method and puts it inside a
     * {@code FrameLayout} with a transparent background to be returned. This
     * fixes an issue causing a hole in the application where the map is.
     * </p>
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup view, Bundle savedInstance) {
        View layout = super.onCreateView(inflater, view, savedInstance);

        FrameLayout frameLayout = new FrameLayout(getActivity());
        frameLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        ((ViewGroup) layout).addView(frameLayout, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        return layout;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>
     * Runs through the behavioral booleans to determine what to do next. If the
     * data has already been fetched ({@code dataFetched}), then it calls
     * {@link #loadMapPlots()}. Otherwise, if it should fetch the data when the
     * fragment is created ({@code fetchOnCreate}), then it calls
     * {@link #fetchData()} and calls {@link #loadMapPlots()}.
     * </p>
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        map = getMap();
        clearMarkers();
        if (map != null) {
            map.setMyLocationEnabled(true);
            map.setOnInfoWindowClickListener(this);
        }

        if (dataFetched) {
            loadMapPlots();
        } else if (fetchOnCreate) {
            fetchData();
            loadMapPlots();
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>
     * Stops the {@code requeryManager} if it is still running.
     * </p>
     */
    @Override
    public void onPause() {
        super.onPause();
        if (requeryManager != null) {
            requeryManager.interrupt();
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>
     * Closes the database.
     * </p>
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        BaseDbAccessor.close();
    }

	/* ----- INHERITED METHODS ----- */
    // Fragment

    /**
     * {@inheritDoc}
     * <p/>
     * <p>
     * Saves the behavioral flags to the saved instance state bundle. Override
     * this to save more variables.
     * </p>
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("dataFetched", dataFetched);
        outState.putBoolean("fetchOnCreate", fetchOnCreate);
        super.onSaveInstanceState(outState);
    }

    // LoaderCallbacks

    /**
     * {@inheritDoc}
     * <p/>
     * <p>
     * Creates the {@code SimpleCursorLoader} based on the cursor returned by
     * {@link #loadCursor()}.
     * </p>
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new SimpleCursorLoader(getActivity()) {
            @Override
            public Cursor loadInBackground() {
                return loadCursor();
            }
        };
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>
     * Adds the markers to the map. Subclass should override to perform any
     * other actions on the loader thread.
     * </p>
     * <p/>
     * <p>
     * Note that {@link #handleRequery()} is called when finishing.
     * </p>
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        int count = cursor.getCount();
        double latitude;
        double longitude;
        int id;
        String title;
        String snippet;

        if (cursor.moveToFirst()) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            do {
                latitude = cursor.getDouble(cursor.getColumnIndex(getCursorLatString()));
                longitude = cursor.getDouble(cursor.getColumnIndex(getCursorLngString()));
                id = cursor.getInt(cursor.getColumnIndex(getCursorIdString()));
                title = cursor.getString(cursor.getColumnIndex(getCursorLocTitle()));
                snippet = cursor.getString(cursor.getColumnIndex(getCursorLocText()));
                LatLng point = new LatLng(latitude, longitude);
                drawMarker(point, id, title, snippet);
                builder.include(point);
            } while (cursor.moveToNext());
            try {
                if (count == 1) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16));
                } else {
                    LatLngBounds bounds = builder.build();
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20));
                }
            } catch (IllegalStateException e) {
                // Do Nothing
            }
        }

        hasSwappedCursor = true;
        handleRequery();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>
     * Default does nothing, subclass should override to change this behavior.
     * </p>
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Does nothing by default
    }

	/* ----- CUTSOM METHODS ----- */

    /**
     * <p>
     * Kickstarts the loading process by starting the loader.
     * </p>
     */
    public void loadMapPlots() {
        clearMarkers();
        Loader<Cursor> loader;
        try {
            loader = getLoaderManager().getLoader(0);
        } catch (Exception e) {
            loader = null;
        }

        if (loader == null) {
            getLoaderManager().initLoader(0, null, this);
        } else {
            // loader.forceLoad();
            getLoaderManager().restartLoader(0, null, this);
        }

        // Logger.i(LOG_TAG, "Loading RequeryManager.");
        requeryManager = new Thread(new RequeryManager(), RequeryManager.class.getSimpleName());
        requeryManager.start();
    }

    /**
     * <p>
     * Creates the marker and adds it to the map and adds it and its id to
     * {@code markerMap}.
     * </p>
     *
     * @param location    the latitude and longitude
     * @param id          the database id
     * @param title       the marker title text
     * @param snippet     the marker snippet text
     * @param colorString The Hexadecimal String representation of the marker color
     * @param icon        The BitmapDescriptor to use instead of the default marker
     * @param heading     The heading of the icon (0-359) in degrees, from North
     */
    protected void drawMarker(LatLng location, int id, String title, String snippet, String colorString, BitmapDescriptor icon, Float heading) {
        MarkerOptions markerOptions = new MarkerOptions().position(location);
        if (!AppState.isNullOrEmpty(title)) {
            markerOptions.title(title);
        }
        if (!AppState.isNullOrEmpty(snippet)) {
            markerOptions.snippet(snippet);
        }
        if (!AppState.isNullOrEmpty(colorString)) {
            float hsvArray[] = new float[3];
            Color.colorToHSV(Color.parseColor(colorString), hsvArray);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(hsvArray[0]));
        }
        if (icon != null) {
            markerOptions.icon(icon);
            // Center the icon on its location
            markerOptions.anchor((float) 0.5, (float) 0.5);

        }

        if (heading != null) {
            markerOptions.flat(true);
            markerOptions.rotation(heading);
            // If we add rotation and don't adjust the infoWindowAnchor,
            // The window will always appear at the 'Top' of the icon
            // By setting the anchor in the center, we can ensure a
            // consistent look.
            markerOptions.infoWindowAnchor((float) 0.5, (float) 0.5);
        }
        Marker marker = map.addMarker(markerOptions);

        markerMap.put(marker, id);

        reverseMarkerMap.put(id, marker);

        // Logger.dFormat(LOG_TAG, "Marker added to map, id=%d", id);
    }

    /**
     * <p>
     * Creates the marker and adds it to the map and adds it and its id to
     * {@code markerMap}.
     * </p>
     *
     * @param location the latitude and longitude
     * @param id       the database id
     * @param title    the marker title text
     * @param snippet  the marker snippet text
     */
    protected void drawMarker(LatLng location, int id, String title, String snippet, String colorString) {
        drawMarker(location, id, title, snippet, colorString, null, null);
    }

    /**
     * <p>
     * Creates the marker and adds it to the map and adds it and its id to
     * {@code markerMap}.
     * </p>
     *
     * @param location the latitude and longitude
     * @param id       the database id
     * @param title    the marker title text
     * @param snippet  the marker snippet text
     */
    protected void drawMarker(LatLng location, int id, String title, String snippet) {
        this.drawMarker(location, id, title, snippet, null, null, null);
    }

    /**
     * Draws a PolyLine given from the list of Lat/Lon values, This line is
     * overlaid onto the map
     *
     * @param latLngList an {@link ArrayList} containing the Lat/Long pairs to be
     *                   plotted
     */
    protected void drawRoute(ArrayList<LatLng> latLngList, String hexColor) {
        if (latLngList.size() >= 2) {
            PolylineOptions line = new PolylineOptions();
            for (LatLng point : latLngList) {
                line.add(point);
            }
            try {
                line.color(Color.parseColor(hexColor));
            } catch (Exception e) {
                //				line.color(R.color.transparentbutton_selected_color);
                line.color(android.R.color.transparent);
            }
            line.width(5);
            line.visible(true);
            map.addPolyline(line);
            latLngList.clear();
        }
    }

    /**
     * <p>
     * Clears all markers from the map and from the hashmap.
     * </p>
     */
    protected void clearMarkers() {
        markerMap.clear();
        if (map != null) {
            map.clear();
            //			map.moveCamera(CameraUpdateFactory.newLatLngZoom(
            //					AppState.JESSE_HALL_LAT_LNG, AppState.MAP_START_ZOOM));
        }
    }

    /**
     * <p>
     * Default does nothing, subclasses should override this method to handle
     * requeries.
     * </p>
     */
    protected void handleRequery() {
        // do nothing by default
    }

    /**
     * <p>
     * Handles the completion of this list's Loader. The subclass should
     * override this method to handle this event.
     * </p>
     */
    protected void finishedLoading() {
        // do nothing by default
    }

    /**
     * <p>
     * Does nothing by default, subclass should override to handle updating the
     * class arguments.
     * </p>
     *
     * @param args  arguments associated with the {@code adn.GoMizzou.ARGS} passed
     *              to the fragment in the bundle
     * @param query argments associated with the {@code SearchManager.QUERY}
     *              passed to the fragment in the bundle
     */
    public void updateArgs(String args, String query) {
        // do nothing by default
    }

    /**
     * <p>
     * Does nothing by default, subclasses should override this method to handle
     * fetching data via a network operation or some other method.
     * </p>
     */
    public void fetchData() {
        // do nothing by default
    }

    /**
     * <p>
     * Default returns null. Subclass can override this method to change this
     * value.
     * </p>
     *
     * @return the text to be displayed in the title field in the master layout
     */
    protected String getTitleText() {
        return null;
    }

    /**
     * <p>
     * Hooks to subclass to pull a query of a database. Note that this method is
     * only called on a worker thread. Also note that the database is already
     * open for you.
     * </p>
     *
     * @return the cursor representing the data to display.
     */
    protected abstract Cursor loadCursor();

    /**
     * <p>
     * Hooks to subclass to check if the list should continue requerying the
     * database for new information, usually from a webservice.
     * </p>
     *
     * @return true if the data could still change, false if not.
     */
    protected abstract boolean shouldRequery();

    protected abstract String getCursorLatString();

    protected abstract String getCursorLngString();

    protected abstract String getCursorIdString();

    protected abstract String getCursorLocTitle();

    protected abstract String getCursorLocText();

	/* ----- NESTED CLASSES ----- */

    /**
     * <p>
     * {@code Runnable} that regularly restarts the loader until
     * {@link BaseMap#shouldRequery()} returns false. Note that
     * {@link BaseMap#finishedLoading()} is called when finishing.
     * </p>
     *
     * @author russellja
     */
    private class RequeryManager implements Runnable {

        @Override
        public void run() {
            // Logger.i(LOG_TAG, "Starting RequeryManager.");

            try {
                while (shouldRequery()) {
                    while (!hasSwappedCursor) {
                        // Logger.i(LOG_TAG, "RequeryManager waiting.");
                        Thread.sleep(AppState.REQUERY_WAIT);
                    }
                    hasSwappedCursor = false;
                    doUpdate();
                }
            } catch (InterruptedException e) {
                // do nothing
            }
            // Logger.i(LOG_TAG, "Finished RequeryManager.");

            doFinish();
        }

        private void doUpdate() {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isAdded()) {
                        getLoaderManager().restartLoader(0, null, BaseMap.this);
                    }
                }
            });
        }

        private void doFinish() {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finishedLoading();
                }
            });
        }
    }
}
