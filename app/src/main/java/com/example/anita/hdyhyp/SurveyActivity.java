package com.example.anita.hdyhyp;

import android.app.Fragment;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;

import static com.example.anita.hdyhyp.ControllerService.storage;

public class SurveyActivity extends FragmentActivity {
    private static final String TAG = SurveyActivity.class.getSimpleName();

    Button seePhotoButton;
    SurveyPictureFragment pictureFragment;

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

    RadioGroup surveyQuestionUserPositionRadioGroup;
    RadioButton surveyQuestionUserPositionRadioButtonTransit;
    RadioButton surveyQuestionUserPositionRadioButtonHome;
    RadioButton surveyQuestionUserPositionRadioButtonWork;
    RadioButton surveyQuestionUserPoitionRadioButtonOther;
    EditText surveyQuestionUserPoitionEditTextOther;

    RadioGroup surveyQuestionDoingSomethingRadioGroup;
    RadioButton surveyQuestionDoingSomethingRadioButtonTV;
    RadioButton surveyQuestionDoingSomethingRadioButtonEating;
    RadioButton surveyQuestionDoingSomethingRadioButtonWork;
    RadioButton surveyQuestionDoingSomethingRadioButtonOther;
    EditText surveyQuestionDoingSomethingEditTextOther;

    Button surveySubmitButton;

    String pictureName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        Intent intent = getIntent();
        int requestID = (int) intent.getExtras().get("requestID");
        final String pictureName = (String) intent.getExtras().get("pictureName");
        final String path = (String) intent.getExtras().get("path");

        Log.v(TAG, "request id: " + requestID);
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(requestID);

