package com.example.anita.hdyhyp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
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
        if (!getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Log.d(TAG, "No camera found. Error code 1.");
        } else {
            cameraId = findFrontFacingCam();
            if (cameraId < 0) {
                Log.d(TAG, "No camera found. Error code 2, camera ID " + cameraId);
            } else {
                if (camera != null) {
                    camera.release();
                    camera = null;
                }
                storagePath = (String) intent.getExtras().get("path");
                userName = (String) intent.getExtras().get("userName");
                camera = Camera.open(cameraId);
                capturePhoto();
            }
        }

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


    private void capturePhoto(){
        Log.v(TAG, "capturePhoto() is called.");
        // ToDo: Beautyfilter drüber legen
        // ToDO: Timestamp auf Foto drucken
        // Todo: Push Notification
        // TODO: eigener Thread
        //capture Photo
        /*Camera.PictureCallback pictureCallBack = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
            File capturedPhotoFile = getOutputMediaFile();
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
                File filePath = new File(storagePath);
                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy_HH:mm:ss");
                String timeString = dateFormat.format(new Date());
                Log.v(TAG, "Current time: " + timeString);
                Toast.makeText(getApplicationContext(), filePath.getPath() + File.separator + userName + "_"
                        + timeString + ".jpg", Toast.LENGTH_SHORT).show();
                return new File(filePath.getPath() + File.separator + userName + "_"
                            + timeString + ".jpg");
            }

        };


        Log.v(TAG, "pictureCallBack created . " + pictureCallBack.toString());

*/

Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
    public void onPictureTaken(byte[] data, Camera camera) {
        FileOutputStream outStream = null;
        try {
            // write to local sandbox file system
            // outStream =
            // CameraDemo.this.openFileOutput(String.format("%d.jpg",
            // System.currentTimeMillis()), 0);
            // Or write to sdcard
            outStream = new FileOutputStream(String.format(
                    "/sdcard/%d.jpg", System.currentTimeMillis()));
            outStream.write(data);
            outStream.close();
            Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
        Log.d(TAG, "onPictureTaken - jpeg");
    }
};


        Preview preview = new Preview(this, camera);
        preview.camera.takePicture(null, null,jpegCallback);

        //camera.startPreview();
        //camera.takePicture(null, null, pictureCallBack);
        //camera.release();
    }



   /* class PicHandler implements Camera.PictureCallback{

        public PicHandler(){
            Log.v("CapturePicService", "Creating pic handler...");
        }

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.v("CapturePicService", "onPictureTaken() is called");
            //Log time
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy_HH:mm:ss");
            String timeString = dateFormat.format(new Date());
            Log.v("CapturePicService", "Current time: " + timeString);

           //Create and save Photo File
            final File capturedPhotoFile = new File(storagePath + userName + "_" + timeString + ".jpeg");
            Log.v("CapturePicService", "neues Bild: " + storagePath + userName + "_" + timeString + ".jpeg");

            //String filename = pictureFileDir.getPath() + File.separator + photoFile;

            //File pictureFile = new File(filename);

            try {
                FileOutputStream fileOutputStream = new FileOutputStream(capturedPhotoFile);
                fileOutputStream.write(data);
                fileOutputStream.close();
            } catch (Exception e) {
                Log.d("CapturePicService", "File: " + storagePath + userName + "_" + timeString + ".jpeg not saved: " + e.getMessage());
            }

        }
    }*/

    }



class Preview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "Preview";

    SurfaceHolder mHolder;
    public Camera camera;

    Preview(Context context, Camera camera) {
        super(context);
        this.camera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        try {
            camera.setPreviewDisplay(holder);

            camera.setPreviewCallback(new Camera.PreviewCallback() {

                public void onPreviewFrame(byte[] data, Camera arg1) {
                    FileOutputStream outStream = null;
                    try {
                        outStream = new FileOutputStream(String.format(
                                "/sdcard/%d.jpg", System.currentTimeMillis()));
                        outStream.write(data);
                        outStream.close();
                        Log.d(TAG, "onPreviewFrame - wrote bytes: "
                                + data.length);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                    }
                    Preview.this.invalidate();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
        camera.stopPreview();
        camera = null;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(w, h);
        //camera.setParameters(parameters);
        camera.startPreview();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Paint p = new Paint(Color.RED);
        Log.d(TAG, "draw");
        canvas.drawText("PREVIEW", canvas.getWidth() / 2,
                canvas.getHeight() / 2, p);
    }
}
