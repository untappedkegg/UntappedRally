package com.untappedkegg.rally.ui;

import android.app.SearchManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
//import adn.GoMizzou.util.Logger;

/**
 * <p>Base implementation of {@link BaseList} that has a child fragment that extends {@link BaseCarousel}.</p>
 *
 * @author russellja
 */
public abstract class CarouselList extends BaseList {
    /* ----- VARIABLES ----- */
    /**
     * Argument associated with the {@code adn.GoMizzou.ARGS}.
     */
    protected String args;
    /**
     * Argument associated with the {@code SearchManager.QUERY}.
     */
    protected String query;

	/* ----- LIFECYCLE METHODS ----- */

    /**
     * <p>Calls {@link #attachCarouselFragment()} to attach the carousel fragment.</p>
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        attachCarouselFragment();
    }

	/* ----- INHERITED METHODS ----- */
    // BaseList

    /**
     * <p>Default implementation returns {@code R.layout.carousel_list}. The subclass can override this method to change this value.</p>
     */
    @Override
    protected int getListLayout() {
        return R.layout.carousel_list;
    }

    /**
     * <p>Default implementation returns {@code R.layout.carousel_list_progress}. The subclass can override this method to change this value.</p>
     */
    @Override
    protected int getProgressBarId() {
        return R.id.carousel_list_progress;
    }

	/* ----- CUSTOM METHODS ----- */

    /**
     * <p>Attaches the carousel fragment returned by {@link #getCarouselFragment()} to the container whose id is returned by {@link #getCarouselContainerId()}
     * and adds it to the back stack.</p>
     */
    protected void attachCarouselFragment() {
        BaseCarousel carousel = getCarouselFragment();
        FragmentManager man = getChildFragmentManager();

        if (!AppState.isNullOrEmpty(args) || !AppState.isNullOrEmpty(query)) {
            Bundle bundle = new Bundle();
            if (!AppState.isNullOrEmpty(args)) bundle.putString(AppState.KEY_ARGS, args);
            if (!AppState.isNullOrEmpty(query)) bundle.putString(SearchManager.QUERY, query);
            carousel.setArguments(bundle);
        }

        int count = man.getBackStackEntryCount();
        if (count > 0) {
            // emulate back button then move forward.
            man.popBackStack(man.getBackStackEntryAt(count - 1).getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        //		Logger.iFormat(LOG_TAG, "Attaching %s for content fragment with tag %s.", carousel.getClass().getSimpleName(), carousel.getClass().getName());
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(getCarouselContainerId(), carousel, ((Object) carousel).getClass().getName());
        transaction.commit();
    }

    /**
     * <p>Default implementation returns {@code R.layout.carousel_list_carousel}. The subclass can override this method to change this value.</p>
     *
     * @return the id of the carousel container
     */
    protected int getCarouselContainerId() {
        return R.id.carousel_list_carousel;
    }

    /**
     * <p>Calls the {@link BaseCarousel#loadPages()} method in the child carousel fragment to load the carousel pages.</p>
     */
    public void loadPages() {
        BaseCarousel fragment = (BaseCarousel) getChildFragmentManager().findFragmentById(getCarouselContainerId());
        fragment.loadPages();
    }

    /**
     * <p>Calls the {@link BaseCarousel#pauseCarousel()} method in the child carousel fragment to pause the carousel rotation.</p>
     */
    public void pauseCarousel() {
        BaseCarousel fragment = (BaseCarousel) getChildFragmentManager().findFragmentById(getCarouselContainerId());
        fragment.pauseCarousel();
    }

    /**
     * <p>Calls the {@link BaseCarousel#resumeCarousel()} method in the child carousel fragment to resume the carousel rotation.</p>
     */
    public void resumeCarousel() {
        BaseCarousel fragment = (BaseCarousel) getChildFragmentManager().findFragmentById(getCarouselContainerId());
        fragment.resumeCarousel();
    }

    /**
     * <p>Hooks into the subclass to create the {@link BaseCarousel} specific to that class.</p>
     *
     * @return the child carousel fragment
     */
    protected abstract BaseCarousel getCarouselFragment();
}
