package com.example.anita.hdyhyp;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView readyTextView;
    private Button startButton;
    private TextView tellMeYourNameView;
    private TextView helloTextView;
    private Storage storage;
    private Spinner namesSpinner;
    private Button settingsButton1;
    private Button settingsButton2;
    private Button settingsButton3;
    private Button reviewButton;
    private Button deleteButton;

    private Button sendMailButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storage = new Storage(getApplicationContext());

        readyTextView = (TextView) findViewById(R.id.statusTextView);
        startButton = (Button) findViewById(R.id.submitButton);
        namesSpinner = (Spinner) findViewById(R.id.spinnerNames);
        tellMeYourNameView = (TextView) findViewById(R.id.tellMeYourNameTextView);
        helloTextView = (TextView) findViewById(R.id.helloTextView);
        settingsButton1 = (Button) findViewById(R.id.buttonSettings1);
        settingsButton2 = (Button) findViewById(R.id.buttonSettings2);
        settingsButton3 = (Button) findViewById(R.id.buttonSettings3);
        reviewButton = (Button) findViewById(R.id.reviewButton);
        sendMailButton = (Button) findViewById(R.id.logSendMailButton);


        settingsButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //get permission for usage stats
                Intent settingsIntent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(settingsIntent);
            }
        });

        settingsButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //get camera, storage and location permission
                Intent permissionIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                permissionIntent.setData(uri);
                startActivity(permissionIntent);
            }
        });

        settingsButton3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //get notification reading permissions
                Intent permissionIntent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                startActivity(permissionIntent);
            }
        });

        reviewButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PictureReviewActivity.class);
                startActivity(intent);
            }
        });


        //send logfile via mail
        sendMailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.v(TAG, "dropboxAPI for fetching data to upload: " + dropboxAPI);
                //ListUploadDropboxFiles list = new ListUploadDropboxFiles(dropboxAPI, gridAdapter.getData(), handler);
                //list.execute();
                Intent mailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                mailIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                mailIntent.setType("text/plain");
                mailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                        new String[]{"anita.baier@campus.lmu.de"});
                mailIntent.putExtra(Intent.EXTRA_SUBJECT, storage.getUserName() + "'s logfile");
                ArrayList<CharSequence> text = new ArrayList<CharSequence>();
                text.add("Please find attached the latest logfile.");
                mailIntent.putExtra(Intent.EXTRA_TEXT, text);
                ArrayList<Uri> uris = new ArrayList<Uri>();
                /*for (int i=0; i< pictureItems.size(); i++){
                   File picturefile = new File(pictureItems.get(i).getAbsolutePath());
                    Uri u = Uri.fromFile(picturefile);
                    uris.add(u);
                }*/
                File logfile = new File(createLogcat());
                //File databaseFile = new File (getDatabase());
                Uri u = Uri.fromFile(logfile);
                uris.add(u);
                //u = Uri.fromFile(databaseFile);
                //uris.add(u);

                mailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                Intent shareIntent = Intent.createChooser(mailIntent, "Send mail...");
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(shareIntent);
            }
        });

        /**Asks for and inizilizes the users pseudonym to identify him during the study if it is not already set
         visual feedback / input not possible if the name is already set
         if the username is stored, but the Controller Service is not running, restart the service */
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v(TAG, "Submit Button clicked");
                if (storage.getUserName() == null){
                    storeUserName(namesSpinner.getSelectedItem().toString());
                }
                if (!(storage.isServiceRunning(getApplicationContext(), ControllerService.class.getName()))){
                    startControllerService();
                    readyTextView.setText("App is ready and running.");
                }
            }
        });

        //give feedback, if a userName is already stored and restart the service, if it is not running although a username is already set
        if (!(storage.getUserName() == null)) {
            userNameAlreadySet();
            if (!(storage.isServiceRunning(getApplicationContext(), ControllerService.class.getName()))){
                readyTextView.setText("App is ready but not running, please start.");
            }
        }

        /* stop controller service and delete user name
         */
        deleteButton = (Button) findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                storage.deleteUserName();
                helloTextView.setText(R.string.hello);
                readyTextView.setText(R.string.waiting);
                namesSpinner.setEnabled(true);
                tellMeYourNameView.setEnabled(true);

                //cancel Service if running
                //if (storage.isServiceRunning(getApplicationContext(), ControllerService.class.getName())){
                Intent controllerIntent = new Intent(getApplicationContext(), ControllerService.class);
                stopService(controllerIntent);
                Intent dataIntent = new Intent(getApplicationContext(), DataCollectorService.class);
                stopService(dataIntent);

                //}

            }
        });



    }

    /**
     * Stores a pseudonym of the user for identifying him and naming the photos after him
     */
    private void storeUserName(String input) {
        try {

            //get index of spinner
            int index = 0;
            for (int i=0;i<namesSpinner.getCount();i++){
                if (namesSpinner.getItemAtPosition(i).toString().equalsIgnoreCase(input)){
                    index = i;
                    break;
                }
            }
            storage.setUserName(getApplicationContext(), input, index);
            Log.v(TAG, "Study user name read: " + input);
            Toast.makeText(this, "Thanks!", Toast.LENGTH_SHORT).show();
            userNameAlreadySet();
            Log.v(TAG, "Study user name successfully stored:" + input);
        } catch (NullPointerException e) {
            Toast.makeText(this, "No input. Please try again.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Study user name not stored.");
        }

    }

    /**
     * Loads the users pseudonym from the storage
     */
    private String loadUserName() {
        String userName = storage.getUserName();
        Log.v(TAG, "Stored user Name: " + userName);
        return userName;
    }

    /**
     * Helper method to give the user feedback, if the name is already set
     */
    private void userNameAlreadySet() {
        String username = storage.getUserName();
        namesSpinner.setSelection(storage.getUserNameIndex());
        helloTextView.setText("Hello " + username + "!");
        readyTextView.setText(R.string.ready);
        namesSpinner.setEnabled(false);
        tellMeYourNameView.setEnabled(false);
    }

    private String createLogcat(){
        File logFile = null;
        String path =null;
        try {
            logFile = new File(storage.STORAGEPATHLOG);
            if (!logFile.exists()) {
                logFile.mkdirs();
            }

            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy_HH-mm-ss");
            String timeString = dateFormat.format(new Date());
            path = File.separator
                    + storage.getUserName()
                    + "_logcat_"
                    + timeString
                    + ".txt";
            Runtime.getRuntime().exec(
                    "logcat  -d -f " + logFile + path);
            Log.e(TAG, "logfile written");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return (storage.STORAGEPATHLOG + path);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /*Starts a longlasting Service which controlls the Data Collection and the Photo Shooting
 */
    private void startControllerService(){
        Intent controllerIntent = new Intent(this, ControllerService.class);
        getApplicationContext().startService(controllerIntent);
    }
}
