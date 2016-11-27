package com.example.anita.hdyhyp;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

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
                int devicePositionID = surveyQuestionDevicePositionRadioGroup.getCheckedRadioButtonId();
                Log.v(TAG, "devicePositionID: " + devicePositionID);
                int handID = surveyQuestionHandRadioGroup.getCheckedRadioButtonId();
                Log.v(TAG, "handID: "  + handID);
                int userPostureID = surveyQuestionUserPostureRadioGroup.getCheckedRadioButtonId();
                Log.v(TAG, "userPostureID: "  + userPostureID);
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
