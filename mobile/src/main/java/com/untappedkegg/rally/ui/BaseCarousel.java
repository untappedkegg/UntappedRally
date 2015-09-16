package com.untappedkegg.rally.ui;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.data.DataFetcher;
import com.untappedkegg.rally.data.NewDataFetcher;
import com.untappedkegg.rally.ui.loaders.CarouselAdapter;
import com.untappedkegg.rally.ui.loaders.SimpleCursorLoader;
import com.untappedkegg.rally.ui.view.CarouselViewPager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * <p>Base implementation for a BaseFragment that uses CursorLoaders with automatic, periodic requerying to populate a {@code ViewPager}.</p>
 *
 * @author russellja
 */
public abstract class BaseCarousel extends BaseFragment implements LoaderCallbacks<Cursor>, View.OnClickListener, CarouselViewPager.Callbacks {
    /* ----- CONSTANTS ----- */
    //	protected final String LOG_TAG = BaseCarousel.class.getSimpleName() + "(" + getClass().getSimpleName() + ")@" + Integer.toHexString(hashCode());

	/* ----- VARIABLES ----- */
    /**
     * Behavior flag, used to determine if the data has been fetched.
     */
    protected boolean dataFetched = false;
    protected CarouselAdapter adapter;
    protected CarouselViewPager pager;
    //	protected ViewPagerDots pagerDots;
    protected Timer timer;
    protected CarouselTimerTask carouselTimerTask = new CarouselTimerTask();
    private volatile boolean hasSwappedCursor = true;
    protected boolean autoRotate;

	/* ----- CONSTRUCTORS ----- */

    /**
     * Required constructor.
     */
    public BaseCarousel() {
    }

	/* ----- LIFECYCLE METHODS ----- */

    /**
     * {@inheritDoc}
     * <p/>
     * <p>Sets {@code dataFetched} to false and {@code hasSwappedCursor} to true.</p>
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        dataFetched = false;
        hasSwappedCursor = true;
        autoRotate = true;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>Opens the database, tracks the fragment in analytics, and tries to get {@code dataFetched} from the savedInstanceState.</p>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //		Analytics.trackScreen(getActivity(), getClass().getName());
        dataFetched = (savedInstanceState != null) ? savedInstanceState.getBoolean("dataFetched") : dataFetched;
        autoRotate = (savedInstanceState != null) ? savedInstanceState.getBoolean("autoRotate") : autoRotate;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>Creates the fragment layout and gets the required views from it based on what is returned from {@link #getLayout()}, {@link #getProgressBarId()},
     * and {@link #getPagerId()}.  Subclass should override those methods to change the values returned, or override this method
     * with a call to get the view returned from the super to get another view from the layout.</p>
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayout(), container, false);
        progressBar = (ProgressBar) view.findViewById(getProgressBarId());
        pager = (CarouselViewPager) view.findViewById(getPagerId());
        return view;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>Runs through the behavioral booleans to determine what to do next.  If the data has already been fetched ({@code dataFetched}), then it calls {@link #loadPages()}.
     * Otherwise, it calls {@link #fetchData()} and {@link #loadPages()}.</p>
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = createCarouselAdapter();
        adapter.setViewBinder(getViewBinder());
        pager.setAdapter(adapter);
        pager.setCallbacks(this);
        pager.setPageMarginDP(getPageMarginDP());

        if (dataFetched) {
            loadPages();
        } else {
            fetchData();
            dataFetched = true;
            loadPages();
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>Stops the {@code timer} and the {@code requeryManager} if either is still running.</p>
     */
    @Override
    public void onPause() {
        super.onPause();
        pauseCarousel();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>Closes the database.</p>
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        this.pauseCarousel();
    }

	/* ----- INHERITED METHODS ----- */
    //	Fragment

