package com.keggemeyer.rallyamerica.ui;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.keggemeyer.rallyamerica.AppState;
import com.keggemeyer.rallyamerica.R;
import com.keggemeyer.rallyamerica.data.DataFetcher;
import com.keggemeyer.rallyamerica.data.NewDataFetcher;
import com.keggemeyer.rallyamerica.ui.loaders.DetailsAdapter;
import com.keggemeyer.rallyamerica.ui.loaders.SimpleCursorLoader;

import java.util.Timer;
import java.util.TimerTask;

/**
 * <p>Base implementation for a BaseFragment that uses CursorLoaders with automatic, periodic requerying.</p>
 *
 * @author russellja
 */
public abstract class BaseDetails extends BaseFragment implements LoaderCallbacks<Cursor> {
    /* ----- CONSTANTS ----- */
    protected final String LOG_TAG = BaseDetails.class.getSimpleName() + "(" + ((Object) this).getClass().getSimpleName() + ")@" + Integer.toHexString(hashCode());

    /* ----- VARIABLES ----- */
    private volatile boolean hasSwappedCursor = true;
    protected DetailsAdapter detailsAdapter;
    //	protected LinearLayout buttonBar;

	/* ----- CONSTRUCTORS ----- */

    /**
     * Required constructor.
     */
    public BaseDetails() {
        dataFetched = false;
        fetchOnCreate = true;
        hasSwappedCursor = true;
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
    }


    /**
     * <p>Creates the fragment layout and gets the required views from it based on what is returned from {@link #getDetailsLayout()}, {@link #getProgressBarId()}, and
     * {@link #getButtonBarId()}.  Subclass should override those methods to change the values returned, or override this method with a call to get the view returned
     * from the super to get another view from the layout.</p>
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getDetailsLayout(), container, false);
        progressBar = (ProgressBar) view.findViewById(getProgressBarId());
        //		buttonBar = (LinearLayout) view.findViewById(getButtonBarId());
        return view;
    }

    /**
     * <p>Runs through the behavioral booleans to determine what to do next.  If the data has already been fetched ({@code dataFetched}), then it calls {@link #loadData()}.
     * Otherwise, if it should fetch the data when the fragment is created ({@code fetchOnCreate}), then it calls {@link #fetchData()} and {@link #loadData()}.
     * If the data has not been fetched and the data should not be fetched when the fragment is created, then it hides the {@code progressBar}.</p>
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //		Logger.i(LOG_TAG, "Loading DetailsAdapter.");
        detailsAdapter = createAdapter();
        detailsAdapter.setViewBinder(getViewBinder());
        detailsAdapter.setEmptyLayout(getEmptyViewLayout(), getEmptyViewId());

        //		BaseContainer.setTitle(this, getTitleText());

        if (dataFetched) {
            loadData();
        } else if (fetchOnCreate) {
            fetchData();
            dataFetched = true;
            loadData();
        } else {
            setProgressBarVisibility(View.GONE);
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


	/* ----- INHERITED METHODS ----- */
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
     * <p>Adds buttons to the button bar and sets the cursor in the adapter. Subclass should override to perform any other actions on the loader thread.</p>
     * <p/>
     * <p>Note that {@link #handleRequery()} is called when finishing.</p>
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //		if(buttonBar != null) {
        //			removeButtonBar();
        //			List<Button> buttonList = new ArrayList<Button>();
        //			createButtons(cursor, buttonList);
        //			addButtonsToButtonBar(buttonList);
        //		}

        detailsAdapter.swapCursor(cursor);
        detailsAdapter.setEmptyText(getEmptyText());
        hasSwappedCursor = true;
        handleRequery();
    }

