package com.untappedkegg.rally.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.data.NewDataFetcher;
import com.untappedkegg.rally.home.ActivityMain;
import com.untappedkegg.rally.ui.loaders.SimpleCursorLoader;

/**
 * <p>Base implementation for a Loader Fragment, such as a Grid.  Includes a requery manager.</p>
 *
 * @author UntappedKegg
 */
public abstract class BaseLoaderFragment extends BaseFragment implements LoaderCallbacks<Cursor> {
    /* ----- CONSTANTS ----- */
    protected final String LOG_TAG = BaseLoaderFragment.class.getSimpleName() + "(" + ((Object) this).getClass().getSimpleName() + ")@" + Integer.toHexString(hashCode());

    /* ----- VARIABLES ----- */
    protected SimpleCursorAdapter adapter;
    protected View emptyView;
    //	protected volatile boolean hasSwappedCursor;

    /* ----- CONSTRUCTORS ----- */
    public BaseLoaderFragment() {
    }

	 /* ----- LIFECYCLE METHODS ----- */
    /**
     * <p>Runs through the behavioral booleans to determine what to do next.
     * If the data has already been fetched ({@code dataFetched} ), then it calls {@link #loadData()}.
     * Otherwise, if it should fetch the data when the fragment is created ({@code fetchOnCreate} ),
     * then it calls {@link #fetchData()} and calls {@link #loadData()}.
     * If the data has not been fetched and the data should not be fetched when the fragment is created,
     * then it hides the {@code progressBar}.</p>
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = createCursorAdapter();
        adapter.setViewBinder(getViewBinder());

        if (dataFetched) {
            loadData();
            setProgressBarVisibility(View.GONE);
        } else if (fetchOnCreate) {
            fetchData();
            dataFetched = true;
            loadData();
        } else {
            setProgressBarVisibility(View.GONE);
            setEmptyText();
            finishRequery();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        final short position = getArguments().getShort(AppState.KEY_POSITION);


        if (position != 0) {
            ActivityMain.setCurPosition(position);
            final String[] modArray = getResources().getStringArray(R.array.action_bar_modules);
            try {
                ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(modArray[position]);

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }
    //	LoaderCallbacks
    /**
     * <p>Creates the {@code SimpleCursorLoader} based on the cursor returned by {@link #loadCursor()}.</p>
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        return new SimpleCursorLoader(getActivity()) {
            @Override
            public Cursor loadInBackground() {
                return loadCursor();
            }


        };
    }

    /**
     * <p>Adds buttons to the button bar, sets the cursor in the adapter,
     * sets the text in the empty and header views. Subclass should override
     * to perform any other actions on the loader thread.</p>
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        adapter.swapCursor(c);
        //		hasSwappedCursor = true;
        setEmptyText();
        //		handleRequery();

    }

    /**
     * <p>Removes the cursor from the adapter to clear the list view.</p>
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }


    /* ----- CUSTOM METHODS ----- */
    protected abstract void fetchData();

    /**
     * <p>Hooks to subclass to create a new {@code SimpleCursorAdapter} specific to this list.
     * The cursor inside the adapter should be set to null.</p>
     *
     * @return a new instance of {@code SimpleCursorAdapter}
     */
    protected abstract SimpleCursorAdapter createCursorAdapter();


    /**
     * <p>Hooks to subclass to pull a query of a database.
     * Note that this method is only called on a worker thread.
     * Also note that the database is already open for you.</p>
     *
     * @return the cursor representing the data to display.
     */
    protected abstract Cursor loadCursor();

    /**
     * @return the ViewBinder to control data for this list.
     */
    protected abstract ViewBinder getViewBinder();

    /**
     * <p>Kickstarts the loading process by starting the loader.</p>
     */
    public void loadData() {
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
            requeryManager = new Thread(new BaseRequery(), BaseRequery.class.getSimpleName());
            requeryManager.start();
        }
    }

    /**
     * <p>Default implementation returns {@code android.R.id.empty}.
     * The subclass can override this method to change this value.
     * The id returned is used to set {@code emptyView}.</p>
     *
     * @return the id for the empty view in the layout.
     */
    protected int getEmptyViewId() {
        return android.R.id.empty;
    }

    /**
     * <p>Sets the text returned from {@link #getEmptyText()} into {@code emptyView}.
     * Also, sets the {@code View.OnClickListener} returned from
     * {@link #getEmptyOnClick()} for the empty view.</p>     *
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
     * <p>This method shouldn't be overridden to return custom empty text.
     * This method checks {@link NewDataFetcher#isInternetConnected()} and {@link #requeryManagerIsAlive()}
     * to display a message in those cases.  Instead override {@link #getCustomEmptyText()} to set the custom
     * empty text or {@link #getContactingEmptyText()} to set the message to be displayed while performing
     * the network operation.
     * If none of these situations apply {@code R.string.generic_empty_list_text} is returned.</p>
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
     * <p>This method should be overridden to customize the message to be displayed while performing
     * the network operation instead of {@link #getEmptyText()}.</p>
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
     * <p>Returns the loader id that the details will automatically use. Only subclass this if
     * the details needs control over this value, for example, if it uses multiple loaders.</p>
     */
    protected int getLoaderId() {
        return 0;
    }

    /**
     * <p>Hooks to subclass to check if the list should continue requerying the database for new information,
     * usually from a webservice.</p>
     *
     * @return true if the data could still change, false if not.
     */
    protected abstract boolean shouldRequery();


    /**
     * <p>Handles the completion of loader. The subclass should override this method to handle this event.</p>
     */
    protected void finishRequery() {
        this.setEmptyText();
        // do nothing by default
    }

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
}
