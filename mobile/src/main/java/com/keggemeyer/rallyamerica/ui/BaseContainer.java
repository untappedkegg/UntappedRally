package com.keggemeyer.rallyamerica.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.keggemeyer.rallyamerica.AppState;
import com.keggemeyer.rallyamerica.R;
import com.keggemeyer.rallyamerica.data.DataFetcher;
import com.keggemeyer.rallyamerica.data.NewDataFetcher;
import com.keggemeyer.rallyamerica.data.NewDataFetcher.Fetcher;
import com.keggemeyer.rallyamerica.util.DialogManager;

/**
 * <p>Base implementation for an Activity designed to hold Fragments. Its subclasses will define which fragments get loaded, extra behavior, etc.</p>
 *
 * @author russellja
 */
public abstract class BaseContainer extends FragmentActivity implements DataFetcher.Callbacks, NewDataFetcher.Callbacks {
    /* ----- CONSTANTS ----- */
    protected final String LOG_TAG = BaseContainer.class.getSimpleName() + "(" + ((Object) this).getClass().getSimpleName() + ")@" + Integer.toHexString(((Object) this).hashCode());

	/* ----- VARIABLES ----- */
    /**
     * Behavior flag, used to determine if the activity is being destroyed by a config change (ie screen rotation).
     */
//    boolean destroyingViaConfigChange = false;
    /**
     * Behavior flag, used to differentiate a normal create from a create caused by a config change.
     */
    protected boolean restarting = false;
    protected boolean stopped = false;

    //	protected static LocationFetcher locationFetcher = LocationFetcher.getInstance();
    protected Fetcher fetcher;

    protected String curUri;
    protected String curArgs;
    protected String curQuery;
    protected ProgressDialog progressDialog;

	/* ----- LIFECYCLE METHODS ----- */

    /**
     * {@inheritDoc}
     * <p/>
     * <p>Note that {@code selectContent()} will be called in the super with ""
     * for both parameters, <em>but not</em> on a configuration change (usually a screen rotation).</p>
     * <p/>
     * <p>{@code restarting} will set when calling the super, and it differentiates a normal create,
     * and a create caused by a configuration change.</p>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        destroyingViaConfigChange = false;
        setContentView(getContentLayout());


        Bundle intentExtras = getIntent().getExtras();
        curUri = firstValueInBundles(AppState.KEY_URI, "", intentExtras);
        curArgs = firstValueInBundles(AppState.KEY_ARGS, "", intentExtras);
        curQuery = firstValueInBundles(SearchManager.QUERY, "", intentExtras);
        restarting = firstValueInBundles(AppState.KEY_RESTARTING, false, intentExtras, savedInstanceState);
        stopped = false;

        fetcher = getFetcher();

        setTitle(this, getTitleText());

        if (restarting) {
            Log.d(LOG_TAG, String.format("Recieved restart: uri=%s args=%s query=%s", curUri, curArgs, curQuery));
        } else {
            selectContent(curUri, curArgs, curQuery, false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopped = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        stopped = true;
    }

    /**
     * <p>Stops all running {@code DataFetcher} threads and {@code LocationFetcher} updates.</p>
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fetcher != null) {
            fetcher.interrupt();
        }

        //		locationFetcher.stopUpdates(this);
    }

	/* ----- INHERITED METHODS ----- */
    // Activity

    /**
     * <p>Redirects pressing the back button to close the menus if they're open. Subclass should override to change this behavior.</p>
     */
    @Override
    public void onBackPressed() {

        //			dataFetcher.nonmenu_interrupt();
        if (fetcher != null) {
            fetcher.interrupt();
        }
        super.onBackPressed();

    }

    /**
     * <p>Redirects pressing the menu button to open the left menu. Subclass should override to change this behavior.</p>
     */
    //	@Override
    //	public boolean onKeyUp(int keyCode, KeyEvent event) {
    //        if (keyCode == KeyEvent.KEYCODE_MENU) {
    //
    //            return true;
    //        }
    //        return super.onKeyUp(keyCode, event);
    //    }

    /**
     * <p>Saves {@code restarting} as true in the saved instance state bundle.  Subclass should override this to save other variables.</p>
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Prevent whatever feed from having to reload everytime there is a configuration change
        outState.putBoolean(AppState.KEY_RESTARTING, true);
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG, "Preparing for container restart");
    }

    /**
     * <p>Sets {@code destroyingViaConfigChange} to true.</p>
     */
//    @Override
//    public Object onRetainNonConfigurationInstance() {
//        destroyingViaConfigChange = true;
//        return super.onRetainNonConfigurationInstance();
//    }

    // DataFetcher.Callbacks

