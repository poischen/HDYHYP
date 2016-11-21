package com.example.anita.hdyhyp;

import android.graphics.Bitmap;
import android.widget.CheckBox;
import android.widget.ImageView;

/**
 * Created by anita on 20.11.2016.
 */

public class PictureItem {

    private Bitmap picture;
    private String path;
    private ImageView imageView;
    private CheckBox checkbox;
    private boolean taggedToDelete = false;

    public PictureItem(Bitmap image, String path) {
        super();
        this.picture = image;
        this.path = path;
        //this.taggedToDelete = false;

    }

    public Bitmap getPicture() {
        return picture;
    }

    public void setPicture(Bitmap image) {
        this.picture = image;
    }

    public String getPath(){
        return path;
    }

    public void setPath(String path){
        this.path = path;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public boolean isTaggedToDelete() {
        return taggedToDelete;
    }

    public void setTaggedToDelete(boolean taggedToDelete) {
        this.taggedToDelete = taggedToDelete;
    }

    public CheckBox getCheckbox() {
        return checkbox;
    }

    public void setCheckbox(CheckBox checkbox) {
        this.checkbox = checkbox;
    }
}
