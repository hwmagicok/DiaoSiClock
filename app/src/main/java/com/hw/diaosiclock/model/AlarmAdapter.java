package com.hw.diaosiclock.model;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hw.diaosiclock.R;
import com.hw.diaosiclock.util.LocalUtil;

import java.util.List;

/**
 * Created by hw on 2016/4/3.
 */
public class AlarmAdapter extends RecyclerView.Adapter<AlarmViewHolder> {
    public static final String ERRTAG = "AlarmAdapter";
    private View view;
    private List<Alarm> list;
    private OnRecycleViewListener onItemClickListener;

    public interface OnRecycleViewListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
        void onCreateContextMenu(ContextMenu menu, View v, int position);
    }

    public AlarmAdapter(List l) {
        if(null == l) {
            Log.e(ERRTAG, "list is null");
            return;
        }
        list = l;
    }

    @Override
    public AlarmViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.alarm_summarize, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AlarmViewHolder holder, final int position) {
        Alarm alarm = list.get(position);
        if(null == alarm) {
            Log.e(ERRTAG, "alarm is null");
            return;
        }

        if (alarm.get24HourFormat()) {
            holder.time.setText(alarm.getTime());
        }else {
            holder.time.setText(alarm.displayTime());
        }

        int[] weekstatus = alarm.getWeekStatus();

        if(null == weekstatus) {
            Log.e(ERRTAG, "WeekStatus is null");
        }

        holder.monday_status.setTextColor(WeekColor(weekstatus[0]));
        holder.tuesday_status.setTextColor(WeekColor(weekstatus[1]));
        holder.wednesday_status.setTextColor(WeekColor(weekstatus[2]));
        holder.thursday_status.setTextColor(WeekColor(weekstatus[3]));
        holder.friday_status.setTextColor(WeekColor(weekstatus[4]));
        holder.saturday_status.setTextColor(WeekColor(weekstatus[5]));
        holder.sunday_status.setTextColor(WeekColor(weekstatus[6]));

        holder.alarm_switch.setChecked(alarm.getAlarmOnOrOff());
        holder.alarm_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox curCheckBox = (CheckBox)v.findViewById(R.id.alarm_switch);
                Alarm alarm = list.get(position);

                boolean status = curCheckBox.isChecked();
                alarm.setAlarmSwitch(status);

                AlarmDB alarmDB = AlarmDB.getInstance(view.getContext());
                if(null == alarmDB) {
                    Log.e(ERRTAG, "db is null");
                    return;
                }
                alarmDB.updateSpecificAlarm(alarm);
                // 实际上架构应该使用ContentObserver，但是由于时间紧迫，就等下次改进吧
                Intent intent = new Intent(view.getContext(), AlarmBackgroundService.class);
                intent.putExtra(LocalUtil.TAG_EXECUTE_ALARM, alarm.getAlarmID());
                view.getContext().startService(intent);
            }
        });

        if(null != onItemClickListener) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getLayoutPosition();
                    onItemClickListener.onItemClick(position);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = holder.getLayoutPosition();
                    onItemClickListener.onItemLongClick(position);
                    return false;
                }
            });

            holder.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    int position = holder.getLayoutPosition();
                    onItemClickListener.onCreateContextMenu(menu, v, position);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setOnItemClickListener(OnRecycleViewListener listener) {
        onItemClickListener = listener;
    }

    private int WeekColor(int weekstatus) {
        if(0 == weekstatus){
            return Color.GRAY;
        }else if(1 == weekstatus) {
            return Color.BLACK;
        }else {
            Log.e("AlarmAdapter", "color input error");
            return 0;
        }
    }

    public Alarm getItem(int position) {
        return list.get(position);
    }
}

class AlarmViewHolder extends RecyclerView.ViewHolder {
    TextView time;
    TextView monday_status;
    TextView tuesday_status;
    TextView wednesday_status;
    TextView thursday_status;
    TextView friday_status;
    TextView saturday_status;
    TextView sunday_status;
    CheckBox alarm_switch;

    public AlarmViewHolder(View view) {
        super(view);

        time = (TextView)view.findViewById(R.id.time);
        monday_status = (TextView)view.findViewById(R.id.monday_status);
        tuesday_status = (TextView)view.findViewById(R.id.tuesday_status);
        wednesday_status = (TextView)view.findViewById(R.id.wednesday_status);
        thursday_status = (TextView)view.findViewById(R.id.thursday_status);
        friday_status = (TextView)view.findViewById(R.id.friday_status);
        saturday_status = (TextView)view.findViewById(R.id.saturday_status);
        sunday_status = (TextView)view.findViewById(R.id.sunday_status);
        alarm_switch = (CheckBox)view.findViewById(R.id.alarm_switch);
    }
}

