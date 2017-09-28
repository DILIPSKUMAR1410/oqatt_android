package com.dk.tagging;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dk.App;
import com.dk.main.R;
import com.dk.models.Bucket;
import com.dk.models.Tag;
import com.dk.models.Tag_;
import com.dk.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;

import static android.content.Context.MODE_PRIVATE;


public class TagFragment extends Fragment {
    View rootView;
    User user;
    JSONObject jsonObject = new JSONObject();
    JSONObject tags = new JSONObject();
    ArrayList<BucketFragment> bucketFragments = new ArrayList<>();

    public TagFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_tag, container, false);

        ViewPager viewPager = rootView.findViewById(R.id.viewpager);

        TabLayout tabLayout = rootView.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        long b = (long) getActivity().getIntent().getExtras().get("UserId");

        Box<User> userBox = ((App) getActivity().getApplication()).getBoxStore().boxFor(User.class);

        user = userBox.get(b);

        setupViewPager(viewPager);


        try {
            SharedPreferences prefs = getActivity().getSharedPreferences("my_oqatt_prefs", MODE_PRIVATE);
            String uid = prefs.getString("uid", null);
            jsonObject.put("uid", uid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Button button = rootView.findViewById(R.id.sync_button);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Box<Bucket> bucketBox = ((App) getActivity().getApplication()).getBoxStore().boxFor(Bucket.class);
                Box<Tag> tagBox = ((App) getActivity().getApplication()).getBoxStore().boxFor(Tag.class);

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
        return rootView;
    }


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getActivity().getSupportFragmentManager());

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

