package com.camerash.android_photo_demo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Esmond on 11-Mar-17.
 */

public class Util {

    private static final String MY_PREFS_NAME = "SkygearPhoto";

    public static void saveData(Context context, String key, String value){
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getData(Context context, String key){
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String name = prefs.getString(key, null);
       if(name!=null){
           return name;
       }
        return null;
    }

    public static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static boolean isNetworkConnected(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null)
        {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }

    //Prettify UI
    public static void startUpAnimation(Activity activity) {

        final ImageView logo = (ImageView) activity.findViewById(R.id.logo);
        final ProgressBar progressBar = (ProgressBar) activity.findViewById(R.id.progress_bar);
        final TextView text = (TextView) activity.findViewById(R.id.init_text);

        final TranslateAnimation moveUp = new TranslateAnimation(0, 0, 100, 0);
        moveUp.setInterpolator(new DecelerateInterpolator());
        moveUp.setDuration(500);
        moveUp.setFillAfter(true);

        final Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(500);
        fadeIn.setStartOffset(500);

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                progressBar.setVisibility(View.VISIBLE);
                text.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        logo.setAnimation(moveUp);
        progressBar.setAnimation(fadeIn);
        text.setAnimation(fadeIn);

    }
}
