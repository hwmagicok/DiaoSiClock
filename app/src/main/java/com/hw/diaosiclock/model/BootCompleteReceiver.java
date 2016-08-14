package com.hw.diaosiclock.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.hw.diaosiclock.util.LocalUtil;

import java.util.ArrayList;

/**
 * Created by hw on 2016/8/5.
 */
public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            Thread.sleep(2000);
        }catch (Exception e) {
            Log.getStackTraceString(e);
        }

        AlarmDB db = AlarmDB.getInstance(context);
        Cursor cursor = db.queryAllAlarm();

        if(cursor.moveToFirst()) {
            do {
                LocalUtil.reopenAlarmService(context, db.getAlarmByCursor(cursor));
            }while (cursor.moveToNext());
        }

    }
}
