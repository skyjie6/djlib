package com.skyjie.djlib;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


import com.skyjie.djlib.ui.PullScrollView;
import com.skyjie.djlib.view.MenuPopwindow;

import java.util.ArrayList;
import java.util.List;


/**
 * Pull down ScrollView demo.
 *
 * @author markmjw
 * @date 2014-04-30
 */
public class PulldownViewActivity extends Activity implements PullScrollView.OnTurnListener {
    private PullScrollView mScrollView;
    private ImageView mHeadImg;

    private TableLayout mMainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_pull_down);

        initView();

        showTable();
    }

    protected void initView() {
        mScrollView = (PullScrollView) findViewById(R.id.scroll_view);
        mHeadImg = (ImageView) findViewById(R.id.background_img);

        mMainLayout = (TableLayout) findViewById(R.id.table_layout);

        mScrollView.setHeader(mHeadImg);
        mScrollView.setOnTurnListener(this);



        final TextView text = (TextView) findViewById(R.id.attention_user);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] icons = {R.drawable.add, R.drawable.add};
                String[] texts = {"编辑", "删除"};
                List<MenuPopwindowBean> list = new ArrayList<>();
                MenuPopwindowBean bean = null;
                for (int i = 0; i < icons.length; i++) {
                    bean = new MenuPopwindowBean();
                    bean.setIcon(icons[i]);
                    bean.setText(texts[i]);
                    list.add(bean);
                }
                MenuPopwindow pw = new MenuPopwindow(PulldownViewActivity.this, list);
                pw.setOnItemClick(null);
                pw.showPopupWindow(text);//点击右上角的那个button
            }
        });
    }

    public void showTable() {
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.leftMargin = 30;
        layoutParams.bottomMargin = 10;
        layoutParams.topMargin = 10;

        for (int i = 0; i < 30; i++) {
            TableRow tableRow = new TableRow(this);
            TextView textView = new TextView(this);
            textView.setText("Test pull down scroll view " + i);
            textView.setTextSize(20);
            textView.setPadding(15, 15, 15, 15);

            tableRow.addView(textView, layoutParams);
            if (i % 2 != 0) {
                tableRow.setBackgroundColor(Color.LTGRAY);
            } else {
                tableRow.setBackgroundColor(Color.WHITE);
            }

            final int n = i;
            tableRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(PulldownViewActivity.this, "Click item " + n, Toast.LENGTH_SHORT).show();
                }
            });

            mMainLayout.addView(tableRow);
        }
    }

    @Override
    public void onTurn() {

    }

}
