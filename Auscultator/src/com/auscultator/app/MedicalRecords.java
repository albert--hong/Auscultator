package com.auscultator.app;

import java.util.HashMap;
import java.util.List;

import com.auscultator.data.DataAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MedicalRecords extends Activity {
	// The elements in the view;
	private TextView new_medical_record;
	private TableLayout new_medical_record_form;
	private Button new_medical_record_cancel;
	private Button new_medical_record_save;
	private ListView medical_records_list;
	private EditText new_medical_record_name;
	private RadioGroup new_medical_record_gender;
	private EditText new_medical_record_age;
	private EditText new_medical_record_tel;
	
	// References
	private DataAdapter dataAdapter = DataAdapter.getInstance();
	// The type of sounds to save;
	private int sounds_type;
	// The data of the medical records
	private List<HashMap<String, Object>> medical_records;
	private SimpleAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_records);
        // Get the parameter
        Intent intent = getIntent();
        sounds_type = intent.getIntExtra("sounds_type", 0);
        // Get the view's elements;
        new_medical_record = (TextView)findViewById(R.id.new_medical_record);
        new_medical_record_form = (TableLayout)findViewById(R.id.new_medical_record_form);
        new_medical_record_cancel = (Button)findViewById(R.id.new_medical_record_cancel);
        new_medical_record_save = (Button)findViewById(R.id.new_medical_record_save);
        medical_records_list = (ListView)findViewById(R.id.medical_records_list);
        
        // Get the medical records from database; 
        medical_records = dataAdapter.get_media_records();
        this.adapter = new SimpleAdapter(this, medical_records, R.layout.list_audio_item, 
        		new String[]{"name", "time", "img"},
        		new int[]{R.id.name, R.id.time, R.id.img});
        medical_records_list.setAdapter(adapter);
        
        // Add the listeners to the view
        new_medical_record.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (new_medical_record_form.getVisibility() == View.VISIBLE) {
					new_medical_record_form.setVisibility(View.GONE);
				} else {
					new_medical_record_form.setVisibility(View.VISIBLE);					
				}
			}
		});
        
        new_medical_record_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// check the validation of the input.
				String name = new_medical_record_name.getText().toString();
				int gender = new_medical_record_gender.getCheckedRadioButtonId();
				String age = new_medical_record_age.getText().toString();
				String phone = new_medical_record_tel.getText().toString();
				
				if (name.length() < 1) {
					Toast.makeText(getApplicationContext(), R.string.hint_lack_name, Toast.LENGTH_SHORT);
					return;
				}
				if (gender != R.id.male && gender!=R.id.female) {
					Toast.makeText(getApplicationContext(), R.string.hint_lack_gender, Toast.LENGTH_SHORT);
					return;
				}
				if (age.length() < 1) {
					Toast.makeText(getApplicationContext(), R.string.hint_lack_age, Toast.LENGTH_SHORT);
					return;
				}
				
				dataAdapter.save(new HashMap<String, Object>());
			}
		});
        
        new_medical_record_save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				new_medical_record_name.setText("");
				new_medical_record_gender.clearCheck();
				new_medical_record_tel.setText("");
				new_medical_record_age.setText("");
				new_medical_record_form.setVisibility(View.INVISIBLE);
			}
		});
    }
}