    /**
     * <p>Removes the cursor from the adapter to clear the details view.</p>
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        detailsAdapter.swapCursor(null);
    }

    /* ----- CUSTOM METHODS ----- */

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
            getLoaderManager().restartLoader(getLoaderId(), null, this);
        }

        Log.d(LOG_TAG, "Loading RequeryManager.");
        requeryManager = new Thread(new DetailsRequery(), DetailsRequery.class.getSimpleName());
        requeryManager.start();
    }

    /**
     * <p>Returns the loader id that the details will automatically use. Only subclass this if the details needs control over this value, for example, if it uses
     * multiple loaders.</p>
     */
    protected int getLoaderId() {
        return 0;
    }

    /**
     * <p>Does nothing by default, subclass should override to handle fetching data via a network operation or some other method.</p>
     */
    public void fetchData() {
        //	do nothing by default
    }

    /**
     * <p>Default does nothing, subclass should override to handle requeries.</p>
     */
    protected void handleRequery() {
        // do nothing by default
    }

    /**
     * <p>Default does nothing, subclass should override to handle finishing the loader.</p>
     */
    protected void finishedLoading() {
        // do nothing by default
    }

    /**
     * <p>Default implementation returns {@code R.layout.generic_details}. The subclass can override this method to change this value.
     * If this value is changed, {@link #getButtonBarId()}, {@link #getProgressBarId()}, {@link #getDetailsContainerId()} will also need to be changed.</p>
     *
     * @return the layout used for this page
     */
    protected int getDetailsLayout() {
        return R.layout.generic_details;
    }

    /**
     * <p>Default implementation returns {@code R.id.generic_details_progress}. The subclass can override this method to change this value
     * if {@link #getDetailsLayout()} is overridden.  The id returned is used to set {@code progressBar}.</p>
     *
     * @return the id for the progress bar in the layout.
     */
    protected int getProgressBarId() {
        return R.id.generic_details_progress;
    }

    /**
     * <p>Default implementation returns {@code R.id.generic_details_container}. The subclass can override this method to change this value
     * if {@link #getDetailsLayout()} is overridden.  The id returned is used as the {@code container} in the {@code DetailsAdapter}.</p>
     *
     * @return the id for the container in the layout
     */
    protected int getDetailsContainerId() {
        return R.id.generic_details_container;
    }

    /**
     * <p>Default implementation returns {@code R.layout.generic_details_empty}.  Note that this does not return a view id within the layout, but a
     * seperate layout.  The subclass can override this method to use a separate layout for the empty view, but it isn't required even if
     * {@link #getDetailsLayout()} is overridden.</p>
     *
     * @return the empty view layout
     */
    protected int getEmptyViewLayout() {
        return R.layout.generic_details_empty;
    }

    /**
     * <p>Default implementation returns {@code R.id.details_empty}. The subclass can override this method to change this value
     * if {@link #getEmptyViewLayout()} is overridden.</p>
     *
     * @return the id for the empty view in the empty layout.
     */
    protected int getEmptyViewId() {
        return R.id.details_empty;
    }

    /**
     * <p>Returns the text to be inserted into the empty view by the {@code DetailsAdapter}.</p>
     * <p/>
     * <p>This method shouldn't be overridden to return custom empty text.  This method checks {@link DataFetcher#isInternetConnected()} as
     * well as {@link #shouldRequery()} to display a message in those cases.  Instead override {@link #getCustomEmptyText()} to set the custom empty
     * text or {@link #getContactingEmptyText()} to set the message to be displayed while performing the network operation.  If none of these
     * situations apply {@code R.string.generic_empty_list_text} is returned.</p>
     *
     * @return the empty message
     */
    protected String getEmptyText() {
        if (!NewDataFetcher.isInternetConnected()) {
            detailsAdapter.setEmptyViewOnClickListener(new OnEmptyViewClick());
            return getActivity().getResources().getString(R.string.no_network_connection_msg);
        } else if (requeryManagerIsAlive()) {
            return getContactingEmptyText();
        } else if (!AppState.isNullOrEmpty(getCustomEmptyText())) {
            return getCustomEmptyText();
        } else {
            return AppState.getApplication().getResources().getString(R.string.generic_empty_list_text);
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
     * <p>Default returns null.  Subclass can override this method to change this value.</p>
     * <p/>
     * <p>This method should be overridden to customize the message to be displayed while performing the network operation instead of
     * {@link #getEmptyText()}.</p>
     *
     * @return the customized message to be displayed while performing the network operation
     */
    protected String getContactingEmptyText() {
        return null;
    }

    /**
     * <p>Default implementation returns {@code R.id.generic_details_buttonbar}.  The subclass can override this method to change this value
     * if {@link #getDetailsLayout()} is overridden.  The id returned is used to set {@code buttonBar}.</p>
     *
     * @return the id for the button bar in the layout.
     */
    protected int getButtonBarId() {
        return R.id.generic_details_buttonbar;
    }

    /**
     * <p>Default implementation returns null. The subclass can override this method to change this value.</p>
     *
     * @return the ViewBinder to control data for the details page.
     */
    protected ViewBinder getViewBinder() {
        return null;
    }

    /**
     * <p>Should not be overridden.  To customize the buttons added to the button bar override {@link #createButtons(Cursor, List)}.</p>
     *
     * @param buttonList the list of buttons to be added to the button bar
     */
    //	@SuppressLint("NewApi")
    //	protected void addButtonsToButtonBar(List<Button> buttonList) {
    //		if(buttonList.size() > 0 && buttonBar.getVisibility() == View.GONE) {
    //			buttonBar.setVisibility(View.VISIBLE);
    //			int weightSum = (buttonList.size() >= 2) ? buttonList.size() : 3;
    //			buttonBar.setWeightSum(weightSum);
    //		}
    //
    //		for(Button button : buttonList) {
    ////			Logger.iFormat(LOG_TAG, "Loading button %s in button bar.", button.getText().toString());
    //
    ////			if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
    ////			    button.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.menubutton));
    ////			} else {
    ////			    button.setBackground(getActivity().getResources().getDrawable(R.drawable.menubutton));
    ////			}
    //
    ////			button.setTextAppearance(getActivity(), R.style.GoMizzouButton);
    //			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
    //			params.setMargins(10, 5, 10, 5);
    //			button.setLayoutParams(params);
    //			button.setGravity(Gravity.CENTER);
    //			button.setSingleLine(true);
    //			button.setEllipsize(TextUtils.TruncateAt.END);
    //			buttonBar.addView(button);
    //		}
    //	}
    //
    //	/**
    //	 * <p>Clears all buttons from the button bar.</p>
    //	 */
    //	protected void removeButtonBar() {
    ////		Logger.i(LOG_TAG, "Removing all buttons.");
    //		LinearLayout buttonBar = (LinearLayout) getActivity().findViewById(getButtonBarId());
    //
    //		if(buttonBar.getChildCount() > 0) {
    //			buttonBar.removeAllViews();
    //		}
    //
    //		if(buttonBar.getVisibility() == View.VISIBLE) {
    //			buttonBar.setVisibility(View.GONE);
    //		}
    //	}
    //
    //	/**
    //	 * <p>Does nothing by default, subclasses should override this method to create the buttons to be placed in the button bar.</p>
    //	 *
    //	 * @param cursor the {@code Cursor} populating the details
    //	 * @param buttonList the {@code List} of buttons to be passed to {@link #addButtonsToButtonBar(List)}
    //	 */
    //	protected void createButtons(Cursor cursor, List<Button> buttonList) {
    //		//	do nothing by default
    //	}

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
     * <p>Hooks to subclass to pull a query of a database. Note that this method is only called on a worker thread. Also note that the database is already
     * open for you.</p>
     *
     * @return the cursor representing the data to display.
     */
    protected abstract Cursor loadCursor();

    /**
     * <p>Hooks to subclass to create a new {@code DetailsAdapter} specific to this list. The cursor inside the adapter should be set to null.</p>
     *
     * @return a new instance of {@code DetailsAdapter}
     */
    protected abstract DetailsAdapter createAdapter();

    /**
     * <p>Hooks to subclass to check if the list should continue requerying the database for new information, usually from a webservice.</p>
     *
     * @return true if the data could still change, false if not.
     */
    //	protected abstract boolean shouldRequery();

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
     * <p>{@code Runnable} that regularly restarts the loader until {@link BaseDetails#shouldRequery()} returns false.
     * Note that {@link BaseDetails#finishedLoading()} is called when finishing.</p>
     */
    private class DetailsRequery implements Runnable {
        /* ----- INHERITED METHODS ----- */
        @Override
        public void run() {
            try {
                do {
                    while (!hasSwappedCursor) {
                        Log.d(LOG_TAG, "RequeryManager waiting.");
                        Thread.sleep(requeryWait);
                    }
                    hasSwappedCursor = false;
                    doUpdate();
                } while (shouldRequery());
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
                        getLoaderManager().restartLoader(0, null, BaseDetails.this);
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
                        detailsAdapter.setEmptyText(getEmptyText());
                        finishedLoading();
                    }
                }
            });
        }
    }
}
