package com.auscultator.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.auscultator.audio.AudioRecorder;
import com.auscultator.data.DataAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 电子病历页面，展示电子病历。
 * 1. 新建电子病历入口
 * 2. 电子病历列表
 *
 * @author hongyan
 */
public class MedicalRecords extends Activity {
    /* The state of this Activity */
    final static private int TARGET_SAVE_RECORD = 0x01;
    final static private int TARGET_VIEW_RECORD = 0x02;

    private int target;
    // The components in the view;
    protected TextView new_medical_record;
    protected TableLayout new_medical_record_form;
    protected Button new_medical_record_cancel;
    protected Button new_medical_record_save;
    protected ListView medical_records_list;
    protected EditText new_medical_record_name;
    protected RadioGroup new_medical_record_gender;
    protected EditText new_medical_record_age;
    protected EditText new_medical_record_tel;

    // References
    protected DataAdapter dataAdapter;
    // The type of sound to save;
    protected int sound_type;
    // The audio file of sound
    protected String sound_file;
    // The data of the medical records
    protected List<Map<String, Object>> medical_records;
    protected MedicalRecordAdapter medical_records_adpter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_records);

        // Get the parameter of this activity.
        this.getParameter();

        // Get the view's elements;
        this.getElements();

		/* Get the medical records from database; */
        dataAdapter = DataAdapter.getInstance();
        medical_records = dataAdapter.get_medical_records();
        this.medical_records_adpter = new MedicalRecordAdapter(this,
                medical_records, R.layout.item_medical_record, null, null);
        medical_records_list.setAdapter(medical_records_adpter);

        /* 新建电子病历按钮按下 */
        new_medical_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (target == TARGET_VIEW_RECORD) {
                    Intent intent = new Intent()
                            .putExtra("target", ActivityEditRecord.TARGET_NEW);
                    intent.setClass(MedicalRecords.this, ActivityEditRecord.class);
                    MedicalRecords.this.startActivity(intent);

                } else {
                    if (new_medical_record_form.getVisibility() == View.VISIBLE) {
                        new_medical_record_form.setVisibility(View.GONE);
                    } else {
                        new_medical_record_form.setVisibility(View.VISIBLE);
                    }

                }
            }
        });
        /* 保存电子病历按钮按下 */
        new_medical_record_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // check the validation of the input.
                String name = new_medical_record_name.getText().toString();
                int gender = new_medical_record_gender
                        .getCheckedRadioButtonId();
                String str_age = new_medical_record_age.getText().toString();
                String tel = new_medical_record_tel.getText().toString();
                Integer age;

                if (name.length() < 1) {
                    Toast.makeText(getApplicationContext(),
                            R.string.hint_lack_name, Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (gender == R.id.male) {
                    gender = 1;
                } else if (gender == R.id.female) {
                    gender = 0;
                } else {
                    Toast.makeText(getApplicationContext(),
                            R.string.hint_lack_gender, Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                try {
                    age = Integer.parseInt(str_age);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                Map<String, Object> record = new HashMap<String, Object>();
                record.put("name", name);
                record.put("gender", gender);
                record.put("age", age);
                record.put("tel", tel);

                // If save a record with sound
                if (sound_type != 0 && sound_file.length() > 0) {
                    record.put("type", sound_type);
                    record.put("sound_file", sound_file);
                    dataAdapter.create_person_record(record);
                } // if create an empty medical record
                else {
                    dataAdapter.create_person(record);
                }

                // close the soft input
                View currentFocus = MedicalRecords.this.getCurrentFocus();
                android.os.IBinder iBinder = null;
                if (null != currentFocus) {
                    iBinder = currentFocus.getWindowToken();
                }
                if (null != iBinder) {
                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(iBinder, InputMethodManager.HIDE_NOT_ALWAYS);
                }

                close_new_medical_record_form();
                MedicalRecords.this.finish();
            }
        });
        /* 取消保存电子病历 */
        new_medical_record_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                close_new_medical_record_form();
            }
        });

        /**
         * The listener for List Item Click
         */
        medical_records_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View itemClicked, int position, long id) {
                Map<String, Object> medical_record = medical_records
                        .get((int) id);
                // 打开一个电子病历
                if (sound_type == 0) {
                    Intent intent = new Intent()
                            .putExtra("userid", (Integer) medical_record.get("userid"))
                            .putExtra("name", (String) medical_record.get("name"))
                            .putExtra("gender", (Integer) medical_record.get("gender"))
                            .putExtra("age", (Integer) medical_record.get("age"))
                            .putExtra("tel", (String) medical_record.get("tel"));

                    intent.setClass(MedicalRecords.this, MedicalRecord.class);
                    MedicalRecords.this.startActivity(intent);
                } // 保存一个电子病历
                else {
                    Integer userid = medical_record
                            .containsKey("userid") ? (Integer) medical_record
                            .get("userid") : null;
                    if (userid != null && sound_file != null
                            && sound_file.length() > 0) {

                        Map<String, Object> record = new HashMap<String, Object>();
                        record.put("type", sound_type);
                        record.put("sound_file", sound_file);
                        dataAdapter.create_record(userid, record);

                        MedicalRecords.this.finish();
                    }

                }
            }
        });

        // 长按条目删除
        medical_records_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                final Map<String, Object> medical_record = medical_records
                        .get((int) id);
                Dialog dlg = new AlertDialog.Builder(MedicalRecords.this)
                        .setTitle(R.string.confirm_delete)
                        .setMessage(R.string.confirm_delete_person)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // confirm delete this person
                                dataAdapter.deletePerson((Integer) medical_record.get("userid"));
                                updateMedicalRecords();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // do nothing
                            }
                        }).create();
                dlg.show();

                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMedicalRecords();
    }
    /**
     * Notify the list view that the data of medical records have been changed.
     */
    protected void updateMedicalRecords() {
        medical_records = dataAdapter.get_medical_records();
        this.medical_records_adpter = new MedicalRecordAdapter(this,
                medical_records, R.layout.item_medical_record, null, null);
        medical_records_list.setAdapter(medical_records_adpter);
        medical_records_adpter.notifyDataSetChanged();
    }

    /**
     * Get parameters of this Activity
     */
    private void getParameter() {
        // Get the parameter
        Intent intent = getIntent();
        int type = intent.getIntExtra("sound_type", 0);
        if (type != 0) {
            this.target = TARGET_SAVE_RECORD;
            // TODO: magic numbers
            if (type == AudioRecorder.BREATH_SOUNDS) {
                this.sound_type = 2;
            } else if (type == AudioRecorder.HEART_SOUNDS) {
                this.sound_type = 8;
            } else {
                this.sound_type = type;
            }
            this.sound_file = intent.getStringExtra("sound_file");
            if (null == this.sound_file) {
                MedicalRecords.this.finish();
            }
        } else {
            this.target = TARGET_VIEW_RECORD;
        }
    }

    private void getElements() {
        new_medical_record = (TextView) findViewById(R.id.new_medical_record);
        new_medical_record_form = (TableLayout) findViewById(R.id.new_medical_record_form);
        new_medical_record_cancel = (Button) findViewById(R.id.new_medical_record_cancel);
        new_medical_record_save = (Button) findViewById(R.id.new_medical_record_save);
        medical_records_list = (ListView) findViewById(R.id.medical_records_list);
        new_medical_record_name = (EditText) findViewById(R.id.new_medical_record_name);
        new_medical_record_gender = (RadioGroup) findViewById(R.id.new_medical_record_gender);
        new_medical_record_age = (EditText) findViewById(R.id.new_medical_record_age);
        new_medical_record_tel = (EditText) findViewById(R.id.new_medical_record_tel);
    }

    /**
     * Close the form of creating new medical record.
     */
    protected void close_new_medical_record_form() {
        new_medical_record_name.setText("");
        new_medical_record_gender.clearCheck();
        new_medical_record_tel.setText("");
        new_medical_record_age.setText("");
        new_medical_record_form.setVisibility(View.GONE);
    }
}
