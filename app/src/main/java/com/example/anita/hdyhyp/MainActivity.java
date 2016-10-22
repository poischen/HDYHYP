package com.example.anita.hdyhyp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

public class MainActivity extends AppCompatActivity {

    private TextView readyTextView;
    private Button submitButton;
    private EditText inputField;
    private TextView tellMeYourNameView;
    private Storage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storage = new Storage(getApplicationContext());

        readyTextView = (TextView) findViewById(R.id.statusTextView);
        submitButton = (Button) findViewById(R.id.submitButton);
        inputField = (EditText) findViewById(R.id.inputName);
        tellMeYourNameView = (TextView) findViewById(R.id.tellMeYourNameTextView);

        /**Asks for and inizilizes the users pseudonym to identify him during the study if it is not already set
          visual feedback / input not possible if the name is already set */

        if (storage.getUserName() == null){
            submitButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Log.v("MainActivity", "Submit Button clicked");
                    storeUserName(inputField.getText().toString());
                }
            });
        } else {
            userNameAlreadySet();
        }
    }

    /** Stores a pseudonym of the user for identifying him and naming the photos later */
    private void storeUserName(String input){
        try {
            storage.setUserName(input);
            Log.v("MainActivity", "Study user name read: " + input);
            Toast.makeText(this, "Thanks!", Toast.LENGTH_SHORT).show();
            userNameAlreadySet();
            Log.v("MainActivity", "Study user name successfully sored:" + input);
        } catch (NullPointerException e) {
            Toast.makeText(this, "No input. Please try again.", Toast.LENGTH_SHORT).show();
            Log.v("MainActivity", "Study user name not stored.");
        }
    };

    /** Loads the users pseudonym from the storage*/
    private String loadUserName() {
        String userName = storage.getUserName();
        Log.v("MainActivity", "Stored user Name: " + userName);
        return userName;
    }

    private void userNameAlreadySet(){
        submitButton.setEnabled(false);
        readyTextView.setText(R.string.ready);
        inputField.setEnabled(false);
        inputField.setText(storage.getUserName());
        tellMeYourNameView.setText("");
    }

}
