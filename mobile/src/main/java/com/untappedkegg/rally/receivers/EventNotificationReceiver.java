package com.untappedkegg.rally.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.notification.EventLiveNotification;

public class EventNotificationReceiver extends BroadcastReceiver {
    public EventNotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        if (AppState.getSettings().getBoolean("setting_notifications", true))
        EventLiveNotification.notify(AppState.getApplication(), 0);
        // an Intent broadcast.
        // throw new UnsupportedOperationException("Not yet implemented");
    }
}
