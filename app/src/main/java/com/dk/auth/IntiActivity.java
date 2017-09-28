package com.dk.auth;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.dk.graph.ApiCalls;
import com.dk.main.R;

public class IntiActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inti);
        Log.d(">>>>>>>>>>", "In init login");

        ApiCalls.createUser(this);
//            ApiCalls.syncObjectBox(this);
    }


    private boolean checkContactPermission() {

        String permission = "android.permission.READ_CONTACTS";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }


}
