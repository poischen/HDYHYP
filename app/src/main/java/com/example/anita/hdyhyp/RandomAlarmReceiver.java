package com.example.anita.hdyhyp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.POWER_SERVICE;

public class RandomAlarmReceiver extends BroadcastReceiver {

    private boolean[] wasRescheduled = new boolean[6];
    private long[] startTime = new long[6];
    private int[] rescheduleCounter = new int[] {0, 0, 0, 0, 0, 0};
    private int shiftMillisScreenOff = 20000;
    private int shiftMillisPicIsCurrentlyTaken = 5000;

    private static final String TAG = RandomAlarmReceiver.class.getSimpleName();
    public RandomAlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int requestID = (int) intent.getExtras().get("requestID");

        Log.v(TAG, "random Alarm received: " + requestID);

        Storage storage = new Storage(context);
        boolean wasAlreadyTaken = storage.getRandomWasTakenInCurrentPeriod(requestID);
        if (!wasAlreadyTaken){
            ObservableObject.getInstance().setReminderPeriod(requestID);
            ObservableObject.getInstance().updateValue(intent);
        }

        // not necessary since new algorithm
        /*int requestID = (int) intent.getExtras().get("requestID");

        if (startTime[requestID] == 0L){
            long time = (long) intent.getExtras().get("time");
                startTime[requestID] = time;
        }

        //check if alarm is in current time intervall (necessary after restart/reboot)
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        long currentHour = c.get(Calendar.HOUR_OF_DAY);
        c.setTimeInMillis(startTime[requestID]);
        long alarmHour = c.get(Calendar.HOUR_OF_DAY);
        long alarmMinute = c.get(Calendar.MINUTE);
        Log.v(TAG, "incoming intent id: " + requestID + ", time: " + alarmHour + ":" + alarmMinute);
        if (((currentHour - alarmHour) >= 2)){
        Log.v(TAG, "incoming alarm not necessary to handle");
        } else {
            Log.v(TAG, "incoming alarm necessary to handle");
            PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
            int rescheduleTime;
            if (powerManager.isScreenOn()){
                Log.v(TAG, "screen is on, handle alarm " + requestID);
                ObservableObject.getInstance().updateValue(intent);
                //reset start time
                //if (rescheduleTimerCounter[currentID] > 0){
                if (wasRescheduled[requestID]){
                    Log.v(TAG, "alarm has been rescheduled, reset now");
                    wasRescheduled[requestID] = false;
                    rescheduleCounter[requestID] = 0;
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC, startTime[requestID], pendingIntent);
                    Log.v(TAG, "alarm reset to starttime.");
                }
            }
            else{
                if (ControllerService.pictureIsCurrentlyTaken){
                    rescheduleTime = shiftMillisPicIsCurrentlyTaken;
                } else {
                    rescheduleTime = shiftMillisScreenOff;
                }

                //reschedule: shift alarm for 5 minutes if the screen was off, or for 2 seconds, if a picture is currently taken due to an event
                Log.v(TAG, "screen off, reschedule alarm " + requestID);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                Log.v(TAG, "rescheduleCounter" + rescheduleCounter[requestID]);
                ++ rescheduleCounter[requestID];
                Log.v(TAG, "rescheduleCounter new" + rescheduleCounter[requestID]);
                long newTime = startTime[requestID] + (rescheduleCounter[requestID] * rescheduleTime);
                Log.v(TAG, "new time" + newTime);
                alarmManager.set(AlarmManager.RTC, newTime, pendingIntent);
                wasRescheduled[requestID] = true;
                rescheduleTime = 0;
            }
        }*/

    }
}
