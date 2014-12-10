package com.keggemeyer.rallyamerica.social;

import android.support.v4.app.Fragment;

import com.keggemeyer.rallyamerica.ui.ViewPagerFragment;

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
