package com.untappedkegg.rally.social;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import com.untappedkegg.rally.interfaces.Refreshable;
import com.untappedkegg.rally.ui.BaseGridView;

public final class YouTubeFragment extends BaseGridView implements NewDataFetcher.Callbacks, Refreshable {


    @Override
    public void onDataFetchComplete(Throwable throwable, String key) {
        if (key.equals(AppState.MOD_YOUTUBE)) {
            if (!this.isDetached() && this.isVisible()) {
                loadData();
            }
        }
    }

    @Override
    public void fetchData() {
        SocialFetcher.getInstance().youTubeStart(this, false);
    }


    @Override
    public void refreshData() {
        SocialFetcher.getInstance().youTubeStart(this, true);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected Cursor loadCursor() {
        return DbSocial.youtube_select();
    }

    @Override
    protected SimpleCursorAdapter createCursorAdapter() {
        String[] from = new String[]{DbSocial.YOUTUBE_ICON, DbSocial.YOUTUBE_TITLE, DbSocial.YOUTUBE_LINK};
        int[] to = new int[]{R.id.grid_image, R.id.grid_text, R.id.grid_link};
        return new SimpleCursorAdapter(getActivity(), R.layout.generic_grid_row, null, from, to, 0);
    }

    @Override
    protected boolean shouldRequery() {
        return SocialFetcher.getInstance().youTubeIsRunning();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String url = ((TextView) view.findViewById(R.id.grid_link)).getText().toString();
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }


    @Override
    protected ViewBinder getViewBinder() {
        return new YouTubeViewBinder();
    }

    public static class YouTubeViewBinder implements ViewBinder {

        @Override
        public boolean setViewValue(View v, Cursor c, int colIdx) {
            switch (v.getId()) {
                case R.id.list_icon:
                case R.id.grid_image:
                    v.setVisibility(View.VISIBLE);
                    ImageLoader.getInstance().displayImage(c.getString(colIdx), (ImageView) v);
                    return true;
                default:
                    return false;
            }

        }

    }

}
