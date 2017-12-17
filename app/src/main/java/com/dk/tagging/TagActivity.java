//package com.dk.tagging;
//
//import android.app.Activity;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.TabLayout;
//import android.support.v7.app.AppCompatActivity;
//import android.util.Log;
//import android.view.View;
//import android.widget.EditText;
//import android.widget.ImageView;
//
//import com.bumptech.glide.Glide;
//import com.dk.App;
//import com.dk.graph.ApiCalls;
//import com.dk.main.R;
//import com.dk.models.Bucket;
//import com.dk.models.User;
//import com.dk.queue.GetBucketEvent;
//import com.dk.queue.SendBucketEvent;
//import com.github.jorgecastilloprz.FABProgressCircle;
//
//import org.greenrobot.eventbus.EventBus;
//import org.greenrobot.eventbus.Subscribe;
//import org.greenrobot.eventbus.ThreadMode;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.ArrayList;
//
//import io.objectbox.Box;
//
//public class TagActivity extends AppCompatActivity {
//
//
//    User user;
//    JSONObject jsonObject = new JSONObject();
//    ArrayList<BucketFragment> bucketFragments = new ArrayList<>();
//    CustomViewPager viewPager;
//    Activity act = this;
//    ViewPagerAdapter adapter;
//    FloatingActionButton floatingActionButton;
//    FABProgressCircle fabProgressCircle;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_tag);
//
//        viewPager = (CustomViewPager) findViewById(R.id.viewpager);
//
//        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
//        tabLayout.setupWithViewPager(viewPager);
//
//        long b = (long) getIntent().getExtras().get("UserId");
//
//        Box<User> userBox = ((App) getApplication()).getBoxStore().boxFor(User.class);
//
//        user = userBox.get(b);
//        viewPager.setPagingEnabled(false);
//
//        try {
//            SharedPreferences prefs = getSharedPreferences("my_oqatt_prefs", MODE_PRIVATE);
//            String uid = prefs.getString("uid", null);
//            jsonObject.put("uid", uid);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        if(user.buckets.isEmpty()){
//            ApiCalls.getUserBuckets(this,user);
//            Log.d(">>>>>>>>>>", "no buckets");
//
//        }
//        else {
//            setupViewPager(viewPager);
//        }
//        Glide.with(act).load("https://www.shoptagr.com/assets/tag_button.png").into((ImageView) findViewById(R.id.send_button));
//        ImageView add_tag = (ImageView) findViewById(R.id.send_button);
//        final EditText tag = (EditText) findViewById(R.id.tag_text);
//        add_tag.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                int current_frag = viewPager.getCurrentItem();
//                BucketFragment currentBucketFrag =  (BucketFragment)(adapter.getItem(current_frag));
//                ArrayList<String> currentList = currentBucketFrag.bucket.getTagLableList();
//
//                if (currentList.size() >= 11)
//                {
//                    Log.d(">>>>>>>>>>", "List full");
//                }
//                else if (currentList.contains(tag.getText().toString())){
//                    Log.d(">>>>>>>>>>", "Already exist");
//                }
//                else{
//                    BucketFragment b  = (BucketFragment) adapter.getItem(current_frag);
//                    b.addTag(tag.getText().toString());
//                }
//            }
//        });
//
//        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
//        fabProgressCircle  = (FABProgressCircle) findViewById(R.id.fabProgressCircle);
//
//        floatingActionButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                fabProgressCircle.show();
////                if (true){
//                    JSONArray bucket_json_list = new JSONArray();
//                    for (Bucket bucket : user.buckets) {
//                        JSONObject bucket_json = new JSONObject();
//                        try {
//                            bucket_json.put("bucket_name", bucket.getName());
//                            bucket_json.put("tags", new JSONArray(bucket.getTagLableList()));
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        bucket_json_list.put(bucket_json);
//                    }
//                    try {
//                        jsonObject.put("Buckets", bucket_json_list);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    Log.d(">>>>>>>>>>", String.valueOf(jsonObject));
//                    ApiCalls.sendBuckets(act, user, jsonObject);
////                }
////                else {
////                }
//            }
//        });
//
//    }
//
//
//    private void setupViewPager(CustomViewPager viewPager) {
//        adapter = new ViewPagerAdapter(getSupportFragmentManager());
//        for (Bucket bucket : user.buckets) {
//            BucketFragment bucket_frag = new BucketFragment();
//            Bundle bdl = new Bundle();
//            bdl.putLong("bucketId", bucket.getId());
//            bucket_frag.setArguments(bdl);
//            bucketFragments.add(bucket_frag);
//            adapter.addFrag(bucket_frag, bucket.getName());
//
//        }
//        viewPager.setAdapter(adapter);
//    }
//
//
//    // This method will be called when a RefreshEvent is posted (in the UI thread for Toast)
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onGetBucketEvent(GetBucketEvent event) {
//        Log.d(">>>>>>>>.", event.message);
//        setupViewPager(viewPager);
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onSendBucketEvent(SendBucketEvent event) {
//        Log.d(">>>>>>>>.", event.message);
//        fabProgressCircle.hide();
//    }
//
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        EventBus.getDefault().register(this);
//    }
//
//    @Override
//    public void onStop() {
//        EventBus.getDefault().unregister(this);
//        super.onStop();
//    }
//}