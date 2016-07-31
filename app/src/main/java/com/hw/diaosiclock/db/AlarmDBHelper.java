package com.hw.diaosiclock.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by hw on 2016/4/4.
 */
public class AlarmDBHelper extends SQLiteOpenHelper {
    public static String CREATE_ALARM = "create table Alarm (" +
            "id integer primary key autoincrement," +
            "alarm_name varchar(40), " +
            "switch integer, " +
            "time varchar(20), " +
            "monday_status integer, " +
            "tuesday_status integer, " +
            "wednesday_status integer, " +
            "thursday_status integer, " +
            "friday_status integer, " +
            "saturday_status integer, " +
            "sunday_status integer, " +
            "volume integer, " +
            "interval integer, " +
            "shock integer, " +
            "music varchar(50), " +
            "last_saturday integer, " +
            "TimeFormat24H integer)";

    public AlarmDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ALARM);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
