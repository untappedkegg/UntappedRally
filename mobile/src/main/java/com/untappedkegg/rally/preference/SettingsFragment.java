package com.untappedkegg.rally.preference;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;

/**
 * A simple {@link Fragment} subclass.
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

        PreferenceCategory fakeHeader = new PreferenceCategory(getActivity());
        fakeHeader.setTitle(R.string.pref_header_notifications);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_notification);

        

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button notifyButton = new Button(getActivity());
        notifyButton.setText(R.string.settings_notify_button);
        notifyButton.setOnClickListener(this);
        getListView().addFooterView(notifyButton);
        
        bindPreferenceSummaryToValue(findPreference("setting_notifications_ringtone"));
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); ++i) {
            Preference preference = getPreferenceScreen().getPreference(i);
            if (preference instanceof PreferenceGroup) {
                PreferenceGroup preferenceGroup = (PreferenceGroup) preference;
                for (int j = 0; j < preferenceGroup.getPreferenceCount(); ++j) {
                    updatePreference(preferenceGroup.getPreference(j));
                }
            } else {
                updatePreference(preference);
            }
        }
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        final Activity activity = getActivity();
        if(activity instanceof OnSharedPreferenceChangeListener) {
            ((OnSharedPreferenceChangeListener) activity).onSharedPreferenceChanged(sharedPreferences, key);
        }
        updatePreference(findPreference(key));
    }

    private void updatePreference(Preference preference) {
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            listPreference.setSummary(listPreference.getEntry());
        } /*else if (preference instanceof RingtonePreference) {
            RingtonePreference rtp = (RingtonePreference) preference;
            rtp.setSummary(rtp.getSharedPreferences().getString(rtp.getKey(), "content://settings/system/notification_sound"));
        }*/
    }


    @Override
    public void onClick(View v) {
        final AlarmManager alarm = (AlarmManager) AppState.getApplication().getSystemService(Context.ALARM_SERVICE);

        //The intent is declared in the manifest, if changed here it must also be changed there
        PendingIntent pendingIntent = PendingIntent.getBroadcast(AppState.getApplication(), 0, new Intent("com.untappedkegg.rally.notification.NEXT_EVENT_RECEIVER"), PendingIntent.FLAG_UPDATE_CURRENT);
        alarm.set(AlarmManager.RTC, 0, pendingIntent);
        AppState.setNextNotification();
    }
}
