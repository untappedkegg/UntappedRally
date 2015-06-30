package com.untappedkegg.rally.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Specialized {@link ListView} designed for use only with a
 * {@link SectionListView}.
 */
public class SectionListView extends ListView implements OnScrollListener {
    /* VARIABLES */
    private ListAdapter adapter;
    private View transparentView;

    /* CONSTRUCTORS */
    public SectionListView(final Context context) {
        super(context);
        commonInitialisation();
    }

    public SectionListView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        commonInitialisation();
    }

    public SectionListView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        commonInitialisation();
    }

	/* INHERITED METHODS */
    // ListView //

    /**
     * Sets the data behind this ListView.
     *
     * @param adapter the data provider.
     * @throws IllegalArgumentException if {@code adapter} is not an instance of
     *                                  SectionListAdapter.
     * @throws IllegalStateException    if ListView is not inside a frameLayout.
     */
    @Override
    public void setAdapter(final ListAdapter adapter) {
        if (!(adapter instanceof SectionListAdapter)) {
            throw new IllegalArgumentException("The adapter needs to be of type " + SectionListAdapter.class + " and is " + adapter.getClass());
        }

        super.setAdapter(adapter);
        final ViewParent parent = getParent();

        if (!(parent instanceof FrameLayout)) {
            throw new IllegalStateException("Section List must have FrameLayout as parent!");
        }
        if (transparentView != null) {
            ((FrameLayout) parent).removeView(transparentView);
        }

        transparentView = ((SectionListAdapter) adapter).getTransparentSectionView();
        final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        ((FrameLayout) parent).addView(transparentView, lp);
        this.adapter = adapter;
        handleRequery();
    }

    // OnScrollListener //
    @Override
    public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
        // TODO: test this
        // final SectionListAdapter adapter = (SectionListAdapter) getAdapter();
        if (adapter != null) {
            ((SectionListAdapter) adapter).handleScrollEvent(firstVisibleItem);
            // adapter.handleScrollEvent(firstVisibleItem);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // Do nothing
    }

    /* CUSTOM METHODS */
    protected final void commonInitialisation() {
        setOnScrollListener(this);
        setVerticalFadingEdgeEnabled(false);
        setFadingEdgeLength(0);
    }

    /**
     * Updates ListView after a requery. Intended for use by
     * {@link SectionListAdapter} only. Normal code should not call this method,
     * because SectionListAdapter automatically calls this method on a call to
     * its requery method.
     */
    // Package scoped
    void handleRequery() {
        if (adapter.isEmpty()) {
            transparentView.setVisibility(View.INVISIBLE);
        } else {
            transparentView.setVisibility(View.VISIBLE);
        }
    }
}
