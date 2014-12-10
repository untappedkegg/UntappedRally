package com.keggemeyer.rallyamerica.ui.view;

import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Adapter wrapper to enable section dividers on a {@link ListView}. should be
 * used only in conjunction with {@link SectionListView}.
 */
public abstract class SectionListAdapter extends BaseAdapter implements OnItemClickListener {
    /* CONSTANTS */
    private final ListAdapter linkedAdapter;
    private final Map<Integer, String> sectionPositions = new LinkedHashMap<Integer, String>();
    private final Map<Integer, Integer> itemPositions = new LinkedHashMap<Integer, Integer>();
    private final Map<View, String> currentViewSections = new HashMap<View, String>();
    private final int headerLayoutId;
    private final LayoutInflater inflater;

    private final DataSetObserver dataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            updateSessionCache();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            updateSessionCache();
        }
    };

    /* VARIABLES */
    private int viewTypeCount;
    private final SectionListView listView;
    private View transparentSectionView;
    private OnItemClickListener linkedListener;

	/* CONSTRUCTORS */

    /**
     * @param inflater       an instance of a layoutInflater
     * @param linkedAdapter  the adapter to connect to this adapter
     *                       (adapter-ception)
     * @param headerLayoutId Layout id for the view for section headers
     * @param listView       The listView that is bound to this adapter
     */
    public SectionListAdapter(final LayoutInflater inflater, final ListAdapter linkedAdapter, final int headerLayoutId, final SectionListView listView) {
        this.linkedAdapter = linkedAdapter;
        this.inflater = inflater;
        linkedAdapter.registerDataSetObserver(dataSetObserver);
        updateSessionCache();
        this.headerLayoutId = headerLayoutId;
        this.listView = listView;
    }

    /* INHERITED METHODS */
    // BaseAdapter //
    @Override
    public synchronized int getCount() {
        return sectionPositions.size() + itemPositions.size();
    }

    @Override
    public synchronized Object getItem(final int position) {
        if (isSection(position)) return sectionPositions.get(position);

        final int linkedItemPosition = getLinkedPosition(position);
        return linkedAdapter.getItem(linkedItemPosition);
    }

    @Override
    public long getItemId(final int position) {
        if (isSection(position)) return sectionPositions.get(position).hashCode();
        try {
            return linkedAdapter.getItemId(getLinkedPosition(position));
        } catch (NullPointerException e) {
            return 0;
        }
    }

    @Override
    public int getItemViewType(final int position) {
        if (isSection(position)) {
            return viewTypeCount - 1;
        }
        return linkedAdapter.getItemViewType(getLinkedPosition(position));
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        if (isSection(position)) {
            return getSectionView(convertView, sectionPositions.get(position));
        }
        return linkedAdapter.getView(getLinkedPosition(position), convertView, parent);
    }

    @Override
    public int getViewTypeCount() {
        return viewTypeCount;
    }

    @Override
    public boolean hasStableIds() {
        return linkedAdapter.hasStableIds();
    }

    @Override
    public boolean isEmpty() {
        return linkedAdapter.isEmpty();
    }

    @Override
    public boolean isEnabled(final int position) {
        if (isSection(position)) {
            return true;
        }
        return linkedAdapter.isEnabled(getLinkedPosition(position));
    }

    @Override
    public boolean areAllItemsEnabled() {
        return linkedAdapter.areAllItemsEnabled();
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        if (isSection(position)) {
            sectionClicked(getSectionName(position));
        } else if (linkedListener != null) {
            linkedListener.onItemClick(parent, view, getLinkedPosition(position), id);
        }
    }

    @Override
    public void registerDataSetObserver(final DataSetObserver observer) {
        linkedAdapter.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(final DataSetObserver observer) {
        linkedAdapter.unregisterDataSetObserver(observer);
    }

	/* CUSTOM METHODS */

    /**
     * @return the view used as the section divider. If one doesn't exist, a new
     * one is returned
     */
    public synchronized View getTransparentSectionView() {
        if (transparentSectionView == null) {
            transparentSectionView = createNewSectionView();
        }
        return transparentSectionView;
    }

    /**
     * Tests whether the specified position is a section divider or a normal
     * row.
     *
     * @param position the position to test
     * @return true if it is a section divider or false if it is a normal row.
     */
    public synchronized boolean isSection(final int position) {
        return sectionPositions.containsKey(position);
    }

    private synchronized void replaceSectionViewsInMaps(final String section, final View theView) {
        if (currentViewSections.containsKey(theView)) {
            currentViewSections.remove(theView);
        }
        currentViewSections.put(theView, section);
    }

    /**
     * Returns the text behind the specified section divider
     *
     * @param position the position of the section divider
     * @return the text or null if the position is <em>not</em> a section
     * divider
     */
    public synchronized String getSectionName(final int position) {
        if (isSection(position)) return sectionPositions.get(position);
        return null;
    }

    /**
     * Registers a onItemClickListener with this adapter's ListView
     *
     * @param linkedListener the onItemClickListener.
     */
    public void setOnItemClickListener(final OnItemClickListener linkedListener) {
        this.linkedListener = linkedListener;
    }

    /**
     * Converts a raw position (ie. the actual position in the ListView) to the
     * position when section dividers are removed. Intended for use only inside
     * other ListAdapters when they need this info to, for example, to correctly
     * style a certain row.
     *
     * @param rawPosition the actual position
     * @return the normal position, or -1 if newPosition refers to a section
     * divider
     */
    public int transformPosition(int rawPosition) {
        if (itemPositions.isEmpty()) {
            updateSessionCache();
        }
        if (itemPositions.containsKey(rawPosition)) {
            return itemPositions.get(rawPosition);
        }
        return -1;
    }

    /**
     * Refreshes elements that change during/after a scroll operation. Intended
     * for use only by SectionListView.
     *
     * @param firstVisibleItem position of first completely visible element.
     */
    // Package scoped
    void handleScrollEvent(final int firstVisibleItem) {
        final String section = getSectionName(firstVisibleItem);
        boolean alreadySetFirstSectionInvisible = false;
        for (final Entry<View, String> itemView : currentViewSections.entrySet()) {
            if (itemView.getValue().equals(section) && !alreadySetFirstSectionInvisible) {
                itemView.getKey().setVisibility(View.INVISIBLE);
                alreadySetFirstSectionInvisible = true;
            } else {
                itemView.getKey().setVisibility(View.VISIBLE);
            }
        }
        for (final Entry<Integer, String> entry : sectionPositions.entrySet()) {
            if (entry.getKey() > firstVisibleItem) {
                break;
            }
            setSectionText(entry.getValue(), getTransparentSectionView());
        }
    }

    /**
     * Refreshes all internal structures to let the ListView reflect new data.
     */
    public void handleRequery() {
        updateSessionCache();
        listView.handleRequery();
    }

    private synchronized void updateSessionCache() {
        int currentPosition = 0;
        sectionPositions.clear();
        itemPositions.clear();
        viewTypeCount = linkedAdapter.getViewTypeCount() + 1;
        String currentSection = null;
        final int count = linkedAdapter.getCount();
        for (int i = 0; i < count; i++) {
            final String itemSection = getSectionNameFromAdapter(linkedAdapter.getItem(i));
            if (!isTheSame(currentSection, itemSection)) {
                sectionPositions.put(currentPosition, itemSection);
                currentSection = itemSection;
                currentPosition++;
            }
            itemPositions.put(currentPosition, i);
            currentPosition++;
        }
    }

    private View createNewSectionView() {
        return inflater.inflate(headerLayoutId, null);
    }

    private View getSectionView(final View convertView, final String section) {
        View theView = convertView;
        if (theView == null) {
            theView = createNewSectionView();
        }
        setSectionText(section, theView);
        replaceSectionViewsInMaps(section, theView);
        return theView;
    }

    private static boolean isTheSame(final String previousSection, final String newSection) {
        if (previousSection == null) return newSection == null;
        return previousSection.equals(newSection);
    }

    private Integer getLinkedPosition(final int position) {
        return itemPositions.get(position);
    }

    // Optional Subclass Overrides //

    /**
     * Sets {@code sectionView} to reflect {@code section}. This implementation
     * assumes that sectionView is an instance of TextView and sets its text
     * field to {@code section}. If this is not the case, the subclass should
     * override this method.
     *
     * @param section     The text to add to {@code sectionView}
     * @param sectionView The view to modify.
     */
    protected void setSectionText(String section, View sectionView) {
        ((TextView) sectionView).setText(section);
    }

    /**
     * Handles a click on a section divider. This implementation does not do
     * anything to handle this. The subclass will override this method to handle
     * these events.
     *
     * @param section the text associated to the clicked section.
     */
    protected void sectionClicked(final String section) {
        // Defaults to no-op, see javadoc
    }

    // Required Subclass Overrides //

    /**
     * Hooks to subclass to get the section String that {@code adapterItem} is
     * associated with.
     *
     * @param adapterItem The object returned from the linked adapter's
     *                    {@code getItem(int)} method
     * @return the section of this item.
     */
    protected abstract String getSectionNameFromAdapter(Object adapterItem);

}
