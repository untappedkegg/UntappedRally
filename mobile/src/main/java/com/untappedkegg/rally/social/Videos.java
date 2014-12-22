package com.untappedkegg.rally.social;

import android.support.v4.app.Fragment;

import com.untappedkegg.rally.ui.ViewPagerFragment;

public class Videos extends ViewPagerFragment {

    public Videos() {
    }

    @Override
    protected int getInitialPageNumber() {
        return 0;
    }

    @Override
    protected Fragment getItems(int position) {
        return new YouTubeFragment();
    }

    @Override
    protected int getNumPages() {
        return 1;
    }

    @Override
    protected CharSequence getPageTitles(int position) {
        return "YouTube";
    }


}
