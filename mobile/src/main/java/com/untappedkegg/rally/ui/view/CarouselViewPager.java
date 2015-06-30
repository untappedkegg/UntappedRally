package com.untappedkegg.rally.ui.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;

import com.untappedkegg.rally.BuildConfig;

import java.lang.reflect.Field;

public class CarouselViewPager extends ViewPager {
    /* ----- VARIABLES ----- */
    private Callbacks callback;

    /* ----- CONSTRUCTORS ----- */
    public CarouselViewPager(Context context) {
        super(context);
        setOffscreenPageLimit(2);
        setScroller();
    }

    public CarouselViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOffscreenPageLimit(2);
        setScroller();
    }

    /* ----- INHERITED METHODS ----- */
    // ViewPager
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        callback.onCarouselInterceptTouchEvent();
        return super.onInterceptTouchEvent(event);
    }

    /* ----- CUSTOM METHODS ----- */
    public void setCallbacks(Callbacks callback) {
        try {
            this.callback = callback;
        } catch (ClassCastException e) {
            throw new ClassCastException(String.format("%s must implement %s.", callback.toString(), Callbacks.class.getName()));
        }
    }

    private void setScroller() {
        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            CarouselScroller scroller = new CarouselScroller(this.getContext());
            mScroller.set(this, scroller);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    public void setPageMarginDP(int pageMarginDP) {
        setPageMargin((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pageMarginDP, getResources().getDisplayMetrics()));
    }

    /* ----- NESTED INTERFACES ----- */
    public interface Callbacks {
        boolean onCarouselInterceptTouchEvent();
    }
}
