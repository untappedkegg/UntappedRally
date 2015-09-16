package com.untappedkegg.rally.util;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;

import com.untappedkegg.rally.R;

public class DialogManager {

    /**
     * Displays an alert dialog to the user with an OK option.
     *
     * @param c         the context used to display the alert.
     * @param throwable the throwable used to build the alert dialog. the name of the throwable
     *                  becomes the title, and the message becomes the body.
     * @param finish    if {@code c} is an instance of Activity, a true value will finish {@code c}
     *                  when the user clicks OK.
     */
    public static void raiseUIError(final Context c, Throwable throwable, final boolean finish) {
        raiseOneButtonDialog(c, throwable.toString(), throwable.getMessage(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (finish && c instanceof Activity) {
                    ((Activity) c).finish();
                }
            }
        });
    }

    /**
     * Displays an alert dialog to the user with an OK option.
     *
     * @param c      the context used to display the alert.
     * @param title  the title the alert or "" if the title should not display.
     * @param body   the body of the alert.
     * @param finish if {@code c} is an instance of Activity, a true value will
     *               finish {@code c} when the user clicks OK.
     */

    public static void raiseUIError(final Context c, String title, String body, final boolean finish) {
        raiseOneButtonDialog(c, title, body, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (finish && c instanceof Activity) {
                    ((Activity) c).finish();
                }
            }
        });
    }

    /**
     * Displays an alert dialog to the user with an OK option.
     *
     * @param c     the context used to display the alert.
     * @param title the title the alert or "" if the title should not display.
     * @param body  the body of the alert.
     * @param onOk  the custom OK click listener.
     */
    public static void raiseOneButtonDialog(final Context c, String title, String body, DialogInterface.OnClickListener onOk) {

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(c, R.style.AlertDialogCustom));
        builder.setTitle(title);
        //		builder.setInverseBackgroundForced(true);
        //		builder.set
        builder.setMessage(body);
        builder.setPositiveButton("OK", onOk);
        AlertDialog alert = builder.create();
        //		alert.setCanceledOnTouchOutside(true);
        alert.show();
    }

    /**
     * Displays an alert dialog to the user with an OK and Cancel option.
     *
     * @param c        the context used to display the alert.
     * @param title    the title the alert or "" if the title should not display.
     * @param body     the body of the alert.
     * @param onOk     the custom OK click listener.
     * @param onCancel the custom Cancel click listener.
     */
    public static void raiseTwoButtonDialog(final Context c, String title, String body, DialogInterface.OnClickListener onOk, DialogInterface.OnClickListener onCancel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle(title);
        if (!TextUtils.isEmpty(body))
        builder.setMessage(body);
        builder.setPositiveButton("OK", onOk);
        builder.setNegativeButton("Cancel", onCancel);
        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(true);
        alert.show();
    }

    public static void raiseListDialog(final Context c, String title, String[] list, DialogInterface.OnClickListener onClick, DialogInterface.OnClickListener onCancel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle(title);
        builder.setItems(list, onClick);
        builder.setNegativeButton("Cancel", onCancel);
        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(true);
        alert.show();
    }

    public static ProgressDialog buildProgressDialog(Context c, String title, boolean cancelable, DialogInterface.OnCancelListener onCancel) {
        ProgressDialog dialog = new ProgressDialog(c);
        dialog.setTitle(title);
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(cancelable);
        if (cancelable) {
            dialog.setOnCancelListener(onCancel);
        }

        return dialog;
    }
}
