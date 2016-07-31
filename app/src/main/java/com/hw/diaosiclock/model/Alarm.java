package com.hw.diaosiclock.model;

import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;
import android.widget.TimePicker;

import com.hw.diaosiclock.R;
import com.hw.diaosiclock.activity.SetAlarmActivity;
import com.hw.diaosiclock.util.AlarmCallbackListener;

import java.util.Calendar;

/**
 * Created by hw on 2016/4/3.
 */
public class Alarm implements Parcelable, Cloneable {
    public static final String ERRTAG = "Alarm";
    /* id是闹钟的唯一标示，不存入数据库，该值是数据库自动赋值(auto increment)
        取值唯一, 只用来取值使用 */
    private int AlarmID;
    //闹钟开关
    private boolean AlarmSwitch = false; //// TODO: 2016/7/16 这个元素需要在DB里面加上，有用 
    //小时只以24小时制进行存储
    private int hour;
    private int minute;
    //一周7天，默认为0，表示不打开闹钟
    private int[] week = {0, 0, 0, 0, 0, 0, 0};
    private String music;
    private int volume;
    private boolean isShock = true;
    private String AlarmName = null;
    private int Alarm_interval;
    //华为专属功能，查看是否为月末周六
    private boolean isLastSaturday = false;
    //是否为24小时制，这个一定要先于时间设置
    private static boolean is24HourFormat;

    public Alarm() {
        AlarmSwitch = true;
    }

    public Alarm(final int h, final int m) {
        AlarmSwitch = true;
        hour = h;
        minute = m;
    }

    public Alarm(Parcel in) {
        setId(in.readInt());
        setAlarmSwitch(in.readInt() == 1);
        setHour(in.readInt());
        setMinute(in.readInt());
        in.readIntArray(week);
        setMusic(in.readString());
        setVolume(in.readInt());
        setShock(in.readInt() == 1);
        setAlarmName(in.readString());
        setAlarm_interval(in.readInt());
        setLastSaturday(in.readInt() == 1);
        set24HourFormat(in.readInt() == 1);
    }

