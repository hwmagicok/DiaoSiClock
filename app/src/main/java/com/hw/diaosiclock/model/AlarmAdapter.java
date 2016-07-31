package com.hw.diaosiclock.model;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.hw.diaosiclock.R;

import java.util.List;

/**
 * Created by hw on 2016/4/3.
 */
public class AlarmAdapter extends ArrayAdapter<Alarm> {
    private int viewID;
    public AlarmAdapter(Context context, int ViewResourceId, List<Alarm> list) {
        super(context, ViewResourceId, list);
        viewID = ViewResourceId;
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        AlarmViewHolder holder;
        Alarm alarm = getItem(position);
        if(null == convertView) {
            view = LayoutInflater.from(getContext()).inflate(viewID, null);
            holder = new AlarmViewHolder();
            holder.time = (TextView)view.findViewById(R.id.time);
            holder.monday_status = (TextView)view.findViewById(R.id.monday_status);
            holder.tuesday_status = (TextView)view.findViewById(R.id.tuesday_status);
            holder.wednesday_status = (TextView)view.findViewById(R.id.wednesday_status);
            holder.thursday_status = (TextView)view.findViewById(R.id.thursday_status);
            holder.friday_status = (TextView)view.findViewById(R.id.friday_status);
            holder.saturday_status = (TextView)view.findViewById(R.id.saturday_status);
            holder.sunday_status = (TextView)view.findViewById(R.id.sunday_status);
            holder.alarm_switch = (CheckBox)view.findViewById(R.id.alarm_switch);
            view.setTag(holder);

        }else {
            view = convertView;
            holder = (AlarmViewHolder)view.getTag();
        }

        if (alarm.get24HourFormat()) {
            holder.time.setText(alarm.getTime());
        }else {
            holder.time.setText(alarm.displayTime());
        }

        int[] weekstatus = alarm.getWeekStatus();

        if(null == weekstatus) {
            Log.e("AlarmAdapter", "WeekStatus is null");
        }

        holder.monday_status.setTextColor(WeekColor(weekstatus[0]));
        holder.tuesday_status.setTextColor(WeekColor(weekstatus[1]));
        holder.wednesday_status.setTextColor(WeekColor(weekstatus[2]));
        holder.thursday_status.setTextColor(WeekColor(weekstatus[3]));
        holder.friday_status.setTextColor(WeekColor(weekstatus[4]));
        holder.saturday_status.setTextColor(WeekColor(weekstatus[5]));
        holder.sunday_status.setTextColor(WeekColor(weekstatus[6]));

        holder.alarm_switch.setChecked(alarm.getAlarmOnOrOff());

        return view;
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
}

class AlarmViewHolder {
    //String pic;
    TextView time;
    TextView monday_status;
    TextView tuesday_status;
    TextView wednesday_status;
    TextView thursday_status;
    TextView friday_status;
    TextView saturday_status;
    TextView sunday_status;
    CheckBox alarm_switch;
}