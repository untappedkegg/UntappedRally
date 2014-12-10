package com.keggemeyer.rallyamerica.ui;

import android.app.Activity;
import android.app.SearchManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.keggemeyer.rallyamerica.AppState;
import com.keggemeyer.rallyamerica.R;

/**
 * Base implementation of {@link BaseFragment} that has next and previous buttons to update the data in a child fragment.
 *
 * @author russellja
 */
public abstract class BaseSelector extends BaseFragment implements View.OnClickListener {
    /* ----- CONSTANTS ----- */
    protected final String LOG_TAG = BaseSelector.class.getSimpleName() + "(" + getClass().getSimpleName() + ")@" + Integer.toHexString(hashCode());

     /* ----- VARIABLES ----- */
    /**
     * The child fragment used to display the data.
     */
    protected Fragment dataFragment;
    /**
     * Argument associated with the {@code adn.GoMizzou.ARGS}.
     */
    protected String args;
    /**
     * Argument associated with the {@code SearchManager.QUERY}.
     */
    protected String query;

     /* ----- CONSTRUCTORS ----- */

    /**
     * Required constructor.
     */
    public BaseSelector() {
    }

     /* ----- LIFECYCLE METHODS ----- */

    /**
     * <p>Creates the fragment layout based on what is returned from {@link #getLayout()}.  Subclass should override that method to change the value returned,
     * or override this method with a call to get the view returned from the super to get a view from the layout.</p>
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayout(), container, false);
        getViews(view);
        return view;
    }

    /**
     * <p>Attaches the {@code dataFragment} loads the selector title and buttons.</p>
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Activity activity = getActivity();
        activity.findViewById(getPreviousButtonId()).setOnClickListener(this);
        activity.findViewById(getNextButtonId()).setOnClickListener(this);
        activity.findViewById(getTitleId()).setOnClickListener(this);
        attachFragment();
        loadSelector();
    }

     /* ----- INHERITED METHODS ----- */
    // BaseFragment

    /**
     * <p>Updates the arguments in the {@code dataFragment} if it extends {@link BaseList} or {@link BaseDetails}., subclass should override to handle
     * updating the class arguments for other types of fragments.  Note that it also calls {@link #loadSelector()} to update the title and buttons.</p>
     */
    @Override
    protected void updateArgs(String args, String query) {
        if (dataFragment != null) {
            if (dataFragment instanceof BaseList) {
                ((BaseList) dataFragment).updateArgs(args, query);
            } else if (dataFragment instanceof BaseFragment) {
                ((BaseFragment) dataFragment).updateArgs(args, query);
            } else if (dataFragment instanceof BaseMap) {
                ((BaseMap) dataFragment).updateArgs(args, query);
            }
        }
        loadSelector();
    }

    //	View.OnClickListener

    /**
     * <p>Calls {@link #onNextButtonClick()}, {@link #onPreviousButtonClick()}, and {@link #onTitleClick()} if the view id matches the ones returned from
     * {@link #getNextButtonId()}, {@link #getPreviousButtonId()}, or {@link #getTitleId()}.  Subclass should override to handle the click of other buttons.</p>
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == getNextButtonId()) {
            onNextButtonClick();
        } else if (v.getId() == getPreviousButtonId()) {
            onPreviousButtonClick();
        } else if (v.getId() == getTitleId()) {
            onTitleClick();
        }
        this.loadSelector();
    }

     /* ----- CUSTOM METHODS ----- */

    /**
     * <p>Default implementation returns {@code R.layout.generic_selector}. The subclass can override this method to change this value.
     * If this value is changed, {@link #getNextButtonId()}, {@link #getPreviousButtonId()}, {@link #getTitleId()}, and {@link #getContainerId()} will also need to be changed.</p>
     *
     * @return the layout used for this page
     */
    protected int getLayout() {
        return R.layout.generic_selector;
    }

