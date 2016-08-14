package com.hw.diaosiclock.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioButton;

import com.hw.diaosiclock.R;
import com.hw.diaosiclock.model.AlarmMusicAdapter;
import com.hw.diaosiclock.util.AlarmMusicUtil;
import com.hw.diaosiclock.util.LocalUtil;

import java.util.ArrayList;

/**
 * Created by hw on 2016/7/9.
 */
public class SelectAlarmMusicActivity extends AppCompatActivity {
    public static final String ERRTAG = "SelectMusicActivity";
    public static final String path = "Alarm";
    private static final String[] keywords = {".mp3", ".wma", ".ogg"};

    private ArrayList<String> MusicList = null;
    private AlarmMusicAdapter listAdapter = null;
    private ListView musicListView = null;

    private MediaPlayer mediaPlayer = null;

    private RadioButton SelectDot;
    private String SelectedMusic = null;
    private static int DotPosition = 0;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musicset);

        mediaPlayer = new MediaPlayer();
        musicListView = (ListView)findViewById(R.id.list_music);
        MusicList = new ArrayList<>();
        listAdapter = new AlarmMusicAdapter(this, R.layout.alarm_music, MusicList);
        musicListView.setAdapter(listAdapter);

        AlarmMusicUtil.SearchAssetsMusic(this, MusicList, LocalUtil.AlarmMusicPath, keywords);

        /* start：此处是为了在修改闹钟的时候，打开Alarm Music列表能正确显示出上一次的选择 */
        final Intent intent = getIntent();
        if(null == intent) {
            Log.e(ERRTAG, "intent is null");
            finish();
        } else {
            String preAlarmMusicName = intent.getStringExtra(LocalUtil.TAG_SET_MUSIC);
            if(null != preAlarmMusicName) {
                DotPosition = MusicList.indexOf(preAlarmMusicName);
                listAdapter.getView(DotPosition, null, null);
            }
        }
        /* end */

        listAdapter.notifyDataSetChanged();
        musicListView.setSelection(DotPosition);

        musicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                DotPosition = position;
                SelectDot = (RadioButton)view.findViewById(R.id.music_select);
                listAdapter.notifyDataSetChanged();
                SelectDot.setChecked(true);

                String musicName = MusicList.get(position);

                if(null == musicName) {
                    Log.e(ERRTAG, "music name is null");
                    return;
                }

                String[] nameSegments = musicName.split("\\.");
                if(0 >= nameSegments.length) {
                    Log.e(ERRTAG, "music name[" + musicName + "] is illegal");
                    return;
                }

                //mediaPlayer = new MediaPlayer();
                if(mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
                for(String keyword : keywords) {
                    if(AlarmMusicUtil.CompareMusicExtension(musicName, keyword)) {
                        SelectedMusic = musicName;
                        LocalUtil.playAlarmMusic(mediaPlayer, SelectAlarmMusicActivity.this, SelectedMusic, false);
                        break;
                    }
                }

            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.musicset_view_toolbar);
        toolbar.setTitle("闹钟铃声");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // 返回键点击逻辑
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null != mediaPlayer) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }

                if(null != SelectedMusic) {
                    Intent intent = new Intent();
                    intent.putExtra("return_musicname", SelectedMusic);
                    setResult(RESULT_OK, intent);
                }
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(null != mediaPlayer) {
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        }

        if(null != SelectedMusic) {
            Intent intent = new Intent();
            intent.putExtra("return_musicname", SelectedMusic);
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    public static int getDotLocation() {
        return DotPosition;
    }



}
