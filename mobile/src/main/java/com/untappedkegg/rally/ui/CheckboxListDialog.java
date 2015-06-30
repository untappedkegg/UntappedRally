package com.untappedkegg.rally.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class CheckboxListDialog extends DialogFragment {
    /* ----- VARIABLES ----- */
    private String title;
    private boolean[] checkedItems;
    private String[] list;
    private CheckboxListDialogCallbacks callback;

    /* ----- CONSTRUCTORS ----- */
    public CheckboxListDialog() {
    }

    public static CheckboxListDialog newInstance(String title, boolean[] checkedItems, String[] list, CheckboxListDialogCallbacks callback) {
        CheckboxListDialog dialog = new CheckboxListDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putBooleanArray("checkedItems", checkedItems);
        args.putStringArray("list", list);
        dialog.setArguments(args);
        dialog.setCallback(callback);
        return dialog;
    }

    /* ----- LIFECYCLE METHODS ----- */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        title = (savedInstanceState != null) ? savedInstanceState.getString("title") : args.getString("title");
        checkedItems = (savedInstanceState != null) ? savedInstanceState.getBooleanArray("checkedItems") : args.getBooleanArray("checkedItems");
        list = (savedInstanceState != null) ? savedInstanceState.getStringArray("list") : args.getStringArray("list");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMultiChoiceItems(list, checkedItems, new CheckboxListOnClickListener());
        builder.setPositiveButton("OK", new PositiveOnClickListener());
        builder.setNegativeButton("Cancel", new NegativeOnClickListener());
        return builder.create();
    }

    /* ----- INHERITED METHODS ----- */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("title", title);
        outState.putBooleanArray("checkedItems", checkedItems);
        outState.putStringArray("list", list);
        super.onSaveInstanceState(outState);
    }

    /* ----- CUSTOM METHODS ----- */
    public void setCallback(CheckboxListDialogCallbacks callback) {
        this.callback = callback;
    }

    /* ----- NESTED INTERFACES ----- */
    public interface CheckboxListDialogCallbacks {
        void doPositiveClick(boolean[] checkedItems, String[] list);

        void doNegativeClick();
    }

    /* ----- NESTED CLASSES ----- */
    private class CheckboxListOnClickListener implements DialogInterface.OnMultiChoiceClickListener {
        /* ----- INHERITED METHODS ----- */
        @Override
        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
            checkedItems[which] = isChecked;
        }
    }

    private class PositiveOnClickListener implements DialogInterface.OnClickListener {
        /* ----- INHERITED METHODS ----- */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (callback != null) {
                callback.doPositiveClick(checkedItems, list);
            }
        }
    }

    private class NegativeOnClickListener implements DialogInterface.OnClickListener {
        /* ----- INHERITED METHODS ----- */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (callback != null) {
                callback.doNegativeClick();
            }
        }
    }
}
