package com.untappedkegg.rally.ui;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.untappedkegg.rally.BuildConfig;
import com.untappedkegg.rally.R;

public abstract class ViewPagerFragment extends Fragment {
    protected boolean updateAllChildren = false;

    /**
     * The {@link android.support.v4.app.FragmentPagerAdapter;} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    protected FragmentsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    protected ViewPager mViewPager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.viewpager, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSectionsPagerAdapter = new FragmentsPagerAdapter(getChildFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) getActivity().findViewById(R.id.pager);
        if (mViewPager != null) {
            mViewPager.setAdapter(mSectionsPagerAdapter);
            mViewPager.setCurrentItem(getInitialPageNumber(), true);
            if (updateAllChildren) {
                mViewPager.setOffscreenPageLimit(getNumPages() - 1);
            }
        }
    }

    protected Fragment getDisplayedFragment() {
        final String tag = "android:switcher:" + mViewPager.getId() + ":" + mViewPager.getCurrentItem();
        return getChildFragmentManager().findFragmentByTag(tag);
    }

    protected Fragment getChildByPosition(int position) {
        final String tag = "android:switcher:" + mViewPager.getId() + ":" + position;
        return getChildFragmentManager().findFragmentByTag(tag);
    }

    /**
     * <p>Updates the arguments in the {@code dataFragment} if it extends {@link com.untappedkegg.rally.ui.BaseList} or {@link com.untappedkegg.rally.ui.BaseDetails}., subclass should override to handle
     * updating the class arguments for other types of fragments.</p>
     */
    protected void updateArgs(String args, String query) {
        updateArgs(args, query, false);
    }

    /**
     * <p>Updates the arguments in the {@code dataFragment} if it extends {@link com.untappedkegg.rally.ui.BaseList}, {@link com.untappedkegg.rally.ui.BaseFragment} or {@link com.untappedkegg.rally.ui.BaseDetails}., subclass should override to handle
     * updating the class arguments for other types of fragments.</p>
     */
    protected void updateArgs(String args, String query, boolean updateAll) {
        Fragment[] fragments;
        if (updateAll) {
            fragments = new Fragment[getNumPages()];
            for (int i = 0; i < getNumPages(); i++) {
                fragments[i] = getChildByPosition(i);
            }
        } else {
            fragments = new Fragment[] {getDisplayedFragment()};
        }

        for (Fragment dataFragment : fragments) {
            try {
                if (dataFragment != null) {
                    /*if (dataFragment instanceof BaseList) {
                        ((BaseList) dataFragment).updateArgs(args, query);
                    } else*/ if (dataFragment instanceof BaseFragment) {
                        ((BaseFragment) dataFragment).updateArgs(args, query);
//                    } else if (dataFragment instanceof BaseMap) {
//                        ((BaseMap) dataFragment).updateArgs(args, query);
                    }
                }
            } catch (Exception e) {
                if(BuildConfig.DEBUG)
                    e.printStackTrace();
            }
        }
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
    protected abstract Fragment getItems(final int position);

    /**
     * @return The total number of pages, which this {@link FragmentPagerAdapter} will hold
     */
    protected abstract int getNumPages();

    protected abstract CharSequence getPageTitles(final int position);

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
