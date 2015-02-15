package com.untappedkegg.rally.ui;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.SimpleCursorTreeAdapter.ViewBinder;
import android.widget.TextView;

import com.diegocarloslima.fgelv.lib.FloatingGroupExpandableListView;
import com.diegocarloslima.fgelv.lib.WrapperExpandableListAdapter;
import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.BuildConfig;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.data.NewDataFetcher;
import com.untappedkegg.rally.home.ActivityMain;
import com.untappedkegg.rally.ui.loaders.SimpleCursorLoader;


/**
 * Created by UntappedKegg on 8/9/2014.
 */
public abstract class ExpandableList extends BaseFragment implements LoaderCallbacks<Cursor>, ExpandableListView.OnChildClickListener {
    /* ----- VARIABLES ----- */
    protected SimpleCursorTreeAdapter adapter;
    protected View emptyView;
    /**
     * Behavior flag, used to determine if the cursor has been swapped in the adapter.
     */
    private volatile boolean hasSwappedCursor;
    protected ProgressBar progressBar;
    protected View listHeaderView;
    protected int scrollIndex;
    protected int scrollTop;
    protected FloatingGroupExpandableListView listView;
    private boolean isFromRestore;

       /* ----- LIFECYCLE METHODS ----- */

    /**
     * <p>Opens the database and tries to get the variables saved to the savedInstanceState.
     * Subclass should override to get any other variables saved.</p>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scrollIndex = (savedInstanceState != null) ? savedInstanceState.getInt("scrollIndex") : 0;
        scrollTop = (savedInstanceState != null) ? savedInstanceState.getInt("scrollTop") : 0;
    }

    /**
     * <p>Creates the fragment layout and gets the required views from it based on what is returned from
     * {@link #getListLayout()} and {@link #getProgressBarId()}.  Subclass should override those methods to change the values returned,
     * or override this method with a call to get the view returned from the super to get another view from the layout.</p>
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getListLayout(), container, false);
        progressBar = (ProgressBar) view.findViewById(getProgressBarId());
        listView = (FloatingGroupExpandableListView) view.findViewById(android.R.id.list);

        listView.setClickable(true);
        emptyView = view.findViewById(android.R.id.empty);
        if (emptyView != null) {
            listView.setEmptyView(emptyView);
        }
        if (getListHeaderId() != -1) {
            listHeaderView = inflater.inflate(getListHeaderId(), null);
        }
        isFromRestore = savedInstanceState != null && savedInstanceState.getBoolean("isFromRestore");
        return view;
    }

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
        listView.setAdapter(new WrapperExpandableListAdapter(adapter));
        listView.setOnChildClickListener(this);

        if (listHeaderView != null) {
            listView.addHeaderView(listHeaderView, null, false);
        }

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
            finishedLoading();
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
    /**
     * <p>Gets the scroll position from the list view, and performs clean up on the list view.</p>
     */
    @Override
    public void onDestroyView() {
        View view = listView.getChildAt(0);
        scrollIndex = listView.getFirstVisiblePosition();
        listHeaderView = null;
        scrollTop = (view == null) ? 0 : view.getTop();
        super.onDestroyView();
    }

       /*----- INHERITED METHODS -----*/

    /**
     * <p>Saves the scroll position and the behavioral flags to the saved instance state bundle.
     * Override this to save more variables.</p>
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("scrollIndex", scrollIndex);
        outState.putInt("scrollTop", scrollTop);
        outState.putBoolean("isFromRestore", true);
        super.onSaveInstanceState(outState);
    }

    /**
     * <p>Resets the scroll to the saved scroll position.</p>
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void setScroll() {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                if ((scrollIndex > 0 || scrollTop > 0) && listView != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listView.setSelectionFromTop(scrollIndex, scrollTop);
                        }
                    });
                }
            }catch(Exception e){
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        }
    }

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
        //        adapter.setChildrenCursor(1, cursor);
//        Log.w(LOG_TAG, String.format("%s: %s %s %s %s", "OnLoadFinished", loader.isAbandoned(), loader.isReset(), loader.isStarted(), loader.dataToString(cursor)));
        adapter.setGroupCursor(cursor);

        if(!listView.isDirty() && !isFromRestore)
        try {
            final int i = adapter.getGroupCount();
            for (int j = 0; j < i; j++) {
                listView.expandGroup(j);
            }
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
        isFromRestore = false;
        hasSwappedCursor = true;
        setEmptyText();
        progressBar.setVisibility(View.GONE);
    }

    /**
     * <p>Removes the cursor from the adapter to clear the list view.</p>
     */
    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        adapter.setGroupCursor(null);
    }

               /*----- CUSTOM METHODS -----*/

    /**
     * <p>Default implementation returns {@code R.layout.generic_list}. The subclass can override this method to change this value.
     * If this value is changed {@link #getProgressBarId()} will also need to be changed.</p>
     *
     * @return the layout used for this list.
     */
    protected int getListLayout() {
        return R.layout.generic_expandable_section_list;
    }


    /**
     * <p>Default implementation returns {@code R.id.generic_list_progress}. The subclass can override this method to change this value
     * if {@link #getListLayout()} is overridden.  The id returned is used to set {@code progressBar}.</p>
     *
     * @return the id for the progress bar in the layout.
     */
    protected int getProgressBarId() {
        return R.id.generic_expandable_section_list_progress;
    }

    /**
     * <p>Default implementation returns {@code -1} (no header view added). The subclass can override this method to change this value.</p>
     *
     * @return the id for the list header to be added to the listview.
     */
    protected int getListHeaderId() {
        return -1;
    }

    protected abstract void fetchData();

    /**
     * <p>Hooks to subclass to create a new {@code SimpleCursorTreeAdapter} specific to this list.
     * The cursor inside the adapter should be set to null.</p>
     *
     * @return a new instance of {@code SimpleCursorTreeAdapter}
     */
    protected abstract SimpleCursorTreeAdapter createCursorAdapter();

    /**
     * <p>Hooks to subclass to pull a query of a database.
     * Note that this method is only called on a worker thread.
     * Also note that the database is already open for you.</p>
     *
     * @return the cursor representing the data to display.
     */
    protected abstract Cursor loadCursor();

    /**
     * <p>Default implementation returns null. The subclass can override this method to change this value.</p>
     *
     * @return the ViewBinder to control data for this list.
     */
    protected ViewBinder getViewBinder() {
        return null;
    }

    /**
     * <p>Kickstarts the loading process by starting the loader.</p>
     */
    public void loadData() {
        setProgressBarVisibility(View.VISIBLE);

        Loader<SimpleCursorTreeAdapter> loader;
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
    protected void finishedLoading() {
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
