package com.hw.diaosiclock.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.hw.diaosiclock.R;

/**
 * Created by hw on 2016/8/5.
 */
public class WelcomeActivity extends Activity {
    private int count = 3;
    private Button jumpToScheduleActivity;
    private Intent intent = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_welcome);

        jumpToScheduleActivity = (Button)findViewById(R.id.jump2schedule);
        jumpToScheduleActivity.getBackground().setAlpha(100);
        jumpToScheduleActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(WelcomeActivity.this, ScheduleActivity.class);
                startActivity(intent);
                handler.removeMessages(count);
                finish();
            }
        });
        handler.sendEmptyMessageDelayed(count, 1000);

    }
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if(0 == msg.what) {
                if(null == intent) {
                    intent = new Intent(WelcomeActivity.this, ScheduleActivity.class);
                }
                startActivity(intent);
                finish();
            }else {
                jumpToScheduleActivity.setText(String.valueOf(count) + " 跳过");
                count--;
                this.sendEmptyMessageDelayed(count, 1000);
            }
        }
    };
}
