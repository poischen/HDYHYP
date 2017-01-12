package com.example.anita.hdyhyp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/*
restarts ControllerService after System Reboot (which also sets new Random Alarms)
 */

public class RebootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent controllerIntent = new Intent(context, ControllerService.class);
            context.startService(controllerIntent);
        }
    }
}