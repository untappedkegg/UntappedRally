package com.keggemeyer.rallyamerica.ui.loaders;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.widget.ListAdapter;

import com.keggemeyer.rallyamerica.ui.view.SectionListAdapter;
import com.keggemeyer.rallyamerica.ui.view.SectionListView;

/**
 * Implementation of {@link SectionListAdapter} for use with Cursors.
 */
public class SectionCursorListAdapter extends SectionListAdapter {
    /* VARIABLES */
    private final String sectionField;

	/* CONSTRUCTORS */

    /**
     * @param inflater       an instance of a layoutInflater
     * @param linkedAdapter  the adapter to connect to this adapter
     *                       (adapter-ception)
     * @param sectionField   The database field name to get the section's name
     * @param headerLayoutId Layout id for the view for section headers
     * @param listView       The listView that is bound to this adapter
     */
    public SectionCursorListAdapter(LayoutInflater inflater, ListAdapter linkedAdapter, int headerLayoutId, SectionListView listView, String sectionField) {
        super(inflater, linkedAdapter, headerLayoutId, listView);
        this.sectionField = sectionField;
    }

    @Override
    protected String getSectionNameFromAdapter(Object adapterItem) {
        Cursor cursor = (Cursor) adapterItem;
        try {
            return cursor.getString(cursor.getColumnIndexOrThrow(sectionField));
        } catch (NullPointerException e) {
            //catch the Exception and return a null String
            return null;
        }
    }
}
