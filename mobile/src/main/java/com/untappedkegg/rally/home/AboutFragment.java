package com.untappedkegg.rally.home;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.BuildConfig;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.ui.view.BaseWebviewDialog;
import com.untappedkegg.rally.util.CommonIntents;

/**
 * A simple {@link Fragment} subclass to display things about me and the app.
 *
 */
public final class AboutFragment extends Fragment implements View.OnClickListener {

    private TextView versionView;
    private ImageButton twitter, gPlus, youTube;
    private Tracker mTracker = AppState.getDefaultTracker();

    public AboutFragment() {
        // Required empty public constructor
    }

    /*----- LIFECYCLE METHODS -----*/
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        this.setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about_fragment, null, false);

        versionView = (TextView) view.findViewById(R.id.about_version);
        twitter = (ImageButton) view.findViewById(R.id.about_twitter_btn);
        gPlus = (ImageButton) view.findViewById(R.id.about_g_plus_btn);
        youTube = (ImageButton) view.findViewById(R.id.about_youtube_btn);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        twitter.setOnClickListener(this);
        gPlus.setOnClickListener(this);
        youTube.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        final short position = getArguments().getShort(AppState.KEY_POSITION);
        final String[] modArray = getResources().getStringArray(R.array.action_bar_modules);
        if (position != 0) {
            ActivityMain.setCurPosition(position);
            try {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(modArray[position]);
            } catch (Exception ignored) {
            }
        }
        versionView.setText(getString(R.string.about_version, BuildConfig.VERSION_NAME));

        mTracker.setScreenName("About");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
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
                mTracker.send(new HitBuilders.EventBuilder().setCategory("Action").setAction("View").setLabel("Change Log").build());
                return true;
            case R.id.menu_about_feedback:
                CommonIntents.sendFeedback(getActivity());
                mTracker.send(new HitBuilders.EventBuilder().setCategory("Action").setAction("View").setLabel("Feedback").build());
                return true;
            case R.id.menu_about_privacy:
                CommonIntents.openUrl(getActivity(), AppState.PRIVACY_POLICY);
                mTracker.send(new HitBuilders.EventBuilder().setCategory("Action").setAction("View").setLabel("Privacy").build());
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

                    mTracker.send(new HitBuilders.EventBuilder().setCategory("Action").setAction("View").setLabel("Twitter").build());
                } catch (ActivityNotFoundException e) {
                    CommonIntents.openUrl(activity, AppState.SOCIAL_TWITTER);
                }
                break;
            case R.id.about_g_plus_btn:
                try {
                    // com.google.android.apps.plus
                    startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(AppState.SOCIAL_G_PLUS)).setPackage("com.google.android.apps.plus"));

                    mTracker.send(new HitBuilders.EventBuilder().setCategory("Action").setAction("View").setLabel("Google Plus").build());
                } catch(ActivityNotFoundException e) {
                    CommonIntents.openUrl(activity, AppState.SOCIAL_G_PLUS);
                }
                break;
            case R.id.about_youtube_btn:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/" + BuildConfig.DEV_NAME)));
                    mTracker.send(new HitBuilders.EventBuilder().setCategory("Action").setAction("View").setLabel("YouTube").build());
                } catch (Exception ignored) {
                    Toast.makeText(getActivity(), R.string.no_apps_available, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

}
