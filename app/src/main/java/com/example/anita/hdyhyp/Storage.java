package com.example.anita.hdyhyp;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;

import static android.content.Context.MODE_PRIVATE;
import static android.database.sqlite.SQLiteDatabase.openOrCreateDatabase;

/**
 * Knows all storage information
 * Shared Preferences for the username
 * Internal Storage for the pictures
 * SQL Database for the collected data
 */

public class Storage extends SQLiteOpenHelper {

    private static final String TAG = Storage.class.getSimpleName();

    public static final String DB_NAME = "HDYHYPDataBase.db";
    public static final String DB_TABLE = "HDYHYPDataCollection";
    public static final String COLUMN_CAPTURE_ID = "_id";
    public static final String COLUMN_PHOTO = "photoName";
    public static final String COLUMN_CAPTUREEVENT = "captureEvent";
    public static final String COLUMN_FOREGROUNDAPP = "foregroundApp";
    public static final String COLUMN_GYROSCOPEX = "gyroscopeX";
    public static final String COLUMN_GYROSCOPEY = "gyroscopeY";
    public static final String COLUMN_GYROSCOPEZ = "gyroscopeZ";
    public static final String COLUMN_ACCELEROMETERX = "accelerometerX";
    public static final String COLUMN_ACCELEROMETERY = "accelerometerY";
    public static final String COLUMN_ACCELEROMETERZ = "accelerometerZ";
    public static final String COLUMN_LIGHT = "light";
    public static final String COLUMN_BRIGHTNESS = "screenLightness";
    public static final String COLUMN_ORIENTATION = "orientation";
    //public static final String COLUMN_PROXIMITY = "proximity";
    public static final String COLUMN_BATTERYSTATUS = "batteryStatus";
    public static final String COLUMN_BATTERYLEVEL = "batteryLevel";
    public static final String COLUMN_RIGHT = "rightEye";
    public static final String COLUMN_LEFT = "leftEye";
    public static final String COLUMN_MOUTH = "Mouth";
    public static final String COLUMN_LOCATIONLATITUDE = "LocationLatitude";
    public static final String COLUMN_LOCATIONLONGITUDE = "LocationLongitude";
    public static final String COLUMN_LOCATIONROAD = "LocationRoad";
    public static final String COLUMN_LOCATIONPOSTALCODE = "LocationPLZ";

    public static final String DB_TABLESURVEY = "HDYHYPSurveyData";
    public static final String COLUMN_SURVEY_ID = "_id";
    public static final String COLUMN_DEVICEPOSITION = "devicePosition";
    public static final String COLUMN_HAND = "holdingHand";
    public static final String COLUMN_USERPOSTURE = "userPosture";
    public static final String COLUMN_USERPOSITION = "userPosition";
    public static final String COLUMN_USERPOSITIONOTEHRANSWERR = "userPositionOtherAnswer";
    public static final String COLUMN_DOINGSTH = "userDoingSomething";
    public static final String COLUMN_DOINGSTHOTHERANSWER = "userDoingSomethingOtherAnswer";


    public static final String SQL_CREATEDATA =
            "CREATE TABLE " + DB_TABLE +
                    "(" + COLUMN_CAPTURE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PHOTO + " TEXT, " +
                    COLUMN_CAPTUREEVENT + " TEXT, " +
                    COLUMN_FOREGROUNDAPP + " TEXT, " +
                    COLUMN_LOCATIONLATITUDE + " INTEGER, " +
                    COLUMN_LOCATIONLONGITUDE + " INTEGER, " +
                    COLUMN_LOCATIONROAD + " TEXT, " +
                    COLUMN_LOCATIONPOSTALCODE + " TEXT, " +
                    COLUMN_ACCELEROMETERX + " TEXT, " +
                    COLUMN_ACCELEROMETERY + " TEXT, " +
                    COLUMN_ACCELEROMETERZ + " TEXT, " +
                    COLUMN_GYROSCOPEX + " TEXT, " +
                    COLUMN_GYROSCOPEY + " TEXT, " +
                    COLUMN_GYROSCOPEZ + " TEXT, " +
                    COLUMN_LIGHT + " TEXT, " +
                    COLUMN_BRIGHTNESS + " INTEGER, " +
                    COLUMN_ORIENTATION + " TEXT, " +
                    //COLUMN_PROXIMITY + " TEXT, " +
                    COLUMN_BATTERYSTATUS + " TEXT, " +
                    COLUMN_BATTERYLEVEL + " INTEGER, " +
                    COLUMN_LEFT + " TEXT, " +
                    COLUMN_RIGHT + " TEXT, " +
                    COLUMN_MOUTH + " TEXT);";

    public static final String SQL_CREATESURVEY =
            "CREATE TABLE " + DB_TABLESURVEY +
                    "(" + COLUMN_SURVEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PHOTO + " TEXT, " +
                    COLUMN_DEVICEPOSITION + " TEXT, " +
                    COLUMN_HAND + " TEXT, " +
                    COLUMN_USERPOSTURE + " TEXT, " +
                    COLUMN_USERPOSITION + " TEXT, " +
                    COLUMN_USERPOSITIONOTEHRANSWERR + " TEXT, " +
                    COLUMN_DOINGSTH + " TEXT, " +
                    COLUMN_DOINGSTHOTHERANSWER + " TEXT, "
                    + " FOREIGN KEY ("+COLUMN_PHOTO+") REFERENCES "+DB_TABLE+"("+COLUMN_PHOTO+"));";


    public static final String STORAGEPATH = "storage/emulated/0/HDYHYP";
    //private UserNameStorage userNameStorage;
    //public static SQLiteDatabase database = openOrCreateDatabase("HDHYHPDataCollection",MODE_PRIVATE,null);

    private SharedPreferences userNameStorage;
    private SharedPreferences.Editor userNameEditor;


    public Storage(Context context) {
        super(context, DB_NAME, null, 2);
        Log.v(TAG, "Database created " + getDatabaseName());
        //userNameStorage = new UserNameStorage(context);
        //storagePath = (context.getFilesDir().toString());
        new File("storage/emulated/0/HDYHYP").mkdir();
        userNameStorage = context.getSharedPreferences("User Name Storage", 0);
        userNameEditor = userNameStorage.edit();
    }

    public String getUserName(){
        return userNameStorage.getString("User Name", null);
        //return this.userNameStorage.getUserName();
    }

    protected void setUserName(Context context, String input){
        userNameEditor.putString("User Name", input);
        userNameEditor.commit();

    }

    protected void deleteUserName(){
        userNameEditor.putString("User Name", null);
        userNameEditor.commit();
    }

    protected String getStoragePath(){
        return STORAGEPATH;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(SQL_CREATEDATA);
            Log.v(TAG, "Table was created. " + SQL_CREATEDATA + " angelegt.");
            db.execSQL(SQL_CREATESURVEY);
            Log.v(TAG, "Table was created. " + SQL_CREATESURVEY + " angelegt.");
        }
        catch (Exception e) {
            Log.e(TAG, "Could not create table. " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static boolean isServiceRunning(Context context, String serviceName){
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(service.service.getClassName())) {
                Log.v(TAG, serviceName + " is running.");
                return true;
            }
        }
        Log.v(TAG, "ControllerService is not running.");
        return false;
    }

}
