package com.hw.diaosiclock.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hw.diaosiclock.R;
import com.hw.diaosiclock.model.Alarm;
import com.hw.diaosiclock.model.AlarmBackgroundService;
import com.hw.diaosiclock.model.AlarmDB;
import com.hw.diaosiclock.util.AlarmCallbackListener;
import com.hw.diaosiclock.util.LocalUtil;

import java.util.Calendar;


/**
 * Created by hw on 2016/5/5.
 */
public class SetAlarmActivity extends AppCompatActivity {

    public static final String ERRTAG = "SetAlarmActivity";

    private boolean bRet = true;
    private Alarm alarm = null;
    private Alarm tmpAlarm = null;

    //用来记录Alarm在AlarmList中的位置，默认是-1
    private int AlarmPosition = -1;
    private TextView setTime = null;

    private TextView monday = null;
    private TextView tuesday = null;
    private TextView wednesday = null;
    private TextView thursday = null;
    private TextView friday = null;
    private TextView saturday = null;
    private TextView sunday = null;

    private EditText alarm_name = null;
    private SeekBar alarm_volume = null;
    private TextView alarm_interval = null;
    private CheckBox alarm_isVibration = null;
    private TextView alarm_save = null;
    private TextView alarm_music = null;

    private LinearLayout layout_time = null;
    private LinearLayout layout_interval = null;
    private LinearLayout layout_repeat = null;

    private AlarmDB alarmDB = null;