    /**
     * <p>Raises error dialog if there was an error in the data fetch ({@code throwable} is not null). Handles the return of the left and right menu data fetches.
     * Subclass should override this to handle other data fetch returns.</p>
     */
    @Override
    public void onDataFetchComplete(Throwable throwable, String parser) {
        if (throwable != null) {
            if (AppState.DEBUG) {
                DialogManager.raiseUIError(this, throwable, false);
            }
        }

        if (progressDialog != null) {
            progressDialog = null;
        }


        //		if(parser.equals(AppState.KEY_NEARBY_RANGE)) {
        //			ContentValues values = new ContentValues();
        //			values.put(parser, true);
        //
        //		}
    }

    // LocationFetcher.Callbacks
    /**
     * <p>Starts the right menu data fetch.</p>
     */
    //	@Override
    //	public void onLocationUpdated(double latitude, double longitude) {
    //
    //	}

    /**
     * <p>Tells the right menu that the location fetch failed.</p>
     */

	/* ----- CUSTOM METHODS ----- */
    /**
     * <p>Performs the navigation for the activity.</p>
     *
     * <p>Calls NavSelector to determine the target.  Calls selectModule if the target is in another package, calls selectContent if the target is in the same package,
     * sends browser intent if target is a link, or displays dialog if target is update.</p>
     *
     * @param uri identifier to be used by NavSelector to determine the target
     * @param args any arguments needed to be sent to the target
     */
    //	protected void navigate(String uri, String args) {
    //		NavSelector.NavUri nav = NavSelector.select(uri, args);
    //
    //		if(nav != null) {
    //			if(nav.module.equals(AppState.KEY_UPDATE)) {
    //				final Context context = this;
    //				DialogManager.raiseTwoButtonDialog(this, "Update", nav.args,
    //					new OnClickListener() {
    //						@Override
    //						public void onClick(DialogInterface dialog, int which) {
    //							CommonIntents.openUrl(context, "https://play.google.com/store/apps/details?id=adn.GoMizzou&hl=en");
    //						}
    //					}, new OnClickListener() {
    //						@Override
    //						public void onClick(DialogInterface dialog, int which) { }
    //					});
    //			} else if(nav.module.equals(AppState.KEY_URL)) {
    //				CommonIntents.openUrl(this, nav.args);
    //			} else if(NavSelector.isSameModule(this.getClass().getName(), nav.module)) {
    //				selectContent(nav.uri, nav.args, nav.query);
    //			} else {
    //				selectModule(nav.module, nav.uri, nav.args, nav.query);
    //			}
    //		}
    //	}

    /**
     * <p>Works just like {@link #navigate(String, String)} except {@code args} is {@code null}.</p>
     *
     * @param uri identifier to be used by NavSelector to determine the target
     * @see #navigate(String, String)
     */
    //	protected void navigate(String uri) {
    //		navigate(uri, null);
    //	}

    /**
     * <p>Calls the activity for another module.</p>
     *
     * @param moduleUri  uniform resource identifier (class defined) for module activity
     * @param contentUri uniform resource identifier (class defined) for content fragment, null or empty for the default
     * @param args       any arguments to be sent to the content fragment
     * @param query      any arguments to be sent to the content fragment as a search query
     */
    public void selectModule(String moduleUri, String contentUri, String args, String query) {
        try {
            if (fetcher != null) {
                fetcher.interrupt();
            }


            Class<?> c = Class.forName(moduleUri);

            Intent intent = new Intent(this, c);

            if (!AppState.isNullOrEmpty(contentUri)) intent.putExtra(AppState.KEY_URI, contentUri);
            if (!AppState.isNullOrEmpty(args)) intent.putExtra(AppState.KEY_ARGS, args);
            if (!AppState.isNullOrEmpty(query)) intent.putExtra(SearchManager.QUERY, query);

            Log.d(LOG_TAG, String.format("Selecting Module: module=%s uri=%s arguments=%s, query=%s", moduleUri, contentUri, args, query));
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            Log.d(LOG_TAG, "", e);
            DialogManager.raiseUIError(this, e, false);
        }
    }

    /**
     * <p>Works just like {@link #selectModule(String, String, String, String)} except {@code contentUri}, {@code args}, and {@code query} are {@code null}.</p>
     *
     * @param moduleUri uniform resource identifier (class defined) for module activity
     * @see #selectModule(String, String, String, String)
     */
    public void selectModule(String moduleUri) {
        selectModule(moduleUri, null, null, null);
    }

    /**
     * <p>Works just like {@link #selectModule(String, String, String, String)} except {@code args}, and {@code query} are {@code null}.</p>
     *
     * @param moduleUri  uniform resource identifier (class defined) for module activity
     * @param contentUri uniform resource identifier (class defined) for content fragment, null or empty for the default
     * @see #selectModule(String, String, String, String)
     */
    public void selectModule(String moduleUri, String contentUri) {
        selectModule(moduleUri, contentUri, null, null);
    }

