package com.dk.main;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.dk.App;
import com.dk.graph.ApiCalls;
import com.dk.models.Poll;
import com.dk.models.Thread;
import com.dk.models.User;
import com.dk.models.User_;
import com.dk.queue.AddParticipants;
import com.dk.utils.Utils;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.Query;

public class SelectFriendsActivity extends AppCompatActivity {
    public Menu menu;
    ListView listview;
    String[] friends_list_name;
    String[] friends_list_contact;
    Box<User> userBox = App.getInstance().getBoxStore().boxFor(User.class);
    SparseBooleanArray sparseBooleanArray;
    ArrayList<String> selected_friends = new ArrayList<>();
    Poll poll;
    Thread thread;
    String hex;
    Box<Poll> pollBox = App.getInstance().getBoxStore().boxFor(Poll.class);
    Box<Thread> threadBox = App.getInstance().getBoxStore().boxFor(Thread.class);
    Boolean isNotMentioned;
    int total_other;
    User subject;
    LottieAnimationView animationView;
    ArrayAdapter<String> adapter;
    AlertDialog.Builder builder;
    boolean isPoll;
    int MIN_GROUP_SIZE = 2;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setContentView(R.layout.activity_select_friends);
        listview = findViewById(R.id.listView);
        isPoll = getIntent().getBooleanExtra("isPoll", true);
        if (isPoll) {
            poll = (Poll) getIntent().getSerializableExtra("poll");
            isNotMentioned = poll.subject.isNull();
            subject = poll.subject.getTarget();
        } else {
            long threadId = getIntent().getLongExtra("threadId", -1);
            thread = threadBox.get(threadId);
            threadBox.remove(threadId);
            isNotMentioned = thread.subject.isNull();
            subject = thread.subject.getTarget();
        }

        hex = getIntent().getStringExtra("hex");
        animationView = (LottieAnimationView) findViewById(R.id.populate_wait_animation);
        animationView.setAnimation("populate_wait.json");

        if (isNotMentioned) {
            getSupportActionBar().setTitle("Select friends");
            Query<User> query = userBox.query().order(User_.name).equal(User_.knows_me, true).build();
            friends_list_name = query.property(User_.name).findStrings();
            friends_list_contact = query.property(User_.contact).findStrings();
            adapter = new ArrayAdapter<String>
                    (this,
                            android.R.layout.simple_list_item_multiple_choice,
                            android.R.id.text1, friends_list_name);
            listview.setAdapter(adapter);

        } else {
            getSupportActionBar().setTitle("Select mutual friends");
            animationView.setVisibility(View.VISIBLE);
            animationView.playAnimation();
            try {
                ApiCalls.getFriendsConnections(SelectFriendsActivity.this, subject.getContact());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_select_friends, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.done_icon:
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Q-ask");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Question asked");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "event");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                selected_friends.clear();
                sparseBooleanArray = listview.getCheckedItemPositions();
                int i = 0;

                while (i < sparseBooleanArray.size()) {

                    if (sparseBooleanArray.valueAt(i)) {
                        selected_friends.add(friends_list_contact[sparseBooleanArray.keyAt(i)]);
                    }

                    i++;
                }


                if (!isNotMentioned) {
                    int ticked = 0;
                    if (selected_friends.contains("others")) {
                        ticked += total_other;
                    }
                    ticked += selected_friends.size();
                    if (ticked < MIN_GROUP_SIZE) {
                        Toast.makeText(SelectFriendsActivity.this, "Select atleast " + MIN_GROUP_SIZE + " friends !", Toast.LENGTH_LONG).show();

                    } else {
                        try {
                            if (isPoll) {
                                ApiCalls.publishMentionedPoll(SelectFriendsActivity.this, poll, hex, selected_friends);
                                Utils.redirectToAnim(SelectFriendsActivity.this, 0);
                            } else {
                                ApiCalls.publishMentionedThread(SelectFriendsActivity.this, thread, hex, selected_friends);
                                Utils.redirectToAnim(SelectFriendsActivity.this, 0);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                } else {
                    if (selected_friends.size() < MIN_GROUP_SIZE) {
                        Toast.makeText(SelectFriendsActivity.this, "Select atleast " + MIN_GROUP_SIZE + " friends !", Toast.LENGTH_LONG).show();
                    } else {
                        try {
                            if (isPoll) {
                                ApiCalls.publishOpenPoll(SelectFriendsActivity.this, poll, hex, selected_friends);
                                Utils.redirectToAnim(SelectFriendsActivity.this, 0);
                            } else {
                                ApiCalls.publishOpenThread(SelectFriendsActivity.this, thread, hex, selected_friends);
                                Utils.redirectToAnim(SelectFriendsActivity.this, 0);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }


            default:
                return super.onOptionsItemSelected(item);
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
    public void OnAddParticipants(AddParticipants event) {
        ArrayList<String> participants = new ArrayList<String>();
        if (event.mutual != null) {
            for (int i = 0; i < event.mutual.length(); i++) {
                try {
                    participants.add(event.mutual.getString(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        List<User> mutual_friends_query = userBox.query()
                .filter((user) -> participants.contains(user.getContact()) && user.getKnows_me())
                .build().find();
        ArrayList<String> mutual_friends_name = new ArrayList<String>();
        ArrayList<String> mutual_friends_contact = new ArrayList<String>();

        for (User u : mutual_friends_query) {
            mutual_friends_name.add(u.name);
            mutual_friends_contact.add(u.getContact());
        }
        adapter = new ArrayAdapter<String>
                (this,
                        android.R.layout.simple_list_item_multiple_choice,
                        android.R.id.text1, mutual_friends_name);
        if (event.unknown > 0) {
            total_other = event.unknown;
            adapter.add("Want to send to " + total_other + " other friends of " + subject.name + " ?");
            mutual_friends_contact.add("others");
        }
        friends_list_contact = mutual_friends_contact.toArray(new String[0]);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener((parent, view, position, id) -> {
            if (friends_list_contact.length == position + 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(this);
                }
                builder.setTitle("Carefully select this group")
                        .setMessage("We know you respect " + subject.name + ", please don't select this group if you are asking a personal question.")
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            // continue with delete
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });
        animationView.setVisibility(View.GONE);
        animationView.cancelAnimation();
    }

}
