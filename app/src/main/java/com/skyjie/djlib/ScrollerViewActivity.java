package com.skyjie.djlib;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.skyjie.djlib.view.MyScrollView;
import com.skyjie.djlib.view.PullScrollView;


/**
 * Created by dangjie on 17-7-17.
 */

public class ScrollerViewActivity extends Activity implements MyScrollView.OnScrollListener, Handler.Callback {
    private EditText search_edit;
    private MyScrollView myScrollView;
    private int searchLayoutTop;

    LinearLayout search01,search02;
    RelativeLayout rlayout;

    private Handler handler ;
    private PullScrollView test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolloer);
        //初始化控件
        init();
    }

    private void init() {
        search_edit = (EditText)findViewById(R.id.search_edit);
        myScrollView = (MyScrollView)findViewById(R.id.myScrollView);
        search01 = (LinearLayout)findViewById(R.id.search01);
        search02 = (LinearLayout)findViewById(R.id.search02);
        rlayout = (RelativeLayout)findViewById(R.id.rlayout);
        myScrollView.setOnScrollListener(this);
        handler = new Handler(getMainLooper(), this);

        test = (PullScrollView) findViewById(R.id.test);

        test.setOnRefreshListener(new PullScrollView.onRefreshListener() {

            @Override
            public void refresh() {
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        test.stopRefresh();
                    }
                }, 2000);
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            searchLayoutTop = rlayout.getBottom();//获取searchLayout的顶部位置
        }
    }

    //监听滚动Y值变化，通过addView和removeView来实现悬停效果
    @Override
    public void onScroll(int scrollY) {
        if(scrollY >= searchLayoutTop){
            if (search_edit.getParent()!=search01) {
                search02.removeView(search_edit);
                search01.addView(search_edit);
            }
        }else{
            if (search_edit.getParent()!=search02) {
                search01.removeView(search_edit);
                search02.addView(search_edit);
            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }
}
