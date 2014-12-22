package com.untappedkegg.rally.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;

import com.untappedkegg.rally.R;
import com.untappedkegg.rally.ui.loaders.ImageDialogAdapter;

public class ImageDialog extends DialogFragment {
    /* ----- VARIABLES ----- */
    private String[] imgLinks;
    private int index;
    private ViewPager pager;
    //	private ImageButton prevButton;
    //	private ImageButton nextButton;

    /* ----- CONSTRUCTORS ----- */
    public ImageDialog() {
    }

    public static ImageDialog newInstance(String[] imgLinks, int index) {
        ImageDialog dialog = new ImageDialog();
        Bundle args = new Bundle();
        args.putStringArray("imgLinks", imgLinks);
        args.putInt("index", index);
        dialog.setArguments(args);
        return dialog;
    }

    /* ----- LIFECYCLE METHODS ----- */
    @SuppressLint("NewApi")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        imgLinks = (savedInstanceState != null) ? savedInstanceState.getStringArray("imgLinks") : args.getStringArray("imgLinks");
        index = (savedInstanceState != null) ? savedInstanceState.getInt("index") : args.getInt("index");

        Dialog dialog = new Dialog(getActivity(), DialogFragment.STYLE_NO_FRAME);
        dialog.setContentView(R.layout.generic_image_dialog);

        Drawable d = new ColorDrawable(Color.BLACK);
        d.setAlpha(130);
        dialog.getWindow().setBackgroundDrawable(d);

        pager = (ViewPager) dialog.findViewById(R.id.generic_image_dialog_pager);

        pager.setAdapter(new ImageDialogAdapter(getActivity(), imgLinks, new OnClick()));
        pager.setPageMargin((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -38, getResources().getDisplayMetrics()));
        pager.setOffscreenPageLimit(2);
        pager.setCurrentItem(index);

        return dialog;
    }

    /* ----- INHERITED METHODS ----- */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArray("imgLinks", imgLinks);
        outState.putInt("index", index);
        super.onSaveInstanceState(outState);
    }

    /* ----- NESTED CLASSES ----- */
    public class OnClick implements View.OnClickListener {
        /* ----- INHERITED METHODS ----- */
        @Override
        public void onClick(View v) {
            Dialog dialog = getDialog();
            if (dialog != null) {
                dialog.dismiss();
            }
        }
    }
}
