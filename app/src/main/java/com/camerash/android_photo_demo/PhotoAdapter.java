package com.camerash.android_photo_demo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Esmond on 11-Mar-17.
 */

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.MyHolder> {

    interface PhotoOnClickListener {
        void onPhotoClicked(Photo photo);
    }

    private ArrayList<Photo> photos;
    private Context mContext;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    PhotoAdapter(Context context, ArrayList<Photo> photos) {
        this.mContext = context;
        this.photos = photos;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.photo_item, parent, false);
        return new MyHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {
        final Photo curPhoto = photos.get(position);

        // Load information of photo into holder title
        holder.title.setText(format.format(curPhoto.getTime()));

        // Load photo with Picasso library
        Picasso.get()
                .load(curPhoto.getUrl())
                .into(holder.photo);

        // Set OnClickListener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mContext instanceof PhotoOnClickListener) {
                    ((PhotoOnClickListener) mContext).onPhotoClicked(curPhoto);
                }
            }
        });
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public void setPhotos(ArrayList<Photo> photos) {
        this.photos = photos;
        notifyDataSetChanged();
    }

    static class MyHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private ImageView photo;

        MyHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            photo = v.findViewById(R.id.photo);
        }
    }
}
