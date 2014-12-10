package com.keggemeyer.rallyamerica.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.keggemeyer.rallyamerica.AppState;
import com.keggemeyer.rallyamerica.R;
import com.keggemeyer.rallyamerica.notification.EventLiveNotification;

public class EventNotificationReceiver extends BroadcastReceiver {
    public EventNotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        if (AppState.getSettings().getBoolean(AppState.getApplication().getString(R.string.settings_show_notifications), true))
        EventLiveNotification.notify(AppState.getApplication(), 0);
        // an Intent broadcast.
        // throw new UnsupportedOperationException("Not yet implemented");
    }
}
