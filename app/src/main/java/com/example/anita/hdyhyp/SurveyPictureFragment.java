package com.example.anita.hdyhyp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

            File file = new File(path + File.separator + pictureName);
            Log.v(TAG, "file: " + path + File.separator + pictureName);
        if (!(file.equals(null))){
            Bitmap picture = BitmapFactory.decodeFile(file.getAbsolutePath());
            int width = picture.getWidth();
            int height = picture.getHeight();

            double scalefactor = (displayWidth/(width));
            double scaleHeight = height / scalefactor;
            Matrix matrix = new Matrix();
            float scaledWidth = ((float) displayWidth) / width;
            float scaledHeight = ((float) scaleHeight) / height;
            matrix.postScale(scaledWidth, scaledHeight);
            Bitmap scaledPicture = Bitmap.createBitmap(picture, 0, 0, width, height, matrix, false);
            imageViewUsersPhoto.setImageBitmap(scaledPicture);

            textViewDate.setText(pictureName.substring((pictureName.length()-12), (pictureName.length()-4)));
        } else {
            textViewDate.setText("Image not found.");
        }

        return myView;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
