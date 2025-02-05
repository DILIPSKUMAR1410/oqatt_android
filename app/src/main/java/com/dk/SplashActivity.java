package com.dk;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.dk.main.R;
import com.dk.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    public static final int RequestPermissionCode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_splash);
        Log.d(">>>>>>>>>>", "In splash");
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE}, RequestPermissionCode);
    }


    @Override
    public void onRequestPermissionsResult(int RC, @NonNull String per[], @NonNull int[] PResult) {

        switch (RC) {

            case RequestPermissionCode:

                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "Permission Granted, Now your application can access CONTACTS.", Toast.LENGTH_LONG).show();
                    // [START initialize_auth]
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    // [END initialize_auth]
                    if (mAuth.getCurrentUser() == null) {
                        Utils.redirectToLogin(this);
                    } else {
                        Log.d(">>>>>>>>>>", "In splash uid  present");
                        Utils.redirectToMain(this);
                    }
                } else {

                    Toast.makeText(this, "Permission Canceled, Now your application cannot access CONTACTS.", Toast.LENGTH_LONG).show();
                    this.finishAffinity();
                    System.exit(0);
                }
                break;
        }
    }

}
