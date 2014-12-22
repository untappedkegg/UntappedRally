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

package com.untappedkegg.rally.home;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.data.BaseDbAccessor;
import com.untappedkegg.rally.event.EventActivity;
import com.untappedkegg.rally.feedback.Feedback;
import com.untappedkegg.rally.feedback.FeedbackFragment;
import com.untappedkegg.rally.interfaces.Refreshable;
import com.untappedkegg.rally.news.NewsFragment;
import com.untappedkegg.rally.schedule.ScheduleFragment;
import com.untappedkegg.rally.social.YouTubeFragment;
import com.untappedkegg.rally.util.CommonIntents;
import com.untappedkegg.rally.util.DialogManager;

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
public class ActivityMain extends FragmentActivity implements ScheduleFragment.Callbacks, HomeFragment.Callbacks {
    private DrawerLayout mDrawerLayout;
    private ListView mLeftDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private int curPosition = 0;

    private CharSequence mDrawerTitle;
    private static CharSequence mTitle;
    private String[] mActionBarDrawer;
    private Intent intent;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionBar = getActionBar();

        mTitle = mDrawerTitle = getTitle();
        mActionBarDrawer = getResources().getStringArray(R.array.action_bar_modules);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mLeftDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mLeftDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mActionBarDrawer));
        mLeftDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        actionBar.setDisplayHomeAsUpEnabled(true);
        //        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */) {
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                actionBar.setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                if (drawerView.equals(mLeftDrawerList)) {
                    mDrawerLayout.closeDrawer(Gravity.RIGHT);
                }
                actionBar.setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            //            selectItem(0);
            Bundle bundle = new Bundle();
            bundle.putInt(AppState.KEY_POSITION, 0);
            Fragment home = new HomeFragment();
            home.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, home).commit();
            mLeftDrawerList.setItemChecked(0, true);
            setTitle(mActionBarDrawer[0]);
        } else {
            //        	mTitle = (CharSequence) savedInstanceState.get("title");
            //        	this.setTitle(mTitle);

            actionBar.setTitle(mTitle);
        }
        //        AppState.hideSystemUI(this);
        //        final Activity acty = this;
        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                // Note that system bars will only be "visible" if none of the
                // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    // TODO: The system bars are visible. Make any desired
                    // adjustments to your UI, such as showing the action bar or
                    // other navigational controls.
                } else {
                    // TODO: The system bars are NOT visible. Make any desired
                    // adjustments to your UI, such as hiding the action bar or
                    // other navigational controls.
                }
                //                AppState.hideSystemUI(acty);
            }
        });


        BaseDbAccessor.open();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("title", mTitle.toString());
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
            //        	mDrawerList.performClick();
            if (!mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
                mDrawerLayout.openDrawer(Gravity.LEFT);
            } else {
                mDrawerLayout.closeDrawers();
            }
            //        	mDrawerList.sh
            return true;
            //        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            //        	this.selectItem(0);
            //        	return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(Gravity.LEFT) || mDrawerLayout.isDrawerOpen(Gravity.RIGHT);
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
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
                if (fragment.isVisible()) {
                    if (fragment instanceof Refreshable) {
                        ((Refreshable) fragment).refreshData();
                    }
                }
                //        case R.id.action_websearch:
                //            // create intent to perform web search for this planet
                //            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                //            intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
                //            // catch event that there's no activity to handle intent
                //            if (intent.resolveActivity(getPackageManager()) != null) {
                //                startActivity(intent);
                //            } else {
                //                Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
                //            }
                //            return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    void selectItem(int position) {
        // update the main content by replacing fragments
        String uri = null;
        Fragment fragment = null;
        Bundle icicle = new Bundle();
        icicle.putInt(AppState.KEY_POSITION, position);
        switch (position) {
            case 0://switch to 2
                //			fragment = new ScheduleFragment();
                //			return new ScheduleFragment();
                uri = HomeFragment.class.getName();
                break;
            case 1:
                uri = ScheduleFragment.class.getName();
                //			fragment = new RadioVideoFragment();
                break;
            case 2:
                //			fragment = new NewsFragment();
                //			break;
                uri = NewsFragment.class.getName();
                break;
            case 3:
                uri = StandingsFragment.class.getName();
                break;
            case 4:
                //			uri = Videos.class.getName();
                uri = YouTubeFragment.class.getName();
                break;
            case 7:
                //			uri = FeedbackFragment.class.getName();
                intent = new Intent(this, Feedback.class);
                startActivity(intent);
                //			menu.findItem(R.id.menu_refresh).setVisible(false);
                break;
            case 10:
                this.finish();
                break;
            default:

                break;
        }


        try {
            Class<Fragment> fragmentClass = (Class<Fragment>) Class.forName(uri);
            fragment = fragmentClass.newInstance();
        } catch (Exception e) {
            if (position != 7 && position != 10) {
                DialogManager.raiseUIError(this, "Error", "This feature has not been implemented yet", false);
            }

        }


        if (fragment != null) {
            if (fragment instanceof FeedbackFragment) {
                fragment.setHasOptionsMenu(false);
            }

            FragmentManager fragmentManager = getSupportFragmentManager();
            //        FragmentTransaction trans = fragmentManager.beginTransaction();
            //        trans.replace(R.id.content_frame, fragment);
            //        if (fragmentManager.getBackStackEntryCount() < 1) {
            if (curPosition != position) {
                fragment.setArguments(icicle);
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(((Object) fragment).getClass().getSimpleName()).commit();

            } else {
                //        	fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                //        	fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount())
            }


            //        mDrawerList.setItemChecked(position, true);
            setTitle(mActionBarDrawer[position]);
        }

        // update selected item and title, then close the drawer

        mDrawerLayout.closeDrawer(mLeftDrawerList);
        curPosition = position;
    }

    @Override
    public void setTitle(CharSequence title) {
        if (AppState.isNullOrEmpty(title.toString())) {
            mTitle = getResources().getString(R.string.app_name);
        } else {
            mTitle = title;
        }
        actionBar.setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    //    @Override
    //	public void onWindowFocusChanged(boolean hasFocus) {
    //	        super.onWindowFocusChanged(hasFocus);
    //	    if (hasFocus ) {
    //	    	AppState.hideSystemUI(this);
    //	    }
    //	}

    // ScheduleFragment.Callbacks
    @Override
    public void showEventDetail(String fragment, String args, String eventName) {
        CommonIntents.startNewContainer(this, fragment, args, EventActivity.class, eventName);

    }

    @Override
    public void onNavDrawerItemSelected(int position) {
        selectItem(position);
    }

    // HomeFragment.Callbacks
}
