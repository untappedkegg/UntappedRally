package com.untappedkegg.rally.ui;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.BuildConfig;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.data.BaseDbAccessor;
import com.untappedkegg.rally.data.DataFetcher;
import com.untappedkegg.rally.data.NewDataFetcher;
import com.untappedkegg.rally.home.ActivityMain;
import com.untappedkegg.rally.ui.loaders.SimpleCursorLoader;

import java.util.Timer;
import java.util.TimerTask;

/**
 * <p>Base implementation for a ListFragment that uses CursorLoaders with automatic, periodic requerying.</p>
 *
 * @author russellja
 */
public abstract class BaseList extends ListFragment implements LoaderCallbacks<Cursor> {
    /* ----- CONSTANTS ----- */
    protected final String LOG_TAG = BaseList.class.getSimpleName() + "(" + ((Object) this).getClass().getSimpleName() + ")@" + Integer.toHexString(hashCode());

	/* ----- VARIABLES ----- */
    /**
     * Behavior flag, used to determine if the data has been fetched.
     */
    protected boolean dataFetched;
    /**
     * Behavior flag, used to determine if the data should be fetched on fragment creation.
     */
    protected boolean fetchOnCreate;
    /**
     * Behavior flag, used to determine if the cursor has been swapped in the adapter.
     */
    private volatile boolean hasSwappedCursor;
    protected ProgressBar progressBar;
    protected View emptyView;
    protected View listHeaderView;
    private SimpleCursorAdapter adapter;
    protected Thread requeryManager;
    protected int scrollIndex;
    protected int scrollTop;
    //ActionBar
    private short position;
    private String[] modArray;

	/* ----- CONSTRUCTORS ----- */

    /**
     * Required constructor.
     */
    public BaseList() {
    }

	/* ----- LIFECYCLE METHODS ----- */

    /**
     * <p>Sets {@code dataFetched} to false, {@code fetchOnCreate} and {@code hasSwappedCursor} to true. Subclass should override to change these values.</p>
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        dataFetched = false;
        fetchOnCreate = true;
        hasSwappedCursor = true;
        try {
            position = getArguments().getShort(AppState.KEY_POSITION);
        } catch (NullPointerException e) {
            position = 0;
        }
        modArray = getResources().getStringArray(R.array.action_bar_modules);

    }

    /**
     * <p>Opens the database and tries to get the variables saved to the savedInstanceState. Subclass should override to get any other
     * variables saved.</p>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseDbAccessor.open();

        dataFetched = (savedInstanceState != null) ? savedInstanceState.getBoolean("dataFetched") : dataFetched;
        fetchOnCreate = (savedInstanceState != null) ? savedInstanceState.getBoolean("fetchOnCreate") : fetchOnCreate;
        scrollIndex = (savedInstanceState != null) ? savedInstanceState.getInt("scrollIndex") : 0;
        scrollTop = (savedInstanceState != null) ? savedInstanceState.getInt("scrollTop") : 0;
    }

    /**
     * <p>Creates the fragment layout and gets the required views from it based on what is returned from {@link #getListLayout()}, {@link #getProgressBarId()},
     * {@link BaseList#getButtonBarId()}, and {@link #getEmptyViewId()}.  Subclass should override those methods to change the values returned, or override this method with
     * a call to get the view returned from the super to get another view from the layout.</p>
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getListLayout(), container, false);
        progressBar = (ProgressBar) view.findViewById(getProgressBarId());
        emptyView = view.findViewById(getEmptyViewId());
        if (emptyView != null) {
            ListView listView = (ListView) view.findViewById(getListViewId());
            listView.setEmptyView(emptyView);
        }
        if (getListHeaderId() != -1) {
            listHeaderView = inflater.inflate(getListHeaderId(), null);
        }

        return view;
    }

    /**
     * <p>Runs through the behavioral booleans to determine what to do next.  If the data has already been fetched ({@code dataFetched}), then it calls {@link #loadList()}.
     * Otherwise, if it should fetch the data when the fragment is created ({@code fetchOnCreate}), then it calls {@link #fetchData()} and calls {@link #loadList()}.
     * If the data has not been fetched and the data should not be fetched when the fragment is created, then it hides the {@code progressBar}.</p>
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (listHeaderView != null) {
            getListView().addHeaderView(listHeaderView, null, false);
        }

        adapter = createCursorAdapter();
        adapter.setViewBinder(getViewBinder());
        setListAdapter(getAdapterForList(getListView()));

        //		BaseContainer.setTitle(this, getTitleText());

        if (dataFetched) {
            loadList();
            setProgressBarVisibility(View.GONE);
        } else if (fetchOnCreate) {
            fetchData();
            dataFetched = true;
            loadList();
        } else {
            setProgressBarVisibility(View.GONE);
            setEmptyText();
            finishedLoading();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (position != 0) {
            ActivityMain.setCurPosition(position);
            try {
                ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(modArray[position]);
            } catch (Exception e) {
            }
        }
    }

    /**
     * <p>Stops the {@code requeryManager} if it is still running.</p>
     */
    @Override
    public void onPause() {
        super.onPause();
        if (requeryManager != null) {
            requeryManager.interrupt();
        }
    }