        seePhotoButton = (Button) findViewById(R.id.seePhotoButton);
        seePhotoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v(TAG, "see photo");
                Log.v(TAG, "path " + path);
                Log.v(TAG, "pictureName " + pictureName);
                pictureFragment = new SurveyPictureFragment();
                Bundle bundle = new Bundle();
                bundle.putString(SurveyPictureFragment.BUNDLEPATH, path);
                bundle.putString(SurveyPictureFragment.BUNDLEPICTURENAME, pictureName);
                pictureFragment.setArguments(bundle);
                FragmentTransaction
                        transaction =
                        getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayoutSurveyTapLong, pictureFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }

        });

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

        surveyQuestionUserPositionRadioGroup = (RadioGroup) findViewById(R.id.surveyQuestionUsersPositionRadioGroup);
        surveyQuestionUserPositionRadioButtonTransit = (RadioButton) findViewById(R.id.surveyQuestionUsersPositionRadioButtonTransit);
        surveyQuestionUserPositionRadioButtonHome = (RadioButton) findViewById(R.id.surveyQuestionUsersPositionRadioButtonHome);
        surveyQuestionUserPositionRadioButtonWork = (RadioButton) findViewById(R.id.surveyQuestionUsersPositionRadioButtonWork);
        surveyQuestionUserPoitionRadioButtonOther = (RadioButton) findViewById(R.id.surveyQuestionUsersPositionRadioButtonOther);
        surveyQuestionUserPoitionEditTextOther = (EditText) findViewById(R.id.surveyQuestionUsersPositionEditTextOther);

        surveyQuestionDoingSomethingRadioGroup  = (RadioGroup) findViewById(R.id.surveyQuestionDoingSomethingRadioGroup);
        surveyQuestionDoingSomethingRadioButtonTV  = (RadioButton) findViewById(R.id.surveyQuestionDoingSomethingRadioButtonTV);
        surveyQuestionDoingSomethingRadioButtonEating  = (RadioButton) findViewById(R.id.surveyQuestionDoingSomethingRadioButtonEating);
        surveyQuestionDoingSomethingRadioButtonWork  = (RadioButton) findViewById(R.id.surveyQuestionDoingSomethingRadioButtonWork);
        surveyQuestionDoingSomethingRadioButtonOther = (RadioButton) findViewById(R.id.surveyQuestionDoingSomethingRadioButtonOther);
        surveyQuestionDoingSomethingEditTextOther  = (EditText) findViewById(R.id.surveyQuestionDoingSomethingEditTextOther);

        surveySubmitButton = (Button) findViewById(R.id.surveySubmitButton);
        surveySubmitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int radioButtonDeviceID = surveyQuestionDevicePositionRadioGroup.getCheckedRadioButtonId();
                RadioButton rbDevice = (RadioButton) surveyQuestionDevicePositionRadioGroup.findViewById(radioButtonDeviceID);
                String surveyQuestionDevicePositionValue = (String) rbDevice.getText();
                Log.v(TAG, "devicePosition: " + surveyQuestionDevicePositionValue);

                int radioButtonHandID = surveyQuestionHandRadioGroup.getCheckedRadioButtonId();
                RadioButton rbHand = (RadioButton) surveyQuestionHandRadioGroup.findViewById(radioButtonHandID);
                String surveyQuestionHandValue = (String) rbHand.getText();
                Log.v(TAG, "hand: " + surveyQuestionHandValue);

                int radioButtonUserPostureID = surveyQuestionUserPostureRadioGroup.getCheckedRadioButtonId();
                RadioButton rbUserPosture = (RadioButton) surveyQuestionUserPostureRadioGroup.findViewById(radioButtonUserPostureID);
                String surveyQuestionUserPostureValue = (String) rbUserPosture.getText();
                Log.v(TAG, "userPosture: " + surveyQuestionUserPostureValue);

                int radioButtonUserPositionID = surveyQuestionUserPositionRadioGroup.getCheckedRadioButtonId();
                RadioButton rbUserPosition = (RadioButton) surveyQuestionUserPositionRadioGroup.findViewById(radioButtonUserPositionID);
                String surveyQuestionUserPositionValue = (String) rbUserPosition.getText();
                Log.v(TAG, "userPosition: " + surveyQuestionUserPositionValue);
                String surveyQuestionUserPositionOtherAnswerValue = surveyQuestionUserPoitionEditTextOther.getText().toString();
                //if (!surveyQuestionUserPoitionEditTextOther.getText().toString().isEmpty()){ };

                int radioButtonDoingSthID = surveyQuestionDoingSomethingRadioGroup.getCheckedRadioButtonId();
                RadioButton rbDoingSth = (RadioButton) surveyQuestionDoingSomethingRadioGroup.findViewById(radioButtonDoingSthID);
                String surveyQuestionDoingSomethingValue = (String) rbDoingSth.getText();
                Log.v(TAG, "userDoingSomething: " + surveyQuestionDoingSomethingValue);
                String surveyQuestionDoingSomethingOtherAnswerValue = surveyQuestionDoingSomethingEditTextOther.getText().toString();

                SurveyActivity.this.pictureName = (String) getIntent().getExtras().get("pictureName");
                Log.v(TAG, "survey was for picture : " + SurveyActivity.this.pictureName);
                ContentValues cv = new ContentValues();
                cv.put(Storage.COLUMN_PHOTO, SurveyActivity.this.pictureName);
                cv.put(Storage.COLUMN_DEVICEPOSITION, surveyQuestionDevicePositionValue);
                cv.put(Storage.COLUMN_HAND, surveyQuestionHandValue);
                cv.put(Storage.COLUMN_USERPOSTURE, surveyQuestionUserPostureValue);
                cv.put(Storage.COLUMN_USERPOSITION, surveyQuestionUserPositionValue);
                cv.put(Storage.COLUMN_USERPOSITIONOTEHRANSWERR, surveyQuestionUserPositionOtherAnswerValue);
                cv.put(Storage.COLUMN_DOINGSTH, surveyQuestionDoingSomethingValue);
                cv.put(Storage.COLUMN_DOINGSTHOTHERANSWER, surveyQuestionDoingSomethingOtherAnswerValue);

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

        surveyQuestionDoingSomethingRadioButtonOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((RadioButton) v).isChecked()) {
                    surveyQuestionDoingSomethingEditTextOther.setEnabled(true);
                }
                else {
                    surveyQuestionDoingSomethingEditTextOther.setEnabled(false);
                }
            }
        });

        surveyQuestionUserPoitionRadioButtonOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((RadioButton) v).isChecked()) {
                    surveyQuestionUserPoitionEditTextOther.setEnabled(true);
                }
                else {
                    surveyQuestionUserPoitionEditTextOther.setEnabled(false);
                }
            }
        });

    }
}
