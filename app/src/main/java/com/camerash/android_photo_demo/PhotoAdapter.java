package com.camerash.android_photo_demo;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Esmond on 11-Mar-17.
 */

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.MyHolder> {
    private ArrayList<String> times, photos;
    private Context mContext;

    public PhotoAdapter(Context context, ArrayList<String> times, ArrayList<String> photos) {
        this.mContext = context;
        this.times = times;
        this.photos = photos;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.photo_item, parent, false);
        return new MyHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(final MyHolder holder, final int position) {
        holder.bindPhoto(mContext, photos.get(position), times.get(position));
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.deleteMode) {
                    if (MainActivity.deleteRecordsThread != null) {
                        AlertDialog.Builder mBuilder = new AlertDialog.Builder(new ContextThemeWrapper(v.getContext(), R.style.myDialog));
                        mBuilder.setTitle("Delete photo");
                        mBuilder.setMessage("Confirm deleting the selected photo?");
                        mBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MainActivity.progressDialog.setMessage("Deleting...");
                                MainActivity.progressDialog.show();
                                MainActivity.pendingDeleteRecord = MainActivity.savedRecords[holder.getAdapterPosition()];
                                MainActivity.deleteRecordsThread.start();
                            }
                        });
                        mBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        AlertDialog dialog = mBuilder.create();
                        dialog.show();
                    }
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

    public static class MyHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private ImageView photo;
        private LinearLayout layout;

        public MyHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            photo = (ImageView) v.findViewById(R.id.photo);
            layout = (LinearLayout) v.findViewById(R.id.layout);
        }

        public void bindPhoto(Context mContext, String photoStr, String time) {
            if (photoStr != "") {
                title.setText(time);
                Bitmap img = Util.loadImage(mContext, photoStr);
                if (img != null) {
                    photo.setImageBitmap(img);
                } else {
                    photo.setVisibility(View.GONE);
                }
            } else {
                photo.setVisibility(View.GONE);
            }
        }
    }
}
