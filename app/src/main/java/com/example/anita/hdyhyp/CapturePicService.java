package com.example.anita.hdyhyp;

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
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.FormatterClosedException;
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
    private FaceDetectionListener faceDetectionListener;
    private String pictureName;

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
        Camera.Parameters params = camera.getParameters();

        if (params.getMaxNumDetectedFaces() > 0) {
            faceDetectionListener = new FaceDetectionListener();
            camera.setFaceDetectionListener(faceDetectionListener);
            camera.startFaceDetection();
        }

        //recording additional information provided by the camera
        //TODO: auf Displaygroesse mappen
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
    }

    public void finishCapturing(){
        String leftEyePoints = "n./a.";
        String rightEyePoints = "n./a.";
        String mouthPoints = "n./a.";

        if (faceDetectionListener != null){
            List<Point> leftEyePointsList = faceDetectionListener.getLeftEyePoints();
            List<Point> rightEyePointsList = faceDetectionListener.getRightEyePoints();
            List<Point> mouthPointsList = faceDetectionListener.getMouthPoints();

            if (leftEyePointsList != null){
                for (int i=0; i <= leftEyePointsList.size(); i++){
                    leftEyePoints = leftEyePoints + " "  + leftEyePointsList.get(i).toString();
                }
            }

            if (rightEyePointsList != null) {
                for (int i = 0; i <= rightEyePointsList.size(); i++) {
                    rightEyePoints = rightEyePoints + " " + rightEyePointsList.get(i).toString();
                }
            }

            if (mouthPointsList != null) {
                for (int i = 0; i <= mouthPointsList.size(); i++) {
                    mouthPoints = mouthPoints + " " + mouthPointsList.get(i).toString();
                }
            }

        }
        ControllerService.startDataCollectionService(getApplicationContext(), foregroundApp, capturingEvent, pictureName, leftEyePoints, rightEyePoints, mouthPoints);
        camera = null;
        pictureName = null;
        capturePhotoThread.interrupt();
        stopSelf();
    }

    public void setPictureName(String pictureName) {
        this.pictureName = pictureName;
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

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        File capturedPhotoFile = getOutputMediaFile();
        if (capturedPhotoFile == null) {
            Log.e(TAG, "Could not create file");
            return;
        }

        Bitmap capturedImage = BitmapFactory.decodeByteArray(data, 0, data.length);
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(camId, info);
        int w = capturedImage.getWidth();
        int h = capturedImage.getHeight();
        float scaleWidth = ((float) 50) / w;
        float scaleHeight = ((float) 50) / h;
        Matrix matrix = new Matrix();
        matrix.postRotate(info.orientation);
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap rotatedImage = Bitmap.createBitmap(capturedImage, 0, 0, w, h, matrix, true);

        try {
            FileOutputStream fos = new FileOutputStream(capturedPhotoFile);
            rotatedImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
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

        Log.v(TAG, "data collection gets started.");
        cps.setPictureName(pictureName);
        camera.release();
        //pictureTaken = true;
        cps.finishCapturing();
    }

    //Hilfsmethode
    private File getOutputMediaFile() {
        Log.v(TAG, "getOutputMediaFile() called.");
        File filePath = new File(storagePath);
        Log.v(TAG, "File created.");
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy_HH:mm:ss");
        String timeString = dateFormat.format(new Date());
        pictureName = userName + "_" + timeString + ".jpg";
        Toast.makeText(getApplicationContext(), filePath.getPath() + File.separator + pictureName, Toast.LENGTH_SHORT).show();
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
    private List<Point> leftEyePoints;
    private List<Point> rightEyePoints;
    private List<Point> mouthPoints;

    @Override
    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        if (faces.length > 0) {
            Log.v(TAG, "face(s) detected in preview: " + faces.length);

            faceRects = new ArrayList<Rect>();
            leftEyePoints = new ArrayList<Point>();
            rightEyePoints = new ArrayList<Point>();
            mouthPoints = new ArrayList<Point>();


            for (int i=0; i<faces.length; i++) {
                Rect uRect = new Rect(faces[i].rect.left, faces[i].rect.top, faces[i].rect.right, faces[i].rect.bottom);
                faceRects.add(uRect);
                leftEyePoints.add(faces[i].leftEye);
                rightEyePoints.add(faces[i].rightEye);
                mouthPoints.add(faces[i].mouth);

            }

        }
    }

    public List<Point> getLeftEyePoints() {
        return leftEyePoints;
    }

    public List<Point> getRightEyePoints() {
        return rightEyePoints;
    }

    public List<Point> getMouthPoints() {
        return mouthPoints;
    }
}

}