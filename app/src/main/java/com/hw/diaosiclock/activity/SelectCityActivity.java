package com.hw.weather1.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hw.weather1.R;
import com.hw.weather1.model.WeatherDataDB;
import com.hw.weather1.util.HttpCallbackListener;
import com.hw.weather1.util.HttpUtil;
import com.hw.weather1.util.LocalUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import android.os.Handler;
import java.util.logging.LogRecord;

/**
 * Created by hw on 2016/2/16.
 */
public class SelectCityActivity extends Activity{
    public static final int PROVINCE_LEVEL = 0;
    public static final int CITY_LEVEL = 1;
    public static final int COUNTRY_LEVEL = 2;
    public static final int WEATHER_LEVEL = 3;
    private int currentlevel = PROVINCE_LEVEL;

    public static final String HEAD_SEARCH_LOCATION = "https://api.thinkpage.cn/v3/location/search.json?key=";
    public static final String HEAD_SEARCH_WEATHER = "https://api.thinkpage.cn/v3/weather/now.json?key=";
    public static final String TAIL_SEARCH_WEATHER = "&language=zh-Hans&unit=c";
    private static final String MY_API_KEY = "NFBQRECLEA";

    private ArrayAdapter<String> listAdapter;
    private ArrayList<String> locationList;
    private ListView list;
    private TextView leveltext;
    private ProgressDialog progressDialog;

    private String curProvince = null;
    private String curCity = null;
    private String curCountry = null;

    WeatherDataDB db;
    Cursor cursor = null;

