package com.hw.diaosiclock.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by hw on 2016/2/18.
 */
public class WeatherDataDBHelper extends SQLiteOpenHelper {
    public static final String CREATE_PROVINCE = "create table Province (" +
            "id integer primary key autoincrement, " +
            "province_name varchar(20), " +
            "province_code varchar(20), " +
            "province_en varchar(20))";

    public static final String CREATE_CITY = "create table City (" +
            "id integer primary key autoincrement, " +
            "city_name varchar(20), " +
            "city_code varchar(20), " +
            "city_en varchar(20), " +
            "belong_province varchar(20))";

    public static final String CREATE_COUNTRY = "create table Country (" +
            "id integer primary key autoincrement, " +
            "country_name varchar(20), " +
            "country_code varchar(20), " +
            "country_en varchar(20), " +
            "belong_city varchar(20))";

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
