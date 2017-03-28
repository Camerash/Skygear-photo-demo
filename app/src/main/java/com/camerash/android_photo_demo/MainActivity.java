package com.camerash.android_photo_demo;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
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
import java.text.SimpleDateFormat;
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
import io.skygear.skygear.User;

public class MainActivity extends AppCompatActivity {

    public static final int PICK_IMAGE = 1;
    public static boolean deleteMode = false;
    public static Thread deleteRecordsThread;
    public static Record[] savedRecords;
    public static Record pendingDeleteRecord;
    public static ProgressDialog progressDialog;
    public Container skygear;
    public Database publicDB;
    public Thread skygearSignupThread, skygearSigninThread, getRecordsThread, uploadImageThread;
    public Context mInstance;
    public byte[] currentImageData;
    public String currentImageName, currentImageType;
    public String username, password;
    public RecyclerView recyclerView;
    public LinearLayoutManager linearLayoutManager;

    public Runnable getRecords = new Runnable() {
        @Override
        public void run() {
            Query noteQuery = new Query("Note");

            publicDB.query(noteQuery, new RecordQueryResponseHandler() {
                @Override
                public void onQuerySuccess(Record[] records) {
                    Log.i("Record Query", String.format("Successfully got %d records", records.length));
                    savedRecords = records;
                    for (Record record : savedRecords) {
                        if (record.get("image") != null) {
                            try {
                                Asset eventImg = (Asset) record.get("image");
                                if (!Util.isImageExist(mInstance, eventImg.getName())) {
                                    byte[] img = new Util.DownloadTask(eventImg.getUrl()).get();
                                    Util.saveImage(mInstance, eventImg.getName(), img);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    preparePhotoList();
                }

                @Override
                public void onQueryError(Error error) {
                    Log.i("Record Query", String.format("Fail with reason:%s", error.getMessage()));
                }
            });
        }
    };
    public Runnable skygearSignin = new Runnable() {
        @Override
        public void run() {
            skygear.loginWithUsername(username, password, new AuthResponseHandler() {
                @Override
                public void onAuthSuccess(User user) {
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
                    Util.saveData(mInstance, "username", username);
                    Util.saveData(mInstance, "password", password);
                    getRecordsThread = new Thread(getRecords);
                    getRecordsThread.start();
                }

                @Override
                public void onAuthFail(Error error) {
                    progressDialog.dismiss();
                    if(error.getCode().equals(Error.Code.INVALID_CREDENTIALS)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mInstance, "Email / Password incorrect", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else{
                        Handler handler = new Handler();
                        handler.postDelayed(skygearSignin, 2000);
                    }

                }
            });
        }
    };

    public Runnable skygearSignup = new Runnable() {
        @Override
        public void run() {
            skygear.signupWithUsername(username, password, new AuthResponseHandler() {
                @Override
                public void onAuthSuccess(User user) {
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
                    Util.saveData(mInstance, "username", username);
                    Util.saveData(mInstance, "password", password);
                    getRecordsThread = new Thread(getRecords);
                    getRecordsThread.start();
                }

                @Override
                public void onAuthFail(Error error) {
                    if (error.getCode().equals(Error.Code.DUPLICATED)) {
                        Toast.makeText(mInstance, "User already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mInstance, "Signup failed", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                    Log.d("error", error.getCode().toString());
                }
            });
        }
    };

    public Runnable deleteRecords = new Runnable() {
        @Override
        public void run() {
            publicDB.delete(pendingDeleteRecord, new RecordDeleteResponseHandler() {
                @Override
                public void onDeleteSuccess(String[] ids) {
                    Record record = pendingDeleteRecord;
                    if (record.get("image") != null) {
                        Asset eventImg = (Asset) record.get("image");
                        Util.deleteImage(mInstance, eventImg.getName());
                    }
                    progressDialog.dismiss();
                    pendingDeleteRecord = null;
                    getRecordsThread = new Thread(getRecords);
                    getRecordsThread.start();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mInstance, "Deleted", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onDeletePartialSuccess(String[] ids, Map<String, Error> errors) {
                    progressDialog.dismiss();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mInstance, "System error, please try again", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onDeleteFail(Error error) {
                    Log.d("test", error.getMessage());
                    progressDialog.dismiss();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mInstance, "System error, please try again", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            deleteMode = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ActionBar mBar = getSupportActionBar();
                    if (mBar != null) {
                        ColorDrawable original = new ColorDrawable(ContextCompat.getColor(mInstance, R.color.colorPrimary));
                        mBar.setBackgroundDrawable(original);
                        mBar.setTitle(R.string.app_name);
                    }
                }
            });
        }
    };

    public Runnable uploadImage = new Runnable() {
        @Override
        public void run() {
            Asset imageAsset = new Asset(currentImageName, currentImageType, currentImageData);

            skygear.uploadAsset(imageAsset, new AssetPostRequest.ResponseHandler() {
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
                            getRecordsThread = new Thread(getRecords);
                            getRecordsThread.start();
                        }

                        @Override
                        public void onPartiallySaveSuccess(Map<String, Record> successRecords, Map<String, Error> errors) {
                            Log.i("Skygear Record", "Partially saved");
                        }

                        @Override
                        public void onSaveFail(Error error) {
                            Log.i("Skygear Record", "Record save fails");
                        }
                    });
                }

                @Override
                public void onPostFail(Asset asset, Error error) {
                    Log.i("Skygear Asset", "Upload fail: " + error.toString());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mInstance, "Upload failed!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        if (!Util.isNetworkConnected(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), "No network connected", Toast.LENGTH_SHORT).show();
            finish();
        }

        skygear = Container.defaultContainer(this);
        publicDB = skygear.getPublicDatabase();

        mInstance = this.getApplicationContext();

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(false);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        Configuration config = new Configuration.Builder()
                .endPoint("https://androidphotodemo.skygeario.com/")
                .apiKey("59910b66ea4e469187247df769100029")
                .build();

        skygear.configure(config);

        ((SwipeRefreshLayout) findViewById(R.id.swiperefresh)).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getRecordsThread = new Thread(getRecords);
                getRecordsThread.start();
            }
        });

        if (Util.getData(this, "username") == null) {
            findViewById(R.id.loading_screen).setVisibility(View.GONE);
            findViewById(R.id.login).setVisibility(View.VISIBLE);
            findViewById(R.id.singin).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    username = ((EditText) findViewById(R.id.username)).getText().toString();
                    password = ((EditText) findViewById(R.id.password)).getText().toString();
                    progressDialog.setMessage("Signing in......");
                    progressDialog.show();
                    skygearSigninThread = new Thread(skygearSignin);
                    skygearSigninThread.start();
                }
            });
            findViewById(R.id.singup).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    username = ((EditText) findViewById(R.id.username)).getText().toString();
                    password = ((EditText) findViewById(R.id.password)).getText().toString();
                    progressDialog.setMessage("Signing up......");
                    progressDialog.show();
                    skygearSignupThread = new Thread(skygearSignup);
                    skygearSignupThread.start();
                }
            });
            return;
        } else {
            findViewById(R.id.login).setVisibility(View.GONE);
            username = Util.getData(this, "username");
            password = Util.getData(this, "password");
            skygearSigninThread = new Thread(skygearSignin);
            skygearSigninThread.start();
        }
        Util.startUpAnimation(MainActivity.this);
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
                prepareDeleteMode();
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

