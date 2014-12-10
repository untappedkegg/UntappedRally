/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.keggemeyer.rallyamerica.home;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.keggemeyer.rallyamerica.AppState;
import com.keggemeyer.rallyamerica.R;
import com.keggemeyer.rallyamerica.data.BaseDbAccessor;
import com.keggemeyer.rallyamerica.event.EventActivity;
import com.keggemeyer.rallyamerica.feedback.Feedback;
import com.keggemeyer.rallyamerica.interfaces.Refreshable;
import com.keggemeyer.rallyamerica.news.NewsFragment;
import com.keggemeyer.rallyamerica.preference.SettingsActivity;
import com.keggemeyer.rallyamerica.schedule.ExpandableScheduleFragment;
import com.keggemeyer.rallyamerica.schedule.ScheduleFragment;
import com.keggemeyer.rallyamerica.social.YouTubeFragment;
import com.keggemeyer.rallyamerica.util.CommonIntents;
import com.keggemeyer.rallyamerica.util.DialogManager;

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
public class ActivityMain2 extends FragmentActivity implements ScheduleFragment.Callbacks, HomeFragment.Callbacks, NextEventFragment.Callbacks, NavDrawerFragment.Callbacks, ExpandableScheduleFragment.Callbacks, SharedPreferences.OnSharedPreferenceChangeListener {
    private DrawerLayout mDrawerLayout;
    private ListView mLeftDrawerList;
    //    private ActionBarDrawerToggle mDrawerToggle;
    private static short curPosition = 0;

    //    private CharSequence mDrawerTitle;
    private static CharSequence mTitle;
    private String[] mActionBarDrawer;

//    private ActionBar actionBar;
    private boolean settingsChanged = false;
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
        setContentView(R.layout.activity_main2);

        //        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
        //            this.setImmersive(true);


        mNavDrawerFragment = (NavDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.left_drawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavDrawerFragment.setUp(R.id.left_drawer, mDrawerLayout);
//        mLeftDrawerList = (ListView) findViewById(R.id.left_drawer);


        if (savedInstanceState == null) {
            Bundle bundle = new Bundle();
            bundle.putInt(AppState.KEY_POSITION, 0);
            Fragment home = new HomeFragment();
            home.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, home).commit();
        } else {
            curPosition = savedInstanceState.getShort("pos");
            //        	this.setTitle(mTitle);

            //        	actionBar.setTitle(mTitle);
        }

    AppState.getSettings().registerOnSharedPreferenceChangeListener(this);

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
            getActionBar().setTitle(mActionBarDrawer[curPosition]);
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
            //            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            //            return true;
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
            if (!mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                //        		mDrawerLayout.closeDrawer(Gravity.RIGHT);
                mDrawerLayout.openDrawer(Gravity.LEFT);
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
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(Gravity.LEFT); //|| mDrawerLayout.isDrawerOpen(Gravity.RIGHT);
        try {
            menu.findItem(R.id.menu_refresh).setVisible(!drawerOpen);
        } catch (Exception e) {

        }

        return super.onPrepareOptionsMenu(menu);
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
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
//        if (settingsChanged) {
//            this.recreate();
//            settingsChanged = false;
//        }
    }

    void restoreActionBar() {
        ActionBar actionBar = getActionBar();

        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setDisplayShowTitleEnabled(true);
//            actionBar.setTitle(mActionBarDrawer[mNavDrawerFragment.mCurrentSelectedPosition]);
            actionBar.setTitle(mActionBarDrawer[curPosition]);
        }
    }

    /**
     * Called when an item in the navigation drawer is selected.
     *
     * @param position
     */
    public void onNavDrawerItemSelected(int position) {
        // update the main content by replacing fragments
//        String uri = null;
        Intent intent = null;
        Fragment fragment = null;
        Bundle icicle = new Bundle();
        icicle.putInt(AppState.KEY_POSITION, position);
        switch (position) {
            case 0:
//                uri = HomeFragment.class.getName();
                fragment = new HomeFragment();
                break;
            case 1:
                if (AppState.getSettings().getBoolean(getString(R.string.settings_use_experimental_sched), false)) {
//                    uri = ExpandableScheduleFragment.class.getName();
                    fragment = new ExpandableScheduleFragment();
                } else {
//                    uri = ScheduleFragment.class.getName();
                    fragment = new ScheduleFragment();
                }
                break;
            case 2:
                fragment = new NewsFragment();
                //			break;
//                uri = NewsFragment.class.getName();
                break;
            case 3:
                fragment = new StandingsFragment();
//                uri = StandingsFragment.class.getName();
                break;
            case 4:
                //			uri = Videos.class.getName();
                fragment = new YouTubeFragment();
//                uri = YouTubeFragment.class.getName();
                break;
            case 5: // Spectating
                CommonIntents.openUrl(this, "http://www.rally-america.com/safety");
                break;
            case 6: // Worker Info
                CommonIntents.openUrl(this, "http://www.rally-america.com/volunteer");
                break;
            case 7:
                intent = new Intent(this, Feedback.class);
//                startActivity(intent);
                break;
            case 8: // Standings
//                fragment = new SettingsFragment();
                intent = new Intent(this, SettingsActivity.class);
//                startActivity(intent);
//                uri = SettingsFragment.class.getName();
                break;
            case 9: // About
                fragment = new AboutFragment();
                break;
            case 10: // Exit
                this.finish();
                return;
            default:

                break;
        }


//        try {
//            Class<Fragment> fragmentClass = (Class<Fragment>) Class.forName(uri);
//            fragment = fragmentClass.newInstance();
//        } catch (Exception e) {
//            if (position != 7 && position != 10) {
//                DialogManager.raiseUIError(this, "Error", "This feature has not been implemented yet", false);
//            }
//        }


        if (fragment != null) {
//            if (fragment instanceof FeedbackFragment) {
//                fragment.setHasOptionsMenu(false);
            if (curPosition != position) {
                fragment.setArguments(icicle);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content_frame, fragment)
                        .addToBackStack(((Object) fragment).getClass().getSimpleName())
                        .commit();
                curPosition = (short) position;
            }
        } else if (intent != null) {
                startActivity(intent);
        } else if (position != 5 && position != 6) {
//            Log.w(this.getClass().getSimpleName(), intent.toString() + " " + fragment.toString());
           DialogManager.raiseUIError(this, "Error", "This feature has not been implemented yet", false);
        }



    }

    @Override
    public void setTitle(CharSequence title) {
        if (AppState.isNullOrEmpty(title.toString())) {
            mTitle = getResources().getString(R.string.app_name);
        } else {
            mTitle = title;
        }
        getActionBar().setTitle(mTitle);
    }

    // CALLBACKS
    // ScheduleFragment.Callbacks
    @Override
    public void showEventDetail(String fragment, String args, String eventName) {
        CommonIntents.startNewContainer(this, fragment, args, EventActivity.class, eventName);

    }

    public static void setCurPosition(short position) {
        curPosition = position;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        settingsChanged = true;

        if (key.equals(getString(R.string.settings_show_notifications))) {
            AppState.setNextNotification();
        }
    }
}
