package com.camerash.android_photo_demo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Esmond on 11-Mar-17.
 */

public class Util {

    private static final String MY_PREFS_NAME = "SkygearPhoto";

    interface DownloadTaskListener {
        void onReturned(byte[] result);
    }

    public static class DownloadTask extends AsyncTask<String, Void, byte[]> {

        private String url;
        private DownloadTaskListener mListener;

        private DownloadTask(String urls, DownloadTaskListener listener) {
            mListener = listener;
            this.execute(urls);
        }

        public DownloadTask(String urls) {
            this(urls, null);
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected byte[] doInBackground(String... urls) {
            return httpGET(urls[0]);
        }

        @Override
        protected void onPostExecute(byte[] result) {

        }
    }

    public static
    byte[] httpGET(String url)
    {
        try
        {
            while (true)
            {
                // make GET request to the given URL
                HttpURLConnection urlConnection = (HttpURLConnection) (new URL(url)).openConnection();
                urlConnection.setInstanceFollowRedirects(false);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setReadTimeout(15000);
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(false);
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                //200 means OK
                int status = urlConnection.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK)
                {
                    // receive response as inputStream
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

                    // convert inputStream to string
                    byte[] result = convertInputStreamToBytes(inputStream);
                    inputStream.close();
                    urlConnection.disconnect();
                    return result;
                }
                else if (status == HttpURLConnection.HTTP_MOVED_TEMP ||
                        status == HttpURLConnection.HTTP_MOVED_PERM ||
                        status == HttpURLConnection.HTTP_SEE_OTHER)
                {
                    String tempUrl = urlConnection.getHeaderField("Location");

                    // relative url
                    if (!tempUrl.startsWith("http"))
                        url = url.substring(0, url.lastIndexOf('/') + 1) + tempUrl;
                    else
                        url = tempUrl;

                    urlConnection.disconnect();
                }
                else
                {
                    urlConnection.disconnect();
                    return null;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

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

    private static
    byte[] convertInputStreamToBytes(InputStream inputStream)
            throws IOException
    {
//		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int num = 0;
        byte[] data = new byte[1024];

        while ((num = inputStream.read(data, 0, data.length)) != -1)
            buffer.write(data, 0, num);

        inputStream.close();
        buffer.flush();

        return buffer.toByteArray();
    }

    public static void saveImage(Context context, String name, byte[] imgBytes)
    {
        try
        {
            File eventDir = new File(context.getFilesDir(), "photos");
            eventDir.mkdir();
            File image = new File(eventDir, name);
            FileOutputStream fos = new FileOutputStream(image);
            fos.write(imgBytes);
            fos.flush();
            fos.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static Bitmap loadImage(Context context, String name)
    {
        try
        {
            File eventDir = new File(context.getFilesDir(), "photos");
            if(eventDir.exists()) {
                File image = new File(eventDir, name);
                FileInputStream fis = new FileInputStream(image);
                Bitmap retImg = BitmapFactory.decodeStream(fis);
                fis.close();
                return retImg;
            }
            return null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static void deleteImage(Context context, String name)
    {
        try
        {
            File eventDir = new File(context.getFilesDir(), "photos");
            if(eventDir.exists()) {
                File image = new File(eventDir, name);
                image.delete();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static boolean isImageExist(Context context, String name){
        File eventDir = new File(context.getFilesDir(), "photos");
        if(eventDir.exists()) {
            File image = new File(eventDir, name);
            if(image.exists()){
                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
    }

    public static String getFileAbsolutePath(Context context, String name){
        try
        {
            File eventDir = new File(context.getFilesDir(), "photos");
            if(eventDir.exists()) {
                File image = new File(eventDir, name);
                return image.getAbsolutePath();
            }
            return null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
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
