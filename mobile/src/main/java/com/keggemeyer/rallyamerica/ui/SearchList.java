package com.keggemeyer.rallyamerica.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.keggemeyer.rallyamerica.AppState;
import com.keggemeyer.rallyamerica.R;

/**
 * <p>Base implementation for a {@link BaseList} is designed to be used for searches.</p>
 *
 * @author russellja
 */
public abstract class SearchList extends BaseList implements View.OnClickListener, View.OnKeyListener {
    /* ----- VARIABLES ----- */
    /**
     * Behavior flag, used to determine if there are one or two search fields.
     */
    protected boolean dualSearch;
    /**
     * Search query, associated with {@code SearchManager.QUERY}.
     */
    protected String query;

	/* ----- LIFECYCLE METHODS ----- */

    /**
     * <p>Sets {@code dualSearch} to {@code false} and fetchOnCreate is dependant on whether the query is empty.</p>
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        dualSearch = false;
        fetchOnCreate = !AppState.isNullOrEmpty(query);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setSearchHint();
        ((ImageButton) getActivity().findViewById(getSearchButtonId())).setOnClickListener(this);
        if (dualSearch) {
            ((EditText) getActivity().findViewById(R.id.search_dual_text1)).setOnKeyListener(this);
            ((EditText) getActivity().findViewById(R.id.search_dual_text2)).setOnKeyListener(this);
        } else {
            ((EditText) getActivity().findViewById(R.id.search_text)).setOnKeyListener(this);
        }
    }

	/* ----- INHERITED METHODS ----- */
    // BaseList

    /**
     * <p>Default returns {@code R.layout.search_list_dual} or {@code R.layout.search_list} depending on {@code dualSearch}.
     * Subclass should override to change this value.</p>
     */
    @Override
    protected int getListLayout() {
        //		if (dualSearch) return R.layout.search_list_dual;
        //		return R.layout.search_list;
        return 0;
    }

    /**
     * <p>Default returns {@code R.id.search_dual_progress} or {@code R.id.search_progress} depending on {@code dualSearch}.
     * Subclass should override to change this value.</p>
     */
    @Override
    protected int getProgressBarId() {
        if (dualSearch) return R.id.search_dual_progress;
        return R.id.search_progress;
    }

    @Override
    public void loadList() {
        super.loadList();
        ((LinearLayout) getActivity().findViewById(getSearchListId())).setVisibility(View.VISIBLE);
    }

    // View.OnClickListener

    /**
     * <p>Validates the search text and performs the search or shows the validation error if the clicked view id is the same as the id returned from {@link #getSearchButtonId()}.
     * Subclass should override to change this behavior or to handle other buttons.</p>
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == getSearchButtonId()) {
            setProgressBarVisibility(View.VISIBLE);
            if (validate()) {
                performSearch();
            } else {
                setProgressBarVisibility(View.GONE);
                handleValidationError();
            }
        }
    }

    //	View.OnKeyListener

    /**
     * <p>Validates the search text and performs the search or shows the validation error if the pressed key is {@code KEYCODE_DPAD_CENTER} or {@code KEYCODE_ENTER}.
     * Subclass should override to change this behavior or to handle other keys.</p>
     */
    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                    setProgressBarVisibility(View.VISIBLE);
                    if (validate()) {
                        performSearch();
                    } else {
                        setProgressBarVisibility(View.GONE);
                        handleValidationError();
                    }
                    return true;
            }
        }
        return false;
    }

	/* ----- CUSTOM METHODS ----- */

    /**
     * <p>Default checks to see if the search text is empty and assigns the text to {@code query}.  Subclass should override to change this behavior.</p>
     *
     * @return whether the search text passes validation
     */
    protected boolean validate() {
        if (dualSearch) {
            ContentValues values = new ContentValues();
            values.put("search1", ((EditText) getActivity().findViewById(R.id.search_dual_text1)).getText().toString());
            values.put("search2", ((EditText) getActivity().findViewById(R.id.search_dual_text2)).getText().toString());
            //			String temp = NavSelector.NavUri.combineArgs(values);

            //			if(!AppState.isNullOrEmpty(temp)) {
            //				query = temp;
            //				return true;
            //			}
            //		} else {
            //			String temp = ((EditText) getActivity().findViewById(R.id.search_text)).getText().toString();
            //			if(!AppState.isNullOrEmpty(temp)) {
            //				query = temp;
            //				return true;
            //			}
        }
        return false;
    }

    /**
     * <p>Default sets the search hint to the text returned by {@link #getSearchHint(int)}. Subclass can override to change this behavior.</p>
     */
    protected void setSearchHint() {
        if (dualSearch) {
            ((EditText) getActivity().findViewById(R.id.search_dual_text1)).setHint(getSearchHint(R.id.search_dual_text1));
            ((EditText) getActivity().findViewById(R.id.search_dual_text2)).setHint(getSearchHint(R.id.search_dual_text2));
        } else {
            ((EditText) getActivity().findViewById(R.id.search_text)).setHint(getSearchHint(R.id.search_text));
        }
    }

    /**
     * <p>Default sets the search text to the text returned by {@link #getSearchText(int)}. Subclass can override to change this behavior.</p>
     */
    protected void setSearchText() {
        if (dualSearch) {
            ((EditText) getActivity().findViewById(R.id.search_dual_text1)).setText(getSearchText(R.id.search_dual_text1));
            ((EditText) getActivity().findViewById(R.id.search_dual_text2)).setText(getSearchText(R.id.search_dual_text2));
        } else {
            ((EditText) getActivity().findViewById(R.id.search_text)).setText(getSearchText(R.id.search_text));
        }
    }

    /**
     * <p>Default returns {@code R.id.search_dual_list} if {@code dualSearch} is {@code true}, otherwise {@code R.id.search_list}.
     *
     * @return id of the linear layout containing the list and empty views
     */
    protected int getSearchListId() {
        //		if (dualSearch) {
        //			return R.id.search_dual_list;
        //		}
        //		return R.id.search_list;
        return 0;
    }

    /**
     * <p>Default returns {@code R.id.search_dual_button} if {@code dualSearch} is {@code true}, otherwise {@code R.id.search_button}.
     *
     * @return id of the search button
     */
    protected int getSearchButtonId() {
        if (dualSearch) {
            return R.id.search_dual_button;
        }
        return R.id.search_button;
    }

    /**
     * <p>Default calls {@link #fetchData()} and {@link #loadList()}, subclass can override to change this behavior.</p>
     */
    protected void performSearch() {
        fetchData();
        loadList();
    }

    /**
     * <p>Default does nothing, subclass should override to show handle a validation error.</p>
     */
    protected void handleValidationError() {
        // do nothing by default
    }

    /**
     * <p>Hooks into the subclass to determine the hint for the search text box(es).</p>
     *
     * @param viewId the id of the edit text in which the returned hint will go
     * @return the search hint text
     */
    protected abstract String getSearchHint(int viewId);

    /**
     * <p>Hooks into the subclass to determine the text for the search text box(es).</p>
     *
     * @param viewId the id of the edit text in which the returned text will go
     * @return the search text
     */
    protected abstract String getSearchText(int viewId);
}
