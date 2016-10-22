package com.example.anita.hdyhyp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
//TODO Steuerung, wann das Bild geschossen wird & wie oft

/*
Controlls when and how often photos are captures
Knows the storage folder of the files
 */
public class CaptureControllerService extends Service {
    public CaptureControllerService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
