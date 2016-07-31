package com.hw.diaosiclock.model;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.hw.diaosiclock.activity.AlarmOnTimeActivity;
import com.hw.diaosiclock.activity.SetAlarmActivity;
import com.hw.diaosiclock.util.LocalUtil;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by hw on 2016/7/23.
 */
public class AlarmBackgroundService extends Service {
    public static final String ERRTAG = "AlarmBackgroundService";
    private AlarmManager alarmManager = null;
    private Alarm alarm = null;
    private String triggerTimeStr = null;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(null == intent) {
                    Log.e(ERRTAG, "intent is null");
                    return;
                }

                // 每次都从数据库中取出，这样可以保证满足闹钟的实时变化
                AlarmDB db = AlarmDB.getInstance(getApplicationContext());
                if(null == db) {
                    Log.e(ERRTAG, "db is null");
                    return;
                }
                int AlarmId = intent.getIntExtra(LocalUtil.TAG_EXECUTE_ALARM, -1);
                if(-1 == AlarmId) {
                    Log.e(ERRTAG, "alarm id is wrong");
                    return;
                }
                //Alarm alarm = intent.getParcelableExtra(LocalUtil.TAG_EXECUTE_ALARM);
                Cursor cursor = db.querySpecificAlarm(AlarmId);
                if(cursor.moveToFirst()) {
                    alarm = db.getAlarmByCursor(cursor);
                }else {
                    Log.e(ERRTAG, "cursor is null");
                }
                if(null == alarm) {
                    Log.e(ERRTAG, "alarm is null");
                    return;
                }

                Intent AlarmOnTime = new Intent("com.hw.diaosiclock.EXECUTE_CLOCK");
                AlarmOnTime.putExtra(LocalUtil.TAG_EXECUTE_ALARM, alarm);
                PendingIntent pi = PendingIntent.getBroadcast(AlarmBackgroundService.this, alarm.getAlarmID(),
                                                    AlarmOnTime, PendingIntent.FLAG_UPDATE_CURRENT);

                alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

                if(alarm.getAlarmOnOrOff() == false) {
                    alarmManager.cancel(pi);
                    return;
                }

                int[] weekStatus = alarm.getWeekStatus();
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                long curTime = calendar.getTimeInMillis();
                long triggerTime = 0;

                calendar.set(Calendar.HOUR_OF_DAY, alarm.getTimeHour());
                calendar.set(Calendar.MINUTE, alarm.getTimeMinute());
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                if(!alarm.isRepeatAlarm()) {
                    // 这是单次闹钟
                    // 这说明闹钟时间比现在时间靠前，不能在今天设定闹钟
                    if(calendar.getTimeInMillis() <= curTime) {
                        calendar.setTimeInMillis(calendar.getTimeInMillis() + 1000*60*60*24);
                    }

                    AlarmOnTime.putExtra(LocalUtil.TAG_EXECUTE_ALARM, LocalUtil.TAG_ONETIME_ALARM);
                    triggerTime = calendar.getTimeInMillis() - curTime;
                    AlarmManagerSet(calendar.getTimeInMillis(), pi);
                } else {
                    // 这是循环闹钟,只能循环查找1次
                    int loop = 1;
                    int curYear = calendar.get(Calendar.YEAR);
                    int curMonth = calendar.get(Calendar.MONTH);
                    int curDay = calendar.get(Calendar.DAY_OF_MONTH);
                    // 系统表示的weekday，是1代表周日，2代表周一，以此类推，7代表周六
                    // alarm数据库中的weekday，是由0-6代表周一到周日
                    // 因此对应关系是：数据库中0~5对应系统中的2~7表示周一到周六，数据库中6对应系统的1，代表周日
                    int curWeekDay = calendar.get(Calendar.DAY_OF_WEEK);
                    AlarmOnTime.putExtra(LocalUtil.TAG_EXECUTE_ALARM, LocalUtil.TAG_REPEAT_ALARM);

                    while (loop >= 0) {
                        if(weekStatus[LocalUtil.SysWeekToDbWeek(curWeekDay)] != 0) {
                            loop--;

                            // 若闹钟时间在当前时间的后面，则当天可以设定闹钟，结束循环
                            if(calendar.getTimeInMillis() > curTime) {
                                triggerTime = calendar.getTimeInMillis() - curTime;
                                AlarmManagerSet(calendar.getTimeInMillis(), pi);
                                break;
                            }
                        }

                        curWeekDay++;
                        if(curWeekDay > Calendar.DAY_OF_WEEK) {
                            curWeekDay %= Calendar.DAY_OF_WEEK;
                        }
                        curDay++;
                        if(curDay > calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                            curMonth++;
                            // 每个月的第一天是从1开始
                            curDay = 1;
                            if(curMonth > calendar.getActualMaximum(Calendar.MONTH)) {
                                // 每年的第一个月是从0开始
                                curMonth = 0;
                                curYear++;
                            }
                        }

                        if(weekStatus[LocalUtil.SysWeekToDbWeek(curWeekDay)] != 0) {
                            calendar.set(Calendar.YEAR, curYear);
                            calendar.set(Calendar.MONTH, curMonth);
                            calendar.set(Calendar.DAY_OF_MONTH, curDay);
                            triggerTime = calendar.getTimeInMillis() - curTime;
                            AlarmManagerSet(calendar.getTimeInMillis(), pi);
                            break;
                        }

                    }
                }
                if(0 == triggerTime) {
                    Log.e(ERRTAG, "trigger time is 0");
                }else {
                    int totalMin = 0;
                    int totalSec = 0;
                    int day = 0;
                    int hour = 0;
                    int minute = 0;
                    int second = 0;

                    totalSec = (int)triggerTime/1000;
                    second = totalSec % 60;
                    totalMin = totalSec / 60;
                    minute = totalMin % 60;
                    hour = totalMin / 60;
                    day = hour / 24;
                    if(day >= 1) {
                        hour %= 24;
                    }

                    triggerTimeStr = "距离闹钟时间还有";

                    /* start:此处为了在设置后显示出下一个闹钟距今还有多久 */
                    // 由于Toast需要在UI界面上显示，因此使用Looper找到主线程(?)，通过handler发消息
                    if(0 != day) {
                        triggerTimeStr += String.valueOf(day) + "天";
                    }
                    if(0 != hour) {
                        triggerTimeStr += String.valueOf(hour) + "小时";
                    }
                    if(0 != minute) {
                        triggerTimeStr += String.valueOf(minute) + "分钟";
                    }
                    triggerTimeStr += String.valueOf(second) + "秒";

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), triggerTimeStr, Toast.LENGTH_SHORT).show();
                        }
                    });
                    /* end:此处为了在设置后显示出下一个闹钟距今还有多久 */

                    Log.e(ERRTAG, "hour:  " + String.valueOf(hour)
                                    + " min: " + String.valueOf(minute)
                                    + " sec: " + String.valueOf(second));
                    }

                }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    protected void AlarmManagerSet(long modifyTime, PendingIntent pi) {
        if(null == pi) {
            Log.e(ERRTAG, "PendingIntent is null");
            return;
        }

        // 当系统版本是android4.4以上，使用setExact函数，下同
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, modifyTime, pi);
        }else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, modifyTime, pi);
        }
    }
}
