package com.keggemeyer.rallyamerica.ui;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;

/**
 * Quick and dirty pop-up window that lets the user choose from a list of
 * options.
 *
 * @author alexg
 */
public class ModalArraySelector {
    private final Context ctx;

    public ModalArraySelector(Context ctx) {
        this.ctx = ctx;
    }

    public void show(String[] list, int selected, OnClickListener listener) {

        Builder builder = new Builder(ctx);

        builder.setSingleChoiceItems(list, selected, listener);
        builder.setCancelable(true);
        builder.setNegativeButton("Cancel", null);

        builder.create().show();
    }

    public void show(String[] list, OnClickListener listener) {
        show(list, -1, listener);
    }

}
