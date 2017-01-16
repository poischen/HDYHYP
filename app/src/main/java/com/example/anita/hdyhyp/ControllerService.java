package com.example.anita.hdyhyp;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.example.anita.hdyhyp.DataCollectorService.REQUESTID;


/*
Controlls when and how often photos are captures and holds all necessary information
it is realized as a foreground service so it won't be killed by Android and the participant has feedback, that the Service is still running
*/


public class ControllerService extends Service implements Observer {

    private static final String TAG = ControllerService.class.getSimpleName();
    private final String NASTRING = "n./a.";

    private boolean firstTrySuccessfullyFlag;
    private String storagePath;
    private String userName;

    public static Storage storage;
    public static boolean pictureReviewAndUpload;

    public enum CapturingEvent {INIT, SCREENON, ORIENTATION, APPLICATION, NOTIFICATION, RANDOM, STOP}

    public CapturingEvent capturingEvent;
    private String lastAsNewDetectedApp = "com.example.anita.hdyhyp";
    private String currentForegroundApp;
    private boolean lastDetectedOrientationPortrait = true;
    private Thread appDetectionThread;
    public static String currentNotification;

    public static boolean pictureIsCurrentlyTaken = false;
    private ArrayList<PendingIntent> eventPendingIntentArray = new ArrayList<PendingIntent>();
    private ArrayList<PendingIntent> randomPendingIntentArray = new ArrayList<PendingIntent>();
    private AlarmManager alarmManager;

    private RememberAlarmReceiver rememberAlarmReceiver;
    private EventAlarmReceiver eventAlarmReceiver;
    private RandomAlarmReceiver randomAlarmReceiver;
    private BroadcastReceiver broadcastReceiver;
    protected Calendar alarm[] = new Calendar[6];

