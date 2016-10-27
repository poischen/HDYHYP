package com.example.anita.hdyhyp;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * This class stores the peusdonym of the user during the study
 */

public class UserNameStorage {

    private static final String TAG = "UserNameStorage";

    private enum pseudonym {DUKE, MONKEY, BOMEAST, TANQUERAY, HENDRICKS, FEEL, MARE, BRANDSTIFER, GRANIT, BEEFEATER};
    private SharedPreferences userNameStorage;
    private SharedPreferences.Editor userNameEditor;
    private StorageController storageController;

    public UserNameStorage (Context context){
        userNameStorage = context.getSharedPreferences("User Name Storage", 0);
        userNameEditor = userNameStorage.edit();
    }

    protected void setUserName(String input){
        // todo: Test if the username is allowed
        userNameEditor.putString("User Name", input);
        userNameEditor.commit();
    }

    protected String getUserName(){
        return userNameStorage.getString("User Name", null);
    }

    protected void deleteUserName(){
        userNameEditor.putString("User Name", null);
        userNameEditor.commit();
    }

}