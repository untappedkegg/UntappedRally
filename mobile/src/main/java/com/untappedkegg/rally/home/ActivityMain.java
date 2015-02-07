package com.untappedkegg.rally.home;


import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.plus.PlusShare;
import com.google.android.gms.plus.model.people.Person;
import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.BuildConfig;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.data.BaseDbAccessor;
import com.untappedkegg.rally.event.EventActivity;
import com.untappedkegg.rally.event.EventPhotos;
import com.untappedkegg.rally.interfaces.Refreshable;
import com.untappedkegg.rally.interfaces.ScheduleItemClickReceiver;
import com.untappedkegg.rally.news.NewsFragment;
import com.untappedkegg.rally.preference.SettingsFragment;
import com.untappedkegg.rally.schedule.DbSchedule;
import com.untappedkegg.rally.schedule.ScheduleStub;
import com.untappedkegg.rally.social.YouTubeFragment;
import com.untappedkegg.rally.stages.EventStages;
import com.untappedkegg.rally.util.CommonIntents;
import com.untappedkegg.rally.util.DateManager;
import com.untappedkegg.rally.util.DialogManager;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
public class ActivityMain extends ActionBarActivity implements ScheduleItemClickReceiver, HomeFragment.Callbacks, NextEventFragment.Callbacks, NavDrawerFragment.Callbacks, PopupMenu.OnMenuItemClickListener {
    private DrawerLayout mDrawerLayout;
    private static short curPosition = 0;

    private static CharSequence mTitle;
    private String[] mActionBarDrawer;
    private View menuView;

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
            bundle.putInt(AppState.KEY_POSITION, 0);
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
            if (!mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
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
    }

    void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setDisplayShowTitleEnabled(true);
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
        Intent intent = null;
        Fragment fragment = null;
        Bundle icicle = new Bundle();
        icicle.putInt(AppState.KEY_POSITION, position);
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
                fragment = new YouTubeFragment();
                break;
            case 5: // Spectating
                CommonIntents.openUrl(this, "http://www.rally-america.com/safety");
                return;
            case 6: // Worker Info
                CommonIntents.openUrl(this, "http://www.rally-america.com/volunteer");
                return;
            case 7:
                this.sendFeedback();
                return;
            case 8: // Settings
                fragment = new SettingsFragment();
//                startActivity(intent);
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

