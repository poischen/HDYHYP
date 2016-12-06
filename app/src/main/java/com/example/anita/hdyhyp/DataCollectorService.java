package com.example.anita.hdyhyp;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


import android.location.Address;
import android.location.Geocoder;
//import com.google.android.gms.location.*;


import android.location.LocationListener;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;

import static com.example.anita.hdyhyp.ControllerService.CapturingEvent.NOTHING;
import static com.example.anita.hdyhyp.ControllerService.CapturingEvent.RANDOM;
import static java.lang.System.currentTimeMillis;

/*
*@class collects all data and write them into database
 */

public class DataCollectorService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, SensorEventListener {

    private static final String TAG = DataCollectorService.class.getSimpleName();

    private SQLiteDatabase database;
    private Storage storage = ControllerService.storage;
    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener;

    private LocationManager locationManager;
    private LocationListener locationListener = null;
    private Location latestLocation;
    private Boolean googleApiClientConnectionFailed;

    String photoName = "n./a."; //CapturePictureService
    String foregroundApp = "n./a."; //ControllerService
    String proximity = "n./a."; //DataColletorService - SensorListener
    int locationLatitude = -1; //DataColletorService - locationManager
    int locationLongitude = -1; //DataColletorService - locationManager
    String locationRoad = "n./a."; //DataColletorService - locationManager
    String locationPLZ = "n./a."; //DataColletorService - locationManager
    String isKeyboardOpen = "n./.a"; //todo
    String accelerometerX = "n./a."; //DataColletorService - SensorListener
    String accelerometerY = "n./a."; //DataColletorService - SensorListener
    String accelerometerZ = "n./a."; //DataColletorService - SensorListener
    String gyroscopeX = "n./a."; //DataColletorService - Sensor
    String gyroscopeY = "n./a."; //DataColletorService - Sensor
    String gyroscopeZ = "n./a."; //DataColletorService - Sensor
    String orientation = "n./.a"; //from EventBroadcastReceiver
    String ambientLight = "n./.a"; //DataColletorService - SensorListener
    int screenBrightness = -1;  //DataColletorService - read directly
    String batteryStatus = "n./a."; //DataColletorService - read directly
    int batteryLevel = -1; //DataColletorService - read directly
    String faceDetectionLeftEye = "n./.a"; //CapturePictureService
    String faceDetectionRightEye = "n./.a"; //CapturePictureService
    String faceDetectionMouth = "n./.a"; //CapturePictureService
    String capturingEvent = "NOTHING"; //ControllerService

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    public DataCollectorService() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onCreate() {
        Log.v(TAG, "starting data collection");

        //register necessary listener---------------------------------------------------------------
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_FASTEST);

        //------------------------------------------------------------------------------------------
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(1);
        mLocationRequest.setFastestInterval(1);

        buildGoogleApiClient();

