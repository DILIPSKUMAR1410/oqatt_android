package com.dk.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.dk.App;
import com.dk.graph.ApiCalls;
import com.dk.models.Mention;
import com.dk.models.Poll;
import com.dk.queue.RefreshEvent;
import com.dk.tagging.CreatePollFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import eu.long1.spacetablayout.SpaceTabLayout;
import io.objectbox.Box;

//import com.dk.tagging.UsersAdapter;

public class MainActivity extends AppCompatActivity {

    //    UsersAdapter adapter;
//    ArrayList<User> users_bi = new ArrayList<>();
//    ArrayList<User> users_uni = new ArrayList<>();
//    RecyclerView recyclerView;
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
//        IncomingPollFragment Ifragment = new IncomingPollFragment();
//        OutgoingPollFragment Ofragment = new OutgoingPollFragment();

        fragmentList.add(Cfragment);
        fragmentList.add(Cfragment);
        fragmentList.add(Cfragment);

        ViewPager viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.spaceTabLayout);

        //we need the savedInstanceState to get the position
        tabLayout.initialize(viewPager, getSupportFragmentManager(),
                fragmentList, savedInstanceState);

        tabLayout.setTabTwoOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // do something
                Log.d(">>>>>>>>.", String.valueOf("inside click"));
                Poll poll = new Poll();
                poll.setQuestion(String.valueOf(Cfragment.commentField.getText()));
                int count = Cfragment.parent_linear_layout.getChildCount();
                EditText x = null;
                for (int i = 0; i < count; i++) {
                    x = (EditText) Cfragment.parent_linear_layout.getChildAt(i);
                    poll.insertOption(String.valueOf(x.getText()));
                }
                Mention mention = (Mention) Cfragment.mentions.getInsertedMentions().get(0);
                poll.subject.setTarget(mention.getMentionUser());
                Box<Poll> pollBoxBox = App.getInstance().getBoxStore().boxFor(Poll.class);
                long poll_id = pollBoxBox.put(poll);
                try {
                    ApiCalls.publishPoll(getApplicationContext(), poll_id);
                } catch (JSONException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });


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
                    ApiCalls.syncContacts(this);
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