    //给直辖市和特别行政区留的，他们只有两个层级，市和区
    private ArrayList<String> specialLocation;
    private int specialLocationFlag = 0;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.response);

        //验证数据库是否完整，若不完整将删除
        db = WeatherDataDB.getDbInstance(this);
        if(!db.verifiyDBCorrection()) {
            //查看数据库是否存在，如果存在就删了它
            /*
            final File databaseFile = this.getDatabasePath("hw_weatherDB");
            if(databaseFile.exists()) {
                databaseFile.delete();
            }
            */
            db.ClearAllDBData();
            LocalUtil.LoadAllLocation(this);
        }


        locationList = new ArrayList<String>();
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, locationList);
        list = (ListView) findViewById(R.id.location_content);
        list.setAdapter(listAdapter);
        //String address = "https://api.heweather.com/x3/citylist?search=allchina&key=8d52bc3c89964e5290c5cdc3e28f626e";
        //HttpUtil.sendHttpRequest(address);
        leveltext = (TextView) findViewById(R.id.level);

        specialLocation = db.querySpecialProvince();
        loadAllProvince();
        setTitleText();
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //当发现所点击的那项属于特别地址，将直接把当前级别改为city,同时
                //将特殊地址的标记置为1
                if (!specialLocation.isEmpty() && COUNTRY_LEVEL != currentlevel) {
                    if (specialLocation.contains(locationList.get(position))) {
                        currentlevel = CITY_LEVEL;
                        specialLocationFlag = 1;
                    }
                }

                if (PROVINCE_LEVEL == currentlevel) {
                    currentlevel = CITY_LEVEL;
                    curProvince = locationList.get(position);
                    loadAllCity(curProvince);
                    setTitleText(curProvince);
                } else if (CITY_LEVEL == currentlevel) {
                    currentlevel = COUNTRY_LEVEL;
                    curCity = locationList.get(position);
                    loadAllCountry(curCity);
                    setTitleText(curCity);
                } else if (COUNTRY_LEVEL == currentlevel) {
                    showProgressDialog();
                    currentlevel = WEATHER_LEVEL;
                    curCountry = locationList.get(position);

                    try {
                        String CountryCode = db.getCountryCode(curCity, curCountry);
                        if(null != CountryCode) {
                            queryCountryWeather(HEAD_SEARCH_WEATHER + MY_API_KEY + "&location=" +
                                    CountryCode + TAIL_SEARCH_WEATHER);
                            CancleProgressDialog();
                        }else {
                            //中文编码转换
                            String curCity_curCountry = java.net.URLEncoder.encode(curCity + curCountry, "UTF-8");
                            HttpUtil.sendHttpRequest(HEAD_SEARCH_LOCATION + MY_API_KEY + "&q=" + curCity_curCountry,
                                    new HttpCallbackListener() {
                                        @Override
                                        public void onFinish(String result) {
                                            try {
                                                JSONObject JsonResult = new JSONObject(result);
                                                JSONArray JsonResults = JsonResult.getJSONArray("results");
                                                JSONObject JsonLocationInfo = JsonResults.getJSONObject(0);
                                                String CountryCode = JsonLocationInfo.getString("id");
                                                queryCountryWeather(HEAD_SEARCH_WEATHER + MY_API_KEY + "&location=" +
                                                        CountryCode + TAIL_SEARCH_WEATHER);
                                                CancleProgressDialog();
                                            }catch (Exception e) {
                                                Log.e("SelectCityActivity", "COUNTRY_LEVEL query error");
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                        }

                    }catch(Exception e) {
                        Log.e(getLocalClassName(), "encode fail");
                        e.printStackTrace();
                    }
                    //setTitleText(curCountry);
                }
            }
        });
        if(null != cursor) {
            cursor.close();
        }

    }

    private void loadAllProvince() {
        if(0 != locationList.size()) {
            locationList.clear();
        }

        //直接把特殊地址作为省载入省列表
        ArrayList<String> specialProvinceList = db.querySpecialProvince();
        if(!specialProvinceList.isEmpty()) {
            for(String tmpString : specialProvinceList) {
                locationList.add(tmpString);
            }
        }

        cursor = db.queryProvince();
        String provinceName;
        if(cursor.moveToFirst()) {
            do {
                provinceName = cursor.getString(cursor.getColumnIndex("province_name"));
                if(!provinceName.equals("直辖市") && !provinceName.equals("特别行政区")) {
                    locationList.add(provinceName);
                }
            }while(cursor.moveToNext());
            listAdapter.notifyDataSetChanged();
            list.setSelection(0);
            return;
        }
        //cursor.close();
        Log.e("SelectCityActivity", "loadAllProvince failed");
    }

    private void loadAllCity(String provinceName) {
        cursor = db.queryCity(provinceName);
        if(cursor.moveToFirst()) {
            if(0 != locationList.size()) {
                locationList.clear();
            }

            do {
                locationList.add(cursor.getString(cursor.getColumnIndex("city_name")));
            }while(cursor.moveToNext());
            listAdapter.notifyDataSetChanged();
            list.setSelection(0);
            return;
        }
        //cursor.close();
        Log.e("SelectCityActivity", "loadAllCity failed");
    }

    private void loadAllCountry(String cityName) {
        cursor = db.queryCountry(cityName);
        if (cursor.moveToFirst()) {
            if (0 != locationList.size()) {
                locationList.clear();
            }

            do {
                locationList.add(cursor.getString(cursor.getColumnIndex("country_name")));
            } while (cursor.moveToNext());
            listAdapter.notifyDataSetChanged();
            list.setSelection(0);
            return;
        }
        //cursor.close();
        Log.e("SelectCityActivity", "loadAllCountry failed");
    }

    private synchronized void queryCountryWeather(final String address) {
        if(null != address && 0 < address.length()) {
            HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
                @Override
                public void onFinish(String result) {
                    if (null == result) {
                        return;
                    }
                    LocalUtil.saveWeatherToSharedPreferences(SelectCityActivity.this, result);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(SelectCityActivity.this, WeatherPageActivity.class);
                            startActivity(intent);
                        }
                    });
                }
            });

        }
        //Log.e("SelectCityActivity", "queryCountryWeather success");
    }

    private void ShowWeatherInfo() {
        SharedPreferences weatherInfo = getSharedPreferences("WeatherInfo", Context.MODE_PRIVATE);
        String countryName = weatherInfo.getString("CountryName", "error");
        String weatherStatus = weatherInfo.getString("WeatherStatus", "error");
        String temperature = weatherInfo.getString("Temperature", "error");
        String picCode = weatherInfo.getString("PicCode", "error");
        String lastUpdata = weatherInfo.getString("LastUpdata", "error");


    }

    public void onBackPressed() {
        if(PROVINCE_LEVEL == currentlevel) {
            finish();
        }else if(CITY_LEVEL == currentlevel) {
            currentlevel = PROVINCE_LEVEL;
            setTitleText();
            loadAllProvince();
        }else if(COUNTRY_LEVEL == currentlevel) {
            if(1 == specialLocationFlag) {
                currentlevel = PROVINCE_LEVEL;
                specialLocationFlag = 0;
                loadAllProvince();
                setTitleText();
            }else {
                currentlevel = CITY_LEVEL;
                loadAllCity(curProvince);
                setTitleText(curProvince);
            }
        }else if(WEATHER_LEVEL == currentlevel) {
            currentlevel = COUNTRY_LEVEL;
            loadAllCountry(curCity);
            setTitleText(curCity);
        }
    }

    public void setTitleText() {
        if(currentlevel == PROVINCE_LEVEL) {
            leveltext.setText("中国");
        }
    }

    public void setTitleText(final String name) {
        switch (currentlevel) {
            case PROVINCE_LEVEL :
                leveltext.setText("中国");
                break;
            case CITY_LEVEL:
                leveltext.setText(name + "省");
                break;
            case COUNTRY_LEVEL:
                leveltext.setText(name + "市");
                break;
            case WEATHER_LEVEL:
                if(1 == specialLocationFlag) {
                    leveltext.setText(name + "区天气情况");
                }else {
                    leveltext.setText(name + "县天气情况");
                }
                break;
        }
    }

    private void showProgressDialog() {
        if(null == progressDialog) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("加载中");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void CancleProgressDialog() {
        if(null != progressDialog) {
            progressDialog.dismiss();
        }
    }
}
