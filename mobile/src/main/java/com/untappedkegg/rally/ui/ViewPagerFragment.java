package com.untappedkegg.rally.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.untappedkegg.rally.R;

public abstract class ViewPagerFragment extends Fragment {

    /**
     * The {@link android.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    FragmentsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.viewpager, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSectionsPagerAdapter = new FragmentsPagerAdapter(getChildFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) getActivity().findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(getInitialPageNumber(), true);
    }

    /**
     * @return 0... {@link #getNumPages}
     */
    protected abstract int getInitialPageNumber();

    /**
     * getItems is called to instantiate the fragment for the given page.
     * Return a BaseFragment (defined as a static class in the home
     * package) with the page number as its lone argument.
     */
    protected abstract Fragment getItems(int position);

    /**
     * @return The total number of pages, which this {@link FragmentPagerAdapter} will hold
     */
    protected abstract int getNumPages();

    protected abstract CharSequence getPageTitles(int position);

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class FragmentsPagerAdapter extends FragmentPagerAdapter {

        public FragmentsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return getItems(position);
        }

        @Override
        public int getCount() {
            return getNumPages();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getPageTitles(position);
        }


    }


}
