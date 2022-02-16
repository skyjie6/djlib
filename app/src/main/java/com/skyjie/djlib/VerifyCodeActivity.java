package com.skyjie.djlib;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.skyjie.djlib.manager.CountDownTimerManager;

import java.lang.ref.WeakReference;

/**
 * @author  dangjie
 */
public class VerifyCodeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);

        final TextView send = (Button) findViewById(R.id.btn_verify_code);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CountDownTimerManager.getInstance().startSameTimer(new WeakReference<>(send), getString(R.string.send), 60, 1);
            }
        });
    }
}
