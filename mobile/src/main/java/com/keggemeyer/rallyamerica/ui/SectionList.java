package com.keggemeyer.rallyamerica.ui;

import android.widget.ListAdapter;
import android.widget.ListView;

import com.keggemeyer.rallyamerica.AppState;
import com.keggemeyer.rallyamerica.R;
import com.keggemeyer.rallyamerica.ui.loaders.SectionCursorListAdapter;
import com.keggemeyer.rallyamerica.ui.view.SectionListAdapter;
import com.keggemeyer.rallyamerica.ui.view.SectionListView;

/**
 * <p>Base implementation of {@link BaseList} that has sections in the list.</p>
 *
 * @author Alex Gittemeier
 */
public abstract class SectionList extends BaseList {
    /* ----- VARIABLES ----- */
    /**
     * shadows the SimpleCursorAdapter {@link BaseList#adapter}
     */
    protected SectionListAdapter adapter;

	/* ----- LIFECYCLE METHODS ----- */
    //This is required to be restart friendly
    //	@Override
    //    public void onDestroyView() {
    //        super.onDestroyView();
    //        setListAdapter(null);
    //    }

	/* ----- INHERITED METHODS ----- */
    // BaseList

    /**
     * <p>Uses the SimpleCursorAdapter returned by {@link #getCursorAdapter()} to create the SectionCursorListAdapter.</p>
     */
    @Override
    protected ListAdapter getAdapterForList(ListView listView) {
        adapter = new SectionCursorListAdapter(getActivity().getLayoutInflater(), getCursorAdapter(), getHeaderLayoutId(), (SectionListView) listView, getSectionField());
        return adapter;
    }

    /**
     * <p>Default returns {@code R.layout.generic_section_list}, subclass can override to change this value.</p>
     */
    @Override
    protected int getListLayout() {
        return R.layout.generic_section_list;
    }

    /**
     * <p>Default returns {@code R.id.generic_section_list_progress}, subclass can override to change this value.</p>
     */
    @Override
    protected int getProgressBarId() {
        return R.id.generic_section_list_progress;
    }

    /**
     * <p>Handles requeries, default calls {@link SectionCursorListAdapter#handleRequery()}.  Subclass can override to change this behavior, but should include
     * either the same call or a call to the super.</p>
     */
    @Override
    protected void handleRequery() {
        adapter.handleRequery();
    }

	/* ----- CUSTOM METHODS ----- */

    /**
     * <p>Default returns the result from {@link #getSectionHeaderId()} or {@code R.layout.empty_view} if {@link #getSectionField()} returns null or empty string.</p>
     *
     * @return the layout id for the section header
     */
    private int getHeaderLayoutId() {
        if (AppState.isNullOrEmpty(getSectionField())) {
            return R.layout.empty_view;
        }
        return getSectionHeaderId();
    }

    /**
     * <p>Default returns {@code R.layout.generic_section_list_header}, subclass can override to change this value.</p>
     *
     * @return the layout id for the section header
     */
    protected int getSectionHeaderId() {
        return R.layout.generic_section_list_header;
    }

    /**
     * <p>Hooks into the subclass to determine the cursor column name in which the adapter should look for the header. If null, no header will be visible.</p>
     *
     * @return the column name for the header usually given in the format {@code DbName.COLUMN_NAME}
     */
    protected abstract String getSectionField();
}
