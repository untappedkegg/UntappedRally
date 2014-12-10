package com.keggemeyer.rallyamerica.news;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.keggemeyer.rallyamerica.AppState;
import com.keggemeyer.rallyamerica.R;
import com.keggemeyer.rallyamerica.util.DateManager;

import java.text.ParseException;

public final class NewsViewBinder implements ViewBinder {
    /* ----- CONSTANTS ----- */
    //	private static final String LOG_TAG = NewsViewBinder.class.getSimpleName();

    /* ----- VARIABLES ----- */
    private boolean isHomeFragment;
    final private Context ctx;

    /* ----- CONSTRUCTORS ----- */
    public NewsViewBinder(Context ctx, boolean isHomeFragment) {
        this.isHomeFragment = isHomeFragment;
        this.ctx = ctx;
    }

    /* ----- INHERITED METHODS ----- */
    public boolean setViewValue(final View view, final Cursor cursor, final int columnIndex) {
        String uri = cursor.getString(columnIndex);
        switch (view.getId()) {
            // Event Feeds
            case R.id.list_icon:

                final ImageView imageView = (ImageView) view;
                if (uri.equals(AppState.SOURCE_IRALLY)) {
                    imageView.setImageResource(R.drawable.irally_logo);
                    view.setVisibility(View.VISIBLE);
                    return true;
                    //			} else if (uri.equals(AppState.SOURCE_CITROEN)) {
                    //				imageView.setImageResource(R.drawable.citroen_racing_logo);
                    //				view.setVisibility(View.VISIBLE);
                    //				return true;
                    //			} else if (uri.equals(AppState.SOURCE_MINI_COOPER)) {
                    //				imageView.setImageResource(R.drawable.mini_logo);
                    //				view.setVisibility(View.VISIBLE);
                    //				return true;
                    //			} else if (uri.equals(AppState.SOURCE_BEST_OF_RALLY)) {
                    //				imageView.setImageResource(R.drawable.michelin_logo);
                    //				view.setVisibility(View.VISIBLE);
                    //				return true;
                } else if (uri.equals(AppState.SOURCE_RALLY_AMERICA)) {
                    imageView.setImageResource(R.drawable.src_ra);
                    view.setVisibility(View.VISIBLE);
                    return true;
                } else {
                    //				view.setVisibility(View.GONE);
                    return false;
                }

            case R.id.list_date:
                TextView tv = (TextView) view;
                if (!AppState.isNullOrEmpty(uri)) {
                    //				String date;
                    try {
                        tv.setText(DateUtils.getRelativeDateTimeString(ctx, DateManager.DATABASE.parse(uri).getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.YEAR_IN_MILLIS, DateUtils.FORMAT_NO_YEAR));//getRelativeTimeSpanString(DateManager.DATABASE.parse(uri).getTime()));
                        //					tv.setText(DateUtils.getRelativeTimeSpanString(DateManager.DATABASE.parse(uri).getTime()));
                        //					view.setVisibility(View.VISIBLE);
                        return true;
                    } catch (ParseException e) {
                        view.setVisibility(View.GONE);
                    }
                    //				try {
                    //					date = DateManager.formatAsRelativeTime(DateManager.DATABASE.parse(uri));
                    //					tv.setText(date);
                    //					view.setVisibility(View.VISIBLE);
                    //					return true;
                    //				} catch (ParseException e) {
                    //					view.setVisibility(View.GONE);
                    //				}

                } else {
                    view.setVisibility(View.GONE);
                    return false;
                }

            case R.id.list_descr:
                if (AppState.isNullOrEmpty(uri) || isHomeFragment) {
                    view.setVisibility(View.GONE);
                    //				 return true;
                } else {
                    //                 ((TextView) view).setText(Html.fromHtml(uri));
                    //                 return false;
                }
                return false;

            case R.id.read_status:
                final Resources res = ctx.getResources();
                tv = (TextView) view;
                tv.setText(uri);
                if (uri.equals(res.getString(R.string.news_read))) {
                    tv.setTextColor(res.getColor(R.color.Aqua));
                } else {
                    //redundant but necessary for the ViewBinder to not F-it up
                    tv.setTextColor(res.getColor(R.color.light_red));
                }
                return false;

            default:
                return false;
        }

    }

}
