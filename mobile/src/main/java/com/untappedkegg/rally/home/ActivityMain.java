package com.untappedkegg.rally.home;


import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.data.BaseDbAccessor;
import com.untappedkegg.rally.event.EventActivity;
import com.untappedkegg.rally.event.EventDetails;
import com.untappedkegg.rally.interfaces.NavDrawerItemSelected;
import com.untappedkegg.rally.interfaces.Refreshable;
import com.untappedkegg.rally.interfaces.ScheduleItemClickReceiver;
import com.untappedkegg.rally.news.NewsFragment;
import com.untappedkegg.rally.preference.SettingsFragment;
import com.untappedkegg.rally.schedule.ScheduleStub;
import com.untappedkegg.rally.social.TwitterFragment;
import com.untappedkegg.rally.util.CommonIntents;

/**
 * This example illustrates a common usage of the DrawerLayout widget
 * in the Android support library.
 * <p/>
 * <p>When a navigation (left) drawer is present, the host activity should detect presses of
 * the action bar's Up affordance as a signal to open and close the navigation drawer. The
 * ActionBarDrawerToggle facilitates this behavior.
 * Items within the drawer should fall into one of two categories:</p>
 * <p/>
 * <ul>
 * <li><strong>View switches</strong>. A view switch follows the same basic policies as
 * list or tab navigation in that a view switch does not create navigation history.
 * This pattern should only be used at the root activity of a task, leaving some form
 * of Up navigation active for activities further down the navigation hierarchy.</li>
 * <li><strong>Selective Up</strong>. The drawer allows the user to choose an alternate
 * parent for Up navigation. This allows a user to jump across an app's navigation
 * hierarchy at will. The application should treat this as it treats Up navigation from
 * a different task, replacing the current task stack using TaskStackBuilder or similar.
 * This is the only form of navigation drawer that should be used outside of the root
 * activity of a task.</li>
 * </ul>
 * <p/>
 * <p>Right side drawers should be used for actions, not navigation. This follows the pattern
 * established by the Action Bar that navigation should be to the left and actions to the right.
 * An action should be an operation performed on the current contents of the window,
 * for example enabling or disabling a data overlay on top of the current content.</p>
 */
public class ActivityMain extends AppCompatActivity implements ScheduleItemClickReceiver, NavDrawerItemSelected {
    private DrawerLayout mDrawerLayout;
    private static short curPosition = 0;

    private static String[] mActionBarDrawer;

    /**
     * Fragment managing the behaviors, interactions and presentation of the
     * navigation drawer.
     */
    private NavDrawerFragment mNavDrawerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseDbAccessor.open();
        mActionBarDrawer = getResources().getStringArray(R.array.action_bar_modules);
        setContentView(R.layout.master);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mNavDrawerFragment = (NavDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.left_drawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavDrawerFragment.setUp(R.id.left_drawer, mDrawerLayout, mToolbar);


        if (savedInstanceState == null) {
            Bundle bundle = new Bundle();
            bundle.putShort(AppState.KEY_POSITION, (short)0);
            Fragment home = new HomeFragment();
            home.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, home).commit();
        } else {
            curPosition = savedInstanceState.getShort("pos");
        }

    }

    /**
     * This method is called after {@link #onStart} when the activity is
     * being re-initialized from a previously saved state, given here in
     * <var>savedInstanceState</var>.  Most implementations will simply use {@link #onCreate}
     * to restore their state, but it is sometimes convenient to do it here
     * after all of the initialization has been done or to allow subclasses to
     * decide whether to use your default implementation.  The default
     * implementation of this method performs a restore of any view state that
     * had previously been frozen by {@link #onSaveInstanceState}.
     * <p/>
     * <p>This method is called between {@link #onStart} and
     * {@link #onPostCreate}.
     *
     * @param savedInstanceState the data most recently supplied in {@link #onSaveInstanceState}.
     * @see #onCreate
     * @see #onPostCreate
     * @see #onResume
     * @see #onSaveInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            curPosition = savedInstanceState.getShort("pos");
            getSupportActionBar().setTitle(mActionBarDrawer[curPosition]);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putShort("pos", curPosition);
        super.onSaveInstanceState(outState);
    }

    /**
     * <p>Closes the database.</p>
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        BaseDbAccessor.close();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            restoreActionBar();
        }
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (fragment instanceof Refreshable) {

            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (!mDrawerLayout.isDrawerOpen(Gravity.START)) {
                mDrawerLayout.openDrawer(Gravity.START);
            } else {
                mDrawerLayout.closeDrawers();
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        final boolean drawerOpen = mDrawerLayout.isDrawerOpen(Gravity.START);
        try {
            menu.findItem(R.id.menu_refresh).setVisible(!drawerOpen);
            return true;
        } catch (Exception e) {
            return super.onPrepareOptionsMenu(menu);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        // Handle action buttons
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
                if (fragment.isVisible()) {
                    if (fragment instanceof Refreshable) {
                        ((Refreshable) fragment).refreshData();
                    }
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
            mDrawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    void restoreActionBar() {
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mActionBarDrawer[curPosition]);
        }
    }

    /**
     * Called when an item in the navigation drawer is selected.
     *
     * @param position The selected position on the Left Menu
     */
    @Override
    public void onNavDrawerItemSelected(final int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        Bundle icicle = new Bundle();
        icicle.putShort(AppState.KEY_POSITION, (short)position);
        switch (position) {
            case 0:
                fragment = new HomeFragment();
                break;
            case 1:
                fragment = new ScheduleStub();
                break;
            case 2:
                fragment = new NewsFragment();
                break;
            case 3:
                fragment = new StandingsFragment();
                break;
            case 4:
//                fragment = new YouTubeFragment();
                fragment = new TwitterFragment();
                break;
            case 5: // Spectating
                CommonIntents.openUrl(this, "http://www.rally-america.com/safety");
                return;
            case 6: // Worker Info
                CommonIntents.openUrl(this, "http://www.rally-america.com/volunteer");
                return;
//            case 7:
//                CommonIntents.sendFeedback(this);
//                return;
            case 7: // Settings
                fragment = new SettingsFragment();
                break;
            case 8: // About
                fragment = new AboutFragment();
                break;
            case 10: // Exit
                this.finish();
                return;
            default:

                break;
        }

        if (fragment != null) {
            if (curPosition != position) {
                fragment.setArguments(icicle);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content_frame, fragment)
                        .addToBackStack(((Object) fragment).getClass().getSimpleName())
                        .commit();
                curPosition = (short)position;
            }
        }

    }



    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(AppState.isNullOrEmpty(title.toString()) ? getResources().getString(R.string.app_name) : title);
        super.setTitle(title);
    }

    // CALLBACKS
    // ScheduleFragment.Callbacks
    @Override
    public void showEventDetail( String eventName, int id) {
        Intent intent = new Intent(AppState.getApplication(), EventActivity.class);
        intent.putExtra(AppState.KEY_URI, EventDetails.class.getName());
        intent.putExtra(SearchManager.QUERY, eventName);
        intent.putExtra(AppState.KEY_ID, id);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        startActivity(intent);

    }

    public static void setCurPosition(short position) {
        NavDrawerFragment.setListViewPosition(position);
        curPosition = position;
    }

}
