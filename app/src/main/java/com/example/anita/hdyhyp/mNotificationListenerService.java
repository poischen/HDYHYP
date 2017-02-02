package com.example.anita.hdyhyp;

import android.service.notification.StatusBarNotification;
import android.util.Log;

public class mNotificationListenerService extends android.service.notification.NotificationListenerService {

    private String TAG = this.getClass().getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            String packageName = sbn.getPackageName();
            Log.i(TAG, "onNotificationPosted(), package name: " + packageName);
            ControllerService.currentNotification = packageName;
            ObservableObject.getInstance().updateValue(sbn);
            } catch (Exception e) {
            Log.d(TAG, "incoming push package Name could not be read");
        }

    }

}