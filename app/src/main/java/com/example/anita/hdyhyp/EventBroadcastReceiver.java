package com.example.anita.hdyhyp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;

/**
 * Knows when the user switches the screen on or off and lets the Controller Service know
 */

public class EventBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = EventBroadcastReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()){
            case Intent.ACTION_SCREEN_ON:
                Log.v(TAG, "Screen is on now.");
                break;
            case Intent.ACTION_SCREEN_OFF:
                Log.v(TAG, "Screen is off now.");
                //ObservableObject.getInstance().setIsScreenOn(false);
                break;
            case Intent.ACTION_CONFIGURATION_CHANGED:
                Configuration configuration = context.getResources().getConfiguration();
                if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
                    Log.v(TAG, "Orientation changed to landscape.");
                    ObservableObject.getInstance().setOrientationPortrait(false);
                } else if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT){
                    Log.v(TAG, "Orientation changed to portrait.");
                    ObservableObject.getInstance().setOrientationPortrait(true);
                }
                break;
            case Intent.ACTION_BOOT_COMPLETED:
                //restarts the controller service after device reboot, which also sets new random alarms (which would be removed after device restart)
                Log.v(TAG, "restart controller service after reboot");
                Intent controllerIntent = new Intent(context, ControllerService.class);
                context.startService(controllerIntent);
                break;
            default:
        }
        ObservableObject.getInstance().updateValue(intent);
    }
}

