package com.hw.diaosiclock.activity;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
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
    private AssetManager assetManager = null;

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

        AlarmMusicUtil.SearchAssetsMusic(this, MusicList, path, keywords);

        /* start：此处是为了在修改闹钟的时候，打开Alarm Music列表能正确显示出上一次的选择 */
        final Intent intent = getIntent();
        if(null == intent) {
            Log.e(ERRTAG, "intent is null");
            finish();
        } else {
            String preAlarmMusicName = intent.getStringExtra(SetAlarmActivity.MUSICTAG);
            if(null != preAlarmMusicName) {
                DotPosition = MusicList.indexOf(preAlarmMusicName);
                listAdapter.getView(DotPosition, null, null);
            }
        }
        /* end */

        listAdapter.notifyDataSetChanged();

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

                for(String keyword : keywords) {
                    if(("." + nameSegments[nameSegments.length - 1]).equals(keyword)) {
                        AssetFileDescriptor AlarmMusicDescriptor;
                        SelectedMusic = musicName;

                        if(mediaPlayer.isPlaying()) {
                            mediaPlayer.reset();
                        }

                        try {
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                            assetManager = getAssets();
                            AlarmMusicDescriptor = assetManager.openFd(path + "/" + musicName);
                            mediaPlayer.setDataSource(AlarmMusicDescriptor.getFileDescriptor(),
                                    AlarmMusicDescriptor.getStartOffset(), AlarmMusicDescriptor.getLength());

                            //这个地妥协了一下，并没有使用该Alarm设置的铃声大小，而是取了一个值
                            mediaPlayer.setVolume(60, 60);
                            AlarmMusicDescriptor.close();
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            break;
                        }catch (Exception e) {
                            Log.e(ERRTAG, "Err on playing alarm music");
                            Log.e(ERRTAG, Log.getStackTraceString(e));
                            return;
                        }

                    }
                }

            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.musicset_view_toolbar);
        toolbar.setTitle("闹钟铃声");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {
        mediaPlayer.stop();
        mediaPlayer.release();
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
