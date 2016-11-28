package com.example.anita.hdyhyp;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.util.ArrayList;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.POWER_SERVICE;
import static java.lang.System.currentTimeMillis;

public class AlarmReceiver extends BroadcastReceiver {

    private boolean[] wasRescheduled = new boolean[6];
    private long[] startTime = new long[6];
    private int[] rescheduleCounter = new int[] {0, 0, 0, 0, 0, 0};
    private int shiftMillis = 20000;

    private static final String TAG = AlarmReceiver.class.getSimpleName();
    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "Alarm received");
        int requestID = (int) intent.getExtras().get("requestID");

        if (startTime[requestID] == 0L){
            long time = (long) intent.getExtras().get("time");
                startTime[requestID] = time;
        }

        Log.v(TAG, "start time of current alarm: " + startTime[requestID]);
        Log.v(TAG, "incoming intent id: " + requestID);

        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        if (powerManager.isScreenOn()){
            Log.v(TAG, "screen is on, handle alarm " + requestID);
            ObservableObject.getInstance().updateValue(intent);
            //reset start time
            //if (rescheduleTimerCounter[currentID] > 0){
            if (wasRescheduled[requestID]){
                Log.v(TAG, "alarm was rescheduled, reset now");
                wasRescheduled[requestID] = false;
                rescheduleCounter[requestID] = 0;
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC, startTime[requestID], pendingIntent);
                Log.v(TAG, "alarm resetet to starttime.");
            }
        }
        else{
            //reschedule: shift alarm 5 minutes
            Log.v(TAG, "reschedule alarm " + requestID);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            Log.v(TAG, "rescheduleCounter" + rescheduleCounter[requestID]);
            ++ rescheduleCounter[requestID];
            Log.v(TAG, "rescheduleCounter new" + rescheduleCounter[requestID]);
            long newTime = startTime[requestID] + (rescheduleCounter[requestID] * shiftMillis);
            Log.v(TAG, "new time" + newTime);
            alarmManager.set(AlarmManager.RTC, newTime, pendingIntent );
            wasRescheduled[requestID] = true;

        }
    }
}