    public ControllerService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "ControllerService created.");
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        /**Starting the Shooting after inizialising the user name in order to test if everything works
         */
        try {
            //Bundle extras = intent.getExtras();
            storage = new Storage(getApplicationContext());
            storagePath = storage.getStoragePath();
            userName = storage.getUserName();
            capturingEvent = CapturingEvent.INIT;
            startCapturePictureService(capturingEvent);
            firstTrySuccessfullyFlag = true;

        } catch (Exception e) {
            Log.v(TAG, "CapturePicService could not be started by the Controller.");
            firstTrySuccessfullyFlag = false;
        }

        if (firstTrySuccessfullyFlag) {
            ObservableObject.getInstance().addObserver(this);
            setRandomPictureAndDatatransferAlarms();

            //Register Broadcast Receiver for listening if the screen is on/off & if orientation has changed
            broadcastReceiver = new EventBroadcastReceiver();
            final IntentFilter onOffFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            onOffFilter.addAction(Intent.ACTION_SCREEN_OFF);
            final IntentFilter configurationFilter = new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED);
            registerReceiver(broadcastReceiver, onOffFilter);
            registerReceiver(broadcastReceiver, configurationFilter);

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
            Toast.makeText(this, "There's a problem with the study app. Please tell Anita!", Toast.LENGTH_LONG).show();
            stopSelf();
        }
        return START_STICKY;
    }

    /*

     */
    public static void startDataCollectionService(String command, Context context, String foregroundApp, CapturingEvent capturingEvent, String pictureName, String faceDetectionLeftEye, String faceDetectionRightEye, String faceDetectionMouth) {
        Intent dataCollectionIntent = new Intent(context, DataCollectorService.class);
        dataCollectionIntent.putExtra(DataCollectorService.DCSCOMMAND, command);
        if (command.equals("collect")){
            dataCollectionIntent.putExtra(DataCollectorService.FOREGROUNDAPP, foregroundApp);
            dataCollectionIntent.putExtra(DataCollectorService.CAPTURINGEVENT, capturingEvent);
            dataCollectionIntent.putExtra(DataCollectorService.PICTURENAME, pictureName);
            if (ObservableObject.getInstance().isOrientationPortrait()) {
                dataCollectionIntent.putExtra(DataCollectorService.ORIENTATION, DataCollectorService.PORTAIT);
            } else {
                dataCollectionIntent.putExtra(DataCollectorService.ORIENTATION, DataCollectorService.LANDSCAPE);
            }
            dataCollectionIntent.putExtra(DataCollectorService.FDLEFTEYE, faceDetectionLeftEye);
            dataCollectionIntent.putExtra(DataCollectorService.FDRIGHTEYE, faceDetectionRightEye);
            dataCollectionIntent.putExtra(DataCollectorService.FDMOUTH, faceDetectionMouth);
        }
        context.startService(dataCollectionIntent);
    }

    /*
    sets 6 random daily alarms between 10 am and 22 pm, where pictures should be taken
    sets a daily alarm to remember transfering data
     */
    private void setRandomPictureAndDatatransferAlarms() {

        IntentFilter filter = new IntentFilter("com.example.anita.hdyhyp.RandomAlarmReceiver");
        randomAlarmReceiver = new RandomAlarmReceiver();
        registerReceiver(randomAlarmReceiver, filter);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        //calculate and set random alarms
        //ArrayList<PendingIntent> eventPendingIntentArray = new ArrayList<PendingIntent>();
        int[] alarmHour = new int[6];
        int[] alarmMinutes = new int[6];
        //alarm between 10 am and 22 pm in intervals of 2 hours
        int hourCounter = 10;
        for (int i = 0; i <= 5; i++) {
            alarmHour[i] = hourCounter + ((int) (Math.random() * 2));
            alarmMinutes[i] = (int) (Math.random() * 59) + 1;
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(System.currentTimeMillis());
            c.set(Calendar.HOUR_OF_DAY, alarmHour[i]);
            c.set(Calendar.MINUTE, alarmMinutes[i]);
            c.set(Calendar.SECOND, 0);
            alarm[i] = c;
            Log.v(TAG, "Alarm No. " + i + ": " + alarmHour[i] + ":" + alarmMinutes[i]);
            hourCounter = hourCounter + 2;

            Intent intent = new Intent(this, RandomAlarmReceiver.class);
            intent.putExtra("requestID", i);
            intent.putExtra("time", c.getTimeInMillis());
            intent.setAction("com.example.anita.hdyhyp.RandomAlarmReceiver");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), i, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            randomPendingIntentArray.add(pendingIntent);
            alarmManager.setInexactRepeating(AlarmManager.RTC, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }

        //set data transfer alarm
        IntentFilter rememberFilter = new IntentFilter("com.example.anita.hdyhyp.RememberAlarmReceiver");
        rememberAlarmReceiver = new RememberAlarmReceiver();
        registerReceiver(rememberAlarmReceiver, rememberFilter);

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(Calendar.HOUR_OF_DAY, 22);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);

        Intent intent = new Intent(this, RememberAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 70, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.setInexactRepeating(AlarmManager.RTC, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }


    @Override
    public void onDestroy() {
         //cancel receiver
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
        if (rememberAlarmReceiver != null) {
            unregisterReceiver(rememberAlarmReceiver);
            rememberAlarmReceiver = null;
        }
        if (eventAlarmReceiver != null) {
            unregisterReceiver(eventAlarmReceiver);
            eventAlarmReceiver = null;
        }

        //stop DataCollectorService
        Intent intent = new Intent(getApplicationContext(), DataCollectorService.class);
        stopService(intent);

        //cancel data transfer teminder
        PendingIntent reminderPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 70, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(reminderPendingIntent);

        //cancel alarms of capturing session
        cancelFutureAlarms();

        //cancel random alarms
        //TODO: cancel alarms not necessary? >> reboot: will be deleted, >> onDestroy -> after restart will be set new with flag "FLAG_CANCEL_CURRENT" -> yes because of possibility to delete user name and stop service with that
        int a = randomPendingIntentArray.size();
        if (a != 0){
        for (int i=0; i<a; i++){
            alarmManager.cancel(randomPendingIntentArray.get(i));
        }
        }
        if (randomAlarmReceiver != null) {
            unregisterReceiver(randomAlarmReceiver);
            randomAlarmReceiver = null;
        }

        //stop detecting apps thread
        if (appDetectionThread != null) {
            appDetectionThread.interrupt();
        }

        //stop Controller Service
        stopSelf();
    }


    @Override
    public void update(Observable observable, Object data) {
        String action = data.toString();
        Log.v(TAG, "action: " + action);
        if (action.contains("EventAlarmReceiver")){
            startCapturePictureService(capturingEvent);
            Log.v(TAG, "event has not changed, capture pic again");
            /*remove pending intent from list
            ObservableObject.getInstance().getPendingIntentRequestID();

            int size = eventPendingIntentArray.size();
            if ( size > 0) {
                for (int i = 0; i < size; i++) {
                    if (eventPendingIntentArray.get(i).get)
                }
            }*/

        } else if (action.contains("RandomAlarmReceiver")){
            this.capturingEvent = CapturingEvent.RANDOM;
            Log.v(TAG, "Event detected, capturingEvent set to: " + this.capturingEvent);
            initPictureTakingSession(capturingEvent);
        } else if (action.contains("android.intent.action.SCREEN_OFF")){
            Log.v(TAG, "screen was turned off, cancel future alarms");
            handleScreenOff();
        } else if (action.contains("android.intent.action.SCREEN_ON")){
            startDataCollectionService(DataCollectorService.DCSCOMMANDREGISTER, getApplicationContext(), null, null, null, null, null, null);
            this.capturingEvent = CapturingEvent.SCREENON;
            Log.v(TAG, "Event detected, capturingEvent set to: " + this.capturingEvent);
            initPictureTakingSession(capturingEvent);
        } else if (action.contains("android.intent.action.CONFIGURATION_CHANGED")){
            if (ObservableObject.getInstance().isOrientationPortrait() != lastDetectedOrientationPortrait) {
                lastDetectedOrientationPortrait = ObservableObject.getInstance().isOrientationPortrait();
                this.capturingEvent = CapturingEvent.ORIENTATION;
                Log.v(TAG, "Event detected, capturingEvent set to: " + this.capturingEvent);
                initPictureTakingSession(capturingEvent);
            }
        }

    }

    private void handleScreenOff(){
        startDataCollectionService(DataCollectorService.DCSCOMMANDUNREGISTER, getApplicationContext(), null, null, null, null, null, null);
        this.capturingEvent = CapturingEvent.STOP;
        cancelFutureAlarms();
    }

    private void initPictureTakingSession(CapturingEvent capturingEvent) {
        Log.v(TAG, "initPictureTakingSession() is called");
        //final int tIteration1 = 3500;
        //final int tIteration2 = 12500;
        //final int tIteration3 = 15000;
        final int tIteration1 = 2;
        final int tIteration2 = 15;
        final int tIteration3 = 30;

        //try to take the first picture immediately
        startCapturePictureService(capturingEvent);

        //check and cancel future taking picture alarms
        cancelFutureAlarms();


        //set alarms for taking pictures for the incoming event
        long currentTimeMillis = System.currentTimeMillis();

        /*Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(System.currentTimeMillis());
        Calendar calendar = (Calendar) currentTime.clone();*/

        int requestId = 80;

        Log.v(TAG, "event in initPictureTakingSession() before setting alarms: "+ capturingEvent.toString());
        //set one alarm for the events SCREENON, NOTIFICATION, APPLICATION & ORIENTATION
        if (capturingEvent.equals(CapturingEvent.SCREENON) || capturingEvent.equals(CapturingEvent.NOTIFICATION) ||
                capturingEvent.equals(CapturingEvent.APPLICATION) || capturingEvent.equals(CapturingEvent.ORIENTATION)){
            Log.v(TAG, "set alarm.");
            setEventAlarm(tIteration1, requestId, currentTimeMillis);
            requestId++;

            //set two more alarms for the event APPLICATION
            if (capturingEvent.equals(CapturingEvent.APPLICATION)){
                setEventAlarm(tIteration2, requestId, currentTimeMillis);
                requestId++;
                setEventAlarm(tIteration3, requestId, currentTimeMillis);
                requestId++;
            }
            //calendar = null;
        }
    }


    private void setEventAlarm(int iteration, int requestId, long currentTimeMillis){

        //set alarms for taking pictures for the incoming event
        IntentFilter filter = new IntentFilter("com.example.anita.hdyhyp.EventAlarmReceiver");
        eventAlarmReceiver = new EventAlarmReceiver();
        registerReceiver(eventAlarmReceiver, filter);

        Intent intent = new Intent(this, EventAlarmReceiver.class);
        intent.putExtra(REQUESTID, requestId);
        intent.setAction("com.example.anita.hdyhyp.EventAlarmReceiver");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), requestId, intent, PendingIntent.FLAG_ONE_SHOT);
        eventPendingIntentArray.add(pendingIntent);

        //new Time
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(currentTimeMillis);
        Calendar calendar = (Calendar) currentTime.clone();
        calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + iteration);
        Log.v(TAG, "currentTimeMillis" + currentTimeMillis);
        Log.v(TAG, "currentSysTime " + System.currentTimeMillis());
        Log.v(TAG, "alarmtime " + calendar.getTimeInMillis());

        //alarmManager.setExact(AlarmManager.RTC, (currentTimeMillis + (iteration)), pendingIntent);
        alarmManager.setExact(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
        Log.v(TAG, "alarm set in " + iteration + " milliseconds.");
    }

    private void cancelFutureAlarms(){
        int size = eventPendingIntentArray.size();
        if ( size > 0){
            for (int i=0; i<size; i++){
                try {
                    alarmManager.cancel(eventPendingIntentArray.get(i));
                } catch (Exception e){
                    Log.d(TAG, "Alarm was not found");
                }
            }
            eventPendingIntentArray.clear();
        }
    }

    private void startCapturePictureService(CapturingEvent capturingEvent) {
        //start taking the picture, if there is not already being taken one. the CapurePicService will run the DataCollection when it
        // is finnished taking the pic
        if (!pictureIsCurrentlyTaken){
            Intent capturePicServiceIntent = new Intent(this, CapturePicService.class);
            capturePicServiceIntent.putExtra("storagePath", storagePath);
            capturePicServiceIntent.putExtra("userName", userName);
            capturePicServiceIntent.putExtra("foregroundApp", currentForegroundApp);
            capturePicServiceIntent.putExtra(DataCollectorService.CAPTURINGEVENT, capturingEvent);
            getApplicationContext().startService(capturePicServiceIntent);
            Log.v(TAG, "CapturePicService will be started now");
        }
        //collect and write data to emphasize that a picture of the session is missing
        else {
            Log.v(TAG, "CapturePicService will not be started, because another picture is currently taken");
            startDataCollectionService("collect", getApplicationContext(), currentForegroundApp, capturingEvent, "no Picture was taken", NASTRING, NASTRING, NASTRING);
        }
    }



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //---detecting foreground-----------------------------------------------------------------------------------------------------
    /**
     * runnable for detecting changes in the forefround apps
     */
    private Runnable runnableAppDetector = new Runnable() {
        @Override
        public void run() {
            while (true) {
                detectApps();
            }
        }

    };

    /**
     * knows the current running apps and detects, if the foreground apps changed
     */
    private void detectApps() {
        //get the latest package name from the usage stats
        String currentAppName = null;
                    UsageStatsManager usageStatsManager = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
                    long time = System.currentTimeMillis();
                    List<UsageStats> apps = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);

            if (apps != null && apps.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                for (UsageStats usageStats : apps) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentAppName = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }

            }

        //TODO: Push seperat speichern und checken ob neu (nicht mehrfach selbe push hinereinander)
        //check if the usagestats is a foregeround application or a status bar notification
        try { if (lastAsNewDetectedApp != null && !lastAsNewDetectedApp.equals(currentAppName)) {
            currentForegroundApp = currentAppName;
            if (currentAppName != null) {
                if (!currentAppName.contains("com.android.") && !currentAppName.contains("com.google.android.googlequicksearchbox")
                        && !currentAppName.equals("com.example.anita.hdyhyp")){
                    if (!pictureReviewAndUpload){
                    Log.v(TAG, "pictureReviewAndUpload: " + pictureReviewAndUpload);
                    Log.v(TAG, "new app detected on foreground: " + currentAppName);
                    Log.v(TAG, "current notification name " + currentNotification);
                    lastAsNewDetectedApp = currentAppName;

                    if (!(currentAppName == null) && currentNotification.equals(currentAppName)){
                        capturingEvent = CapturingEvent.NOTIFICATION;
                        currentNotification = "";
                        Log.v(TAG, "new on foreground detected app was a notification " + currentNotification);

                    } else {
                        capturingEvent = CapturingEvent.APPLICATION;
                        currentNotification = "";
                        Log.v(TAG, "new on foreground detected app was an application " + currentAppName);

                    }
                    initPictureTakingSession(capturingEvent);
                }
                }
            }
        } } catch (Exception e){
            Log.d(TAG, "detecting app failed: " + e.toString());
        }

    }

}