    /**
     * <p>Attaches the {@code dataFragment} returned by {@link #getDataFragment()} to the container returned by {@link #getContainerId()} and adds it to the backstack.</p>
     */
    protected void attachFragment() {
        FragmentManager man = getChildFragmentManager();
        dataFragment = getDataFragment();

        if (!AppState.isNullOrEmpty(args) || !AppState.isNullOrEmpty(query)) {
            Bundle bundle = new Bundle();
            if (!AppState.isNullOrEmpty(args)) bundle.putString(AppState.KEY_ARGS, args);
            if (!AppState.isNullOrEmpty(query)) bundle.putString(SearchManager.QUERY, query);
            dataFragment.setArguments(bundle);
        }

        int count = man.getBackStackEntryCount();
        if (count > 0) {
            man.popBackStack(man.getBackStackEntryAt(0).getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        //		NavSelector.NavUri navUri = NavSelector.NavUri.parse(dataFragment.getClass().getName(), args, query);
        //		Log.d(LOG_TAG, String.format("Attaching %s for child fragment with tag %s.", dataFragment.getClass().getSimpleName(), navUri.toString()));
        FragmentTransaction transaction = man.beginTransaction();
        transaction.replace(getContainerId(), dataFragment, dataFragment.getClass().getName());
        //		transaction.addToBackStack(dataFragment.getClass().getName());
        transaction.commit();
    }

    /**
     * <p>Calls the {@link BaseList#loadList()}, {@link BaseDetails#loadData()}, or {@link BaseMap#loadMapPlots()} in the {@code dataFragment} if it extends
     * {@link BaseList}, {@link BaseDetails}, or {@link BaseMap}.  Subclass should override to handle other types of child fragments.
     * Note that {@link BaseSelector#loadSelector()} is called when finishing.</p>
     */
    public void loadContent() {
        if (BaseList.class.isAssignableFrom(dataFragment.getClass())) {
            ((BaseList) dataFragment).loadList();
        } else if (BaseDetails.class.isAssignableFrom(dataFragment.getClass())) {
            ((BaseDetails) dataFragment).loadData();
        } else if (BaseMap.class.isAssignableFrom(dataFragment.getClass())) {
            ((BaseMap) dataFragment).loadMapPlots();
        }
        loadSelector();
    }

    /**
     * <p>Sets the title text for the selector with the text returned from {@link #getSelectorTitleText()} and calls {@link #disableButtons()}.</p>
     */
    public void loadSelector() {
        ((TextView) getActivity().findViewById(getTitleId())).setText(getSelectorTitleText());
        disableButtons();
    }

    /**
     * <p>Default returns {@code R.id.generic_selector_title_next}, subclass can override to change this value.</p>
     *
     * @return the layout id of the next button
     */
    protected int getNextButtonId() {
        return R.id.generic_selector_title_next;
    }

    /**
     * <p>Default returns {@code R.id.generic_selector_title_prev}, subclass can override to change this value.</p>
     *
     * @return the layout id of the previous button
     */
    protected int getPreviousButtonId() {
        return R.id.generic_selector_title_prev;
    }

    /**
     * <p>Default returns {@code R.id.generic_selector_title_text}, subclass can override to change this value.</p>
     *
     * @return the layout id of the selector title
     */
    protected int getTitleId() {
        return R.id.generic_selector_title_text;
    }

    /**
     * <p>Default returns {@code R.id.generic_selector_container}, subclass can override to change this value.</p>
     *
     * @return the layout id of the container in which the {@code dataFragment} is placed
     */
    protected int getContainerId() {
        return R.id.generic_selector_container;
    }

    /**
     * <p>Default does nothing, subclass can override to define the behavior that should be completed when the selector title is pressed.</p>
     */
    protected void onTitleClick() {
        // do nothing by default
    }

    protected void getViews(View container) {
        // do nothing by default
    }

    /**
     * <p>Hooks to subclass to create a new fragment specific to that selector.</p>
     *
     * @return the {@code dataFragment} that will be placed into the selector container.
     */
    protected abstract Fragment getDataFragment();

    /**
     * <p>Hooks to sublcass to define the behavior that should be completed when the next button is pressed.</p>
     */
    protected abstract void onNextButtonClick();

    /**
     * <p>Hooks to sublcass to define the behavior that should be completed when the previous button is pressed.</p>
     */
    protected abstract void onPreviousButtonClick();

    /**
     * <p>Hooks to sublcass to customize the text that is placed into the selector title field.</p>
     *
     * @return the text that is placed into the selector title field
     */
    protected abstract String getSelectorTitleText();

    /**
     * <p>Hooks to sublcass to define the behavior that should be used to disable the next and previous buttons.</p>
     */
    protected abstract void disableButtons();
}
