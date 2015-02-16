package com.untappedkegg.rally.home;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.BuildConfig;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.ui.view.BaseWebviewDialog;
import com.untappedkegg.rally.util.CommonIntents;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public final class AboutFragment extends Fragment implements View.OnClickListener {

    private TextView versionView;
    private ImageButton twitter, gPlus;

    public AboutFragment() {
        // Required empty public constructor
    }

    /*----- LIFECYCLE METHODS -----*/
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about_fragment, null, false);

        versionView = (TextView) view.findViewById(R.id.about_version);
        twitter = (ImageButton) view.findViewById(R.id.about_twitter_btn);
        gPlus = (ImageButton) view.findViewById(R.id.about_g_plus_btn);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        twitter.setOnClickListener(this);
        gPlus.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        final short position = getArguments().getShort(AppState.KEY_POSITION);
        final String[] modArray = getResources().getStringArray(R.array.action_bar_modules);
        if (position != 0) {
            ActivityMain.setCurPosition(position);
            try {
                ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(modArray[position]);
            } catch (Exception e) {
            }
        }
        versionView.setText(getString(R.string.about_version, BuildConfig.VERSION_NAME));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().invalidateOptionsMenu();
    }

    /*----- INHERITED METHODS -----*/
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.about, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_about_changelog:
                DialogFragment changelogFragment = BaseWebviewDialog.newInstance(AppState.CHANGELOG_URL, R.string.changelog);
                this.getChildFragmentManager().beginTransaction().add(changelogFragment, changelogFragment.toString()).commit();
                return true;
            case R.id.menu_about_feedback:
                CommonIntents.sendFeedback(getActivity());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        final Activity activity = getActivity();
        switch (v.getId()) {

            case R.id.about_twitter_btn:
                try {
                    // com.twitter.android
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=" + BuildConfig.DEV_NAME)));
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    CommonIntents.openUrl(activity, AppState.SOCIAL_TWITTER);
                }
                break;
            case R.id.about_g_plus_btn:
                try {
                    // com.google.android.apps.plus
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(AppState.SOCIAL_G_PLUS));
                    intent.setPackage("com.google.android.apps.plus");
                    startActivity(intent);
                } catch(ActivityNotFoundException e) {
                    e.printStackTrace();
                    CommonIntents.openUrl(activity, AppState.SOCIAL_G_PLUS);
                }

                break;
        }
    }

}
