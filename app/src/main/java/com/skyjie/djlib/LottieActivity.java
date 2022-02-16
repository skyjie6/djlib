package com.skyjie.djlib;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.airbnb.lottie.LottieAnimationView;

/**
 *
 * Created by dangjie on 17-9-22.
 */

public class LottieActivity extends Activity {
    private LottieAnimationView mLottieAnimationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lotte);
        mLottieAnimationView = (LottieAnimationView) findViewById(R.id.lottieView);
        mLottieAnimationView.setImageAssetsFolder("images/images");
        mLottieAnimationView.setAnimation("data.json");
    }

    public void playanim(View v) {
        mLottieAnimationView.playAnimation();
    }

}
