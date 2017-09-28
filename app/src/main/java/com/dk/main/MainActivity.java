package com.dk.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dk.App;
import com.dk.graph.ApiCalls;
import com.dk.models.User;
import com.dk.tagging.ProfileActivity;
import com.dk.tagging.UsersAdapter;

import org.json.JSONException;

import java.util.ArrayList;

import io.github.privacystreams.core.exceptions.PSException;
import io.objectbox.Box;

public class MainActivity extends AppCompatActivity {

    UsersAdapter adapter;
    ArrayList<User> StoreContacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StoreContacts = new ArrayList<>();
        Box<User> userBox = App.getInstance().getBoxStore().boxFor(User.class);
        Log.d(">>>>>>>>.", String.valueOf(userBox.count()));

        StoreContacts.addAll(userBox.getAll());
        adapter = new UsersAdapter(this, StoreContacts);
        final ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                User user_item = (User) listView.getItemAtPosition(position);
                intent.putExtra("UserId", user_item.getId());

                startActivity(intent);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.refresh:
                try {
                    ApiCalls.syncObjectBox(this);
                } catch (JSONException | InterruptedException | PSException e) {
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


}