package com.dk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.dk.graph.ApiCalls;
import com.dk.main.R;
import com.dk.models.User;
import com.dk.models.User_;
import com.dk.utils.Utils;

import org.json.JSONException;

import java.util.ArrayList;

import io.objectbox.Box;

public class SelectFriendsActivity extends AppCompatActivity {
    ListView listview ;
    String[] friends_list_name;
    String[] friends_list_contact;
    Box<User> userBox = App.getInstance().getBoxStore().boxFor(User.class);
    SparseBooleanArray sparseBooleanArray ;
    ArrayList<String> selected_friends = new ArrayList<>();
    long poll_id;
    String hex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friends);
        listview = findViewById(R.id.listView);
        friends_list_name= userBox.query().order(User_.name).equal(User_.knows_me, true).build().property(User_.name).findStrings();
        friends_list_contact= userBox.query().order(User_.name).equal(User_.knows_me, true).build().property(User_.contact).findStrings();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this,
                        android.R.layout.simple_list_item_multiple_choice,
                        android.R.id.text1, friends_list_name );

        listview.setAdapter(adapter);
        poll_id = getIntent().getLongExtra("poll_id",0);
        hex = getIntent().getStringExtra("hex");
        Button done = findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                sparseBooleanArray = listview.getCheckedItemPositions();
                int i = 0 ;

                while (i < sparseBooleanArray.size()) {

                    if (sparseBooleanArray.valueAt(i)) {

                        selected_friends.add(friends_list_contact [ sparseBooleanArray.keyAt(i) ]);
                    }

                    i++ ;
                }

                try {
                    ApiCalls.publishOpenPoll(SelectFriendsActivity.this, poll_id, hex,selected_friends);
                } catch (JSONException | InterruptedException e) {
                    e.printStackTrace();
                }

                Utils.redirectToAnim(SelectFriendsActivity.this);

            }
        });

    }



}
