package com.example.anita.hdyhyp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;

/**
 * Knows when the user switches the screen on or off and lets the Controller Service know
 */

public class MyBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "MyBroadcastReceiver";
    private boolean isScreenOn;
 //TODO: ggf. Case statt if?
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.v(TAG, "Screen is on now.");
            isScreenOn = true;
            ObservableObject.getInstance().setIsScreenOn(isScreenOn);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.v(TAG, "Screen is off now.");
            isScreenOn = false;
            ObservableObject.getInstance().setIsScreenOn(isScreenOn);
        } else if (intent.getAction().equals(Intent.ACTION_CONFIGURATION_CHANGED)){
            Configuration configuration = context.getResources().getConfiguration();
            if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
                Log.v(TAG, "Orientation changed to landscape.");
                ObservableObject.getInstance().setOrientationPortrait(false);
            } else if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT){
                Log.v(TAG, "Orientation changed to portrait.");
                ObservableObject.getInstance().setOrientationPortrait(true);
            }
            if (configuration.keyboardHidden == Configuration.KEYBOARDHIDDEN_NO) {
                Log.v(TAG, "Keyboard is opened.");
                ObservableObject.getInstance().setKeyboardVisible(true);
            }  else if (configuration.keyboardHidden == Configuration.KEYBOARDHIDDEN_YES) {
                Log.v(TAG, "Keyboard is not opened.");
                ObservableObject.getInstance().setKeyboardVisible(false);
            }
        }

        if (intent.getAction().equals(Intent.ACTION_GET_CONTENT)) {
            Log.v(TAG, "Screen is on now.");
            isScreenOn = true;
            ObservableObject.getInstance().setIsScreenOn(isScreenOn);
        }



        ObservableObject.getInstance().updateValue(intent);
    }
}
