package com.hw.diaosiclock.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hw.diaosiclock.db.AlarmDBHelper;

/**
 * Created by hw on 2016/4/4.
 */
public class AlarmDB {
    public static final String ERRTAG = "AlarmDB";

    private static AlarmDB alarmDB = null;
    private SQLiteDatabase db;
    private String dbName = "hw_alarmDB";
    private int version = 1;

    private AlarmDB(Context context) {
        AlarmDBHelper dbHelper = new AlarmDBHelper(context, dbName, null, version);
        db = dbHelper.getWritableDatabase();
    }

    public synchronized static AlarmDB getInstance(Context context) {
        if(null == alarmDB) {
            alarmDB = new AlarmDB(context);
        }
        return alarmDB;
    }

    public Cursor queryAllAlarm() {
        if(null == db){
            Log.e(ERRTAG + "queryAllAlarm", "db is null");
        }

        Cursor cursor = db.rawQuery("select * from Alarm", null);
        return cursor;
    }

    public Cursor querySpecificAlarm(int id) {
        Cursor cursor = db.rawQuery("select * from Alarm where id = ?", new String[]{String.valueOf(id)});
        return cursor;
    }

    public void updateSpecificAlarm(Alarm alarm) {
        if(null == alarm) {
            Log.e(ERRTAG + "updateAlarm", "alarm is null");
            return;
        }

        int weekArr[] = alarm.getWeekStatus();
        String weekStr[] = new String[7];

        for(int i= 0; i < 7; i++) {
            weekStr[i] = String.valueOf(weekArr[i]);
        }

        db.execSQL("update Alarm set alarm_name = ?," +
                "switch = ?," +
                "time = ?," +
                "monday_status = ?," +
                "tuesday_status = ?," +
                "wednesday_status = ?," +
                "thursday_status = ?," +
                "friday_status = ?," +
                "saturday_status = ?," +
                "sunday_status = ?," +
                "volume = ?," +
                "interval = ?," +
                "shock = ?," +
                "music = ?," +
                "month = ?," +
                "last_saturday = ?," +
                "TimeFormat24H = ? " +
                "where id = ?",
                new String[] {
                        alarm.getAlarmName(),
                        String.valueOf(alarm.getAlarmOnOrOff() ? 1 : 0),
                        alarm.getTime(),
                        weekStr[0], weekStr[1], weekStr[2], weekStr[3], weekStr[4], weekStr[5], weekStr[6],
                        String.valueOf(alarm.getVolume()),
                        String.valueOf(alarm.getAlarm_interval()),
                        String.valueOf((alarm.getShockStatus()) ? 1 : 0),
                        alarm.getAlarmMusic(),
                        String.valueOf(alarm.getMonthOfLastSaturday()),
                        String.valueOf((alarm.getLastSaturday())),
                        String.valueOf((alarm.get24HourFormat()) ? 1 : 0),
                        String.valueOf(alarm.getAlarmID())});
    }

    //插入表中的Alarm不带id
    public void saveAlarm(final Alarm alarm) {
        if(null == alarm) {
            Log.e(ERRTAG + "saveAlarm", "alarm is null");
            return;
        }
        ContentValues content = new ContentValues();
        content.put("alarm_name", alarm.getAlarmName());
        content.put("switch", alarm.getAlarmOnOrOff() ? 1 : 0);
        content.put("time", alarm.getTime());
        content.put("monday_status", String.valueOf(alarm.getWeekStatus()[0]));
        content.put("tuesday_status", String.valueOf(alarm.getWeekStatus()[1]));
        content.put("wednesday_status", String.valueOf(alarm.getWeekStatus()[2]));
        content.put("thursday_status", String.valueOf(alarm.getWeekStatus()[3]));
        content.put("friday_status", String.valueOf(alarm.getWeekStatus()[4]));
        content.put("saturday_status", String.valueOf(alarm.getWeekStatus()[5]));
        content.put("sunday_status", String.valueOf(alarm.getWeekStatus()[6]));
        content.put("volume", alarm.getVolume());
        content.put("interval", alarm.getAlarm_interval());
        content.put("shock", alarm.getShockStatus() ? 1 : 0);
        content.put("music", alarm.getAlarmMusic());
        content.put("month", alarm.getMonthOfLastSaturday());
        content.put("last_saturday", alarm.getLastSaturday());
        content.put("TimeFormat24H", alarm.get24HourFormat() ? 1 : 0);

        db.insert("Alarm", null, content);
    }

