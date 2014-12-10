package com.keggemeyer.rallyamerica.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.keggemeyer.rallyamerica.AppState;
import com.keggemeyer.rallyamerica.R;
import com.keggemeyer.rallyamerica.data.DataFetcher;
import com.keggemeyer.rallyamerica.event.EventActivity;
import com.keggemeyer.rallyamerica.interfaces.Refreshable;
import com.keggemeyer.rallyamerica.news.NewsFragment;
import com.keggemeyer.rallyamerica.schedule.ScheduleFragment;
import com.keggemeyer.rallyamerica.util.CommonIntents;
import com.keggemeyer.rallyamerica.util.DialogManager;

import java.util.Locale;

public class Home extends FragmentActivity implements ScheduleFragment.Callbacks/*, HomeFragment.Callbacks*/ {

    //	static Fragment news = new NewsFragment();
    //	static Fragment schedule = new ScheduleFragment();
    //	final ActionBar mActionBar = getActionBar();
    //	static Fragment todaysNews = new TodaysNews();
    //	static Fragment upcomingEvents = new UpcomingEvents();
    /**
     * The {@link android.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //		AppState.hideSystemUI(this);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(2, true);

        //		try {
        //	        ViewConfiguration config = ViewConfiguration.get(this);
        //	        Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
        //	        if(menuKeyField != null) {
        //	            menuKeyField.setAccessible(true);
        //	            menuKeyField.setBoolean(config, false);
        //	        }
        //	    } catch (Exception ex) {
        //	        // Ignore
        //	    }

        //		DataFetcher.getInstance().events_start();
        getActionBar().setDisplayShowTitleEnabled(true);
        //		getActionBar().setTitle(R.string.app_name);
        getActionBar().setSubtitle(R.string.app_name);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        //		getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setTitle(R.string.app_name);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            //	    case R.id.action_feedback:
            //	    	intent = new Intent(Home.this, Feedback.class);
            //	        startActivity(intent);
            //	        break;
            case R.id.menu_refresh:
                Fragment fragment = getDisplayedFragment();
                if (fragment.isVisible()) {
                    if (fragment instanceof Refreshable) {
                        ((Refreshable) fragment).refreshData();
                    }
                }
                break;
            //	    case R.id.action_settings:
            //	    	intent = new Intent(Home.this, SettingsActivity.class);
            //	    	startActivityForResult(intent, 0);
            ////	    	startActivity(intent);
            ////	    	AppState.startNewFragment(this, SettingsFragment.class.getName(), null, null, null);
            //	    	break;
            default:
                DialogManager.raiseUIError(this, "Error", "This feature has not been implemented yet", false);

        }
        return false;
    }

    public Fragment getDisplayedFragment() {
        String tag = "android:switcher:" + mViewPager.getId() + ":" + mViewPager.getCurrentItem();
        return getSupportFragmentManager().findFragmentByTag(tag);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            //	    	AppState.hideSystemUI(this);
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            // getItem is called to instantiate the fragment for the given page.
            // Return a BaseFragment (defined as a static class in the home
            // package) with the page number as its lone argument.
            switch (position) {
                case 2://switch to 2
                    //				fragment = new ScheduleFragment();
                    //				return new ScheduleFragment();
                    return new HomeFragment();
                //				break;
                case 1:
                    //				fragment = new NewsFragment();
                    //				break;
                    return new NewsFragment();
                case 0:
                    return new ScheduleFragment();
                //				fragment = new RadioVideoFragment();
                //				break;
                case 3:
                    return new StandingsFragment();
                //			default:
                //				fragment = new BaseFragment();
            }

            Bundle args = new Bundle();
            args.putInt(AppState.KEY_ARGS, position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 5 total pages.
            return AppState.PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = AppState.localeUser;
            switch (position) {
                case 2:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 0:
                    return getString(R.string.title_section3).toUpperCase(l);
                case 3:
                    return getString(R.string.title_section4).toUpperCase(l);
                case 4:
                    return getString(R.string.title_section5).toUpperCase(l);
            }
            return null;
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataFetcher.getInstance().global_interrupt();
        //		locationFetcher.stopUpdates();
    }

    @Override
    public void showEventDetail(String fragment, String args, String eventName) {
        CommonIntents.startNewContainer(this, fragment, args, EventActivity.class, eventName);

    }

    //	@Override
    //	public void selectItem(int position) {
    //		// TODO Auto-generated method stub
    //
    //	}


}
