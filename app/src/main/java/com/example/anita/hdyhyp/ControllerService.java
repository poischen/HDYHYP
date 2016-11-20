package com.example.anita.hdyhyp;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.example.anita.hdyhyp.ControllerService.CapturingEvent.NOTHING;

/*
Controlls when and how often photos are captures and holds all necessary inforamtion
it is realized as a foreground service so it won't be killed by Android and the participant has feedback, that the Service is still running
*/


public class ControllerService extends Service implements Observer {

    private static final String TAG = ControllerService.class.getSimpleName();

    private boolean firstTrySuccessfullyFlag;
    private String storagePath;
    private String userName;
    private BroadcastReceiver broadcastReceiver;
    private Thread picturesAreCurrentlyTakenThread;
   // private CapturePicService currentCapturePicService;

    public static Storage storage;

    public enum CapturingEvent {NOTHING, SCREENON, ORIENTATION, KEYBOARD, APPLICATION, PUSHNOTIFICATION, CAM};
    public CapturingEvent capturingEvent;
    private String LastAsNewDetectedApp = "com.example.anita.hdyhyp";
    private String currentForegroundApp;
    private boolean lastDetectedOrientaionPortrait = true;
    private Thread appDetectionThread;
    private boolean dataFlag;

    List<Intent> intentList = new ArrayList<>(); //TODO: Wann werden die Intents aus der Liste gelöscht?


    public ControllerService() {

    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {

        try {
            /**Starting the Shooting after inizialising the user name in order to test if everything works
             */
            Bundle extras = intent.getExtras();
            //storagePath = (String) extras.get("storagePath");
            //userName = (String) extras.get("userName");
            storage = new Storage(getApplicationContext());
            storagePath = storage.getStoragePath();
            userName = storage.getUserName();
            capturingEvent = NOTHING;
            startCapturePictureService();
            firstTrySuccessfullyFlag = true;

        } catch (Exception e) {
            Log.v(TAG, "CapturePicService could not be started by the Controller.");
            firstTrySuccessfullyFlag = false;
        }

        if (firstTrySuccessfullyFlag){

            //Register Broadcast Receiver for listening if the screen is on/off & if orientation has changed
            broadcastReceiver = new MyBroadcastReceiver();
            final IntentFilter onOffFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            onOffFilter.addAction(Intent.ACTION_SCREEN_OFF);
            final IntentFilter configurationFilter = new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED);
            final IntentFilter camButtonFilter = new IntentFilter(Intent.ACTION_CAMERA_BUTTON);
            registerReceiver(broadcastReceiver, onOffFilter);
            registerReceiver(broadcastReceiver, configurationFilter);
            registerReceiver(broadcastReceiver, camButtonFilter);
            ObservableObject.getInstance().addObserver(this);

            // Notification about starting the controller service foreground according to the design guidelines
            Notification notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle("HDYHYP")
                    .setContentText("Thank you for participating :)")
                    .setSmallIcon(R.drawable.logo)
                    .build();
            startForeground(456, notification);

            //Start detection of running apps
            appDetectionThread = new Thread(runnableAppDetector);
            appDetectionThread.start();
        } else {
            stopSelf();

        }
        return START_STICKY;
    }


    @Override
    public void onDestroy(){
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
        if (appDetectionThread != null){
            appDetectionThread.interrupt();
        }
    }

        @Override
    public void update(Observable observable, Object data) {
            String action =  data.toString();
            if (action.contains("android.intent.action.SCREEN_OFF")){
                stopIfPicturesAreCurrentlyTaken();
            } else{
                if (action.contains("android.intent.action.SCREEN_ON")){
                    stopIfPicturesAreCurrentlyTaken();
                    capturingEvent = CapturingEvent.SCREENON;
                }
                else if (action.contains("android.intent.action.CONFIGURATION_CHANGED")){
                    if (ObservableObject.getInstance().isOrientationPortrait() != lastDetectedOrientaionPortrait){
                        lastDetectedOrientaionPortrait = ObservableObject.getInstance().isOrientationPortrait();
                        stopIfPicturesAreCurrentlyTaken();
                        capturingEvent = CapturingEvent.ORIENTATION;
                    }
                }
                else if (action.contains("android.intent.action.ACTION_CAMERA_BUTTON")){

                    stopIfPicturesAreCurrentlyTaken();
                    capturingEvent = CapturingEvent.CAM;
                }
                picturesAreCurrentlyTakenThread = new Thread(runnableShootPicture);
                picturesAreCurrentlyTakenThread.start();
            }
    }


    private void startCapturePictureService() {
        Intent capturePicServiceIntent = new Intent(this, CapturePicService.class);
        capturePicServiceIntent.putExtra("storagePath", storagePath);
        capturePicServiceIntent.putExtra("userName", userName);
        capturePicServiceIntent.putExtra("foregroundApp", currentForegroundApp);
        capturePicServiceIntent.putExtra("capturingEvent", capturingEvent);
        intentList.add(capturePicServiceIntent);
        getApplicationContext().startService(capturePicServiceIntent);
    }

