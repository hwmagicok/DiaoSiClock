package com.hw.diaosiclock.util;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.hw.diaosiclock.activity.SelectCityActivity;
import com.hw.diaosiclock.db.WeatherDataDBHelper;
import com.hw.diaosiclock.model.City;
import com.hw.diaosiclock.model.Country;
import com.hw.diaosiclock.model.Province;
import com.hw.diaosiclock.model.WeatherDataDB;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by hw on 2016/2/20.
 */
public class LocalUtil {

    //验证数据库是否正常所用
    private static String CurDBKeyTime = null;

    public synchronized static void LoadAllLocation(Context context) {
        String localAddress = "ChinaLocationData.txt";
        FileInputStream input = null;
        BufferedReader reader = null;

        try {
            input = context.openFileInput(localAddress);
            reader = new BufferedReader(new InputStreamReader(input));
            StringBuffer locationJsonData = new StringBuffer();
            String line;

            while(null != (line = reader.readLine())) {
                locationJsonData.append(line);
            }

            WeatherDataDB db = WeatherDataDB.getDbInstance(context);
            JSONObject locationJsonObj = new JSONObject((locationJsonData.toString()));
            JSONArray ChinaJsonArr = locationJsonObj.getJSONArray("list");

            JSONObject provinceJson = null;
            JSONObject cityJson = null;
            JSONObject countryJson = null;
            Province province = new Province();
            City city = new City();
            Country country = new Country();
            JSONArray cityJsonList;
            JSONArray countryJsonList;

            for(int i = 0 ; i < ChinaJsonArr.length(); i++) {
                provinceJson = ChinaJsonArr.getJSONObject(i);
                //province.SetProvinceCode(provinceJson.getString("id"));
                province.SetProvinceCode(null);
                province.SetProvinceName(provinceJson.getString("name"));
                province.SetProvinceEn(provinceJson.getString("en"));

                db.saveProvince(province);
                cityJsonList = provinceJson.getJSONArray("list");
                for(int j = 0; j < cityJsonList.length(); j++) {
                    cityJson = cityJsonList.getJSONObject(j);
                    //city.SetCityCode(cityJson.getString("id"));
                    city.SetCityCode(null);
                    city.SetCityName(cityJson.getString("name"));
                    city.SetCityEn(cityJson.getString("en"));
                    city.SetBelongProvinceEn(provinceJson.getString("en"));

                    db.saveCity(city);
                    countryJsonList = cityJson.getJSONArray("list");

                    for(int k = 0; k < countryJsonList.length(); k++) {
                        countryJson = countryJsonList.getJSONObject(k);
                        country.SetCountryCode(null);
                        country.SetCountryName(countryJson.getString("name"));
                        country.SetCountryEn(countryJson.getString("en"));
                        country.SetBelongCityEn(cityJson.getString("en"));

                        db.saveCountry(country);
                        countryJson = null;
                    }
                    cityJson = null;
                }
                provinceJson = null;
            }

            //加入验证机制，若验证不通过将重新建立数据库
            Calendar now = Calendar.getInstance();
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            CurDBKeyTime = dateFormat.format(now.getTime());

            /*
            province.SetProvinceName("hw");
            province.SetProvinceCode(CurDBKeyTime);
            province.SetProvinceEn(null);
            db.saveProvince(province);
            */

            city.SetCityName("hw");
            city.SetCityCode(CurDBKeyTime);
            city.SetCityEn("null");
            city.SetBelongProvinceEn("null");
            db.saveCity(city);

            country.SetCountryName("hw");
            country.SetCountryCode(CurDBKeyTime);
            country.SetCountryEn("null");
            country.SetBelongCityEn("null");
            db.saveCountry(country);


        }catch (Exception e) {
            Log.e("LocalUtil", "error");
            e.printStackTrace();
        }finally {
            try {
                if (null != input) {
                    input.close();
                }
                if(null != reader) {
                    reader.close();
                }
            }catch (Exception e) {
                Log.e("close", "fail");
                e.printStackTrace();
            }
        }
    }

    public static void saveWeatherToSharedPreferences(Context context, final String JsonWeatherStr) {
        SharedPreferences.Editor editor = context.getSharedPreferences("WeatherInfo", Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.commit();

        try {
            JSONObject JsonWeather = new JSONObject(JsonWeatherStr);
            JSONArray JsonResult = JsonWeather.getJSONArray("results");
            JSONObject JsonWeatherInfo = JsonResult.getJSONObject(0);
            JSONObject JsonLocationInfo = JsonWeatherInfo.getJSONObject("location");
            JSONObject JsonNowInfo = JsonWeatherInfo.getJSONObject("now");
            //JSONObject JsonLastUpdataInfo = JsonWeatherInfo.getJSONObject("last_update");

            String countryName = JsonLocationInfo.getString("name");
            String weatherStatus = JsonNowInfo.getString("text");
            String temperature = JsonNowInfo.getString("temperature");
            String picCode = JsonNowInfo.getString("code");
            String lastUpdata = JsonWeatherInfo.getString("last_update");

            editor.putString("CountryName", countryName);
            editor.putString("WeatherStatus", weatherStatus);
            editor.putString("Temperature", temperature);
            editor.putInt("PicCode", Integer.parseInt(picCode));
            editor.putString("LastUpdata", lastUpdata);

            editor.commit();

        }catch (Exception e) {
            Log.e("LocalUtil", "saveWeatherToSharedPreferences error");
            e.printStackTrace();
        }
    }

    public static String GetCurDBKeyTime() {
        return CurDBKeyTime;
    }

    public static String splitLastUpdataData(final String str) {
        if(null == str) {
            return null;
        }
        StringBuilder newData = new StringBuilder();
        String date[] = str.split("T");
        String time[] = date[1].split("\\+");
        newData.append(date[0] + "  ");
        newData.append(time[0]);
        return newData.toString();
    }
}
