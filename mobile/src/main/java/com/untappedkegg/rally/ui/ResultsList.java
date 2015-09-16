package com.untappedkegg.rally.ui;

import android.text.TextUtils;
import android.widget.TextView;

/**
 * <p>Base implementation of {@link BaseList} that is designed to display the results of a search.</p>
 *
 * @author russellja
 */
public abstract class ResultsList extends BaseList {
    /* ----- VARIABLES ----- */
    /**
     * Argument associated with the {@code SearchManager.QUERY}.
     */
    protected String query;

	/* ----- INHERITED METHODS ----- */
    //	BaseList

    /**
     * <p>Default implementation returns {@code R.layout.results_list}. The subclass can override this method to change this value.</p>
     */
    @Override
    protected int getListLayout() {
        //		return R.layout.results_list;
        return 0;
    }

    /**
     * <p>Default implementation returns {@code R.layout.results_progress}. The subclass can override this method to change this value.</p>
     */
    @Override
    protected int getProgressBarId() {
        //		return R.id.results_progress;
        return 0;
    }

    /**
     * Handles the completion of this list's Loader, default sets the title text. The subclass should override this method to handle this event.
     */
    @Override
    protected void finishedLoading() {
        setTitleText(getQueryFields());
    }

    /**
     * <p>Default returns {@code R.string.results_list_empty_text}. Subclass can override this method to change this value.</p>
     */
    @Override
    protected String getCustomEmptyText() {
        //		return getActivity().getResources().getString(R.string.results_list_empty_text);
        return null;
    }

	/* ----- CUSTOM METHODS ----- */

    /**
     * <p>Constructs the results title text and adds it the text view whose id is returned by {@link #getTitleId()}.  </p>
     *
     * @param fields the list of search terms to be added to the results title text
     */
    protected void setTitleText(String... fields) {
        StringBuilder title = new StringBuilder("Results");

        for (String field : fields) {
            if (!TextUtils.isEmpty(field)) {
                if (!title.toString().equals("Results")) {
                    title.append(", ");
                } else {
                    title.append(" for ");
                }
                title.append("\"").append(field).append("\"");
            }
        }

        try {
            ((TextView) getActivity().findViewById(getTitleId())).setText(title.toString());
        } catch (NullPointerException e) {
            //don't worry about it
        }
    }

    /**
     * <p>Default returns the {@code query} in an array of one.</p>
     *
     * @return array of the search terms
     */
    protected String[] getQueryFields() {
        return new String[]{query};
    }

    /**
     * <p>Default returns {@code R.id.results_text}, subclass can override to change this value.</p>
     *
     * @return the id of the results title text view
     */
    protected int getTitleId() {
        //		return R.id.results_text;
        return 0;
    }
}

