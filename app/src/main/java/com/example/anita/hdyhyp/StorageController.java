package com.example.anita.hdyhyp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

/**
 * Knows all storage information
 */

public class StorageController {

    private String storagePath;
    private UserNameStorage userNameStorage;

    public StorageController(Context context){
        userNameStorage = new UserNameStorage(context);
        storagePath = context.getFilesDir().toString();
        Log.v("CapturePicService", "Storage Path " +  storagePath);
        //Environment.getExternalStorageDirectory();
    }

    public String getUserName(){
        return this.userNameStorage.getUserName();
    }

    protected void setUserName(String input){
        this.userNameStorage.setUserName(input);
    }

    protected void deleteUserName(){
        userNameStorage.deleteUserName();
    }

    protected String getStoragePath(){
        return storagePath;
    }
}
