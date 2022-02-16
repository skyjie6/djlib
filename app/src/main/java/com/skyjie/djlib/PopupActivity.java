package com.skyjie.djlib;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.skyjie.djlib.view.popupwindow.BubblePopupWindow;
import com.skyjie.djlib.view.popupwindow.LostPopupWindow;


/**
 *
 * Created by dangjie on 17-4-18.
 */

public class PopupActivity extends Activity {
    private BubblePopupWindow leftTopWindow;
    private BubblePopupWindow rightTopWindow;
    private BubblePopupWindow leftBottomWindow;
    private BubblePopupWindow rightBottomWindow;
    private BubblePopupWindow centerWindow;

    LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);
        leftTopWindow = new BubblePopupWindow(PopupActivity.this);
        rightTopWindow = new BubblePopupWindow(PopupActivity.this);
        leftBottomWindow = new BubblePopupWindow(PopupActivity.this);
        rightBottomWindow = new BubblePopupWindow(PopupActivity.this);
        centerWindow = new BubblePopupWindow(PopupActivity.this);

        inflater = LayoutInflater.from(this);


    }

    public void leftTop(View view) {
        View bubbleView = inflater.inflate(R.layout.layout_popup_view, null);
        TextView tvContent = (TextView) bubbleView.findViewById(R.id.tvContent);
        tvContent.setText("HelloWorld");
        leftTopWindow.setBubbleView(bubbleView);
        leftTopWindow.show(view, Gravity.BOTTOM);

        LostPopupWindow popWindow = new LostPopupWindow(this);
        ((TextView)(popWindow.getConentView().findViewById(R.id.item_content))).setText("查看评论");
        ((TextView)(popWindow.getConentView().findViewById(R.id.item_content1))).setText("发表评论");
        ((TextView)(popWindow.getConentView().findViewById(R.id.item_content2))).setText("转发文章");
        popWindow.showPopupWindow(view);
    }

    public void rightTop(View view) {
        View bubbleView = inflater.inflate(R.layout.layout_popup_view, null);
        rightTopWindow.setBubbleView(bubbleView);
        rightTopWindow.show(view, Gravity.LEFT, 0);
    }

    public void leftBottom(View view) {
        View bubbleView = inflater.inflate(R.layout.layout_popup_view, null);
        leftBottomWindow.setBubbleView(bubbleView);
        leftBottomWindow.show(view);
    }

    public void rightBottom(View view) {
        View bubbleView = inflater.inflate(R.layout.layout_popup_view, null);
        rightBottomWindow.setBubbleView(bubbleView);
        rightBottomWindow.show(view, Gravity.RIGHT, 0);
    }

    public void center(View view) {
        View bubbleView = inflater.inflate(R.layout.layout_popup_view, null);
        centerWindow.setBubbleView(bubbleView);
        centerWindow.show(view, Gravity.BOTTOM, 0);
    }
}
