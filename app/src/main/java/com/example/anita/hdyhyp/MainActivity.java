package com.example.anita.hdyhyp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.Serializable;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView readyTextView;
    private Button submitButton;
    private TextView tellMeYourNameView;
    private TextView helloTextView;
    private Storage storage;
    private Spinner namesSpinner;
    private Button settingsButton1;
    private Button settingsButton2;
    private Button reviewButton;
    private Button deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storage = new Storage(getApplicationContext());

        readyTextView = (TextView) findViewById(R.id.statusTextView);
        submitButton = (Button) findViewById(R.id.submitButton);
        namesSpinner = (Spinner) findViewById(R.id.spinnerNames);
        tellMeYourNameView = (TextView) findViewById(R.id.tellMeYourNameTextView);
        helloTextView = (TextView) findViewById(R.id.helloTextView);
        settingsButton1 = (Button) findViewById(R.id.buttonSettings1);
        settingsButton2 = (Button) findViewById(R.id.buttonSettings2);
        reviewButton = (Button) findViewById(R.id.reviewButton);

        settingsButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //get permission reading information about current running apps
                Intent settingsIntent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(settingsIntent);
            }
        });

        settingsButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //get camera and storage permission
                Intent permissionIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                permissionIntent.setData(uri);
                startActivity(permissionIntent);
            }
        });

        reviewButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PictureReviewActivity.class);
                startActivity(intent);
            }
        });



        /**Asks for and inizilizes the users pseudonym to identify him during the study if it is not already set
         visual feedback / input not possible if the name is already set */
        if (storage.getUserName() == null) {
            //update UI
            submitButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Log.v(TAG, "Submit Button clicked");
                    storeUserName(namesSpinner.getSelectedItem().toString());
                }
            });
        } else {
            userNameAlreadySet();
        }

        /**Temporary possibility to delete the user name
         * TODO: remove or hide for user
         */
        deleteButton = (Button) findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                storage.deleteUserName();
                submitButton.setEnabled(true);
                helloTextView.setText(R.string.hello);
                readyTextView.setText(R.string.waiting);
                namesSpinner.setEnabled(true);
                tellMeYourNameView.setEnabled(true);
                submitButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Log.v(TAG, "Submit Button clicked");
                        storeUserName(namesSpinner.getSelectedItem().toString());
                    }
                });
            }
        });

    }

    /**
     * Stores a pseudonym of the user for identifying him and naming the photos after him
     */
    private void storeUserName(String input) {
        try {
            storage.setUserName(getApplicationContext(), input);
            Log.v(TAG, "Study user name read: " + input);
            Toast.makeText(this, "Thanks!", Toast.LENGTH_SHORT).show();
            userNameAlreadySet();
            Log.v(TAG, "Study user name successfully stored:" + input);
        } catch (NullPointerException e) {
            Toast.makeText(this, "No input. Please try again.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Study user name not stored.");
        }


        /*Starts a longlasting Service which controlls the Data Collection and the Photo Shooting
         */

        Intent controllerIntent = new Intent(this, ControllerService.class);
        controllerIntent.putExtra("storagePath", storage.getStoragePath());
        controllerIntent.putExtra("userName", storage.getUserName());
        getApplicationContext().startService(controllerIntent);


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
        submitButton.setEnabled(false);
        helloTextView.setText("Hello " + username + "!");
        readyTextView.setText(R.string.ready);
        namesSpinner.setEnabled(false);
        tellMeYourNameView.setEnabled(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
