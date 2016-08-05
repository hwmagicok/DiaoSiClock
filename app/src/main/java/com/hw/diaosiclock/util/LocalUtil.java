package com.hw.diaosiclock.util;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import com.hw.diaosiclock.model.Alarm;
import com.hw.diaosiclock.model.AlarmBackgroundService;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by hw on 2016/2/20.
 */
public class LocalUtil {
    public static final String ERRTAG = "LocalUtil";
    public static final String TAG_EXECUTE_ALARM = "execute alarm";
    public static final String TAG_SET_MUSIC = "SetAlarmMusic";
    public static final int TAG_ONETIME_ALARM = 1;
    public static final int TAG_REPEAT_ALARM = 2;

    public static final String AlarmMusicPath = "Alarm";

    // 系统中周几的表示转化成本程序中Db中周几的表示
    public static int SysWeekToDbWeek(int sysWeek) {
        int dbWeek;
        if(sysWeek <= 0 || sysWeek > 7) {
            Log.e(ERRTAG, "sysWeek is wrong");
            return -1;
        }

        if(Calendar.SUNDAY == sysWeek) {
            dbWeek = 6;
        }else {
            dbWeek = sysWeek - 2;
        }
        return dbWeek;
    }

    // Db中周几的表示转化成系统中周几的表示
    public static int DbWeekToSysWeek(int dbWeek) {
        int sysWeek;
        if(dbWeek < 0 || dbWeek > 6) {
            Log.e(ERRTAG, "dbWeek is wrong");
            return -1;
        }

        sysWeek = (dbWeek + 2) % Calendar.DAY_OF_WEEK;
        return sysWeek;
    }

    // 获得月末周六的时间，时间保存在calendar中，传进来的calendar需确保跟现有年月一致无误
    public static void getLastSaturdayOfMonth(Calendar calendar, Alarm alarm) {
        if(null == alarm) {
            Log.e(ERRTAG, "alarm is null");
            return;
        }
        if(null == calendar) {
            calendar = Calendar.getInstance();
        }

        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getTimeHour());
        calendar.set(Calendar.MINUTE, alarm.getTimeMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        if(Calendar.SUNDAY == weekDay) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }else if(Calendar.SATURDAY != weekDay) {
            calendar.add(Calendar.DAY_OF_MONTH, -weekDay);
        }
    }

    // 使用alarm中的音乐进行播放
    public static boolean playAlarmMusic(MediaPlayer mediaPlayer, Context context, Alarm alarm) {

        if(null == context || null == alarm) {
            Log.e(ERRTAG, "context or alarm is null");
            return false;
        }

        boolean bRet = true;
        if(null == mediaPlayer) {
            mediaPlayer = new MediaPlayer();
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        mediaPlayer.reset();

        try {
            AssetFileDescriptor AlarmMusicDescriptor;
            AssetManager assetManager = context.getAssets();
            String AlarmMusicName;

            AlarmMusicName = alarm.getAlarmMusic();

            AlarmMusicDescriptor = assetManager.openFd(AlarmMusicPath + "/" + AlarmMusicName);
            //mediaPlayer.setLooping(true);
            mediaPlayer.setDataSource(AlarmMusicDescriptor.getFileDescriptor(),
                    AlarmMusicDescriptor.getStartOffset(), AlarmMusicDescriptor.getLength());
            mediaPlayer.setVolume(alarm.getVolume(), alarm.getVolume());
            AlarmMusicDescriptor.close();
            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch (Exception e) {
            bRet = false;
            Log.e(ERRTAG, "playing alarm music occurs error");
            Log.getStackTraceString(e);
        }finally {
            return bRet;
        }

    }

    // 使用music name代表的音乐进行播放，上一个方法的重载
    public static boolean playAlarmMusic(MediaPlayer mediaPlayer, Context context, String MusicName) {

        if(null == context || null == MusicName) {
            Log.e(ERRTAG, "context or music name is null");
            return false;
        }

        boolean bRet = true;
        if(null == mediaPlayer) {
            mediaPlayer = new MediaPlayer();
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        mediaPlayer.reset();

        try {
            AssetFileDescriptor AlarmMusicDescriptor;
            AssetManager assetManager = context.getAssets();
            AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

            String AlarmMusicName = MusicName;
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);

            AlarmMusicDescriptor = assetManager.openFd(AlarmMusicPath + "/" + AlarmMusicName);
            //mediaPlayer.setLooping(true);
            mediaPlayer.setDataSource(AlarmMusicDescriptor.getFileDescriptor(),
                    AlarmMusicDescriptor.getStartOffset(), AlarmMusicDescriptor.getLength());
            mediaPlayer.setVolume(maxVolume/2, maxVolume/2);
            AlarmMusicDescriptor.close();
            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch (Exception e) {
            bRet = false;
            Log.e(ERRTAG, "playing alarm music occurs error");
            Log.getStackTraceString(e);
        }finally {
            return bRet;
        }

    }

    // 重启闹钟服务，应用于开机自启或是进程被关后重新打开应用
    public static void reopenAlarmService(Context context, ArrayList<Alarm> list) {
        if(null == context || null == list) {
            Log.e(ERRTAG, "context or list is null");
            return;
        }

        for(Alarm alarm : list) {
            Intent intent = new Intent(context, AlarmBackgroundService.class);
            intent.putExtra(TAG_EXECUTE_ALARM, alarm.getAlarmID());
            PendingIntent pi = PendingIntent.getService(context, alarm.getAlarmID(), intent, PendingIntent.FLAG_NO_CREATE);
            if(null == pi) {
                context.startService(intent);
            }
        }
    }

}