    /**
     * <p>Works just like {@link #selectModule(String, String, String, String)} except {@code query} is {@code null}.</p>
     *
     * @param moduleUri  uniform resource identifier (class defined) for module activity
     * @param contentUri uniform resource identifier (class defined) for content fragment, null or empty for the default
     * @param args       any arguments to be sent to the content fragment
     * @see #selectModule(String, String, String, String)
     */
    public void selectModule(String moduleUri, String contentUri, String args) {
        selectModule(moduleUri, contentUri, args, null);
    }

    /**
     * <p>Sets the content fragment of this container.</p>
     * <p/>
     * <p>It is very important to set {@code curUri} and {@code curArgs} to values that can recreate the current screen in a case of a
     * configuration change (usually a screen rotation).</p>
     * <p/>
     * </p>The fragment is attached by calling {@link #attachFragment(Fragment, boolean, String, int)}.</p>
     *
     * @param uri            uniform resource identifier (class defined), or "" to denote the default. Does not need to be globally unique.
     * @param arguments      any extra data associated with the uri. If there is no data, pass "".
     * @param query          any extra data meant to be used as a search query.
     * @param addToBackStack whether or not to add this transaction to the back
     *                       button's history.
     */
    public abstract void selectContent(String uri, String args, String query, boolean addToBackStack);

    /**
     * <p>Works just like {@link #selectContent(String, String, String, boolean)} except {@code addToBackStack} is {@code true}.</p>
     *
     * @param uri       uniform resource identifier (class defined), or "" to denote the default. Does not need to be globally unique.
     * @param arguments any extra data associated with the uri. If there is no data, pass "".
     * @param query     any extra data meant to be used as a search query.
     * @see #selectContent(String, String, String, boolean)
     */
    public void selectContent(String uri, String args, String query) {
        selectContent(uri, args, query, true);
    }

    /**
     * <p>Works just like {@code selectContent(String, String, String, boolean)} except {@code query} is {@code null} and {@code addToBackStack}
     * is {@code true}.</p>
     *
     * @param uri  uniform resource identifier (class defined), or "" to denote the default. Does not need to be globally unique.
     * @param args any extra data associated with the uri. If there is no data, pass "".
     * @see #selectContent(String, String, String, boolean)
     */
    public void selectContent(String uri, String args) {
        selectContent(uri, args, null, true);
    }

    /**
     * <p>Works just like {@code selectContent(String, String, String, boolean)} except {@code args} and {@code query} are {@code null} and
     * {@code addToBackStack} is {@code true}.</p>
     *
     * @param uri uniform resource identifier (class defined), or "" to denote the default. Does not need to be globally unique.
     * @see #selectContent(String, String, String, boolean)
     */
    public void selectContent(String uri) {
        selectContent(uri, null, null, true);
    }

