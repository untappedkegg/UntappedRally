package com.untappedkegg.rally.news;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.data.DbUpdated;
import com.untappedkegg.rally.data.NewDataFetcher;
import com.untappedkegg.rally.interfaces.Refreshable;
import com.untappedkegg.rally.ui.BaseDialogFragment;
import com.untappedkegg.rally.ui.SectionList;
import com.untappedkegg.rally.util.CommonIntents;
import com.untappedkegg.rally.util.DateManager;
import com.untappedkegg.rally.util.DialogManager;

/**
 * SectionList Fragment to display the news stories
 *
 * @author UntappedKegg
 */
public final class NewsFragment extends SectionList implements NewDataFetcher.Callbacks, Refreshable, AdapterView.OnItemLongClickListener {

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setOnItemLongClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (AppState.NEWS_REFRESH) {
            this.refreshData();
            AppState.NEWS_REFRESH = false;
        }
        loadList();

        Tracker mTracker = AppState.getDefaultTracker();
        mTracker.setScreenName("News");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    /* ----- INHERITED METHODS ----- */
    @Override
    protected String getSectionField() {
        return DbNews.SHORTDATE;
    }

    @Override
    protected Cursor loadCursor() {
        return DbNews.fetchAllNews();
    }

    @Override
    protected SimpleCursorAdapter createCursorAdapter() {
        String[] from = new String[]{DbNews.TITLE, DbNews.DESCR, DbNews.PUBDATE, DbNews.LINK, DbNews.SOURCE, DbNews.IMAGE_LINK, DbNews.ID, DbNews.STATUS, DbNews.PUBDATE};
        int[] to = new int[]{R.id.list_title, R.id.list_descr, R.id.list_date, R.id.list_link, R.id.list_icon, R.id.list_uri, R.id.list_id, R.id.read_status, R.id.list_date2};
        return new SimpleCursorAdapter(getActivity(), R.layout.generic_list_row, null, from, to, 0);
    }

    @Override
    protected boolean shouldRequery() { return NewsFetcher.getInstance().isRunning(); }

    @Override
    protected ViewBinder getViewBinder() {
        return new NewsViewBinder(getActivity());
    }

    @Override
    public void onDataFetchComplete(Throwable throwable, String parser) {
        if (parser.equals(AppState.MOD_NEWS)) {
            if (!this.isDetached() && this.isVisible()) {
                loadList();
            }
            DbUpdated.open();
            DbUpdated.updated_insert(AppState.MOD_NEWS);
            DbUpdated.close();
        }
    }

    @Override
    public void fetchData() {
        DbUpdated.open();
        if (DateManager.timeBetweenInMinutes(DbUpdated.lastUpdated_by_Source(AppState.MOD_NEWS)) <= AppState.RSS_UPDATE_DELAY) {
            loadList();
        } else {
            loadList();
            NewsFetcher.getInstance().news_start(this);
                progressBar.setVisibility(View.VISIBLE);
        }
        DbUpdated.close();
    }

    @Override
    public void refreshData() {
        NewsFetcher.getInstance().news_start(this);
        progressBar.setVisibility(View.VISIBLE);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (!adapter.isSection(position)) {
            final String source = ((TextView) v.findViewById(R.id.list_uri)).getText().toString();
            final String link = ((TextView) v.findViewById(R.id.list_link)).getText().toString();
            final String title = ((TextView) v.findViewById(R.id.list_title)).getText().toString();

            final String descr = ((TextView) v.findViewById(R.id.list_descr)).getText().toString();
            if (!descr.endsWith("...") && !descr.endsWith(Html.fromHtml("&#8594;").toString()) && !AppState.startsWithIgnoreCase(title, "Video")) {
                // Call new fragment to display the Title, Image, and full Description for Michelin/Rally_America item

                final String pubdate = ((TextView) v.findViewById(R.id.list_date2)).getText().toString();
                final Fragment dialog = BaseDialogFragment.newInstance(title, descr, pubdate, link, source, true);

                this.getChildFragmentManager().beginTransaction().add(dialog, dialog.toString()).commit();

            } else {
                CommonIntents.openUrl(getActivity(), link);
            }
            if (!((TextView) v.findViewById(R.id.read_status)).getText().toString().equals(getString(R.string.news_read))) {
                DbNews.open();
                DbNews.updateReadStatusById(((TextView) v.findViewById(R.id.list_id)).getText().toString(), true);
                DbNews.close();
                loadList();
            }
        }
    }

    @Override
    protected String getCustomEmptyText() {
        return this.getString(R.string.news_no_new);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        if (((TextView) view.findViewById(R.id.read_status)).getText().toString().equals(getString(R.string.news_read))) {
            final String dbId = ((TextView) view.findViewById(R.id.list_id)).getText().toString();
            // Ask the User if they want to mark this item unread
            DialogManager.raiseTwoButtonDialog(getActivity(), getString(R.string.news_mark_unread), null, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DbNews.updateReadStatusById(dbId, false);
                    loadList();
                }
            }, null);
            return true;
        }
        return false;
    }


}
