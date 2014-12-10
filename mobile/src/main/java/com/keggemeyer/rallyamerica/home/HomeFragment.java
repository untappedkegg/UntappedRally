package com.keggemeyer.rallyamerica.home;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.keggemeyer.rallyamerica.AppState;
import com.keggemeyer.rallyamerica.R;
import com.keggemeyer.rallyamerica.interfaces.Refreshable;
import com.keggemeyer.rallyamerica.news.NewsCarousel;

/**
 * Container {@link Fragment} to hold the Fragments that make up the 'Home' tab
 *
 * @author Kyle
 */
public class HomeFragment extends Fragment implements OnClickListener, Refreshable {


    private Callbacks callbacks;
    private TextView events;
    private TextView news;
    private int position;
    private String[] modArray;
        private static final Fragment nextEventFrag = new NextEventFragment();
    private static final Fragment newsFrag = new NewsCarousel();
//    private static final boolean isJB = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;

    /* ----- LIFECYCLE METHODS ----- */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //		activity.getActionBar().setDisplayShowTitleEnabled(true);
        //		getActivity().getActionBar().setTitle(R.string.app_name);
        try {
            callbacks = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement " + ((Object) this).getClass().getCanonicalName() + ".Callbacks");
        }
        position = getArguments().getInt(AppState.KEY_POSITION);
        modArray = getResources().getStringArray(R.array.action_bar_modules);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setTitle(modArray[position]);
        //        ((ListView) getActivity().findViewById(R.id.left_drawer)).setItemChecked(position, true);
        NavDrawerFragment.getListView().setItemChecked(position, true);


        ActivityMain2.setCurPosition((short) position);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {




        return inflater.inflate(R.layout.home_fragment, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final FragmentTransaction transaction = getChildFragmentManager().beginTransaction() ;
//        if (savedInstanceState == null) {
//            Bundle bundle = new Bundle();
//            bundle.putBoolean("isHomeFragment", true);

            //            Fragment schedFrag = new ScheduleFragment();
            //            schedFrag.setArguments(bundle);
            //		Fragment newsFrag = new NewsFragment();
//            Fragment newsFrag = new NewsCarousel();
//            newsFrag.setArguments(bundle);

            //            transaction.add(R.id.home_top, schedFrag, ScheduleFragment.class.getCanonicalName());
            transaction.replace(R.id.home_middle, nextEventFrag, NextEventFragment.class.getCanonicalName());
            transaction.replace(R.id.home_bottom, new NewsCarousel(), NewsCarousel.class.getCanonicalName());
//            transaction.add(R.id.home_bottom, newsFrag, NewsCarousel.class.getCanonicalName());
//            transaction.addToBackStack(null);
            transaction.commit();
//        } else if (!isJB) {
//            transaction.replace(R.id.home_middle, new NextEventFragment(), NextEventFragment.class.getCanonicalName());
//            transaction.replace(R.id.home_bottom, new NewsCarousel(), NewsCarousel.class.getCanonicalName());
//            transaction.commit();
//        }



        //get textviews
        //        events = (TextView) getActivity().findViewById(R.id.home_upcoming_text);
        //        		news = ((TextView) getActivity().findViewById(R.id.home_news_text));
        //        events.setOnClickListener(this);
        //        		news.setOnClickListener(this);
        getActivity().findViewById(R.id.home_news_text).setOnClickListener(this);
        getActivity().findViewById(R.id.home_next_event_text).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home_news_text:
                callbacks.onNavDrawerItemSelected(2);
                break;
            //            case R.id.home_upcoming_text:
            case R.id.home_next_event_text:
                callbacks.onNavDrawerItemSelected(1);
        }

    }


    public void refreshData() {
        //        ((Refreshable) getActivity().getSupportFragmentManager().findFragmentByTag(ScheduleFragment.class.getCanonicalName())).refreshData();
        ((Refreshable) this.getChildFragmentManager().findFragmentByTag(NextEventFragment.class.getCanonicalName())).refreshData();
        ((Refreshable) this.getChildFragmentManager().findFragmentByTag(NewsCarousel.class.getCanonicalName())).refreshData();
    }

    /* ----- NESTED INTERFACES ----- */
    public interface Callbacks {
        public void onNavDrawerItemSelected(int position);
    }


}
