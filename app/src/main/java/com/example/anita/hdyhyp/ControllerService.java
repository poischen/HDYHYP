package com.example.anita.hdyhyp;

import android.app.IntentService;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
//TODO Steuerung, wann das Bild geschossen wird & wie oft

/*
Controlls when and how often photos are captures
Knows the storage folder of the files
when picture processor set flag that the picture is taken and processed and the data collector sets a flag, that he collected all data, the service triggers a Notification for the user that he can review his picture and fil the surveys
it is realized as a foreground service so it won't be killed by Android and the participant has feedback, that the Service is still running
 */

public class ControllerService extends Service {

    private static final String TAG = "ControllerService";

    private boolean picFlag;
    private boolean dataFlag;
    private boolean firstTrySuccessfullyFlag;
    private String storagePath;
    private String userName;

    public ControllerService() {

        picFlag = false;
        dataFlag = false;
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
                // put your socket-code here  sidlfgnskgfhsdiungnhsdidldgf was hier passiert
            }
        }
    };


    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {

        try {
            /**Starting the Shooting after inizialising the user name in order to test if everything works
             * TODO: in Controller auslagern, testen ob es passt, sonst Controller Service wieder schließen
             */
            Intent capturePicServiceIntent = new Intent(this, CapturePicService.class);
            Bundle extras = intent.getExtras();
            storagePath = (String) extras.get("storagePath");
            userName = (String) extras.get("userName");
            capturePicServiceIntent.putExtra("storagePath", storagePath);
            capturePicServiceIntent.putExtra("userName", userName);
            getApplicationContext().startService(capturePicServiceIntent);
            firstTrySuccessfullyFlag = true;

        } catch (Exception e) {
            Log.v(TAG, "CapturePicService could not be started by the Controller.");
            firstTrySuccessfullyFlag = false;
        }

        if (firstTrySuccessfullyFlag){
            // start new thread and you your work there
            new Thread(runnable).start();

            // Notification to start service foreground after design guidelines
            Notification notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle("HDYHYP")
                    .setContentText("Thank you for participating :)")
                    .setSmallIcon(R.drawable.logo)
                    .build();
            startForeground(456, notification);
        } else {
            onDestroy();
        }
        return START_STICKY;
    }


    public void setPicFlag(boolean picFlag) {
        this.picFlag = picFlag;
    }

    public void setDataFlag(boolean dataFlag){
        this.dataFlag = dataFlag;
    }
}