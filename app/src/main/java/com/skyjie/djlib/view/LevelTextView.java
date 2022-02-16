package com.skyjie.djlib.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;

import androidx.appcompat.widget.AppCompatTextView;

import com.skyjie.djlib.R;


/**
 * 用户等级TextView
 * Created by dangjie on 17-3-24.
 */

public class LevelTextView extends AppCompatTextView {
    private final String[][] COLOR_GRADIENT = {{"#3eabf0", "#50e1ee"}, {"#2ddad0", "#4befa0"}, {"#67db49", "#b5e939"}, {"#ffac1b", "#ffe119"}, {"#ff7742", "#ffc23e"},  {"#ff503e", "#ff806f"},
            {"#ff486f", "#ffaf8c"}, {"#ff4abd", "#ff6cf3"}, {"#8f58ff", "#c06eff"}, {"#5b76ff", "#51bbff"}, {"#7191b7", "#a8c8e6"}, {"#d9a760", "#f7d187"}};
    private int mLevel = 1;

    private Paint mPaint;
    private RectF mRectF;
    private int mHeight;

    public LevelTextView(Context context, int level) {
        this(context, null);
        mLevel = level;
    }

    public LevelTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setGravity(Gravity.CENTER);
        setTextColor(Color.WHITE);
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
        setLevel(mLevel);
        initFonts();
    }

    private void initFonts() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/medium.ttf");
        setTypeface(tf);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mHeight == 0) {
            mHeight = 48;
        }
        setMeasuredDimension(90, mHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, 0, 0, 90, mHeight);
    }

    @Override
    public void setHeight(int height) {
        mHeight = height;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = h;
        //建立矩形坐标
        mRectF = new RectF(new Rect(0, 0, w, h));
        setShader();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //画左右是半圆的矩形，rx,ry分别表示四个角的弧度在x,y轴上的半径
        canvas.drawRoundRect(mRectF, mHeight / 2, mHeight / 2, mPaint);
        //绘制文字
        super.onDraw(canvas);
    }


    /**
     * 获取等级的背景颜色,写死一套规则
     */
    private int getLevelColor() {
        int index = mLevel / 10;
        if (index > 0) {
            return index +1;
        } else {
            if (mLevel > 4) {
                return 1;
            } else {
                return  0;
            }
        }
    }

    private Drawable getLevelDrawable() {
        int index = mLevel / 10;
        switch (index) {
            case 0:
            case 1:
                return getResources().getDrawable(R.drawable.ic_small_medal_0119);
            case 2:
            case 3:
                return getResources().getDrawable(R.drawable.ic_small_medal_2039);
            case 4:
            case 5:
                return getResources().getDrawable(R.drawable.ic_small_medal_4059);
            case 6:
            case 7:
                return getResources().getDrawable(R.drawable.ic_small_medal_6079);
            case 8:
            case 9:
                return getResources().getDrawable(R.drawable.ic_small_medal_8089);
            default:
                break;
        }
        return null;
    }

    public void setLevel(int level) {
        mLevel = level;
        if (level == 100) {
            Drawable drawable = getResources().getDrawable(R.drawable.ic_level_100);
            setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            setPadding(0, 0, 0, 0);
            setText("");
        } else {
            setShader();
//        SpannableString spannable = new SpannableString("Lv " + level);
//        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
//        //皇冠图标-居中
//        CenterImageSpan span = new CenterImageSpan(drawable);
//        spannable.setSpan(span, 0, 2, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//        spannable.setSpan(new RelativeSizeSpan(0.5f), 2, 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

            Drawable drawable = getLevelDrawable();
            setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

            if (level < 10) {
                setPadding(12, 0, 6, 0);
            } else {
                setPadding(9, 0, 9, 0);
            }

            setText("" + level);
        }
    }

    private void setShader() {
        //填入线性渐变色。ui图是左下到右上的对角线
        int color = getLevelColor();
        if (color < COLOR_GRADIENT.length) {
            String color1 = COLOR_GRADIENT[color][0];
            String color2 = COLOR_GRADIENT[color][1];
            LinearGradient lg = new LinearGradient(0, mHeight, getMeasuredWidth(), 0, Color.parseColor(color1), Color.parseColor(color2), Shader.TileMode.MIRROR);
            mPaint.setShader(lg);
        }
    }
}
