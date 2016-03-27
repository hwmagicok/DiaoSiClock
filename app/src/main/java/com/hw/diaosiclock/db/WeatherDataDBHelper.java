package com.hw.weather1.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by hw on 2016/2/18.
 */
public class WeatherDataDBHelper extends SQLiteOpenHelper {
    public static final String CREATE_PROVINCE = "create table Province (" +
            "id integer primary key autoincrement, " +
            "province_name text, " +
            "province_code text, " +
            "province_en text)";

    public static final String CREATE_CITY = "create table City (" +
            "id integer primary key autoincrement, " +
            "city_name text, " +
            "city_code text, " +
            "city_en text, " +
            "belong_province text)";

    public static final String CREATE_COUNTRY = "create table Country (" +
            "id integer primary key autoincrement, " +
            "country_name text, " +
            "country_code text, " +
            "country_en text, " +
            "belong_city text)";

    public WeatherDataDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PROVINCE);
        db.execSQL(CREATE_CITY);
        db.execSQL(CREATE_COUNTRY);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
