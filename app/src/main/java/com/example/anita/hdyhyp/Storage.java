package com.example.anita.hdyhyp;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Service?? that stores study data
 */

class Storage {

    

    private Context context;
    private SharedPreferences userNameStorage;
    private SharedPreferences.Editor userNameEditor;

    public Storage (Context context){
        this.context = context;
        userNameStorage = context.getSharedPreferences("User Name Storage", 0);
        userNameEditor = userNameStorage.edit();

    }

    protected void setUserName(String input){
        // todo: Test ob Eingabe passt
        userNameEditor.putString("User Name", input);
        userNameEditor.commit();
    }

    protected String getUserName(){
        return userNameStorage.getString("User Name", null);
    }

}
