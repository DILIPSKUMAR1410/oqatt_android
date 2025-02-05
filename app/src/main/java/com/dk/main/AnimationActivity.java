package com.dk.main;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.airbnb.lottie.LottieAnimationView;
import com.dk.utils.Utils;

/**
 * Created by dk on 06/03/18.
 */

public class AnimationActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_anim);
        final LottieAnimationView animationView = (LottieAnimationView) findViewById(R.id.animation);
        int type = getIntent().getIntExtra("type", 0);
        if (type == 1) {
            animationView.setAnimation("bolt.json");
        } else {
            {
                animationView.setAnimation("send.json");
            }

        }
        animationView.playAnimation();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 2000ms
                Log.d(">>>>>>>>>>", "In Animation ");
                animationView.cancelAnimation();
                Utils.redirectToMain(AnimationActivity.this);
            }
        }, 2000);
    }
}
