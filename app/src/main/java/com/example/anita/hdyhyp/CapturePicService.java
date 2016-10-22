package com.example.anita.hdyhyp;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CapturePicService extends Service {
    String storagePath;

    public CapturePicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public void onCreate() {
        Log.v("CapturePicService", "CapturePicService created.");
    }

    @Override
    public void onDestroy(){
        Log.v("CapturePicService", "CapturePicService destroyed.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("CapturePicService", "CapturePicService started.");
        storagePath = (String) intent.getExtras().get("path");
        capturePhoto();
        stopSelf();
        return START_STICKY;
    }

    public void capturePhoto(String path, String userName){
        //ToDo: Bild schießen, Beautyfilter drüber legen + Timestamp, Zwischenspeichern, Push Notification
        //TODO: eigener Thread
        String time = (new SimpleDateFormat("dd.MM.yyyy hh:mm:ss", Locale.GERMANY)).toString();
        Log.v("CapturePicService", "Current time: " + time);
        final File capturedPhoto = new File(path, userName + time + ".jpg");
        Uri photoUri = Uri.fromFile(capturedPhoto);

        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

    }
}
