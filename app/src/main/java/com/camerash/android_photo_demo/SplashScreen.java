package com.camerash.android_photo_demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Esmond on 21-Feb-17.
 */

public class SplashScreen extends AppCompatActivity {
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
