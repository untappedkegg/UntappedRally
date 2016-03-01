package com.untappedkegg.rally.news;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.SimpleCursorTreeAdapter;

/**
 * Created by UntappedKegg on 3/1/2016.
 */
public class NewsTreeCursorAdapter extends SimpleCursorTreeAdapter {
    public NewsTreeCursorAdapter(Context context, Cursor cursor, int groupLayout, String[] groupFrom, int[] groupTo, int childLayout, String[] childFrom, int[] childTo) {
        super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);
    }



    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        Log.e("Foo", "Cursor @ pos:" + groupCursor.getPosition());
        return DbNews.getChildren(groupCursor.getString(groupCursor.getColumnIndexOrThrow(DbNews.SHORTDATE)));
    }

}
