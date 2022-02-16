package com.skyjie.djlib.function.capture;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.skyjie.djlib.R;


public class CaptureActivity extends Activity implements SystemCaptureHelper.SystemCaptureCallback {
    private SystemCaptureHelper mSystemCaptureHelper;
    private ImageView mPreView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        mPreView = (ImageView) findViewById(R.id.capture_img);
        mSystemCaptureHelper = new SystemCaptureHelper(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SystemCaptureHelper.SCREEN_CAPTURE_REQUEST:
                if (data != null && resultCode == RESULT_OK) {
                    // 用户同意使用截屏
                    if (mSystemCaptureHelper != null) {
                        mSystemCaptureHelper.startMPShot(this, resultCode, data);
                    }
                } else {
                    if (mSystemCaptureHelper != null && mSystemCaptureHelper.getCallback() != null) {
                        mSystemCaptureHelper.getCallback().onCaptureFinished(null);
                    }
                }
                break;
            default:
                break;
        }
    }

    public void capture1(View v) {
        mSystemCaptureHelper.startSysCap(this);
    }

    public void capture2(View v) {
        mSystemCaptureHelper.saveCurrentImage(this);
    }

    @Override
    public void onCaptureFinished(final Bitmap bitmap) {
        mPreView.post(() -> mPreView.setImageBitmap(bitmap));
    }
}