    /**
     * {@inheritDoc}
     * <p/>
     * <p>Saves {@code dataFetched} to the saved instance state bundle.  Override this to save more variables.</p>
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("dataFetched", dataFetched);
        outState.putBoolean("autoRotate", autoRotate);
        super.onSaveInstanceState(outState);
    }

    //	LoaderCallbacks

    /**
     * {@inheritDoc}
     * <p/>
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
     * {@inheritDoc}
     * <p/>
     * <p>Sets the cursor in the adapter and updates the {@code pagerDots}. Subclass should override to perform any other actions on the loader thread.</p>
     * <p/>
     * <p>Note that {@link #handleRequery()} is called when finishing.</p>
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
        //		if(pagerDots != null) {
        //			pagerDots.onDataSetChanged();
        //		}
        hasSwappedCursor = true;
        handleRequery();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>Removes the cursor from the adapter to clear the pager view and updates the {@code pagerDots}.</p>
     */
    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        adapter.swapCursor(null);
        //		if(pagerDots != null) {
        //			pagerDots.onDataSetChanged();
        //		}
        adapter.setEmptyText(getEmptyText());
    }

    // CarouselViewPager.Callbacks

    /**
     * {@inheritDoc}
     * <p/>
     * <p>Restarts the timer on {@code ViewPager} touch events.</p>
     */
    @Override
    public boolean onCarouselInterceptTouchEvent() {
        if (autoRotate) {
            pauseCarousel();
            resumeCarousel();
        }
        return false;
    }

	/* ----- CUSTOM METHODS ----- */

    /**
     * <p>Kickstarts the loading process by starting the loader.</p>
     */
    public void loadPages() {
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

        requeryManager = new Thread(new CarouselRequery(), CarouselRequery.class.getSimpleName());
        requeryManager.start();
    }

    /**
     * <p>Does nothing by default, subclass should override to handle fetching data via a network operation or some other method.</p>
     */
    protected void fetchData() {
        // do nothing by default
    }

    /**
     * <p>Default implementation returns {@code R.layout.generic_carousel}. The subclass can override this method to change this value.
     * If this value is changed, {@link #getProgressBarId()}, {@link #getPagerId()} will also need to be changed.</p>
     *
     * @return the layout used for this page
     */
    protected int getLayout() {
        return R.layout.generic_carousel;
    }

    /**
     * <p>Default implementation returns {@code R.id.generic_carousel_progress}. The subclass can override this method to change this value
     * if {@link #getLayout()} is overridden.  The id returned is used to set {@code progressBar}.</p>
     *
     * @return the id for the progress bar in the layout.
     */
    protected int getProgressBarId() {
        return R.id.generic_carousel_progress;
    }

    /**
     * <p>Default implementation returns {@code R.id.generic_carousel_pager}. The subclass can override this method to change this value
     * if {@link #getLayout()} is overridden.  The id returned is used to set {@code pager}.</p>
     *
     * @return the id for the {@code CarouselViewPager} in the layout.
     */
    protected int getPagerId() {
        return R.id.generic_carousel_pager;
    }

    /**
     * <p>Default implementation returns {@code R.id.generic_carousel_dots}. The subclass can override this method to change this value
     * if {@link #getLayout()} is overridden.  The id returned is used to set {@code pagerDots}.</p>
     *
     * @return the id for the {@code ViewPagerDots} in the layout.
     */
    //	protected int getPagerDotsId() {
    //		return R.id.generic_carousel_dots;
    //	}

    /**
     * <p>Default implementation returns null. The subclass can override this method to change this value.</p>
     *
     * @return the {@code ViewBinder} to control data for the carousel.
     */
    protected ViewBinder getViewBinder() {
        return null;
    }

    /**
     * <p>Returns the loader id that the carousel will automatically use. Only subclass this if the details needs control over this value, for example, if it uses
     * multiple loaders.</p>
     */
    protected int getLoaderId() {
        return 0;
    }

    /**
     * <p>Default does nothing, subclasses should override this method to handle requeries.</p>
     */
    protected void handleRequery() {
        // do nothing by default
    }

    /**
     * <p>Handles the completion of this carousel's loader. The subclass should override this method to handle this event.</p>
     */
    protected void finishedLoading() {
        // do nothing by default
    }

    /**
     * <p>Returns the text to be inserted into the empty view by the {@code CarouselAdapter}.</p>
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
            return getString(R.string.no_network_connection_msg);
        } else if (requeryManagerIsAlive()) {
            return getContactingEmptyText();
        } else if (!TextUtils.isEmpty(getCustomEmptyText())) {
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
     * <p>Stops the timer that rotates the carousel.  Sublcass can override to change this behavior.</p>
     */
    public void pauseCarousel() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    /**
     * <p>Starts the timer that rotates the carousel.  Subclass can override to change this behavior.</p>
     */
    public void resumeCarousel() {
        //        pauseCarousel();
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        timer = new Timer();
        timer.purge();
        timer.scheduleAtFixedRate(new CarouselTimerTask(), AppState.CAROUSEL_START_DELAY_SECS * 1000, AppState.CAROUSEL_DELAY_SECS * 1000);

    }

    /**
     * <p>Gets the visibility of the {@code progressBar}, if not null.</p>
     *
     * @return the visibility of the {@code progressBar} or -1 if it is null
     */
    private int getProgressBarVisibility() {
        if (progressBar == null) {
            return -1;
        } else {
            return progressBar.getVisibility();
        }
    }

    protected int getPageMarginDP() {
        return -20;
    }

    /**
     * <p>Hooks to subclass to pull a query of a database. Note that this method is only called on a worker thread. Also note that the database is already
     * open for you.</p>
     *
     * @return the cursor representing the data to display.
     */
    protected abstract Cursor loadCursor();

    /**
     * <p>Hooks to subclass to create a new {@code CarouselAdapter} specific to this list. The cursor inside the adapter should be set to null.</p>
     *
     * @return a new instance of {@code DetailsAdapter}
     */
    protected abstract CarouselAdapter createCarouselAdapter();

    /**
     * <p>Hooks to subclass to check if the list should continue requerying the database for new information, usually from a webservice.</p>
     *
     * @return true if the data could still change, false if not.
     */
    protected abstract boolean shouldRequery();

	/* ----- NESTED CLASSES ----- */

    /**
     * <p>{@code Runnable} that regularly restarts the loader until {@link #shouldRequery()} returns false.
     * Note that {@link BaseCarousel#finishedLoading} and {@link BaseCarousel#resumeCarousel()} are called when finishing.</p>
     */
    private class CarouselRequery implements Runnable {
        @Override
        public void run() {
            try {
                do {
                    while (!hasSwappedCursor) {
                        Log.d(LOG_TAG, "RequeryManager waiting.");
                        Thread.sleep(AppState.REQUERY_WAIT);
                    }
                    hasSwappedCursor = false;
                    doUpdate();
                } while (shouldRequery());
            } catch (InterruptedException e) {
                Log.v(LOG_TAG, "RequeryManager interrupted");
            }

            doFinish();
        }

        private void doUpdate() {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isAdded()) {
                        getLoaderManager().restartLoader(getLoaderId(), null, BaseCarousel.this);
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
     * <p>A {@code TimerTask} used to rotate the carousel.</p>
     */
    private class CarouselTimerTask extends TimerTask {
		/* ----- INHERITED METHODS ----- */

        /**
         * Rotates the carousel to the next item.
         */
        @Override
        public void run() {
            if (getActivity() == null) {
                return;
            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int page = pager.getCurrentItem();
                    if (page >= adapter.getCount() - 1) {
                        page = 0;
                    } else {
                        page++;
                    }
                    pager.setCurrentItem(page, true);
                }
            });
        }
    }

    /**
     * <p>A {@code TimerTask} used to call the UI methods needed to finish up after the {@link #requeryManager} finishes.</p>
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
                        adapter.setEmptyText(getEmptyText());
                        finishedLoading();
                        if (autoRotate) {
                            resumeCarousel();
                        }
                    }
                }
            });
        }
    }
}
