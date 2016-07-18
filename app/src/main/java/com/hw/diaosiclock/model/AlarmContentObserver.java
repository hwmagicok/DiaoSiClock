package com.hw.diaosiclock.model;

import android.database.ContentObserver;
import android.os.Handler;
import android.widget.Adapter;

/**
 * Created by hw on 2016/6/23.
 */
public class AlarmContentObserver extends ContentObserver {

    private Handler mhandler = null;
    private AlarmAdapter madapter = null;

    public AlarmContentObserver(Handler handler, AlarmAdapter adapter) {
        super(handler);
        mhandler = handler;
        madapter = adapter;
    }

    @Override
    public void onChange(boolean selfChange) {
        madapter.notifyDataSetChanged();
    }
}
