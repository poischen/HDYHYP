package com.example.anita.hdyhyp;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.graphics.Bitmap.createBitmap;

public class PictureReviewActivity extends AppCompatActivity {

    private static final String TAG = PictureReviewActivity.class.getSimpleName();

    private GridView gridView;
    private PictureReviewGridViewAdapter gridAdapter;
    private ArrayList<PictureItem> pictureItems;
    private ArrayList<PictureItem> taggedToDeleteItems = new ArrayList<>();
    private Button deleteButton;
    private Button submitButton;
    private Button logcatButton;
    private ProgressBar progressBar;
    private ProgressDialog uploadProgressDialog;

    /*private DropboxAPI<AndroidAuthSession> dropboxAPI;
    private final static String ACCESS_KEY = "8d7mhculhyjk8yf";
    private final static String ACCESS_SECRET = "4s5digo1cwmxmqe";
    private final static String DROPBOX_NAME = "dropbox_prefs";
    final static public Session.AccessType ACCESS_TYPE = Session.AccessType.DROPBOX;*/

    private Storage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ControllerService.pictureReviewAndUpload = true;
        setContentView(R.layout.activity_picture_review);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        //initDropboxSession();
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(41);
    }

    @Override
    protected void onStart(){
        super.onStart();
        ControllerService.pictureReviewAndUpload = true;
        storage = new  Storage(getApplicationContext());

        gridView = (GridView) findViewById(R.id.gridView);
        ArrayList<PictureItem> dummy = new ArrayList<PictureItem>();
        gridAdapter = new PictureReviewGridViewAdapter(getApplicationContext(), R.layout.picture_review_grid_item, dummy);
        gridView.setAdapter(gridAdapter);


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                PictureItem item = (PictureItem) parent.getItemAtPosition(position);
                Log.v(TAG, "Item clicked: " + item.getAbsolutePath());
                if (!item.isTaggedToDelete()){
                    item.setTaggedToDelete(true);
                    taggedToDeleteItems.add(item);
                } else {
                    item.setTaggedToDelete(false);
                    taggedToDeleteItems.remove(item);
                }
            }
        });


        deleteButton = (Button) findViewById(R.id.picturesDeleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int s = gridAdapter.getDataSize();
                for (int i=0; i < s; i++){
                    PictureItem currentItem = (PictureItem) gridAdapter.getItem(i);
                    if (currentItem.getCheckbox().isChecked()){
                        taggedToDeleteItems.add(currentItem);
                    }
                }

                for (int i=0; i < taggedToDeleteItems.size(); i++){
                    PictureItem currentItem = taggedToDeleteItems.get(i);
                    gridAdapter.remove(currentItem);
                    File file = new File(taggedToDeleteItems.get(i).getAbsolutePath());
                    file.delete();
                    pictureItems.remove(currentItem); //TODO: test
                    Log.v(TAG, "File deleted: " + file);
                }

                gridAdapter.notifyDataSetChanged();
                taggedToDeleteItems.clear();
            }
        });

        submitButton = (Button) findViewById(R.id.picturesSendMailButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.v(TAG, "dropboxAPI for fetching data to upload: " + dropboxAPI);
                //ListUploadDropboxFiles list = new ListUploadDropboxFiles(dropboxAPI, gridAdapter.getData(), handler);
                //list.execute();

                Intent mailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                mailIntent.setType("text/plain");
                mailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                        new String[]{"anita.baier@gmx.de"});
                mailIntent.putExtra(Intent.EXTRA_SUBJECT, storage.getUserName() + "'s photos");
                mailIntent.putExtra(Intent.EXTRA_TEXT, "Please find attached todays photos.");
                ArrayList<Uri> uris = new ArrayList<Uri>();
                //convert from paths to Android friendly Parcelable Uri's

                for (int i=0; i< pictureItems.size(); i++){
                   File file = new File(pictureItems.get(i).getAbsolutePath());
                    Uri u = Uri.fromFile(file);
                    uris.add(u);
                }

                mailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                mailIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(Intent.createChooser(mailIntent, "Send mail..."));

                //TODO: add logfile & database
                //TODO: Delete after send

            }
        });

        logcatButton = (Button) findViewById(R.id.logcatButton);
        logcatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Log.v(TAG, "dropboxAPI for fetching data to upload: " + dropboxAPI);
                ListUploadDropboxFiles list = new ListUploadDropboxFiles(dropboxAPI, null,
                        handler);
                list.execute();*/
            }
        });

        AsyncTaskBuildGrid stbg = new AsyncTaskBuildGrid();
        stbg.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ControllerService.pictureReviewAndUpload = true;

        /*if (!dropboxAPI.equals(null) && dropboxAPI.getSession().authenticationSuccessful()) {
            try {
                dropboxAPI.getSession().finishAuthentication();
                String accessToken = dropboxAPI.getSession().getOAuth2AccessToken();
                Log.d(TAG, "dropbox authentication successfull");

                //store keys
                SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, 0);
                SharedPreferences.Editor edit = prefs.edit();
                //edit.putString(ACCESS_KEY, "oauth2:");
                edit.putString(ACCESS_KEY, "cJRuLLohCSUAAAAAAABtJDh6U6eRQ4Z1CbdzyWpQBZjzlDs2bcxGj41ZJ-QFDnUZ");
                edit.putString(ACCESS_SECRET, accessToken);
                edit.commit();
                Log.d(TAG, "dropbox access token stored: oauth2 & " + accessToken);

            } catch (IllegalStateException e) {
                Log.d(TAG, "dropbox authentication failed");
            }
        }*/
    }

    @Override
    protected void onStop() {
        super.onStop();
        ControllerService.pictureReviewAndUpload = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ControllerService.pictureReviewAndUpload = false;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        ControllerService.pictureReviewAndUpload = true;
    }

    /*@Override
    protected void onPause() {
        super.onPause();
        ControllerService.pictureReviewAndUpload = true;
        Log.v(TAG, "onPause pictureReviewAndUpload: " + ControllerService.pictureReviewAndUpload);
    }*/

    private final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            ArrayList<String> result = msg.getData().getStringArrayList("data");
            for (String fileName : result) {
                Log.v(TAG, "Listed File: " + fileName);
            }
        }
    };

    /*private void initDropboxSession() {
        Log.v(TAG, "initialize Dropbox");
        // store app key and secret key
        AppKeyPair appKeys = new AppKeyPair(ACCESS_KEY, ACCESS_SECRET);
        AndroidAuthSession session;

        SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, 0);
        String[] keys = new String[2];
        keys[0] = prefs.getString(ACCESS_KEY, null);
        if (keys[0] == null){
            keys[0] = "null";
        }
        keys[1] = prefs.getString(ACCESS_SECRET, null);
        if (keys[1] == null){
            keys[1] = "null";
        }

        String accessKey = prefs.getString(ACCESS_KEY, null);
        Log.v(TAG, "stored accessKey: " + accessKey);
        String secretKey = prefs.getString(ACCESS_SECRET, null);
        Log.v(TAG, "stored secretKey: " + secretKey);

        //if (keys[0].equals("oauth2:")){
        if (keys[0].equals("cJRuLLohCSUAAAAAAABtJDh6U6eRQ4Z1CbdzyWpQBZjzlDs2bcxGj41ZJ-QFDnUZ")){
            Log.v(TAG, "accessKeys were stored");
            session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
            dropboxAPI = new DropboxAPI<AndroidAuthSession>(session);
        } else {
            Log.v(TAG, "accessKeys were not stored, start new session");
            AccessTokenPair accessToken = new AccessTokenPair(ACCESS_KEY,ACCESS_SECRET);
            session = new AndroidAuthSession(appKeys, ACCESS_TYPE, accessToken);
            dropboxAPI = new DropboxAPI<AndroidAuthSession>(session);

        }
        dropboxAPI.getSession().startOAuth2Authentication(PictureReviewActivity.this);
    }*/

    private void getData() {
        File folder = new File(storage.getStoragePath() + File.separator);
        File[] listOfFiles = folder.listFiles();

        try {
            for (File file : listOfFiles) {
            if (file.isFile()) {

            }
        }

        if (listOfFiles.length > 0) {
            //calculate scale
            Display display = getWindowManager().getDefaultDisplay();
            Point displaySize = new Point();
            display.getSize(displaySize);
            int displayWidth = displaySize.x;

            int width;
            int height;

            Bitmap picture = BitmapFactory.decodeFile(listOfFiles[0].getAbsolutePath());
            width = picture.getWidth();
            height = picture.getHeight();

            float scaledWidth = displayWidth / 3;
            float scalefactor = width / scaledWidth;
            float scaledHeight = height / scalefactor;
            int scaledWidthInt = (int) scaledWidth;
            int scaledHeightInt = (int) scaledHeight;

            //get pictures
            pictureItems = new ArrayList<PictureItem>();
            for (int i = 0; i < listOfFiles.length; i++) {

                Log.v(TAG, "Image: " + i + ": path: " + listOfFiles[i].getAbsolutePath());
                String currentPath = listOfFiles[i].getAbsolutePath();
                Bitmap currentPicture = BitmapFactory.decodeFile(listOfFiles[i].getAbsolutePath());

                Bitmap scaledPicture = Bitmap.createScaledBitmap(currentPicture, scaledWidthInt, scaledHeightInt, false);

                PictureItem pictureItem = new PictureItem(scaledPicture, currentPath);
                pictureItems.add(pictureItem);
                gridAdapter.addData(pictureItem);
            }
        }
    } catch (Exception e) {
            Log.d(TAG, "could not load files: " + e);
        }
    }

    private class AsyncTaskBuildGrid extends AsyncTask<String, String, String> {
        private ArrayList<PictureItem> data;
        private String resp;

        @Override
        protected String doInBackground(String... params) {
            getData();
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            gridAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(String... text) {

        }
    }


    class ListUploadDropboxFiles extends AsyncTask<Void, Void, ArrayList<String>> {

        private DropboxAPI<?> dropbox;
        private Handler handler;
        private ArrayList<PictureItem> dataList = new ArrayList<>();

        public ListUploadDropboxFiles(DropboxAPI<?> dropbox, ArrayList<PictureItem> dataList, Handler handler) {
            this.dropbox = dropbox;
            this.handler = handler;
            this.dataList = dataList;

            int datasize;
            if (dataList == null){
                datasize = 0;
            } else {
                datasize = dataList.size();
            }
            uploadProgressDialog = new ProgressDialog(PictureReviewActivity.this);
            uploadProgressDialog.setTitle("Uploading images to Dropbox");
            uploadProgressDialog.setMessage("Upload in progress... ");
            uploadProgressDialog.setProgressStyle(uploadProgressDialog.STYLE_HORIZONTAL);
            uploadProgressDialog.setProgress(0);
            uploadProgressDialog.setMax(datasize + 1);
            uploadProgressDialog.show();
        }

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            ArrayList<String> pictureAbsolutePaths = new ArrayList<String>();
            String userName = storage.getUserName();
            //logcat upload
            File logFile = null;
            try {
                logFile = new File("storage/emulated/0/HDYHYP/debug/");
                if (!logFile.exists()) {
                    logFile.mkdir();
                }
                Runtime.getRuntime().exec(
                        "logcat  -d -f " + logFile + File.separator
                                + "logcat"
                                + ".txt");
                Log.e(TAG, "logfile written");
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                File logcatFile = new File("storage/emulated/0/HDYHYP/debug/logcat.txt");

                FileInputStream inputStreamLogcat = new FileInputStream(logcatFile);
                dropbox.putFile((userName + File.separator + "storage/emulated/0/HDYHYP/debug/logcat.txt"), inputStreamLogcat,
                        logcatFile.length(), null, null);
                Log.v(TAG, "Logfile uploaded.");
                logcatFile.delete();
            } catch (FileNotFoundException e) {
                Log.v(TAG, "Logfile not found.");
                e.printStackTrace();
            } catch (DropboxException e) {
                Log.v(TAG, "Logfile could not be uploaded.");
                e.printStackTrace();
            }

            if (!(dataList == null)){
                //sql data upload
                String sqlPath = storage.getWritableDatabase().getPath();
                try {
                    Log.v(TAG, "SqlPath: " + sqlPath);
                    File sqlFile = new File(sqlPath);
                    FileInputStream inputStreamSQL = new FileInputStream(sqlFile);
                    dropbox.putFile((userName + File.separator + sqlPath), inputStreamSQL,
                            sqlFile.length(), null, null);
                    Log.v(TAG, "Database uploaded.");
                    uploadProgressDialog.incrementProgressBy(1);

                } catch (FileNotFoundException e) {
                    Log.v(TAG, "Database not found.");
                    e.printStackTrace();
                } catch (DropboxException e) {
                    Log.v(TAG, "Database could not be uploaded.");
                    e.printStackTrace();
                }

                //picture upload
                for(int i=0;i<dataList.size();i++)
                {
                    try {
                        String currentPath = dataList.get(i).getAbsolutePath();
                        File currentFile = new File(currentPath);
                        pictureAbsolutePaths.add(currentPath);
                        FileInputStream inputStream = new FileInputStream(currentFile);

                        // put picture to dropbox
                        dropbox.putFile((userName + File.separator + currentPath), inputStream,
                                currentFile.length(), null, null);
                        Log.v(TAG, "Picture uploaded.");
                        currentFile.delete();
                        currentFile = null;
                        currentPath = null;
                        inputStream = null;
                        uploadProgressDialog.incrementProgressBy(1);

                    } catch (Exception e){
                        Log.v(TAG, "Picture could not be uploaded.");
                        e.printStackTrace();
                    }
                }
            }

            uploadProgressDialog.dismiss();
            return pictureAbsolutePaths;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            if (result.isEmpty() == false){
                Message msgObj = handler.obtainMessage();
                Bundle b = new Bundle();
                b.putStringArrayList("data", result);
                msgObj.setData(b);
                handler.sendMessage(msgObj);
                Log.v(TAG, "collected all images to upload");
                gridAdapter.notifyDataSetChanged();
                gridAdapter.clear();
            } else {
                Log.v(TAG, "Failed to upload files");
            }
        }
    }

}