package com.keggemeyer.rallyamerica.home;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.keggemeyer.rallyamerica.AppState;
import com.keggemeyer.rallyamerica.R;
import com.keggemeyer.rallyamerica.data.DataFetcher;
import com.keggemeyer.rallyamerica.data.DbUpdated;
import com.keggemeyer.rallyamerica.interfaces.Refreshable;
import com.keggemeyer.rallyamerica.util.CommonIntents;
import com.keggemeyer.rallyamerica.util.DateManager;

import java.util.Calendar;
//import android.util.Log;

public class StandingsFragment extends Fragment implements DataFetcher.Callbacks, OnItemSelectedListener, Refreshable {

    private WebView mWebView;
    private ProgressBar progressBar;
    private String year;
    private int cClass;
    private int endo;
    private String fileName;
    Spinner champSpinner;
    Spinner yearSpinner;
    private String selection = "";
    //ActionBar
    private int position;
    private String[] modArray;

    public StandingsFragment() {
        //		 dataFetch = DataFetcher.getInstance();
        year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        cClass = 0;
        endo = 1;
        fileName = "";
    }

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
        position = getArguments().getInt(AppState.KEY_POSITION);
        modArray = getResources().getStringArray(R.array.action_bar_modules);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        //
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.standings, null);


        mWebView = (WebView) view.findViewById(R.id.web_view);
        WebSettings websettings = mWebView.getSettings();
        //	            websettings.setLoadWithOverviewMode(true);
        websettings.setUseWideViewPort(true);
        //	    		mWebView.setInitialScale(100);

        mWebView.setWebChromeClient(new WebChromeClient());


        if (position != 0) {
            getActivity().getActionBar().setTitle(modArray[position]);
//            ((ListView) getActivity().findViewById(R.id.left_drawer)).setItemChecked(position, true);
            NavDrawerFragment.getListView().setItemChecked(position, true);
            ActivityMain2.setCurPosition((short) position);
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final String[] mYearSpinner = new String[5];
        final short mYear = Short.parseShort(year);
        for (int i = 0; i < 5; i++) {
            mYearSpinner[i] = String.valueOf(mYear - i);
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mYearSpinner);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Spinners
        final Spinner champSpinner = (Spinner) getActivity().findViewById(R.id.standings_championship_spinner);
        champSpinner.setOnItemSelectedListener(this);

        final Spinner yearSpinner = (Spinner) getActivity().findViewById(R.id.standings_year_spinner);
        yearSpinner.setAdapter(adapter);
        yearSpinner.setOnItemSelectedListener(this);

        fileName = ((String) champSpinner.getItemAtPosition(0)).replaceAll(" ", "") + "_" + yearSpinner.getItemAtPosition(0);


        progressBar = (ProgressBar) getActivity().findViewById(R.id.web_view_progress);

        this.showPage();
    }

    @Override
    public void onDataFetchComplete(Throwable throwable, String parser) {
        if (parser.equalsIgnoreCase(AppState.FUNC_RA_STAND) /*|| parser.equalsIgnoreCase(AppState.FUNC_WRC_STAND)*/) {

            this.showPage();

            if (throwable == null) {
                DbUpdated.open();
                DbUpdated.updated_insert(AppState.MOD_STAND + fileName);
                DbUpdated.close();
            }

        }

    }

    /*----- CUSTOM METHODS -----*/
    public void refreshData() {
        DataFetcher.getInstance().standings_start(this, getLink(), fileName);
        progressBar.setVisibility(View.VISIBLE);
    }


    public void showPage() {
        DbUpdated.open();
        if (CommonIntents.fileExists(getActivity(), fileName) && DateManager.timeBetweenInDays(DbUpdated.lastUpdated_by_Source(AppState.MOD_STAND + fileName)) <= AppState.STAND_UPDATE_DELAY) {
            //			Log.e(this.getClass().getName(), "Loading File: " + fileName);
            //			Log.e("contents", CommonIntents.readFile(getActivity(), fileName));
            progressBar.setVisibility(View.GONE);
            mWebView.loadData(CommonIntents.readFile(getActivity(), fileName), "text/html", "UTF-8");
            //			mWebView.loadData(CommonIntents.readFile(getActivity(), fileName), "text/html", "UTF-8");
            mWebView.reload();
        } else {
            //			Log.e(this.getClass().getName(), "File Not Found");
            progressBar.setVisibility(View.VISIBLE);
            DataFetcher.getInstance().standings_start(this, getLink(), fileName);
        }
        DbUpdated.close();
    }

    private String getLink() {
        return String.format(AppState.RA_STANDINGS, endo, cClass, year);
    }


    //OnItemSelectedListener
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //		parent.getItemAtPosition(position);

        switch (parent.getId()) {

            case R.id.standings_championship_spinner:
                selection = (String) parent.getItemAtPosition(position);
                switch (position) {
                    // Driver - Overall
                    case 0:
                        endo = 1;
                        cClass = 0;
                        break;
                    // Driver - Super Production
                    case 1:
                        endo = 1;
                        cClass = 11;
                        break;
                    // Driver - 2-Wheel Drive
                    case 2:
                        endo = 1;
                        cClass = 10;
                        break;
                    // Driver - B-Spec
                    case 3:
                        endo = 1;
                        cClass = 8;
                        break;
                    // Co-Driver - Overall
                    case 4:
                        endo = 2;
                        cClass = 0;
                        break;
                    // Co-Driver - Super Production
                    case 5:
                        endo = 2;
                        cClass = 11;
                        break;
                    // Co-Driver - 2-Wheel Drive
                    case 6:
                        endo = 2;
                        cClass = 100;
                        break;
                    // Co-Driver - B-Spec
                    case 7:
                        endo = 2;
                        cClass = 8;
                        break;
                }
                break;
            case R.id.standings_year_spinner:
                year = (String) parent.getItemAtPosition(position);
                break;

        }

        fileName = selection.replaceAll(" ", "") + "_" + year;
        //		Log.w("file name = ",fileName);

        //For some reason I have to call this twice in order for it to take effect
        //		this.showPage();
        this.showPage();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub

    }
}
