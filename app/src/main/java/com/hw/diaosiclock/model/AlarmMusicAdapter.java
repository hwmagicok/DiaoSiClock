package com.hw.diaosiclock.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.hw.diaosiclock.R;
import com.hw.diaosiclock.activity.SelectAlarmMusicActivity;

import java.util.List;

/**
 * Created by hw on 2016/7/13.
 */
public class AlarmMusicAdapter extends ArrayAdapter<String> {
    //动态加载的view的ID
    private int viewID;

    public AlarmMusicAdapter(Context context, int resource, List<String> list) {
        super(context, resource, list);

        viewID = resource;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        AlarmMusicViewHolder holder;
        String name = getItem(position);
        if(null == convertView) {
            view = LayoutInflater.from(getContext()).inflate(viewID, null);
            holder = new AlarmMusicViewHolder();
            holder.layout_MusicSet = (LinearLayout)view.findViewById(R.id.segment_music_select) ;
            holder.SelectDot = (RadioButton)view.findViewById(R.id.music_select);
            holder.MusicName = (TextView)view.findViewById(R.id.alarm_music_name);

            view.setTag(holder);
        }else {
            view = convertView;
            holder = (AlarmMusicViewHolder)view.getTag();
        }

        holder.MusicName.setText(name);
        holder.SelectDot.setChecked(position == SelectAlarmMusicActivity.getDotLocation());

        return view;
    }
}

class AlarmMusicViewHolder {
    LinearLayout layout_MusicSet;
    RadioButton SelectDot;
    TextView MusicName;
}