    /**
     * <p>Gets the scroll position from the list view, and performs clean up on the list view.</p>
     */
    @Override
    public void onDestroyView() {
        ListView listView = getListView();
        View view = listView.getChildAt(0);
        scrollIndex = listView.getFirstVisiblePosition();
        listHeaderView = null;
        scrollTop = (view == null) ? 0 : view.getTop();
        super.onDestroyView();
        setListAdapter(null);
    }

    /**
     * <p>Closes the database.</p>
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        BaseDbAccessor.close();
    }

	/* ----- INHERITED METHODS ----- */
    //	Fragment

    /**
     * <p>Saves the scroll position and the behavioral flags to the saved instance state bundle.  Override this to save more variables.</p>
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("scrollIndex", scrollIndex);
        outState.putInt("scrollTop", scrollTop);
        outState.putBoolean("dataFetched", dataFetched);
        outState.putBoolean("fetchOnCreate", fetchOnCreate);
        super.onSaveInstanceState(outState);
    }

    //	LoaderCallbacks

    /**
     * <p>Creates the {@code SimpleCursorLoader} based on the cursor returned by {@link #loadCursor()}.</p>
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
     * <p>Adds buttons to the button bar, sets the cursor in the adapter, sets the text in the empty and header views. Subclass should override to perform any other actions
     * on the loader thread.</p>
     * <p/>
     * <p>Note that {@link #handleRequery()} is called when finishing.</p>
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        adapter.swapCursor(cursor);
        hasSwappedCursor = true;
        setEmptyText();

        handleRequery();
        setListHeaderText(cursor);
    }

    /**
     * <p>Removes the cursor from the adapter to clear the list view.</p>
     */
    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        adapter.swapCursor(null);
    }

	/* ----- CUSTOM METHODS ----- */

    /**
     * <p>Resets the scroll to the saved scroll position.</p>
     */
    protected void setScroll() {
        try {
            final ListView listView = getListView();
            if ((scrollIndex > 0 || scrollTop > 0) && listView != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listView.setSelectionFromTop(scrollIndex, scrollTop);
                    }
                });
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    /**
     * <p>Default implementation returns {@code R.layout.generic_list}. The subclass can override this method to change this value.
     * If this value is changed, {@link #getButtonBarId()}, {@link #getProgressBarId()}, {@link #getListViewId()}, {@link #getEmptyViewId()}
     * will also need to be changed.</p>
     *
     * @return the layout used for this list.
     */
    protected int getListLayout() {
        return R.layout.generic_list;
    }

    /**
     * <p>Default implementation returns {@code R.id.generic_list_progress}. The subclass can override this method to change this value
     * if {@link #getListLayout()} is overridden.  The id returned is used to set {@code progressBar}.</p>
     *
     * @return the id for the progress bar in the layout.
     */
    protected int getProgressBarId() {
        return R.id.generic_list_progress;
    }

    /**
     * <p>Default implementation returns {@code -1} (no header view added). The subclass can override this method to change this value.</p>
     *
     * @return the id for the list header to be added to the listview.
     */
    protected int getListHeaderId() {
        return -1;
    }

    /**
     * <p>Default implementation returns null. The subclass can override this method to change this value.</p>
     *
     * @return the ViewBinder to control data for this list.
     */
    protected ViewBinder getViewBinder() {
        return null;
    }

    /**
     * <p>Returns the loader id that the list will automatically use. Only subclass this if the list needs control over this value, for example, if it uses
     * multiple loaders.</p>
     */
    protected int getLoaderId() {
        return 0;
    }

    /**
     * <p>Default implementation returns {@code android.R.id.list}. The subclass can override this method to change this value
     * if {@link #getListLayout()} is overridden.</p>
     *
     * @return the id for the listview in the layout.
     */
    protected int getListViewId() {
        return android.R.id.list;
    }

    /**
     * <p>Default implementation returns {@code android.R.id.empty}. The subclass can override this method to change this value
     * if {@link #getListLayout()} is overridden.  The id returned is used to set {@code emptyView}.</p>
     *
     * @return the id for the empty view in the layout.
     */
    protected int getEmptyViewId() {
        return android.R.id.empty;
    }

    /**
     * <p>Sets the text returned from {@link #getEmptyText()} into {@code emptyView}.  Also, sets the {@code View.OnClickListener} returned from
     * {@link #getEmptyOnClick()} for the empty view.</p>
     *
     */
    protected void setEmptyText() {
        if (emptyView != null && isAdded()) {
            if (TextView.class.isAssignableFrom(emptyView.getClass())) {
                ((TextView) emptyView).setText(getEmptyText());
            }

            View.OnClickListener onClick = getEmptyOnClick();
            if (onClick != null) {
                emptyView.setOnClickListener(onClick);
            } else if (!NewDataFetcher.isInternetConnected()) {
                emptyView.setOnClickListener(new OnEmptyViewClick());
            }
        }
    }

    /**
     * <p>This method shouldn't be overridden to return custom empty text.  This method checks {@link DataFetcher#isInternetConnected()} to display a message in that case.
     * Instead override {@link #getCustomEmptyText()} to set the custom empty
     * text or {@link #getContactingEmptyText()} to set the message to be displayed while performing the network operation.  If none of these
     * situations apply {@code R.string.generic_empty_list_text} is returned.</p>
     *
     * @return the text to be inserted into {@code emptyView} by {@code setEmptyText}.
     */
    private String getEmptyText() {
        if (!NewDataFetcher.isInternetConnected()) {
            return getString(R.string.no_network_connection_msg);
        } else if (requeryManagerIsAlive()) {
            return getContactingEmptyText();
        } else if (!AppState.isNullOrEmpty(getCustomEmptyText())) {
            return getCustomEmptyText();
        } else {
            return getString(R.string.generic_empty_list_text);
        }
    }

    /**
     * <p>Default returns null.  Subclass can override this method to change this value.</p>
     * <p/>
     * <p>This method should be overridden to customize the empty message instead of {@link #getEmptyText()}.</p>
     *
     * @return the customized empty message
     */
    protected String getCustomEmptyText() {
        return null;
    }

    /**
     * <p>Default returns "Just a Moment...".  Subclass can override this method to change this value.</p>
     * <p/>
     * <p>This method should be overridden to customize the message to be displayed while performing the network operation instead of
     * {@link #getEmptyText()}.</p>
     *
     * @return the customized message to be displayed while performing the network operation
     */
    protected String getContactingEmptyText() {
        return getResources().getString(R.string.just_a_moment);
    }

    /**
     * <p>Default returns null.  Subclass can override this method to change this value.</p>
     *
     * @return the {@code View.OnClickListener} for the {@code emptyView}
     */
    protected View.OnClickListener getEmptyOnClick() {
        return null;
    }

    /**
     * <p>Default returns null.  Subclass can override this method to change this value.</p>
     *
     * @return the text to be displayed in the title field in the master layout
     */
    protected String getTitleText() {
        return null;
    }

    /**
     * <p>Default does nothing.  Subclass can override this method to change this behavior.</p>
     * <p/>
     * <p>Sets the text in the {@code listHeaderView} whose id is returned by {@link #getListHeaderId()}.</p>
     *
     * @param cursor the cursor populating the listview
     */
    protected void setListHeaderText(Cursor cursor) {
        // do nothing by default
    }

    /**
     * <p>Kickstarts the loading process by starting the loader.</p>
     */
    public void loadList() {
        setProgressBarVisibility(View.VISIBLE);

        Loader<Cursor> loader;
        try {
            loader = getLoaderManager().getLoader(getLoaderId());
        } catch (Exception e) {
            loader = null;
        }

        if (loader == null) {
            getLoaderManager().initLoader(getLoaderId(), null, this);
        } else {
            loader.forceLoad();
            getLoaderManager().restartLoader(getLoaderId(), null, this);
        }

        if (!requeryManagerIsAlive()) {
            requeryManager = new Thread(new ListRequery(), ListRequery.class.getSimpleName());
            requeryManager.start();
        }
    }

    /**
     * <p>Subclasses should override this method if the {@code SimpleCursorAdapter} returned by
     * {@link #getCursorAdapter()} needs to be modified or disregarded.</p>
     *
     * @param listView The {@code ListView} returned by {@code ListFragment} method {@link #getListView()}.
     * @return The {@code ListAdapter} for the {@code ListView}
     */
    protected ListAdapter getAdapterForList(ListView listView) {
        return getCursorAdapter();
    }

    /**
     * <p>Subclasses shouldn't override this method.  It allows access to the possibly shadowed class variable
     * {@code adapter} of type {@code SimpleCursorAdapter}.</p>
     *
     * @return the class variable {@code adapter}
     */
    protected SimpleCursorAdapter getCursorAdapter() {
        return adapter;
    }

    /**
     * <p>Default does nothing, subclasses should override this method to handle requeries.</p>
     */
    protected void handleRequery() {
        // do nothing by default
    }

    /**
     * <p>Does nothing by default, subclass should override to handle updating the class arguments.</p>
     *
     * @param args  arguments associated with the {@code adn.GoMizzou.ARGS} passed to the fragment in the bundle
     * @param query argments associated with the {@code SearchManager.QUERY} passed to the fragment in the bundle
     */
    public void updateArgs(String args, String query) {
        //	do nothing by default
    }

    /**
     * <p>Does nothing by default, subclasses should override this method to handle fetching data via a network operation or some other method.</p>
     */
    public abstract void fetchData();

    /**
     * <p>Handles the completion of loader. The subclass should override this method to handle this event.</p>
     */
    protected void finishedLoading() {
        // do nothing by default
    }

    /**
     * <p>Gets the visibility of the {@code progressBar}, if not null.</p>
     *
     * @return the visibility of the {@code progressBar} or -1 if it is null
     */
    //	private int getProgressBarVisibility() {
    //		if(progressBar == null) {
    //			return -1;
    //		} else {
    //			return progressBar.getVisibility();
    //		}
    //	}

    /**
     * <p>Sets the visibility of {@code progressBar}, if not null.</p>
     *
     * @param visibility {@link View#GONE} or {@link View#VISIBLE}
     */
    protected void setProgressBarVisibility(int visibility) {
        if (progressBar != null && (visibility == View.VISIBLE || visibility == View.GONE)) {
            progressBar.setVisibility(visibility);
        }
    }

    /**
     * <p>Checks to see if the {@link #requeryManager} is still running.</p>
     *
     * @return true if the {@link requeryManager} is not null and is still running, false otherwise
     */
    protected boolean requeryManagerIsAlive() {
        return requeryManager != null && requeryManager.isAlive();
    }

    /**
     * <p>Hooks to subclass to pull a query of a database. Note that this method is only called on a worker thread. Also note that the database is already
     * open for you.</p>
     *
     * @return the cursor representing the data to display.
     */
    protected abstract Cursor loadCursor();

    /**
     * <p>Hooks to subclass to create a new {@code SimpleCursorAdapter} specific to this list. The cursor inside the adapter should be set to null.</p>
     *
     * @return a new instance of {@code SimpleCursorAdapter}
     */
    protected abstract SimpleCursorAdapter createCursorAdapter();

    /**
     * <p>Hooks to subclass to check if the list should continue requerying the database for new information, usually from a webservice.</p>
     *
     * @return true if the data could still change, false if not.
     */
    protected abstract boolean shouldRequeryData();

	/* ----- NESTED CLASSES ----- */

    /**
     * <p>Generic {@code View.OnClickListener} for the empty view.</p>
     */
    private class OnEmptyViewClick implements View.OnClickListener {
		/* ----- INHERITED METHODS ----- */

        /**
         * <p>Tries to fetch the data again.  Subclass can override this method to change this behavior. </p>
         */
        @Override
        public void onClick(View view) {
            setProgressBarVisibility(View.VISIBLE);
            fetchData();
        }
    }

    /**
     * <p>{@code Runnable} that regularly restarts the loader until {@link #shouldRequery()} returns false.
     * Note that {@link #finishedLoading()} and {@link #setScroll()} are called when finishing.</p>
     *
     * @author russellja
     */
    private class ListRequery implements Runnable {
        /* ----- INHERITED METHODS ----- */
        @Override
        public void run() {
            Log.d(LOG_TAG, "Starting RequeryManager.");
            try {
                do {
                    while (!hasSwappedCursor) {
                        Log.d(LOG_TAG, "RequeryManager waiting.");
                        Thread.sleep(AppState.REQUERY_WAIT);
                    }
                    hasSwappedCursor = false;
                    doUpdate();
                } while (shouldRequeryData());
            } catch (InterruptedException e) {
                Log.d(LOG_TAG, "RequeryManager interrupted");
            }
            Log.d(LOG_TAG, "Finished RequeryManager.");

            doFinish();
        }

        private void doUpdate() {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isAdded()) {
                        getLoaderManager().restartLoader(getLoaderId(), null, BaseList.this);
                    }
                }
            });
        }

        private void doFinish() {
            Timer finish = new Timer();
            finish.schedule(new FinishTask(), AppState.REQUERY_WAIT);
        }
    }

    /**
     * <p>A {@code TimerTask} used to call the UI methods needed to finish up after the {@link requeryManager} finishes.</p>
     */
    private class FinishTask extends TimerTask {
        /* ----- INHERITED METHODS ----- */
        @Override
        public void run() {
            if (getActivity() == null) {
                return;
            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isAdded()) {
                        setProgressBarVisibility(View.GONE);
                        setEmptyText();
                        finishedLoading();
                        setScroll();
                    }
                }
            });
        }
    }
}