    private void stopIfPicturesAreCurrentlyTaken(){
        if (picturesAreCurrentlyTakenThread != null){
            if (picturesAreCurrentlyTakenThread.isAlive()){
                picturesAreCurrentlyTakenThread.interrupt();
            }
            picturesAreCurrentlyTakenThread = null;
            //TODO: This can cause errors - try not to stop the services with the intensts and let them do their pictures but give the cps the captureevent for storring
            /*if (!intentList.isEmpty()){
                for (Intent i : intentList) {
                    stopService(i);
                    intentList.remove(i);
                }
            }*/
            Log.v(TAG, "Picture taking interrupted.");
        }
    }

    private Runnable runnableShootPicture = new Runnable() {
        @Override
        public void run() {
            switch (capturingEvent) {
                case SCREENON:
                    Log.v(TAG, "SCREENON Event - Take 1. photo.");
                    startCapturePictureService();
                    try {
                        //TODO: Reserach time, people need to unlock their phones
                        Thread.sleep(3000);
                        Log.v(TAG, "SCREENON Event - Take 2. photo.");
                        startCapturePictureService();
                    } catch (InterruptedException e) {
                        Log.d(TAG, "SCREENON Event - Thread cannot sleep :(");
                        e.printStackTrace();
                    }
                    try {
                        //TODO: Research time, people need to get to their target on the device
                        Thread.sleep(8000);
                        Log.v(TAG, "SCREENON Event - Take 3. photo.");
                        startCapturePictureService();
                    } catch (InterruptedException e) {
                        Log.d(TAG, "SCREENON Event - Sad thread cannot sleep yet another time :(");
                        e.printStackTrace();
                    }
                    break;

                case ORIENTATION:
                    Log.v(TAG, "ORIENTATION Event - Take 1. photo.");
                    startCapturePictureService();
                    try {
                        Thread.sleep(3000);
                        Log.v(TAG, "ORIENTATION Event - Take 2. photo.");
                        startCapturePictureService();
                    } catch (InterruptedException e) {
                        Log.d(TAG, "ORIENTATION Event - Thread cannot sleep :(");
                        e.printStackTrace();
                    }
                    break;
                case KEYBOARD:
                    break;
                case APPLICATION:
                    Log.v(TAG, "APPLICATION Event - Take 1. photo.");
                    startCapturePictureService();
                    try {
                        Thread.sleep(3000);
                        Log.v(TAG, "APPLICATION Event - Take 2. photo.");
                        startCapturePictureService();
                    } catch (InterruptedException e) {
                        Log.d(TAG, "APPLICATION Event - Thread cannot sleep :(");
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(25000);
                        Log.v(TAG, "APPLICATION Event - Take 3. photo.");
                        startCapturePictureService();
                    } catch (InterruptedException e) {
                        Log.d(TAG, "APPLICATION Event - Sad thread cannot sleep yet another time :(");
                        e.printStackTrace();
                    }
                    break;
                case PUSHNOTIFICATION:
                    Log.v(TAG, "NOTIFICATION Event - Take 1. photo.");
                    startCapturePictureService();
                    break;
                case CAM:
                    Log.v(TAG, "REARCAM Event - Take 1. photo.");
                    startCapturePictureService();
                    break;
                default:
                    Log.d(TAG, "No event was detected while runnableShootPicture was called.");
                    break;
            }

            capturingEvent = NOTHING;
            if (picturesAreCurrentlyTakenThread != null && picturesAreCurrentlyTakenThread.isAlive()){
                picturesAreCurrentlyTakenThread.interrupt();
                picturesAreCurrentlyTakenThread = null;
            }
            if (!intentList.isEmpty()){
                intentList.clear();
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

/*//TESTTEST
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);

        // Checks whether a hardware keyboard is available
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            Toast.makeText(this, "keyboard visible", Toast.LENGTH_SHORT).show();
        } else if (newConfig.hardKeyboardHidden ==
                Configuration.HARDKEYBOARDHIDDEN_YES) {
            Toast.makeText(this, "keyboard hidden", Toast.LENGTH_SHORT).show();
        }
    }*/


    private Runnable runnableAppDetector = new Runnable() {
        @Override
        public void run() {
            while (true) {
                detectApps();
            }}

    };

    //know the current running apps and detects, if the foreground apps changed
    private void detectApps() {
        String currentApp = null;
        if (Build.VERSION.SDK_INT >= 21) {
            UsageStatsManager usageStatsManager = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> apps = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
            if (apps != null && apps.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                for (UsageStats usageStats : apps) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        }
        else {
            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            currentApp=(manager.getRunningTasks(1).get(0)).topActivity.getPackageName();
            //(manager.getRunningTasks(1).get(0)).describeContents();
        }
        if (LastAsNewDetectedApp !=null && !LastAsNewDetectedApp.equals(currentApp)){
            currentForegroundApp = currentApp;
            if (!currentApp.contains("com.android.") && !currentApp.equals("com.example.anita.hdyhyp")){
                Log.v(TAG, "Current SDK: " + Build.VERSION.SDK_INT + ", new app detected on foreground: " + currentApp);
                LastAsNewDetectedApp = currentApp;
                stopIfPicturesAreCurrentlyTaken();
                capturingEvent = CapturingEvent.APPLICATION;
                picturesAreCurrentlyTakenThread = new Thread(runnableShootPicture);
                picturesAreCurrentlyTakenThread.start();
                //com.google.android.googlequicksearchbox
                //TODO: erfassen, ob es sich um eine push notification handelt
            }
        }

    }

}