    public void prepareDeleteMode() {
        ActionBar mBar = getSupportActionBar();
        if (deleteMode) {
            deleteMode = false;
            if (mBar != null) {
                ColorDrawable original = new ColorDrawable(ContextCompat.getColor(mInstance, R.color.colorPrimary));
                mBar.setBackgroundDrawable(original);
                mBar.setTitle(R.string.app_name);
            }
        } else {
            deleteRecordsThread = new Thread(deleteRecords);
            deleteMode = true;
            if (mBar != null) {
                ColorDrawable red = new ColorDrawable(Color.RED);
                mBar.setBackgroundDrawable(red);
                mBar.setTitle("Press photo to delete");
            }
        }
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
                currentImageName = f.getName();
                ContentResolver cR = getContentResolver();
                currentImageType = cR.getType(imageUri);
                InputStream iStream = cR.openInputStream(imageUri);
                currentImageData = Util.getBytes(iStream);

                progressDialog.setMessage("Uploading......");
                progressDialog.show();

                uploadImageThread = new Thread(uploadImage);
                uploadImageThread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void preparePhotoList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (savedRecords.length <= 0) {
                    findViewById(R.id.no_photo).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.no_photo).setVisibility(View.GONE);
                }
                findViewById(R.id.loading_screen).setVisibility(View.GONE);
                ((SwipeRefreshLayout) findViewById(R.id.swiperefresh)).setRefreshing(false);
            }
        });
        ArrayList<String> times = new ArrayList<String>();
        ArrayList<String> photos = new ArrayList<String>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        for (Record record : savedRecords) {
            Asset eventImg = (Asset) record.get("image");
            times.add(format.format(record.getCreatedAt()));
            photos.add(eventImg.getName());
        }

        final PhotoAdapter mAdapter = new PhotoAdapter(mInstance, times, photos);
        recyclerView.setAdapter(mAdapter);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
            }
        });
    }
}
