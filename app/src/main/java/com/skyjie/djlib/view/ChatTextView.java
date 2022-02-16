package com.skyjie.djlib.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.skyjie.djlib.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dangjie on 17-11-24.
 */

public class ChatTextView extends AppCompatTextView {

    private Paint paint;
    private Bitmap mTextBitmap;

    public ChatTextView(Context context) {
        this(context, null);
    }

    public ChatTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
    }

    public void setHtmlText(String text) {
        setText(Html.fromHtml(text, new MyImageGetter(this), null));
    }

    public void setTextBitmap(Bitmap mTextBitmap) {
        this.mTextBitmap = mTextBitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint.FontMetricsInt fm = paint.getFontMetricsInt();
        fm.descent = 10;

        super.onDraw(canvas);
    }

    //    @Override
//    protected void onDraw(Canvas canvas){
//        int lineHeight =this.getLineHeight();
//        int topPadding =this.getPaddingTop();
//        int leftPadding=this.getPaddingLeft();
//
//        float textSize =getTextSize();
//        setGravity(Gravity.LEFT|Gravity.TOP);
//        int y=(int)(topPadding + textSize);
//        for(int i=0; i<getLineCount(); i++){
//            canvas.drawLine(leftPadding,y+5,getRight()-leftPadding,y+5,paint);
//            y+=lineHeight;
//        }
//        canvas.translate(0, 0);
//        super.onDraw(canvas);
//    }

    public class MyImageGetter implements Html.ImageGetter {
        private TextView mTextView;
        private LevelListDrawable mLevelListDrawable;
        private SimpleTarget<GlideDrawable> mGlideDrawableSimpleTarget = new SimpleTarget<GlideDrawable>() {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                if (resource != null) {
                    mLevelListDrawable.addLevel(1, 1, resource);
                    mLevelListDrawable.setBounds(0, 0,resource.getIntrinsicWidth(), resource.getIntrinsicHeight());
                    mLevelListDrawable.setLevel(1);
                    CharSequence text = mTextView.getText();
                    mTextView.setText(text);
                    mTextView.refreshDrawableState();
                }
            }
        };

        public MyImageGetter(TextView textView) {
            mTextView = textView;
        }

        @Override
        public Drawable getDrawable(String source) {
            if (isNumeric(source)) {
                //本地资源
                int id = Integer.parseInt(source);
                Drawable drawable = ContextCompat.getDrawable(getContext(), id);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                return drawable;
            } else if (source.equals("level")) {
                //显示等级
                Drawable drawable = null;
                if (mTextBitmap != null) {
                    drawable = new BitmapDrawable(mTextBitmap);
                    drawable.setBounds(0, 0, mTextBitmap.getWidth(), mTextBitmap.getHeight());
                }
                return drawable;
            } else {
                //网络图片
                mLevelListDrawable = new LevelListDrawable();
                Drawable empty = getResources().getDrawable(R.drawable.ic_launcher);
                mLevelListDrawable.addLevel(0, 0, empty);
                mLevelListDrawable.setBounds(0, 0, empty.getIntrinsicWidth(), empty.getIntrinsicHeight());
                Glide.with(getContext()).load(source).placeholder(R.drawable.ic_launcher).dontAnimate().into(mGlideDrawableSimpleTarget);
                return mLevelListDrawable;
            }
        }
    }

    /**
     * 利用正则表达式判断字符串是否是数字
     */
    public static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }
}
