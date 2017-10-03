package com.dk.auth;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.dk.main.R;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;

public class IntiActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inti);
        Log.d(">>>>>>>>>>", "In init login");
        try {
            GifDrawable gifFromResource = new GifDrawable(getResources(), R.drawable.dribbble);
            gifFromResource.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        ApiCalls.createUser(this);
//            ApiCalls.syncObjectBox(this);
    }


    private boolean checkContactPermission() {

        String permission = "android.permission.READ_CONTACTS";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }


}
