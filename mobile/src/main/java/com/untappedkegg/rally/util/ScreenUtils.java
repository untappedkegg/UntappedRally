package com.untappedkegg.rally.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.untappedkegg.rally.AppState;

/**
 * Created by UntappedKegg on 1/11/15.
 */
public final class ScreenUtils {
    private static Context ctx = AppState.getApplication();

    public enum Size {
        UNDEFINED,
        SMALL,  // 426dp x 320dp
        NORMAL, // 470dp x 320dp
        LARGE,  // 640dp x 480dp
        XLARGE  // 960dp x 720dp

    }

    public enum Density {
        UNDEFINED,
        LDPI,
        MDPI,
        HDPI,
        XHDPI,
        XXHDPI,
        XXXHDPI,
        TVDPI

    }

    public static final int getDeviceResolution() {
        int density = ctx.getResources().getDisplayMetrics().densityDpi;
        switch (density) {
            case DisplayMetrics.DENSITY_MEDIUM:
                return Density.MDPI.ordinal();
            case DisplayMetrics.DENSITY_HIGH:
                return Density.HDPI.ordinal();
            case DisplayMetrics.DENSITY_LOW:
                return Density.LDPI.ordinal();
            case DisplayMetrics.DENSITY_XHIGH:
                return Density.XHDPI.ordinal();
            case DisplayMetrics.DENSITY_TV:
                return Density.TVDPI.ordinal();
            case DisplayMetrics.DENSITY_XXHIGH:
                return Density.XXHDPI.ordinal();
            case DisplayMetrics.DENSITY_XXXHIGH:
                return Density.XXXHDPI.ordinal();
            default:
                return Density.UNDEFINED.ordinal();
        }
    }

    public static final int getScreenSize( Context ctx) {

        int screenLayout = ctx.getResources().getConfiguration().screenLayout;
        screenLayout &= Configuration.SCREENLAYOUT_SIZE_MASK;

        switch (screenLayout) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return Size.SMALL.ordinal();
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return Size.NORMAL.ordinal();
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                return Size.LARGE.ordinal();
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                return Size.XLARGE.ordinal();
            case Configuration.SCREENLAYOUT_SIZE_UNDEFINED:
            default:
                return Size.UNDEFINED.ordinal();
        }


    }

    public static final boolean isLargeTablet(Context ctx) {
        final Resources res = ctx.getResources();
        return (res.getConfiguration().screenLayout &  Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE &&
                res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

    }

    public static final boolean isDisplayLandscape() {
        return ctx.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static final int getScreenWidth(Context mActivityContext) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity) mActivityContext).getWindowManager().getDefaultDisplay().getMetrics( displaymetrics);
        return (int) (displaymetrics.widthPixels / displaymetrics.xdpi);
    }

    public static final int getScreenHeight(Context mActivityContext) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity) mActivityContext).getWindowManager().getDefaultDisplay().getMetrics( displaymetrics);
        return displaymetrics.heightPixels;
    }

    public static final DisplayMetrics getScreenMetrics(Context mActivityContext) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity) mActivityContext).getWindowManager().getDefaultDisplay().getMetrics( displaymetrics);
        return displaymetrics;
    }

}
