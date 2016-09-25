package com.hw.diaosiclock.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.hw.diaosiclock.R;
import com.hw.diaosiclock.util.LocalUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;
import java.util.TimeZone;

/**
 * Created by hw on 2016/9/15.
 */
public class WeatherDetailActivity extends AppCompatActivity {
    public static final String ERRTAG = "WeatherDetailActivity";
    private static final int CODE_SELECT_COUNTRY = 1;
    SharedPreferences pref;
    private String[] updateDate;

    private SwipeRefreshLayout refreshLayout;
    private EditText searchBar;
    private Button searchButton;
    private LineChart hourlyForecastChart;
    private LineChart dailyForecastChart;
    private TextView title_pm25;
    private TextView pm25;
    private TextView wind_degree;
    private TextView wind_direction;

    private ArrayList<String> hourlyXValues;
    private ArrayList<Entry> hourlyYValues;
    private ArrayList<Entry> dailyMinYValues;
    private ArrayList<Entry> dailyMaxYValues;

    String curUpdate;
    String[] curDate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_detail);

        hourlyForecastChart = (LineChart)findViewById(R.id.chart_hourly_forecast);
        dailyForecastChart = (LineChart)findViewById(R.id.chart_daily_forecast);

        if(null == hourlyForecastChart || null == dailyForecastChart) {
            Log.e(ERRTAG, "chart widget is null");
            return;
        }

        hourlyForecastChart.setDescription("每小时");
        dailyForecastChart.setDescription("每日");

        pref = getSharedPreferences(LocalUtil.SharedPreferenceName, MODE_PRIVATE);

        hourlyXValues = new ArrayList<>();
        hourlyYValues = new ArrayList<>();
        dailyMinYValues = new ArrayList<>();
        dailyMaxYValues = new ArrayList<>();

        curUpdate = pref.getString("update", null);
        curDate = LocalUtil.splitToGetDate(curUpdate);
        String curTime = LocalUtil.splitToGetTime(curUpdate);
        updateDate = curDate;

        // 填充城市名、update时间、温度和图标
        LocalUtil.LoadResForWeather(WeatherDetailActivity.this, false);

        // 绘制每小时和每天的曲线
        drawHourlyChart();
        drawDailyChart();

        // 填充pm2.5、风向、风级
        fillOtherWeatherInfo();

        // 搜索框
        searchBar = (EditText)findViewById(R.id.detail_search_city);
        searchButton = (Button)findViewById(R.id.detail_search_button);
        if(null != searchBar && null != searchButton) {
            // 监听回车键，即回车便搜索
            searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if(EditorInfo.IME_ACTION_SEARCH == actionId ||
                            (null != event && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                        String searchText = searchBar.getText().toString();
                        jumpToCountryListActivity(searchText);
                        return true;
                    }
                    return false;
                }
            });

            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String searchText = searchBar.getText().toString();
                    jumpToCountryListActivity(searchText);
                }
            });
        }

        refreshLayout = (SwipeRefreshLayout)findViewById(R.id.layout_FreshWeatherDetail);
        if(null != refreshLayout) {
            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    String countryCode = pref.getString("code", null);
                    if(null != countryCode) {
                        refreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.white);
                        RequestQueue requestQueue = Volley.newRequestQueue(WeatherDetailActivity.this);
                        String url = LocalUtil.URL + countryCode + "&key=" + LocalUtil.getApiKey();
                        JsonObjectRequest request = new JsonObjectRequest(url,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        boolean bRet = LocalUtil.saveWeatherInfoToLocal(WeatherDetailActivity.this, response);
                                        if (bRet) {
                                            LocalUtil.LoadResForWeather(WeatherDetailActivity.this, false);
                                            drawHourlyChart();
                                            drawDailyChart();
                                            fillOtherWeatherInfo();
                                        }

                                        if (null != refreshLayout) {
                                            refreshLayout.setRefreshing(false);
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError volleyError) {
                                        // Todo 后续可根据返回码判断是否需要重发请求
                                        Log.e(ERRTAG, "get weather info online is fail");
                                        if(null != refreshLayout) {
                                            refreshLayout.setRefreshing(false);
                                        }
                                    }
                                });
                        requestQueue.add(request);
                    }
                }
            });
        }
    }

    // 绘制每小时天气变化的曲线
    private void drawHourlyChart() {
        StringBuilder builder = new StringBuilder();
        Set<String> tmpSet = null;
        builder.append("hourly_");
        String tmpDate;
        String tmpTemperature;

        if(0 != hourlyXValues.size()) {
            hourlyXValues.clear();
        }
        if(0 != hourlyYValues.size()) {
            hourlyYValues.clear();
        }

        for(int i = 0; i <= 7; i++) {
            builder.append(i);
            tmpSet = pref.getStringSet(builder.toString(), null);
            if(null != tmpSet) {
                String[] tmpSetArr = tmpSet.toArray(new String[tmpSet.size()]);
                Entry entry = new Entry();

                if(2 != tmpSetArr.length) {
                    Log.e(ERRTAG, "hourly info is wrong");
                    continue;
                }

                // 由于set的顺序不确定，无法保证时间和温度的排列顺序，故暂通过长度来判别
                if(tmpSetArr[0].length() < tmpSetArr[1].length()) {
                    tmpDate = tmpSetArr[1];
                    tmpTemperature = tmpSetArr[0];
                }else {
                    tmpTemperature = tmpSetArr[1];
                    tmpDate = tmpSetArr[0];
                }


                String[] dateArr = LocalUtil.splitToGetDate(tmpDate);
                if(null == dateArr) {
                    Log.e(ERRTAG, "hourly xValue is abnormal");
                    continue;
                }
                // 索引为0是年份，1是月份，2是日期
                if(Integer.parseInt(dateArr[0]) < Integer.parseInt(curDate[0])
                        || (dateArr[0].equals(curDate[0]) &&
                        (Integer.parseInt(dateArr[1]) < Integer.parseInt(curDate[1])))) {
                    Log.e(ERRTAG, "date info is backward");
                    break;
                }

                entry.setX(i);
                if(null != LocalUtil.splitToGetTime(tmpDate)) {
                    hourlyXValues.add(LocalUtil.splitToGetTime(tmpDate));
                }else {
                    Log.e(ERRTAG, "xValue time is null");
                }

                entry.setY(Integer.parseInt(tmpTemperature));
                hourlyYValues.add(entry);

            }else {
                break;
            }
            builder.deleteCharAt(builder.length() - 1);
        }

        if(1 < hourlyXValues.size() || 1 < hourlyYValues.size()) {
            ArrayList<ArrayList<Entry>> hourlyYValuesList = new ArrayList<>();
            hourlyYValuesList.add(hourlyYValues);

            LocalUtil.drawLineChart(WeatherDetailActivity.this, hourlyForecastChart, hourlyXValues,
                    hourlyYValuesList, LocalUtil.FLAG_HOURLY_CHART);

        }
    }

    // 绘制每天天气变化的曲线
    private void drawDailyChart() {
        StringBuilder builder = new StringBuilder();
        builder.append("daily_");
        Set<String> tmpSet = null;

        if(0 != dailyMinYValues.size()) {
            dailyMinYValues.clear();
        }
        if(0 != dailyMaxYValues.size()) {
            dailyMaxYValues.clear();
        }

        for(int i = 0; i <= 6; i++) {
            builder.append(i);
            tmpSet = pref.getStringSet(builder.toString(), null);

            if(null != tmpSet) {
                String[] tmpSetArr = tmpSet.toArray(new String[tmpSet.size()]);
                Entry minEntry = new Entry();
                Entry maxEntry = new Entry();

                if(3 != tmpSetArr.length) {
                    Log.e(ERRTAG, "daily info is wrong");
                    continue;
                }

                // picCode暂时可能不用
                String picCode;
                int minTemperature;
                int maxTemperature;

                String tmpStr;
                for(int j = 0; j < tmpSetArr.length - 1; j++) {
                    for(int k = 0; k < tmpSetArr.length - j - 1; k++) {
                        if(Integer.parseInt(tmpSetArr[k]) > Integer.parseInt(tmpSetArr[k + 1])) {
                            tmpStr = tmpSetArr[k];
                            tmpSetArr[k] = tmpSetArr[k + 1];
                            tmpSetArr[k + 1] = tmpStr;
                        }
                    }
                }

                minTemperature = Integer.parseInt(tmpSetArr[0]);
                maxTemperature = Integer.parseInt(tmpSetArr[1]);
                picCode = tmpSetArr[2];

                minEntry.setX(i);
                maxEntry.setX(i);
                minEntry.setY(minTemperature);
                maxEntry.setY(maxTemperature);

                dailyMinYValues.add(minEntry);
                dailyMaxYValues.add(maxEntry);
            }
            builder.deleteCharAt(builder.length() - 1);
        }

        if(0 != dailyMinYValues.size() && 0 != dailyMaxYValues.size()) {
            ArrayList<ArrayList<Entry>> dailyYValuesList = new ArrayList<>();
            dailyYValuesList.add(dailyMinYValues);
            dailyYValuesList.add(dailyMaxYValues);

            LocalUtil.drawLineChart(WeatherDetailActivity.this, dailyForecastChart, null,
                    dailyYValuesList, LocalUtil.FLAG_DAILY_CHART);
        }
    }

    // 填充pm2.5、风速之类的信息
    private void fillOtherWeatherInfo() {
        title_pm25 = (TextView)findViewById(R.id.detail_title_pm25);
        pm25 = (TextView)findViewById(R.id.detail_pm25);
        int pm25Val = pref.getInt("pm25", -1);
        if(null != title_pm25 && null != pm25 && -1 != pm25Val) {
            title_pm25.setText("PM2.5:");
            pm25.setText(String.valueOf(pm25Val));
        }

        wind_degree = (TextView)findViewById(R.id.detail_wind_degree);
        wind_direction = (TextView)findViewById(R.id.detail_wind_direction);
        String windDegreeStr = pref.getString("wind_degree", null);
        String windDirectionStr = pref.getString("wind_direction", null);
        if(null != wind_degree && null != windDegreeStr) {
            wind_degree.setText(windDegreeStr);
        }
        if(null != wind_direction && null != windDirectionStr) {
            wind_direction.setText(windDirectionStr);
        }
    }

    private void jumpToCountryListActivity(String countryName) {
        if(null ==  countryName || 0 == countryName.length()) {
            return;
        }
        Intent intent = new Intent(WeatherDetailActivity.this, SelectCountryActivity.class);
        intent.putExtra("country_name", countryName);
        startActivityForResult(intent, CODE_SELECT_COUNTRY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // 这是当选完城市后返回该视图刷新天气显示
            case CODE_SELECT_COUNTRY:
                if(RESULT_OK == resultCode) {
                    String countryCode = data.getStringExtra("return_country_code");

                    if(null != countryCode) {
                        RequestQueue requestQueue = Volley.newRequestQueue(WeatherDetailActivity.this);
                        String url = LocalUtil.URL + countryCode + "&key=" + LocalUtil.getApiKey();
                        //Todo to delete
                        Log.e(ERRTAG, url);
                        JsonObjectRequest req = new JsonObjectRequest(url, null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        boolean bRet = LocalUtil.saveWeatherInfoToLocal(WeatherDetailActivity.this, response);
                                        if(bRet) {

                                            curUpdate = pref.getString("update", null);
                                            curDate = LocalUtil.splitToGetDate(curUpdate);
                                            updateDate = curDate;

                                            LocalUtil.LoadResForWeather(WeatherDetailActivity.this, false);
                                            drawHourlyChart();
                                            drawDailyChart();
                                            fillOtherWeatherInfo();
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError volleyError) {
                                        // Todo 后续可根据返回码判断是否需要重发请求
                                        Log.e(ERRTAG, "get weather info online is fail");
                                    }
                                });
                        requestQueue.add(req);
                    }
                }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ScheduleActivity.class);
        setResult(RESULT_OK, intent);
        finish();
    }

    // hourly的x轴
    public class hourlyXAxisValueFormatter implements IAxisValueFormatter {
        String[] hourStr;
        public hourlyXAxisValueFormatter() {
            hourStr = null;
        }

        public hourlyXAxisValueFormatter(ArrayList<String> list) {
            if(null != list) {
                hourStr = list.toArray(new String[list.size()]);
            }
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            if(value < 0 || null == hourStr || (int)value >= hourStr.length) {
                Log.e(WeatherDetailActivity.ERRTAG, "xAxis is abnormal");
                return null;
            }

            return hourStr[(int)value];
        }

        @Override
        public int getDecimalDigits() {
            return 0;
        }
    }

    // daily的x轴
    public class dailyXAxisValueFormatter implements IAxisValueFormatter {
        Calendar calendar;
        long curTime;
        int weekday;

        public dailyXAxisValueFormatter() {
            calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
            curTime = calendar.getTimeInMillis();
        }
        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            if(0 > value) {
                Log.e(WeatherDetailActivity.ERRTAG, "xAxis is abnormal");
                return null;
            }
            String xStr;
            int val = (int)value;
            weekday = calendar.get(Calendar.DAY_OF_WEEK);

            int day = (val + weekday - 1) % 7;
            String Day;
            if(0 > day) {
                day += 7;
            }

            switch (day) {
                case 1:
                    Day = "日";
                    break;
                case 2:
                    Day = "一";
                    break;
                case 3:
                    Day = "二";
                    break;
                case 4:
                    Day = "三";
                    break;
                case 5:
                    Day = "四";
                    break;
                case 6:
                    Day = "五";
                    break;
                default:
                    Day = "六";
                    break;
            }
            xStr = "星期" + Day;

            if(null != updateDate && 3 == updateDate.length) {
                calendar.set(Calendar.YEAR, Integer.parseInt(updateDate[0]));
                calendar.set(Calendar.MONTH, Integer.parseInt(updateDate[1]));
                calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(updateDate[2]));

                // 这说明当前时间据上次天气更新时间不超过一周
                if(curTime - calendar.getTimeInMillis() < 7 * 24 * 3600 * 1000){
                    if(weekday == day) {
                        xStr = "今天";
                    }
                }
            }

            return xStr;
        }

        @Override
        public int getDecimalDigits() {
            return 0;
        }
    }
}


