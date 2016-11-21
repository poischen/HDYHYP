package com.example.anita.hdyhyp;

import android.app.IntentService;
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


public class CapturePicService extends IntentService {

    private static final String TAG = CapturePicService.class.getSimpleName();
    private Camera camera = null;
    private int cameraId;
    private String storagePath;
    private String userName;
    private SurfaceTexture surfaceTexture;
    private boolean onPictureTakenAlreadyFinished;
    private String pictureNameFromCallback;

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

    /*Search for the front facing camera*/
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

    private void capturePhoto(String foregroundApp, String capturingEvent) throws InterruptedException {
        Log.v(TAG, "capturePhoto() is called.");
        // Todo: Foto Orientierung da 13 +
        Log.v(TAG, "Camera object: " + camera);
        Camera.PictureCallback pictureCallBack = new CameraPictureCallback();
        camera.startPreview();

        //recording additional information provided by the camera
        //TODO: store data
        //TODO: auf Displaygroesse mappen
        Camera.Parameters params = camera.getParameters();

        /*if (Build.VERSION.SDK_INT >= 17) {
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
        }*/


            //taking the picture
        /* <p>This method is only valid when preview is active (after
        * {@link #startPreview()}).  Preview will be stopped after the picture is
        * taken; callers must call {@link #startPreview()} again if they want to
        * re-start preview or take more pictures.
        *
        * <p>After calling this method, you must not call {@link #startPreview()}
        * or take another picture until the JPEG callback has returned.
        */
            camera.takePicture(null, null, pictureCallBack);

            Log.v(TAG, "pictureNameFromCallback: " + pictureNameFromCallback);
            pictureNameFromCallback = null;
            camera = null;

            //recording sensor data and give everything else to the Storage
        //TODO: first Try abfangen da nullpointer
            Intent dataCollectionIntent = new Intent(this, DataCollectorService.class);
            dataCollectionIntent.putExtra("foregroundApp", foregroundApp);
            dataCollectionIntent.putExtra("capturingEvent", capturingEvent);
            //Bildname von PictureCallback
            //Face Data
            //TODO: Auf Picture Callback warten um Namen im Intent zu übergeben für die Tabelle -> evtl. dann Buffer Problem gelöst
            //if (capturingEvent.equals("test")){
            //  dataCollectionIntent.putExtra("firstTryOrNot", "yes");
            //}

            getApplicationContext().startService(dataCollectionIntent);
        }



    class CameraPictureCallback implements Camera.PictureCallback {
        String pictureName;
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            Log.v(TAG, "pictureCallBack created. " + "onPictureTaken() is called.");
// Todo: Foto Orientierung da 13 +

            File capturedPhotoFile = getOutputMediaFile();
            if (capturedPhotoFile == null) {
                Log.e(TAG, "Could not create file");
                return;
            }

            Bitmap capturedImage = BitmapFactory.decodeByteArray(data, 0, data.length);
            android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(cameraId, info);
            //Bitmap bitmap = rotate(realImage, info.orientation);
            int w = capturedImage.getWidth();
            int h = capturedImage.getHeight();
            Matrix mtx = new Matrix();
            mtx.postRotate(info.orientation);
            Bitmap rotatedImage = Bitmap.createBitmap(capturedImage, 0, 0, w, h, mtx, true);

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

/*
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
            }*/

            camera.release();
            pictureNameFromCallback = pictureName;
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

