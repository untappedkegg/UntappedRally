package com.untappedkegg.rally.news;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.data.DataFetcher;
import com.untappedkegg.rally.data.DbUpdated;
import com.untappedkegg.rally.interfaces.Refreshable;
import com.untappedkegg.rally.ui.BaseCarousel;
import com.untappedkegg.rally.ui.BaseDialogFragment;
import com.untappedkegg.rally.ui.loaders.CarouselAdapter;
import com.untappedkegg.rally.util.CommonIntents;
import com.untappedkegg.rally.util.DateManager;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NewsCarousel.Callbacks} interface
 * to handle interaction events.
 * Use the {@link NewsCarousel#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewsCarousel extends BaseCarousel implements DataFetcher.Callbacks, Refreshable {

    public NewsCarousel() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        if (AppState.NEWS_REFRESH) {
            this.refreshData();
            AppState.NEWS_REFRESH = false;
        }
    }

    @Override
    public void fetchData() {
        DbUpdated.open();
        if (DateManager.timeBetweenInMinutes(DbUpdated.lastUpdated_by_Source(AppState.MOD_NEWS)) > AppState.RSS_UPDATE_DELAY) {

            if (!DataFetcher.getInstance().news_isRunning()) {
                DataFetcher.getInstance().news_start(AppState.MOD_NEWS, this);
                progressBar.setVisibility(View.VISIBLE);
            }
        }
        DbUpdated.close();
    }

    /**
     * <p>Hooks to subclass to pull a query of a database. Note that this method is only called on a worker thread. Also note that the database is already
     * open for you.</p>
     *
     * @return the cursor representing the data to display.
     */
    @Override
    protected Cursor loadCursor() {
        return DbNews.fetchCarouselCurrentEvents();
    }

    /**
     * <p>Hooks to subclass to create a new {@code CarouselAdapter} specific to this list. The cursor inside the adapter should be set to null.</p>
     *
     * @return a new instance of {@code DetailsAdapter}
     */
    @Override
    protected CarouselAdapter createCarouselAdapter() {
        String[] from = new String[]{DbNews.ID, DbNews.TITLE, DbNews.LINK, DbNews.DESCR, DbNews.PUBDATE, DbNews.IMAGE_LINK, DbNews.IMAGE_LINK};
        int to[] = new int[]{R.id.carousel_page_id, R.id.carousel_page_title, R.id.carousel_page_uri, R.id.carousel_page_params, R.id.carousel_page_date, R.id.carousel_page_image, R.id.carousel_page_img_link};
        return new CarouselAdapter(getActivity(), null, from, to, R.layout.generic_carousel_page, this);
    }

    /**
     * <p>Hooks to subclass to check if the list should continue requerying the database for new information, usually from a webservice.</p>
     *
     * @return true if the data could still change, false if not.
     */
    @Override
    protected boolean shouldRequery() {
        return DataFetcher.getInstance().news_isRunning();
    }

    @Override
    protected ViewBinder getViewBinder() {
        return new NewsCarouselViewBinder();
    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        final String source = ((TextView) v.findViewById(R.id.carousel_page_img_link)).getText().toString();
        final String link = ((TextView) v.findViewById(R.id.carousel_page_uri)).getText().toString();
        final String title = ((TextView) v.findViewById(R.id.carousel_page_title)).getText().toString();

        if (!AppState.isNullOrEmpty(source) && !AppState.startsWithIgnoreCase(title, "Video")) {

            final String pubdate = ((TextView) v.findViewById(R.id.carousel_page_date)).getText().toString();
            final String descr = ((TextView) v.findViewById(R.id.carousel_page_params)).getText().toString();
            final Fragment dialog = BaseDialogFragment.newInstance(title, descr, pubdate, link, source, true);

            getActivity().getSupportFragmentManager().beginTransaction().add(dialog, dialog.toString()).commit();

        } else {
            CommonIntents.openUrl(getActivity(), link);
        }
        DbNews.open();
        DbNews.updateReadStatusById(((TextView) v.findViewById(R.id.carousel_page_id)).getText().toString());
        DbNews.close();


    }

    @Override
    public void onDataFetchComplete(Throwable throwable, String parser) {
        if (parser.equals(AppState.MOD_NEWS)) {
            if (!this.isDetached() && this.isVisible()) {
                this.loadPages();
            }
            DbUpdated.open();
            DbUpdated.updated_insert(AppState.MOD_NEWS);
            DbUpdated.close();
        }

    }

    @Override
    public void refreshData() {
        DataFetcher.getInstance().news_start(AppState.MOD_NEWS, this);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected String getCustomEmptyText() {
        return this.getString(R.string.news_no_new);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     */

    private class NewsCarouselViewBinder implements ViewBinder {

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (view.getId() == R.id.carousel_page_image) {
                ImageLoader.getInstance().displayImage(cursor.getString(columnIndex), (ImageView) view);
                view.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        }
    }

}
