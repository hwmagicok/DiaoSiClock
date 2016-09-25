package com.hw.diaosiclock.util;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.hw.diaosiclock.R;
import com.hw.diaosiclock.activity.WeatherDetailActivity;
import com.hw.diaosiclock.model.Alarm;
import com.hw.diaosiclock.model.AlarmBackgroundService;
import com.hw.diaosiclock.model.AlarmDB;
import com.hw.diaosiclock.model.Country;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

/**
 * Created by hw on 2016/2/20.
 */
public class LocalUtil {
    public static final String ERRTAG = "LocalUtil";
    public static final String TAG_EXECUTE_ALARM = "execute alarm";
    public static final String TAG_SET_MUSIC = "SetAlarmMusic";
    public static final int TAG_ONETIME_ALARM = 1;
    public static final int TAG_REPEAT_ALARM = 2;
    public static final int FLAG_REOPEN_SERVICE = 100;

    public static final String AlarmMusicPath = "Alarm";
    public static final String LocationFilePath = "JsonLocation.txt";
    public static final String SharedPreferenceName = "WeatherData";

    public static final String URL = "https://api.heweather.com/x3/weather?cityid=";
    private static final String APIKEY = "8d52bc3c89964e5290c5cdc3e28f626e";

    public static final int FLAG_HOURLY_CHART = 1;
    public static final int FLAG_DAILY_CHART = 2;

    public static String getApiKey() {
        return APIKEY;
    }

    // 载入所有城市信息
    public static synchronized void LoadAllCountry(Context context) {
        if(null == context) {
            return;
        }

        InputStream in;
        BufferedReader reader;
        String str;
        StringBuffer buffer;
        List<Country> list;


        try {
            in = context.getAssets().open(LocationFilePath);
            buffer = new StringBuffer();
            reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            while(null != (str = reader.readLine())) {
                buffer.append(str);
            }

            list = JSON.parseArray(buffer.toString(), Country.class);
            AlarmDB db = AlarmDB.getInstance(context);
            for(Country country : list) {
                db.saveCountryInfo(country);
            }


        }catch (Exception e) {
            Log.getStackTraceString(e);
        }

    }

    // 系统中周几的表示转化成本程序中Db中周几的表示
    public static int SysWeekToDbWeek(int sysWeek) {
        int dbWeek;
        if(sysWeek <= 0 || sysWeek > 7) {
            Log.e(ERRTAG, "sysWeek is wrong");
            return -1;
        }

        if(Calendar.SUNDAY == sysWeek) {
            dbWeek = 6;
        }else {
            dbWeek = sysWeek - 2;
        }
        return dbWeek;
    }

    // Db中周几的表示转化成系统中周几的表示
    public static int DbWeekToSysWeek(int dbWeek) {
        int sysWeek;
        if(dbWeek < 0 || dbWeek > 6) {
            Log.e(ERRTAG, "dbWeek is wrong");
            return -1;
        }

        sysWeek = (dbWeek + 2) % Calendar.DAY_OF_WEEK;
        return sysWeek;
    }

