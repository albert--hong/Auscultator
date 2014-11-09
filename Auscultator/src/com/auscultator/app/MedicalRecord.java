package com.auscultator.app;


import com.auscultator.audio.AudioPlayer;
import com.auscultator.data.DataAdapter;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * 电子病历展示页面，展示一个完整的病历。
 *
 * 1. 展示病人个人信息
 * 2. 展示心音列表
 * 3. 展示呼吸音列表
 */
public class MedicalRecord extends Activity {

	// views
	private TextView medical_record_name, medical_record_gender,
			medical_record_age, medical_record_tel;
    private TextView more_person_info;
    private TextView btn_edit_record;
	private ListView list_breath_sound_record_time;
	private ListView list_heart_sound_record_time;

    private TableRow row_origin,
            row_married,
            row_diagnosis_time,
            row_admission_time,
            row_chief_complaint,
            row_HPI,
            row_past_history,
            row_personal_history;
    private TextView medical_record_origin,
            medical_record_married,
            medical_record_diagnosis_time,
            medical_record_admission_time,
            medical_record_chief_complaint,
            medical_record_HPI,
            medical_record_past_history,
            medical_record_personal_history;

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

    // person info 's state
    final static private int SIMPLE_PERSON_INFO = 0x01;
    final static private int VERBOSE_PERSON_INFO = 0x02;

