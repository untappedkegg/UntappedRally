package com.untappedkegg.rally.social;

import android.support.v4.app.Fragment;

import com.untappedkegg.rally.ui.ViewPagerFragment;

public final class SocialFragment extends ViewPagerFragment {

    public SocialFragment() {
    }

    @Override
    protected int getInitialPageNumber() {
        return 0;
    }

    @Override
    protected Fragment getItems(int position) {
        switch (position) {
            case 0:
                return new TwitterFragment();
            default:
                return new YouTubeFragment();
        }
    }

    @Override
    protected int getNumPages() {
        return 2;
    }

    @Override
    protected CharSequence getPageTitles(int position) {
        if (position == 0) return "Twitter";
        return "YouTube";
    }


}
