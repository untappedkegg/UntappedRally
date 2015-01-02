package com.untappedkegg.rally.ui;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.util.CommonIntents;
import com.untappedkegg.rally.util.DateManager;

import java.text.ParseException;

/**
 * A simple {@link android.app.DialogFragment} subclass.
 * Use the {@link BaseDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BaseDialogFragment extends DialogFragment implements View.OnClickListener {

    private static final String ARG_TITLE = "title";
    private static final String ARG_DESCRIPTION = "descr";
    private static final String ARG_PUBDATE = "pubdate";
    private static final String ARG_IMGLINK = "img_link";
    private static final String ARG_LINKIFY = "linkify";
    private static final String ARG_URL = "url";

    private String title, description, pubDate, imgLink, webLink;
    private TextView pubDateText, mDescription, mTitle;
    private ImageView imageView;
    private boolean linkify;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BlankFragment.
     */
    public static BaseDialogFragment newInstance(String title, String description, String pubDate, String webLink, String imgLink, boolean linkify) {
        BaseDialogFragment fragment = new BaseDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESCRIPTION, description.trim());
        args.putString(ARG_PUBDATE, pubDate);
        args.putString(ARG_IMGLINK, imgLink);
        args.putBoolean(ARG_LINKIFY, linkify);
        args.putString(ARG_URL, webLink);
        fragment.setArguments(args);
        return fragment;
    }

    public BaseDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        if (args != null) {
            title = args.getString(ARG_TITLE);
            description = args.getString(ARG_DESCRIPTION);
            pubDate = args.getString(ARG_PUBDATE);
            imgLink = args.getString(ARG_IMGLINK);
            linkify = args.getBoolean(ARG_LINKIFY);
            webLink = args.getString(ARG_URL);


        }
        this.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        this.setCancelable(true);
        this.setShowsDialog(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_detail, null);
        pubDateText = (TextView) view.findViewById(R.id.news_pub_date);
        mDescription = (TextView) view.findViewById(R.id.news_details_descr);
        mTitle = (TextView) view.findViewById(R.id.news_details_title);
        imageView = (ImageView) view.findViewById(R.id.news_details_img);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ImageLoader.getInstance().displayImage(imgLink, imageView);
        imageView.setOnClickListener(this);
        mTitle.setText(title);
        mTitle.setOnClickListener(this);

        mDescription.setText(description);
        try {
            pubDateText.setText(DateManager.FULL_HUMAN_READABLE.format(DateManager.DATABASE.parse(pubDate)));
        } catch (ParseException e) {
            pubDateText.setText(pubDate);
        }
        if (linkify) {
            Linkify.addLinks(mDescription, Linkify.ALL);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.news_details_title:
            case R.id.news_details_img:
                CommonIntents.openUrl(getActivity(), webLink);
                break;
        }
    }
}
