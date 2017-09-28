package com.dk.tagging;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.dk.App;
import com.dk.main.R;
import com.dk.models.Bucket;
import com.dk.models.Tag;
import com.dk.models.User;
import com.github.fabtransitionactivity.SheetLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.objectbox.Box;

public class ProfileActivity extends AppCompatActivity implements SheetLayout.OnFabAnimationEndListener {

    private static final int REQUEST_CODE = 1;
    User user;
    JSONObject jsonObject = new JSONObject();
    JSONObject tags = new JSONObject();
    ArrayList<ProfileFragment> bucketFragments = new ArrayList<>();
    @BindView(R.id.bottom_sheet)
    SheetLayout mSheetLayout;
    @BindView(R.id.fab)
    FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ButterKnife.bind(this);

        mSheetLayout.setFab(mFab);
        mSheetLayout.setFabAnimationEndListener(this);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        long b = (long) getIntent().getExtras().get("UserId");

        Box<User> userBox = ((App) getApplication()).getBoxStore().boxFor(User.class);

        user = userBox.get(b);

        setupViewPager(viewPager);


        try {
            SharedPreferences prefs = getSharedPreferences("my_oqatt_prefs", MODE_PRIVATE);
            String uid = prefs.getString("uid", null);
            jsonObject.put("uid", uid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        for (Bucket bucket : user.buckets) {
            ProfileFragment bucket_frag = new ProfileFragment();
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


    @OnClick(R.id.fab)
    void onFabClick() {
        mSheetLayout.expandFab();
    }


    @Override
    public void onFabAnimationEnd() {
        Intent intent = new Intent(this, TagActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            mSheetLayout.contractFab();
        }
    }
}