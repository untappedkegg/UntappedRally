package com.untappedkegg.rally.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.data.BaseDbAccessor;
import com.untappedkegg.rally.data.NewDataFetcher;
import com.untappedkegg.rally.event.DbEvent;
import com.untappedkegg.rally.notification.EventLiveNotification;
import com.untappedkegg.rally.schedule.DbSchedule;
import com.untappedkegg.rally.stages.StagesFetcher;

public class EventNotificationReceiver extends BroadcastReceiver {
    public EventNotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        if (AppState.getSettings().getBoolean("setting_notifications", true)) {
            EventLiveNotification.notify(AppState.getApplication(), false);
            if(AppState.getSettings().getBoolean("stage_notifications", false)) {
                BaseDbAccessor.open();
                // TODO: Determine which event
                final String link = DbSchedule.fetchNextEventStart(DbSchedule.SCHED_EVT_SITE);
                final String[] linkPts = link.split("/");
                // TODO: Ensure stage data
                if(!DbEvent.isStageDataPresent(linkPts[5])) {
                    StagesFetcher.getInstance().startAll(new NewDataFetcher.Callbacks() {
                        @Override
                        public void onDataFetchComplete(Throwable throwable, String key) {
                            if(throwable == null) {
                                // Everything came back fine, data should be present
                            }
                        }
                    }, link, linkPts[4]);
                }
                // TODO: Queue notification

                BaseDbAccessor.close();
            }
        }
        // an Intent broadcast.
        // throw new UnsupportedOperationException("Not yet implemented");
    }
}
