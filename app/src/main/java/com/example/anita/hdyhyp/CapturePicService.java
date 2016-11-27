package com.example.anita.hdyhyp;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class CapturePicService extends Service {

    private static final String TAG = CapturePicService.class.getSimpleName();
    private int camId = -2;
    private Camera camera = null;
    private String storagePath;
    private String userName;
    private SurfaceTexture surfaceTexture;
    private String capturingEvent;
    private String foregroundApp;

    private Thread capturePhotoThread;

    public CapturePicService()
    {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            camera = getCameraInstance();
            storagePath = (String) intent.getExtras().get("storagePath");
            userName = (String) intent.getExtras().get("userName");

            surfaceTexture = new SurfaceTexture(0);
            try {
                camera.setPreviewTexture(surfaceTexture);
            } catch (IOException e) {
                e.printStackTrace();
            }
            capturingEvent = intent.getExtras().get("capturingEvent").toString();
            foregroundApp = (String) intent.getExtras().get("foregroundApp");
            capturePhotoThread = new Thread(runnableShootPicture);
            capturePhotoThread.start();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, " could not be handled.");

        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        findFrontFacingCam();
        Log.v(TAG, "CapturePicService created.");
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "CapturePicService destroyed.");
    }

    //Search for the front facing camera
    private void findFrontFacingCam() {
        int cameras = camera.getNumberOfCameras();
        for (int i = 0; i < cameras; i++) {
            android.hardware.Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                camId = i;
                Log.v(TAG, "Camera found. Camera id: " + camId);
                break;
            }
        }
    }

    //TODO: Test ob Camera anderweitig belegt
    public Camera getCameraInstance() {
        //System.gc();
        Camera c = null;
        if (!getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Log.d(TAG, "No camera found. Error code 1.");
        } else {
            if (camId < 0) {
                Log.d(TAG, "No camera found. Error code 2, camera ID " + camId);
            } else {
                if (c != null) {
                    c.release();
                    c = null;
                }
                c = Camera.open(camId);
                Log.v(TAG, "Camera opened: " + c);
            }
        }
        return c;
    }


    private Runnable runnableShootPicture = new Runnable() {
        @Override
        public void run() {
            try {
                capturePhoto(foregroundApp, capturingEvent);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG, "runnableShootPicture not successful");
            }
        }
    };

    private synchronized void capturePhoto(String foregroundApp, String capturingEvent) throws InterruptedException {
        Log.v(TAG, "capturePhoto() is called.");
        CameraPictureCallback pictureCallBack = new CameraPictureCallback(surfaceTexture, foregroundApp, capturingEvent, this);
//        pictureCallBack.setSurfaceTexture(surfaceTexture);
//        pictureCallBack.setForegroundApp(foregroundApp);
//        pictureCallBack.setCapturingEvent(capturingEvent);

        camera.startPreview();

        //recording additional information provided by the camera
        //TODO: auf Displaygroesse mappen
        Camera.Parameters params = camera.getParameters();
        Log.v(TAG, "3 camera: " + camera);

        ////if (Build.VERSION.SDK_INT >= 17) {
       // FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
         //       .setTrackingEnabled(false)
           //     .setLandmarkType(FaceDetector.ALL_LANDMARKS)
             //   .build();

       // detector.setProcessor(
         //       new MultiProcessor.Builder<Face>()
           //             .build(new GraphicFaceTrackerFactory()));

   // } else {
     //   if (params.getMaxNumDetectedFaces() > 0) {
 //           camera.setFaceDetectionListener(new FaceDetectionListener());

   //         camera.startFaceDetection();

     //   } else {
       //     Log.e(TAG, "Face detection is not supported.");
       // }
    //////


    //taking the picture
    camera.takePicture(null, null, pictureCallBack);
        //TODO 1. while
        //2. Thread schlafen
        //falls erfolgreich: Observable?


        //while (!pictureCallBack.isPictureTaken()){
        //    Log.v(TAG, "Waiting for pcb");
        //}
        Log.v(TAG, "wait");
        //wait(5000); //FUNKTIONIERT aber evtl gar nicht nötig, wenn stopSelf weggelassen wird
        Log.v(TAG, "wait no more");

    Log.v(TAG, "4 camera: " + camera);
   //TODO: TEST OB ES VLT DARAN LAG?
    //camera = null;
    Log.v(TAG, "5 camera: " + camera);
        //stopSelf();

    //TODO: first Try abfangen

    //Face Data

    //if (capturingEvent.equals("test")){
    //  dataCollectionIntent.putExtra("firstTryOrNot", "yes");
    //}


    }

    public void finnishCapturing(){
        camera = null;
        capturePhotoThread.interrupt();
        stopSelf();
    }



