package com.dk.tagging;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.dk.App;
import com.dk.main.R;
import com.dk.models.Bucket;
import com.dk.models.Me;
import com.dk.models.Tag;
import com.dk.models.Tag_;
import com.dk.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;

public class TagActivity extends AppCompatActivity {


    User user;
    JSONObject jsonObject = new JSONObject();
    JSONObject tags = new JSONObject();
    ArrayList<BucketFragment> bucketFragments = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        long b = (long) getIntent().getExtras().get("UserId");

        Box<User> userBox = ((App) getApplication()).getBoxStore().boxFor(User.class);

        user = userBox.get(b);

        setupViewPager(viewPager);


        try {
            jsonObject.put("uid", Me.getOurInstance().getUid());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Button button = (Button) findViewById(R.id.sync_button);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Box<Bucket> bucketBox = ((App) getApplication()).getBoxStore().boxFor(Bucket.class);
                Box<Tag> tagBox = ((App) getApplication()).getBoxStore().boxFor(Tag.class);

                try {
                    jsonObject.put("contact", user.getContact());
                    for (BucketFragment bucket_frag : bucketFragments) {
                        Bucket bucket = bucketBox.get(bucket_frag.getBucketId());
                        Log.d(">>>>>>bucket_id", bucket.getName() + ">>>>>>" + bucket.getId());
                        List<Tag> current_tag_list = getTagList(bucket_frag.nachoTextView.getChipValues());


                        bucket.tags.clear();

                        Log.d(">>>>>>Bucket_khali", String.valueOf(bucket.tags.isEmpty()));

                        for (Tag t : current_tag_list) {

                            List<Tag> t_exist = tagBox.find(Tag_.name, t.getName());

                            if (!t_exist.isEmpty()) {

                                bucket.tags.add(t_exist.get(0));

                            } else {
                                bucket.tags.add(t);
                            }
                        }

                        bucketBox.put(bucket);

                        try {
                            tags.put(bucket.getName(), new JSONArray(bucket_frag.nachoTextView.getChipValues()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    jsonObject.put("tags", tags);
                    Log.d(">>>>>>>>>>", String.valueOf(jsonObject));

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });


    }


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        for (Bucket bucket : user.buckets) {
            BucketFragment bucket_frag = new BucketFragment();
            Bundle bdl = new Bundle();
            bdl.putLong("bucketId", bucket.getId());
            bucket_frag.setArguments(bdl);
            bucketFragments.add(bucket_frag);
            adapter.addFrag(bucket_frag, bucket.getName());
        }
        viewPager.setAdapter(adapter);
    }

    List<Tag> getTagList(List<String> chipValues) {
        ArrayList<Tag> tags_objects = new ArrayList<>();
        for (String chip : chipValues) {
            Tag tag = new Tag();
            tag.setName(chip);
            tags_objects.add(tag);
        }
        return tags_objects;
    }

}