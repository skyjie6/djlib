package com.skyjie.djlib.utils;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RSRuntimeException;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.CursorLoader;

import com.bumptech.glide.load.resource.bitmap.ImageHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <br>类描述:
 * <br>功能详细描述:
 *
 * @author zxc
 * @date [16-5-20]
 */
public class BitmapUtils {

    public static final int UNCONSTRAINED = -1;

    public static int computeSampleSize(BitmapFactory.Options options,
                                        int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }
        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;

        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    /**
     * Compute the sample size as a function of minSideLength
     * and maxNumOfPixels.
     * minSideLength is used to specify that minimal width or height of a
     * bitmap.
     * maxNumOfPixels is used to specify the maximal size in pixels that is
     * tolerable in terms of memory usage.
     * <p>
     * The function returns a sample size based on the constraints.
     * Both size and minSideLength can be passed in as UNCONSTRAINED,
     * which indicates no care of the corresponding constraint.
     * The functions prefers returning a sample size that
     * generates a smaller bitmap, unless minSideLength = UNCONSTRAINED.
     * <p>
     * Also, the function rounds up the sample size to a power of 2 or multiple
     * of 8 because BitmapFactory only honors sample size this way.
     * For example, BitmapFactory downsamples an image by 2 even though the
     * request is 3. So we round up the sample size to avoid OOM.
     *
     * @param width
     * @param height
     * @param minSideLength
     * @param maxNumOfPixels
     * @return
     */
    public static int computeSampleSize(int width, int height,
                                        int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(
                width, height, minSideLength, maxNumOfPixels);

        return initialSize <= 8
                ? nextPowerOf2(initialSize)
                : (initialSize + 7) / 8 * 8;
    }

    private static int computeInitialSampleSize(int w, int h,
                                                int minSideLength, int maxNumOfPixels) {
        if (maxNumOfPixels == UNCONSTRAINED
                && minSideLength == UNCONSTRAINED) {
            return 1;
        }

        int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 :
                (int) Math.ceil(Math.sqrt((double) (w * h) / maxNumOfPixels));

        if (minSideLength == UNCONSTRAINED) {
            return lowerBound;
        } else {
            int sampleSize = Math.min(w / minSideLength, h / minSideLength);
            return Math.max(sampleSize, lowerBound);
        }
    }

    public static int nextPowerOf2(int n) {
        if (n <= 0 || n > (1 << 30)) {
            throw new IllegalArgumentException("n is invalid: " + n);
        }
        n -= 1;
        n |= n >> 16;
        n |= n >> 8;
        n |= n >> 4;
        n |= n >> 2;
        n |= n >> 1;
        return n + 1;
    }

    public static Bitmap resizeDownBySideLength(
            Bitmap bitmap, int maxLength, boolean recycle) {
        if (bitmap == null) {
            return null;
        }
        int srcWidth = bitmap.getWidth();
        int srcHeight = bitmap.getHeight();
        float scale = Math.min(
                (float) maxLength / srcWidth, (float) maxLength / srcHeight);
        if (scale >= 1.0f) {
            return bitmap;
        }
        return resizeBitmapByScale(bitmap, scale, recycle);
    }

    public static Bitmap resizeUpBySideLength(
            Bitmap bitmap, int maxLength, boolean recycle) {
        if (bitmap == null) {
            return null;
        }
        int srcWidth = bitmap.getWidth();
        int srcHeight = bitmap.getHeight();
        float scale = Math.max(
                (float) maxLength / srcWidth, (float) maxLength / srcHeight);
        if (scale >= 1.0f) {
            return bitmap;
        }
        return resizeBitmapByScale(bitmap, scale, recycle);
    }

