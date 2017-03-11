package com.camerash.android_photo_demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Esmond on 11-Mar-17.
 */

public class PhotoAdapter extends BaseAdapter{
    private ArrayList<String> times, photos;
    private Context mContext;

    public static class ViewHolder {
        TextView title, date;
        ImageView photo;
    }

    public PhotoAdapter(Context context, ArrayList<String> times, ArrayList<String> photos) {
        this.mContext = context;
        this.times = times;
        this.photos = photos;
    }

    public int getCount() {
        return photos.size();
    }

    public Object getItem(int arg0) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(mContext).inflate(R.layout.photo_item, parent, false);
            holder = new ViewHolder();
            holder.title = (TextView) v.findViewById(R.id.title);
            holder.date = (TextView) v.findViewById(R.id.date);
            holder.photo = (ImageView) v.findViewById(R.id.photo);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        if(!photos.get(position).equals("")){
            holder.title.setText(photos.get(position));
            holder.date.setText(times.get(position));
            Bitmap img = Util.loadImage(mContext, photos.get(position));
            if(img!=null){
                holder.photo.setImageBitmap(img);
            }
            else{
                holder.photo.setVisibility(View.GONE);
            }
        }
        else{
            holder.photo.setVisibility(View.GONE);
        }
        return v;
    }
}
