package com.keggemeyer.rallyamerica.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.keggemeyer.rallyamerica.R;

public abstract class BaseGridView extends BaseLoaderFragment implements OnItemClickListener {

    protected GridView gridView;

    /*----- LIFECYCLE METHODS -----*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.generic_grid, null, false);
        gridView = (GridView) view.findViewById(getGridViewId());
        progressBar = (ProgressBar) view.findViewById(getProgressId());
        emptyView = view.findViewById(getEmptyViewId());


        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (gridView == null) gridView = (GridView) getActivity().findViewById(getGridViewId());
        gridView.setOnItemClickListener(this);
        gridView.setAdapter(adapter);


        if (emptyView != null) {
            gridView.setEmptyView(emptyView);
        }

    }

	/*----- CUSTOM METHODS -----*/

    protected int getGridViewId() {
        return android.R.id.list;
    }

    protected int getProgressId() {
        return R.id.generic_grid_progress;
    }
}