    /**
     * <p>Attaches the fragment for {@link #selectContent(String, String, String, boolean)}. Subclasses should not override this method.</p>
     *
     * @param fragment
     * @param addToBackStack
     * @param tag
     * @param replaceId
     */
    protected void attachFragment(Fragment fragment, boolean addToBackStack, String tag, int replaceId) {
        if (!stopped) {
            FragmentManager man = getSupportFragmentManager();
            if (!addToBackStack) {
                int count = man.getBackStackEntryCount();
                if (count > 0) {
                    // emulate back button then move forward.
                    man.popBackStack(man.getBackStackEntryAt(count - 1).getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    addToBackStack = true;
                }
            }

            Log.d(LOG_TAG, String.format("Attaching %s for content fragment with tag %s.", ((Object) fragment).getClass().getSimpleName(), tag));
            FragmentTransaction transaction = man.beginTransaction();
            transaction.replace(replaceId, fragment, tag);
            if (addToBackStack) transaction.addToBackStack(tag);
            transaction.commit();
        }
    }

    /**
     * <p>Default creates the left and right side menus and starts the {@code locationFetcher} updates.  The subclass can override this method
     * to change this behavior.</p>
     */
//    protected void createSideMenus() {
        //		locationFetcher.startUpdates(this);
//    }

    /**
     * <p>Sets the title field in the master layout.</p>
     *
     * @param fragment the fragment calling this method
     * @param title the title text
     * @throws NullPointerException if the fragment is null, the fragment's activity is null, or the
     *             view couldn't be found.
     */
    //	public static void setTitle(Fragment fragment, String title) {
    //		setTitle(fragment.getActivity(), title);
    //	}

    /**
     * <p>Sets the title field in the master layout.</p>
     *
     * @param activity the activity to set the title of
     * @param title    the title text
     * @throws NullPointerException if the activity is null or the view couldn't be found.
     */
    public static void setTitle(Activity activity, String title) {
        if (!AppState.isNullOrEmpty(title)) {
            activity.getActionBar().setTitle(title);
        }
    }

    /**
     * <p>Returns the text to be set in the title field of the master layout.  Default returns null so that no change is made,
     * the subclass can override this value.</p>
     *
     * @return String to be set in the title field of the master layout (default is null).
     */
    protected String getTitleText() {
        return null;
    }

    /**
     * <p>Returns the {@code destroyingViaConfigChange} for the BaseContainer associated with a fragment.</p>
     *
     * @param fragment the fragment used to determine the BaseContainer
     * @return a boolean indicating whether the activity is being destroyed via a config change (usually a screen rotation).
     */
//    public static boolean isDestroyingViaConfigChange(Fragment fragment) {
//        return ((BaseContainer) fragment.getActivity()).destroyingViaConfigChange;
//    }

    /**
     * <p>Returns the {@code destroyingViaConfigChange}.</p>
     *
     * @return a boolean indicating whether the activity is being destroyed via a config change (usually a screen rotation).
     */
//    public boolean isDestroyingViaConfigChange() {
//        return destroyingViaConfigChange;
//    }

    /**
     * <p>Default implementation returns {@code R.layout.master}. The subclass can override this method
     * to change this value.</p>
     *
     * @return the layout used for this container.
     */
    protected int getContentLayout() {
        //		return R.layout.master;
        return R.layout.generic_frame_layout;
    }

    /**
     * <p>Default sets the left and right buttons to the default {@code OnMenuButtonClick}.</p>
     */
    //	protected void setMenuButtonClickListeners() {
    //		OnMenuButtonClick onMenuButtonClick = new OnMenuButtonClick(this);
    //		findViewById(getLeftButtonId()).setOnClickListener(onMenuButtonClick);
    //		findViewById(getRightButtonId()).setOnClickListener(onMenuButtonClick);
    //	}

    /**
     * <p>Default implementation returns {@code R.id.left_menu_button}. The subclass can override this
     * method to change this value.</p>
     *
     * @return the id used for this container.
     */
    //	protected int getLeftButtonId() {
    //		return R.id.left_menu_button;
    //	}

    /**
     * <p>Default implementation returns {@code R.id.right_menu_button}. The subclass can override this
     * method to change this value.</p>
     *
     * @return the id used for this container.
     */
    //	protected int getRightButtonId() {
    //		return R.id.right_menu_button;
    //	}

    /**
     * <p>Returns the first occurrence of a key in a list of bundles.</p>
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
     * <p>Returns the first occurrence of a key in a list of bundles.</p>
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

    /**
     * <p>Default returns null, subclass should override to change this value.</p>
     *
     * @return the {@link NewDataFetcher.Fetcher} implementation used to fetch data
     */
    protected Fetcher getFetcher() {
        return null;
    }

	/* ----- NESTED CLASSES ----- */

    /**
     * <p>The default {@code View.OnClickListener} for the left and right menu buttons.</p>
     *
     * @author russellja
     */
//    public class OnMenuButtonClick implements View.OnClickListener {
//        /* ----- VARIABLES ----- */
//        private Context ctx;
//
//        /* ----- CONSTRUCTORS ----- */
//        public OnMenuButtonClick(Context ctx) {
//            super();
//            this.ctx = ctx;
//        }
//
//        /* ----- INHERITED METHODS ----- */
//        @Override
//        public void onClick(View v) {
//            switch (v.getId()) {
//
//            }
//        }
//
//		/* ----- CUSTOM METHODS ----- */
//
//        /**
//         * <p>If the content fragment has a carousel then it pauses it.</p>
//         */
//        private void pause() {
//            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
//            if (BaseCarousel.class.isAssignableFrom(((Object) fragment).getClass())) {
//                ((BaseCarousel) fragment).pauseCarousel();
//            } else if (CarouselList.class.isAssignableFrom(((Object) fragment).getClass())) {
//                ((CarouselList) fragment).pauseCarousel();
//            }
//        }
//
//        /**
//         * <p>If the content fragment has a carousel then it resumes it.</p>
//         */
//        private void resume() {
//            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
//            if (BaseCarousel.class.isAssignableFrom(((Object) fragment).getClass())) {
//                ((BaseCarousel) fragment).resumeCarousel();
//            } else if (CarouselList.class.isAssignableFrom(((Object) fragment).getClass())) {
//                ((CarouselList) fragment).resumeCarousel();
//            }
//        }
//    }
}