    // 获得月末周六的时间，时间保存在calendar中，传进来的calendar需确保跟现有年月一致无误
    public static void getLastSaturdayOfMonth(Calendar calendar, Alarm alarm) {
        if(null == alarm) {
            Log.e(ERRTAG, "alarm is null");
            return;
        }
        if(null == calendar) {
            calendar = Calendar.getInstance();
        }

        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getTimeHour());
        calendar.set(Calendar.MINUTE, alarm.getTimeMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        if(Calendar.SUNDAY == weekDay) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }else if(Calendar.SATURDAY != weekDay) {
            calendar.add(Calendar.DAY_OF_MONTH, -weekDay);
        }
    }

    // 使用alarm中的音乐进行播放
    public static boolean playAlarmMusic(MediaPlayer mediaPlayer, Context context, Alarm alarm, Boolean isLoop) {
        String MusicName = alarm.getAlarmMusic();
        if(null == context) {
            Log.e(ERRTAG, "context is null");
            return false;
        }

        // 若Alarm没有设置闹铃音乐（默认），就播放这个
        if(null == MusicName) {
            MusicName = "alarm.ogg";
        }
        boolean bRet = true;
        if(null == mediaPlayer) {
            mediaPlayer = new MediaPlayer();
        }
        //mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        mediaPlayer.reset();

        try {
            AssetFileDescriptor AlarmMusicDescriptor;
            AssetManager assetManager = context.getAssets();

            AlarmMusicDescriptor = assetManager.openFd(AlarmMusicPath + "/" + MusicName);
            if(isLoop) {
                mediaPlayer.setLooping(true);
            }else {
                mediaPlayer.setLooping(false);
            }
            mediaPlayer.setDataSource(AlarmMusicDescriptor.getFileDescriptor(),
                    AlarmMusicDescriptor.getStartOffset(), AlarmMusicDescriptor.getLength());
            mediaPlayer.setVolume(alarm.getVolume(), alarm.getVolume());
            AlarmMusicDescriptor.close();
            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch (Exception e) {
            bRet = false;
            Log.e(ERRTAG, "playing alarm music occurs error");
            Log.getStackTraceString(e);
        }
        return bRet;
    }

    // 使用music name代表的音乐进行播放，上一个方法的重载
    public static boolean playAlarmMusic(MediaPlayer mediaPlayer, Context context, String MusicName, Boolean isLoop) {

        if(null == context) {
            Log.e(ERRTAG, "context is null");
            return false;
        }

        // 若Alarm没有设置闹铃音乐（默认），就播放这个
        if(null == MusicName) {
            MusicName = "alarm.ogg";
        }
        boolean bRet = true;
        if(null == mediaPlayer) {
            mediaPlayer = new MediaPlayer();
        }
        //mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        mediaPlayer.reset();

        try {
            AssetFileDescriptor AlarmMusicDescriptor;
            AssetManager assetManager = context.getAssets();
            AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

            String AlarmMusicName = MusicName;
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);

            AlarmMusicDescriptor = assetManager.openFd(AlarmMusicPath + "/" + AlarmMusicName);
            if(isLoop) {
                mediaPlayer.setLooping(true);
            }
            mediaPlayer.setDataSource(AlarmMusicDescriptor.getFileDescriptor(),
                    AlarmMusicDescriptor.getStartOffset(), AlarmMusicDescriptor.getLength());
            mediaPlayer.setVolume(maxVolume/2, maxVolume/2);
            AlarmMusicDescriptor.close();
            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch (Exception e) {
            bRet = false;
            Log.e(ERRTAG, "playing alarm music occurs error");
            Log.getStackTraceString(e);
        }
        return bRet;
    }

    // 重启闹钟服务，应用于开机自启或是进程被关后重新打开应用
    public synchronized static void reopenAlarmService(Context context, Alarm alarm) {
        if(null == context || null == alarm) {
            Log.e(ERRTAG, "context or alarm is null");
            return;
        }

        Intent intent = new Intent(context, AlarmBackgroundService.class);
        intent.putExtra(TAG_EXECUTE_ALARM, alarm.getAlarmID());
        intent.addFlags(FLAG_REOPEN_SERVICE);
        context.startService(intent);
    }

    // 从天气API处获得天气信息后，存入本地SharedPreferences
    public static synchronized boolean saveWeatherInfoToLocal(Context context, JSONObject response) {
        if(null == response) {
            Log.e(ERRTAG, "weather response is null");
            return false;
        }

        boolean bRet = true;
        try {
            JSONArray data_service = response.getJSONArray("HeWeather data service 3.0");
            JSONObject data_service_obj = data_service.getJSONObject(0);
            // aqi是空气质量指数
            JSONObject aqi = data_service_obj.getJSONObject("aqi");
            JSONObject basic = data_service_obj.getJSONObject("basic");
            JSONArray daily_forecast = data_service_obj.getJSONArray("daily_forecast");
            JSONArray hourly_forecast = data_service_obj.getJSONArray("hourly_forecast");
            JSONObject now = data_service_obj.getJSONObject("now");

            SharedPreferences.Editor editor = context.getSharedPreferences(SharedPreferenceName,
                    Context.MODE_PRIVATE).edit();
            /* 数据存放分几个部分，首先分为当日信息，其余六天的天气预报，每小时的天气预报。
            其中当日信息分为：城市名称、城市代码、当前温度、风向(dir)、风力等级(sc)、配图代码、pm2.5
            七天预报分为（包含今天）：最高、最低气温，配图代码
            每小时天气预报分为（一共八个小时）：时间、温度
             */

            /* start:当日信息 */
            editor.putString("code", basic.getString("id"));
            editor.putString("country", basic.getString("city"));
            editor.putInt("temperature", now.getInt("tmp"));
            editor.putInt("cond_code", now.getJSONObject("cond").getInt("code"));
            editor.putString("wind_direction", now.getJSONObject("wind").getString("dir"));
            editor.putString("wind_degree", now.getJSONObject("wind").getString("sc"));
            editor.putInt("pm25", aqi.getJSONObject("city").getInt("pm25"));
            editor.putString("update", basic.getJSONObject("update").getString("loc"));
            /* end:当日信息 */

            /* start:七天预报 */
            JSONObject daily_forecast_obj;
            HashSet<String> dailyForecastSet = new HashSet<>();
            for(int i = 0; i < daily_forecast.length(); i++) {
                daily_forecast_obj = daily_forecast.getJSONObject(i);
                dailyForecastSet.add(daily_forecast_obj.getJSONObject("tmp").getString("min"));
                dailyForecastSet.add(daily_forecast_obj.getJSONObject("tmp").getString("max"));
                dailyForecastSet.add(daily_forecast_obj.getJSONObject("cond").getString("code_d"));

                editor.putStringSet("daily_" + String.valueOf(i), dailyForecastSet);
                dailyForecastSet.clear();
            }
            /* end:七天预报 */

            /* start:每小时天气预报 */
            // 和风天气貌似是只能提供某一天的每小时预报，不能跨天
            JSONObject hourly_forecast_obj;
            HashSet<String> hourlyForecastSet = new HashSet<>();
            for(int i = 0; i < hourly_forecast.length(); i++) {
                hourly_forecast_obj = hourly_forecast.getJSONObject(i);
                hourlyForecastSet.add(hourly_forecast_obj.getString("date"));
                hourlyForecastSet.add(hourly_forecast_obj.getString("tmp"));

                editor.putStringSet("hourly_" + String.valueOf(i), hourlyForecastSet);
                hourlyForecastSet.clear();
            }
            /* end:每小时天气预报 */
            editor.apply();

        }catch (Exception e) {
            Log.getStackTraceString(e);
            bRet = false;
        }
        return bRet;
    }

    public static void LoadResForWeather(Context context, boolean simpleFlag) {
        if(null == context) {
            Log.e(ERRTAG, "context is null");
            return;
        }

        Activity activity = (Activity)context;
        SharedPreferences pref = context.getSharedPreferences(SharedPreferenceName, Context.MODE_PRIVATE);
        TextView TextCntyName;
        TextView TextTemperature;
        ImageView IconWeather;
        TextView TextUpdate;

        if(simpleFlag) {
            RelativeLayout weatherLayout = (RelativeLayout) activity.findViewById(R.id.layout_brief_weather);
            TextCntyName = (TextView) activity.findViewById(R.id.brief_country_name);
            TextTemperature = (TextView) activity.findViewById(R.id.brief_temperature);
            IconWeather = (ImageView) activity.findViewById(R.id.brief_weather_icon);
            TextUpdate = (TextView) activity.findViewById(R.id.brief_last_update);

            weatherLayout.setVisibility(View.VISIBLE);
        }else {
            TextCntyName = (TextView) activity.findViewById(R.id.detail_country_name);
            TextTemperature = (TextView) activity.findViewById(R.id.detail_temperature);
            IconWeather = (ImageView) activity.findViewById(R.id.detail_weather_icon);
            TextUpdate = (TextView) activity.findViewById(R.id.detail_last_update);
        }

        if(null == TextCntyName || null == TextTemperature
                || null == IconWeather || null == TextUpdate) {
            Log.e(ERRTAG, "weather widget is null");
            return;
        }

        String countryName = pref.getString("country", null);
        if(null == countryName) {
            return;
        }

        int temperature = pref.getInt("temperature", 999);
        if(999 == temperature) {
            return;
        }

        String lastUpdate = pref.getString("update", null);
        if(null == lastUpdate) {
            return;
        }

        int picCode = pref.getInt("cond_code", -1);
        if(-1 == picCode) {
            return;
        }
        TextCntyName.setText(countryName);
        TextTemperature.setText(temperature + "℃");
        TextUpdate.setText("已更新" + lastUpdate);

        String picCodeStr = "weather_" + picCode;
        IconWeather.setImageResource(getResId(picCodeStr, R.mipmap.class));

    }

    public static String splitToGetTime(String dateInfo) {
        String time = null;
        if(null != dateInfo) {
            String[] curDate = dateInfo.split(" ");
            if(2 != curDate.length) {
                Log.e(ERRTAG, "update info is abnormal");
            }else {
                time = curDate[1];
            }
        }
        return time;
    }

    public static String[] splitToGetDate(String dateInfo) {
        String[] dateArr = null;
        if(null != dateInfo) {
            String[] curDate = dateInfo.split(" ");
            if(2 != curDate.length) {
                Log.e(ERRTAG, "update info is abnormal");
            }else {
                String date = curDate[0];
                dateArr = date.split("-");
                if(3 != dateArr.length) {
                    Log.e(ERRTAG, "update info is abnormal");
                    dateArr = null;
                }
            }
        }
        return dateArr;
    }

    public static void drawLineChart(Context context, LineChart chart, ArrayList<String> XValues,
                                     ArrayList<ArrayList<Entry>> YValuesList, int type) {
        if(null == context || null == chart) {
            Log.e(ERRTAG, "context or chart is null");
            return;
        }

        if(0 != YValuesList.size()) {
            // 此处将X轴绑定为自己的坐标表示
            XAxis xAxis = chart.getXAxis();
            IAxisValueFormatter XAxisValueFormatter;
            if(FLAG_HOURLY_CHART == type && null != XValues && 0 != XValues.size()) {
                XAxisValueFormatter = ((WeatherDetailActivity)context).new hourlyXAxisValueFormatter(XValues);
            }else if(FLAG_DAILY_CHART == type) {
                XAxisValueFormatter = ((WeatherDetailActivity)context).new dailyXAxisValueFormatter();
            }else {
                Log.e(ERRTAG, "type is wrong");
                return;
            }

            xAxis.setGranularity(1f);
            xAxis.setAxisMinimum(0);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setValueFormatter(XAxisValueFormatter);

            ArrayList<ILineDataSet> lineDataSetsList = new ArrayList<>();
            for(ArrayList<Entry> YValues : YValuesList) {
                LineDataSet LineDataSet = new LineDataSet(YValues, "");
                LineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                lineDataSetsList.add(LineDataSet);
            }

            LineData lineData = new LineData(lineDataSetsList);
            Legend legend = chart.getLegend();
            legend.setEnabled(false);
            chart.setScaleEnabled(false);
            chart.getAxisRight().setEnabled(false);
            chart.getAxisLeft().setEnabled(false);

            chart.setData(lineData);
            chart.invalidate();
            chart.notifyDataSetChanged();
        }
    }

    // 反射机制，通过字符串获得字符串对应的ID
    public static int getResId(String resName, Class<?> c) {
        try {
            Field field = c.getDeclaredField(resName);
            return field.getInt(field);
        }catch (Exception e){
            Log.e(ERRTAG, "get Res id failed");
            Log.getStackTraceString(e);
            return -1;
        }
    }
}
