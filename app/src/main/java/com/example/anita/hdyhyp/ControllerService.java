package com.example.anita.hdyhyp;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

/*
Controlls when and how often photos are captures
Knows the storage folder of the files
when picture processor set flag that the picture is taken and processed and the data collector sets a flag, that he collected all data, the service triggers a Notification for the user that he can review his picture and fil the surveys
it is realized as a foreground service so it won't be killed by Android and the participant has feedback, that the Service is still running
*/


class ControllerService extends Service implements Observer {

    private static final String TAG = "ControllerService";

    private boolean picFlag;
    private boolean dataFlag;
    private boolean firstTrySuccessfullyFlag;
    private String storagePath;
    private String userName;
    private BroadcastReceiver screenOnOffReceiver;
    private int lastPicCapturedOn;
    private Timer onOffTimer = null;
    private Thread picturesAreCurrentlyTakenThread;

    public enum CapturingEvent {SCREENON, ORIENTATION, KEYBOARD, APPLICATION};
    public CapturingEvent capturingEvent;


    public ControllerService() {

        picFlag = false;
        dataFlag = false;
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {

        try {
            /**Starting the Shooting after inizialising the user name in order to test if everything works
             */
            Bundle extras = intent.getExtras();
            storagePath = (String) extras.get("storagePath");
            userName = (String) extras.get("userName");
            startCapturePictureService();
            firstTrySuccessfullyFlag = true;

        } catch (Exception e) {
            Log.v(TAG, "CapturePicService could not be started by the Controller.");
            firstTrySuccessfullyFlag = false;
        }

        if (firstTrySuccessfullyFlag){
            //Register Broadcast Receiver for listening if the screen is on/off
            final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            screenOnOffReceiver = new ScreenOnOffReceiver();
            registerReceiver(screenOnOffReceiver, filter);

            ObservableObject.getInstance().addObserver(this);

            //start the controlling function of the class in a new thread
            //new Thread(runnableIntervallTest).start();

            // Notification to start service foreground according to the design guidelines
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


    @Override
    public void onDestroy(){
        if (screenOnOffReceiver != null) {
            unregisterReceiver(screenOnOffReceiver);
            screenOnOffReceiver = null;
        }
    }

        @Override
    public void update(Observable observable, Object data) {
        Log.v(TAG, "Screen is on: " + ObservableObject.getInstance().getIsScreenOn());
        if (picturesAreCurrentlyTakenThread != null && !ObservableObject.getInstance().getIsScreenOn()){
            picturesAreCurrentlyTakenThread.interrupt();
            picturesAreCurrentlyTakenThread = null;
            Log.v(TAG, "Screen is turned off, picture taking thread is canceled.");
        } else if (ObservableObject.getInstance().getIsScreenOn()) {
            capturingEvent = CapturingEvent.SCREENON;
            picturesAreCurrentlyTakenThread = new Thread(runnableShootPicture);
            picturesAreCurrentlyTakenThread.start();
        }
    }

    /*Shoots pictures while the user turns on the screen, 2 seconds after and 8 seconds after*/
    private Runnable runnableShootPicture = new Runnable() {
        @Override
        public void run() {
            while (true) {
            switch (capturingEvent) {
                case SCREENON:
                    startCapturePictureService();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "Thread cannot sleep :(");
                        e.printStackTrace();
                    }
                    startCapturePictureService();
                    try {
                        Thread.sleep(8000);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "Thread cannot sleep yet another time :(");
                        e.printStackTrace();
                    }
                    startCapturePictureService();
                    picturesAreCurrentlyTakenThread.interrupt();
                    picturesAreCurrentlyTakenThread = null;
                    break;

                case ORIENTATION:
                    break;
                case KEYBOARD:
                    break;
                case APPLICATION:
                    break;

                default:
                    Log.d(TAG, "No event was detected while runnableShootPicture was called.");
                    break;
            }
        }}

    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }






    private void startCapturePictureService() {
        Intent capturePicServiceIntent = new Intent(this, CapturePicService.class);
        capturePicServiceIntent.putExtra("storagePath", storagePath);
        capturePicServiceIntent.putExtra("userName", userName);
        getApplicationContext().startService(capturePicServiceIntent);
    }



    /*
    Knows when the user switches the screen on or off and lets the Controller Service know
     */
    public class ScreenOnOffReceiver extends BroadcastReceiver {

        private static final String TAG = "ScreenOnOffReceiver";
        private boolean isScreenOn;

        @Override
        public void onReceive(Context context, Intent intent) {
             if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                Log.v(TAG, "Screen is on now.");
                isScreenOn = true;
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Log.v(TAG, "Screen is off now.");
                isScreenOn = false;
            }

            ObservableObject.getInstance().setIsScreenOn(isScreenOn);
            ObservableObject.getInstance().updateValue(intent);
        }

    }


    /*private class RunnableShootPicture extends AsyncTask<CapturingEvent, Void, Void> {

        @Override
        protected Void doInBackground(CapturingEvent... params) {
            switch (params[0]) {
                case SCREENON:
                startCapturePictureService();
                onOffTimer.schedule(new startCapturePictureServiceTask(), 2000 );
                onOffTimer.schedule(new startCapturePictureServiceTask(), 8000 );
                onOffTimer.cancel();
                onOffTimer = null;
                    break;

                case ORIENTATION:
                    break;
                case KEYBOARD:
                    break;
                case APPLICATION:
                    break;

                default:
                    Log.d(TAG, "No event was detected while runnableShootPicture was called.");
                    break;
            }

            return null;
        }
    }*/

}