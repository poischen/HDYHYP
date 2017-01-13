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

    ImageView imageViewUsersPhoto;
    TextView textViewDate;

    String path;
    String pictureName;

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
        }

        try {
            imageViewUsersPhoto = (ImageView) getView().findViewById(R.id.imageViewUsersPhoto);

            File file = new File(path + File.separator + pictureName);
            Bitmap picture = BitmapFactory.decodeFile(file.getAbsolutePath());
            int width = picture.getWidth();
            int height = picture.getHeight();
            Matrix matrix = new Matrix();
            float scaleWidth = ((float) 90) / width;
            float scaleHeight = ((float) 90) / height;
            matrix.postScale(scaleWidth, scaleHeight);
            Bitmap scaledPicture = Bitmap.createBitmap(picture, 0, 0, width, height, matrix, false);
            imageViewUsersPhoto.setImageBitmap(scaledPicture);
            textViewDate = (TextView) getView().findViewById(R.id.textViewDate);
            textViewDate.setText(pictureName.substring(0, pictureName.length()-4));
        } catch (Exception e){
            Log.d(TAG, "Image not found");
        }

        return myView;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
