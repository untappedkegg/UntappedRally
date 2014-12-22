package com.untappedkegg.rally.home;


import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class AboutFragment extends Fragment {
    private TextView versionView, main, learnMore;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AboutFragment.
     */
    // TODO: Rename and change types and number of parameters
//    public static AboutFragment newInstance() {
//        AboutFragment fragment = new AboutFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about_fragment, null, false);

        final short position = (short) getArguments().getInt(AppState.KEY_POSITION);
        final String[] modArray = getResources().getStringArray(R.array.action_bar_modules);
        if (position != 0) {
            try {
                getActivity().getActionBar().setTitle(modArray[position]);
                //                ((ListView) getActivity().findViewById(R.id.left_drawer)).setItemChecked(position, true);
                NavDrawerFragment.getListView().setItemChecked(position, true);
                ActivityMain2.setCurPosition(position);
            } catch (Exception e) {
            }
        }

        versionView = (TextView) view.findViewById(R.id.about_version);
        main = (TextView) view.findViewById(R.id.about_main);
        learnMore = (TextView) view.findViewById(R.id.about_learn_more);

        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.about_fragment, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        String version;
        try {
            version = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(((Object) this).getClass().getSimpleName(), "Could not find our package. Initiate FUBAR sequence.");
            version = "";
        }
        versionView.setText(getString(R.string.about_version, version));
        main.setText(R.string.about_main);
        learnMore.setText(R.string.about_learn_more);

    }
}
