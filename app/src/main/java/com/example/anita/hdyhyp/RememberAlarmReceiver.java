package com.example.anita.hdyhyp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import static java.lang.System.currentTimeMillis;


public class RememberAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationCompat.Builder reviewNotificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle("HDYHYP")
                        .setContentText("Please transfer data if you have WIFI")
                        .setOngoing(true);

        reviewNotificationBuilder.setLights(Color.rgb(230, 74, 25), 2500, 3000);
        reviewNotificationBuilder.setVibrate(new long[] { 1000, 1000, 1000 });

        Intent reviewIntent = new Intent(context, PictureReviewActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(PictureReviewActivity.class);
        stackBuilder.addNextIntent(reviewIntent);
        PendingIntent reviewPendingIntent =
                stackBuilder.getPendingIntent(
                        (int) currentTimeMillis(),
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        reviewNotificationBuilder.setContentIntent(reviewPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(41, reviewNotificationBuilder.build());


        Storage storage = new Storage(context);
        storage.setAllRandomWasTakenInCurrentPeriod(false);
    }
}