    //上个函数的重载，插入表中的Alarm带id
    public void saveAlarm(final Alarm alarm, final int id) {
        if(null == alarm) {
            Log.e(ERRTAG + "saveAlarm", "alarm is null");
            return;
        }
        ContentValues content = new ContentValues();
        content.put("id", alarm.getAlarmID());
        content.put("alarm_name", alarm.getAlarmName());
        content.put("time", alarm.getTime());
        content.put("monday_status", String.valueOf(alarm.getWeekStatus()[0]));
        content.put("tuesday_status", String.valueOf(alarm.getWeekStatus()[1]));
        content.put("wednesday_status", String.valueOf(alarm.getWeekStatus()[2]));
        content.put("thursday_status", String.valueOf(alarm.getWeekStatus()[3]));
        content.put("friday_status", String.valueOf(alarm.getWeekStatus()[4]));
        content.put("saturday_status", String.valueOf(alarm.getWeekStatus()[5]));
        content.put("sunday_status", String.valueOf(alarm.getWeekStatus()[6]));
        content.put("volume", alarm.getVolume());
        content.put("interval", alarm.getAlarm_interval());
        content.put("shock", alarm.getShockStatus() ? 1 : 0);
        content.put("music", alarm.getAlarmMusic());
        content.put("month", alarm.getMonthOfLastSaturday());
        content.put("last_saturday", alarm.getLastSaturday());
        content.put("TimeFormat24H", alarm.get24HourFormat() ? 1 : 0);

        db.insert("Alarm", null, content);
    }

    public void delSpecificAlarm(Alarm alarm) {
        if(null == alarm) {
            Log.e(ERRTAG, "alarm is null");
        }else {
            int id = alarm.getAlarmID();
            Cursor cursor = querySpecificAlarm(id);
            if(null == cursor) {
                Log.e(ERRTAG, "alarm" + id + "can`t be find in db.");
                return;
            }
            db.execSQL("delete from Alarm where id = ?", new String[] {String.valueOf(id)});
        }
    }

    //debug用，用于输出一个Alarm的所有参数
    public void outputAlarmAllPara(Cursor cursor) {
        if(null == cursor) {
            Log.e(ERRTAG, "outputAlarmAllPara cursor is null");
        }

        Log.e(ERRTAG, "alarm id [" + cursor.getString(cursor.getColumnIndex("id"))
                + "] alarm name [" + cursor.getString(cursor.getColumnIndex("alarm_name"))
                + "] time [" + cursor.getString(cursor.getColumnIndex("time")) + "]");
    }

    // 调用该函数的时候，确保cursor的游标不在最后，否则会出现
    // “CursorIndexOutOfBoundsException: Index -1 requested”的错误
    public Alarm getAlarmByCursor(Cursor cursor) {
        if(null == cursor) {
            Log.e(ERRTAG, "cursor is null");
            return null;
        }
        Alarm alarm = new Alarm();
        int week_status[] = new int[7];

        alarm.setId(cursor.getInt(cursor.getColumnIndex("id")));
        if(!alarm.setTime(cursor.getString(cursor.getColumnIndex("time")))) {
            cursor.close();
            Log.e(ERRTAG, "setTime fail");
            return null;
        }

        alarm.setAlarmName(cursor.getString(cursor.getColumnIndex("alarm_name")));
        alarm.setAlarmSwitch(cursor.getInt(cursor.getColumnIndex("switch")) == 1);

        week_status[0] = cursor.getInt(cursor.getColumnIndex("monday_status"));
        week_status[1] = cursor.getInt(cursor.getColumnIndex("tuesday_status"));
        week_status[2] = cursor.getInt(cursor.getColumnIndex("wednesday_status"));
        week_status[3] = cursor.getInt(cursor.getColumnIndex("thursday_status"));
        week_status[4] = cursor.getInt(cursor.getColumnIndex("friday_status"));
        week_status[5] = cursor.getInt(cursor.getColumnIndex("saturday_status"));
        week_status[6] = cursor.getInt(cursor.getColumnIndex("sunday_status"));
        alarm.setWeek(week_status);

        alarm.setShock(cursor.getInt(cursor.getColumnIndex("shock")) > 0);
        alarm.setMusic(cursor.getString(cursor.getColumnIndex("music")));
        alarm.setMonthOfLastSaturday(cursor.getInt(cursor.getColumnIndex("month")));
        alarm.setLastSaturday(cursor.getInt(cursor.getColumnIndex("last_saturday")));
        alarm.set24HourFormat(cursor.getInt(cursor.getColumnIndex("TimeFormat24H")) > 0);
        alarm.setAlarm_interval(cursor.getInt(cursor.getColumnIndex("interval")));
        alarm.setVolume(cursor.getInt(cursor.getColumnIndex("volume")));

        return alarm;
    }

    // 创建完Alarm后将其保存在数据库中，并从数据库中获取该Alarm的ID
    public synchronized int getLastAlarmID() {
        Cursor cursor = db.rawQuery("select id from Alarm", null);
        int id = -1;
        if(cursor.moveToLast()) {
            id = cursor.getInt(cursor.getColumnIndex("id"));
        }
        cursor.close();
        return id;
    }
}
