package com.auscultator.app;

import java.util.Map;

import com.auscultator.audio.AudioPlayer;
import com.auscultator.audio.AudioRecorder;
import com.auscultator.data.DataAdapter;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MedicalRecord extends Activity {

	// views
	private TextView medical_record_name, medical_record_gender,
			medical_record_age, medical_record_tel;
	private ListView list_breath_sound_record_time;
	private ListView list_heart_sound_record_time;

	// person info
	private Integer userid;
	private String name;
	private Integer gender;
	private Integer age;
	private String tel;
	// records
	private DataAdapter dataAdapter;
	private SimpleCursorAdapter heart_sound_adapter, breath_sound_adapter;
	// audio player
	private AudioPlayer audioPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_medical_record);
		// get parameters
		Intent intent = getIntent();
		userid = intent.getIntExtra("userid", -1);
		if (userid > 0) {
			name = intent.getStringExtra("name");
			gender = intent.getIntExtra("gender", 0);
			age = intent.getIntExtra("age", 0);
			tel = intent.getStringExtra("tel");
		} else {
			MedicalRecord.this.finish();
		}

		// Get the views;
		medical_record_name = (TextView) findViewById(R.id.medical_record_name);
		medical_record_gender = (TextView) findViewById(R.id.medical_record_gender);
		medical_record_age = (TextView) findViewById(R.id.medical_record_age);
		medical_record_tel = (TextView) findViewById(R.id.medical_record_tel);
		list_breath_sound_record_time = (ListView) findViewById(R.id.list_breath_sound_record_time);
		list_heart_sound_record_time = (ListView) findViewById(R.id.list_heart_sound_record_time);

		// Show the person info
		String tmp;
		medical_record_name.setText(name);
		tmp = gender == 0 ? "女" : "男";
		medical_record_gender.setText(tmp);
		tmp = String.valueOf(age) + "岁";
		medical_record_age.setText(tmp);
		medical_record_tel.setText(tel);

		// get records
		SQLiteDatabase db = openOrCreateDatabase("auscultation.db",
				Context.MODE_PRIVATE, null);
		dataAdapter = DataAdapter.getInstance(db);
		Cursor heart_sounds = dataAdapter.get_heart_sounds(userid);
		Cursor breath_sounds = dataAdapter.get_breath_sounds(userid);
		
		// Show the List
		breath_sound_adapter = new SimpleCursorAdapter(getApplicationContext(),
				R.layout.item_breath_record_time, breath_sounds,
				new String[] { "time" }, new int[] { R.id.breath_record_time },
				SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		list_breath_sound_record_time.setAdapter(breath_sound_adapter);

		heart_sound_adapter = new SimpleCursorAdapter(getApplicationContext(),
				R.layout.item_heart_record_time, heart_sounds,
				new String[] { "time" }, new int[] { R.id.heart_record_time },
				SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		list_heart_sound_record_time.setAdapter(heart_sound_adapter);
		
		// audio player
		audioPlayer = new AudioPlayer();

		// set listeners
		list_heart_sound_record_time
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Cursor cursor = (Cursor) parent.getAdapter().getItem(
								position);
						if (cursor.moveToFirst() != false) {
							String sound_file = cursor.getString(cursor
									.getColumnIndex("sound_file"));
							if (sound_file != null) {
								audioPlayer.play(sound_file, null);
							}
						}
					}
				});
		list_breath_sound_record_time
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Cursor cursor = (Cursor) parent.getAdapter().getItem(
								position);
						if (cursor.moveToFirst() != false) {
							String sound_file = cursor.getString(cursor
									.getColumnIndex("sound_file"));
							if (sound_file != null) {
								audioPlayer.play(sound_file, null);
							} else {

							}
						}
					}
				});
	}
}
