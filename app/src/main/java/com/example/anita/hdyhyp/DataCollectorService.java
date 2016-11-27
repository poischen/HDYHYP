package com.example.anita.hdyhyp;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentValues;
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
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;

import static com.example.anita.hdyhyp.ControllerService.CapturingEvent.NOTHING;

/*
*@class collects Data while the Picture is processed
 */

public class DataCollectorService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = DataCollectorService.class.getSimpleName();

    private SQLiteDatabase database;
    private Storage storage = ControllerService.storage;
    private SensorManager sensorManager;

    private LocationManager locationManager;
    private LocationListener locationListener = null;
    private Location latestLocation;
    private Boolean googleApiClientConnectionFailed;

    String photoName = "n./a.";
    String foregroundApp = "n./a.";
    String proximity = "n./a.";
    String location = "n./.a";
    String isKeyboardOpen = "n./.a";
    String accelerometer = "n./.a";
    String rotationGyroscopeRotationvector = "n./.a";
    String orientation = "n./.a";
    String ambientLight = "n./.a";
    int screenBrightness = -1;
    String batteryStatus = "n./a.";
    int batteryLevel = -1;
    String faceDetectionLeftEye = "n./.a";
    String faceDetectionRightEye = "n./.a";
    String faceDetectionMouth = "n./.a";
    String capturingEvent = "NOTHING";

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    public DataCollectorService() {
        super("DataCollectorService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //TODO: in mehr zeilen aufteilen
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(TAG, "data collection started.");
        capturingEvent = (String) intent.getExtras().get("capturingEvent");
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        ContentValues cv = new ContentValues();

        //register necessary listener
        SensorEventListener sensorEventListener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
            //TODO: weglassen, da man nicht gleichzeitig die Sensoren auslesen kann
            @Override
            public void onSensorChanged(SensorEvent event) {
                int detectedSensorEvent = event.sensor.getType();
                switch (detectedSensorEvent){

                }
                if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                    proximity = String.valueOf(event.values[0]);
                }
                if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                    ambientLight = String.valueOf(event.values[0]);
                }
            }
        };

        Sensor proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (proximitySensor != null) {
            sensorManager.registerListener(sensorEventListener, proximitySensor, SensorManager.SENSOR_DELAY_FASTEST);
        }

        //-------------
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(1);
        mLocationRequest.setFastestInterval(1);

        buildGoogleApiClient();




        Log.v(TAG, "listener registered.");

        //collect data
        //if (capturingEvent != null && capturingEvent.equals("NOTHING")) {
            /*
            //Read and store general sensor info first
            String photoName = (String) intent.getExtras().get("photoName");
            cv.put(Storage.COLUMN_PHOTO, photoName);
            Log.v(TAG, "photoName" + photoName);

            String foregroundApp = (String) intent.getExtras().get("foregroundApp");
            cv.put(Storage.COLUMN_FOREGROUNDAPP, foregroundApp);
            Log.v(TAG, "foregroundApp: " + foregroundApp);

            String accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).toString();
            cv.put(Storage.COLUMN_ACCELEROMETER, accelerometer);
            Log.v(TAG, "accelerometer Sensor:" + accelerometer);

            String gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE).toString();
            cv.put(Storage.COLUMN_GYROSCOPE, gyroscope);
            Log.v(TAG, "gyroscope Sensor:" + gyroscope);

            String lin_accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION ).toString();
            cv.put(Storage.COLUMN_LINEAR_ACCELERATION, lin_accelerometer);
            Log.v(TAG, "lin_accelerometer Sensor:" + lin_accelerometer);

            String light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT).toString();
            cv.put(Storage.COLUMN_LIGHT, light);
            Log.v(TAG, "light Sensor:" + light);

            String orientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION).toString();
            cv.put(Storage.COLUMN_ORIENTATION, orientation);
            Log.v(TAG, "orientation Sensor:" + orientation);

            String proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY).toString();
            cv.put(Storage.COLUMN_PROXIMITY, proximity);
            Log.v(TAG, "proximity Sensor:" + proximity);

            String rotation_vector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR).toString();
            cv.put(Storage.COLUMN_ROTATION_VECTOR, rotation_vector);
            Log.v(TAG, "rotation_vector Sensor:" + rotation_vector);

            Log.v(TAG, "VALUES " + cv.toString());*/


        //} else {
        //Read and store sensor values
        //picture name--------------------------------------------------------------------------
        photoName = (String) intent.getExtras().get("foregroundApp");
        Log.v(TAG, "foreground App: " + photoName);

        //foreground App------------------------------------------------------------------------
        foregroundApp = (String) intent.getExtras().get("foregroundApp");
        Log.v(TAG, "foreground App: " + foregroundApp);

        //capture Event-------------------------------------------------------------------------
        Log.v(TAG, "capture event: " + capturingEvent);

        //proximity-----------------------------------------------------------------------------
        Log.v(TAG, "proximity: " + proximity);

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
            return;
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
        //}

        if (latestLocation != null) {
            Log.v(TAG, "latest location: " + latestLocation);
            Geocoder geocoder = new Geocoder(this);
            Double latitude = latestLocation.getLatitude();
            Double longitude = latestLocation.getLongitude();
            List<Address> addressList = null;
            try {
                addressList = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            location = "latitude: " + latitude + ", longitude: " + longitude + ", road: " + address.getThoroughfare() + ", postalcode: " + address.getPostalCode();
        }
        Log.v(TAG, "location: " + location);


        //keyboard------------------------------------------------------------------------------


        //battery level & status------------------------------------------------------------------------
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
                batteryStatus = "nothing";
            }
            //unregister?
        }
        Log.v(TAG, "battery status: " + batteryStatus + ", battery level: " + batteryLevel);

        //accelerometer-------------------------------------------------------------------------


        //rotation------------------------------------------------------------------------------


        //orientation---------------------------------------------------------------------------

        //String orientation = sensorManager.getOrientation();

        //light and screen Brightness-----------------------------------------------------------
        try {
            screenBrightness = android.provider.Settings.System.getInt(
                    getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        Log.v(TAG, "screen birghtness: " + screenBrightness);

        //face detection------------------------------------------------------------------------



        // }

        //unregister listener and write data to database
        sensorManager.unregisterListener(sensorEventListener);
        // database = storage.getWritableDatabase();
        //  long insertId = database.insert(Storage.DB_TABLE, null, cv);
        // database.close();

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

    public void setProximity(String proximity){
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
        }else {
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



    /*TYPE_ACCELEROMETER 	Hardware 	Measures the acceleration force in m/s2 that is applied to a device on all three physical axes (x, y, and z), including the force of gravity. 	Motion detection (shake, tilt, etc.).
TYPE_AMBIENT_TEMPERATURE 	Hardware 	Measures the ambient room temperature in degrees Celsius (°C). See note below. 	Monitoring air temperatures.
TYPE_GRAVITY 	Software or Hardware 	Measures the force of gravity in m/s2 that is applied to a device on all three physical axes (x, y, z). 	Motion detection (shake, tilt, etc.).
TYPE_GYROSCOPE 	Hardware 	Measures a device's rate of rotation in rad/s around each of the three physical axes (x, y, and z). 	Rotation detection (spin, turn, etc.).
TYPE_LIGHT 	Hardware 	Measures the ambient light level (illumination) in lx. 	Controlling screen brightness.
TYPE_LINEAR_ACCELERATION 	Software or Hardware 	Measures the acceleration force in m/s2 that is applied to a device on all three physical axes (x, y, and z), excluding the force of gravity. 	Monitoring acceleration along a single axis.
TYPE_MAGNETIC_FIELD 	Hardware 	Measures the ambient geomagnetic field for all three physical axes (x, y, z) in μT. 	Creating a compass.
TYPE_ORIENTATION 	Software 	Measures degrees of rotation that a device makes around all three physical axes (x, y, z). As of API level 3 you can obtain the inclination matrix and rotation matrix for a device by using the gravity sensor and the geomagnetic field sensor in conjunction with the getRotationMatrix() method. 	Determining device position.
TYPE_PRESSURE 	Hardware 	Measures the ambient air pressure in hPa or mbar. 	Monitoring air pressure changes.
TYPE_PROXIMITY 	Hardware 	Measures the proximity of an object in cm relative to the view screen of a device. This sensor is typically used to determine whether a handset is being held up to a person's ear. 	Phone position during a call.
TYPE_RELATIVE_HUMIDITY 	Hardware 	Measures the relative ambient humidity in percent (%). 	Monitoring dewpoint, absolute, and relative humidity.
TYPE_ROTATION_VECTOR 	Software or Hardware 	Measures the orientation of a device by providing the three elements of the device's rotation vector. 	Motion detection and rotation detection.
TYPE_TEMPERATURE 	Hardware 	Measures the temperature of the device in degrees Celsius (°C). This sensor implementation varies across devices and this sensor was replaced with the TYPE_AMBIENT_TEMPERATURE sensor in API Level 14 	Monitoring temperatures.*/


