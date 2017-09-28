package com.dk.utils;

import android.app.Activity;
import android.content.Intent;

import com.dk.auth.IntiActivity;
import com.dk.auth.PhoneAuthActivity;
import com.dk.main.MainActivity;

/**
 * Created by dk on 27/09/17.
 */

public class Utils {

    public void redirectToLogin(Activity activity) {

        Intent intent = new Intent(activity, PhoneAuthActivity.class);
        activity.startActivity(intent);
    }

    public void redirectToMain(Activity activity) {

        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
    }

    public void redirectToInit(Activity activity) {
        Intent intent = new Intent(activity, IntiActivity.class);
        activity.startActivity(intent);
    }
}
