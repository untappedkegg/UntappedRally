package com.untappedkegg.rally.news;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.util.DateManager;

import java.text.ParseException;

public final class NewsViewBinder implements ViewBinder {

    /* ----- VARIABLES ----- */
    private final Context ctx;

    /* ----- CONSTRUCTORS ----- */
    public NewsViewBinder(Context ctx) {
        this.ctx = ctx;
    }

    /* ----- INHERITED METHODS ----- */
    public boolean setViewValue(final View view, final Cursor cursor, final int columnIndex) {
        String uri = cursor.getString(columnIndex);
        switch (view.getId()) {
            // Event Feeds
            case R.id.list_icon:

                final ImageView imageView = (ImageView) view;
                imageView.setAdjustViewBounds(true);

                if (uri.equalsIgnoreCase(AppState.SOURCE_RALLY_AMERICA)) {
                    imageView.setImageResource(R.drawable.ra_large);
                    return true;
                }

                if (uri.contains("100aw")) {
                    uri = "ra" + uri;
                }

                ImageLoader.getInstance().displayImage(AppState.EGG_DRAWABLE + uri.toLowerCase() + "_large", imageView);

                return true;

            case R.id.list_date:
                TextView tv = (TextView) view;
                if (!TextUtils.isEmpty(uri)) {

                    try {
                        tv.setText(DateUtils.getRelativeDateTimeString(ctx, DateManager.DATABASE.parse(uri).getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.YEAR_IN_MILLIS, DateUtils.FORMAT_NO_YEAR));
                        return true;
                    } catch (ParseException e) {
                        view.setVisibility(View.GONE);
                    }

                } else {
                    view.setVisibility(View.GONE);
                    return false;
                }

            case R.id.list_descr:
                if (TextUtils.isEmpty(uri)) {
                    view.setVisibility(View.GONE);
                }
                return false;

            case R.id.read_status:
                final Resources res = ctx.getResources();
                tv = (TextView) view;
                tv.setText(uri);
                if (uri.equals(res.getString(R.string.news_read))) {
                    tv.setTextColor(res.getColor(R.color.cyan));
                } else {
                    //redundant but necessary for the ViewBinder to not F-it up
                    tv.setTextColor(res.getColor(R.color.red));
                }
                return false;

            default:
                return false;
        }

    }

}
