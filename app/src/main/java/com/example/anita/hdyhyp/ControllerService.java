package com.example.anita.hdyhyp;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
//TODO Steuerung, wann das Bild geschossen wird & wie oft

/*
Controlls when and how often photos are captures
Knows the storage folder of the files
when picture processor set flag that the picture is taken and processed and the data collector sets a flag, that he collected all data, the service triggers a Notification for the user that he can review his picture and fil the surveys
 */
public class ControllerService extends Service {

    private static final String TAG = "CaptureControllerService";

    private boolean picFlag;
    private boolean dataFlag;

    public ControllerService() {

        picFlag = false;
        dataFlag = false;
        //return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while(true) {
                // put your socket-code here
            }
        }
    };

    @Override
    public void onCreate() {

        // start new thread and you your work there
        new Thread(runnable).start();

        // Notification to start service foreground after design guidelines
        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("HDYHYP")
                .setContentText("Thank you for participating :)")
                .setSmallIcon(R.drawable.logo)
                .build();

        // this will ensure your service won't be killed by Android
        startForeground(456, notification);
    }


    public void setPicFlag(boolean picFlag) {
        this.picFlag = picFlag;
    }

    public void setDataFlag(boolean dataFlag){
        this.dataFlag = dataFlag;
    }
}