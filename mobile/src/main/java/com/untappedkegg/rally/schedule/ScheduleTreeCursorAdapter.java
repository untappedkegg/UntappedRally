package com.untappedkegg.rally.schedule;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.util.DateManager;

import java.util.Locale;

/**
 * Created by UntappedKegg on 8/10/2014.
 */
public class ScheduleTreeCursorAdapter extends SimpleCursorTreeAdapter {

    private final DisplayImageOptions scheduleOptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_launcher_large) // resource or drawable
            .showImageForEmptyUri(R.drawable.ic_launcher_large) // resource or drawable
            .showImageOnFail(R.drawable.ic_launcher_large) // resource or drawable
            .cacheInMemory(true).cacheOnDisk(true).imageScaleType(ImageScaleType.EXACTLY_STRETCHED).bitmapConfig(Bitmap.Config.RGB_565).displayer(new FadeInBitmapDisplayer(750, true, true, false)).build();


    public ScheduleTreeCursorAdapter(Context context, Cursor cursor, int groupLayout, String[] groupFrom, int[] groupTo, int childLayout, String[] childFrom, int[] childTo) {
        super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);
    }

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        return DbSchedule.getChildren(groupCursor.getString(groupCursor.getColumnIndex(DbSchedule.SCHED_YEAR_ACTUAL)));
    }

    @Override
    protected void bindChildView(View v, Context context, Cursor c, boolean isLastChild) {
        super.bindChildView(v, context, c, isLastChild);

        ScheduleViewHolder holder = (ScheduleViewHolder) v.getTag();

        if (holder == null) {
            holder = new ScheduleViewHolder();
            holder.icon = (ImageView) v.findViewById(R.id.sched_icon);
            holder.title = (TextView) v.findViewById(R.id.sched_title);
            holder.id = (TextView) v.findViewById(R.id.sched_title);

            holder.date = (TextView) v.findViewById(R.id.sched_date);
            holder.date = (TextView) v.findViewById(R.id.sched_date);
            holder.startDate = (TextView) v.findViewById(R.id.sched_start_date);
            holder.endDate = (TextView) v.findViewById(R.id.sched_end_date);
            holder.eventWebsite = (TextView) v.findViewById(R.id.sched_event_website);
            holder.website = (TextView) v.findViewById(R.id.sched_website);
            holder.status = (TextView) v.findViewById(R.id.sched_status);
            holder.location = (TextView) v.findViewById(R.id.sched_location);

            holder.locColumn = c.getColumnIndexOrThrow(DbSchedule.SCHED_LOC);
            holder.iconColumn = c.getColumnIndex(DbSchedule.SCHED_SHORT_CODE);
            holder.titleColumn = c.getColumnIndexOrThrow(DbSchedule.SCHED_TITLE);
            holder.idColumn = c.getColumnIndexOrThrow(DbSchedule.SCHED_ID);
            holder.fromToColumn = c.getColumnIndexOrThrow(DbSchedule.SCHED_FROM_TO);
            holder.eventWebsiteColumn = c.getColumnIndexOrThrow(DbSchedule.SCHED_EVT_SITE);
            holder.websiteColumn = c.getColumnIndexOrThrow(DbSchedule.SCHED_SITE);
            holder.startColumn = c.getColumnIndex(DbSchedule.SCHED_START_DATE);
            holder.endColumn = c.getColumnIndex(DbSchedule.SCHED_END_DATE);

            v.setTag(holder);
        }

        // Icon
        String uri = c.getString(holder.iconColumn).toLowerCase(Locale.US);
        if (!AppState.isNullOrEmpty(uri)) {
//            holder.icon.setMaxWidth(125);
//            holder.icon.setMaxHeight(125);
            holder.icon.setAdjustViewBounds(true);

            if (uri.contains("100aw")) {
                uri = "ra" + uri;
            }

            ImageLoader.getInstance().displayImage(AppState.EGG_DRAWABLE + uri + "_large", holder.icon, scheduleOptions);
        }


        // ID
        holder.id.setText(c.getString(holder.idColumn));
        // Title
        holder.title.setText(c.getString(holder.titleColumn));
        holder.title.setVisibility(View.VISIBLE);
        // Start
        holder.startDate.setText(c.getString(holder.startColumn));
        // End
        holder.endDate.setText(c.getString(holder.endColumn));
        // Location
        holder.location.setText(c.getString(holder.locColumn));

        //Menu
        holder.eventWebsite.setText(c.getString(holder.eventWebsiteColumn));
        holder.website.setText(c.getString(holder.websiteColumn));


        // Status
        short status = DateManager.todayIsBetween(c.getString(holder.startColumn), c.getString(holder.endColumn));
        if (status == 0) {
            holder.status.setText(R.string.upcoming);
            holder.status.setTextColor(AppState.getApplication().getResources().getColor(R.color.title_bar_background));
        } else if (status == 1) {
            //currently running
            holder.status.setText(R.string.live);
            holder.status.setTextColor(AppState.getApplication().getResources().getColor(R.color.light_green));
        } else if (status == 2) {
            //completed
            holder.status.setText(R.string.complete);
            holder.status.setTextColor(AppState.getApplication().getResources().getColor(R.color.DimGray));
        } else if (status == -1) {
            holder.status.setText(R.string.cancelled);
            holder.status.setTextColor(AppState.getApplication().getResources().getColor(R.color.red));
        }
    }

    static class ScheduleViewHolder {
        ImageView icon;
        TextView title, id, date, status, eventWebsite, website, startDate, endDate, location;
        int websiteColumn, eventWebsiteColumn, startColumn, endColumn, iconColumn, titleColumn, idColumn, fromToColumn, locColumn;
    }
}
