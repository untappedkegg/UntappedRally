package com.keggemeyer.rallyamerica.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

public class BaseDialog extends Dialog {
    private Activity acty;

    public BaseDialog(Context context) {
        super(context);
        this.acty = (Activity) context;
    }

    public BaseDialog(Context context, int theme) {
        super(context, theme);
    }

    public BaseDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


}
