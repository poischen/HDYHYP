package com.example.anita.hdyhyp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class SurveyPictureFragment extends Fragment {
    private static final String TAG = SurveyPictureFragment.class.getSimpleName();
    public static final String BUNDLEPATH = "path";
    public static final String BUNDLEPICTURENAME = "pictureName";
    public static final String BUNDLEDISPLAYWITH = "displayWidth";
    public static final String NOPICTAKEN = "no Picture was taken";

    ImageView imageViewUsersPhoto;
    TextView textViewDate;

    String path;
    String pictureName;
    int displayWidth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            //restore picture and date
        }
        View myView = inflater.inflate(R.layout.fragment_show_picture,
                container, false);

        Bundle args = getArguments();
        if (args != null){
            path = args.getString(BUNDLEPATH);
            pictureName = args.getString(BUNDLEPICTURENAME);
            displayWidth = args.getInt(BUNDLEDISPLAYWITH);
        }

        //try {
        imageViewUsersPhoto = (ImageView) myView.findViewById(R.id.imageViewUsersPhotoFragment);
        textViewDate = (TextView) myView.findViewById(R.id.textViewDate);

        if (pictureName.contains(NOPICTAKEN)){
            textViewDate.setText("Picture could not be displayed. Nevertheless - please fill the survey :)");
        } else {
            try {
                File file = new File(path + File.separator + pictureName);
                Log.v(TAG, "file: " + path + File.separator + pictureName);

                Bitmap picture = BitmapFactory.decodeFile(file.getAbsolutePath());

                int widthScaled = displayWidth;

                int width = picture.getWidth();
                int height = picture.getHeight();

                double scalefactor = ((double) height) / ((double) width);

                int scaleHeight = (int) (widthScaled * scalefactor);

                float scaleWidth = ((float) widthScaled) / width;

                float scaledHeight = ((float) scaleHeight) / height;

                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaledHeight);


                Bitmap scaledPicture = Bitmap.createBitmap(picture, 0, 0, width, height, matrix, false);

            /*int width = picture.getWidth();
            int height = picture.getHeight();

            double scalefactor = (displayWidth/(width));
            double scaleHeight = height / scalefactor;
            Matrix matrix = new Matrix();
            float scaledWidth = ((float) displayWidth) / width;
            float scaledHeight = ((float) scaleHeight) /height;
            matrix.postScale(scaledWidth, scaledHeight);
            Bitmap scaledPicture = Bitmap.createBitmap(picture, 0, 0, width, height, matrix, false);*/
                imageViewUsersPhoto.setImageBitmap(scaledPicture);

                textViewDate.setText(pictureName.substring((pictureName.length() - 12), (pictureName.length() - 4)));
            } catch (Exception e){
                Log.d(TAG, "Error while loading picture");
                textViewDate.setText("Picture could not be displayed. Nevertheless - please fill the survey :)");
            }
        }


        return myView;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
