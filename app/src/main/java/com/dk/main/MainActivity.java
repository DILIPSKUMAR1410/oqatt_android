package com.dk.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.dk.App;
import com.dk.graph.ApiCalls;
import com.dk.models.User;
import com.dk.models.User_;
import com.dk.tagging.UsersAdapter;
import com.turingtechnologies.materialscrollbar.AlphabetIndicator;
import com.turingtechnologies.materialscrollbar.DragScrollBar;

import org.json.JSONException;

import java.util.ArrayList;

import io.objectbox.Box;
import io.objectbox.query.QueryFilter;

public class MainActivity extends AppCompatActivity {

    UsersAdapter adapter;
    ArrayList<User> users_bi = new ArrayList<>();
    ArrayList<User> users_uni = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        Box<User> userBox = App.getInstance().getBoxStore().boxFor(User.class);
        Log.d(">>>>>>>>.", String.valueOf(userBox.count()));
        userBox.query().order(User_.name).filter(new QueryFilter<User>() {
            @Override
            public boolean keep(User user) {
                if (user.getKnows_me()) {
                    users_bi.add(user);
                } else {
                    users_uni.add(user);
                }
                return true;
            }
        }).build().find();

        users_bi.addAll(users_uni);
        adapter = new UsersAdapter(this);
        adapter.setUserList(users_bi);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        ((DragScrollBar) findViewById(R.id.dragScrollBar))
                .setIndicator(new AlphabetIndicator(this), true);


//        recyclerView.addOnItemTouchListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
//                User user_item = (User) listView.getItemAtPosition(position);
//                intent.putExtra("UserId", user_item.getId());
//
//                startActivity(intent);
//            }
//        });


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


}