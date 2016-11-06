package com.example.anita.hdyhyp;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class stores the peusdonym of the user during the study
 */

public class UserNameStorage {

    private static final String TAG = "UserNameStorage";

    private SharedPreferences userNameStorage;
    private SharedPreferences.Editor userNameEditor;

    public UserNameStorage (Context context){
        userNameStorage = context.getSharedPreferences("User Name Storage", 0);
        userNameEditor = userNameStorage.edit();
    }

    protected void setUserName(String input){
        //if (pseudonyms.contains(input)) {
            userNameEditor.putString("User Name", input);
            userNameEditor.commit();
      //  }

    }

    protected String getUserName(){
        return userNameStorage.getString("User Name", null);
    }

    protected void deleteUserName(){
        userNameEditor.putString("User Name", null);
        userNameEditor.commit();
    }

}