    /* 用于设置闹钟界面设置闹铃时间 */
    public boolean AddAlarm(final Context context, final AlarmCallbackListener listener) {
        final Alarm tmpAlarm = this;
        int initHour = 0;
        int initMinute = 0;
        Calendar calendar = Calendar.getInstance();
        String timeFormat = android.provider.Settings.System.getString(context.getContentResolver(),
                                                        android.provider.Settings.System.TIME_12_24);
        // 有的机子如果获得的是12小时制的可能返回null
        if(null == timeFormat || timeFormat.equals("12")) {
            set24HourFormat(false);
        }else {
            set24HourFormat(true);
        }

        String timeStr = ((TextView)((SetAlarmActivity)context).findViewById(R.id.time)).getText().toString();
        if(timeStr.equals("")) {
            initHour = calendar.get(Calendar.HOUR_OF_DAY);
            initMinute = calendar.get(Calendar.MINUTE);
        }else {
            String[] timeSegment = timeStr.split(":");
            initHour = Integer.parseInt(timeSegment[0]);
            initMinute = Integer.parseInt(timeSegment[1]);
        }

        new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar tmpCalendar = Calendar.getInstance();
                Calendar curCalendar = Calendar.getInstance();

                tmpCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                tmpCalendar.set(Calendar.MINUTE, minute);
                tmpCalendar.set(Calendar.SECOND, 0);
                tmpCalendar.set(Calendar.MILLISECOND, 0);

                //设置的时间不能比现在时间早
                while(curCalendar.getTimeInMillis() >= tmpCalendar.getTimeInMillis()) {
                    tmpCalendar.setTimeInMillis(tmpCalendar.getTimeInMillis() + 24*60*60*1000);
                }

                tmpAlarm.setTime(tmpCalendar.get(Calendar.HOUR_OF_DAY), tmpCalendar.get(tmpCalendar.MINUTE));
                listener.onFinish(tmpAlarm);

            }
        }, initHour, initMinute, is24HourFormat).show();
        return true;
    }

    public void setId(final int id) {
        AlarmID = id;
    }

    public void set24HourFormat(boolean flag) {
        is24HourFormat = flag;
    }

    public boolean setHour(final int h) {
        boolean result = false;
        if(h >= 0 && h < 24) {
            hour = h;
            result = true;
        }
        return result;
    }

    public boolean setMinute(final int m) {
        if(m >= 0 && m < 60) {
            minute = m;
            return true;
        }
        return false;
    }

    public boolean setTime(final int h, final int m) {
        return setHour(h) && setMinute(m);
    }

    public boolean setTime(final String t) {
        if(null == t) {
            return false;
        }
        String h_m[] = t.split(":");
        int h = Integer.parseInt(h_m[0]);
        int m = Integer.parseInt(h_m[1]);
        return setHour(h) && setMinute(m);
    }

    public void setWeek(final int[] weekFlag) {
        for(int i = 0; i < 7; i++) {
            week[i] = weekFlag[i];
        }
    }

    public boolean setWeek(final int index, final boolean bIsAlarm) {
        if(index > 6 || index < 0) {
            return false;
        }

        if(bIsAlarm) {
            week[index] = 1;
        }else {
            week[index] = 0;
        }

        return true;
    }

    public void setMusic(final String musicName) {
        music = musicName;
    }

    public void setLastSaturday(boolean flag) {
        isLastSaturday = flag;
    }

    public void setAlarmSwitch(boolean status) {
        AlarmSwitch = status;
    }

    public void setAlarmName(final String name) {
        if(null == AlarmName) {
            AlarmName = new String();
        }
        AlarmName = name;
    }

    public void setVolume(final int alarmVolume) {
        volume = alarmVolume;
    }

    public void setAlarm_interval(final int interval) {
        Alarm_interval = interval;
    }

    public void setShock(final boolean flag) {
        isShock = flag;
    }

    public int getAlarmID() {
        return AlarmID;
    }

    public int getTimeHour() {
        return hour;
    }

    public int getTimeMinute() {
        return minute;
    }

    public String getTime() {
        String time = String.valueOf(hour) + ":" + String.valueOf(minute);
        return time;
    }

    public boolean getAlarmOnOrOff() {
        return AlarmSwitch;
    }

    public int[] getWeekStatus() {
        return week;
    }

    public boolean getShockStatus() {
        return isShock;
    }

    public String getAlarmName() {
        return AlarmName;
    }

    public String getAlarmMusic() {
        return music;
    }

    public boolean getLastSaturday() {
        return isLastSaturday;
    }

    public boolean get24HourFormat() {
        return is24HourFormat;
    }

    public int getVolume() {
        return volume;
    }

    public int getAlarm_interval() {
        return Alarm_interval;
    }

    public String displayTime() {
        String str;
        if(!get24HourFormat()){
            if(getTimeHour() > 12) {
                str = "下午 " + (String.valueOf(getTimeHour() - 12))
                        + ":" + String.valueOf(getTimeMinute());
            }else {
                str = "上午 " + getTime();
            }
        }else {
            str = getTime();
        }
        return str;
    }

    @Override
    // 深复制
    public Object clone() {
        Alarm newAlarm = null;
        try {
            newAlarm = (Alarm)super.clone();
        }catch (Exception e) {
            Log.e(ERRTAG, "clone fail");
            Log.e(ERRTAG, Log.getStackTraceString(e));
        }
        return newAlarm;
    }

    // 浅复制
    public void copyFromAlarm(Alarm alarm) {
        if(null == alarm) {
            Log.e(ERRTAG, "alarm is null");
            return;
        }

        AlarmSwitch = alarm.getAlarmOnOrOff();
        hour = alarm.getTimeHour();
        minute = alarm.getTimeMinute();
        System.arraycopy(alarm.getWeekStatus(), 0, week, 0, 7);
        music = alarm.getAlarmMusic();
        volume = alarm.getVolume();
        isShock = alarm.getShockStatus();
        AlarmName = alarm.getAlarmName();
        Alarm_interval = alarm.getAlarm_interval();
        isLastSaturday = alarm.getLastSaturday();
        is24HourFormat = alarm.get24HourFormat();
    }

    public boolean isRepeatAlarm() {
        boolean bRet = false;
        for(int i : getWeekStatus()) {
            if(i == 1) {
                bRet = true;
                break;
            }
        }
        return bRet;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if(null == dest) {
            dest = Parcel.obtain();
        }

        dest.writeInt(AlarmID);
        dest.writeInt(AlarmSwitch ? 1 : 0);
        dest.writeInt(hour);
        dest.writeInt(minute);
        dest.writeIntArray(week);
        dest.writeString(music);
        dest.writeInt(volume);
        dest.writeInt(isShock ? 1 : 0);
        dest.writeString(AlarmName);
        dest.writeInt(Alarm_interval);
        dest.writeInt(isLastSaturday ? 1 : 0);
        dest.writeInt(is24HourFormat ? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Alarm> CREATOR = new Creator<Alarm>() {
        @Override
        public Alarm createFromParcel(Parcel source) {
            return new Alarm(source);
        }

        @Override
        public Alarm[] newArray(int size) {
            return new Alarm[size];
        }
    };

}
