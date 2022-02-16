package com.skyjie.djlib;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import java.lang.reflect.Method;
import java.util.Locale;

/**
 * @author dangjie
 */
public class CustomAnimateActivity extends ListActivity {
    private String[] mTitles = { "圆角drawable", "frameanimate", "气泡view", "PulldownViewActivity", "StretchViewActivity", "srollowView", "lottleView", "chatTextview"};


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
                intent = new Intent(this, RoundImageDrawableActivity.class);
                break;
            case 1:
                intent = new Intent(this, AnimateActivity.class);
                break;
            case 2:
                intent = new Intent(this, PopupActivity.class);
                break;
            case 3:
                intent = new Intent(this, PulldownViewActivity.class);
                break;
            case 4:
                intent = new Intent(this, StretchViewActivity.class);
                break;
            case 5:
                intent = new Intent(this, ScrollerViewActivity.class);
                break;
            case 6:
                intent = new Intent(this, LottieActivity.class);
                break;
            case 7:
                intent = new Intent(this, ChatTextViewActivity.class);
            default:
                break;
        }

        startActivity(intent);
    }



    /**
     * 反射API-23 的 Setting.canDrawOverlays(Context)
     * @return
     */
    public static boolean isCallingPackageAllowedToDrawOverlays(Context context, int uid, String callingPackage, boolean throwException) {
        try {
            Class<?> clazz = Class.forName("android.provider.Settings");
            Method method = clazz.getMethod("isCallingPackageAllowedToDrawOverlays", Context.class, int.class, String.class, boolean.class);
            return (boolean) method.invoke(null, context, uid, callingPackage, throwException);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }


    public String GetCountryZipCode(){
        String CountryID="";
        String CountryZipCode="";

        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        //getNetworkCountryIso
        CountryID= manager.getSimCountryIso().toUpperCase();
        if (TextUtils.isEmpty(CountryID)) {
            CountryID = Locale.getDefault().getCountry().toUpperCase();
        }
        String[] rl=this.getResources().getStringArray(R.array.CountryCodes);
        for(int i=0;i<rl.length;i++){
            String[] g=rl[i].split(",");
            if(g[1].trim().equals(CountryID.trim())){
                CountryZipCode=g[0];
                break;
            }
        }
        return CountryZipCode;
    }
}
