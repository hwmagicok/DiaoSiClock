package com.hw.weather1.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.hw.weather1.R;
import com.hw.weather1.util.LocalUtil;

/**
 * Created by hw on 2016/3/6.
 */
public class WeatherPageActivity extends Activity {
    private TextView countryName;
    private TextView temperature;
    private ImageView weatherIcon;
    private TextView iconDecribe;
    private TextView lastUpdata;

    public static final int[] iconArray = new int[] {R.drawable.p0, R.drawable.p1, R.drawable.p2, R.drawable.p3,
            R.drawable.p4, R.drawable.p5, R.drawable.p6, R.drawable.p7, R.drawable.p8, R.drawable.p9, R.drawable.p10,
            R.drawable.p12, R.drawable.p13, R.drawable.p14, R.drawable.p15, R.drawable.p16, R.drawable.p17, R.drawable.p18,
            R.drawable.p19, R.drawable.p20, R.drawable.p21, R.drawable.p22, R.drawable.p23, R.drawable.p24, R.drawable.p25,
            R.drawable.p26, R.drawable.p27, R.drawable.p28, R.drawable.p29, R.drawable.p30, R.drawable.p31, R.drawable.p32,
            R.drawable.p33, R.drawable.p34, R.drawable.p35, R.drawable.p36, R.drawable.p37, R.drawable.p38, R.drawable.p99
    };

    SharedPreferences weatherInfo;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.showweather);

        weatherInfo = getSharedPreferences("WeatherInfo", Context.MODE_PRIVATE);

        countryName = (TextView)findViewById(R.id.country_name);
        temperature = (TextView)findViewById(R.id.temperature);
        weatherIcon = (ImageView)findViewById(R.id.weather_icon);
        iconDecribe = (TextView)findViewById(R.id.icon_decribe);
        lastUpdata = (TextView)findViewById(R.id.last_updata);

        countryName.setText(weatherInfo.getString("CountryName", "error"));
        temperature.setText(weatherInfo.getString("Temperature", "error") + "℃");
        weatherIcon.setImageResource(iconArray[weatherInfo.getInt("PicCode", -1)]);
        iconDecribe.setText(weatherInfo.getString("WeatherStatus", "error"));

        String LastUpdataStr = weatherInfo.getString("LastUpdata", "error");
        String SplitedStr = new String("error");
        if(!LastUpdataStr.equals("error")) {
            SplitedStr = "最后更新于" + LocalUtil.splitLastUpdataData(LastUpdataStr);
        }
        lastUpdata.setText(SplitedStr);
    }
}
