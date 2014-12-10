/**
 *
 */
package com.keggemeyer.rallyamerica.news;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.keggemeyer.rallyamerica.AppState;
import com.keggemeyer.rallyamerica.R;
import com.keggemeyer.rallyamerica.data.DataFetcher;
import com.keggemeyer.rallyamerica.data.DbUpdated;
import com.keggemeyer.rallyamerica.interfaces.Refreshable;
import com.keggemeyer.rallyamerica.ui.BaseDialogFragment;
import com.keggemeyer.rallyamerica.ui.SectionList;
import com.keggemeyer.rallyamerica.util.CommonIntents;
import com.keggemeyer.rallyamerica.util.DateManager;

/**
 * SectionList Fragment to display the news stories
 *
 * @author Kyle
 */
public class NewsFragment extends SectionList implements DataFetcher.Callbacks, Refreshable {
    //	private Callbacks callbacks;
    private boolean isHomeFragment;

    /**
     *
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            isHomeFragment = savedInstanceState.getBoolean("isHomeFragment");
        }

    }

    //	@Override
    //	public void onActivityCreated(Bundle savedInstanceState) {
    //		super.onActivityCreated(savedInstanceState);
    //		dataFetched = false;
    //OnClickListeners must be set in onActivityCreated
    //	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //		activity.getActionBar().setDisplayShowTitleEnabled(true);
        //		getActivity().getActionBar().setTitle(R.string.app_name);

        try {
            //	    	Bundle bundle = getArguments();
            isHomeFragment = getArguments().getBoolean("isHomeFragment");
        } catch (NullPointerException e) {
            isHomeFragment = false;
        }
        //		try {
        //			callbacks = (Callbacks) activity;
        //		} catch (ClassCastException e) {
        //			throw new ClassCastException(activity.toString()
        //					+ " must implement HomeFragment.Callbacks.");
        //		}
    }

    /* ----- INHERITED METHODS ----- */
    //	Fragment
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("isHomeFragment", isHomeFragment);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected String getSectionField() {
        return isHomeFragment ? null : DbNews.SHORTDATE;
    }

    @Override
    protected Cursor loadCursor() {
        return isHomeFragment ? DbNews.fetchCurrentEvents() : DbNews.fetchAllNews();
    }

    @Override
    protected SimpleCursorAdapter createCursorAdapter() {
        String[] from = new String[]{DbNews.TITLE, DbNews.DESCR, DbNews.PUBDATE, DbNews.LINK, DbNews.SOURCE, DbNews.IMAGE_LINK, DbNews.ID, DbNews.STATUS, DbNews.PUBDATE};
        int[] to = new int[]{R.id.list_title, R.id.list_descr, R.id.list_date, R.id.list_link, R.id.list_icon, R.id.list_uri, R.id.list_id, R.id.read_status, R.id.list_date2};
        return new SimpleCursorAdapter(getActivity(), R.layout.generic_list_row, null, from, to, 0);
    }

    @Override
    protected boolean shouldRequeryData() { return DataFetcher.getInstance().news_isRunning(); }

    @Override
    protected ViewBinder getViewBinder() {
        return new NewsViewBinder(getActivity(), isHomeFragment);
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

//            if (!DataFetcher.getInstance().news_isRunning()) {
                DataFetcher.getInstance().news_start(AppState.MOD_NEWS, this);
                progressBar.setVisibility(View.VISIBLE);
//            }
        }
        DbUpdated.close();
    }

    @Override
    public void refreshData() {
        DataFetcher.getInstance().news_start(AppState.MOD_NEWS, this);
        progressBar.setVisibility(View.VISIBLE);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (!adapter.isSection(position)) {
            final String source = ((TextView) v.findViewById(R.id.list_uri)).getText().toString();
            final String link = ((TextView) v.findViewById(R.id.list_link)).getText().toString();
            //			String myId = ((TextView) v.findViewById(R.id.list_id)).getText().toString();
            //			String status = ((TextView) v.findViewById(R.id.read_status)).getText().toString();
            final String title = ((TextView) v.findViewById(R.id.list_title)).getText().toString();
            if (!AppState.isNullOrEmpty(source) && !AppState.startsWithIgnoreCase(title, "Video")) {
                // Call new fragment to display the Title, Image, and full Description for Michelin/Rally_America item

                final String pubdate = ((TextView) v.findViewById(R.id.list_date2)).getText().toString();
                final String descr = ((TextView) v.findViewById(R.id.list_descr)).getText().toString();
//                Log.w(LOG_TAG, descr);
                //                descr = Html.fromHtml(descr);
                //                descr = Html.fromHtml(descr, ImageLoader.getInstance()., null);
                final Fragment dialog = BaseDialogFragment.newInstance(title, descr, pubdate, link, source, true);
                this.getChildFragmentManager().beginTransaction().add(dialog, dialog.toString()).commit();

            } else {
                CommonIntents.openUrl(getActivity(), link);
            }
            if (!((TextView) v.findViewById(R.id.read_status)).getText().toString().equals(getActivity().getResources().getString(R.string.news_read))) {
                DbNews.open();
                DbNews.updateReadStatusById(((TextView) v.findViewById(R.id.list_id)).getText().toString());
                DbNews.close();
                loadList();
            }
        }
    }

	/*----- NESTED CLASSES -----*/
    //	private class NewsUpdateTimerTask extends TimerTask {

		/* ----- INHERITED METHODS ----- */
    //		@Override
    //		public void run() {
    ////			if(getActivity() == null) {
    ////				return;
    ////			}
    ////
    ////			getActivity().runOnUiThread(new Runnable() {
    ////				@Override
    ////				public void run() {
    //					refreshData();
    ////				}
    ////			});
    //		}
    //	}

/*	public static class NewsCursorAdapter extends SimpleCursorAdapter {

		public NewsCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);

		}

		@Override
		public void bindView(View v, Context ctx, Cursor c) {
			super.bindView(v, ctx, c);

			ViewHolder holder = (ViewHolder) v.getTag();
			if(holder == null) {
				holder= new ViewHolder();
				holder.icon = (ImageView) v.findViewById(R.id.list_icon);
				holder.description = (TextView) v.findViewById(R.id.list_descr);
			}
		}

		static class ViewHolder {
			ImageView icon;
			TextView title, description;
			int titleCol, descrCol, iconCol;
		}
	}
*/
}
