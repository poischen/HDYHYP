package com.example.anita.hdyhyp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by anita on 19.11.2016.
 */

public class PictureReviewGridViewAdapter extends ArrayAdapter {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Context context;
    private int layoutResourceId;
    private ArrayList<PictureItem> data = new ArrayList<>();

    public PictureReviewGridViewAdapter(Context context, int layoutResourceId, ArrayList<PictureItem> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View row;
        ViewHolder holder;

        LayoutInflater inflater = LayoutInflater.from(context);
        row = inflater.inflate(layoutResourceId, parent, false);
        holder = new ViewHolder();
        holder.picture = (ImageView) row.findViewById(R.id.picture);

        final CheckBox deleteCheckbox = (CheckBox)row.findViewById(R.id.deleteCheckbox);
        holder.deleteCheckbox = deleteCheckbox;

        row.setTag(holder);

        final PictureItem item = data.get(position);
        holder.picture.setImageBitmap(item.getPicture());
        item.setImageView(holder.picture);
        item.setCheckbox(holder.deleteCheckbox);

        deleteCheckbox.setTag(position);

        boolean checked = item.isTaggedToDelete();
        Log.v(TAG, "Checkbox was checked: " + checked);
        if (checked){
            deleteCheckbox.setChecked(true);
        }
        else if (!checked)
        {
            deleteCheckbox.setChecked(false);
        }

       row.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Log.v(TAG, "Checkbox/Picture clicked.");
               //CheckBox deleteCheckbox = (CheckBox)v.findViewWithTag(position);
               //PictureItem currentPictureItem = data.get(position);
               if (deleteCheckbox.isChecked())
               {
                   deleteCheckbox.setChecked(false);
                   item.setTaggedToDelete(false);
               }
               else
               {
                   deleteCheckbox.setChecked(true);
                   item.setTaggedToDelete(true);
               }
           }

        });
        return row;
    }

    public int getDataSize(){
        return data.size();
    }

    static class ViewHolder {
        ImageView picture;
        CheckBox deleteCheckbox;
    }
}