    public static Bitmap resizeBitmapByScale(
            Bitmap bitmap, float scale, boolean recycle) {
        if (bitmap == null) {
            return null;
        }
        int width = Math.round(bitmap.getWidth() * scale);
        int height = Math.round(bitmap.getHeight() * scale);
        if (width == bitmap.getWidth()
                && height == bitmap.getHeight()) {
            return bitmap;
        }
        Bitmap target = Bitmap.createBitmap(width, height, getConfig(bitmap));
        Canvas canvas = new Canvas(target);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle) {
            bitmap.recycle();
        }
        return target;
    }

    public static Bitmap resizeBitmap(Bitmap bitmap, int width, int height, boolean recycle) {
        if (bitmap == null) {
            return null;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w == width && h == height) {
            return bitmap;
        }
        Bitmap target = Bitmap.createBitmap(width, height, getConfig(bitmap));
        Canvas canvas = new Canvas(target);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle) {
            bitmap.recycle();
        }
        return target;
    }

    public static Bitmap resizeBitmap(Bitmap bitmap, Rect src, Rect dest, boolean recycle) {
        if (bitmap == null) {
            return null;
        }
        Bitmap target = Bitmap.createBitmap(dest.width(), dest.height(), getConfig(bitmap));
        Canvas canvas = new Canvas(target);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, src, dest, paint);
        if (recycle) {
            bitmap.recycle();
        }
        return target;
    }

    private static Bitmap.Config getConfig(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        Bitmap.Config config = bitmap.getConfig();
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }
        return config;
    }

    public static Bitmap resizeAndCropCenter(Bitmap bitmap, int size, boolean recycle) {
        if (bitmap == null) {
            return null;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w == size && h == size) {
            return bitmap;
        }

        // scale the image so that the shorter side equals to the target;
        // the longer side will be center-cropped.
        float scale = (float) size / Math.min(w, h);

        Bitmap target = Bitmap.createBitmap(size, size, getConfig(bitmap));
        int width = Math.round(scale * bitmap.getWidth());
        int height = Math.round(scale * bitmap.getHeight());
        Canvas canvas = new Canvas(target);
        canvas.translate((size - width) / 2f, (size - height) / 2f);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle) {
            bitmap.recycle();
        }
        return target;
    }

    public static Bitmap resizeAndCropCenter(Bitmap bitmap, int sizeW, int sizeH, boolean recycle) {
        if (bitmap == null) {
            return null;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w == sizeW && h == sizeH) {
            return bitmap;
        }
        float scaleX = ((float) sizeW) / w;
        float scaleY = ((float) sizeH) / h;
        float scale = scaleX > scaleY ? scaleX : scaleY;
        Bitmap target = Bitmap.createBitmap(sizeW, sizeH, getConfig(bitmap));
        int width = Math.round(scale * w);
        int height = Math.round(scale * h);
        Canvas canvas = new Canvas(target);
        canvas.translate((sizeW - width) / 2f, (sizeH - height) / 2f);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle) {
            bitmap.recycle();
        }
        return target;
    }


    /**
     * 根据屏幕的大小，来缩放位图(降低质量)
     *
     * @param customWallpaperUri
     * @return
     * @throws Exception
     */
    public static Bitmap scaleBitmapByScreenSize(Context context, Uri customWallpaperUri) throws Exception {

        InputStream iStream = context.getContentResolver().openInputStream(customWallpaperUri);

        byte[] inputData = getBytes(iStream);

        iStream = context.getContentResolver().openInputStream(customWallpaperUri);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;


        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;

        BitmapFactory.decodeByteArray(inputData, 0, inputData.length, options);

        float widthScale = 1.0f * options.outWidth / screenWidth;
        float heightScale = 1.0f * options.outHeight / screenHeight;

        float minScale = widthScale > heightScale ? heightScale : widthScale;
        options.inJustDecodeBounds = false;
        if (minScale >= 2) {
            options.inSampleSize = (int) minScale;
            Bitmap bm = BitmapFactory.decodeByteArray(inputData, 0, inputData.length, options);
            return bm;
        } else if (minScale >= 1) {

            if (widthScale >= 3) {   //如果有一个边界大于3倍，则认为是需要裁剪的图
                int maxWidth = (int) (options.outWidth / widthScale * heightScale);
                int middleWidth = options.outWidth / 2;
                Bitmap bm = BitmapFactory.decodeStream(iStream, new Rect(middleWidth - maxWidth / 2, 0, middleWidth + maxWidth / 2, options.outHeight), options);
                return bm;
            } else if (heightScale >= 3) {  //如果
                int maxHeight = (int) (options.outHeight / heightScale * widthScale);
                int middleHeight = options.outHeight / 2;
                Bitmap bm = BitmapFactory.decodeStream(iStream, new Rect(0, middleHeight - maxHeight / 2, options.outWidth, middleHeight + maxHeight / 2), options);
                return bm;
            }
        }

        Bitmap bm = BitmapFactory.decodeByteArray(inputData, 0, inputData.length, options);
        return bm;
    }

    public static Bitmap glideBlur(Context context, Bitmap source) {
        int scaledWidth = source.getWidth();
        int scaledHeight = source.getHeight();

        Bitmap bitmap = bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(source, 0, 0, paint);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            try {
//                bitmap = RSBlur.blur(context, bitmap, 25);
            } catch (RSRuntimeException e) {
//                bitmap = FastBlur.blur(bitmap, 25, true);
            }
        } else {
//            bitmap = FastBlur.blur(bitmap, 25, true);
        }
        return bitmap;
    }

    /**
     * StackBlur By Java Bitmap
     *
     * @param original Original Image
     * @param radius   Blur radius
     * @return Image Bitmap
     */
    public static Bitmap blur(Bitmap original, int radius) {
        // Stack Blur v1.0 from
        // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
        //
        // Java Author: Mario Klingemann <mario at quasimondo.com>
        // http://incubator.quasimondo.com
        // created Feburary 29, 2004
        // Android port : Yahel Bouaziz <yahel at kayenko.com>
        // http://www.kayenko.com
        // ported april 5th, 2012

        // This is a compromise between Gaussian Blur and Box blur
        // It creates much better looking blurs than Box Blur, but is
        // 7x faster than my Gaussian Blur implementation.
        //
        // I called it Stack Blur because this describes best how this
        // filter works internally: it creates a kind of moving stack
        // of colors whilst scanning through the image. Thereby it
        // just has to add one new block of color to the right side
        // of the stack and remove the leftmost color. The remaining
        // colors on the topmost layer of the stack are either added on
        // or reduced by one, depending on if they are on the right or
        // on the left side of the stack.
        //
        // If you are using this algorithm in your code please add
        // the following line:
        //
        // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>
        if (radius < 1) {
            return null;
        }

        Bitmap bitmap = buildBitmap(original, false);

        // Return this none blur
        if (radius == 1) {
            return null;
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        // get array
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        // run Blur
        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rSum, gSum, bSum, x, y, i, p, yp, yi, yw;
        int vMin[] = new int[Math.max(w, h)];

        int divSum = (div + 1) >> 1;
        divSum *= divSum;
        int dv[] = new int[256 * divSum];
        for (i = 0; i < 256 * divSum; i++) {
            dv[i] = i / divSum;
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackPointer;
        int stackStart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routSum, goutSum, boutSum;
        int rinSum, ginSum, binSum;

        for (y = 0; y < h; y++) {
            rinSum = ginSum = binSum = routSum = goutSum = boutSum = rSum = gSum = bSum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = p & 0x0000ff;
                rbs = r1 - Math.abs(i);
                rSum += sir[0] * rbs;
                gSum += sir[1] * rbs;
                bSum += sir[2] * rbs;
                if (i > 0) {
                    rinSum += sir[0];
                    ginSum += sir[1];
                    binSum += sir[2];
                } else {
                    routSum += sir[0];
                    goutSum += sir[1];
                    boutSum += sir[2];
                }
            }
            stackPointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rSum];
                g[yi] = dv[gSum];
                b[yi] = dv[bSum];

                rSum -= routSum;
                gSum -= goutSum;
                bSum -= boutSum;

                stackStart = stackPointer - radius + div;
                sir = stack[stackStart % div];

                routSum -= sir[0];
                goutSum -= sir[1];
                boutSum -= sir[2];

                if (y == 0) {
                    vMin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vMin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = p & 0x0000ff;

                rinSum += sir[0];
                ginSum += sir[1];
                binSum += sir[2];

                rSum += rinSum;
                gSum += ginSum;
                bSum += binSum;

                stackPointer = (stackPointer + 1) % div;
                sir = stack[stackPointer % div];

                routSum += sir[0];
                goutSum += sir[1];
                boutSum += sir[2];

                rinSum -= sir[0];
                ginSum -= sir[1];
                binSum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinSum = ginSum = binSum = routSum = goutSum = boutSum = rSum = gSum = bSum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rSum += r[yi] * rbs;
                gSum += g[yi] * rbs;
                bSum += b[yi] * rbs;

                if (i > 0) {
                    rinSum += sir[0];
                    ginSum += sir[1];
                    binSum += sir[2];
                } else {
                    routSum += sir[0];
                    goutSum += sir[1];
                    boutSum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackPointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rSum] << 16) | (dv[gSum] << 8) | dv[bSum];

                rSum -= routSum;
                gSum -= goutSum;
                bSum -= boutSum;

                stackStart = stackPointer - radius + div;
                sir = stack[stackStart % div];

                routSum -= sir[0];
                goutSum -= sir[1];
                boutSum -= sir[2];

                if (x == 0) {
                    vMin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vMin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinSum += sir[0];
                ginSum += sir[1];
                binSum += sir[2];

                rSum += rinSum;
                gSum += ginSum;
                bSum += binSum;

                stackPointer = (stackPointer + 1) % div;
                sir = stack[stackPointer];

                routSum += sir[0];
                goutSum += sir[1];
                boutSum += sir[2];

                rinSum -= sir[0];
                ginSum -= sir[1];
                binSum -= sir[2];

                yi += w;
            }
        }

        // set Bitmap
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
        return bitmap;
    }

    private static Bitmap buildBitmap(Bitmap bitmap, boolean canReuseInBitmap) {
        // If can reuse in bitmap return this or copy
        Bitmap rBitmap;
        if (canReuseInBitmap) {
            rBitmap = bitmap;
        } else {
            Bitmap.Config config = bitmap.getConfig() != null ? bitmap.getConfig() : Bitmap.Config.ARGB_8888;
            rBitmap = bitmap.copy(config, true);
        }
        return rBitmap;
    }

    public static Bitmap fastBlur(Context context, Bitmap bitmap) {
        return fastBlur(context, bitmap, true);
    }

    public static Bitmap fastBlur(Context context, Bitmap bitmap, boolean recycle) {
        if (bitmap == null) {
            return null;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            //Let's create an empty bitmap with the same size of the bitmap we want to blur
            Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

            //Instantiate a new Renderscript
            RenderScript rs = RenderScript.create(context);

            //Create an Intrinsic Blur Script using the Renderscript
            ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

            //Create the Allocations (in/out) with the Renderscript and the in/out bitmaps
            Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
            Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

            //Set the radius of the blur
            blurScript.setRadius(25.f);

            //Perform the Renderscript
            blurScript.setInput(allIn);
            blurScript.forEach(allOut);

            //Copy the final bitmap created by the out Allocation to the outBitmap
            allOut.copyTo(outBitmap);

            //recycle the original bitmap
            if (recycle) {
                bitmap.recycle();
            }

            //After finishing everything, we release the Renderscript.
            rs.destroy();

            return outBitmap;
        } else {
            return doBlur(bitmap, 25, false, recycle);
        }
    }

    public static Bitmap doBlur(Bitmap sentBitmap, int radius, boolean canReuseInBitmap, boolean recycle) {

        // Stack Blur v1.0 from
        // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
        //
        // Java Author: Mario Klingemann <mario at quasimondo.com>
        // http://incubator.quasimondo.com
        // created Feburary 29, 2004
        // Android port : Yahel Bouaziz <yahel at kayenko.com>
        // http://www.kayenko.com
        // ported april 5th, 2012

        // This is a compromise between Gaussian Blur and Box blur
        // It creates much better looking blurs than Box Blur, but is
        // 7x faster than my Gaussian Blur implementation.
        //
        // I called it Stack Blur because this describes best how this
        // filter works internally: it creates a kind of moving stack
        // of colors whilst scanning through the image. Thereby it
        // just has to add one new block of color to the right side
        // of the stack and remove the leftmost color. The remaining
        // colors on the topmost layer of the stack are either added on
        // or reduced by one, depending on if they are on the right or
        // on the left side of the stack.
        //
        // If you are using this algorithm in your code please add
        // the following line:
        //
        // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

        Bitmap bitmap;
        if (canReuseInBitmap) {
            bitmap = sentBitmap;
        } else {
            bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
            if (recycle) {
                sentBitmap.recycle();
            }
        }

        if (radius < 1) {
            return null;
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = i / divsum;
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = p & 0x0000ff;
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = p & 0x0000ff;

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return bitmap;
    }

    /**
     * 获得圆角图片的方法
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
        if (bitmap == null) {
            return null;
        }
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    /**
     * 获取圆形图片
     *
     * @param bitmap 原图
     * @param size   圆形图大小
     * @return , int newWidth, int newHeight
     */
    public static Bitmap getCircleBitmap(Bitmap bitmap, int size) {
        if (bitmap == null) {
            return null;
        }
        //size是0则取图片大小
        if (0 == size) {
            size = bitmap.getHeight() > bitmap.getWidth() ? bitmap.getWidth() : bitmap.getHeight();
        }
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        // 创建画布
        Canvas canvas = new Canvas(output);
        //创建抗锯齿画笔
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        // 绘圆
        canvas.drawCircle(size / 2, size / 2, size / 2, paint);
        // 设置交叉模式
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        // 交叉后得到绘制图片
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return output;
    }


    /**
     * 按照指定宽高缩放图片后，并获得圆角图片的方法
     *
     * @param bitmap
     * @param roundPx
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx, int newWidth, int newHeight, float scale) {
        if (bitmap == null) {
            return null;
        }
        //获取Bitmap宽度
        int width = bitmap.getWidth();
        //获取Bitmap高度
        int height = bitmap.getHeight();
        Rect src = new Rect(0, 0, width, height);
        if (width == newWidth && height == newHeight) {
        } else {
            if (width * scale - newWidth > 0 || height * scale - newHeight > 0) {
                // 缩放并截取图片
                int x = width - newWidth / scale > 0 ? (int) (width - newWidth / scale) / 2 : 0;
                int y = height - newHeight / scale > 0 ? (int) (height - newHeight / scale) / 2 : 0;
                src.set(x, y, width - x, height - y);
            }
        }
        Bitmap newBitmap = null;
        try {
            newBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
        }
        if (newBitmap != null) {
            Canvas canvas = new Canvas(newBitmap);
            final int color = 0xff424242;
            final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
            final Rect rect = new Rect(0, 0, newBitmap.getWidth(), newBitmap.getHeight());
            final RectF rectF = new RectF(rect);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, src, rect, paint);
            return newBitmap;
        }
        return bitmap;
    }

    /**
     * 获取半圆角图片
     *
     * @param bitmap
     * @param roundPx
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static Bitmap getHalfRoundedCornerBitmap(Bitmap bitmap, float roundPx, int newWidth, int newHeight, float scale) {
        if (bitmap == null) {
            return null;
        }
        //获取Bitmap宽度
        int width = bitmap.getWidth();
        //获取Bitmap高度
        int height = bitmap.getHeight();
        Rect src = new Rect(0, 0, width, height);
        if (width == newWidth && height == newHeight) {
//			output = bitmap;
        } else {
            if (width * scale - newWidth > 0 || height * scale - newHeight > 0) {
                // 缩放并截取图片
//				output = Bitmap.createBitmap(Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true), width * scale - newWidth > 0 ? (int) (width * scale - newWidth) / 2 : 0, height * scale - newHeight > 0 ? (int) (height * scale - newHeight) / 2 : 0, newWidth, newHeight);
                int x = width - newWidth / scale > 0 ? (int) (width - newWidth / scale) / 2 : 0;
                int y = height - newHeight / scale > 0 ? (int) (height - newHeight / scale) / 2 : 0;
                src.set(x, y, width - x, height - y);
            }
        }
        Bitmap newBitmap = null;
        try {
            newBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
        }
        if (newBitmap != null) {
            Canvas canvas = new Canvas(newBitmap);
            final int color = 0xff424242;
            final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
            final Rect rect = new Rect(0, 0, newBitmap.getWidth(), (int) (newBitmap.getHeight() + roundPx) + 1);
            final RectF rectF = new RectF(rect);
//			paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, src, rect, paint);
            return newBitmap;
        }
        return bitmap;
    }

    /**
     * 获得圆角图片的方法
     * @param bitmap
     * @param roundPx
     * @return , int newWidth, int newHeight
     */
    /**
     * 按照指定宽高缩放图片后，并获得圆角图片的方法
     *
     * @param bitmap
     * @param roundPx
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx, int newWidth, int newHeight) {
        if (bitmap == null) {
            return null;
        }
//		Bitmap output;
        //获取Bitmap宽度
        int width = bitmap.getWidth();
        //获取Bitmap高度
        int height = bitmap.getHeight();
        Rect src = new Rect(0, 0, width, height);
        if (width == newWidth && height == newHeight) {
//			output = bitmap;
        } else {
//			Matrix matrix = new Matrix();
            //参考Bitmap高度获取缩放比例(高度)
            float scale = newHeight / (float) height;
            //如果缩放出来的宽度少于需要的宽度,则参照宽度比例缩放Bitmap.
            if (width * scale < newWidth) {
                scale = newWidth / (float) width;
            }
            //设置缩放比例
//			matrix.postScale(scale, scale);
            if (width * scale - newWidth > 0 || height * scale - newHeight > 0) {
                // 缩放并截取图片
//				output = Bitmap.createBitmap(Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true), width * scale - newWidth > 0 ? (int) (width * scale - newWidth) / 2 : 0, height * scale - newHeight > 0 ? (int) (height * scale - newHeight) / 2 : 0, newWidth, newHeight);
                int x = width - newWidth / scale > 0 ? (int) (width - newWidth / scale) / 2 : 0;
                int y = height - newHeight / scale > 0 ? (int) (height - newHeight / scale) / 2 : 0;
                src.set(x, y, width - x, height - y);
            }
        }
        Bitmap newBitmap = null;
        try {
            newBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
        }
        if (newBitmap != null) {
            Canvas canvas = new Canvas(newBitmap);
            final int color = 0xff424242;
            final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
            final Rect rect = new Rect(0, 0, newBitmap.getWidth(), newBitmap.getHeight());
            final RectF rectF = new RectF(rect);
//			paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, src, rect, paint);
            return newBitmap;
        }
        return bitmap;
    }

    public static Bitmap getHalfRoundedCornerBitmap(Bitmap bitmap, float roundPx, int newWidth, int newHeight) {
        if (bitmap == null) {
            return null;
        }
//		Bitmap output;
        //获取Bitmap宽度
        int width = bitmap.getWidth();
        //获取Bitmap高度
        int height = bitmap.getHeight();
        Rect src = new Rect(0, 0, width, height);
        if (width == newWidth && height == newHeight) {
//			output = bitmap;
        } else {
//			Matrix matrix = new Matrix();
            //参考Bitmap高度获取缩放比例(高度)
            float scale = newHeight / (float) height;
            //如果缩放出来的宽度少于需要的宽度,则参照宽度比例缩放Bitmap.
            if (width * scale < newWidth) {
                scale = newWidth / (float) width;
            }
            //设置缩放比例
//			matrix.postScale(scale, scale);
            if (width * scale - newWidth > 0 || height * scale - newHeight > 0) {
                // 缩放并截取图片
//				output = Bitmap.createBitmap(Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true), width * scale - newWidth > 0 ? (int) (width * scale - newWidth) / 2 : 0, height * scale - newHeight > 0 ? (int) (height * scale - newHeight) / 2 : 0, newWidth, newHeight);
                int x = width - newWidth / scale > 0 ? (int) (width - newWidth / scale) / 2 : 0;
                int y = height - newHeight / scale > 0 ? (int) (height - newHeight / scale) / 2 : 0;
                src.set(x, y, width - x, height - y);
            }
        }
        Bitmap newBitmap = null;
        try {
            newBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
        }
        if (newBitmap != null) {
            Canvas canvas = new Canvas(newBitmap);
            final int color = 0xff424242;
            final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
            final Rect rect = new Rect(0, 0, newBitmap.getWidth(), (int) (newBitmap.getHeight() + roundPx + 1));
            final RectF rectF = new RectF(rect);
//			paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, src, rect, paint);
            return newBitmap;
        }
        return bitmap;
    }

    public static Bitmap getDownHalfRoundedCornerBitmap(Bitmap bitmap, float roundPx, int newWidth, int newHeight) {
        if (bitmap == null) {
            return null;
        }
//		Bitmap output;
        //获取Bitmap宽度
        int width = bitmap.getWidth();
        //获取Bitmap高度
        int height = bitmap.getHeight();
        Rect src = new Rect(0, 0, width, height);
        if (width == newWidth && height == newHeight) {
//			output = bitmap;
        } else {
//			Matrix matrix = new Matrix();
            //参考Bitmap高度获取缩放比例(高度)
            float scale = newHeight / (float) height;
            //如果缩放出来的宽度少于需要的宽度,则参照宽度比例缩放Bitmap.
            if (width * scale < newWidth) {
                scale = newWidth / (float) width;
            }
            //设置缩放比例
//			matrix.postScale(scale, scale);
            if (width * scale - newWidth > 0 || height * scale - newHeight > 0) {
                // 缩放并截取图片
//				output = Bitmap.createBitmap(Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true), width * scale - newWidth > 0 ? (int) (width * scale - newWidth) / 2 : 0, height * scale - newHeight > 0 ? (int) (height * scale - newHeight) / 2 : 0, newWidth, newHeight);
                int x = width - newWidth / scale > 0 ? (int) (width - newWidth / scale) / 2 : 0;
                int y = height - newHeight / scale > 0 ? (int) (height - newHeight / scale) / 2 : 0;
                src.set(x, y, width - x, height - y);
            }
        }
        Bitmap newBitmap = null;
        try {
            newBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
        }
        if (newBitmap != null) {
            Canvas canvas = new Canvas(newBitmap);
            final int color = 0xff424242;
            final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
            final Rect rect = new Rect(0, (int) (-roundPx - 1), newBitmap.getWidth(), newBitmap.getHeight());
            final RectF rectF = new RectF(rect);
//			paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, src, rect, paint);
            return newBitmap;
        }
        return bitmap;
    }


    /**
     * 生成指定颜色和直径的圆
     *
     * @param circleR
     * @param color
     * @return
     */
    public static Bitmap getCircleWidthColor(int circleR, int color) {
        Bitmap newBitmap = null;
        try {
            newBitmap = Bitmap.createBitmap(circleR, circleR, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
        }
        if (newBitmap != null) {

            Canvas canvas = new Canvas(newBitmap);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(color);
            canvas.drawCircle(circleR / 2, circleR / 2, circleR / 2, paint);
        }

        return newBitmap;
    }

    /**
     * 生成指定颜色和直径的圆
     *
     * @param width
     * @param height
     * @param color
     * @return
     */
    public static Bitmap getRectangeWidthColor(int width, int height, int color) {
        Bitmap newBitmap = null;
        try {
            newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
        }
        if (newBitmap != null) {

            Canvas canvas = new Canvas(newBitmap);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(color);
            canvas.drawRect(0, 0, width, height, paint);
        }

        return newBitmap;
    }

    /**
     * 将本地图片文件转换成可解码二维码的 Bitmap。为了避免图片太大，这里对图片进行了压缩。感谢 https://github.com/devilsen 提的 PR
     *
     * @param picturePath 本地图片文件路径
     * @return
     */
    public static Bitmap getDecodeAbleBitmap(String picturePath) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(picturePath, options);
            int sampleSize = options.outHeight / 400;
            if (sampleSize <= 0) {
                sampleSize = 1;
            }
            options.inSampleSize = sampleSize;
            options.inJustDecodeBounds = false;

            return BitmapFactory.decodeFile(picturePath, options);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据图片的Uri获取图片的绝对路径(已经适配多种API)
     *
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    public static String getRealPathFromUri(Context context, Uri uri) {
        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion < 11) {
            // SDK < Api11
            return getRealPathFromUriBelowApi11(context, uri);
        }
        if (sdkVersion < 19) {
            // SDK > 11 && SDK < 19
            return getRealPathFromUriApi11To18(context, uri);
        }
        // SDK > 19
        return getRealPathFromUriAboveApi19(context, uri);
    }

    /**
     * 适配api19以上,根据uri获取图片的绝对路径
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String getRealPathFromUriAboveApi19(Context context, Uri uri) {
        if (context == null || uri == null) {
            return null;
        }
        try {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                    String docId = DocumentsContract.getDocumentId(uri);
                    String[] split = docId.split(":");
                    String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                    String id = DocumentsContract.getDocumentId(uri);
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(context, contentUri, null, null);
                } else if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                    String docId = DocumentsContract.getDocumentId(uri);
                    String[] split = docId.split(":");
                    String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    String selection = MediaStore.Images.Media._ID + "=?";
                    String[] selectionArgs = new String[]{split[1]};
                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            } // MediaStore (and general)
            else if ("content".equalsIgnoreCase(uri.getScheme())) {
                // Return the remote address
                if ("com.google.android.apps.photos.content".equals(uri.getAuthority())) {
                    return uri.getLastPathSegment();
                }
                return getDataColumn(context, uri, null, null);
            }
            // File
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        } catch (Exception e) {

        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * 适配api11-api18,根据uri获取图片的绝对路径
     */
    private static String getRealPathFromUriApi11To18(Context context, Uri uri) {
        String filePath = null;
        String[] projection = {MediaStore.Images.Media.DATA};

        CursorLoader loader = new CursorLoader(context, uri, projection, null,
                null, null);
        Cursor cursor = loader.loadInBackground();

        if (cursor != null) {
            cursor.moveToFirst();
            filePath = cursor.getString(cursor.getColumnIndex(projection[0]));
            cursor.close();
        }
        return filePath;
    }

    /**
     * 适配api11以下(不包括api11),根据uri获取图片的绝对路径
     */
    private static String getRealPathFromUriBelowApi11(Context context, Uri uri) {
        String filePath = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection,
                null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            filePath = cursor.getString(cursor.getColumnIndex(projection[0]));
            cursor.close();
        }
        return filePath;
    }

    /**
     * 旋转图片
     *
     * @param bm
     * @param orientationDegree
     * @return
     */
    public static Bitmap adjustPhotoRotation(Bitmap bm, final Float orientationDegree) {
        try {
            Bitmap bitmap = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.rotate(orientationDegree, bm.getWidth() / 2, bm.getHeight() / 2);
            canvas.save();
            canvas.drawBitmap(bm, 0, 0, null);
            canvas.restore();
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] getBytes(InputStream inputStream) throws Exception {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);  ///把流转化为Bitmap图片

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


    private static final String TAG = "BitmapUtility";

    public static final boolean saveBitmap(Bitmap bmp, String bmpName, Bitmap.CompressFormat format) {
        if (null == bmp) {
            return false;
        }
        FileOutputStream stream = null;
        try {
            File file = new File(bmpName);
            if (file.exists()) {
                boolean bDel = file.delete();
                if (!bDel) {
                    return false;
                }
            } else {
                File parent = file.getParentFile();
                if (null == parent) {
                    return false;
                }
                if (!parent.exists()) {
                    boolean bDir = parent.mkdirs();
                    if (!bDir) {
                        return false;
                    }
                }
            }
            boolean bCreate = file.createNewFile();
            if (!bCreate) {
                return false;
            }
            stream = new FileOutputStream(file);
            boolean bOk = bmp.compress(format, 100, stream);

            if (!bOk) {
                return false;
            }
        } catch (Exception e) {
            return false;
        } finally {
            if (null != stream) {
                try {
                    stream.close();
                } catch (Exception e2) {
                }
            }
        }
        return true;
    }


    public static Bitmap loadBitmap(Context context, Uri uri, int w, int h, int limitSize) {
        Bitmap result;
        if (uri == null || context == null) {
            return null;
        }
        final ParcelFileDescriptor parcelFileDescriptor;
        try {
            parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
        } catch (FileNotFoundException e) {
            return null;
        }

        final FileDescriptor fileDescriptor;
        if (parcelFileDescriptor != null) {
            fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        } else {
            return null;
        }

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
        if (options.outWidth == -1 || options.outHeight == -1) {
            return null;
        }
        if (limitSize > 0) {
            if (options.outWidth < limitSize || options.outHeight < limitSize) {
                return null;
            }
        }

        options.inSampleSize = calculateInSampleSize(options, w, h);
        options.inJustDecodeBounds = false;

        Bitmap decodeSampledBitmap = null;

        boolean decodeAttemptSuccess = false;
        while (!decodeAttemptSuccess) {
            try {
                decodeSampledBitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
                decodeAttemptSuccess = true;
            } catch (OutOfMemoryError error) {
                options.inSampleSize++;
            }
        }

        if (decodeSampledBitmap == null) {
            return null;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            close(parcelFileDescriptor);
        }

        int exifOrientation = getExifOrientation(context, uri);
        int exifDegrees = exifToDegrees(exifOrientation);
        int exifTranslation = exifToTranslation(exifOrientation);

        Matrix matrix = new Matrix();
        if (exifDegrees != 0) {
            matrix.preRotate(exifDegrees);
        }
        if (exifTranslation != 1) {
            matrix.postScale(exifTranslation, 1);
        }
        if (!matrix.isIdentity()) {
            result = transformBitmap(decodeSampledBitmap, matrix);
        } else {
            result = decodeSampledBitmap;
        }
        return result;
    }

    public static int calculateInSampleSize(@NonNull BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width lower or equal to the requested height and width.
            while ((height / inSampleSize) > reqHeight || (width / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap transformBitmap(@NonNull Bitmap bitmap, @NonNull Matrix transformMatrix) {
        try {
            Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), transformMatrix, true);
            if (bitmap != converted) {
                bitmap.recycle();
                bitmap = converted;
            }
        } catch (OutOfMemoryError error) {
        }
        return bitmap;
    }

    @SuppressWarnings("ConstantConditions")
    public static void close(@Nullable Closeable c) {
        if (c != null && c instanceof Closeable) { // java.lang.IncompatibleClassChangeError: interface not implemented
            try {
                c.close();
            } catch (IOException e) {
                // silence
            }
        }
    }

    private static int getExifOrientation(@NonNull Context context, @NonNull Uri imageUri) {
        int orientation = ExifInterface.ORIENTATION_UNDEFINED;
        try {
            InputStream stream = context.getContentResolver().openInputStream(imageUri);
            if (stream == null) {
                return orientation;
            }
            orientation = new ImageHeaderParser(stream).getOrientation();
            close(stream);
        } catch (IOException e) {
        }
        return orientation;
    }

    private static int exifToDegrees(int exifOrientation) {
        int rotation;
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
            case ExifInterface.ORIENTATION_TRANSPOSE:
                rotation = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                rotation = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
            case ExifInterface.ORIENTATION_TRANSVERSE:
                rotation = 270;
                break;
            default:
                rotation = 0;
        }
        return rotation;
    }

    private static int exifToTranslation(int exifOrientation) {
        int translation;
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
            case ExifInterface.ORIENTATION_TRANSPOSE:
            case ExifInterface.ORIENTATION_TRANSVERSE:
                translation = -1;
                break;
            default:
                translation = 1;
        }
        return translation;
    }

    /**
     * 重叠合并两张图片，合并后的大小等同于作为底图的图片大小
     *
     * @param background：下层图，即底图
     * @param foreground：上层图，即前置图
     * @return 合并后的Bitmap
     */
    public static Bitmap toConformBitmap(Bitmap background, Bitmap foreground, Paint paint) {
        if (null == background) {
            return null;
        }
        return toConformBitmapWithSize(background, foreground, background.getWidth(), background.getHeight(), paint);
    }

    public static Bitmap toConformBitmap(Bitmap background, Bitmap foreground, Paint paint, int marginRight, int marginCenterBottom) {
        if (null == background) {
            return null;
        }
        return toConformBitmapWithSize(background, foreground, background.getWidth(), background.getHeight(), paint, marginRight, marginCenterBottom);
    }

    /**
     * 重叠合并两张图片
     *
     * @param background：下层图，即底图
     * @param foreground：上层图，即前置图
     * @return 合并后的Bitmap
     */
    public static Bitmap toConformBitmapWithSize(Bitmap background, Bitmap foreground, int width, int height, Paint paint) {
        if (null == background) {
            return null;
        }

        if (null == foreground) {
            return background;
        }
        // int fgWidth = foreground.getWidth();
        // int fgHeight = foreground.getHeight();
        // create the new blank bitmap 创建一个新的和SRC长度宽度一样的位图
        Bitmap newbmp = null;
        try {
            newbmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            // OOM,return null
            return null;
        }
        if (newbmp == null) {
            return null;
        }
        Canvas cv = new Canvas(newbmp);
        int bgWidth = background.getWidth();
        int bgHeight = background.getHeight();
        int left;
        int top = 0;
        if (width > height) {
            float scale = (width + 0.01f) / height;
            left = (int) (bgHeight - bgWidth / scale) / 2;
            top = (int) (bgHeight - bgWidth / scale) / 2;
        } else {
            float scale = (height + 0.01f) / width;
            left = (int) (bgWidth - bgHeight / scale) / 2;
        }
        // draw bg into
        cv.drawBitmap(background, new Rect(left, top, bgWidth - left, bgHeight - top), new Rect(0, 0, width, height), paint);
        // draw fg into
        if (null != foreground) {
            cv.translate(-(foreground.getWidth() - width) / 2,
                    -(foreground.getHeight() - height) / 2);
            // 在居中绘制fg ，可以从任意位置画入
            cv.drawBitmap(foreground, 0, 0, paint);
        }
        return newbmp;
    }

    public static Bitmap toConformBitmapWithSize(Bitmap background, Bitmap foreground, int width, int height, Paint paint, int marginRight, int marginCenterBottom) {
        if (null == background) {
            return null;
        }

        if (null == foreground) {
            return background;
        }
        // int fgWidth = foreground.getWidth();
        // int fgHeight = foreground.getHeight();
        // create the new blank bitmap 创建一个新的和SRC长度宽度一样的位图
        Bitmap newbmp = null;
        try {
            newbmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            // OOM,return null
            return null;
        }
        Canvas cv = new Canvas(newbmp);
        int bgWidth = background.getWidth();
        int bgHeight = background.getHeight();
        float scale = (height + 0.01f) / width;
        int left = (int) (bgWidth - bgHeight / scale) / 2;
        // draw bg into
        cv.drawBitmap(background, new Rect(left, 0, bgWidth - left, bgHeight), new Rect(0, 0, width, height), paint);
        // draw fg into
        if (null != foreground) {
            cv.translate(width - foreground.getWidth() - marginRight, height / 2 - marginCenterBottom - foreground.getHeight());
            cv.drawBitmap(foreground, 0, 0, paint);
        }
        return newbmp;
    }


    /**
     * 放大or缩小图片
     */
    public static Bitmap resizeBitmap(Bitmap background, int width, int height) {
        Bitmap newbmp;
        try {
            newbmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            // OOM,return null
            return null;
        }
        if (background == null || newbmp == null) {
            return null;
        }
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        Canvas cv = new Canvas(newbmp);
        int bgWidth = background.getWidth();
        int bgHeight = background.getHeight();
        float scale = (height + 0.01f) / width;
        int left = (int) (bgWidth - bgHeight / scale) / 2;
        // draw bg into
        cv.drawBitmap(background, new Rect(Math.abs(left), 0, bgWidth - Math.abs(left), bgHeight), new Rect(0, 0, width, height), paint);
        return newbmp;
    }

    public static int getTextHeight(String text, Paint paint) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int height = bounds.bottom + bounds.height();
        return height;
    }

    public static Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.TRANSPARENT);
        }
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }
}