    // 用来展示week重复情况的那个list，实际上是专门为了全选准备的
    private ListView weekStatusList = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarmset);

        setTime = (TextView)findViewById(R.id.time);

        monday = (TextView)findViewById(R.id.monday_status);
        tuesday = (TextView)findViewById(R.id.tuesday_status);
        wednesday = (TextView)findViewById(R.id.wednesday_status);
        thursday = (TextView)findViewById(R.id.thursday_status);
        friday = (TextView)findViewById(R.id.friday_status);
        saturday = (TextView)findViewById(R.id.saturday_status);
        sunday = (TextView)findViewById(R.id.sunday_status);

        alarm_name = (EditText)findViewById(R.id.alarm_name);
        alarm_interval = (TextView)findViewById(R.id.interval);
        alarm_isVibration = (CheckBox)findViewById(R.id.isVibration);
        alarm_music = (TextView)findViewById(R.id.alarm_music);
        alarm_volume = (SeekBar)findViewById(R.id.alarm_volume);

        alarm_save = (TextView)findViewById(R.id.save);

        final Intent intent = getIntent();
        if(null == intent) {
            Log.e(ERRTAG, "intent is null");
            finish();
        }else {
            AlarmPosition = intent.getIntExtra("Set_AlarmData", -1);
            alarm = new Alarm();
            if(-1 != AlarmPosition) {
                tmpAlarm = ScheduleActivity.GetAlarmList().get(AlarmPosition);
                alarm = (Alarm) tmpAlarm.clone();

                SetTimeText(alarm);
                SetWeekRepeatText(alarm);
                SetAlarmNameText(alarm);
                SetAlarmMusicText(alarm);
                SetAlarmVolumeText(alarm);
                SetAlarmIntervalText(alarm);
                SetAlarmShockText(alarm);
            }
        }

        // 闹钟的开关打开
        alarm.setAlarmSwitch(true);

        //设置闹钟时间
        layout_time = (LinearLayout)findViewById(R.id.layout_time);
        layout_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bRet = alarm.AddAlarm(SetAlarmActivity.this, new AlarmCallbackListener() {
                    @Override
                    public void onFinish(Alarm alarm) {
                        if(null == alarm) {
                            Log.e("SetAlarmActivity", "Alarm is null");
                        }
                        SetTimeText(alarm);
                    }
                });
                if(!bRet) {
                    Log.e("SetAlarmActivity", "Add Alarm fail");
                }
            }
        });

        //设置重复
        layout_repeat = (LinearLayout)findViewById(R.id.layout_repeat);
        layout_repeat.setOnClickListener(new View.OnClickListener() {
            int[] weekStatus = alarm.getWeekStatus();
            boolean []weekChoiceResult = new boolean[8];
            TextView [] WeekTextViewArr = new TextView[] {monday, tuesday, wednesday, thursday, friday, saturday, sunday};
            @Override
            public void onClick(View v) {

                weekChoiceResult[0] = true;
                for(int day = 0; day < 7; day++) {
                    if(0 == weekStatus[day]) {
                        weekChoiceResult[day + 1] = false;

                        if(weekChoiceResult[0]) {
                            weekChoiceResult[0] = false;
                        }
                    }else {
                        weekChoiceResult[day + 1] = true;
                    }
                }

                final String weekChoice[] = {"全选", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
                AlertDialog weekDialog = new AlertDialog.Builder(SetAlarmActivity.this)
                        .setMultiChoiceItems(weekChoice, weekChoiceResult, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                weekChoiceResult[which] = isChecked;
                                if(0 == which) {
                                    for(int i = 0; i < weekChoice.length; i++) {
                                        weekStatusList.setItemChecked(i, isChecked);
                                        weekChoiceResult[i] = isChecked;
                                    }
                                }else {
                                    weekChoiceResult[which] = isChecked;
                                    if(!isChecked && true == weekChoiceResult[0]) {
                                        weekChoiceResult[0] = false;
                                        weekStatusList.setItemChecked(0, false);
                                    }
                                }
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for(int i = 1; i < weekChoiceResult.length; i++) {
                                    alarm.setWeek(i - 1, weekChoiceResult[i]);
                                    if(!weekChoiceResult[i]) {
                                        WeekTextViewArr[i-1].setTextColor(Color.GRAY);
                                    }else {
                                        WeekTextViewArr[i-1].setTextColor(Color.BLACK);
                                    }
                                }
                            }
                        }).create();
                weekStatusList = weekDialog.getListView();
                weekDialog.show();
            }
        });

        // 设置闹钟铃声
        alarm_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetAlarmActivity.this, SelectAlarmMusicActivity.class);
                if(-1 != AlarmPosition) {
                    intent.putExtra(LocalUtil.TAG_SET_MUSIC, alarm.getAlarmMusic());
                }
                startActivityForResult(intent, ScheduleActivity.CODE_SET_ALARM_MUSIC);
            }
        });

        //设置闹钟音量
        // 先设置progress bar的最大音量
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        alarm_volume.setMax(maxVolume);
        if(null == tmpAlarm) {
            // 这说明是新创建的Alarm而非要进行修改的
            alarm_volume.setProgress(maxVolume/2);
            alarm.setVolume(maxVolume/2);
        }

        alarm_volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Todo 这个地还应该添加一个用该音量播放的铃声辅助用户判断音量是否合适
                alarm.setVolume(seekBar.getProgress());
            }
        });

        //设置闹钟重复间隔（定时器）
        layout_interval = (LinearLayout)findViewById(R.id.layout_interval);
        layout_interval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(SetAlarmActivity.this);
                String[] interval_choice = {"3分钟", "5分钟", "10分钟", "15分钟"};

                dialog.setSingleChoiceItems(interval_choice, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                alarm.setAlarm_interval(3);
                                break;
                            case 1:
                                alarm.setAlarm_interval(5);
                                break;
                            case 2:
                                alarm.setAlarm_interval(10);
                                break;
                            case 3:
                                alarm.setAlarm_interval(15);
                                break;
                            default:
                                alarm.setAlarm_interval(0);
                        }
                        dialog.dismiss();
                        SetAlarmIntervalText(alarm);
                    }
                });
                dialog.show();
            }
        });

        //设置是否震动
        alarm_isVibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm.setShock(alarm_isVibration.isChecked());
            }
        });

        //设置保存
        alarm_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //设置闹钟名称
                String name = alarm_name.getText().toString();
                if(!name.equals("")) {
                    alarm.setAlarmName(name);
                }

                // 保存的时候时间不能为空，Alarm在创建的时候时间可以默认为0，故不能通过时间来判断
                if(null == setTime.getText() || "" == setTime.getText()) {
                    Toast.makeText(SetAlarmActivity.this, "时间不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                alarmDB = AlarmDB.getInstance(SetAlarmActivity.this);
                if(null == alarmDB) {
                    Log.e(ERRTAG, "alarmDB is null");
                }

                /* 保存的时候，如果是新创建的Alarm则直接保存到数据库
                若是修改的Alarm，则更新数据库 */
                Intent intent = new Intent();
                if(-1 == AlarmPosition) {
                    alarmDB.saveAlarm(alarm);
                    //intent.putExtra("Create Alarm", alarm);
                    int id = AlarmDB.getInstance(SetAlarmActivity.this).getLastAlarmID();
                    if(-1 == id) {
                        Log.e(ERRTAG, "the last id is wrong");
                        return;
                    }
                    alarm.setId(id);
                    ScheduleActivity.GetAlarmList().add(alarm);

                }else {
                    alarmDB.updateSpecificAlarm(alarm);
                    //intent.putExtra("Set Alarm", alarm);
                    tmpAlarm.copyFromAlarm(alarm);
                }

                setResult(RESULT_OK, intent);

                // 此处开始，调用service和定时器
                Intent service_intent = new Intent(SetAlarmActivity.this, AlarmBackgroundService.class);
                service_intent.putExtra(LocalUtil.TAG_EXECUTE_ALARM, alarm);
                startService(service_intent);

                finish();
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.alarm_set_toolbar);
        //toolbar.setTitle("闹钟设置");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // 返回键点击逻辑
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    //通过Alarm中数据设置SetAlarmActivity界面的显示
    protected void SetTimeText(Alarm alarm) {
        if(null == alarm) {
            Log.e(ERRTAG, "setTimeText" + "alarm is null");
            return;
        }
        setTime.setText(alarm.displayTime());
    }

    protected void SetWeekRepeatText(Alarm alarm) {
        if(null == alarm) {
            Log.e(ERRTAG, "SetWeekRepeatText" + "alarm is null");
            return;
        }

        int [] week = alarm.getWeekStatus();
        TextView [] WeekTextViewArr = new TextView[] {monday, tuesday, wednesday, thursday, friday, saturday, sunday};
        for(int i = 0; i < week.length; i++) {
            if(0 == week[i]) {
                WeekTextViewArr[i].setTextColor(Color.GRAY);
            }else if(1 == week[i]) {
                WeekTextViewArr[i].setTextColor(Color.BLACK);
            }
        }
    }

    protected void SetAlarmNameText(Alarm alarm) {
        if(null == alarm) {
            Log.e(ERRTAG, "SetAlarmNameText" + "alarm is null");
            return;
        }
        alarm_name.setText(alarm.getAlarmName());
    }

    protected void SetAlarmMusicText(Alarm alarm) {
        if(null == alarm) {
            Log.e(ERRTAG, "SetAlarmMusicText" + "alarm is null");
            return;
        }
        String fullMusicName = alarm.getAlarmMusic();
        if(null != fullMusicName) {
            int dotLoc = fullMusicName.lastIndexOf(".");
            if(0 >= dotLoc) {
                Log.e(ERRTAG, "dot location is wrong");
            }else {
                String musicName = fullMusicName.substring(0, dotLoc);
                alarm_music.setText(musicName);
            }
        }
    }

    protected void SetAlarmVolumeText(Alarm alarm) {
        if(null == alarm) {
            Log.e(ERRTAG, "SetAlarmVolumeText" + "alarm is null");
            return;
        }
        alarm_volume.setProgress(alarm.getVolume());
    }

    protected void SetAlarmIntervalText(Alarm alarm) {
        if(null == alarm) {
            Log.e(ERRTAG, "SetAlarmIntervalText" + "alarm is null");
            return;
        }
        alarm_interval.setText(String.valueOf(alarm.getAlarm_interval()) + "分钟");
    }

    protected void SetAlarmShockText(Alarm alarm) {
        if(null == alarm) {
            Log.e(ERRTAG, "SetAlarmShockText" + "alarm is null");
            return;
        }
        alarm_isVibration.setChecked(alarm.getShockStatus());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(null == data) {
            Log.e(ERRTAG, "intent from SelectAlarmMusicActivity is null");
            return;
        }
        switch (requestCode) {
            case ScheduleActivity.CODE_SET_ALARM_MUSIC:
                String fullMusicName = data.getStringExtra("return_musicname");
                if(null != fullMusicName) {
                    //为了去除歌曲名后面的后缀名
                    int dotLoc = fullMusicName.lastIndexOf(".");
                    if(0 >= dotLoc) {
                        Log.e(ERRTAG, "dot location is wrong");
                    }else {
                        String musicName = fullMusicName.substring(0, dotLoc);
                        alarm_music.setText(musicName);
                        alarm.setMusic(fullMusicName);
                    }
                }
            break;
        }
    }
}
