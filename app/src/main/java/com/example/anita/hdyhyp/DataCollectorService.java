package com.example.anita.hdyhyp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.Surface;

/*
*@class collects Data while the Picture is processed
* sets flag in Controller Service, when ecerything is collected
 */

public class DataCollectorService extends Service {
    public DataCollectorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }



    /*int rotation = getWindowManager().getDefaultDisplay().getRotation();
    int degrees = 0;
    switch (rotation) {
        case Surface.ROTATION_0:
            degrees = 0;
            break; // Natural orientation
        case Surface.ROTATION_90:
            degrees = 90;
            break; // Landscape left
        case Surface.ROTATION_180:
            degrees = 180;
            break;// Upside down
        case Surface.ROTATION_270:
            degrees = 270;
            break;// Landscape right
    }
    int displayRotation;*/


}
