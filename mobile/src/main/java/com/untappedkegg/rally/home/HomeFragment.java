package com.untappedkegg.rally.home;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.interfaces.NavDrawerItemSelected;
import com.untappedkegg.rally.interfaces.Refreshable;
import com.untappedkegg.rally.news.NewsCarousel;

/**
 * Container {@link Fragment} to hold the Fragments that make up the 'Home' tab
 *
 * @author UntappedKegg
 */
public final class HomeFragment extends Fragment implements OnClickListener, Refreshable {


    private NavDrawerItemSelected callbacks;
    private short position;
    private String[] modArray;

    /* ----- LIFECYCLE METHODS ----- */
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);

        try {
            callbacks = (NavDrawerItemSelected) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement " + ((Object) this).getClass().getCanonicalName() + ".Callbacks");
        }
        position = getArguments().getShort(AppState.KEY_POSITION);
        modArray = getResources().getStringArray(R.array.action_bar_modules);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActivityMain.setCurPosition(position);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(modArray[position]);
        Tracker mTracker = AppState.getDefaultTracker();
        mTracker.setScreenName("Home");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_fragment, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState == null) {
            final FragmentTransaction transaction = getChildFragmentManager().beginTransaction() ;
            transaction.replace(R.id.home_middle, new NextEventFragment(), NextEventFragment.class.getCanonicalName());
            transaction.replace(R.id.home_bottom, new NewsCarousel(), NewsCarousel.class.getCanonicalName());
            transaction.commit();
        }

        getActivity().findViewById(R.id.home_news_text).setOnClickListener(this);
        getActivity().findViewById(R.id.home_next_event_text).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home_news_text:
                callbacks.onNavDrawerItemSelected(2);
                break;
            case R.id.home_next_event_text:
                callbacks.onNavDrawerItemSelected(1);
        }

    }


    public void refreshData() {
        try {
            ((Refreshable) this.getChildFragmentManager().findFragmentByTag(NextEventFragment.class.getCanonicalName())).refreshData();
        } catch (NullPointerException ignored) {}

        try {
            ((Refreshable) this.getChildFragmentManager().findFragmentByTag(NewsCarousel.class.getCanonicalName())).refreshData();
        } catch(NullPointerException ignored) {}
    }


}
