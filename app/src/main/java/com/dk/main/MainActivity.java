package com.dk.main;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.dk.fragments.CreateFragment;
import com.dk.fragments.NotificationFragment;
import com.dk.fragments.ThreadFragment;
import com.dk.graph.ApiCalls;
import com.dk.queue.AppUpdateVersion;
import com.dk.queue.TokenBalance;
import com.dk.utils.DownloadNewVersion;
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
        final CreateFragment Cfragment = new CreateFragment();
        NotificationFragment Nfragment = new NotificationFragment();
        ThreadFragment Tfragment = new ThreadFragment();

        fragmentList.add(Cfragment);
        fragmentList.add(Tfragment);
        fragmentList.add(Nfragment);


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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnAppUpdateVersion(AppUpdateVersion event) {
        Log.d(">>>>>", String.valueOf(event.version));
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Update to new version")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    // continue with delete
                    DownloadNewVersion atualizaApp = new DownloadNewVersion();
                    atualizaApp.setversion(String.valueOf(event.version));
                    atualizaApp.setContext(MainActivity.this);
                    atualizaApp.execute();
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    // do nothing
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}