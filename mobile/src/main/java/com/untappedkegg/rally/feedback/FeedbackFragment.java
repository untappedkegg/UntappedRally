package com.untappedkegg.rally.feedback;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.util.CommonIntents;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FeedbackFragment extends Fragment implements View.OnClickListener {
    /* ----- CONSTANTS ----- */
    //	private static final int ANONYMOUS_LIMIT = AppState.ANONYMOUS_LIMIT;
    //	private static final int TWITTER_LIMIT = AppState.TWITTER_LIMIT;
    private static final String LOG_TAG = FeedbackFragment.class.getSimpleName();

    /* ----- VARIABLES -----*/
    private TextView mTextView;
    private EditText mEditText;
    private TextView mNotifyLimits;
    private String mFbMsg;
    boolean successful = false;
    //	private Callbacks callbacks;
    private final String SUBJECT = "Feedback: " + AppState.getApplication().getString(R.string.app_name);

	/* ----- LIFECYCLE METHODS ----- */

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().getActionBar().setDisplayShowHomeEnabled(true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            getActivity().getActionBar().setHomeButtonEnabled(true);
        }
        return inflater.inflate(R.layout.feedback, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //		getActivity().getActionBar().setDisplayShowHomeEnabled(true);
        mEditText = (EditText) getActivity().findViewById(R.id.fb_text);
        mTextView = (TextView) getActivity().findViewById(R.id.TextCounter);
        mNotifyLimits = (TextView) getActivity().findViewById(R.id.FeedbackLimit);
        mEditText.addTextChangedListener(mTextEditorWatcher);

        //restores the EditText if the last Anonymous attempt was unsuccessful
        Activity activity = getActivity();
        String ret = "";
        File file = activity.getFileStreamPath("feedback.txt");
        if (file.exists()) {
            try {
                final InputStream inputStream = getActivity().openFileInput("feedback.txt");

                if (inputStream != null) {
                    //		            final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String receiveString;
                    final StringBuilder stringBuilder = new StringBuilder();

                    while ((receiveString = bufferedReader.readLine()) != null) {
                        stringBuilder.append(receiveString);
                    }

                    inputStream.close();
                    ret = stringBuilder.toString();
                }
            } catch (FileNotFoundException e) {
                Log.d(LOG_TAG, "File not found " + e.toString());
            } catch (IOException e) {
                Log.d(LOG_TAG, "Can not read file: " + e.toString());
            }
        }

        mEditText.setText(ret);

        //restores the EditText on screen rotation
        if (savedInstanceState != null) {
            // Restore last state for checked position.
            mFbMsg = savedInstanceState.getString("feedbackMsg");
        }

        Button send = (Button) getActivity().findViewById(R.id.fb_send);
        send.setOnClickListener(this);
    }

    //	@Override
    //	public void onAttach(Activity activity) {
    //		super.onAttach(activity);
    //		try {
    //			callbacks = (Callbacks) activity;
    //		} catch(ClassCastException e) {
    //			throw new ClassCastException(activity.toString() + " must implement FeedbackFragment.Callbacks.");
    //		}
    //	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("feedbackMsg", mFbMsg);
    }

    /* ----- INHERITED METHODS ----- */
    //	View.OnClickListener
    @Override
    public void onClick(View v) {
        mFbMsg = mEditText.getText().toString();

        if (mFbMsg.length() > 0) {
            //			callbacks.onSendPressed(mFbMsg);
            CommonIntents.sendEmail(getActivity(), "TeamKyleRacing@gmail.com", SUBJECT, mFbMsg);
        } else {
            Activity activity = getActivity();
            Toast.makeText(activity, "Please include some text for feedback.", Toast.LENGTH_SHORT).show();
        }
    }

    /* ----- CUSTOM METHODS -----*/
    //	Counts number of characters the user has entered into the EditText
    private final TextWatcher mTextEditorWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //Sets a textview to the current length
            mTextView.setText(String.valueOf(s.length()));

            //tells the user which limits they have exceeded if any
            //           if(s.length() > ANONYMOUS_LIMIT) {
            //        	   mNotifyLimits.setText("Over Twitter & Anonymous character limit.");
            //           }
            //           else if(s.length() > TWITTER_LIMIT) {
            //        	   mNotifyLimits.setText("Over Twitter character limit.");
            //           } else mNotifyLimits.setText("");
        }

        public void afterTextChanged(Editable s) {
        }
    };

	/* ----- NESTED INTERFACES ----- */
    //	public interface Callbacks{
    //		public void onSendPressed(String fbMsg);
    //	}
}