        Log.v(TAG, "listener registered.");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Log.v(TAG, "intent " + intent);
            Log.v(TAG, "intent extras " + intent.getExtras());
            capturingEvent = (String) intent.getExtras().get("capturingEvent");
        } catch (Exception e){
            Log.v(TAG, "onStartCommand intent null");
        }

        ContentValues cv = new ContentValues();
        if (capturingEvent.equals("NOTHING")) {
            //read general sensor information and store to db---------------------------------------
            photoName = (String) intent.getExtras().get("photoName");
            cv.put(Storage.COLUMN_PHOTO, photoName);
            Log.v(TAG, "photoName" + photoName);

            cv.put(Storage.COLUMN_CAPTUREEVENT, capturingEvent);
            Log.v(TAG, "captureEvent: " + capturingEvent);

            cv.put(Storage.COLUMN_FOREGROUNDAPP, foregroundApp);
            Log.v(TAG, "foregroundApp: " + foregroundApp);

            cv.put(Storage.COLUMN_LOCATIONLATITUDE, locationLatitude);
            cv.put(Storage.COLUMN_LOCATIONLONGITUDE, locationLongitude);
            cv.put(Storage.COLUMN_LOCATIONROAD, locationRoad);
            cv.put(Storage.COLUMN_LOCATIONPOSTALCODE, locationPLZ);
            Log.v(TAG, "location: latitude: " + locationLatitude + ", longitude: " + locationLongitude + ", road: " + locationRoad + ", postalcode: " + locationPLZ);

            String accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).toString();
            cv.put(Storage.COLUMN_ACCELEROMETERX, accelerometerSensor);
            cv.put(Storage.COLUMN_ACCELEROMETERY, accelerometerY);
            cv.put(Storage.COLUMN_ACCELEROMETERZ, accelerometerZ);
            Log.v(TAG, "accelerometer Sensor:" + accelerometerSensor);

            String gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE).toString();
            cv.put(Storage.COLUMN_GYROSCOPEX, gyroscopeSensor);
            cv.put(Storage.COLUMN_GYROSCOPEY, gyroscopeY);
            cv.put(Storage.COLUMN_GYROSCOPEZ, gyroscopeZ);
            Log.v(TAG, "gyroscope Sensor:" + gyroscopeSensor);

            String lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT).toString();
            cv.put(Storage.COLUMN_LIGHT, lightSensor);
            Log.v(TAG, "light Sensor:" + lightSensor);

            cv.put(Storage.COLUMN_BRIGHTNESS, screenBrightness);
            Log.v(TAG, "foregroundApp: " + screenBrightness);

            String orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION).toString();
            cv.put(Storage.COLUMN_ORIENTATION, orientationSensor);
            Log.v(TAG, "orientation Sensor:" + orientationSensor);

            String proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY).toString();
            cv.put(Storage.COLUMN_PROXIMITY, proximity);
            Log.v(TAG, "proximity Sensor:" + proximity);

            cv.put(Storage.COLUMN_BATTERYSTATUS, batteryStatus);
            Log.v(TAG, "batteryStatus: " + batteryStatus);

            cv.put(Storage.COLUMN_BATTERYLEVEL, batteryLevel);
            Log.v(TAG, "batteryLevel: " + batteryLevel);

            cv.put(Storage.COLUMN_LEFT, faceDetectionLeftEye);
            Log.v(TAG, "faceDetectionLeftEye: " + faceDetectionLeftEye);

            cv.put(Storage.COLUMN_RIGHT, faceDetectionRightEye);
            Log.v(TAG, "faceDetectionRightEye: " + faceDetectionRightEye);

            cv.put(Storage.COLUMN_MOUTH, faceDetectionMouth);
            Log.v(TAG, "faceDetectionMouth: " + faceDetectionMouth);

            Log.v(TAG, "VALUES " + cv.toString());
        } else {
            //Read and store current data-----------------------------------------------------------
            //picture name--------------------------------------------------------------------------
            photoName = (String) intent.getExtras().get("photoName");
            cv.put(Storage.COLUMN_PHOTO, photoName);
            Log.v(TAG, "photoName" + photoName);

            //capture Event-------------------------------------------------------------------------
            cv.put(Storage.COLUMN_CAPTUREEVENT, capturingEvent);
            Log.v(TAG, "captureEvent: " + capturingEvent);

            //foreground App------------------------------------------------------------------------
            cv.put(Storage.COLUMN_FOREGROUNDAPP, foregroundApp);
            Log.v(TAG, "foregroundApp: " + foregroundApp);

            //location------------------------------------------------------------------------------
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD){
                buildGoogleApiClient();
                mGoogleApiClient.connect();
                latestLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                Intent pendingIntent = new Intent(getApplicationContext(), DataCollectorService.class);
                //latestLocation = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) pendingIntent);
                Log.v(TAG, "google location api, latest location: "+ latestLocation);
            }

            if (googleApiClientConnectionFailed) {
                Log.v(TAG, "failed to connect google play services location api.");
                locationListener = new LocationListener() {
                    public void onLocationChanged(Location location) {
                        latestLocation = location;
                        Log.v(TAG, "latest location from listener: " + location);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                };}

            locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    latestLocation = location;
                    Log.v(TAG, "latest location from listener: " + location);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            };

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                latestLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Log.v(TAG, "gps enabled.");

            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                latestLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                Log.v(TAG, "network enabled.");
            } else {
                Log.v(TAG, "neither gps or network enabled.");
            }
            //locationManager.removeUpdates(locationListener);

            if (latestLocation != null) {
                Log.v(TAG, "latest location: " + latestLocation);
                Geocoder geocoder = new Geocoder(this);
                locationLatitude = (int) latestLocation.getLatitude();
                locationLongitude = (int) latestLocation.getLongitude();
                try {
                    List<Address> addressList = null;
                    addressList = geocoder.getFromLocation(locationLatitude, locationLongitude, 1);
                    Address address = addressList.get(0);
                    locationRoad = address.getThoroughfare();
                    locationPLZ = address.getPostalCode();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                latestLocation = null;
            }
            cv.put(Storage.COLUMN_LOCATIONLATITUDE, locationLatitude);
            cv.put(Storage.COLUMN_LOCATIONLONGITUDE, locationLongitude);
            cv.put(Storage.COLUMN_LOCATIONROAD, locationRoad);
            cv.put(Storage.COLUMN_LOCATIONPOSTALCODE, locationPLZ);
            Log.v(TAG, "location: latitude: " + locationLatitude + ", longitude: " + locationLongitude + ", road: " + locationRoad + ", postalcode: " + locationPLZ);

            //accelerometer-------------------------------------------------------------------------
            cv.put(Storage.COLUMN_ACCELEROMETERX, accelerometerX);
            cv.put(Storage.COLUMN_ACCELEROMETERY, accelerometerY);
            cv.put(Storage.COLUMN_ACCELEROMETERZ, accelerometerZ);
            Log.v(TAG, "accelerometer: " + accelerometerX + " " + accelerometerY + " " + accelerometerZ);

            //rotation------------------------------------------------------------------------------
            cv.put(Storage.COLUMN_GYROSCOPEX, gyroscopeX);
            cv.put(Storage.COLUMN_GYROSCOPEY, gyroscopeY);
            cv.put(Storage.COLUMN_GYROSCOPEZ, gyroscopeZ);
            Log.v(TAG, "gyroscope: " + gyroscopeX + " " + gyroscopeY + " " + gyroscopeZ);

            //light---------------------------------------------------------------------------------
            cv.put(Storage.COLUMN_LIGHT, ambientLight);
            Log.v(TAG, "light:" + ambientLight);

            //screen brightness---------------------------------------------------------------------
            try {
                screenBrightness = android.provider.Settings.System.getInt(
                        getContentResolver(),
                        android.provider.Settings.System.SCREEN_BRIGHTNESS);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            cv.put(Storage.COLUMN_BRIGHTNESS, screenBrightness);
            Log.v(TAG, "screen brightness: " + screenBrightness);

            //orientation---------------------------------------------------------------------------
            orientation = (String) intent.getExtras().get("orientation");
            cv.put(Storage.COLUMN_ORIENTATION, orientation);
            Log.v(TAG, "orientation Sensor:" + orientation);

            //proximity-----------------------------------------------------------------------------
            cv.put(Storage.COLUMN_PROXIMITY, proximity);
            Log.v(TAG, "proximity Sensor:" + proximity);

            //battery level & status----------------------------------------------------------------
            if (Build.VERSION.SDK_INT >= 23) {
                BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
                batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                if (batteryManager.isCharging()) {
                    batteryStatus = "charging";
                } else {
                    batteryStatus = "nothing";
                }
            } else {
                IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Intent batteryIntent = registerReceiver(null, filter);

                int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

                float batteryPct = level / (float) scale;
                batteryLevel = (int) (batteryPct * 100);

                if (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL) {
                    batteryStatus = "charging";
                } else {
                    batteryStatus = "not charging";
                }
            }

            cv.put(Storage.COLUMN_BATTERYSTATUS, batteryStatus);
            Log.v(TAG, "batteryStatus: " + batteryStatus);

            cv.put(Storage.COLUMN_BATTERYLEVEL, batteryLevel);
            Log.v(TAG, "batteryLevel: " + batteryLevel);

            //face detection------------------------------------------------------------------------
            faceDetectionLeftEye = (String) intent.getExtras().get("faceDetectionLeftEye");
            faceDetectionRightEye = (String) intent.getExtras().get("faceDetectionRightEye");
            faceDetectionMouth = (String) intent.getExtras().get("faceDetectionMouth");

            cv.put(Storage.COLUMN_LEFT, faceDetectionLeftEye);
            cv.put(Storage.COLUMN_RIGHT, faceDetectionRightEye);
            cv.put(Storage.COLUMN_MOUTH, faceDetectionMouth);
            Log.v(TAG, "face detection: left: " + faceDetectionLeftEye + ", right: " + faceDetectionRightEye + ", mouth: " + faceDetectionMouth);
        }

        //create survey
        if (capturingEvent.equals(RANDOM)) {
            createSurvey();
        }

        //write data to database--------------------------------------------------------------------
        database = storage.getWritableDatabase();
        long insertId = database.insert(Storage.DB_TABLE, null, cv);
        Log.v(TAG, "data stored to db");
        database.close();

        //reset values not depending from sensor listener
        photoName = foregroundApp = locationRoad = locationPLZ = orientation = batteryStatus = faceDetectionLeftEye = faceDetectionRightEye = faceDetectionMouth = "n./a.";
        locationLatitude = locationLongitude = screenBrightness = -1;
        capturingEvent = "NOTHING";

        //TODO: pause listener due to battery life?------------------------------------------------

        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            proximity = String.valueOf(event.values[0]);
            Log.v(TAG, "new proximity: " + proximity);
        }
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            ambientLight = String.valueOf(event.values[0]);
            //Log.v(TAG, "new ambientLight: " + ambientLight);
        }
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) { //excluding the force of gravity
            accelerometerX = String.valueOf(event.values[0]);
            accelerometerY = String.valueOf(event.values[1]);
            accelerometerZ = String.valueOf(event.values[2]);
        }
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            gyroscopeX = String.valueOf(event.values[0]);
            gyroscopeY = String.valueOf(event.values[1]);
            gyroscopeZ = String.valueOf(event.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void createSurvey() {
        //create survey
        NotificationCompat.Builder surveyNotificationBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle("HDYHYP")
                        .setContentText("A questionnaire is waiting for you...")
                        .setOngoing(true)
                        .setVibrate(new long[] { 1000, 500 });

        Intent resultIntent = new Intent(getApplicationContext(), SurveyActivity.class);
        resultIntent.putExtra("photoName", photoName);
        resultIntent.putExtra("path", storage.getStoragePath());

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addParentStack(SurveyActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        (int) currentTimeMillis(),
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        surveyNotificationBuilder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(42, surveyNotificationBuilder.build());
    }


    protected synchronized void buildGoogleApiClient() {
        this.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClientConnectionFailed = false;
    }

    private void unregisterListener() {
    }

    public void setProximity(String proximity) {
        this.proximity = proximity;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        latestLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        googleApiClientConnectionFailed = false;
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient = null;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //TODO: in activity auslagern
        if (connectionResult.hasResolution()) {
            //connectionResult.startResolutionForResult(this, 1000);
        } else {
            googleApiClientConnectionFailed = true;
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        latestLocation = location;
        Log.v(TAG, "latest location from listener: " + location);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onDestroy() {
        if (this.mGoogleApiClient != null) {
            this.mGoogleApiClient.unregisterConnectionCallbacks(this);
            this.mGoogleApiClient.unregisterConnectionFailedListener(this);
            this.mGoogleApiClient.disconnect();
            this.mGoogleApiClient = null;
        }
    }
}