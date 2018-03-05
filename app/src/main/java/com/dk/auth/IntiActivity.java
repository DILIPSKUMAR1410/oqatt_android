package com.dk.auth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.dk.App;
import com.dk.graph.ApiCalls;
import com.dk.main.R;
import com.dk.models.User;
import com.dk.utils.Utils;
import com.github.tamir7.contacts.Contact;
import com.github.tamir7.contacts.Contacts;
import com.github.tamir7.contacts.PhoneNumber;
import com.github.tamir7.contacts.Query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.objectbox.Box;

public class IntiActivity extends AppCompatActivity {
    private static final String TAG = ">>>>>>>>>>>>.";
    Context context = this;

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_inti);
        Log.d(">>>>>>>>>>", "In init login");

        new AsyncTask<String, Void, Context>() {
            @Override
            protected Context doInBackground(String... urls) {
                sync();
                ApiCalls.createUser(context);
                return context;
            }

            @Override
            protected void onPostExecute(Context context) {

                //Use result for something
                Log.d(TAG, "Initialization completed");
                Utils.redirectToMain((Activity) context);
            }
        }.execute();
    }

    private void sync() {
        Contacts.initialize(this);
        Query q = Contacts.getQuery();
        q.hasPhoneNumber();
        List<Contact> contacts = q.find();
        Iterator<Contact> itr = contacts.iterator();
        Contact next;
        ArrayList<User> users = new ArrayList<>();
        while (itr.hasNext()) {
            next = itr.next();
            for (PhoneNumber p : next.getPhoneNumbers()) {
                String phone = String.valueOf(p.getNormalizedNumber());
                User user = new User();
                user.setContact(phone);
                user.setName(next.getDisplayName());
                user.setKnows_me(false);
                users.add(user);
            }
        }
        Box<User> userBox = App.getInstance().getBoxStore().boxFor(User.class);
        userBox.put(users);

    }


}


