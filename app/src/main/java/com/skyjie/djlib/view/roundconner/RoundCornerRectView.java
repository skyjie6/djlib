package com.skyjie.djlib.view.roundconner;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.skyjie.djlib.R;
import com.skyjie.djlib.utils.BitmapUtils;
import com.skyjie.djlib.utils.device.Machine;


/**
 * 圆角矩形(可根据高度，宽度，角高宽组出 圆形 操场形)
 * 直接放xml，设置android:background即可
 * @author dangjie on 17-3-31.
 */
public class RoundCornerRectView extends RelativeLayout {
    private Drawable mBackGround;

    private float mWidthR = -1;
    private float mHeightR = -1;
    private boolean mBitmapFit;

    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int COLOR_DRAWABLE_DIMENSION = 2;

    public RoundCornerRectView(Context context) {
        this(context, null);
    }

    public RoundCornerRectView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public RoundCornerRectView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttrs(context, attrs, defStyle);
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RoundCornerRectView, defStyleAttr, 0);
        mWidthR = typedArray.getDimension(R.styleable.RoundCornerRectView_cornerWidth, -1);
        mHeightR = typedArray.getDimension(R.styleable.RoundCornerRectView_cornerHeigh, -1);
        mBitmapFit = typedArray.getBoolean(R.styleable.RoundCornerRectView_scareTypeFit, false);
        typedArray.recycle();
    }

    public void setRoundLayoutRadius(float roundLayoutRadius) {
        this.mWidthR = mHeightR = roundLayoutRadius;
        postInvalidate();
    }

    @Override
    public void setBackground(Drawable background) {
        mBackGround = background;
        if (Machine.IS_JELLY_BEAN) {
            super.setBackground(mBackGround);
        } else {
            super.setBackgroundDrawable(mBackGround);
        }
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
        if (!Machine.IS_JELLY_BEAN) {
            mBackGround = background;
        }
        super.setBackgroundDrawable(background);
    }


    public void setBg(int w, int h) {
        if (mBackGround instanceof GradientDrawable) {
            //针对 渐变图案 做圆角转换
            GradientDrawable gradient = (GradientDrawable) mBackGround;
            gradient.setSize(w, h);
            Bitmap bitmap = Bitmap.createBitmap(w, h, BITMAP_CONFIG);
            Canvas canvas = new Canvas(bitmap);
            gradient.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            gradient.draw(canvas);
            super.setBackgroundDrawable(new PlayGroundDrawable(bitmap, mWidthR, mHeightR));
        } else if (mBackGround instanceof BitmapDrawable) {
            //针对 bitmap图 做圆角转换
            if (mBitmapFit && w > 0 && h > 0) {
                if (!((BitmapDrawable) mBackGround).getBitmap().isRecycled()) {
                    Bitmap bitmap = BitmapUtils.resizeBitmap(((BitmapDrawable) mBackGround).getBitmap(), w, h);
                    super.setBackgroundDrawable(new PlayGroundDrawable(bitmap, mWidthR, mHeightR));
                } else {
                    super.setBackgroundDrawable(null);
                }
            } else {
                super.setBackgroundDrawable(new PlayGroundDrawable(((BitmapDrawable) mBackGround).getBitmap(), mWidthR, mHeightR));
            }

        } else if (mBackGround instanceof ColorDrawable) {
            //针对 纯色 做圆角转换
            Bitmap bitmap = Bitmap.createBitmap(COLOR_DRAWABLE_DIMENSION, COLOR_DRAWABLE_DIMENSION, BITMAP_CONFIG);
            Canvas canvas = new Canvas(bitmap);
            mBackGround.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            mBackGround.draw(canvas);
            super.setBackgroundDrawable(new PlayGroundDrawable(bitmap, mWidthR, mHeightR));

        } else {
            super.setBackgroundDrawable(null);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //防止动态改变子view大小时,wrap_content失效
        Drawable drawable = mBackGround;
        setBackground(null);
        mBackGround = drawable;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        setBg(r - l, b - t);
    }

    /**
     * 重写圆角矩形drawable
     * Created by dangjie on 17-3-31.
     */
    private class PlayGroundDrawable extends Drawable {
        private Paint mPaint;
        private Bitmap mBitmap;

        private RectF rectF;

        private float mWidthR;
        private float mHeightR;

        PlayGroundDrawable(Bitmap bitmap, float widthR, float heighR) {
            mBitmap = bitmap;
            BitmapShader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setShader(bitmapShader);

            mWidthR = widthR;
            mHeightR = heighR;
        }

        @Override
        public void setBounds(int left, int top, int right, int bottom) {
            super.setBounds(left, top, right, bottom);
            rectF = new RectF(left, top, right, bottom);
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            // 如果没指定圆角宽度，则默认为左右圆角，所以原角宽和高都是矩形高。
            // 如果宽和高为矩形宽则是上下圆角
            // 这里的rectF必不为空，当背景为空时是不会走此处的draw()
            if (mWidthR < 0) {
                mWidthR = rectF.height() / 2;
            }
            if (mHeightR < 0) {
                mHeightR = rectF.height() / 2;
            }
            canvas.drawRoundRect(rectF, mWidthR, mHeightR, mPaint);
        }

        @Override
        public int getIntrinsicWidth() {
            return mBitmap.getWidth();
        }

        @Override
        public int getIntrinsicHeight() {
            return mBitmap.getHeight();
        }

        @Override
        public void setAlpha(int alpha) {
            mPaint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            mPaint.setColorFilter(cf);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }
}