    private int statePersonInfo = SIMPLE_PERSON_INFO;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_medical_record);

        this.getParameters();

		// Get the views;
        this.getElements();

		// Show the person info
        this.showPersonInfo();

        // Show the record sounds
        this.showSoundsList();

        // Add the listeners
        more_person_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (statePersonInfo == SIMPLE_PERSON_INFO) {
                    showMorePersonInfo();
                    statePersonInfo = VERBOSE_PERSON_INFO;
                    more_person_info.setText(R.string.less_person_info);
                } else if (statePersonInfo == VERBOSE_PERSON_INFO) {
                    showLessPersonInfo();
                    statePersonInfo = SIMPLE_PERSON_INFO;
                    more_person_info.setText(R.string.more_person_info);
                }
            }
        });
        btn_edit_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent()
                        .putExtra("target", ActivityEditRecord.TARGET_EDIT)
                        .putExtra("userid", userid);
                intent.setClass(MedicalRecord.this, ActivityEditRecord.class);
                MedicalRecord.this.startActivity(intent);
                MedicalRecord.this.finish();
            }
        });
	}

    private void showLessPersonInfo() {
        row_origin.setVisibility(View.GONE);
        row_married.setVisibility(View.GONE);
        row_diagnosis_time.setVisibility(View.GONE);
        row_admission_time.setVisibility(View.GONE);
        row_chief_complaint.setVisibility(View.GONE);
        row_HPI.setVisibility(View.GONE);
        row_past_history.setVisibility(View.GONE);
        row_personal_history.setVisibility(View.GONE);
    }
    /**
     * Show more info of this person.
     */
    private void showMorePersonInfo() {
        Cursor person_info = dataAdapter.getPersonInfo(userid);
        person_info.moveToFirst();
        // origin
        String origin = person_info.getString(person_info.getColumnIndex("origin"));
        if (origin != null && origin.length() > 0) {
            medical_record_origin.setText(origin);
            row_origin.setVisibility(View.VISIBLE);
        }
        // married
        int married = person_info.getInt(person_info.getColumnIndex("married"));
        if (married == 0 || married == 1) {
            medical_record_married.setText(married == 1 ? R.string.has_married : R.string.unmarried);
            medical_record_married.setVisibility(View.VISIBLE);
        }
        // diagnosis_time
        String diagnosis_time = person_info.getString(person_info.getColumnIndex("diagnosis_time"));
        if (diagnosis_time != null && diagnosis_time.length() > 0) {
            medical_record_diagnosis_time.setText(diagnosis_time);
            row_diagnosis_time.setVisibility(View.VISIBLE);
        }
        // admission_time
        String admission_time = person_info.getString(person_info.getColumnIndex("admission_time"));
        if (admission_time != null && admission_time.length() > 0) {
            medical_record_admission_time.setText(admission_time);
            row_admission_time.setVisibility(View.VISIBLE);
        }
        // chief_complaint
        String chief_complaint = person_info.getString(person_info.getColumnIndex("chief_complaint"));
        if (chief_complaint != null && chief_complaint.length() > 0) {
            medical_record_chief_complaint.setText(chief_complaint);
            row_chief_complaint.setVisibility(View.VISIBLE);
        }
        // HPI
        String HPI = person_info.getString(person_info.getColumnIndex("HPI"));
        if (HPI != null && HPI.length() > 0) {
            medical_record_HPI.setText(HPI);
            row_HPI.setVisibility(View.VISIBLE);
        }
        // past_history
        String past_history = person_info.getString(person_info.getColumnIndex("past_history"));
        if (past_history != null && past_history.length() > 0) {
            medical_record_past_history.setText(past_history);
            row_past_history.setVisibility(View.VISIBLE);
        }
        // personal_history
        String personal_history = person_info.getString(person_info.getColumnIndex("personal_history"));
        if (personal_history != null && personal_history.length() > 0) {
            medical_record_personal_history.setText(personal_history);
            row_personal_history.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Show the sounds list.
     */
    private void showSoundsList() {
        // get records
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

        // set listeners
        list_heart_sound_record_time.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {

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
                }
        );
        list_breath_sound_record_time.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Cursor cursor = (Cursor) parent.getAdapter().getItem(
                                position);
                        if (cursor.moveToFirst()) {
                            String sound_file = cursor.getString(cursor
                                    .getColumnIndex("sound_file"));
                            if (sound_file != null) {
                                audioPlayer.play(sound_file, null);
                            }
                        }
                    }
                }
        );
    }

    /**
     * Show the simple person's info
     */
    private void showPersonInfo() {
        String tmp;
        medical_record_name.setText(name);
        tmp = gender == 0 ? "女" : "男";
        medical_record_gender.setText(tmp);
        tmp = String.valueOf(age) + "岁";
        medical_record_age.setText(tmp);
        medical_record_tel.setText(tel);
    }

    /**
     * Get the elements for this activity
     */
    private void getElements() {
        // get data adapter
        dataAdapter = DataAdapter.getInstance();
        // get audio player
        audioPlayer = AudioPlayer.getInstance();
        // get elements in view;
        medical_record_name = (TextView) findViewById(R.id.medical_record_name);
        medical_record_gender = (TextView) findViewById(R.id.medical_record_gender);
        medical_record_age = (TextView) findViewById(R.id.medical_record_age);
        medical_record_tel = (TextView) findViewById(R.id.medical_record_tel);
        more_person_info = (TextView) findViewById(R.id.more_person_info);
        btn_edit_record = (TextView) findViewById(R.id.btn_edit_record);
        list_breath_sound_record_time = (ListView) findViewById(R.id.list_breath_sound_record_time);
        list_heart_sound_record_time = (ListView) findViewById(R.id.list_heart_sound_record_time);

        row_origin = (TableRow) findViewById(R.id.row_origin);
        row_married = (TableRow) findViewById(R.id.row_married);
        row_diagnosis_time = (TableRow) findViewById(R.id.row_diagnosis_time);
        row_admission_time = (TableRow) findViewById(R.id.row_admission_time);
        row_chief_complaint = (TableRow) findViewById(R.id.row_chief_complaint);
        row_HPI = (TableRow) findViewById(R.id.row_HPI);
        row_past_history = (TableRow) findViewById(R.id.row_past_history);
        row_personal_history = (TableRow) findViewById(R.id.row_personal_history);

        medical_record_origin = (TextView) findViewById(R.id.medical_record_origin);
        medical_record_married = (TextView) findViewById(R.id.medical_record_married);
        medical_record_diagnosis_time = (TextView) findViewById(R.id.medical_record_diagnosis_time);
        medical_record_admission_time = (TextView) findViewById(R.id.medical_record_admission_time);
        medical_record_chief_complaint = (TextView) findViewById(R.id.medical_record_chief_complaint);
        medical_record_HPI = (TextView) findViewById(R.id.medical_record_HPI);
        medical_record_past_history = (TextView) findViewById(R.id.medical_record_past_history);
        medical_record_personal_history = (TextView) findViewById(R.id.medical_record_personal_history);

    }

    /**
     * Get the parameters for this activity
     */
    private void getParameters() {
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
    }
}
