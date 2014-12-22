package com.untappedkegg.rally.home;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final String[] modArray = getResources().getStringArray(R.array.action_bar_modules);
        final short position = (short) getArguments().getInt(AppState.KEY_POSITION);
        if (position != 0) {
            try {
                getActivity().getActionBar().setTitle(modArray[position]);
//                ((ListView) getActivity().findViewById(R.id.left_drawer)).setItemChecked(position, true);
                NavDrawerFragment.getListView().setItemChecked(position, true);
                ActivityMain2.setCurPosition(position);
            } catch (Exception e) {
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
