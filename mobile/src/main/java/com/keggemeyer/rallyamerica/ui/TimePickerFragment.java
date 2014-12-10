package com.keggemeyer.rallyamerica.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.TimePicker;

import com.keggemeyer.rallyamerica.AppState;
import com.keggemeyer.rallyamerica.util.DateManager;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * {@link DialogFragment} class used to set the time (hour and minute)
 * of a Button, TextView, etc.
 * The {@code R.id} of the object whose time is to be set, should
 * be passed in as an {@code int} via a {@link Bundle} with key {@code id}.
 * <p>
 * The Start Time for the {@link TimePickerFragment} should be passed
 * in as a {@link String} via a bundle, with key {@code AppState.KEY_ARGS}
 * </p><p>
 * On <strong>OK</strong> selected, the object whose {@code R.id} was passed in, is
 * updated to the time the user selected
 * </p>
 *
 * @author kpetg6
 */
public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    /* ----- VARIABLES ----- */
    private int resId;
    private final Calendar c = Calendar.getInstance();

    /* ----- INHERITED METHODS ----- */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        resId = getArguments().getInt("id");
        String startTime = getArguments().getString(AppState.KEY_ARGS);

        if (!AppState.isNullOrEmpty(startTime)) {
            try {
                c.setTime(DateManager.parse(startTime, DateManager.GOMIZZOU_TIMEONLY));
            } catch (ParseException e) {
                c.setTime(new Date());
                if (AppState.DEBUG) {
                    e.printStackTrace();
                }
            }
        }

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), DateFormat.is24HourFormat(getActivity()));
    }

    /*
     * (non-Javadoc)
     * @see android.app.TimePickerDialog.OnTimeSetListener#onTimeSet(android.widget.TimePicker, int, int)
     *
     * This method is called when the user clicks the "OK" button.
     * The hour and minute are set in the Calendar object, parsed
     * into the correct format, and set as a String the the object
     * specified by resId
     */
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
        // Do something set the time back the the textview
        TextView tv = (TextView) getActivity().findViewById(resId);

        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minuteOfHour);
        tv.setText(DateManager.format(c.getTime(), DateManager.GOMIZZOU_TIMEONLY));
    }
}
