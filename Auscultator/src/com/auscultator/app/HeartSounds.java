package com.auscultator.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class HeartSounds extends Activity {
	private static String TAG = "HEART_SOUNDS";

	private ListView listView;
    private AudioTag heartSoundsLib;

    private AudioPlayer audioPlayer;

    private AssetManager assets;
    private AssetFileDescriptor fd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_heart_sounds);

        assets = getApplicationContext().getAssets();

        listView = (ListView) findViewById(R.id.listView);
        audioPlayer = AudioPlayer.getInstance();

        SAX_AudioTagService audioLibService = SAX_AudioTagService.getInstance();
        heartSoundsLib = audioLibService.getHeartSounds();
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
                        audioPlayer.play(fd, null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (imgType == R.drawable.sound_folder) {
                    // directory
                    String dirPath = (String) item.get("sound_name");
                    List<AudioTag> children = heartSoundsLib.getChildren();
                    AudioTag child;
                    for (int i = 0; i < children.size(); ++i) {
                        child = children.get(i);
                        if (child.getName().equals(dirPath)) {
                            heartSoundsLib = child;
                            break;
                        }
                    }
                    refreshListView();
                } else {
                    heartSoundsLib = heartSoundsLib.getParent();
                    refreshListView();
                }
			}

		});
	}

    private void refreshListView() {
        SimpleAdapter adapter = new SimpleAdapter(this, getDataList(heartSoundsLib),
                R.layout.item_sound_record, new String[] { "name", "gender",
                "age", "time", "img" }, new int[] {
                R.id.sound_record_name, R.id.sound_record_gender,
                R.id.sound_record_age, R.id.sound_record_time,
                R.id.sound_record_img });

        listView.setAdapter(adapter);
    }

    private List<Map<String,Object>> getDataList(AudioTag heartSoundsLib) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        List<AudioTag> heartSoundsList = heartSoundsLib.getChildren();
        Map<String, Object> data;
        AudioTag audioTag;

        // Add an item used to go to previous page
        if (heartSoundsLib.getParent() != null) {
            data = new HashMap<String, Object>();
            data.put("name", "上一层");
            data.put("img", R.drawable.blue_arrow_up);
            list.add(data);
        }
        // Add the items in
        for (int i = 0; i < heartSoundsList.size(); ++i) {
            data = new HashMap<String, Object>();
            audioTag = heartSoundsList.get(i);

            data.put("name", audioTag.getLabel());

            if (audioTag.isDir()) {
                data.put("img", R.drawable.sound_folder);
            } else {
                data.put("img", R.drawable.sound);
            }

            data.put("sound_path", audioTag.getPath());
            data.put("sound_name", audioTag.getName());

            list.add(data);
        }

        return list;
    }

    @Deprecated
    private List<Map<String, Object>> get_data_list(Cursor cursor) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			Map<String, Object> data = new HashMap<String, Object>();
			String tmp;
			data.put("name", cursor.getString(cursor.getColumnIndex("name")));
			tmp = ((int)cursor.getInt(cursor.getColumnIndex("gender"))==0) ? "女" : "男" ;
			data.put("gender", tmp);
			tmp = ((Integer)cursor.getInt(cursor.getColumnIndex("age"))).toString();
			data.put("age", tmp);
			data.put("time", cursor.getString(cursor.getColumnIndex("time")));
			if (cursor.getPosition() % 2 == 1) {
				data.put("img", R.drawable.sound_folder);
			} else {
				data.put("img", R.drawable.sound);
			}
			
			data.put("sound_file", cursor.getString(cursor.getColumnIndex("sound_file")));
			data.put("sound_path", cursor.getString(cursor.getColumnIndex("sound_path")));
			
			list.add(data);
		}
		return list;
	}

}