public class CameraPictureCallback implements Camera.PictureCallback {

    //boolean pictureTaken = false;
    private String pictureName;
    private SurfaceTexture surfaceTexture;
    private String foregroundApp;
    private String capturingEvent;
    private CapturePicService cps;

    public CameraPictureCallback (SurfaceTexture surfaceTexture, String foregroundApp, String capturingEvent, CapturePicService cps){
        this.surfaceTexture = surfaceTexture;
        this.foregroundApp = foregroundApp;
        this.capturingEvent = capturingEvent;
        this.cps = cps;
    }
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!in neuer version
    /*
    @Override
    public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                   @NonNull CaptureRequest request,
                                   @NonNull TotalCaptureResult result) {

        // your picture has been taken
    };*/

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.v(TAG, "1 surface Texture: " + surfaceTexture);
        Log.v(TAG, "6 camera: " + camera);
        File capturedPhotoFile = getOutputMediaFile();
        if (capturedPhotoFile == null) {
            Log.e(TAG, "Could not create file");
            return;
        }

        Log.v(TAG, "4 surface Texture: " + surfaceTexture);
        Log.v(TAG, "7 camera: " + camera);
        Bitmap capturedImage = BitmapFactory.decodeByteArray(data, 0, data.length);
        Log.v(TAG, "5 surface Texture: " + surfaceTexture);
        Log.v(TAG, "8 camera: " + camera);
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(camId, info);
        int w = capturedImage.getWidth();
        int h = capturedImage.getHeight();
        Matrix mtx = new Matrix();
        mtx.postRotate(info.orientation);
        Bitmap rotatedImage = Bitmap.createBitmap(capturedImage, 0, 0, w, h, mtx, true);
        Log.v(TAG, "6 surface Texture: " + surfaceTexture);
        Log.v(TAG, "9 camera: " + camera);

        try {
            FileOutputStream fos = new FileOutputStream(capturedPhotoFile);
            rotatedImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.write(data);
            Log.v(TAG, "7 surface Texture: " + surfaceTexture);
            Log.v(TAG, "10 camera: " + camera);
            fos.flush();
            Log.v(TAG, "8 surface Texture: " + surfaceTexture);
            Log.v(TAG, "11 camera: " + camera);
            fos.close();
            Log.v(TAG, "9 surface Texture: " + surfaceTexture);
            Log.v(TAG, "12 camera: " + camera);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.getMessage());
            e.getStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "I/O error writing file: " + e.getMessage());
            e.getStackTrace();
        }

        Log.v(TAG, "10 surface Texture: " + surfaceTexture);
        Log.v(TAG, "13 camera: " + camera);
        Intent dataCollectionIntent = new Intent(getApplicationContext(), DataCollectorService.class);
        dataCollectionIntent.putExtra("foregroundApp", foregroundApp);
        dataCollectionIntent.putExtra("capturingEvent", capturingEvent);
        dataCollectionIntent.putExtra("photoName", pictureName);
        getApplicationContext().startService(dataCollectionIntent);
        Log.v(TAG, "11 surface Texture: " + surfaceTexture);
        Log.v(TAG, "14 camera: " + camera);
        Log.v(TAG, "data collection gets started.");
        camera.release();
        //pictureTaken = true;
        cps.finnishCapturing();
    }

    //Hilfsmethode
    private File getOutputMediaFile() {
        Log.v(TAG, "getOutputMediaFile() called.");
        Log.v(TAG, "2 surface Texture: " + surfaceTexture);
        File filePath = new File(storagePath);
        Log.v(TAG, "File created.");
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy_HH:mm:ss");
        String timeString = dateFormat.format(new Date());
        pictureName = userName + "_" + timeString + ".jpg";
        Toast.makeText(getApplicationContext(), filePath.getPath() + File.separator + pictureName, Toast.LENGTH_SHORT).show();
        Log.v(TAG, "3 surface Texture: " + surfaceTexture);
        return new File(filePath.getPath() + File.separator + pictureName);
    }

    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        this.surfaceTexture = surfaceTexture;
    }

    public void setForegroundApp(String foregroundApp) {
        this.foregroundApp = foregroundApp;
    }

    public void setCapturingEvent(String capturingEvent) {
        this.capturingEvent = capturingEvent;
    }

    //public boolean isPictureTaken(){
    //    return pictureTaken;
    //}
}

