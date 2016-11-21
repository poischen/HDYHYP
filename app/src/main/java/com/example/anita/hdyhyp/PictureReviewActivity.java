package com.example.anita.hdyhyp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Picture;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;

import java.io.File;
import java.util.ArrayList;

import static android.graphics.PorterDuff.Mode.SCREEN;
import static com.example.anita.hdyhyp.ControllerService.storage;

public class PictureReviewActivity extends AppCompatActivity {

    private static final String TAG = PictureReviewActivity.class.getSimpleName();

    private GridView gridView;
    private PictureReviewGridViewAdapter gridAdapter;
    private ArrayList<PictureItem> pictureItems;
    private ArrayList<PictureItem> taggedToDeleteItems = new ArrayList<>();
    private Button deleteButton;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_review);

        gridView = (GridView) findViewById(R.id.gridView);
        gridAdapter = new PictureReviewGridViewAdapter(getApplicationContext(), R.layout.picture_review_grid_item, getData());
        gridView.setAdapter(gridAdapter);

        /*gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                PictureItem item = (PictureItem) parent.getItemAtPosition(position);
                Log.v(TAG, "Item clicked, position: " + position);
                Log.v(TAG, "Item clicked, item: " + item);
                Log.v(TAG, "Item clicked, path: " + item.getPath());
                if (!item.isTaggedToDelete()){
                    //Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.logo);
                    //item.setPicture(icon);
                    item.getImageView().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark), SCREEN);
                    item.setTaggedToDelete(true);
                    taggedToDeleteItems.add(item);
                } else {
                    item.getImageView().clearColorFilter();
                    item.setTaggedToDelete(false);
                    taggedToDeleteItems.remove(item);
                }
                gridAdapter.notifyDataSetChanged();
                gridView.invalidateViews();
                gridView.setAdapter(gridAdapter);
            }
        });*/


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                PictureItem item = (PictureItem) parent.getItemAtPosition(position);
                Log.v(TAG, "Item clicked, position: " + position);
                Log.v(TAG, "Item clicked, item: " + item);
                Log.v(TAG, "Item clicked, path: " + item.getPath());
                if (!item.isTaggedToDelete()){
                    item.setTaggedToDelete(true);
                    taggedToDeleteItems.add(item);
                } else {
                    item.setTaggedToDelete(false);
                    taggedToDeleteItems.remove(item);
                }
            }
        });



        deleteButton = (Button) findViewById(R.id.picturesDeleteButton);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*for (int i=0; i < taggedToDeleteItems.size(); i++){
                    PictureItem item = taggedToDeleteItems.get(i);
                    gridAdapter.remove(item);
                    File file = new File(taggedToDeleteItems.get(i).getPath());
                    file.delete();
                    Log.v(TAG, "File deleted: " + file);
                }*/


                ArrayList<PictureItem> pictureItems;
                int s = gridAdapter.getDataSize();
                for (int i=0; i < s; i++){
                    PictureItem currentItem = (PictureItem) gridAdapter.getItem(i);
                    if (currentItem.getCheckbox().isChecked()){
                        taggedToDeleteItems.add(currentItem);
                    }
                }

                for (int i=0; i < taggedToDeleteItems.size(); i++){
                    PictureItem currentItem = taggedToDeleteItems.get(i);
                    gridAdapter.remove(currentItem);
                    File file = new File(taggedToDeleteItems.get(i).getPath());
                    file.delete();
                    Log.v(TAG, "File deleted: " + file);
                }

                gridAdapter.notifyDataSetChanged();
                taggedToDeleteItems.clear();
            }
        });

        submitButton = (Button) findViewById(R.id.picturesSendButton);

    }

    private ArrayList<PictureItem> getData(){
        File folder = new File(storage.getStoragePath() + File.separator);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                System.out.println(file.getName());
            }
        }

        pictureItems = new ArrayList<PictureItem>();
        for(int i=0;i<listOfFiles.length;i++)
        {
            Log.v("Image: "+i+": path", listOfFiles[i].getAbsolutePath());

            String currentPath = listOfFiles[i].getAbsolutePath();
            Bitmap currentPicture = BitmapFactory.decodeFile(listOfFiles[i].getAbsolutePath());

            int width = currentPicture.getWidth();
            int height = currentPicture.getHeight();
            float scaleWidth = ((float) 90) / width;
            float scaleHeight = ((float) 90) / height;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            Bitmap scaledPicture = Bitmap.createBitmap(currentPicture, 0, 0, width, height, matrix, false);

            PictureItem pictureItem = new PictureItem(scaledPicture, currentPath);
            pictureItems.add(pictureItem);

        }

        return pictureItems;
    }
        }