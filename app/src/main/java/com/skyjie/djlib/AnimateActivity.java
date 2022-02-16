package com.skyjie.djlib;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.appcompat.widget.AppCompatImageView;

import com.skyjie.djlib.view.CustomProgress;
import com.skyjie.djlib.view.LevelTextView;

import java.lang.ref.WeakReference;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;

/**
 * @author dangjie on 17-4-18.
 */
public class AnimateActivity extends Activity {
    private RelativeLayout mRootView;
    private TextView mRelativeLayout;
    private CustomProgress mCustomProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animate);


        LevelTextView mLevelTextView = (LevelTextView) findViewById(R.id.tv_level);
        mLevelTextView.setLevel(10);

        final LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.addTransitionListener(new LayoutTransition.TransitionListener() {
            @Override
            public void startTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
                if (view.getId() == R.id.rl_viewgroup) {
                    if (transitionType == LayoutTransition.CHANGE_APPEARING) {
                    }
                }
            }

            @Override
            public void endTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
                if (view.getId() == R.id.rl_viewgroup) {
                    if (transitionType == LayoutTransition.CHANGE_DISAPPEARING) {
                        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mCustomProgress, "alpha", 0f, 1f).setDuration(200);
                        objectAnimator.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                mCustomProgress.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {

                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                        objectAnimator.start();
                    }
                }
            }
        });

        mRootView = (RelativeLayout) findViewById(R.id.rl_viewgroup);
        mRelativeLayout = (TextView) findViewById(R.id.btn_register);
        mRelativeLayout.setText(Html.fromHtml("sending <font color=#000000>" + ((45 + 15) / 1000) + "</font>"));
        mCustomProgress = (CustomProgress) findViewById(R.id.progress);

        mRootView.setLayoutTransition(layoutTransition);
        RelativeLayout rootView = (RelativeLayout) findViewById(R.id.activity_main);
        LevelImageView imageView = new LevelImageView(this);
        rootView.addView(imageView);

        //设置level资源.
        imageView.setImageResource(R.drawable.drawable_refresh_image);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        //新建动画.属性值从1-12的变化
        ObjectAnimator headerAnimator = ObjectAnimator.ofInt(imageView, "imageLevel", 1, 13);
        //设置动画的播放数量为一直播放.
        headerAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        //设置一个速度加速器.让动画看起来可以更贴近现实效果.
        headerAnimator.setInterpolator(new LinearInterpolator());
        headerAnimator.setRepeatMode(ObjectAnimator.RESTART);
        headerAnimator.setDuration(1600);
        headerAnimator.start();
    }

    // 提供一个用于属性动画操作的图片类。imageLevel不属于ImageView的属性，因此只能自己定义此属性。
    // 并通过属性动画进行修改。
    public static class LevelImageView extends AppCompatImageView {

        public LevelImageView(Context context) {
            super(context);
        }

        private int imageLevel = 0;

        @Override
        public void setImageLevel(int level) {
            if (this.imageLevel == level) {
                return;
            }
            super.setImageLevel(level);
            this.imageLevel = level;
            Log.d("djjj", "" + imageLevel);
        }

        public int getImageLevel() {
            return imageLevel;
        }

        private int maxLevel = 10;

        /**
         * 下一level接口
         */
        public void nextLevel() {
            setImageLevel(imageLevel++ % maxLevel);
        }


        public void setMaxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
        }
    }

    public void change(View v) {
        if (mRelativeLayout.getVisibility() == View.VISIBLE) {
            mRelativeLayout.setVisibility(GONE);
//            mCustomProgress.setVisibility(View.VISIBLE);
        } else {
            mRelativeLayout.setVisibility(View.VISIBLE);
            mCustomProgress.setVisibility(INVISIBLE);
        }
    }
}
