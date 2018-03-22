package com.dk.main;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.airbnb.lottie.LottieAnimationView;
import com.dk.App;
import com.dk.graph.ApiCalls;
import com.dk.models.User;
import com.dk.models.User_;
import com.dk.queue.UpdateFriendList;
import com.github.tamir7.contacts.Contact;
import com.github.tamir7.contacts.Contacts;
import com.github.tamir7.contacts.PhoneNumber;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.Query;

public class FriendListActivity extends AppCompatActivity {
    private static final String TAG = ">>>>>>FriendList";
    public Menu menu;
    ListView listview;
    String[] friends_list_name;
    LottieAnimationView animationView;
    ArrayAdapter<String> adapter;
    ArrayList<String> lst;

    //    String[] friends_list_contact;
    Box<User> userBox = App.getInstance().getBoxStore().boxFor(User.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);
        getSupportActionBar().setTitle("Friends");
        listview = findViewById(R.id.listView);
        Query<User> query = userBox.query().order(User_.name).equal(User_.knows_me, true).build();
        animationView = findViewById(R.id.empty_animation);
        friends_list_name = query.property(User_.name).findStrings();
        if (friends_list_name.length < 1) {
            animationView.setAnimation("empty.json");
            animationView.setVisibility(View.VISIBLE);
            animationView.playAnimation();
        }

//        friends_list_contact= query.property(User_.contact).findStrings();
        lst = new ArrayList<>(Arrays.asList(friends_list_name));
        adapter = new ArrayAdapter<>
                (this,
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1, lst);
        listview.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_friend_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.refresh:
                refresh();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refresh() {
        animationView.setAnimation("populate_wait.json");
        animationView.setVisibility(View.VISIBLE);
        animationView.playAnimation();
        try {
            Log.d(TAG, "Refresh is started");
            Box<User> userBox = App.getInstance().getBoxStore().boxFor(User.class);
            Log.d(TAG, String.valueOf(userBox.query().build().count()));
            List<User> users_contacts = userBox.query().order(User_.contact).build().find();
//                    property(User_.contact).distinct().findStrings();
            List<String> objbox_user_contact_list = new ArrayList<>();
            String temp_contact_repeat = "";
            for (User u:users_contacts) {
                if (!temp_contact_repeat.equalsIgnoreCase(u.getContact()))
                    objbox_user_contact_list.add(u.getContact());
                temp_contact_repeat = u.getContact();
            }

            Contacts.initialize(this);
            com.github.tamir7.contacts.Query q = Contacts.getQuery();
            q.hasPhoneNumber();
            List<Contact> contacts = q.find();
            Iterator<Contact> itr = contacts.iterator();
            Contact next;
            ArrayList<String> fresh_contacts_list = new ArrayList<>();
            ArrayList<User> users = new ArrayList<>();

            while (itr.hasNext()) {
                next = itr.next();
                for (PhoneNumber p : next.getPhoneNumbers()) {
                    String phone = String.valueOf(p.getNormalizedNumber());
                    if (phone.length() < 10)
                        continue;
                    if (!phone.startsWith("+91"))
                        phone = "+91" + phone.substring(phone.length() - 10);

                    if (!objbox_user_contact_list.contains(phone)) {
                        Log.d(">>>>>>>>.new", String.valueOf(phone));
                        fresh_contacts_list.add(phone);
                        User user = new User();
                        user.setContact(phone);
                        user.setName(next.getDisplayName());
                        user.setKnows_me(false);
                        users.add(user);
                    }
                }

            }
            userBox.put(users);
            if (fresh_contacts_list.isEmpty()) {
                List<String> objbox_user_unidi_contact_list = new ArrayList<>();
                List<User> x = userBox.query().equal(User_.knows_me, false).build().find();
                String temp2_contact_repeat = "";
                for (User u:x) {
                    if (!temp2_contact_repeat.equalsIgnoreCase(u.getContact()))
                        objbox_user_unidi_contact_list.add(u.getContact());
                    temp2_contact_repeat = u.getContact();
                }
//                        .property(User_.contact).distinct().findStrings();
//                List<String> objbox_user_unidi_contact_list = Arrays.asList(x);
                ApiCalls.syncContacts(this, 2, (objbox_user_unidi_contact_list));
            } else {
                ApiCalls.syncContacts(this, 3, fresh_contacts_list);
            }

        } catch (InterruptedException | JSONException e) {
            e.printStackTrace();
        }
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
    public void OnUpdateFriendList(UpdateFriendList event) {
        Log.d(TAG, event.message);
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            //Do something after 2000ms
            animationView.setVisibility(View.GONE);
            animationView.cancelAnimation();
        }, 2000);

        Query<User> query = userBox.query().order(User_.name).equal(User_.knows_me, true).build();
        friends_list_name = query.property(User_.name).findStrings();
        if (friends_list_name.length < 1) {
            animationView = (LottieAnimationView) findViewById(R.id.empty_animation);
            animationView.setAnimation("empty.json");
            animationView.setVisibility(View.VISIBLE);
            animationView.playAnimation();
        } else {
            adapter.clear();
            lst = new ArrayList<>(Arrays.asList(friends_list_name));
            adapter.addAll(lst);
            adapter.notifyDataSetChanged();
            handler.postDelayed(() -> {
                //Do something after 2000ms
                animationView.setVisibility(View.GONE);
                animationView.cancelAnimation();
            }, 2000);
        }
    }
}
