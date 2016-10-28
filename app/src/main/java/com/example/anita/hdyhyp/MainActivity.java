package com.example.anita.hdyhyp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView readyTextView;
    private Button submitButton;
    private EditText inputField;
    private TextView tellMeYourNameView;
    private TextView helloTextView;
    private StorageController storage;

    private Button deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storage = new StorageController(getApplicationContext());

        readyTextView = (TextView) findViewById(R.id.statusTextView);
        submitButton = (Button) findViewById(R.id.submitButton);
        inputField = (EditText) findViewById(R.id.inputName);
        tellMeYourNameView = (TextView) findViewById(R.id.tellMeYourNameTextView);
        helloTextView = (TextView) findViewById(R.id.helloTextView);

        /**Asks for and inizilizes the users pseudonym to identify him during the study if it is not already set
          visual feedback / input not possible if the name is already set */
        if (storage.getUserName() == null){
            submitButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Log.v(TAG, "Submit Button clicked");
                    storeUserName(inputField.getText().toString());
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
                inputField.setEnabled(true);
                inputField.setText("");
                tellMeYourNameView.setEnabled(true);
                submitButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Log.v(TAG, "Submit Button clicked");
                        storeUserName(inputField.getText().toString());
                    }
                });
            }
        });


    }

    /** Stores a pseudonym of the user for identifying him and naming the photos later */
    private void storeUserName(String input){
        try {
            storage.setUserName(input);
            Log.v(TAG, "Study user name read: " + input);
            Toast.makeText(this, "Thanks!", Toast.LENGTH_SHORT).show();
            userNameAlreadySet();
            Log.v(TAG, "Study user name successfully stored:" + input);
        } catch (NullPointerException e) {
            Toast.makeText(this, "No input. Please try again.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Study user name not stored.");
        }

        /**Starting the Shooting after inizialising the user name in order to test if everything works
         * TODO: in Controller auslagern, testen ob es passt, sonst Controller Service wieder schließen
         */
        Intent capturePicServiceIntent = new Intent(this, CapturePicService.class);
        capturePicServiceIntent.putExtra("path", storage.getStoragePath());
        capturePicServiceIntent.putExtra("userName", storage.getUserName());
        startService(capturePicServiceIntent);

        /*Starts the longlasting Service which Controlls the Data Collection and the Photo Shooting
         */

        Intent controllerIntent = new Intent(this, ControllerService.class);
        getApplicationContext().startService(controllerIntent);
    }

    /** Loads the users pseudonym from the storage*/
    private String loadUserName() {
        String userName = storage.getUserName();
        Log.v(TAG, "Stored user Name: " + userName);
        return userName;
    }

    /**Helper method to give the user feedback, if the name is already set*/
    private void userNameAlreadySet(){
        String username = storage.getUserName();
        submitButton.setEnabled(false);
        helloTextView.setText("Hello " + username + "!");
        readyTextView.setText(R.string.ready);
        inputField.setEnabled(false);
        inputField.setText(username);
        tellMeYourNameView.setEnabled(false);
    }

}
