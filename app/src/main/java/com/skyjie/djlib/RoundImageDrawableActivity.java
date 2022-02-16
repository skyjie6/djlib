package com.skyjie.djlib;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class RoundImageDrawableActivity extends Activity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_round_conner_drawble);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mv);
        final ImageView iv = (ImageView) findViewById(R.id.id_one);
        iv.setImageDrawable(new PlayGroundDrawable(bitmap));

        RelativeLayout iv2 = (RelativeLayout) findViewById(R.id.id_two);
        iv2.setBackgroundDrawable(new RoundImageDrawable(bitmap));

        mTextView = (TextView) findViewById(R.id.game_name);
    }

    public void test(View v) {
        mTextView.setText("asdasd");
    }
}
