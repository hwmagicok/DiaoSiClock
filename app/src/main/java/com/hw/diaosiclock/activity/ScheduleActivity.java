package com.hw.diaosiclock.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

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
    private ListView alarmListView;

    public static final int CODE_CREATE_ALARM = 1;
    public static final int CODE_SET_ALARM = 2;
    public static final int CODE_SET_ALARM_MUSIC = 3;

    public static final int TAG_SET_CONTEXTMENU = 0;
    public static final int TAG_DELETE_CONTEXTMENU = 1;

    private Cursor cursor;

    //当点击list上某个Alarm的时候，此处用来标记所选择的Alarm的位置，-1代表没有被修改过
    private int modifyAlarmPosition = -1;
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
        AlarmList = new ArrayList<Alarm>();

        if(cursor.moveToFirst()) {
            do {
                AlarmList.add(alarmDB.getAlarmByCursor(cursor));
                alarmDB.outputAlarmAllPara(cursor);
            }while (cursor.moveToNext());
            cursor.close();
        }else {
            Log.e(ERRTAG, "moveToFirst fail");
        }

        alarmAdapter = new AlarmAdapter(this, R.layout.alarm_summarize, AlarmList);
        alarmListView = (ListView)findViewById(R.id.schedule);
        alarmListView.setAdapter(alarmAdapter);

        //点击list修改闹钟
        alarmListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                modifyAlarmPosition = position;
                Intent intent_modifyAlarm = new Intent(ScheduleActivity.this, SetAlarmActivity.class);
                intent_modifyAlarm.putExtra("Set_AlarmData", position);
                startActivityForResult(intent_modifyAlarm, CODE_SET_ALARM);
            }
        });

        /* start:list长按弹出contextMenu */
        alarmListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                AdapterView.AdapterContextMenuInfo SelectedAlarmInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
                int SelectedPosition = SelectedAlarmInfo.position;
                Alarm SelectedAlarm = alarmAdapter.getItem(SelectedPosition);
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

        /* end:list长按弹出contextMenu */

        Toolbar toolbar = (Toolbar) findViewById(R.id.schedule_view_toolbar);
        toolbar.setTitle("hw闹钟");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // 返回键点击逻辑
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //点击加号创建闹钟
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ScheduleActivity.this, SetAlarmActivity.class);
                startActivityForResult(intent, CODE_CREATE_ALARM);

            }
        });

        LocalUtil.reopenAlarmService(this, AlarmList);
    }

    // 此处配合上面的长按list，选择不同的选项产生不同的反馈
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo= (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        // 此处为了给PositiveButton的内部类OnClickListener使用，将SelectedAlarmPosition定义为了final
        final int SelectedAlarmPosition = menuInfo.position;
        switch (item.getItemId()) {
            // 修改选中的那个闹钟
            case TAG_SET_CONTEXTMENU:
                modifyAlarmPosition = SelectedAlarmPosition;
                Intent intent_modifyAlarm = new Intent(ScheduleActivity.this, SetAlarmActivity.class);
                intent_modifyAlarm.putExtra("Set_AlarmData", SelectedAlarmPosition);
                startActivityForResult(intent_modifyAlarm, CODE_SET_ALARM);
                break;

            // 删除选中的那个闹钟
            case TAG_DELETE_CONTEXTMENU:
                // 此处为了给PositiveButton的内部类OnClickListener使用，将Alarm定义为了final
                final Alarm SelectedAlarm = alarmAdapter.getItem(SelectedAlarmPosition);

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
                        AlarmList.remove(SelectedAlarmPosition);
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
            // 这个地有妥协，刚创建完Alarm还没有获得ID，出去修改会由于没有ID找不到对应的Alarm
            // 故此处每次创建完清除老列表，用数据库重新填充列表
            case CODE_CREATE_ALARM:
                if (resultCode == RESULT_OK ) {
                    /* 妥协的地方注释起来
                    Alarm newAlarm = data.getParcelableExtra("Create Alarm");
                    if(null != newAlarm) {
                        AlarmList.add(newAlarm);
                        alarmAdapter.notifyDataSetChanged();
                    }
                    */
                    /*
                    AlarmList.clear();

                    cursor = alarmDB.queryAllAlarm();

                    if(cursor.moveToFirst()) {
                        do {
                            AlarmList.add(alarmDB.getAlarmByCursor(cursor));
                            alarmDB.outputAlarmAllPara(cursor);
                        }while (cursor.moveToNext());
                        cursor.close();
                    }else {
                        Log.e(ERRTAG, "moveToFirst fail");
                    }
                    */
                }
                break;

            case CODE_SET_ALARM:
                /*
                if(-1 == modifyAlarmPosition) {
                    Log.e(ERRTAG, "the position of modified Alarm is wrong");
                } else {
                    if(RESULT_OK == resultCode) {
                        Alarm modifiedAlarm = data.getParcelableExtra("Set Alarm");
                        if(null == modifiedAlarm) {
                            Log.e(ERRTAG, "Set Alarm is null");
                        }else {
                            AlarmList.set(modifyAlarmPosition, modifiedAlarm);
                            alarmAdapter.notifyDataSetChanged();
                            modifyAlarmPosition = -1;
                        }
                    }
                    break;
                }
                */
                break;
        }
        alarmAdapter.notifyDataSetChanged();
    }

    static synchronized ArrayList<Alarm> GetAlarmList() {
        return AlarmList;
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.schedule_toolbar_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */
}
