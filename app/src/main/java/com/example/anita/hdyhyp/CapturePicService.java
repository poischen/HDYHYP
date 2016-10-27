package com.example.anita.hdyhyp;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class CapturePicService extends Service {

    private static final String TAG = "CapturePicService";
    private Camera camera;
    private int cameraId;
    private String storagePath;
    private String userName;

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
        Log.v(TAG, "CapturePicService created.");
    }

    @Override
    public void onDestroy(){
        Log.v(TAG, "CapturePicService destroyed.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "CapturePicService started.");
        //Check if camera can be found

        camera = getCameraInstance();
        storagePath = (String) intent.getExtras().get("path");
        userName = (String) intent.getExtras().get("userName");

        SurfaceTexture surfaceTexture = new SurfaceTexture(0);
        try {
            camera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
        capturePhoto();
        stopSelf();
        return START_NOT_STICKY;
    }

    /*Search for the front facing camera*/
    private int findFrontFacingCam() {
        int camId = -2;

        int cameras = camera.getNumberOfCameras();
        for (int i=0;i<cameras;i++) {
            android.hardware.Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                camId = i;
                Log.v(TAG, "Camera found. Camera id: " + camId);
                break;
            }
        }
        return camId;
    }

    public Camera getCameraInstance(){
        Camera c = null;
        if (!getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Log.d(TAG, "No camera found. Error code 1.");
        } else {
            cameraId = findFrontFacingCam();
            if (cameraId < 0) {
                Log.d(TAG, "No camera found. Error code 2, camera ID " + cameraId);
            } else {
                if (c != null) {
                    c.release();
                    c = null;
                }
                c = Camera.open(cameraId);
            }
        }
        return c;
    }

    private void capturePhoto(){
        Log.v(TAG, "capturePhoto() is called.");
        // ToDO: Timestamp auf Foto drucken, evtl Filter
        // Todo: Push Notification

        Camera.PictureCallback pictureCallBack = new Camera.PictureCallback() {

            public void onPictureTaken(byte[] data, Camera camera) {
                Log.v(TAG, "pictureCallBack created. " + "onPictureTaken() is called.");
            File capturedPhotoFile = getOutputMediaFile();
                camera.startPreview();
            if (capturedPhotoFile == null) {
                Log.e(TAG, "Could not create file");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(capturedPhotoFile);
                fos.write(data);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "File not found: " + e.getMessage());
                e.getStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "I/O error writing file: " + e.getMessage());
                e.getStackTrace();
            }
        }

            //Hilfsmethode für Timestamp, Beaityfilter etc.
            private File getOutputMediaFile() {
                Log.v(TAG, "getOutputMediaFile() called.");
                File filePath = new File(storagePath);
                Log.v(TAG, "File created.");
                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy_HH:mm:ss");
                String timeString = dateFormat.format(new Date());
                Log.v(TAG, "Current time: " + timeString);
                Toast.makeText(getApplicationContext(), filePath.getPath() + File.separator + userName + "_"
                        + timeString + ".jpg", Toast.LENGTH_SHORT).show();
                return new File(filePath.getPath() + File.separator + userName + "_"
                            + timeString + ".jpg");
            }

        };

        camera.takePicture(null, null, pictureCallBack);
        Log.v(TAG, "takePicture is called.");
        camera.release();
        camera = null;
    }
    
}
