package com.dk.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.dk.App;
import com.dk.fragments.CreatePollFragment;
import com.dk.fragments.IncomingPollFragment;
import com.dk.fragments.OutgoingPollFragment;
import com.dk.graph.ApiCalls;
import com.dk.models.User;
import com.dk.queue.RefreshEvent;
import com.github.tamir7.contacts.Contact;
import com.github.tamir7.contacts.Contacts;
import com.github.tamir7.contacts.Query;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import eu.long1.spacetablayout.SpaceTabLayout;
import io.objectbox.Box;


public class MainActivity extends AppCompatActivity {


    SpaceTabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
//        updateUI();
        //add the fragments you want to display in a List
        List<Fragment> fragmentList = new ArrayList<>();
        final CreatePollFragment Cfragment = new CreatePollFragment();
        IncomingPollFragment Ifragment = new IncomingPollFragment();
        OutgoingPollFragment Ofragment = new OutgoingPollFragment();

        fragmentList.add(Ifragment);
        fragmentList.add(Cfragment);
        fragmentList.add(Ofragment);

        ViewPager viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.spaceTabLayout);

        //we need the savedInstanceState to get the position
        tabLayout.initialize(viewPager, getSupportFragmentManager(),
                fragmentList, savedInstanceState);

//        tabLayout.setTabTwoOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // do something
//                Log.d(">>>>>>>>.", String.valueOf("inside click"));
//                Poll poll = new Poll();
//                poll.setQuestion(String.valueOf(Cfragment.commentField.getText()));
//                int count = Cfragment.parent_linear_layout.getChildCount();
//                for (int i = 0; i < count; i++) {
//                    String EdittextID = "op" + i;
//                    int RadioButtonresID = getResources().getIdentifier(EdittextID, "id", getPackageName());
//                    EditText x = Cfragment.parent_linear_layout.findViewById(RadioButtonresID);
//                    poll.insertOption(String.valueOf(x.getText()));
//                }
//                Mention mention = (Mention) Cfragment.mentions.getInsertedMentions().get(0);
//                poll.subject.setTarget(mention.getMentionUser());
//                Box<Poll> pollBoxBox = App.getInstance().getBoxStore().boxFor(Poll.class);
//                long poll_id = pollBoxBox.put(poll);
//                try {
//                    ApiCalls.publishPoll(getApplicationContext(), poll_id);
//                } catch (JSONException | InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });


    }

    //we need the outState to save the position
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        tabLayout.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    // This method will be called when a RefreshEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {
        Log.d(">>>>>>>>.", event.message);
//        updateUI();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.refresh:
                try {
                    Box<User> userBox = App.getInstance().getBoxStore().boxFor(User.class);
                    List<User> objbox_user_list = userBox.getAll();

                    Contacts.initialize(this);
                    Query q = Contacts.getQuery();
                    q.hasPhoneNumber();
                    q.whereStartsWith(Contact.Field.PhoneNormalizedNumber, "+91");
                    List<Contact> contacts = q.find();
                    Iterator<Contact> itr = contacts.iterator();
                    Contact next;
                    ArrayList<User> fresh_contacts_list = new ArrayList<>();
                    while (itr.hasNext()) {
                        User user = new User();
                        next = itr.next();
                        user.setContact(String.valueOf(next.getPhoneNumbers().get(0).getNormalizedNumber()));
                        user.setName(next.getDisplayName());
                        user.setKnows_me(false);
                        fresh_contacts_list.add(user);
                    }
                    fresh_contacts_list.removeAll(objbox_user_list);
                    ArrayList<String> new_contacts = new ArrayList<>();
                    for (User u:fresh_contacts_list) {
                        new_contacts.add(u.getContact());
                    }
                    ApiCalls.syncContacts(this,0,new_contacts);
                } catch (InterruptedException | JSONException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.profile:
//                showHelp();
                return true;
            case R.id.help:
//                showHelp();
                return true;
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

    @Override
    public void onBackPressed() {
        // code here to show dialog
        this.finishAffinity();
        System.exit(0);
        super.onBackPressed();  // optional depending on your needs
    }

//    public void updateUI(){
//
//        Box<User> userBox = App.getInstance().getBoxStore().boxFor(User.class);
//        Log.d(">>>>>>>>.", String.valueOf(userBox.count()));
//        users_bi.clear();
//        users_uni.clear();
//        userBox.query().order(User_.name).filter(new QueryFilter<User>() {
//            @Override
//            public boolean keep(User user) {
//                if (user.getKnows_me()) {
//                    users_bi.add(user);
//                } else {
//                    users_uni.add(user);
//                }
//                return true;
//            }
//        }).build().find();
//
//        users_bi.addAll(users_uni);
//        adapter = new UsersAdapter(this);
//        adapter.setUserList(users_bi);
//
//        recyclerView.setAdapter(adapter);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.addItemDecoration(new DividerItemDecoration(this,
//                DividerItemDecoration.VERTICAL));
//        ((DragScrollBar) findViewById(R.id.dragScrollBar))
//                .setIndicator(new AlphabetIndicator(this), true);
//    }


}