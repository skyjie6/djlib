package com.skyjie.djlib.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.skyjie.djlib.R;

/**
 * Created by dangjie on 17-11-24.
 */

public class LiveChatTextView extends AppCompatTextView {

    public LiveChatTextView(Context context) {
        this(context, null);
    }

    public LiveChatTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setImageviewText(CharSequence text) {
        SpannableString spannable = new SpannableString("1234567890");

        //皇冠图标-居中
        Drawable drawable = getResources().getDrawable(R.drawable.gif_signup1);
        drawable.setBounds(0, 0, 72, 72);
        CenterImageSpan span1 = new CenterImageSpan(drawable);
        spannable.setSpan(span1, 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannable.setSpan(new RelativeSizeSpan(0.5f), 2, 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        final LevelListDrawable levelListDrawable = new LevelListDrawable();
        Drawable empty = getResources().getDrawable(R.drawable.ic_launcher);
        levelListDrawable.addLevel(0, 0, empty);
        levelListDrawable.setBounds(0, 0, empty.getIntrinsicWidth(), empty.getIntrinsicHeight());
        Glide.with(getContext()).load("http://img2.cxtuku.com/00/07/42/s557b25c7597.jpg").placeholder(R.drawable.ic_launcher).dontAnimate().into(new SimpleTarget<GlideDrawable>() {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                if (resource != null) {
                    levelListDrawable.addLevel(1, 1, resource);
                    levelListDrawable.setBounds(0, 0, 72, 72);
                    levelListDrawable.setLevel(1);
//                    CharSequence text = textView.getText();
//                    textView.setText(text);
//                    textView.refreshDrawableState();
                }
            }
        });
        CenterImageSpan span2 = new CenterImageSpan(levelListDrawable);
        spannable.setSpan(span2, 3, 6, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        setText(spannable);
    }

    public void setTextDrawable(String[] urls) {
        String string = "123";
        SpannableString spannable = new SpannableString(string);
        for (int i = 0; i < urls.length; i++) {
            final LevelListDrawable levelListDrawable = new LevelListDrawable();
            Drawable empty = getResources().getDrawable(R.drawable.ic_launcher);
            levelListDrawable.addLevel(0, 0, empty);
            levelListDrawable.setBounds(0, 0, 72, 72);
            Glide.with(getContext()).load(urls[i]).placeholder(R.drawable.ic_launcher).dontAnimate().into(new SimpleTarget<GlideDrawable>() {
                @Override
                public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                    if (resource != null) {
                        levelListDrawable.addLevel(1, 1, resource);
                        levelListDrawable.setBounds(0, 0, 72, 72);
                        levelListDrawable.setLevel(1);
                        CharSequence text = getText();
                        setText(text);
                        refreshDrawableState();
                    }
                }
            });
            CenterImageSpan span2 = new CenterImageSpan(levelListDrawable);
            spannable.setSpan(span2, i, i + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            setText(spannable);
        }
    }
}
