package com.hw.diaosiclock.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hw.diaosiclock.activity.AlarmOnTimeActivity;
import com.hw.diaosiclock.util.LocalUtil;

/**
 * Created by hw on 2016/7/22.
 */
public class AlarmReceiver extends BroadcastReceiver {
    public static final String ERRTAG = "AlarmReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, AlarmOnTimeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Alarm alarm = intent.getParcelableExtra(LocalUtil.TAG_EXECUTE_ALARM);
        if(null == alarm) {
            Log.e(ERRTAG, "alarm is null");
        }else {
            Log.e(ERRTAG, "AlarmReceiver on receive alarm id[" + String.valueOf(alarm.getAlarmID()) + "]");
            i.putExtra(LocalUtil.TAG_EXECUTE_ALARM, alarm);
        }

        context.startActivity(i);
    }

}
