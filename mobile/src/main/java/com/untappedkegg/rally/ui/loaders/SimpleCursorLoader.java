package com.untappedkegg.rally.ui.loaders;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

public abstract class SimpleCursorLoader extends AsyncTaskLoader<Cursor> {
    /* ----- VARIABLES ----- */
    private Cursor loaderCursor;

    /* ----- CONSTRUCTORS ----- */
    public SimpleCursorLoader(Context context) {
        super(context);
    }

	/* ----- INHERITED METHODS ----- */

    /**
     * Runs on the UI thread.
     */
    @Override
    public void deliverResult(Cursor cursor) {
        if (isReset()) {
            //An async query came in while the loader is stopped
            if (cursor != null) {
                cursor.close();
            }
            return;
        }
        Cursor oldCursor = loaderCursor;
        loaderCursor = cursor;

        if (isStarted()) {
            super.deliverResult(cursor);
        }

        if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    /**
     * Starts an asynchronous load of the contacts list data. When the result is ready the callbacks
     * will be called on the UI thread. If a previous load has been completed and is still valid
     * the result may be passed to the callbacks immediately.
     * <p/>
     * Must be called from the UI thread
     */
    @Override
    protected void onStartLoading() {
        if (loaderCursor != null) {
            deliverResult(loaderCursor);
        }

        if (takeContentChanged() || loaderCursor == null) {
            forceLoad();
        }
    }

    /**
     * Must be called from the UI thread.
     */
    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    public void onReset() {
        super.onReset();

        onStopLoading();
        if (loaderCursor != null && !loaderCursor.isClosed()) {
            loaderCursor.close();
        }
        loaderCursor = null;
    }
}
