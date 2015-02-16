package com.untappedkegg.rally.preference;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.home.ActivityMain;

/**
 * An {@link android.support.v4.app.Fragment} compatible implementation of {@link android.preference.PreferenceFragment}
 */
public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener, View.OnClickListener {

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button notifyButton = new Button(getActivity());
        notifyButton.setText(R.string.settings_notify_button);
        notifyButton.setOnClickListener(this);
        getListView().addFooterView(notifyButton);

    }

    @Override
    public void onStart() {
        super.onStart();
        AppState.getSettings().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        final int count = getPreferenceScreen().getPreferenceCount();
        for (int i = 0; i < count; ++i) {
            Preference preference = getPreferenceScreen().getPreference(i);
            if (preference instanceof PreferenceGroup) {
                PreferenceGroup preferenceGroup = (PreferenceGroup) preference;
                final int innerCount = preferenceGroup.getPreferenceCount();
                for (int j = 0; j < innerCount; ++j) {
                    updatePreference(preferenceGroup.getPreference(j));
                }
            } else {
                updatePreference(preference);
            }
        }

        final short position = getArguments().getShort(AppState.KEY_POSITION);
        final String[] modArray = getResources().getStringArray(R.array.action_bar_modules);
        if (position != 0) {
            ActivityMain.setCurPosition(position);
            try {
                ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(modArray[position]);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        AppState.getSettings().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().invalidateOptionsMenu();
    }

    /*----- INHERITED METHODS -----*/
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals("setting_notifications")) {
            AppState.setNextNotification();
        } else if (key.equals("pref_news_cutoff") || key.equals("event_feeds")) {
            AppState.NEWS_REFRESH = true;
        }

        updatePreference(findPreference(key));
    }

    @Override
    public void onClick(View v) {
        final AlarmManager alarm = (AlarmManager) AppState.getApplication().getSystemService(Context.ALARM_SERVICE);

        //The intent is declared in the manifest, if changed here it must also be changed there
        PendingIntent pendingIntent = PendingIntent.getBroadcast(AppState.getApplication(), 0, new Intent("com.untappedkegg.rally.notification.NEXT_EVENT_RECEIVER"), PendingIntent.FLAG_UPDATE_CURRENT);
        alarm.set(AlarmManager.RTC, 0, pendingIntent);
        AppState.setNextNotification();
    }

    /*----- CUSTOM METHODS -----*/

    /**
     * Updated the UI to reflect changes in the Shared Preferences
     *
     * @param preference the preference that was changed
     */
    private void updatePreference(Preference preference) {
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            listPreference.setSummary(listPreference.getEntry());
        }
    }


}
