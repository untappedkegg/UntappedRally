package com.keggemeyer.rallyamerica.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.keggemeyer.rallyamerica.AppState;
import com.keggemeyer.rallyamerica.data.BaseDbAccessor;

/**
 * <p>Base implementation for a Fragment.  Includes a requery manager.</p>
 *
 * @author russellja
 */
public abstract class BaseFragment extends Fragment {
    /* ----- CONSTANTS ----- */
    protected final String LOG_TAG = BaseFragment.class.getSimpleName() + "(" + ((Object) this).getClass().getSimpleName() + ")@" + Integer.toHexString(hashCode());

    /* ----- VARIABLES ----- */
    protected boolean dataFetched = false;
    /**
     * Behavior flag, used to determine if the data should be fetched on fragment creation.
     */
    protected boolean fetchOnCreate = true;
    /**
     * Behavior flag, used to determine if the cursor has been swapped in the adapter.
     */
    protected Thread requeryManager;
    protected ProgressBar progressBar;
    /**
     * <p>Number of milliseconds that the {@code requeryManager} should sleep between updates.  Default is {@link AppState#REQUERY_WAIT}.</p>
     */
    protected final int requeryWait = AppState.REQUERY_WAIT;

    /* ----- CONSTRUCTORS ----- */
    public BaseFragment() {
    }

	 /* ----- LIFECYCLE METHODS ----- */

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

    }

    @Override
    public void onPause() {
        super.onPause();
        if (requeryManager != null) {
            requeryManager.interrupt();
        }
    }

    /**
     * <p>Closes the database.</p>
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        BaseDbAccessor.close();
    }


    /**
     * <p>Saves the behavioral flags to the saved instance state bundle.  Override this to save more variables.</p>
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("dataFetched", dataFetched);
        outState.putBoolean("fetchOnCreate", fetchOnCreate);
        super.onSaveInstanceState(outState);
    }


	 /* ----- CUSTOM METHODS ----- */

    /**
     * Does nothing by default, subclasses should override this method to handle updating the class arguments.
     *
     * @param args  arguments associated with the {@code adn.GoMizzou.ARGS} passed to the fragment in the bundle
     * @param query argments associated with the {@code SearchManager.QUERY} passed to the fragment in the bundle
     */
    protected void updateArgs(String args, String query) {
        //	do nothing by default
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
     * <p>Starts the {@code requeryManager}.  Subclass can override to change this behavior.</p>
     */
    protected void startRequery() {
        setProgressBarVisibility(View.VISIBLE);
        requeryManager = new Thread(new BaseRequery(), BaseRequery.class.getSimpleName());
        requeryManager.start();
    }

    /**
     * <p>Default returns false.  Subclass can override to change this value.</p>
     *
     * @return true if the {@code requeryManager} should continue requerying, false if it should finish
     */
    protected abstract boolean shouldRequery();

    /**
     * <p>Called when the {@code requeryManager} updates.  Default does nothing, subclass can override to change this behavior.</p>
     */
    protected void updateRequery() {
        // do nothing by default
    }

    /**
     * <p>Called when the {@code requeryManager} finishes.  Default does nothing, subclass can override to change this behavior.</p>
     */
    protected void finishRequery() {
        // do nothing by default
    }

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

	/* ----- NESTED CLASSES ----- */

    /**
     * <p>Runnable started by calling {@link BaseFragment#startRequery()} and used to update the fragment regularly by calling {@link BaseFragment#updateRequery()} until
     * {@link BaseFragment#shouldRequery()} returns {@code false}.  Upon completion it calls {@link BaseFragment#finishRequery()} to update the fragment for the last time.</p>
     *
     * @author russellja
     */
    protected class BaseRequery implements Runnable {
        /* ----- INHERITED METHODS ----- */
        @Override
        public void run() {
            Log.d(LOG_TAG, "Starting RequeryManager.");

            try {
                do {
                    Log.d(LOG_TAG, "RequeryManager waiting.");
                    Thread.sleep(requeryWait);
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
                        updateRequery();
                    }
                }
            });
        }

        private void doFinish() {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isAdded()) {
                        setProgressBarVisibility(View.GONE);
                        finishRequery();
                    }
                }
            });
        }
    }
}
