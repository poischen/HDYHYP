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

public class AlarmReceiver extends BroadcastReceiver {

    private boolean wasRescheduled = false;
    private long[] startTime = new long[6];
    private int[] rescheduleCounter = new int[] {0, 0, 0, 0, 0, 0};
    private int shiftMillis = 5000;

    private static final String TAG = AlarmReceiver.class.getSimpleName();
    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "Alarm received");
        int currentID = (int) intent.getExtras().get("id");
        //long currentMillis = (long) intent.getExtras().get("time");
        //if (rescheduleCounter[currentID] == 0){
        //    startTime[currentID] = currentMillis;
        //}

        if (!wasRescheduled){
            long time = (long) intent.getExtras().get("time");
                startTime[currentID] = time;

        }
        Log.v(TAG, "start time of current alarm: " + startTime[currentID]);
        Log.v(TAG, "incoming intent id: " + currentID);

        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        if (powerManager.isScreenOn()){
            Log.v(TAG, "screen is on, handle alarm " + currentID);
            //inform controller
            //create survey TODO: von controller übernehmen lassen, wenn er bild auslöst
            NotificationCompat.Builder surveyNotificationBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.logo)
                    .setContentTitle("HDYHYP")
                    .setContentText("A survey is waiting for you");
                Intent resultIntent = new Intent(context, SurveyActivity.class);

                //TaskStackBuilder stackBuilder = TaskStackBuilder.create(AlarmReceiver.this);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addParentStack( SurveyActivity.class);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
            surveyNotificationBuilder.setContentIntent(resultPendingIntent);
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(currentID, surveyNotificationBuilder.build());



            //reset start time
            //if (rescheduleTimerCounter[currentID] > 0){
            if (wasRescheduled){
                Log.v(TAG, "alarm was rescheduled, reset now");
                wasRescheduled = false;
                rescheduleCounter[currentID] = 0;
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, currentID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC, startTime[currentID], pendingIntent);
            }
        }
        else{
            //reschedule: shift alarm 5 minutes
            Log.v(TAG, "reschedule alarm " + currentID);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, currentID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            Log.v(TAG, "rescheduleCounter" + rescheduleCounter[currentID]);
            ++ rescheduleCounter[currentID];
            Log.v(TAG, "rescheduleCounter new" + rescheduleCounter[currentID]);
            long newTime = startTime[currentID] + (rescheduleCounter[currentID] * shiftMillis);
            Log.v(TAG, "new time" + newTime);
            alarmManager.set(AlarmManager.RTC, newTime, pendingIntent );
            wasRescheduled = true;
        }
    }
}
