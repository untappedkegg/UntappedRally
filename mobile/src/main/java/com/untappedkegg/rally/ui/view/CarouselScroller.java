package com.untappedkegg.rally.ui.view;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.ui.BaseCarousel;

/**
 * <p>{@link Scroller} implementation used to customize the scroll speed for the {@link BaseCarousel}.</p>
 *
 * @author russellja
 */
public class CarouselScroller extends Scroller {
    /* ----- CONSTANTS ----- */
    private final int duration = (int) (AppState.CAROUSEL_SCROLL_DURATION_SECS * 1000);

    /* ----- CONSTRUCTORS ----- */
    public CarouselScroller(Context context) {
        super(context);
    }

    public CarouselScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    /* ----- INHERITED METHODS ----- */
    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        // Ignore received duration, use fixed one instead
        super.startScroll(startX, startY, dx, dy, this.duration);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        // Ignore received duration, use fixed one instead
        super.startScroll(startX, startY, dx, dy, this.duration);
    }
}