class FaceDetectionListener implements Camera.FaceDetectionListener {

    private static final String TAG = "FaceDetectionListener";
    private List<Rect> faceRects;
    private List<Point> eyeAndMoutPoints;

    @Override
    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        if (faces.length > 0) {
            Log.v(TAG, "face(s) detected in preview: " + faces.length);

            faceRects = new ArrayList<Rect>();
            eyeAndMoutPoints = new ArrayList<Point>();


            for (int i=0; i<faces.length; i++) {
                Rect uRect = new Rect(faces[i].rect.left, faces[i].rect.top, faces[i].rect.right, faces[i].rect.bottom);
                faceRects.add(uRect);
                eyeAndMoutPoints.add(faces[i].leftEye);
                eyeAndMoutPoints.add(faces[i].rightEye);
                eyeAndMoutPoints.add(faces[i].mouth);

            }

        }
    }
}

}














/*
//SurfaceTexture queueBuffer: BufferQueue has been abandoned
public class CapturePicService extends IntentService {

    private static final String TAG = CapturePicService.class.getSimpleName();
    private Camera camera = null;
    private int cameraId;
    private String storagePath;
    private String userName;
    private SurfaceTexture surfaceTexture;

    public CapturePicService() {
        super("CapturePicService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            camera = getCameraInstance();
            storagePath = (String) intent.getExtras().get("storagePath");
            userName = (String) intent.getExtras().get("userName");

            surfaceTexture = new SurfaceTexture(0);
            try {
                camera.setPreviewTexture(surfaceTexture);
            } catch (IOException e) {
                e.printStackTrace();
            }
            capturePhoto((String) intent.getExtras().get("foregroundApp"), intent.getExtras().get("capturingEvent").toString());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, " could not be handled.");

        }
        onPause()

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "CapturePicService created.");
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "CapturePicService destroyed.");
    }

    //Search for the front facing camera
    private int findFrontFacingCam() {
        int camId = -2;

        int cameras = camera.getNumberOfCameras();
        for (int i = 0; i < cameras; i++) {
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

    //TODO: Test ob Camera anderweitig belegt
    public Camera getCameraInstance() {
        System.gc();
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
                Log.v(TAG, "Camera opened: " + c);
            }
        }
        return c;
    }

    private synchronized void capturePhoto(String foregroundApp, String capturingEvent) throws InterruptedException {
        Log.v(TAG, "capturePhoto() is called.");
        CameraPictureCallback pictureCallBack = this.new CameraPictureCallback(surfaceTexture, foregroundApp, capturingEvent);
        System.gc();
//        pictureCallBack.setSurfaceTexture(surfaceTexture);
//        pictureCallBack.setForegroundApp(foregroundApp);
//        pictureCallBack.setCapturingEvent(capturingEvent);

        Log.v(TAG, "1 camera: " + camera);
        camera.startPreview();
        Log.v(TAG, "2 camera: " + camera);

        //recording additional information provided by the camera
        //TODO: auf Displaygroesse mappen
        Camera.Parameters params = camera.getParameters();
        Log.v(TAG, "3 camera: " + camera);

        ////if (Build.VERSION.SDK_INT >= 17) {
            FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
                    .setTrackingEnabled(false)
                    .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                    .build();

            detector.setProcessor(
                    new MultiProcessor.Builder<Face>()
                            .build(new GraphicFaceTrackerFactory()));

        } else {
            if (params.getMaxNumDetectedFaces() > 0) {
                camera.setFaceDetectionListener(new FaceDetectionListener());

                camera.startFaceDetection();

            } else {
                Log.e(TAG, "Face detection is not supported.");
            }
        }//////


            //taking the picture
        System.gc();
        camera.takePicture(null, null, pictureCallBack);
        Log.v(TAG, "4 camera: " + camera);
        camera = null;
        Log.v(TAG, "5 camera: " + camera);

            //TODO: first Try abfangen

            //Face Data

            //if (capturingEvent.equals("test")){
            //  dataCollectionIntent.putExtra("firstTryOrNot", "yes");
            //}


        }



    public class CameraPictureCallback implements Camera.PictureCallback {

        boolean pictureTaken = false;
        String pictureName;
        private SurfaceTexture surfaceTexture;
        private String foregroundApp;
        private String capturingEvent;

        public CameraPictureCallback (SurfaceTexture surfaceTexture, String foregroundApp, String capturingEvent){
            this.surfaceTexture = surfaceTexture;
            this.foregroundApp = foregroundApp;
            this.capturingEvent = capturingEvent;
        }
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            System.gc();
            Log.v(TAG, "1 surface Texture: " + surfaceTexture);
            Log.v(TAG, "6 camera: " + camera);
            File capturedPhotoFile = getOutputMediaFile();
            if (capturedPhotoFile == null) {
                Log.e(TAG, "Could not create file");
                return;
            }

            Log.v(TAG, "4 surface Texture: " + surfaceTexture);
            Log.v(TAG, "7 camera: " + camera);
            Bitmap capturedImage = BitmapFactory.decodeByteArray(data, 0, data.length);
            Log.v(TAG, "5 surface Texture: " + surfaceTexture);
            Log.v(TAG, "8 camera: " + camera);
            android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(cameraId, info);
            int w = capturedImage.getWidth();
            int h = capturedImage.getHeight();
            Matrix mtx = new Matrix();
            mtx.postRotate(info.orientation);
            Bitmap rotatedImage = Bitmap.createBitmap(capturedImage, 0, 0, w, h, mtx, true);
            Log.v(TAG, "6 surface Texture: " + surfaceTexture);
            Log.v(TAG, "9 camera: " + camera);

            try {
                FileOutputStream fos = new FileOutputStream(capturedPhotoFile);
                rotatedImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.write(data);
                Log.v(TAG, "7 surface Texture: " + surfaceTexture);
                Log.v(TAG, "10 camera: " + camera);
                fos.flush();
                Log.v(TAG, "8 surface Texture: " + surfaceTexture);
                Log.v(TAG, "11 camera: " + camera);
                fos.close();
                Log.v(TAG, "9 surface Texture: " + surfaceTexture);
                Log.v(TAG, "12 camera: " + camera);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "File not found: " + e.getMessage());
                e.getStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "I/O error writing file: " + e.getMessage());
                e.getStackTrace();
            }

            Log.v(TAG, "10 surface Texture: " + surfaceTexture);
            Log.v(TAG, "13 camera: " + camera);
            Intent dataCollectionIntent = new Intent(getApplicationContext(), DataCollectorService.class);
            dataCollectionIntent.putExtra("foregroundApp", foregroundApp);
            dataCollectionIntent.putExtra("capturingEvent", capturingEvent);
            dataCollectionIntent.putExtra("photoName", pictureName);
            getApplicationContext().startService(dataCollectionIntent);
            Log.v(TAG, "11 surface Texture: " + surfaceTexture);
            Log.v(TAG, "14 camera: " + camera);
            Log.v(TAG, "data collection gets started.");
            camera.release();
        }

        //Hilfsmethode
        private File getOutputMediaFile() {
            Log.v(TAG, "getOutputMediaFile() called.");
            Log.v(TAG, "2 surface Texture: " + surfaceTexture);
            File filePath = new File(storagePath);
            Log.v(TAG, "File created.");
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy_HH:mm:ss");
            String timeString = dateFormat.format(new Date());
            pictureName = userName + "_" + timeString + ".jpg";
            Toast.makeText(getApplicationContext(), filePath.getPath() + File.separator + pictureName, Toast.LENGTH_SHORT).show();
            Log.v(TAG, "3 surface Texture: " + surfaceTexture);
            return new File(filePath.getPath() + File.separator + pictureName);
        }

        public SurfaceTexture getSurfaceTexture() {
            return surfaceTexture;
        }

        public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
            this.surfaceTexture = surfaceTexture;
        }

        public void setForegroundApp(String foregroundApp) {
            this.foregroundApp = foregroundApp;
        }

        public void setCapturingEvent(String capturingEvent) {
            this.capturingEvent = capturingEvent;
        }
    }

    class FaceDetectionListener implements Camera.FaceDetectionListener {

        private static final String TAG = "FaceDetectionListener";
        private List<Rect> faceRects;
        private List<Point> eyeAndMoutPoints;

        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            if (faces.length > 0) {
                Log.v(TAG, "face(s) detected in preview: " + faces.length);

                faceRects = new ArrayList<Rect>();
                eyeAndMoutPoints = new ArrayList<Point>();


                for (int i=0; i<faces.length; i++) {
                    Rect uRect = new Rect(faces[i].rect.left, faces[i].rect.top, faces[i].rect.right, faces[i].rect.bottom);
                    faceRects.add(uRect);
                    eyeAndMoutPoints.add(faces[i].leftEye);
                    eyeAndMoutPoints.add(faces[i].rightEye);
                    eyeAndMoutPoints.add(faces[i].mouth);

                }

            }
        }
    }

}
*/

