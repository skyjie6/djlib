package com.skyjie.djlib.view.webview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebBackForwardList;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LiveWebView extends WebView {
    private static class JsConstant {
        /**
         * 拦截键盘事件
         */
        static final String keyCode = "keyCode";
        static final String KEYCODE_BACK = "KEYCODE_BACK";

        /**
         * 统计
         */
        static final String STATISTICS_OP_CODE = "opCode";
        static final String STATISTICS_OBJECT = "object";
        static final String STATISTICS_ENTRANCE = "entrance";
        static final String STATISTICS_RELATION = "relation";
        static final String STATISTICS_TAB = "tab";
        static final String STATISTICS_POSITION = "position";
        static final String STATISTICS_REMARK = "remark";

        /**
         * 活动模板
         */
        static final String VCOIN_TYPE = "vcoin_type";
        static final String ACTIVITY_ID = "activity_id";
        static final String TASK_ARR = "task_arr";
        static final String TASK_ID = "task_id";
        static final String ERROR_CODE = "error_code";

        static final String ORIENTATION = "orientation";

        static final String ACCESS_TOKEN = "access_token";
    }

    private String mCurActivityId;

    public static class WebViewClientProxy {
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
        }

        public void onPageFinished(WebView view, String url) {
        }


        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

        }
    }

    public static class ActionFromJSCallback {
        public void onCloseAction() {
        }

        public void onDismissLoading() {
        }

        public void onGoUserProfile() {
        }

        public void onSetRequestedOrientation(String orientation) {
        }
    }

    private WebViewClientProxy mWebViewClientProxy;
    private ActionFromJSCallback mActionFromJSCallback;

    /**
     * 是否需要拦截返回事件
     */
    private boolean isNeedInterceptKeyEventBack;

    private String mStartUrl;
    private long mLastLoadTime;
    private int mCurrentIndex;

    private boolean isDestroyed = false;

    /**
     * 同步锁
     */
    private Lock lock = new ReentrantLock();

    public LiveWebView(Context context) {
        this(context, null);
    }

    public LiveWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LiveWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public Lock getLock() {
        return lock;
    }

    public void setWebViewClientProxy(WebViewClientProxy webViewClientProxy) {
        mWebViewClientProxy = webViewClientProxy;
    }

    public void setActionFromJSCallback(ActionFromJSCallback actionFromJSCallback) {
        this.mActionFromJSCallback = actionFromJSCallback;
    }

    private void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //用于4.4+远程调试
            WebView.setWebContentsDebuggingEnabled(true);
        }
        setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if ("about:blank".equals(url)) {
                    // 重定向到空白页直接跳过 2.x手机在开启文件选择后会出现这种异常跳转，原因未知
                    return false;
                }

                if (mStartUrl != null && mStartUrl.equals(url)) {
                    view.loadUrl(url);
                    return true;
                } else {
                    //交给系统处理
                    return super.shouldOverrideUrlLoading(view, url);
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (mWebViewClientProxy != null) {
                    mWebViewClientProxy.onPageStarted(view, url, favicon);
                }
                mStartUrl = url;
                long time = System.currentTimeMillis();
                if (time - mLastLoadTime > 1000) {
                    //体验优化，超过1秒的都记录下来，否则点返回时直接回去（防止重定向不断跳转）
                    mLastLoadTime = time;
                    WebBackForwardList list = copyBackForwardList();
                    if (list != null) {
                        mCurrentIndex = list.getCurrentIndex();
                    }
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (mWebViewClientProxy != null) {
                    mWebViewClientProxy.onPageFinished(view, url);
                }
                WebBackForwardList list = copyBackForwardList();
                if (list != null) {
                    mCurrentIndex = list.getCurrentIndex();
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                if (mWebViewClientProxy != null) {
                    mWebViewClientProxy.onReceivedError(view, errorCode, description, failingUrl);
                }
            }
        });

        setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype, long contentLength) {
                // TODO Auto-generated method stub
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                if (getContext() != null) {
                    getContext().startActivity(intent);
                }
            }
        });

        WebSettings webSettings = getSettings();
        webSettings.setAllowFileAccess(true); // 设置允许访问文件数据
        webSettings.setSavePassword(false); // 设置是否保存密码
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptEnabled(true); // 设置支持JavaScript脚本
        // webSettings.setSupportZoom(true); // 设置支持缩放
        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        String dir = getContext().getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();

        //启用地理定位
        webSettings.setGeolocationEnabled(true);
        //设置定位的数据库路径
        webSettings.setGeolocationDatabasePath(dir);
    }

    @Override
    public void destroy() {
        super.destroy();
        isDestroyed = true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isNeedInterceptKeyEventBack) {
                isNeedInterceptKeyEventBack = false;
                try {
                    JSONObject paramObject = new JSONObject();
                    paramObject.put(JsConstant.keyCode, JsConstant.KEYCODE_BACK);
                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            WebBackForwardList list = copyBackForwardList();
            if (list.getSize() > 2) {
//                for (int i = 2; i < list.getSize() - 1; i++) {
//                    if (mStartUrl.equals(list.getItemAtIndex(list.getSize() - i))) {
//                        mWebView.goBackOrForward(-i);
//                        return true;
//                    }
//                }
                goBackOrForward(mCurrentIndex - list.getCurrentIndex() - 1);
                return true;
            }

            if (canGoBack()) {
                // 返回上一页面
                goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * 设置屏幕方向
     */
    private void setRequestedOrientation(JSONObject paramObject) {
        if (paramObject != null) {
            String orientation = paramObject.optString(JsConstant.ORIENTATION);
            if (getContext() != null && getContext() instanceof Activity) {
                lock.lock();
                try {
                    if (!isDestroyed) {
                        if (mActionFromJSCallback != null) {
                            mActionFromJSCallback.onSetRequestedOrientation(orientation);
                        }
//                        if (Constants.ScreenOrientation.LANDSCAPE.equals(orientation)) {
//                            ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//                        } else if (Constants.ScreenOrientation.PORTRAIT.equals(orientation)) {
//                            ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//                        }
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
    }
}