        if (fragment != null) {
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
           DialogManager.raiseUIError(this, "Error", "This feature has not been implemented yet", false);
        }

    }

    private void sendFeedback() {
        List<Intent> targetedShareIntents = new ArrayList<Intent>();

            final String emailMsg = String.format("App Version: %s\nAndroid: %s : %s\nDevice: %s (%s)\nPlease leave the above lines for debugging purposes. Thank you!\n\n", BuildConfig.VERSION_NAME, Build.VERSION.SDK_INT, Build.VERSION.RELEASE, /*Build.FINGERPRINT,*/ Build.MODEL, Build.DEVICE);

        // Google+
        ArrayList<Person> recipients = new ArrayList<Person>();
        recipients.add(PlusShare.createPerson("109961307643513437237", BuildConfig.DEV_NAME));
        targetedShareIntents.add(new PlusShare.Builder(this).setType("text/plain").setRecipients(recipients).getIntent());

        // Email
        try {
            targetedShareIntents.add(CommonIntents.getShareIntent("email", "Feedback: " + getString(R.string.app_name), emailMsg).putExtra(Intent.EXTRA_EMAIL, new String[] {"UntappedKegg@gmail.com"}));
        } catch (Exception e) { }

        try {
            targetedShareIntents.add(CommonIntents.getShareIntent("gmail", "Feedback: " + getString(R.string.app_name), emailMsg).putExtra(Intent.EXTRA_EMAIL, new String[] {"UntappedKegg@gmail.com"}));
        } catch (Exception e) { }

        // Twitter
        Intent twitterIntent = CommonIntents.getShareIntent("twitter", "Untapped Rally", "@UntappedKegg ");
        if(twitterIntent != null)
            targetedShareIntents.add(twitterIntent);

        // Market
        try {
            final String mPackageName = getPackageName();
            final String installer = getPackageManager().getInstallerPackageName(mPackageName);
            Intent marketIntent = null;

            if (AppState.MARKET_GOOGLE.equalsIgnoreCase(installer)) {
                marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(AppState.APP_LINK_GOOGLE + mPackageName));
                marketIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

            } else if (AppState.MARKET_AMAZON.equalsIgnoreCase(installer)) {
                marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(AppState.APP_LINK_AMAZON + mPackageName));
                marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            if (marketIntent != null)
            targetedShareIntents.add(marketIntent);

        } catch (Exception e) { }

        if(!targetedShareIntents.isEmpty()) {
            Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), "Send Feedback via:");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
            startActivity(chooserIntent);
        } else Toast.makeText(this, R.string.no_apps_available, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void setTitle(CharSequence title) {
        if (AppState.isNullOrEmpty(title.toString())) {
            mTitle = getResources().getString(R.string.app_name);
        } else {
            mTitle = title;
        }
        getSupportActionBar().setTitle(mTitle);
    }

    // CALLBACKS
    // ScheduleFragment.Callbacks
    @Override
    public void showEventDetail(String fragment, String eventName, int id) {
//        CommonIntents.startNewContainer(this, fragment, args, EventActivity.class, eventName);
//        Log.e("SELECT showEventDetail", "args = " + args + " id = " + id);
        Intent intent = new Intent(AppState.getApplication(), EventActivity.class);
        intent.putExtra(AppState.KEY_URI, fragment);
        intent.putExtra(SearchManager.QUERY, eventName);
        intent.putExtra(AppState.KEY_ID, id);

        startActivity(intent);

    }

    public void showEventDetail(String fragment, String link, String eventName, int id) {
//        CommonIntents.startNewContainer(this, fragment, args, EventActivity.class, eventName);
        Intent intent = new Intent(AppState.getApplication(), EventActivity.class);
        intent.putExtra(AppState.KEY_ARGS, link);
        intent.putExtra(AppState.KEY_URI, fragment);
        intent.putExtra(SearchManager.QUERY, eventName);
        intent.putExtra(AppState.KEY_ID, id);

        startActivity(intent);

    }

    public static void setCurPosition(short position) {
        curPosition = position;
    }

    /*----- SCHEDULE FRAGMENT ONCLICK METHODS -----*/
    public void onScheduleMenuClick(View v) {
        menuView = ((View) v.getParent().getParent());
        final boolean isFinished = DbSchedule.isEventFinished(((TextView) menuView.findViewById(R.id.sched_id)).getText().toString());
        final String date = ((TextView) menuView.findViewById(R.id.sched_date)).getText().toString();
        PopupMenu popup = new PopupMenu(this, v);
        popup.inflate(R.menu.schedule);
        if (AppState.isNullOrEmpty(((TextView) menuView.findViewById(R.id.sched_website)).getText().toString())) {
            popup.getMenu().removeItem(R.id.menu_schedule_website);
        }
        if (!isFinished) {
            popup.getMenu().removeItem(R.id.menu_schedule_photos);
        }
        if (isFinished || "TBD".equalsIgnoreCase(date) || "CANCELLED".equalsIgnoreCase(date)) {
            popup.getMenu().removeItem(R.id.menu_schedule_add_to_cal);
        }
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        final String website = ((TextView) menuView.findViewById(R.id.sched_event_website)).getText().toString();
        final String eventName = ((TextView) menuView.findViewById(R.id.sched_title)).getText().toString();
        final int id = Integer.parseInt(((TextView) menuView.findViewById(R.id.sched_id)).getText().toString());

        switch (item.getItemId()) {
            case R.id.menu_schedule_stages:
                showEventDetail(EventStages.class.getName(), website, eventName, id);
                break;
            case R.id.menu_schedule_photos:
                showEventDetail(EventPhotos.class.getName(), website, eventName, id);
                break;
            case R.id.menu_schedule_website:
                CommonIntents.openUrl(this, ((TextView) menuView.findViewById(R.id.sched_website)).getText().toString());
                break;
            case R.id.menu_schedule_event_website:
                CommonIntents.openUrl(this, website);
                break;
            case R.id.menu_schedule_add_to_cal:
                final String startDate = ((TextView) menuView.findViewById(R.id.sched_start_date)).getText().toString();
                final String endDate = ((TextView) menuView.findViewById(R.id.sched_end_date)).getText().toString();
                final String location = ((TextView) menuView.findViewById(R.id.sched_location)).getText().toString();


                try {
                    CommonIntents.addRallyToCalendar(this, eventName, DateManager.ISO8601_DATEONLY.parse(startDate), DateManager.add(Calendar.DAY_OF_MONTH, DateManager.ISO8601_DATEONLY.parse(endDate), 1), location);
                    return true;
                } catch (ParseException e) {
                    Toast.makeText(this, getResources().getString(R.string.calendar_error), Toast.LENGTH_LONG).show();
                }
        }
        return true;
    }
}
