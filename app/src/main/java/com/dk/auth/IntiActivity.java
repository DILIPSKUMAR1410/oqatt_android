package com.dk.auth;

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
import com.github.tamir7.contacts.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.objectbox.Box;
import pl.droidsonroids.gif.GifDrawable;

public class IntiActivity extends AppCompatActivity {
    private static final String TAG = ">>>>>>>>>>>>.";
    Context context = this;
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
                Log.d(TAG, "Background completed");
                new Utils().redirectToMain((Activity) context);


            }
        }.execute();

    }

    private String sync() {
        Contacts.initialize(this);
        Query q = Contacts.getQuery();
        q.hasPhoneNumber();
        q.whereStartsWith(Contact.Field.PhoneNormalizedNumber, "+91");
        List<Contact> contacts = q.find();
        Iterator<Contact> itr = contacts.iterator();
        Contact next;
        ArrayList<User> users = new ArrayList<>();
        while (itr.hasNext()) {
            User user = new User();
            next = itr.next();
            user.setContact(String.valueOf(next.getPhoneNumbers().get(0).getNormalizedNumber()));
            user.setName(next.getDisplayName());
            user.setKnows_me(false);
            users.add(user);
        }
        Box<User> userBox = App.getInstance().getBoxStore().boxFor(User.class);
        userBox.put(users);

        return "Sync completed";
    }


}


