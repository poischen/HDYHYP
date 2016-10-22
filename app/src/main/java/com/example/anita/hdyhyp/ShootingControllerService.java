package com.example.anita.hdyhyp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
//TODO Steuerung, wann das Bild geschossen wird & wie oft
public class ShootingControllerService extends Service {
    public ShootingControllerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
