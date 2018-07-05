package com.camerash.android_photo_demo;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import io.skygear.skygear.Asset;
import io.skygear.skygear.AssetPostRequest;
import io.skygear.skygear.AuthResponseHandler;
import io.skygear.skygear.Configuration;
import io.skygear.skygear.Container;
import io.skygear.skygear.Database;
import io.skygear.skygear.Error;
import io.skygear.skygear.Query;
import io.skygear.skygear.Record;
import io.skygear.skygear.RecordDeleteResponseHandler;
import io.skygear.skygear.RecordQueryResponseHandler;
import io.skygear.skygear.RecordSaveResponseHandler;

public class MainActivity extends AppCompatActivity implements PhotoAdapter.PhotoOnClickListener {

    public static final int PICK_IMAGE = 1;
    public static boolean deleteMode = false;
    private ArrayList<Photo> photoList = new ArrayList<>();
    public ProgressDialog progressDialog;
    public Container skygear;
    public Database publicDB;
    public RecyclerView recyclerView;
    public LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        if (!Util.isNetworkConnected(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), "No network connected", Toast.LENGTH_SHORT).show();
            finish();
        }

        skygear = Container.defaultContainer(this);
        publicDB = skygear.getPublicDatabase();

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(false);

        recyclerView = findViewById(R.id.recycler_view);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        Configuration config = new Configuration.Builder()
                .endPoint(getResources().getString(R.string.skygear_endPoint))
                .apiKey(getResources().getString(R.string.skygear_apiKey))
                .build();

        skygear.configure(config);

        ((SwipeRefreshLayout) findViewById(R.id.swiperefresh)).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getRecords();
            }
        });

        if (Util.getData(this, "username") == null) {
            findViewById(R.id.loading_screen).setVisibility(View.GONE);
            findViewById(R.id.login).setVisibility(View.VISIBLE);
            findViewById(R.id.singin).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String username = ((EditText) findViewById(R.id.username)).getText().toString();
                    String password = ((EditText) findViewById(R.id.password)).getText().toString();
                    progressDialog.setMessage("Signing in......");
                    progressDialog.show();
                    skygearSignIn(username, password);
                }
            });
            findViewById(R.id.singup).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String username = ((EditText) findViewById(R.id.username)).getText().toString();
                    String password = ((EditText) findViewById(R.id.password)).getText().toString();
                    progressDialog.setMessage("Signing up......");
                    progressDialog.show();
                    skygearSignup(username, password);
                }
            });
            return;
        } else {
            findViewById(R.id.login).setVisibility(View.GONE);
            skygearSignIn(Util.getData(this, "username"), Util.getData(this, "password"));
        }
        Util.startUpAnimation(MainActivity.this);
    }

    public void getRecords() {
        Query noteQuery = new Query("Note");

        publicDB.query(noteQuery, new RecordQueryResponseHandler() {
            @Override
            public void onQuerySuccess(Record[] records) {
                Log.i("Record Query", String.format("Successfully got %d records", records.length));
                preparePhotoList(records);
            }

            @Override
            public void onQueryError(Error error) {
                Log.i("Record Query", String.format("Fail with reason:%s", error.getMessage()));
            }
        });
    }

    public void skygearSignIn(final String username, final String password) {
        skygear.getAuth().loginWithUsername(username, password, new AuthResponseHandler() {
            @Override
            public void onAuthSuccess(Record user) {
                progressDialog.dismiss();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.login).setVisibility(View.GONE);
                        findViewById(R.id.loading_screen).setVisibility(View.GONE);
                        progressDialog.setMessage("Loading......");
                        progressDialog.show();
                    }
                });
                Util.saveData(MainActivity.this, "username", username);
                Util.saveData(MainActivity.this, "password", password);
                getRecords();
            }

            @Override
            public void onAuthFail(Error error) {
                progressDialog.dismiss();
                if(error.getCode().equals(Error.Code.INVALID_CREDENTIALS) || error.getCode().equals(Error.Code.RESOURCE_NOT_FOUND)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Email / Password incorrect", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else{
                    Toast.makeText(MainActivity.this, "Error, please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void skygearSignup(final String username, final String password) {
        skygear.getAuth().signupWithUsername(username, password, new AuthResponseHandler() {
            @Override
            public void onAuthSuccess(Record user) {
                progressDialog.dismiss();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.login).setVisibility(View.GONE);
                        findViewById(R.id.loading_screen).setVisibility(View.GONE);
                        progressDialog.setMessage("Loading......");
                        progressDialog.show();
                    }
                });
                Util.saveData(MainActivity.this, "username", username);
                Util.saveData(MainActivity.this, "password", password);
                getRecords();
            }

            @Override
            public void onAuthFail(Error error) {
                if (error.getCode().equals(Error.Code.DUPLICATED)) {
                    Toast.makeText(MainActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Signup failed", Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
                Log.d("error", error.getCode().toString());
            }
        });
    }

    public void deleteRecord(Record pendingDeleteRecord) {
        publicDB.delete(pendingDeleteRecord, new RecordDeleteResponseHandler() {
            @Override
            public void onDeleteSuccess(String[] ids) {
                progressDialog.dismiss();
                getRecords();
                Toast.makeText(MainActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeletePartialSuccess(String[] ids, Map<String, Error> errors) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "System error, please try again", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteFail(Error error) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "System error, please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void uploadPhoto(String imageName, String imageType, byte[] imageData) {
        progressDialog.setMessage("Uploading......");
        progressDialog.show();

        Asset imageAsset = new Asset(imageName, imageType, imageData);

        publicDB.uploadAsset(imageAsset, new AssetPostRequest.ResponseHandler() {
            @Override
            public void onPostSuccess(Asset asset, String response) {
                Log.i("Skygear Asset", "Successfully uploaded to " + asset.getUrl());
                Record aNote = new Record("Note");
                aNote.setPublicNoAccess();
                aNote.set("image", asset);

                publicDB.save(aNote, new RecordSaveResponseHandler() {
                    @Override
                    public void onSaveSuccess(Record[] records) {
                        Log.i("Skygear Record", "Successfully saved");
                        progressDialog.dismiss();
                        getRecords();
                    }

                    @Override
                    public void onPartiallySaveSuccess(Map<String, Record> successRecords, Map<String, Error> errors) {
                        progressDialog.dismiss();
                        Log.i("Skygear Record", "Partially saved");
                    }

                    @Override
                    public void onSaveFail(Error error) {
                        progressDialog.dismiss();
                        Log.i("Skygear Record", "Record save fails");
                    }
                });
            }

            @Override
            public void onPostFail(Asset asset, Error error) {
                progressDialog.dismiss();
                Log.i("Skygear Asset", "Upload fail: " + error.toString());
                Toast.makeText(MainActivity.this, "Upload failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.upload_photo:
                pickImageFromGallery();
                return true;
            case R.id.delete_photo:
                showDeletingActionBar(!deleteMode);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void pickImageFromGallery() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    public void showDeletingActionBar(Boolean show) {
        ActionBar mBar = getSupportActionBar();
        if(mBar == null) return;
        if(show) {
            ColorDrawable red = new ColorDrawable(Color.RED);
            mBar.setBackgroundDrawable(red);
            mBar.setTitle("Press photo to delete");
        } else {
            ColorDrawable original = new ColorDrawable(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary));
            mBar.setBackgroundDrawable(original);
            mBar.setTitle(R.string.app_name);
        }
        deleteMode = show;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE) {
            if (data == null || resultCode != RESULT_OK) {
                return;
            }
            try {
                Uri imageUri = data.getData();
                File f = new File("" + imageUri);
                String imageName = f.getName();
                ContentResolver cR = getContentResolver();
                assert imageUri != null;
                String imageType = cR.getType(imageUri);
                InputStream iStream = cR.openInputStream(imageUri);
                assert iStream != null;
                byte[] imageData = Util.getBytes(iStream);

                uploadPhoto(imageName, imageType, imageData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void preparePhotoList(final Record[] photoRecords) {

        // Show empty view if no photo is loaded
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.no_photo).setVisibility(photoRecords.length <= 0 ? View.VISIBLE : View.GONE);
                findViewById(R.id.loading_screen).setVisibility(View.GONE);
                ((SwipeRefreshLayout) findViewById(R.id.swiperefresh)).setRefreshing(false);
                if (progressDialog != null) { progressDialog.dismiss(); }
            }
        });

        //
        photoList.clear();
        for (Record record : photoRecords) {
            Asset photo = (Asset) record.get("image");
            photoList.add(new Photo(record, photo));
        }

        final PhotoAdapter mAdapter = new PhotoAdapter(this, photoList);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onPhotoClicked(final Photo photo) {
        if (deleteMode) {
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
            mBuilder.setTitle("Delete photo");
            mBuilder.setMessage("Confirm deleting the selected photo?");
            mBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    progressDialog.setMessage("Deleting...");
                    progressDialog.show();
                    deleteRecord(photo.getRecord());
                }
            });
            mBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {}
            });
            AlertDialog dialog = mBuilder.create();
            dialog.show();
        }
    }
}
