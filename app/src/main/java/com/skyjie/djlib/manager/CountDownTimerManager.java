package com.skyjie.djlib.manager;

import android.graphics.Color;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;


import com.skyjie.djlib.R;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author dangjie on 17-6-16.
 * 发送验证码的倒数计时器
 */
public class CountDownTimerManager {

    private static class CountDownTimerHolder {
        private static CountDownTimerManager instance = new CountDownTimerManager();
    }

    private CountDownTimerManager() {
    }

    public static CountDownTimerManager getInstance() {
        return CountDownTimerHolder.instance;
    }

    private CountDownTimer mCountDownTimer;
    private WeakReference<TextView> mTvVerifyCode;
    private int mMaxTime, mInterval;
    private String mDefaultString = "";

    private AtomicBoolean mIsFinish = new AtomicBoolean(true);

    private void init() {
        // 由于CountDownTimer并不是准确计时，在onTick方法调用的时候，time会有1-10ms左右的误差，这会导致最后一秒不会调用onTick()
        // 因此，设置间隔的时候，默认减去了10ms，从而减去误差。
        // 经过以上的微调，最后一秒的显示时间会由于10ms延迟的积累，导致显示时间比1s长max*10ms的时间，其他时间的显示正常,总时间正常
        mCountDownTimer = new CountDownTimer(mMaxTime * 1000, mInterval * 1000 - 10) {

            @Override
            public void onTick(long time) {
                mIsFinish.set(false);
                // 第一次调用会有1-10ms的误差，因此需要+15ms，防止第一个数不显示，第二个数显示2sT
                if(null == mTvVerifyCode.get()) {
                    this.cancel();
                } else {
                    int second = (int) ((time + 15) / 1000);
                    String send = mTvVerifyCode.get().getResources().getString(R.string.sending_code);
                    SpannableString spannableString = new SpannableString(send + " " + second);
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), send.length(), spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mTvVerifyCode.get().setText(spannableString);
                }
            }

            @Override
            public void onFinish() {
                mIsFinish.set(true);
                if(null == mTvVerifyCode.get()) {
                    this.cancel();
                    return;
                }
                mTvVerifyCode.get().setEnabled(true);
                mTvVerifyCode.get().setText(mDefaultString);
            }
        };
    }

    public void startSameTimer(WeakReference<TextView> tvVerifyCode, String defaultString, int max, int interval) {
        mDefaultString = defaultString;
        tvVerifyCode.get().setEnabled(false);
        //保证字体颜色生效
        tvVerifyCode.get().setAllCaps(false);
        if (mIsFinish.get()) {
            mMaxTime = max;
            mInterval = interval;
            mTvVerifyCode = tvVerifyCode;
            init();
            mCountDownTimer.start();
        } else {
            mTvVerifyCode = tvVerifyCode;
        }
    }

    public void setDefaultString(String defaultString) {
     mDefaultString = defaultString;
    }

    public void finish() {
        if (mCountDownTimer != null) {
            mCountDownTimer.onFinish();
            mCountDownTimer.cancel();
        }
    }
}
