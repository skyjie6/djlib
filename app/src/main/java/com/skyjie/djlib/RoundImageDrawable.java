package com.skyjie.djlib;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;

public class RoundImageDrawable extends Drawable
{

	private Paint mPaint;
	private Bitmap mBitmap;

	private RectF rectF;

	public RoundImageDrawable(Bitmap bitmap)
	{
		mBitmap = bitmap;
		BitmapShader bitmapShader = new BitmapShader(bitmap, TileMode.REPEAT, TileMode.REPEAT);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setShader(bitmapShader);
	}

	@Override
	public void setBounds(int left, int top, int right, int bottom)
	{
		super.setBounds(left, top, right, bottom);
		rectF = new RectF(left, top, right, bottom);
	}

	@Override
	public void draw(Canvas canvas)
	{
		canvas.drawRoundRect(rectF, rectF.height() / 2 , rectF.height() / 2, mPaint);
	}

	@Override
	public int getIntrinsicWidth()
	{
		return mBitmap.getWidth();
	}

	@Override
	public int getIntrinsicHeight()
	{
		return mBitmap.getHeight();
	}

	@Override
	public void setAlpha(int alpha)
	{
		mPaint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf)
	{
		mPaint.setColorFilter(cf);
	}

	@Override
	public int getOpacity()
	{
		return PixelFormat.TRANSLUCENT;
	}

}
