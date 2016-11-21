package com.example.anita.hdyhyp;

import android.app.IntentService;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObservable;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;

import static com.example.anita.hdyhyp.ControllerService.CapturingEvent.NOTHING;

/*
*@class collects Data while the Picture is processed
 */

public class DataCollectorService extends IntentService {

    private static final String TAG = DataCollectorService.class.getSimpleName();

    private SQLiteDatabase database;
    private Storage storage = ControllerService.storage;

    public DataCollectorService() {
        super("DataCollectorService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String capturingEvent = (String) intent.getExtras().get("capturingEvent");
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        ContentValues cv = new ContentValues();
        if (capturingEvent != null && capturingEvent.equals("NOTHING")){
                //Read and store general sensor info first
                String accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).toString();
                cv.put(Storage.COLUMN_ACCELEROMETER, accelerometer);
                Log.v(TAG, "accelerometer" + "TEST");

                String gyroscope = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE).toString();
                cv.put(Storage.COLUMN_GYROSCOPE, gyroscope);
                Log.v(TAG, "gyroscope" + "TEST");

                String lin_accelerometer = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION ).toString();
                cv.put(Storage.COLUMN_LINEAR_ACCELERATION, lin_accelerometer);
                Log.v(TAG, "lin_accelerometer" + "TEST");

                String light = sm.getDefaultSensor(Sensor.TYPE_LIGHT).toString();
                cv.put(Storage.COLUMN_LIGHT, "TEST");
                Log.v(TAG, "light" + light);

                String orientation = sm.getDefaultSensor(Sensor.TYPE_ORIENTATION).toString();
                cv.put(Storage.COLUMN_ORIENTATION, "TEST");
                Log.v(TAG, "orientation" + orientation);

                String proximity = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY).toString();
                cv.put(Storage.COLUMN_PROXIMITY, "TEST");
                Log.v(TAG, "proximity" + proximity);

                String rotation_vector = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR).toString();
                cv.put(Storage.COLUMN_ROTATION_VECTOR, rotation_vector);
                Log.v(TAG, "rotation_vector" + "TEST");

            Log.v(TAG, "VALUES " + cv.toString());


        } else {
            //Read and store sensor values
            String foregroundApp = (String) intent.getExtras().get("foregroundApp");


            //String orientation = sm.getOrientation();
        }
        database = storage.getWritableDatabase();
        database.insert(Storage.DB_TABLE, null, cv);
        database.close();

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


}
