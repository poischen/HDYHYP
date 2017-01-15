package com.example.anita.hdyhyp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static com.example.anita.hdyhyp.DataCollectorService.REQUESTID;

public class EventAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = EventAlarmReceiver.class.getSimpleName();

    public EventAlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "Alarm received");
        int requestID = (int) intent.getExtras().get(REQUESTID);
        //ObservableObject.getInstance().setPendingIntentRequestID(requestID);
        ObservableObject.getInstance().updateValue(intent);
    }
}
