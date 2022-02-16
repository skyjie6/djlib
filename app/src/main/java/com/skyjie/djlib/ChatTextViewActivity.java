package com.skyjie.djlib;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.skyjie.djlib.view.CenterImageSpan;
import com.skyjie.djlib.view.ChatTextView;
import com.skyjie.djlib.view.LevelTextView;
import com.skyjie.djlib.view.LiveChatTextView;


public class ChatTextViewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_textview);

        float[] widths = new float[1];
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.getTextWidths(" ", widths);
        Log.d("djjjj", "" + widths[0]);

        final TextView textView = (TextView) findViewById(R.id.textview);
        SpannableString spannable = new SpannableString(" 234567890");

        //皇冠图标-居中
        Drawable drawable = getDrawable(R.drawable.gif_signup1);
        drawable.setBounds(0, 0, 72, 72);
        CenterImageSpan span1 = new CenterImageSpan(drawable);
        spannable.setSpan(span1, 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//        spannable.setSpan(new RelativeSizeSpan(0.5f), 2, 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        final LevelListDrawable levelListDrawable = new LevelListDrawable();
        Drawable empty = getResources().getDrawable(R.drawable.ic_launcher);
        levelListDrawable.addLevel(0, 0, empty);
        levelListDrawable.setBounds(0, 0, empty.getIntrinsicWidth(), empty.getIntrinsicHeight());
        Glide.with(this).load("https://www.baidu.com/img/PC_880906d2a4ad95f5fafb2e540c5cdad7.png").placeholder(R.drawable.ic_launcher).dontAnimate().into(new SimpleTarget<GlideDrawable>() {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                if (resource != null) {
                    levelListDrawable.addLevel(1, 1, resource);
                    levelListDrawable.setBounds(0, 0, 72, 72);
                    levelListDrawable.setLevel(1);
//                    CharSequence text = textView.getText();
//                    textView.setText(text);
//                    textView.refreshDrawableState();
                }
            }
        });
        CenterImageSpan span2 = new CenterImageSpan(levelListDrawable);
        spannable.setSpan(span2, 3, 6, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        textView.setText(spannable);







        final String htmlFor02 = "<img src='" + R.drawable.ic_launcher + "'>" +
                "<img src='http://img2.cxtuku.com/00/07/42/s557b25c7597.jpg'><img src='level'>asdlkjasldkjfas: ldjasldjasldjaslkdbvblksflozsjl;sfdlslsf;laskd;asd;asd;as;dkas;ldk;asdjlasjlasvlsajvl;asjvl;asfv";

        final LevelTextView levelTextView = (LevelTextView) findViewById(R.id.level);

        final ChatTextView chatTextView = (ChatTextView) findViewById(R.id.text2);

        levelTextView.post(new Runnable() {
                               @Override
                               public void run() {
                                   levelTextView.setDrawingCacheEnabled(true);
                                   Bitmap tBitmap = levelTextView.getDrawingCache();
                                   chatTextView.setTextBitmap(tBitmap);
                                   chatTextView.setHtmlText(htmlFor02);
                               }
                           });

//        final ImageView imgage= (ImageView) findViewById(R.id.image);
//
//        final String htmlFor03 = "网络图片测试："+ "<img src='http://glive.gomocdn.com/a5966-2017-10-31_headpic_T11021hFgqNBlt'>";
//        textView.setText(Html.fromHtml(htmlFor03, new Html.ImageGetter() {
//            @Override
//            public Drawable getDrawable(String source) {
////                Drawable drawable = getResources().getDrawable(id);
////                drawable.setBounds(0, 0, drawable.getIntrinsicWidth() ,drawable.getIntrinsicHeight());
//                return null;
//            }
//        }, null));
//
//        levelTextView.setLevel(20);
//        levelTextView.post(new Runnable() {
//            @Override
//            public void run() {
//                levelTextView.setDrawingCacheEnabled(true);
//                final Bitmap tBitmap = levelTextView.getDrawingCache();
//                final Drawable drawable = new BitmapDrawable(tBitmap);
//                drawable.setBounds(0, 0, tBitmap.getWidth(), tBitmap.getHeight());
//                imgage.setImageDrawable(drawable);
//                textView.setText(Html.fromHtml(htmlFor02, new Html.ImageGetter() {
//                    @Override
//                    public Drawable getDrawable(String source) {
//                int id = Integer.parseInt(source);
////                Drawable drawable = getResources().getDrawable(id);
////                drawable.setBounds(0, 0, drawable.getIntrinsicWidth() ,drawable.getIntrinsicHeight());
//                        return drawable;
//                    }
//                }, null));
//            }
//        });


        LiveChatTextView liveChatTextView = (LiveChatTextView) findViewById(R.id.live_chat_tv);
        liveChatTextView.setTextDrawable(new String[]{"http://img2.cxtuku.com/00/07/42/s557b25c7597.jpg", "http://img2.cxtuku.com/00/07/42/s557b25c7597.jpg", "http://img2.cxtuku.com/00/07/42/s557b25c7597.jpg"});

    }

}
