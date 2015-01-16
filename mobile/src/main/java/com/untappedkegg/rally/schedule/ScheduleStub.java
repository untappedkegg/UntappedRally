package com.untappedkegg.rally.schedule;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.interfaces.Refreshable;


public class ScheduleStub extends Fragment implements Refreshable {

    public ScheduleStub() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.schedule_stub, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        final Fragment fragment = AppState.getSettings().getBoolean(getString(R.string.settings_use_experimental_sched), false) ? new ExpandableScheduleFragment() : new ScheduleFragment();
        // Pass the arguments on to the child
        fragment.setArguments(this.getArguments());
        this.getChildFragmentManager().beginTransaction().replace(R.id.schedule_stub_content, fragment).commit();
    }

    @Override
    public void refreshData() {
        ((Refreshable) getChildFragmentManager().findFragmentById(R.id.schedule_stub_content)).refreshData();
    }
}
