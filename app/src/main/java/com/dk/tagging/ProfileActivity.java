package com.dk.tagging;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.dk.App;
import com.dk.main.R;
import com.dk.models.Bucket;
import com.dk.models.User;

import java.util.ArrayList;

import io.objectbox.Box;

public class ProfileActivity extends AppCompatActivity {
    User user;
    ArrayList<ProfileFragment> profileFragments = new ArrayList<>();
    CustomViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final long b = (long) getIntent().getExtras().get("UserId");
        Box<User> userBox = ((App) getApplication()).getBoxStore().boxFor(User.class);

        user = userBox.get(b);
//        viewPager = (CustomViewPager) findViewById(R.id.profile_viewpager);
//        viewPager.setPagingEnabled(false);
//        setupViewPager(viewPager);

        ImageView imageView = (ImageView) findViewById(R.id.tag);
        imageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TagActivity.class);
                intent.putExtra("UserId", b);
                startActivity(intent);

            }
        });

//        LinearLayout app_layer = (LinearLayout) findViewById (R.id.profile_buck1);
//        app_layer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                viewPager.setCurrentItem(0);
//            }
//        });
//        LinearLayout app_layer1 = (LinearLayout) findViewById (R.id.profile_buck2);
//        app_layer1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                viewPager.setCurrentItem(1);
//            }
//        });
//        LinearLayout app_layer2 = (LinearLayout) findViewById (R.id.profile_buck3);
//        app_layer2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                viewPager.setCurrentItem(2);
//            }
//        });
    }

    private void setupViewPager(CustomViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        for (Bucket bucket : user.buckets) {
            ProfileFragment prof_frag = new ProfileFragment();
            Bundle bdl = new Bundle();
            bdl.putLong("bucketId", bucket.getId());
            prof_frag.setArguments(bdl);
            profileFragments.add(prof_frag);
            adapter.addFrag(prof_frag, bucket.getName());
        }
        viewPager.setAdapter(adapter);
    }
}