package com.dk.auth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.dk.App;
import com.dk.graph.ApiCalls;
import com.dk.main.R;
import com.dk.models.User;
import com.dk.queue.Intialization;
import com.dk.utils.Utils;
import com.github.tamir7.contacts.Contact;
import com.github.tamir7.contacts.Contacts;
import com.github.tamir7.contacts.PhoneNumber;
import com.github.tamir7.contacts.Query;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.objectbox.Box;

public class IntiActivity extends AppCompatActivity {
    private static final String TAG = ">>>>>>>Init";
    Context context = this;

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_inti);
        Log.d(">>>>>>>>>>", "In init login");
        sync();
        ApiCalls.createUser(context);

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
                if (phone.length() < 10)
                    continue;
                User user = new User();
                if (phone.startsWith("+91"))
                    user.setContact(phone);
                else
                    user.setContact("+91" + phone.substring(phone.length() - 10));
                user.setName(next.getDisplayName());
                user.setKnows_me(false);
                users.add(user);

            }
        }
        Box<User> userBox = App.getInstance().getBoxStore().boxFor(User.class);
        userBox.put(users);

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    // This method will be called when a OnTokenBalance is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnIntialIzationComplete(Intialization event) {
        Log.d(TAG, event.message);
        Utils.redirectToMain((Activity) context);
    }


}


