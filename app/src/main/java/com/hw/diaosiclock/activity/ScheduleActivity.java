package com.hw.diaosiclock.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.hw.diaosiclock.R;
import com.hw.diaosiclock.model.Alarm;
import com.hw.diaosiclock.model.AlarmAdapter;
import com.hw.diaosiclock.model.AlarmDB;
import com.hw.diaosiclock.util.LocalUtil;

import java.util.ArrayList;


public class ScheduleActivity extends AppCompatActivity {
    public static final String ERRTAG = "ScheduleActivity";

    private static ArrayList<Alarm> AlarmList;
    private AlarmAdapter alarmAdapter;
    private AlarmDB alarmDB;
    private RecyclerView alarmRecyclerView;
    private int contextMenuLongPressPosition;

    public static final int CODE_CREATE_ALARM = 1;
    public static final int CODE_SET_ALARM = 2;
    public static final int CODE_SET_ALARM_MUSIC = 3;
    public static final int CODE_DISPLAY_WEATHER_DETAIL = 4;

    public static final int TAG_SET_CONTEXTMENU = 0;
    public static final int TAG_DELETE_CONTEXTMENU = 1;

    private Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_schedule);

        alarmDB = AlarmDB.getInstance(this);
        if(null == alarmDB) {
            Log.e(ERRTAG, "alarmDB is null");
            finish();
        }
        cursor = alarmDB.queryAllAlarm();
        AlarmList = new ArrayList<>();

        if(cursor.moveToFirst()) {
            do {
                AlarmList.add(alarmDB.getAlarmByCursor(cursor));
                alarmDB.outputAlarmAllPara(cursor);
            }while (cursor.moveToNext());
            cursor.close();
        }else {
            Log.e(ERRTAG, "moveToFirst fail");
        }

        alarmRecyclerView = (RecyclerView)findViewById(R.id.schedule);
        alarmAdapter = new AlarmAdapter(AlarmList);
        alarmRecyclerView.setHasFixedSize(true);
        alarmRecyclerView.setAdapter(alarmAdapter);
        //Todo list的分割线
        /*
        alarmRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST)); */

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        alarmRecyclerView.setLayoutManager(layoutManager);

        alarmAdapter.setOnItemClickListener(new AlarmAdapter.OnRecycleViewListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent_modifyAlarm = new Intent(ScheduleActivity.this, SetAlarmActivity.class);
                intent_modifyAlarm.putExtra("Set_AlarmData", position);
                startActivityForResult(intent_modifyAlarm, CODE_SET_ALARM);
            }

            @Override
            public void onItemLongClick(int position) {

            }

            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, int position) {
                Alarm SelectedAlarm = alarmAdapter.getItem(position);
                contextMenuLongPressPosition = position;
                if(null == SelectedAlarm) {
                    Log.e(ERRTAG, "Selected Alarm is null");
                }else {
                    String time = SelectedAlarm.displayTime();
                    menu.setHeaderTitle(time);
                    menu.add(0, TAG_SET_CONTEXTMENU, 0, "修改闹钟配置");
                    menu.add(0, TAG_DELETE_CONTEXTMENU, 0, "删除闹钟配置");
                }
            }
        });

        // 可伸缩的toolbar
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        if(null != collapsingToolbar) {
            collapsingToolbar.setTitle("hw闹钟");
            collapsingToolbar.setExpandedTitleColor(Color.TRANSPARENT);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.schedule_view_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(null != toolbar) {
            // 返回键点击逻辑
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        //点击加号创建闹钟
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if(null != fab) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ScheduleActivity.this, SetAlarmActivity.class);
                    startActivityForResult(intent, CODE_CREATE_ALARM);

                }
            });
        }

        for(Alarm tmpAlarm : AlarmList) {
            LocalUtil.reopenAlarmService(this, tmpAlarm);
        }


        LocalUtil.LoadResForWeather(this, true);

        RelativeLayout layout_briefWeather = (RelativeLayout)findViewById(R.id.layout_brief_weather);
        if(null != layout_briefWeather) {
            layout_briefWeather.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ScheduleActivity.this, WeatherDetailActivity.class);
                    startActivityForResult(intent, CODE_DISPLAY_WEATHER_DETAIL);
                }
            });
        }

    }

    // 此处配合上面的长按list，选择不同的选项产生不同的反馈
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // 修改选中的那个闹钟
            case TAG_SET_CONTEXTMENU:
                Intent intent_modifyAlarm = new Intent(ScheduleActivity.this, SetAlarmActivity.class);
                intent_modifyAlarm.putExtra("Set_AlarmData", contextMenuLongPressPosition);
                startActivityForResult(intent_modifyAlarm, CODE_SET_ALARM);
                break;

            // 删除选中的那个闹钟
            case TAG_DELETE_CONTEXTMENU:
                // 此处为了给PositiveButton的内部类OnClickListener使用，将Alarm定义为了final
                final Alarm SelectedAlarm = alarmAdapter.getItem(contextMenuLongPressPosition);

                AlertDialog.Builder DelAlertDialog = new AlertDialog.Builder(ScheduleActivity.this);
                DelAlertDialog.setTitle(SelectedAlarm.displayTime());
                DelAlertDialog.setMessage("你确定要删除该闹钟？");

                DelAlertDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                DelAlertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 数据库中删除对应Alarm
                        alarmDB.delSpecificAlarm(SelectedAlarm);
                        // list中删除对应Alarm
                        AlarmList.remove(contextMenuLongPressPosition);
                        alarmAdapter.notifyDataSetChanged();
                    }
                });

                DelAlertDialog.show();
                break;

        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(null == data) {
            Log.e(ERRTAG, "intent from SetAlarmActivity is null");
            return;
        }
        switch (requestCode) {

            case CODE_CREATE_ALARM:
                break;

            case CODE_SET_ALARM:
                break;

            case CODE_DISPLAY_WEATHER_DETAIL:
                if(RESULT_OK == resultCode) {
                    LocalUtil.LoadResForWeather(this, true);
                }
        }
        alarmAdapter.notifyDataSetChanged();
    }

    static synchronized ArrayList<Alarm> GetAlarmList() {
        return AlarmList;
    }
}
