package com.auscultator.app;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.auscultator.audio.AudioPlayer;
import com.auscultator.data.AudioTag;
import com.auscultator.data.DataAdapter;
import com.auscultator.data.SAX_AudioTagService;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class BreathSounds extends Activity {
	private static String TAG = "BREATH_SOUNDS";

	private ListView listView;
    private AudioTag breathSoundsLib;

    private AudioPlayer audioPlayer;
    private int playingSoundID = -1;

    private AssetManager assets;
    private AssetFileDescriptor fd;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_breath_sounds);

		listView = (ListView) findViewById(R.id.listView);

        audioPlayer = AudioPlayer.getInstance();

        assets = getApplicationContext().getAssets();

        SAX_AudioTagService audioLibService = SAX_AudioTagService.getInstance();
        breathSoundsLib = audioLibService.getBreathSounds();
        // show the list view
        refreshListView();

		// add listener
		this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long rowid) {
                HashMap<String, Object> item = (HashMap<String, Object>) parent.getAdapter().getItem(position);
                Integer imgType = (Integer)item.get("img");
                if (imgType == R.drawable.sound) {
                    String soundPath = (String)item.get("sound_path");
                    try {
                        fd = assets.openFd(soundPath);
                        if (playingSoundID != position) {
                            audioPlayer.play(fd, null);
                            playingSoundID = position;
                        } else {
                            audioPlayer.playOrPause();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (imgType == R.drawable.sound_folder) {
                    // directory

                    String dirPath = (String) item.get("sound_name");
                    List<AudioTag> children = breathSoundsLib.getChildren();
                    AudioTag child;
                    for (int i = 0; i < children.size(); ++i) {
                        child = children.get(i);
                        if (child.getName().equals(dirPath)) {
                            breathSoundsLib = child;
                            break;
                        }
                    }
                    refreshListView();
                } else {
                    breathSoundsLib = breathSoundsLib.getParent();
                    refreshListView();
                }
			}
		});
	}

    @Override
    protected void onStop() {
        resetPlayer();
        super.onStop();
    }

    private List<Map<String, Object>> getDataList(AudioTag breathSoundsLib) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        List<AudioTag> breathSoundsList = breathSoundsLib.getChildren();
        Map<String, Object> data;
        AudioTag audioTag;
        String audioPath;

        // Add an item used to go to previous page
        if (breathSoundsLib.getParent() != null) {
            data = new HashMap<String, Object>();
            data.put("name", "上一层");
            data.put("img", R.drawable.blue_arrow_up);
            list.add(data);
        }
        // Add the items in
        for (int i = 0; i < breathSoundsList.size(); ++i) {
            data = new HashMap<String, Object>();
            audioTag = breathSoundsList.get(i);

            data.put("name", audioTag.getLabel());

            if (audioTag.isDir()) {
                data.put("img", R.drawable.sound_folder);
            } else {
                data.put("img", R.drawable.sound);
            }

            audioPath = audioTag.getPath();
            // audioPath = audioPath.substring(0, audioPath.length() - 4);
            data.put("sound_path", audioPath);

            data.put("sound_name", audioTag.getName());

            list.add(data);
        }

        return list;
    }

    private void refreshListView() {
        SimpleAdapter adapter = new SimpleAdapter(this, getDataList(breathSoundsLib),
                R.layout.item_sound_record, new String[] { "name", "gender",
                "age", "time", "img" }, new int[] {
                R.id.sound_record_name, R.id.sound_record_gender,
                R.id.sound_record_age, R.id.sound_record_time,
                R.id.sound_record_img });

        listView.setAdapter(adapter);
    }


    private void resetPlayer() {
        this.audioPlayer.reset();
        this.playingSoundID = -1;
    }

}
