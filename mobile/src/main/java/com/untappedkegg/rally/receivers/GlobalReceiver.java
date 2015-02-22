package com.untappedkegg.rally.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.data.BaseDbAccessor;
import com.untappedkegg.rally.schedule.DbSchedule;


/**
 * Provides a unified location to catch system-wide intents.
 *
 * @author alexg
 */
public class GlobalReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(getClass().getSimpleName(), "Handling broadcast intent: " + intent.getAction());
        BaseDbAccessor.open();
        if (DbSchedule.isDataPresent()) {
            AppState.setNextNotification();
        }
        BaseDbAccessor.close();
    }

    /**
     * Provides a class that sends onRecieve intents to an interface (usually a
     * fragment/activity) so it the intent can interact with the other code in
     * the interface.
     *
     * @author alexg
     */
    public static class ReceiverWrapper extends BroadcastReceiver {
        private final IBroadcastReceiver wrappedReceiver;

        public ReceiverWrapper(IBroadcastReceiver receiver) {
            wrappedReceiver = receiver;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            wrappedReceiver.onReceive(context, intent);
        }

    }

    /**
     * Interface for use with {@link ReceiverWrapper}
     *
     * @author alexg
     */
    public interface IBroadcastReceiver {
        public void onReceive(Context context, Intent intent);
    }
}
