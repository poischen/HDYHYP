package com.example.anita.hdyhyp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class ShootPicService extends Service {
    public ShootPicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public void onCreate() {
        Log.v("ShootPicService", "ShootPicService created.");
    }

    @Override
    public void onDestroy(){
        Log.v("ShootPicService", "ShootPicService destroyed.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("ShootPicService", "ShootPicService started.");
        shootPic();
        stopSelf();
        return START_STICKY;
    }

    public void shootPic(){
        //ToDo: Bild schießen, Beautyfilter drüber legen + Timestamp, Zwischenspeichern, Push Notification
        //TODO: eigener Thread


    }
}
