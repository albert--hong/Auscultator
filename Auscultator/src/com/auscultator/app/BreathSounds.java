package com.auscultator.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.auscultator.audio.AudioPlayer;
import com.auscultator.data.DataAdapter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class BreathSounds extends Activity {
	private static String TAG = "BREATH_SOUNDS";

	private SimpleAdapter adapter;
	private ListView listView;
	private DataAdapter dataAdapter;
	private List<Map<String, Object>> breath_sounds;
	private AudioPlayer audio_player;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_breath_sounds);

		listView = (ListView) findViewById(R.id.listView);

		SQLiteDatabase db = openOrCreateDatabase("auscultation.db",
				Context.MODE_PRIVATE, null);
		dataAdapter = DataAdapter.getInstance(db);
		breath_sounds = get_data_list(dataAdapter.get_breath_sounds());

		this.adapter = new SimpleAdapter(this, breath_sounds,
				R.layout.item_sound_record, new String[] { "name", "gender",
						"age", "time", "img" }, new int[] {
						R.id.sound_record_name, R.id.sound_record_gender,
						R.id.sound_record_age, R.id.sound_record_time,
						R.id.sound_record_img });

		listView.setAdapter(adapter);
		
		// 
		this.audio_player = new AudioPlayer();
		// add listener
		this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long rowid) {
				HashMap<String, Object> item = (HashMap<String, Object>) parent.getAdapter().getItem(position);
				String sound_file = (String)item.get("sound_file");
				audio_player.play(sound_file, null);
			}
			
		});
	}

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
				data.put("img", R.drawable.head_portrait_1);
			} else {
				data.put("img", R.drawable.head_portrait);
			}

			data.put("sound_file", cursor.getString(cursor.getColumnIndex("sound_file")));
			data.put("sound_path", cursor.getString(cursor.getColumnIndex("sound_path")));
			
			list.add(data);
		}
		return list;
	}

}
