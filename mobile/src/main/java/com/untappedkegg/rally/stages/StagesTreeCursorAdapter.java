package com.untappedkegg.rally.stages;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

import com.untappedkegg.rally.R;
import com.untappedkegg.rally.event.DbEvent;

/**
 * Created by UntappedKegg on 8/10/2014.
 */
public class StagesTreeCursorAdapter extends SimpleCursorTreeAdapter {

    public StagesTreeCursorAdapter(Context context, Cursor cursor, int groupLayout, String[] groupFrom, int[] groupTo, int childLayout, String[] childFrom, int[] childTo) {
        super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);
    }

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        return DbEvent.getChildren(groupCursor.getString(groupCursor.getColumnIndex(DbEvent.STAGES_HEADER)), groupCursor.getString(groupCursor.getColumnIndex(DbEvent.STAGES_EVENT)), groupCursor.getString(groupCursor.getColumnIndex(DbEvent.STAGES_YEAR)));
    }

    @Override
    protected void bindChildView(View v, Context context, Cursor c, boolean isLastChild) {
        super.bindChildView(v, context, c, isLastChild);

        StagesViewHolder holder = (StagesViewHolder) v.getTag();

        if (holder == null) {
            holder = new StagesViewHolder();
            holder.name = (TextView) v.findViewById(R.id.stages_name);
            holder.id = (TextView) v.findViewById(R.id.stages_id);

            holder.atcText = (TextView) v.findViewById(R.id.stages_atc);
            holder.distance = (TextView) v.findViewById(R.id.stages_distance);

            holder.nameColumn = c.getColumnIndexOrThrow(DbEvent.STAGES_NAME);
            holder.numColumn = c.getColumnIndexOrThrow(DbEvent.STAGES_NUMBER);
            holder.atcColumn = c.getColumnIndexOrThrow(DbEvent.STAGES_ATC);
            holder.distanceColumn = c.getColumnIndexOrThrow(DbEvent.STAGES_LENGTH);

            v.setTag(holder);
        }


        // ID
        holder.id.setText(c.getString(holder.numColumn));
        // Title
        holder.name.setText(c.getString(holder.nameColumn));
        // ATC
        holder.atcText.setText(c.getString(holder.atcColumn));
        // Distance
        holder.distance.setText(c.getString(holder.distanceColumn));


    }

    static class StagesViewHolder {
        TextView name, id, atcText, distance;
        int nameColumn, numColumn, atcColumn, distanceColumn;
    }
}
