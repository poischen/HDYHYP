package com.example.anita.hdyhyp;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.io.File;
import java.util.ArrayList;

import static com.example.anita.hdyhyp.ControllerService.storage;

public class PictureReviewActivity extends AppCompatActivity {

    private GridView gridView;
    private PictureReviewGridViewAdapter gridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_review);

        gridView = (GridView) findViewById(R.id.gridview);
        gridAdapter = new PictureReviewGridViewAdapter(getApplicationContext(), R.layout.picture_review_grid_item, getData());
        gridView.setAdapter(gridAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Bitmap item = (Bitmap) parent.getItemAtPosition(position);
                //TODO: löschen Möglichkeit
                //Create intent
                //Intent intent = new Intent(getApplicationContext(), dsfjsdjf.class);
                //intent.putExtra("image", item);
                //Start details activity
                //startActivity(intent);

            }
        });
    }

    private ArrayList<Bitmap> getData(){
        /*final ArrayList<Bitmap> imageItems = new ArrayList<>();
        TypedArray imgs = getResources().obtainTypedArray(R.array.image_ids);
        for (int i = 0; i < imgs.length(); i++) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imgs.getResourceId(i, -1));
            imageItems.add(new ImageItem(bitmap, "Image#" + i));
        }*/

        File folder = new File(storage.getStoragePath() + File.separator);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                System.out.println(file.getName());
            }
        }

        ArrayList<Bitmap> pictureItems = new ArrayList<Bitmap>();
        for(int i=0;i<listOfFiles.length;i++)
        {
            Log.v("Image: "+i+": path", listOfFiles[i].getAbsolutePath());

            //Uri selectedImage = imageReturnedIntent.getData();
            //InputStream imageStream = getContentResolver().openInputStream(selectedImage);

            Bitmap currentPicture = BitmapFactory.decodeFile(listOfFiles[i].getAbsolutePath());

            Matrix matrix = new Matrix();
            matrix.postScale(0.5f, 0.5f);
            Bitmap scaledPicture = Bitmap.createBitmap(currentPicture, 90, 90, 90, 90, matrix, true);

            pictureItems.add(scaledPicture);

        }

        return pictureItems;
    }
        }