package com.hw.diaosiclock.model;

import android.util.Log;

/**
 * Created by hw on 2016/7/9.
 */
public class AlarmMusic {
    public static final String ERRTAG = "AlarmMusic";
    private String musicName;
    private String musicPath;

    public AlarmMusic() {

    }

    public AlarmMusic(final String name, final String path) {
        musicName = name;
        musicPath = path;
    }

    public void SetAlarmMusicName(final String name) {
        musicName = name;
    }

    public void SetAlarmMusicPath(final String path) {
        musicPath = path;
    }

    public String GetAlarmMusicName(AlarmMusic alarm_music) {
        if(null == alarm_music) {
            Log.e(ERRTAG, "alarm music is null");
            return null;
        }
        return alarm_music.musicName;
    }

    public String GetAlarmMusicPath(AlarmMusic alarm_music) {
        if(null == alarm_music) {
            Log.e(ERRTAG, "alarm music is null");
            return null;
        }
        return alarm_music.musicPath;
    }
}
