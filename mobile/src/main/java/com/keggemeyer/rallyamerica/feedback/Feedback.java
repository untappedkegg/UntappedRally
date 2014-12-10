package com.keggemeyer.rallyamerica.feedback;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.keggemeyer.rallyamerica.R;
import com.keggemeyer.rallyamerica.util.CommonIntents;

public class Feedback extends Activity implements OnClickListener {

    //private static final String LOG_TAG = Feedback.class.getSimpleName();

    /* ----- VARIABLES ----- */
    private String mFbMsg;
    boolean isSuccessful = true;
    private TextView mTextView;
    private EditText mEditText;
    //	private TextView mNotifyLimits;
    boolean successful = false;
    private String SUBJECT;

    /* ----- LIFECYCLE METHODS -----*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback);
        //		AppState.hideSystemUI(this);

        mEditText = (EditText) findViewById(R.id.fb_text);
        mTextView = (TextView) findViewById(R.id.TextCounter);
        //		mNotifyLimits = (TextView) findViewById(R.id.FeedbackLimit);
        mEditText.addTextChangedListener(mTextEditorWatcher);

        Button send = (Button) findViewById(R.id.fb_send);
        send.setOnClickListener(this);

        SUBJECT = "Feedback: " + getString(R.string.app_name);

        getActionBar().setDisplayShowHomeEnabled(true);
        //		getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
//                NavUtils.navigateUpFromSameTask(this);
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            //	    	AppState.hideSystemUI(this);
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
        }

        public void afterTextChanged(Editable s) {
            //Do Nothing
        }
    };

    @Override
    public void onClick(View v) {
        //		switch(v.getId()){
        //		case R.id.fb_send_twitter:
        //			if (mFbMsg.length() > TWITTER_LIMIT) {
        //				mMsg = "Twitter posts must be " + TWITTER_LIMIT + " characters or less.";
        //				Toast.makeText(this, mMsg, Toast.LENGTH_LONG).show();
        //			}
        //			else{
        //				getSupportFragmentManager().popBackStack();
        //
        //				Intent twitter = new Intent(android.content.Intent.ACTION_SEND);
        //				twitter.setType("text/plain");
        //				twitter.putExtra(android.content.Intent.EXTRA_TEXT, "@MUdoit " + mFbMsg);
        //				PackageManager pm = getPackageManager();
        //				List<ResolveInfo> activityList = pm.queryIntentActivities(twitter, 0);
        //				for (final ResolveInfo app : activityList) {
        //				    if ("com.twitter.android.PostActivity".equals(app.activityInfo.name)) {
        //				        final ActivityInfo activity = app.activityInfo;
        //				        final ComponentName name = new ComponentName(activity.applicationInfo.packageName, activity.name);
        //				        twitter.addCategory(Intent.CATEGORY_LAUNCHER);
        //				        twitter.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        //				        twitter.setComponent(name);
        //				        startActivity(twitter);
        //				        break;
        //				     }
        //				  }
        //			}
        //			break;
        String version;
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(((Object) this).getClass().getSimpleName(), "Could not find our package. Initiate FUBAR sequence.");
            version = "";
        }
//        mFbMsg = getString(R.string.about_version, version + "\n", Build.VERSION.SDK_INT + "\n", Build.VERSION.RELEASE + '\n', Build.PRODUCT, Build.MODEL);
        mFbMsg = String.format("App Version: %s\nAndroid: %s : %s\nDevice: %s \nPlease leave the above lines for debugging purposes. Thank you!\n\n", version, Build.VERSION.SDK_INT, Build.VERSION.RELEASE, /*Build.FINGERPRINT,*/ Build.MODEL);
        mFbMsg += mEditText.getText().toString();

        if (mFbMsg.length() < 1) {
            Toast.makeText(this, "Please include some text for feedback.", Toast.LENGTH_SHORT).show();
        } else {
            CommonIntents.sendEmail(this, "TeamKyleRacing@gmail.com", SUBJECT, mFbMsg);
            //		Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","TeamKyleRacing@gmail.com", null));
            //		emailIntent.putExtra(Intent.EXTRA_SUBJECT, SUBJECT);
            //		emailIntent.putExtra(Intent.EXTRA_TEXT, mFbMsg);
            //		startActivityForResult(Intent.createChooser(emailIntent, "Send email..."), 0);
        }
    }

}
