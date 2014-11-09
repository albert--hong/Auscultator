package com.auscultator.app;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.auscultator.data.DataAdapter;

/**
 * The Activity to edit the Medical Record.
 * Created by hongyan on 2014/11/9.
 */
public class ActivityEditRecord extends Activity {
    final static protected int TARGET_NEW = 0x01;
    final static protected int TARGET_EDIT = 0x02;

    /* The target of this activity */
    private int target;

    private int userid;

    /* The date adapter for SQLite */
    private DataAdapter dataAdapter;

    /* The elements of this activity */
    private EditText new_record_name;
    private RadioGroup new_record_gender;
    private EditText new_record_age;
    private EditText new_record_tel;
    private EditText new_record_origin;
    private RadioGroup new_record_married;
    private TextView new_record_diagnosis_date;
    private TextView new_record_admission_date;
    private EditText new_record_chief_complaint;
    private EditText new_record_HPI;
    private EditText new_record_past_history;
    private EditText new_record_personal_history;
    private Button new_record_cancel;
    private Button new_record_save;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_record);

        // get the parameters of this activity
        this.getParameter();

        // get the data adapter
        dataAdapter = DataAdapter.getInstance();
        // get the elements of this activity
        this.getElements();

        if (target == TARGET_NEW) {
            this.initDatePicker();
        } else if (target == TARGET_EDIT) {
            this.initElements();
        }


        new_record_diagnosis_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(ActivityEditRecord.this,new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        new_record_diagnosis_date.setText(String.format("%04d-%02d-%02d", year, month + 1, day));
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        new_record_admission_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(ActivityEditRecord.this,new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        new_record_admission_date.setText(String.format("%04d-%02d-%02d", year, month + 1, day));
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // when the save button clicked, save the medical record
        new_record_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (saveRecord()) {
                    ActivityEditRecord.this.finish();
                }
            }
        });

        // when the cancel button clicked, close the
        new_record_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityEditRecord.this.finish();
            }
        });
    }

    protected void onResume() {
        super.onResume();
    }

    private void initElements() {
        Cursor personInfo = dataAdapter.getPersonInfo(userid);
        personInfo.moveToFirst();
        if (personInfo.getCount() < 1) {
            this.finish();
        }
        // name
        String name = personInfo.getString(personInfo.getColumnIndex("name"));
        new_record_name.setText(name);
        // gender
        int gender = personInfo.getInt(personInfo.getColumnIndex("gender"));
        new_record_gender.check(gender == 0 ? R.id.female : R.id.male);
        // age
        new_record_age.setText(personInfo.getString(personInfo.getColumnIndex("age")));
        // tel
        new_record_tel.setText(personInfo.getString(personInfo.getColumnIndex("tel")));
        // origin
        new_record_origin.setText(personInfo.getString(personInfo.getColumnIndex("origin")));
        // married
        int married = personInfo.getInt(personInfo.getColumnIndex("married"));
        new_record_married.check(married == 1 ? R.id.married: R.id.unmarried);
        // diagnosis_time
        String diagnosis_time = personInfo.getString(personInfo.getColumnIndex("diagnosis_time"));
        new_record_diagnosis_date.setText(diagnosis_time);
        // admission_time
        String admission_time = personInfo.getString(personInfo.getColumnIndex("admission_time"));
        new_record_admission_date.setText(admission_time);
        // chief_complaint
        new_record_chief_complaint.setText(personInfo.getString(personInfo.getColumnIndex("chief_complaint")));
        // HPI
        new_record_HPI.setText(personInfo.getString(personInfo.getColumnIndex("HPI")));
        // past_history
        new_record_past_history.setText(personInfo.getString(personInfo.getColumnIndex("past_history")));
        // personal_history
        new_record_personal_history.setText(personInfo.getString(personInfo.getColumnIndex("personal_history")));
    }

    @Override
    protected void onStop() {
        new_record_name.setText("");
        new_record_age.setText("");
        new_record_tel.setText("");
        new_record_origin.setText("");
        new_record_chief_complaint.setText("");
        new_record_HPI.setText("");
        new_record_past_history.setText("");
        new_record_personal_history.setText("");

        super.onStop();
    }

    /**
     * Get parameters of this Activity
     */
    private void getParameter() {
        Intent intent = getIntent();
        this.target = intent.getIntExtra("target", 0);
        if (target == TARGET_EDIT) {
            this.userid = intent.getIntExtra("userid", -1);
        } else {
            this.userid = -1;
        }
    }

    /**
     * Get elements of this Activity
     */
    private void getElements() {
        this.new_record_name = (EditText) findViewById(R.id.new_record_name);
        this.new_record_gender = (RadioGroup) findViewById(R.id.new_record_gender);
        this.new_record_age = (EditText) findViewById(R.id.new_record_age);
        this.new_record_tel = (EditText) findViewById(R.id.new_record_tel);
        this.new_record_origin = (EditText) findViewById(R.id.new_record_origin);
        this.new_record_married = (RadioGroup) findViewById(R.id.new_record_married);
        this.new_record_diagnosis_date = (TextView) findViewById(R.id.new_record_diagnosis_date);
        this.new_record_admission_date = (TextView) findViewById(R.id.new_record_admission_date);
        this.new_record_chief_complaint = (EditText) findViewById(R.id.new_record_chief_complaint);
        this.new_record_HPI = (EditText) findViewById(R.id.new_record_HPI);
        this.new_record_past_history = (EditText) findViewById(R.id.new_record_past_history);
        this.new_record_personal_history = (EditText) findViewById(R.id.new_record_personal_history);
        this.new_record_cancel = (Button) findViewById(R.id.new_record_cancel);
        this.new_record_save = (Button) findViewById(R.id.new_record_save);

        this.new_record_chief_complaint.setSingleLine(false);
        this.new_record_HPI.setSingleLine(false);
        this.new_record_past_history.setSingleLine(false);
        this.new_record_personal_history.setSingleLine(false);
    }

    private void initDatePicker() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String new_date = dateFormat.format(date);
        String new_time = timeFormat.format(date);
        new_record_diagnosis_date.setText(new_date);
        new_record_admission_date.setText(new_date);

    }

    private boolean saveRecord() {
        ContentValues initialValues = new ContentValues();
        // name
        String name = new_record_name.getText().toString();
        if (name.length() < 2) {
            Toast.makeText(getApplicationContext(), R.string.hint_lack_name, Toast.LENGTH_SHORT).show();
            new_record_name.requestFocus();
            return false;
        }
        initialValues.put("name", name);
        // gender
        Integer gender = new_record_gender.getCheckedRadioButtonId();
        if (gender == R.id.male) {
            gender = 1;
        } else if (gender == R.id.female) {
            gender = 0;
        } else {
            Toast.makeText(getApplicationContext(), R.string.hint_lack_gender, Toast.LENGTH_SHORT).show();
            return false;
        }
        initialValues.put("gender", gender);
        // age
        String str_age =  new_record_age.getText().toString();
        Integer age = 0;
        try {
            age = Integer.parseInt(str_age);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    R.string.hint_lack_gender, Toast.LENGTH_SHORT)
                    .show();
            new_record_age.requestFocus();
            return false;
        }
        initialValues.put("age", age);
        // tel. TODO: 正则表达式判断是否符合电话号码
        String tel = new_record_tel.getText().toString();
        initialValues.put("tel", tel.length() > 0 ? tel : null);
        // new record origin
        String origin = new_record_origin.getText().toString();
        initialValues.put("origin", origin.length() > 0 ? origin : null);
        // married
        Integer married = new_record_married.getCheckedRadioButtonId();
        if (married == R.id.married) {
            married = 1;
        } else {
            married = 0;
        }
        initialValues.put("married", married);
        // time format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // diagnosis time
        Date diagnosis_time;
        try {
            diagnosis_time = dateFormat.parse(new_record_diagnosis_date.getText().toString() + " 00:00:00");
        } catch (Exception e) {
            diagnosis_time = new Date();
        }
        initialValues.put("diagnosis_time", dateFormat.format(diagnosis_time));
        // admission time
        Date admission_time;
        try {
            admission_time = dateFormat.parse(new_record_admission_date.getText() + " 00:00:00");
        } catch (Exception e) {
            admission_time = new Date();
        }
        initialValues.put("admission_time", dateFormat.format(admission_time));
        // chief complaint
        String chief_complaint = new_record_chief_complaint.getText().toString();
        initialValues.put("chief_complaint", chief_complaint.length() > 0 ? chief_complaint : null);
        // PHI
        String HPI = new_record_HPI.getText().toString();
        initialValues.put("HPI", HPI.length() > 0 ? HPI : null);
        // past history
        String past_history = new_record_past_history.getText().toString();
        initialValues.put("past_history", past_history.length() > 0 ? past_history : null);
        // personal history
        String personal_history = new_record_personal_history.getText().toString();
        initialValues.put("personal_history", personal_history.length() > 0 ? personal_history : null);

        if (target == TARGET_EDIT) {
            initialValues.put("_id", userid);
        }
        dataAdapter.create_person(initialValues);
        return true;
    }
}