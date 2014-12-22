package com.untappedkegg.rally;

import android.app.SearchManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.untappedkegg.rally.ui.BaseContainer;
import com.untappedkegg.rally.util.DialogManager;

public class HelperActivity extends FragmentActivity {
    /* ----- CONSTANTS ----- */
    private final String LOG_TAG = BaseContainer.class.getSimpleName() + "(" + getClass().getSimpleName() + ")@" + Integer.toHexString(hashCode()).toUpperCase();

    /* ----- VARIABLES ----- */ boolean destroyingViaConfigChange = false;

    //	protected static DataFetcher dataFetcher = DataFetcher.getInstance();

    protected String curUri;
    protected String curUrl;
    protected String curArgs;
    protected String curQuery;
    protected boolean restarting = false;
    protected double latitude;
    protected double longitude;

	/* ----- LIFECYCLE METHODS ----- */

    /**
     * <p>
     * Perform initialization. Note that {@code selectContent()} will be called in the super with ""
     * for both parameters, <em>but not</em> on a configuration change.(usually a screen rotation)
     * </p>
     * <p>
     * {@code restarting} will set when calling the super, and it differentiates a normal create,
     * and a create caused by a configuration change.
     * </p>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        destroyingViaConfigChange = false;

        Log.i(LOG_TAG, "Loading layout.");
        setContentView(getContentLayout());


        Bundle intentExtras = getIntent().getExtras();
        curUrl = firstValueInBundles(AppState.KEY_URL, "", intentExtras);
        curUri = firstValueInBundles(AppState.KEY_URI, "", intentExtras);
        curArgs = firstValueInBundles(AppState.KEY_ARGS, "", intentExtras);
        curQuery = firstValueInBundles(SearchManager.QUERY, "", intentExtras);
        restarting = firstValueInBundles(AppState.KEY_RESTARTING, false, intentExtras, savedInstanceState);

        if (restarting) {
            Log.i(LOG_TAG, String.format("Recieved restart: uri=%s args=%s query=%s", curUri, curArgs, curQuery));
        } else {
            //			selectContent(curUri, curArgs, curQuery, false);
            attachFragment(curUri, false, curUri, curArgs);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Prevent whatever feed from having to reload everytime there is a
        // configuration change
        outState.putBoolean(AppState.KEY_RESTARTING, true);
        super.onSaveInstanceState(outState);
        Log.w(LOG_TAG, "Preparing for container restart");
    }

    /* ----- CUSTOM METHODS ----- */
    public void attachFragment(String fragment, boolean addToBackStack, String tag, String args) {
        attachFragment(fragment, addToBackStack, tag, args, R.id.main_container);
    }

    public void attachFragment(String fragment, boolean addToBackStack, String tag, String args, int replaceId) {
        Fragment frag = null;
        Bundle bundle = new Bundle();
        bundle.putString(AppState.KEY_URL, curUrl);
        bundle.putString(AppState.KEY_ARGS, curArgs);

        try {
            Class<Fragment> fragmentClass = (Class<Fragment>) Class.forName(fragment);
            frag = fragmentClass.newInstance();
            frag.setArguments(bundle);
        } catch (Exception e) {
            DialogManager.raiseUIError(this, e, false);
            //		e.printStackTrace();
        }
        //	Fragment frag = new fragment();


        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(replaceId, frag, tag);
        if (addToBackStack) transaction.addToBackStack(tag);
        transaction.commit();
    }

    // Utility functions

    /**
     * Returns the first occurrence of a key in a list of bundles.
     *
     * @param key          The key to search for.
     * @param defaultValue The default value to return if no match is found.
     * @param bundles      List of Bundles to search in. Allowed to contain nulls.
     * @return The value of {@code key} or {@code defaultValue} if {@code key} was not found.
     */
    protected static String firstValueInBundles(String key, String defaultValue, Bundle... bundles) {
        for (Bundle bundle : bundles) {
            if (bundle != null && bundle.containsKey(key)) {
                return bundle.getString(key);
            }
        }
        return defaultValue;
    }

    /**
     * Returns the first occurrence of a key in a list of bundles.
     *
     * @param key          The key to search for.
     * @param defaultValue The default value to return if no match is found.
     * @param bundles      List of Bundles to search in. Allowed to contain nulls.
     * @return The value of {@code key} or {@code defaultValue} if {@code key} was not found.
     */
    protected static boolean firstValueInBundles(String key, boolean defaultValue, Bundle... bundles) {
        for (Bundle bundle : bundles) {
            if (bundle != null && bundle.containsKey(key)) {
                return bundle.getBoolean(key);
            }
        }
        return defaultValue;
    }

    protected int getContentLayout() {
        return R.layout.generic_frame_layout;
    }

}
