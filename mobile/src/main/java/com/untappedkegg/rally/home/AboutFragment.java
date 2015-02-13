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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    private Button changeBtn;
    private ImageButton twitter, gPlus;

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about_fragment, null, false);

        final short position = (short) getArguments().getInt(AppState.KEY_POSITION);
        final String[] modArray = getResources().getStringArray(R.array.action_bar_modules);
        if (position != 0) {
            try {
                ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(modArray[position]);
                NavDrawerFragment.getListView().setItemChecked(position, true);
                ActivityMain.setCurPosition(position);
            } catch (Exception e) {
            }
        }

        versionView = (TextView) view.findViewById(R.id.about_version);
        changeBtn = (Button) view.findViewById(R.id.about_changelog_btn);
        twitter = (ImageButton) view.findViewById(R.id.about_twitter_btn);
        gPlus = (ImageButton) view.findViewById(R.id.about_g_plus_btn);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        changeBtn.setOnClickListener(this);
        twitter.setOnClickListener(this);
        gPlus.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        versionView.setText(getString(R.string.about_version, BuildConfig.VERSION_NAME));

    }

    @Override
    public void onClick(View v) {

        final Activity activity = getActivity();
        switch (v.getId()) {
            case R.id.about_changelog_btn:

                DialogFragment changelogFragment = BaseWebviewDialog.newInstance(AppState.CHANGELOG_URL, R.string.changelog);
                this.getChildFragmentManager().beginTransaction().add(changelogFragment, changelogFragment.toString()).commit();
                break;
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
