package com.untappedkegg.rally.event;

import android.app.Activity;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.data.NewDataFetcher;
import com.untappedkegg.rally.ui.BaseGridView;
import com.untappedkegg.rally.util.CommonIntents;

public class EventPhotos extends BaseGridView implements NewDataFetcher.Callbacks {


    private String link;
    private String[] linkPts;

    /*----- LIFECYCLE METHODS -----*/
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        link = getArguments().getString(AppState.KEY_ARGS);
        linkPts = link.split("/");
    }


    /*----- INHERITED METHODS -----*/
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final String link = ((TextView) view.findViewById(R.id.grid_link)).getText().toString();
        //		Uri uri = Uri.parse(ImageLoader.getInstance().getLoadingUriForView((ImageView) view.findViewById(R.id.grid_image)));
        CommonIntents.openImage(getActivity(), link);

    }

    @Override
    protected void fetchData() {
        //		if (DateManager.timeBetweenInDays(DbUpdated.lastUpdated_by_Source(AppState.MOD_PICS + linkPts[5] + linkPts[4])) > AppState.STAND_UPDATE_DELAY) {

        EventFetcher.getInstance().start(this, link, linkPts[4]);
        progressBar.setVisibility(View.VISIBLE);

        //		}
    }

    @Override
    protected SimpleCursorAdapter createCursorAdapter() {
        String[] from = new String[]{DbEvent.PHOTO_URL, DbEvent.PHOTO_TITLE, DbEvent.PHOTO_URL};
        int[] to = new int[]{R.id.grid_image, R.id.grid_text, R.id.grid_link};
        return new SimpleCursorAdapter(getActivity(), R.layout.generic_grid_row, null, from, to, 0);
    }

    @Override
    protected Cursor loadCursor() {
        return DbEvent.getPhotosByEvent_and_Year(linkPts[5], linkPts[4]);
    }

    @Override
    protected ViewBinder getViewBinder() {
        return new PhotoViewBinder();
    }

    @Override
    protected boolean shouldRequery() {
        return EventFetcher.getInstance().isRunning();
    }

    //Callbacks
    @Override
    public void onDataFetchComplete(Throwable throwable, String parser) {
        if (!this.isDetached() && this.isVisible()) {
            this.loadData();
        }
        //		DbUpdated.open();
        //		DbUpdated.updated_insert(AppState.MOD_PICS + linkPts[5] + linkPts[4]);
        //		DbUpdated.close();
    }

    public static class PhotoViewBinder implements ViewBinder {

        @Override
        public boolean setViewValue(View v, Cursor c, int colIdx) {
            switch (v.getId()) {

                case R.id.grid_image:
                    //				v.setVisibility(View.VISIBLE);
                    ImageLoader.getInstance().displayImage(c.getString(colIdx), (ImageView) v);
                    return true;
                case R.id.grid_text:
                    String uri = c.getString(colIdx);
                    TextView tv = ((TextView) v);
                    tv.setLines(1);
                    if (AppState.isNullOrEmpty(uri)) {
                        //                    tv.setBackgroundColor(AppState.getApplication().getResources().getColor(android.R.color.transparent));
                        tv.setVisibility(View.INVISIBLE);
                    } else {
                        tv.setText(uri);
                        tv.setVisibility(View.VISIBLE);
                    }
                    return true;
                default:
                    return false;
            }
        }

    }

}
