package com.skyjie.djlib.function.capture;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 系统截图工具.5.0以上使用.
 * 实际使用的是录屏api，录取后获取一帧的bitmap
 */
public class SystemCaptureHelper {
    private static final String TAG = SystemCaptureHelper.class.getSimpleName();

    public static final int SCREEN_CAPTURE_REQUEST = 98;

    private Activity mActivity;

    private int mSysCapResult;

    private Intent mSysCapIntent;

    private MediaProjection mMediaProjection;
    private MediaProjectionManager mMediaProjectionManager;

    private ImageReader mImageReader;

    private SystemCaptureCallback mSystemCaptureCallback;

    public interface SystemCaptureCallback {
        void onCaptureFinished(Bitmap bitmap);
    }

    public SystemCaptureHelper(Activity activity) {
        mActivity = activity;
    }

    /**
     * 系统5.0以上的截屏
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startSysCap(SystemCaptureCallback systemCaptureCallback) {
        mSystemCaptureCallback = systemCaptureCallback;
        if (mMediaProjectionManager == null || mSysCapIntent == null || mSysCapResult == 0) {
            //没有截屏权限，向用户获取
            mMediaProjectionManager = (MediaProjectionManager) mActivity.getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            mActivity.startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), SCREEN_CAPTURE_REQUEST);
        } else {
            //直接截屏
            startMPShot(mActivity, mSysCapResult, mSysCapIntent);
        }
    }

    /**
     * 注意：准备阶段完成后需要等待1s或者更长时间才可以去操作截屏，否则有可能imageReader在进行newInstance时不成功
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startMPShot(final Context context, int sysCapResult, Intent sysCapIntent) {
        mSysCapResult = sysCapResult;
        mSysCapIntent = sysCapIntent;
        WindowManager windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        int virtulHeight = 0;
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        try {
            @SuppressWarnings("rawtypes")
            Class c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            virtulHeight = dm.heightPixels - windowManager.getDefaultDisplay().getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
        final int windowWidth = windowManager.getDefaultDisplay().getWidth();
        final int windowHeight = windowManager.getDefaultDisplay().getHeight() + virtulHeight;
        final DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        final int mScreenDensity = metrics.densityDpi;
        mImageReader = ImageReader.newInstance(windowWidth, windowHeight, PixelFormat.RGBA_8888, 2); //或者ImageFormat.RGB_565

        ScheduledExecutorService pools = Executors.newScheduledThreadPool(1);
        pools.execute(new Runnable() {
            @Override
            public void run() {
                if (mMediaProjection == null) {
                    mMediaProjection = mMediaProjectionManager.getMediaProjection(mSysCapResult, mSysCapIntent);
                }

                if (mMediaProjection != null) {
                    mMediaProjection.createVirtualDisplay("screen-mirror",
                            windowWidth, windowHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                            mImageReader.getSurface(), null, null);
                }
            }
        });

        pools.schedule(new Runnable() {
            @Override
            public void run() {
                startCapture();
            }
        }, 800, TimeUnit.MILLISECONDS);

        pools.schedule(new Runnable() {
            @Override
            public void run() {
                if (mMediaProjection != null) {
                    mMediaProjection.stop();
                    mMediaProjection = null;
                }
            }
        }, 1000, TimeUnit.MILLISECONDS);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startCapture() {
        //获取屏幕最新一帧
        Image image = mImageReader.acquireLatestImage();
        if (image == null) {
            //截屏失败
            return;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        //像素个数，RGBA为4 byte
        int pixelStride = planes[0].getPixelStride();
        //屏幕的真实宽度
        int rowStride = planes[0].getRowStride();
        //右边多余的宽度(透明)
        int rowPadding = rowStride - pixelStride * width;
        //将byte数据填入 真实宽度 * 真实高度
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        //填入数据
        bitmap.copyPixelsFromBuffer(buffer);
        //截取屏幕大小的部分
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        image.close();
        if (bitmap != null) {
            //得到bitmap
            if (mSystemCaptureCallback != null) {
                mSystemCaptureCallback.onCaptureFinished(bitmap);
            }
        } else {
            mSystemCaptureCallback.onCaptureFinished(null);
        }
    }



    // 这种方法状态栏是空白，显示不了状态栏的信息
    public void saveCurrentImage(SystemCaptureCallback systemCaptureCallback) {
        mSystemCaptureCallback = systemCaptureCallback;
        // 获取当前屏幕的大小
        int width = mActivity.getWindow().getDecorView().getRootView().getWidth();
        int height = mActivity.getWindow().getDecorView().getRootView().getHeight();
        // 生成相同大小的图片
        Bitmap temBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // 找到当前页面的跟布局
        View view = mActivity.getWindow().getDecorView().getRootView();
        // 设置缓存
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        // 从缓存中获取当前屏幕的图片
        temBitmap = view.getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);;

        if (temBitmap != null) {
            //得到bitmap
            if (mSystemCaptureCallback != null) {
                mSystemCaptureCallback.onCaptureFinished(temBitmap);
            }
        } else {
            mSystemCaptureCallback.onCaptureFinished(null);
        }
        view.setDrawingCacheEnabled(false);
    }


    public SystemCaptureCallback getCallback() {
        return mSystemCaptureCallback;
    }
}
