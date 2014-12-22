package com.untappedkegg.rally.ui.view;

import android.content.Context;
import android.widget.LinearLayout;

/**
 * Dots view that can attach to a ViewPager via xml. Note that the onPageChangeListener of the
 * attached is overwritten. If you want to register another onPageChangeListener, use the method
 * {@link setAttachedOnPageChangeListener()} instead.
 * <p/>
 * Use in xml:
 * <p/>
 * {@code <?xml version="1.0" encoding="utf-8"?>}
 * <p/>
 * {@code <Layout xmlns:android="http://schemas.android.com/apk/res/android"}
 * {@code xmlns:custom="http://schema.android.com/apk/res-auto" >}
 * <p/>
 * {@code <android.view.ViewPager} {@code android:id="@+id/view_pager" />}
 * <p/>
 * {@code <adn.GoMizzou.ui.view.ViewPagerDots} {@code android:id="@id/view_pager_dots"}
 * {@code custom:attachedPager="@id/view_pager" />}
 * <p/>
 * {@code </Layout>} <!-- <?xml version="1.0" encoding="utf-8"?> <Layout
 * xmlns:android="http://schemas.android.com/apk/res/android"
 * xmlns:custom="http://schema.android.com/apk/res-auto" > <android.view.ViewPager
 * android:id="@+id/view_pager" /> <adn.GoMizzou.ui.view.ViewPagerDots
 * android:id="@id/view_pager_dots" custom:attachedPager="@id/view_pager" /> </Layout> -->
 *
 * @author alexg
 */
public final class ViewPagerDots extends LinearLayout {
/*	private Context context;

	// Settings
	private int dotCount = 1;
	private int activeDot = 0;
	*/

    /**
     * NOTE: units in px NOT dp
     *//*
    private int internalPadding = 0;
	private int inactiveDrawable = R.drawable.dots_inactive;
	private int activeDrawable = R.drawable.dots_active;
	private ViewPager attachedPager;
*/
    public ViewPagerDots(Context context) {
        super(context);
        //		this.context = context;
        //		init();
    }
/*
	public ViewPagerDots(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;

		// Apply attributes from xml
		TypedArray attArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ViewPagerDots, 0, 0);
		try {
			dotCount = attArray.getInt(R.styleable.ViewPagerDots_dotCount, dotCount);
			activeDot = attArray.getInt(R.styleable.ViewPagerDots_activeDot, activeDot);
			internalPadding = attArray.getDimensionPixelSize(R.styleable.ViewPagerDots_internalPadding, internalPadding);
			inactiveDrawable = attArray.getResourceId(R.styleable.ViewPagerDots_inactiveDrawable, inactiveDrawable);
			activeDrawable = attArray.getResourceId(R.styleable.ViewPagerDots_activeDrawable, activeDrawable);
			if (attArray.hasValue(R.styleable.ViewPagerDots_attachedPager) && !isInEditMode()) {
				final int pagerId = attArray.getResourceId(R.styleable.ViewPagerDots_attachedPager, 0);
				post(new Runnable() {
					@Override
					public void run() {
						attachViewPager((ViewPager) getRootView().findViewById(pagerId));
					}
				});
			}
		}
		finally {
			attArray.recycle();
		}

		init();
	}

	private void init() {
		setOrientation(LinearLayout.HORIZONTAL);
		setGravity(Gravity.CENTER_HORIZONTAL);

		refreshDots();
	}

	public void attachViewPager(ViewPager pager) {
		attachedPager = pager;
		pager.setOnPageChangeListener(new ScrollHandler(this, pager));
	}

	public void setInactiveDrawable(int resourceId) {
		inactiveDrawable = resourceId;
		refreshDots();
	}

	public void setActiveDrawable(int resourceId) {
		activeDrawable = resourceId;
		refreshDots();
	}

	public void setDotCount(int count) {
		dotCount = count;
		refreshDots();
	}

	public void setActiveDot(int index) {
		activeDot = index;
		refreshDots();
	}

	public void setInternalPadding(int padding) {
		internalPadding = padding;
		refreshDots();
	}

	public ViewPager getAttachedViewPager() {
		return attachedPager;
	}

	public int getInactiveDrawable() {
		return inactiveDrawable;
	}

	public int getAcitveDrawable() {
		return activeDrawable;
	}

	public int getDotCount() {
		return dotCount;
	}

	public int getActiveDot() {
		return activeDot;
	}

	public int getInternalPadding() {
		return internalPadding;
	}

	public void onDataSetChanged() {
		if (attachedPager != null) attachViewPager(attachedPager);
	}

	private void refreshDots() {
		removeAllViews();

		while (getChildCount() < dotCount) {
			ImageView newView = new ImageView(context);
			newView.setPadding(internalPadding, internalPadding, internalPadding, internalPadding);
			addView(newView);
		}

		for (int i = 0; i < getChildCount(); i++) {
			ImageView view = (ImageView) getChildAt(i);
			view.setImageResource(inactiveDrawable);
		}

		ImageView view = (ImageView) getChildAt(activeDot);
		if (view != null) view.setImageResource(activeDrawable);
	}

	private static class ScrollHandler extends SimpleOnPageChangeListener {
		private final ViewPagerDots attachedDots;
		private final ViewPager pager;

		public ScrollHandler(ViewPagerDots attachedDots, ViewPager pager) {
			this.attachedDots = attachedDots;
			this.pager = pager;

			onPageSelected(pager.getCurrentItem());
		}

		@Override
		public void onPageSelected(int position) {
			if (pager.getAdapter() != null) {
				attachedDots.setDotCount(pager.getAdapter().getCount());
				attachedDots.setActiveDot(position);
			}
		}
	}*/
}
