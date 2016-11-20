package com.example.anita.hdyhyp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by anita on 19.11.2016.
 */

public class PictureReviewGridViewAdapter extends ArrayAdapter {
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
    public View getView(int position, View convertView, ViewGroup parent) {

        View row;
        ViewHolder holder;

        LayoutInflater inflater = LayoutInflater.from(context);
        row = inflater.inflate(layoutResourceId, parent, false);
        holder = new ViewHolder();
        holder.picture = (ImageView) row.findViewById(R.id.picture);
        holder.deleteCheckbox = (CheckBox) row.findViewById(R.id.deleteCheckbox);
        row.setTag(holder);

        PictureItem item = data.get(position);
        holder.picture.setImageBitmap(item.getPicture());
        item.setImageView(holder.picture);
        item.setCheckbox(holder.deleteCheckbox);
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