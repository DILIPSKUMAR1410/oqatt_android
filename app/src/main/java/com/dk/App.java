package com.dk;

/**
 * Created by dk on 10/09/17.
 */

import android.app.Application;

import com.androidnetworking.AndroidNetworking;
import com.dk.models.MyObjectBox;

import io.objectbox.BoxStore;


public class App extends Application {

    public static final String TAG = "ObjectBoxExample";
    private static App sInstance = null;
    private BoxStore boxStore;

    public static App getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidNetworking.initialize(getApplicationContext());

//        if (EXTERNAL_DIR) {
//            // Example how you could use a custom dir in "external storage"
//            // (Android 6+ note: give the app storage permission in app info settings)
//            File directory = new File(Environment.getExternalStorageDirectory(), "objectbox-notes");
//            boxStore = MyObjectBox.builder().androidContext(App.this).directory(directory).build();
//        } else {
        // This is the minimal setup required on Android
        boxStore = MyObjectBox.builder().androidContext(App.this).build();
        sInstance = this;
//        }
    }

    public BoxStore getBoxStore() {
        return boxStore;
    }
}