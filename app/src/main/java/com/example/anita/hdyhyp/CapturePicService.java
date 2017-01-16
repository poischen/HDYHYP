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
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.android.gms.vision.face.FaceDetector.FAST_MODE;


public class CapturePicService extends Service {

    private static final String TAG = CapturePicService.class.getSimpleName();
    private static final String NASTRING = "n./a.";

    private int camId = -2;
    private Camera camera = null;
    private String storagePath;
    private String userName;
    private SurfaceTexture surfaceTexture;
    private ControllerService.CapturingEvent capturingEvent;
    private String foregroundApp;
    //private FaceDetectionListener faceDetectionListener;
    private String pictureName;
    private FaceDetector faceDetector;
    private SparseArray<Face> faces;

    public CapturePicService()
    {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ControllerService.pictureIsCurrentlyTaken = true;
        try {
            capturingEvent = (ControllerService.CapturingEvent) intent.getExtras().get(DataCollectorService.CAPTURINGEVENT);
            foregroundApp = (String) intent.getExtras().get("foregroundApp");
        } catch (Exception e) {
            Log.d(TAG, "intent extras were empty");
        }
        try {
            camera = getCameraInstance();
            surfaceTexture = new SurfaceTexture(0);


            faceDetector = new FaceDetector.Builder(getApplicationContext())
                    .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                    .setMode(FAST_MODE)
                    .setTrackingEnabled(false)
                    .build();
            try {
                camera.setPreviewTexture(surfaceTexture);
            } catch (IOException e) {
                e.printStackTrace();
            }
            capturePhoto();

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, " could not be handled.");

        }

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
       /* faceDetector = new FaceDetector.Builder(getApplicationContext())
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FAST_MODE)
                .setTrackingEnabled(false)
                .build();*/
        Storage storage = new Storage(getApplicationContext());
        storagePath = storage.getStoragePath();
        userName = storage.getUserName();
        findFrontFacingCam();
        Log.v(TAG, "CapturePicService created.");
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "CapturePicService destroyed.");
    }

    //Search for the front facing camera
    private void findFrontFacingCam() {
        if (camId == -2) {
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
    }

    //initialize camera
    public Camera getCameraInstance() {
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

    //creates the CameraPictureCallback, starts the camera preview and let the picture be taken
    private synchronized void capturePhoto() throws InterruptedException {
        Log.v(TAG, "capturePhoto() is called.");
        //pictureIsCurrentlyTaken = true;

        CameraPictureCallback pictureCallBack = new CameraPictureCallback(this);
        camera.startPreview();

        /*if (Build.VERSION.SDK_INT < 17) {
            Camera.Parameters params = camera.getParameters();
            if (params.getMaxNumDetectedFaces() > 0) {
                camera.setFaceDetectionListener(new FaceDetectionListener());
                camera.startFaceDetection();
            } else {
                Log.e(TAG, "Face detection is not supported.");
            }
        }*/

        //taking the picture
        camera.takePicture(null, null, pictureCallBack);
        ControllerService.pictureIsCurrentlyTaken = false;
    }

    /* after picture was taken, the method detects face landmarks in the detected faces, which should be stored in the surveys database,
    * it releases the camera and the detector and starts the DataCollectorService
    */
    public void finishCapturing(){

        //detect face landmarks with google play service
        String leftEyePoints = NASTRING;
        String rightEyePoints = NASTRING;
        String mouthPoints = NASTRING;

        /*int facesSize = faces.size();
        if (facesSize != 0) {
            for (int i = 0; i < facesSize; i++) {
                Face face = faces.valueAt(i);
                Log.v(TAG, "found landmarks on face: " + face.getLandmarks().size());
                for (Landmark landmark : face.getLandmarks()) {
                    switch (landmark.getType()) {
                        case Landmark.LEFT_EYE:
                            leftEyePoints = leftEyePoints + i + ".) x: " + landmark.getPosition().x + " y: " + landmark.getPosition() + ", ";
                            break;
                        case Landmark.RIGHT_EYE:
                            rightEyePoints = rightEyePoints + i + ".) x: " + landmark.getPosition().x + " y: " + landmark.getPosition() + ", ";
                            break;
                        case Landmark.BOTTOM_MOUTH:
                            mouthPoints = mouthPoints + i + ".) x: " + landmark.getPosition().x + " y: " + landmark.getPosition() + ", ";
                    }
                }
            }
            faces = null;

        }*/

        //unused due to decreased api level
        /*else if (faceDetectionListener != null){
            List<Point> leftEyePointsList = faceDetectionListener.getLeftEyePoints();
            List<Point> rightEyePointsList = faceDetectionListener.getRightEyePoints();
            List<Point> mouthPointsList = faceDetectionListener.getMouthPoints();

            if (leftEyePointsList != null){
                for (int i=0; i <= leftEyePointsList.size(); i++){
                    leftEyePoints = leftEyePoints + i + ".: "  + leftEyePointsList.get(i).toString() +  ", ";
                }
            }
            if (rightEyePointsList != null) {
                for (int i = 0; i <= rightEyePointsList.size(); i++) {
                    rightEyePoints = rightEyePoints + i + ".: "  + rightEyePointsList.get(i).toString() + ", " ;
                }
            }
            if (mouthPointsList != null) {
                for (int i = 0; i <= mouthPointsList.size(); i++) {
                    mouthPoints = mouthPoints + i + ".: "  + mouthPointsList.get(i).toString() + ", ";
                }
            }
            camera.stopFaceDetection();
        }*/

        camera.release();
        //faceDetector.release();
        ControllerService.pictureIsCurrentlyTaken = false;
        ControllerService.startDataCollectionService("collect", getApplicationContext(), foregroundApp, capturingEvent, pictureName, leftEyePoints, rightEyePoints, mouthPoints);
        camera = null;
        pictureName = null;
    }


    public void setPictureName(String pictureName) {
        this.pictureName = pictureName;
    }

    //processes the picture while taking the photo and detects the faces on the bitmap, calls finishCapturing() after processing is done
    public class CameraPictureCallback implements Camera.PictureCallback {

        private String pictureName;
        private CapturePicService cps;

        public CameraPictureCallback (CapturePicService cps){
            this.cps = cps;
        }

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            ControllerService.pictureIsCurrentlyTaken = true;
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
            Matrix matrix = new Matrix();
            matrix.postRotate(info.orientation);
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

            //detect faces on the bitmap with google play service
            if (faceDetector.isOperational()) {
                Log.v(TAG, "face detector is operational");
                Frame frame = new Frame.Builder().setBitmap(rotatedImage).build();
                //faces = faceDetector.detect(frame); -> java.lang.RuntimeException: Cannot use detector after release()
                //Log.v(TAG, "face detector detected number of faces: " + faces.size());
            } else {
                Log.v(TAG, "face detector is not operational.");
            }
            cps.setPictureName(pictureName);
            cps.finishCapturing();
        }

        //helpermethod
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

    //unused due to increased api level
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