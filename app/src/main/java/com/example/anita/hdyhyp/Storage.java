package com.example.anita.hdyhyp;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;


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
    public static final String DB_TABLE = "HDHYHPDataCollection";
    public static final String COLUMN_CAPTURE_ID = "_id";
    public static final String COLUMN_PHOTO = "photoName";
    public static final String COLUMN_CAPTUREEVENT = "captureEvent";
    public static final String COLUMN_FOREGROUNDAPP = "foregroundApp";
    public static final String COLUMN_GYROSCOPE = "accelerometer";
    public static final String COLUMN_ACCELEROMETER = "accelerometer";
    public static final String COLUMN_LINEAR_ACCELERATION = "lin_accelerometer";
    public static final String COLUMN_ROTATION_VECTOR = "rotation_vector";
    public static final String COLUMN_LIGHT = "light";
    public static final String COLUMN_BRIGHTNESS = "screenLightness";
    public static final String COLUMN_ORIENTATION = "orientation";
    public static final String COLUMN_PROXIMITY = "proximity";
    public static final String COLUMN_BATTERYSTATUS = "batteryStatus";
    public static final String COLUMN_BATTERYLEVEL = "batteryLevel";
    public static final String COLUMN_RIGHT = "rightEye";
    public static final String COLUMN_LEFT = "leftEye";
    public static final String COLUMN_MOUTH = "Mouth";




    public static final String SQL_CREATE =
            "CREATE TABLE " + DB_TABLE +
                    "(" + COLUMN_CAPTURE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PHOTO + " TEXT, " +
                    COLUMN_CAPTUREEVENT + " TEXT, " +
                    COLUMN_FOREGROUNDAPP + " TEXT, " +
                    COLUMN_ACCELEROMETER + " TEXT, " +
                    COLUMN_GYROSCOPE + " TEXT, " +
                    COLUMN_LINEAR_ACCELERATION  + " TEXT, " +
                    COLUMN_ROTATION_VECTOR + " TEXT " +
                    COLUMN_LIGHT + " INTEGER, " +
                    COLUMN_BRIGHTNESS + " TEXT, " +
                    COLUMN_ORIENTATION + " TEXT, " +
                    COLUMN_PROXIMITY + " TEXT, " +
                    COLUMN_BATTERYSTATUS + " TEXT, " +
                    COLUMN_BATTERYLEVEL + " INTEGER, " +
                    COLUMN_LEFT + " TEXT, " +
                    COLUMN_RIGHT + " TEXT, " +
                    COLUMN_MOUTH + " TEXT);";

    private static String storagePath = "storage/emulated/0/HDYHYP";
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
        // this.userNameStorage.setUserName(input);

    }

    protected void deleteUserName(){
        userNameEditor.putString("User Name", null);
        userNameEditor.commit();
        //userNameStorage.deleteUserName();
    }

    protected String getStoragePath(){
        return storagePath;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(SQL_CREATE);
            Log.v(TAG, "Table was created. " + SQL_CREATE + " angelegt.");
        }
        catch (Exception e) {
            Log.e(TAG, "Could not create table. " + e.getMessage());
        }


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
