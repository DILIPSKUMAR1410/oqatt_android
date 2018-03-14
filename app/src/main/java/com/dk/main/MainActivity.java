package com.dk.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.dk.fragments.CreatePollFragment;
import com.dk.fragments.IncomingPollFragment;
import com.dk.fragments.OutgoingPollFragment;
import com.dk.graph.ApiCalls;
import com.dk.queue.TokenBalance;
import com.dk.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import eu.long1.spacetablayout.SpaceTabLayout;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = ">>>>>>Main";
    public SpaceTabLayout tabLayout;
    public Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        Log.d(TAG, "In Main");
        setContentView(R.layout.activity_main);
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

    }

    //we need the outState to save the position
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        tabLayout.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_main, menu);
        try {
            ApiCalls.getTokenBalance(this);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }


    @SuppressLint("StaticFieldLeak")
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.refresh:
                Utils.redirectToFriendList(this);
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onBackPressed() {
        // code here to show dialog
        this.finishAffinity();
        System.exit(0);
        super.onBackPressed();  // optional depending on your needs
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
    public void OnTokenBalance(TokenBalance event) {
        Log.d(">>>>>", event.message);
        menu.getItem(1).setTitle(event.message);
    }

}