package com.keggemeyer.rallyamerica.ui.loaders;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * <p>Object used to populate a details page from a cursor.</p>
 *
 * @author russellja
 */
public class DetailsAdapter extends SimpleCursorAdapter {
    /* ----- VARIABLES ----- */
    private final Context context;
    private Cursor cursor;
    private final String[] from;
    private final int[] to;
    private final ViewGroup container;
    private final int layout;
    private ViewBinder binder;
    private int emptyLayout;
    private int emptyViewId;
    private String emptyText;
    private View.OnClickListener emptyListener;

    /* ----- CONSTRUCTORS ----- */
    public DetailsAdapter(Context context, Cursor cursor, String[] from, int[] to, int container, int layout) {
        super(context, layout, cursor, from, to, 0);
        this.context = context;
        this.cursor = cursor;
        this.from = from;
        this.to = to;
        this.container = (ViewGroup) ((Activity) context).findViewById(container);
        this.layout = layout;
        this.emptyLayout = -1;
        this.emptyText = "";
        this.binder = null;
        adapt();
    }

	/* ----- CUSTOM METHODS ----- */

    /**
     * <p>Swaps the cursors and calls {@link #notifyDataSetChanged()} to update the views.</p>
     *
     * @param cursor the new cursor
     * @return the old cursor
     */
    public Cursor swapCursor(Cursor cursor) {
        Cursor returnCursor = this.cursor;
        this.cursor = cursor;
        notifyDataSetChanged();
        return returnCursor;
    }

    public void setViewBinder(ViewBinder binder) {
        this.binder = binder;
    }

    public void setEmptyLayout(int emptyLayout, int emptyView) {
        this.emptyLayout = emptyLayout;
        this.emptyViewId = emptyView;
    }

    public void setEmptyText(String emptyText) {
        this.emptyText = emptyText;
        if (getCount() == 0) {
            notifyDataSetChanged();
        }
    }

    public void setEmptyViewOnClickListener(View.OnClickListener onClickListener) {
        this.emptyListener = onClickListener;
    }

    public int getCount() {
        if (cursor == null) {
            return 0;
        }
        return cursor.getCount();
    }

    /**
     * <p>Updates the views.  Removes all views in the container and calls {@link #adapt()} to create new views.</p>
     */
    public void notifyDataSetChanged() {
        container.removeAllViews();
        adapt();
    }

    /**
     * <p>Inflates the {@code layout} and populates the layout view from the cursor.  If the cursor is empty, then it inflates the {@code emptyLayout}.</p>
     */
    private void adapt() {
        if (getCount() > 0) {
            int length = from.length <= to.length ? from.length : to.length;
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(layout, container, false);

            for (int idx = 0; idx < length; idx++) {
                if (cursor.moveToFirst()) {
                    View v = view.findViewById(to[idx]);
                    int columnIndex = cursor.getColumnIndex(from[idx]);

                    if (binder == null || !binder.setViewValue(v, cursor, columnIndex)) {
                        ((TextView) v).setText(cursor.getString(columnIndex));
                    }
                }
            }

            container.addView(view);
        } else {
            if (emptyLayout > 0) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(emptyLayout, container, false);

                if (emptyViewId > 0) {
                    View emptyView = view.findViewById(emptyViewId);
                    if (binder == null || !binder.setViewValue(emptyView, cursor, 0)) {
                        ((TextView) emptyView).setText(emptyText);
                    }
                }

                if (emptyListener != null) {
                    view.setOnClickListener(emptyListener);
                }

                container.addView(view);
            }
        }
    }
}
