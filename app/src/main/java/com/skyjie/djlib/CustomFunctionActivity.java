package com.skyjie.djlib;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.skyjie.djlib.function.capture.CaptureActivity;
import com.skyjie.djlib.function.WebViewActivity;


/**
 * @author dangjie
 */
public class CustomFunctionActivity extends ListActivity {
    private String[] mTitles = { "截屏", "WebView"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getListView().setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mTitles));
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        Intent intent = null;
        switch (position)
        {
            case 0:
                intent = new Intent(this, CaptureActivity.class);
                break;
            case 1:
                intent = new Intent(this, WebViewActivity.class);
                break;
            default:
                break;
        }

        startActivity(intent);
    }
}
