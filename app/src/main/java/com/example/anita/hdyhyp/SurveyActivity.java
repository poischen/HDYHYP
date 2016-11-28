package com.example.anita.hdyhyp;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import static com.example.anita.hdyhyp.ControllerService.storage;

public class SurveyActivity extends AppCompatActivity {
    private static final String TAG = SurveyActivity.class.getSimpleName();

    RadioGroup surveyQuestionDevicePositionRadioGroup;
    RadioButton surveyQuestionDevicePositionRadioButtonHands;
    RadioButton surveyQuestionDevicePositionRadioButtonSurface;

    RadioGroup surveyQuestionHandRadioGroup;
    RadioButton surveyQuestionHandRadioButtonDominant;
    RadioButton surveyQuestionHandRadioButtonNonDominant;
    RadioButton surveyQuestionHandRadioButtonBoth;
    RadioButton surveyQuestionHandRadioButtonNone;

    RadioGroup surveyQuestionUserPostureRadioGroup;
    RadioButton surveyQuestionUserPostureRadioButtonWalking;
    RadioButton surveyQuestionUserPostureRadioButtonSitting;
    RadioButton surveyQuestionUserPostureRadioButtonStaying;
    RadioButton surveyQuestionUserPostureRadioButtonLying;

    Button surveySubmitButton;

    String pictureName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        Intent intent = getIntent();
        int requestID = (int) intent.getExtras().get("requestID");

        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(requestID+42);

        //TODO: id des alarms speichern bzw auszulösendes Bild
        surveyQuestionDevicePositionRadioGroup = (RadioGroup) findViewById(R.id.surveyQuestionDevicePositionRadioGroup);
        surveyQuestionDevicePositionRadioButtonHands = (RadioButton) findViewById(R.id.surveyQuestionDevicePositionRadioButtonHands);
        surveyQuestionDevicePositionRadioButtonSurface = (RadioButton) findViewById(R.id.surveyQuestionDevicePositionRadioButtonSurface);


        surveyQuestionHandRadioGroup = (RadioGroup) findViewById(R.id.surveyQuestionHandRadioGroup);
        surveyQuestionHandRadioButtonDominant = (RadioButton) findViewById(R.id.surveyQuestionHandRadioButtonDominant);
        surveyQuestionHandRadioButtonNonDominant = (RadioButton) findViewById(R.id.surveyQuestionHandRadioButtonNonDominant);
        surveyQuestionHandRadioButtonBoth = (RadioButton) findViewById(R.id.surveyQuestionHandRadioButtonBoth);
        surveyQuestionHandRadioButtonNone = (RadioButton) findViewById(R.id.surveyQuestionHandRadioButtonNone);

        surveyQuestionUserPostureRadioGroup = (RadioGroup) findViewById(R.id.surveyQuestionUserPostureRadioGroup);
        surveyQuestionUserPostureRadioButtonWalking = (RadioButton) findViewById(R.id.surveyQuestionUserPostureRadioButtonWalking);
        surveyQuestionUserPostureRadioButtonSitting = (RadioButton) findViewById(R.id.surveyQuestionUserPostureRadioButtonSitting);
        surveyQuestionUserPostureRadioButtonStaying = (RadioButton) findViewById(R.id.surveyQuestionUserPostureRadioButtonStaying);
        surveyQuestionUserPostureRadioButtonLying = (RadioButton) findViewById(R.id.surveyQuestionUserPostureRadioButtonLying);

        surveySubmitButton = (Button) findViewById(R.id.surveySubmitButton);
        surveySubmitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RadioGroup rgDevice = (RadioGroup) findViewById(R.id.surveyQuestionDevicePositionRadioGroup);
                int radioButtonDeviceID = rgDevice.getCheckedRadioButtonId();
                RadioButton rbDevice = (RadioButton) rgDevice.findViewById(radioButtonDeviceID);
                String surveyQuestionDevicePositionValue = (String) rbDevice.getText();
                Log.v(TAG, "devicePosition: " + surveyQuestionDevicePositionValue);

                RadioGroup rgHand = (RadioGroup) findViewById(R.id.surveyQuestionHandRadioGroup);
                int radioButtonHandID = rgHand.getCheckedRadioButtonId();
                RadioButton rbHand = (RadioButton) rgHand.findViewById(radioButtonHandID);
                String surveyQuestionHandValue = (String) rbHand.getText();
                Log.v(TAG, "hand: " + surveyQuestionHandValue);

                RadioGroup rgUser = (RadioGroup) findViewById(R.id.surveyQuestionUserPostureRadioGroup);
                int radioButtonUserID = rgUser.getCheckedRadioButtonId();
                RadioButton rbUser = (RadioButton) rgUser.findViewById(radioButtonUserID);
                String surveyQuestionUserPostureValue = (String) rbUser.getText();
                Log.v(TAG, "userPosture: " + surveyQuestionUserPostureValue);

                pictureName = (String) getIntent().getExtras().get("pictureName");
                Log.v(TAG, "survey was for picture : " + pictureName);
                ContentValues cv = new ContentValues();
                cv.put(Storage.COLUMN_PHOTO, pictureName);
                cv.put(Storage.COLUMN_DEVICEPOSITION, surveyQuestionDevicePositionValue);
                cv.put(Storage.COLUMN_HAND, surveyQuestionHandValue);
                cv.put(Storage.COLUMN_USERPOSTURE, surveyQuestionUserPostureValue);

                SQLiteDatabase database = ControllerService.storage.getWritableDatabase();
                long insertId = database.insert(Storage.DB_TABLESURVEY, null, cv);
                Log.v(TAG, "survey data stored to db");
                database.close();

                finish();
            }
        });

        surveyQuestionDevicePositionRadioButtonSurface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((RadioButton) v).isChecked()) {
                    surveyQuestionHandRadioButtonNone.setChecked(true);
                }
                else {
                    surveyQuestionHandRadioButtonNone.setChecked(false);
                }
            }
        });

    }
}
