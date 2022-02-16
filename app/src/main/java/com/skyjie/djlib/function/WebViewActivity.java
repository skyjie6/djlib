package com.skyjie.djlib.function;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.skyjie.djlib.Constants;
import com.skyjie.djlib.R;
import com.skyjie.djlib.view.webview.LiveWebView;


/**
 * Created by  on 7/19/16.
 */
public class WebViewActivity extends Activity {

    private static final String TAG = "WebViewActivity";

    public static void start(Context context, String url) {
        start(context, url, true);
    }

    public static void start(Context context, String url, boolean isShowTitle) {
        Intent intent = new Intent(context, WebViewActivity.class);
        if (!(context instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(Constants.Extra.WEBVIEW_URL, url);
        intent.putExtra(Constants.Extra.WEBVIEW_IS_SHOW_TITLE, isShowTitle);
        context.startActivity(intent);
    }

    private LiveWebView mWebView = null;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        mWebView = (LiveWebView) findViewById(R.id.webview);

        mWebView.setActionFromJSCallback(new LiveWebView.ActionFromJSCallback() {
            @Override
            public void onCloseAction() {
                finish();
            }


        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    if (View.GONE == mProgressBar.getVisibility()) {
                        mProgressBar.setVisibility(View.VISIBLE);
                    }
                    mProgressBar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }
        });

        processIntent(getIntent());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && mWebView != null) {
            //4.4+手机关闭webview的硬件加速,防止ThreadedRenderer.finalize()超时
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && mWebView != null) {
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
    }

    private void processIntent(Intent intent) {
        if (intent != null) {
            String url = intent.getStringExtra(Constants.Extra.WEBVIEW_URL);
            boolean isShowTitle = intent.getBooleanExtra(Constants.Extra.WEBVIEW_IS_SHOW_TITLE, true);
            if (url != null) {
                //加载需要显示的网页
            }
            mWebView.loadUrl("http://www.baidu.com/s?ie=utf-8&f=8&rsv_bp=1&tn=ubuntuu_dg&wd=asdsdsad&oq=asdsdsad&rsv_pq=dabccf9500003ee1&rsv_t=a46cW3eallMjjiUN9IyrBHfUGSqTyMlQeHVTuFqyUN7q3ibVYhwuRAReL%2BaW5ZkDFg&rqlang=cn&rsv_enter=0");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean flag = false;
        if (mWebView != null) {
            flag = mWebView.onKeyDown(keyCode, event);
        }
        if (!flag) {
            return super.onKeyDown(keyCode, event);
        }
        return flag;
    }
}
