package com.example.anita.hdyhyp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
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

            //create survey

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(yourActivity.this)
                                .setSmallIcon(R.drawable.app_icon)
                                .setContentTitle("title")
                                .setContentText("text");
                // Creates an explicit intent for an Activity in your app
                Intent resultIntent = new Intent(yourActivity.this, wantToOpenActivity.this);

                // The stack builder object will contain an artificial back stack for the
                // started Activity.
                // This ensures that navigating backward from the Activity leads out of
                // your application to the Home screen.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(yourActivity.this);
                // Adds the back stack for the Intent (but not the Intent itself)
                stackBuilder.addParentStack( wantToOpenActivity.this);
                // Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);
                NotificationManager mNotificationManager =
                        (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                // mId allows you to update the notification later on.
                int mId=001;;
                mNotificationManager.notify(mId, mBuilder.build());



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
