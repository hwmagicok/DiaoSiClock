package com.hw.diaosiclock.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.WindowManager;

import com.hw.diaosiclock.R;
import com.hw.diaosiclock.model.Alarm;
import com.hw.diaosiclock.model.AlarmBackgroundService;
import com.hw.diaosiclock.model.AlarmDB;
import com.hw.diaosiclock.util.LocalUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by hw on 2016/7/22.
 */
public class AlarmOnTimeActivity extends Activity {
    public static final String ERRTAG = "AlarmOnTimeActivity";
    private AlertDialog alarmOnTime;
    private static HashMap<Integer, Integer> repeatTimeMap = null;
    private AlarmManager alarmManager = null;
    private Intent intent = null;
    private Intent StartServiceIntent = null;
    private PendingIntent pi = null;
    private MediaPlayer mediaPlayer = null;
    private Vibrator vibrator = null;
    AlarmDB db = null;
    private Alarm alarm = null;

    Timer timer = null;
    AlarmTimerTask timerTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_ontime);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        final Intent intentFromReceiver = getIntent();
        if(null == intentFromReceiver) {
            Log.e(ERRTAG, "intent is null");
            return;
        }

        alarm = intentFromReceiver.getParcelableExtra(LocalUtil.TAG_EXECUTE_ALARM);
        if(null == alarm) {
            Log.e(ERRTAG, "alarm is null");
            return;
        }

        if(null == repeatTimeMap) {
            repeatTimeMap = new HashMap<>();
        }

        Log.e(ERRTAG, "alarm id: " + String.valueOf(alarm.getAlarmID())
                        + " time: " + alarm.getTime());

        SimpleDateFormat curTime = new SimpleDateFormat("HH:mm", Locale.CHINA);

        /* start: 设置1分钟的Timer */
        timer = new Timer();
        timerTask = new AlarmTimerTask();
        timer.schedule(timerTask, 1000*60);
        /* end: 设置1分钟的Timer */

        db = AlarmDB.getInstance(this);
        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        // 播放闹铃音乐。也不知道这个地是不是该开个线程
        mediaPlayer = new MediaPlayer();
        LocalUtil.playAlarmMusic(mediaPlayer, AlarmOnTimeActivity.this, alarm, true);

        // 打开震动
        if(alarm.getShockStatus()) {
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] vibratorPattern = {1000, 2000, 1000, 2000};
            vibrator.vibrate(vibratorPattern, 0);
        }

        alarmOnTime = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(alarm.getTime() + "的闹钟")
                .setMessage("当前时间：" + curTime.format(new Date()) + "\n")
                .setNegativeButton("推迟", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Todo to delete
                        Log.e(ERRTAG, "alarm id[" + alarm.getAlarmID() + "] repeat time[" + getRepeatTime(alarm.getAlarmID()) + "]");
                        mediaPlayer.stop();
                        if (alarm.getShockStatus()) {
                            vibrator.cancel();
                        }
                        timer.cancel();

                        // 最多重复三次
                        if (alarm.getAlarm_interval() != 0 && getRepeatTime(alarm.getAlarmID()) < 3) {
                            setRepeatTime(alarm.getAlarmID());
                            SendIntervalAlarmBroadcast();
                        } else {
                            if(!alarm.isRepeatAlarm()) {
                                if(0 == alarm.getAlarm_interval() || getRepeatTime(alarm.getAlarmID()) == 3) {
                                    if(repeatTimeMap.containsKey(alarm.getAlarmID())) {
                                        repeatTimeMap.remove(alarm.getAlarmID());
                                    }
                                    alarm.setAlarmSwitch(false);
                                    db.updateSpecificAlarm(alarm);
                                }
                            }else {
                                if(getRepeatTime(alarm.getAlarmID()) == 3) {
                                    repeatTimeMap.remove(alarm.getAlarmID());
                                }
                                SendNextAlarmService();
                            }
                        }

                        dialog.dismiss();
                        finish();
                    }
                })
                .setPositiveButton("结束", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mediaPlayer.stop();
                        if (alarm.getShockStatus()) {
                            vibrator.cancel();
                        }
                        timer.cancel();

                        if(!alarm.isRepeatAlarm()) {
                            alarm.setAlarmSwitch(false);
                            db.updateSpecificAlarm(alarm);
                        }

                        SendNextAlarmService();
                        dialog.dismiss();
                        finish();
                    }
                }).create();
        alarmOnTime.show();

    }

    private class AlarmTimerTask extends TimerTask {
        @Override
        public void run() {
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                vibrator.cancel();
                alarmOnTime.dismiss();
            }

            if(0 != alarm.getAlarm_interval()) {
                if(3 == getRepeatTime(alarm.getAlarmID())) {
                    if(alarm.isRepeatAlarm()) {
                        SendNextAlarmService();
                    }else {
                        repeatTimeMap.remove(alarm.getAlarmID());
                        alarm.setAlarmSwitch(false);
                        db.updateSpecificAlarm(alarm);
                    }
                } else {
                    // Todo to delete
                    Log.e(ERRTAG, "alarm id[" + alarm.getAlarmID() + "] repeat time[" + getRepeatTime(alarm.getAlarmID()) + "]");
                    setRepeatTime(alarm.getAlarmID());
                    SendIntervalAlarmBroadcast();
                }
            }else {
                if(alarm.isRepeatAlarm()) {
                    SendNextAlarmService();
                }else {
                    alarm.setAlarmSwitch(false);
                    db.updateSpecificAlarm(alarm);
                }
            }
            finish();
        }
    }

    // 取出闹钟已重复的次数，最大重复次数为3
    private synchronized int getRepeatTime(int alarmId) {
        int repeatTime = 0;
        if(!repeatTimeMap.isEmpty()) {
            if(repeatTimeMap.containsKey(alarmId)) {
                repeatTime = repeatTimeMap.get(alarmId);
            }
        }
        return repeatTime;
    }

    private synchronized void setRepeatTime(int alarmId) {
        if(!repeatTimeMap.containsKey(alarmId)) {
            repeatTimeMap.put(alarmId, 0);
        }
        if(repeatTimeMap.get(alarmId) < 3) {
            repeatTimeMap.put(alarmId, repeatTimeMap.get(alarmId) + 1);
        } else {
            Log.e(ERRTAG, "repeat time reaches upper limit(3)");
        }
    }

    // 启动3、5分钟间隔的那种闹钟
    private void SendIntervalAlarmBroadcast() {
        if(null == intent) {
            intent = new Intent("com.hw.diaosiclock.EXECUTE_CLOCK");
        }
        intent.putExtra(LocalUtil.TAG_EXECUTE_ALARM, alarm.getAlarmID());

        pi = PendingIntent.getBroadcast(AlarmOnTimeActivity.this, alarm.getAlarmID(), intent, 0);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + alarm.getAlarm_interval() * 60 * 1000, pi);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + alarm.getAlarm_interval() * 60 * 1000, pi);
        }
    }

    // 停止当前3、5分钟间隔的闹钟，启动明天或是后几天闹钟的Service
    private void SendNextAlarmService() {
        if(repeatTimeMap.containsKey(alarm.getAlarmID())) {
            repeatTimeMap.remove(alarm.getAlarmID());
        }

        if(null != alarmManager) {
            alarmManager.cancel(pi);
        }

        mediaPlayer.release();

        StartServiceIntent = new Intent(AlarmOnTimeActivity.this, AlarmBackgroundService.class);
        StartServiceIntent.putExtra(LocalUtil.TAG_EXECUTE_ALARM, alarm.getAlarmID());
        startService(StartServiceIntent);
    }

}
