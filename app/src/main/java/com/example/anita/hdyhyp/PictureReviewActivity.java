package com.example.anita.hdyhyp;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;
import com.dropbox.client2.session.TokenPair;

import java.io.File;
import java.util.ArrayList;

import static com.example.anita.hdyhyp.ControllerService.storage;

public class PictureReviewActivity extends AppCompatActivity {

    private static final String TAG = PictureReviewActivity.class.getSimpleName();

    private GridView gridView;
    private PictureReviewGridViewAdapter gridAdapter;
    private ArrayList<PictureItem> pictureItems;
    private ArrayList<PictureItem> taggedToDeleteItems = new ArrayList<>();
    private Button deleteButton;
    private Button submitButton;
    private ProgressBar progressBar;

    private DropboxAPI<AndroidAuthSession> dropbox;
    //private final static String FILE_DIR = "/HDYHYP/" + storage.getUserName() + "/";
    private final static String FILE_DIR = storage.getStoragePath();
    private final static String DROPBOX_NAME = "dropbox_prefs";
    private final static String ACCESS_KEY = "8d7mhculhyjk8yf";
    private final static String ACCESS_SECRET = "4s5digo1cwmxmqe";
    private boolean isLoggedIn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_review);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //dropbox-----------------------------------------------------------------------------------
        //loggedIn(false);
        AndroidAuthSession session;
        AppKeyPair pair = new AppKeyPair(ACCESS_KEY, ACCESS_SECRET);

        SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, 0);
        String key = prefs.getString(ACCESS_KEY, null);
        String secret = prefs.getString(ACCESS_SECRET, null);

        if (key != null && secret != null) {
            AccessTokenPair token = new AccessTokenPair(key, secret);
            session = new AndroidAuthSession(pair, Session.AccessType.APP_FOLDER, token);
        } else {
            session = new AndroidAuthSession(pair, Session.AccessType.APP_FOLDER);
            Log.v(TAG, "new dropbox session");
        }
        dropbox = new DropboxAPI<AndroidAuthSession>(session);

    }

    @Override
    protected void onStart(){
        super.onStart();

        gridView = (GridView) findViewById(R.id.gridView);
        ArrayList<PictureItem> dummy = new ArrayList<PictureItem>();
        gridAdapter = new PictureReviewGridViewAdapter(getApplicationContext(), R.layout.picture_review_grid_item, dummy);
        gridView.setAdapter(gridAdapter);


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                PictureItem item = (PictureItem) parent.getItemAtPosition(position);
                Log.v(TAG, "Item clicked: " + item.getPath());
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

                ArrayList<PictureItem> pictureItems;
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
                    File file = new File(taggedToDeleteItems.get(i).getPath());
                    file.delete();
                    Log.v(TAG, "File deleted: " + file);
                }

                gridAdapter.notifyDataSetChanged();
                taggedToDeleteItems.clear();
            }
        });

        submitButton = (Button) findViewById(R.id.picturesSendButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (isLoggedIn) {
                //    dropbox.getSession().unlink();
                    //loggedIn(false);
                //} else {
                //    dropbox.getSession().startAuthentication(DropboxActivity.this);
                //}

                //Login
                try {
                    dropbox.getSession().unlink();
                } catch (Exception e){
                    dropbox.getSession().startAuthentication(PictureReviewActivity.this);
                }

                //fetch data to store - pictures & todo: SQLite db
                ListDropboxFiles list = new ListDropboxFiles(dropbox, FILE_DIR,
                        handler);
                list.execute();
                //TODO: erst ausführen wenn fertig?
                //upload data
                UploadFileToDropbox upload = new UploadFileToDropbox(getApplicationContext(), dropbox,
                        FILE_DIR);
                upload.execute();
            }
        });

        AsyncTaskBuildGrid stbg = new AsyncTaskBuildGrid();
        stbg.execute();

    }

    @Override
    protected void onResume() {
        super.onResume();

        AndroidAuthSession session = dropbox.getSession();
        if (session.authenticationSuccessful()) {
            try {
                session.finishAuthentication();
                TokenPair tokens = session.getAccessTokenPair();
                SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(ACCESS_KEY, tokens.key);
                editor.putString(ACCESS_SECRET, tokens.secret);
                editor.commit();
                //loggedIn(true);
            } catch (IllegalStateException e) {
                Toast.makeText(this, "Error during Dropbox authentication",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*public void loggedIn(boolean isLogged) {
        isLoggedIn = isLogged;
        uploadFile.setEnabled(isLogged);
        listFiles.setEnabled(isLogged);
    }*/

    private final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            ArrayList<String> result = msg.getData().getStringArrayList("data");
            for (String fileName : result) {
                Log.v(TAG, "Listed File: " + fileName);
            }
        }
    };


    private void getData(){
        File folder = new File(storage.getStoragePath() + File.separator);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                System.out.println(file.getName());
            }
        }

        pictureItems = new ArrayList<PictureItem>();
        for(int i=0;i<=listOfFiles.length;i++)
        {
            Log.v("Image: "+i+": path", listOfFiles[i].getAbsolutePath());

            String currentPath = listOfFiles[i].getAbsolutePath();
            Bitmap currentPicture = BitmapFactory.decodeFile(listOfFiles[i].getAbsolutePath());

            int width = currentPicture.getWidth();
            int height = currentPicture.getHeight();
            Matrix matrix = new Matrix();
            float scaleWidth = ((float) 50) / width;
            float scaleHeight = ((float) 50) / height;
            matrix.postScale(scaleWidth, scaleHeight);
            Bitmap scaledPicture = Bitmap.createBitmap(currentPicture, 0, 0, width, height , matrix, false);

            PictureItem pictureItem = new PictureItem(scaledPicture, currentPath);
            pictureItems.add(pictureItem);
            gridAdapter.addData(pictureItem);

        }

        //return pictureItems;
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
            // Things to be done before execution of long running operation. For
            // example showing ProgessDialog
        }

        @Override
        protected void onProgressUpdate(String... text) {

        }
    }
}