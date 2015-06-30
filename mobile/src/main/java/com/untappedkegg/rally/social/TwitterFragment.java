package com.untappedkegg.rally.social;


import android.app.Activity;
import android.app.Fragment;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;
import com.twitter.sdk.android.tweetui.TwitterListTimeline;
import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.BuildConfig;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.data.NewDataFetcher;
import com.untappedkegg.rally.home.ActivityMain;
import com.untappedkegg.rally.interfaces.Refreshable;

/**
 * A simple {@link Fragment} subclass.
 */
public class TwitterFragment extends ListFragment implements Refreshable {

    /* ----- VARIABLES ----- */
    protected ProgressBar progressBar;
    protected View emptyView;
    protected TweetTimelineListAdapter adapter;
    //ActionBar
    private short position;
    private String[] modArray;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            position = getArguments().getShort(AppState.KEY_POSITION);
        } catch (NullPointerException e) {
            position = 0;
        }
        modArray = getResources().getStringArray(R.array.action_bar_modules);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final TwitterListTimeline listTimeline = new TwitterListTimeline.Builder()
                .slugWithOwnerScreenName("Rally", BuildConfig.DEV_NAME)
                .includeRetweets(true)
                .build();

        adapter = new TweetTimelineListAdapter(getActivity(), listTimeline);

        setListAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.generic_list, container, false);

        progressBar = (ProgressBar) view.findViewById(R.id.generic_list_progress);
        emptyView = view.findViewById(android.R.id.empty);
        ((TextView) emptyView).setText(getEmptyText());
        if (emptyView != null) {
            ListView listView = (ListView) view.findViewById(android.R.id.list);
            listView.setEmptyView(emptyView);
        }

        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                if (adapter.isEmpty()) {
                    if ( emptyView != null) {
                        ((TextView) emptyView).setText(R.string.no_tweets);
                    }
                } else {
                        progressBar.setVisibility(View.GONE);
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (position != 0) {
            ActivityMain.setCurPosition(position);
            try {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(modArray[position]);
            } catch (Exception ignored) {
            }
        }
//        refreshData();
        progressBar.setVisibility(View.GONE);
    }

//    @Override
    private String getEmptyText() {
        if (!NewDataFetcher.isInternetConnected()) {
            return getString(R.string.no_network_connection_msg);
        } else
        return getString(R.string.loading_tweets);
    }

    @Override
    public void refreshData() {
        adapter.refresh(null);
        progressBar.setVisibility(View.VISIBLE);
    }
}
