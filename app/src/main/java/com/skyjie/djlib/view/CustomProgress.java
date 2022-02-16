package com.skyjie.djlib.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ProgressBar;

/**
 * Created by dangjie on 17-4-20.
 */

public class CustomProgress extends ProgressBar{
    public CustomProgress(Context context) {
        super(context);
        setIndeterminateDrawable(new MaterialProgressDrawable(context, this));
    }

    public CustomProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        MaterialProgressDrawable drawable = new MaterialProgressDrawable(context, this);
        drawable.setBackgroundColor(Color.WHITE);
        setIndeterminateDrawable(drawable);
        drawable.setAlpha(255);
        drawable.start();
    }

    public CustomProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
