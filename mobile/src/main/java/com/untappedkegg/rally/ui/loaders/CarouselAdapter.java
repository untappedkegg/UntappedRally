package com.untappedkegg.rally.ui.loaders;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.untappedkegg.rally.R;

/**
 * <p>{@link PagerAdapter} implementation that uses views (rather than fragments) populated from a cursor.</p>
 *
 * @author russellja
 */
public class CarouselAdapter extends PagerAdapter {
    /* ----- VARIABLES ----- */
    private final Context context;
    private Cursor cursor;
    private final String[] from;
    private final int[] to;
    private final int layout;
    private int emptyLayout;
    private String emptyText;
    private ViewBinder binder;
    private View.OnClickListener onClickListener;

    /* ----- CONSTRUCTORS ----- */
    public CarouselAdapter(Context context, Cursor cursor, final String[] from, final int[] to, final int layout, View.OnClickListener onClickListener) {
        this.context = context;
        this.cursor = cursor;
        this.from = from;
        this.to = to;
        this.layout = layout;
        this.emptyLayout = R.layout.generic_carousel_empty_page;
        this.emptyText = "";
        this.binder = null;
        this.onClickListener = onClickListener;
    }

	/* ----- INHERITED METHODS ----- */

    /**
     * <p>Instantiates the new view and populates it using the cursor. If the cursor is empty, then it inflates the {@code emptyLayout} and sets th
     *
     * @param container the ViewPager to which the new view will be added
     * @param position  the view position to be instantiated
     * @return the new view added to the ViewPager
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(layout, container, false);

        int count = from.length <= to.length ? from.length : to.length;

        if (cursor.moveToFirst()) {
            cursor.moveToPosition(position);
            for (int idx = 0; idx < count; idx++) {
                View v = view.findViewById(to[idx]);
                int columnIndex = cursor.getColumnIndex(from[idx]);

                if (binder == null || !binder.setViewValue(v, cursor, columnIndex)) {
                    ((TextView) v).setText(cursor.getString(columnIndex));
                }
            }
        } else {
            View v = inflater.inflate(emptyLayout, container, false);
            ((TextView) v.findViewById(R.id.carousel_page_empty)).setText(emptyText);
            container.addView(v);
            return v;
        }

        if (onClickListener != null) {
            view.setOnClickListener(onClickListener);
        }

        container.addView(view);
        return view;
    }

    /**
     * <p>Removes the view from the container.</p>
     *
     * @param container the ViewPager from which the view will be removed
     * @param position  the view position to be removed
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        if (cursor == null) {
            return 0;
        }

        if (cursor.getCount() == 0) {
            return 1;
        }

        return cursor.getCount();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    /* ----- CUSTOM METHODS ----- */
    public void setViewBinder(ViewBinder binder) {
        this.binder = binder;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setEmptyLayout(int emptyLayout) {
        this.emptyLayout = emptyLayout;
    }

    public void setEmptyText(String emptyText) {
        this.emptyText = emptyText;
        if (getCount() == 1) {
            notifyDataSetChanged();
        }
    }

    /**
     * <p>Swaps out the cursor, returning the old one and calls {@link #notifyDataSetChanged()} to update the adapter.</p>
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
}
