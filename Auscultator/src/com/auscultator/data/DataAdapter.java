package com.auscultator.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * The data adapter of the application.
 * @author hongyan
 */
public class DataAdapter {
    private final static String TAG = "DataAdapter";

    /**
     * The the instance for singleton model
     */
    private static DataAdapter mInstance;

    private SQLiteDatabase db;

    private DataAdapter() {
        this.db = null;
    }
    public static DataAdapter getInstance() {
        if (mInstance == null) {
            mInstance = new DataAdapter();
        }
        return mInstance;
    }

    /**
     * Initialize the database for data adapter.
     * @param database
     */
    public void initialize(SQLiteDatabase database) {
        this.db = database;
        this.create_db();
    }
    /**
     * Create the tables for database.
     *
     * @return error_code
     */
    private int create_db() {
        // create person table 创建个人信息表
        String sql_create_person_table = "CREATE TABLE IF NOT EXISTS person(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "gender SMALLINT, " +
                "age SMALLINT, " +
                "tel TEXT," +
                "origin TEXT," +
                "married SMALLINT," +
                "diagnosis_time DATETIME, " +
                "admission_time DATETIME, " +
                "chief_complaint TEXT," +
                "HPI TEXT, " +
                "past_history TEXT," +
                "personal_history TEXT" +
                ");";

        Log.d(TAG, sql_create_person_table);
        // create records table 创建病历表
        String sql_create_records_table = "CREATE TABLE IF NOT EXISTS medical_records("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "userid INTEGER, "
                + "type SMALLINT, "
                + "time DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "sound_file TEXT,"
                + "sound_path TEXT,"
                + "FOREIGN KEY(userid) REFERENCES person(_id)" + ");";

        Log.d(TAG, sql_create_records_table);

        try {
            this.db.execSQL(sql_create_person_table);
            this.db.execSQL(sql_create_records_table);
        } catch (Exception e) {
            System.out.print(e.getStackTrace());
            return -1;
        }
        return 0;
    }

    /**
     * Create one person info.
     *
     * @param person_info The object describing person
     * @return person_id
     */
    public int create_person(Map<String, Object> person_info) {
        int person_id = -1;
        Object tmpValue;

        if (person_info.containsKey("name")
                && person_info.containsKey("gender")
                && person_info.containsKey("age")
                && person_info.containsKey("tel")) {
            String name = (String) person_info.get("name");
            Integer gender = (Integer) person_info.get("gender");
            Integer age = (Integer) person_info.get("age");
            String tel = (String) person_info.get("tel");
            tmpValue = person_info.get("origin");
            String origin = tmpValue != null ? (String) tmpValue : "";
            tmpValue = person_info.get("married");
            Integer married = tmpValue != null ? ((Integer) tmpValue) : 0;
            tmpValue = person_info.get("diagnosis_time");
            String diagnosisTime = (String) tmpValue;
            tmpValue = person_info.get("admission_time");
            String admissionTime = (String) tmpValue;
            tmpValue = person_info.get("chief_complaint");
            String chiefComplaint = (String) tmpValue;
            tmpValue = person_info.get("HPI");
            String HPI = (String) tmpValue;
            tmpValue = person_info.get("past_history");
            String pastHistory = (String) tmpValue;
            tmpValue = person_info.get("personal_history");
            String personalHistory = (String) tmpValue;

            ContentValues values = new ContentValues();
            values.put("name", name);
            values.put("gender", gender);
            values.put("age", age);
            values.put("tel", tel);
            values.put("origin", origin);
            values.put("married", married);
            values.put("diagnosis_time", diagnosisTime);
            values.put("admission_time", admissionTime);
            values.put("chief_complaint", chiefComplaint);
            values.put("HPI", HPI);
            values.put("past_history", pastHistory);
            values.put("personal_history", personalHistory);

            long rowid = this.db.insert("person", null, values);

            Cursor cursor = this.db.rawQuery(
                    "SELECT _id FROM person WHERE rowid=?;",
                    new String[]{String.valueOf(rowid)});

            if (cursor.moveToFirst()) {
                person_id = cursor.getInt(cursor.getColumnIndex("_id"));
            }
        }
        return person_id;
    }

    /**
     * Create one person info.
     *
     * @param person_values The object describing person
     * @return person_id
     */
    public int create_person(ContentValues person_values) {
        int person_id = -1;

        long rowid = this.db.insertWithOnConflict("person", null, person_values, SQLiteDatabase.CONFLICT_REPLACE);
        Cursor cursor = this.db.rawQuery(
                "SELECT _id FROM person WHERE rowid=?;",
                new String[]{String.valueOf(rowid)});

        if (cursor.moveToFirst()) {
            person_id = cursor.getInt(cursor.getColumnIndex("_id"));
        }

        return person_id;
    }

    /**
     * Delete one person info from person table
     * @param userid The person's userid.
     * @return success
     */
    public boolean deletePerson(int userid) {
        int count = this.db.delete("person", "_id=?", new String[] {Integer.toString(userid)});
        return count > 0;
    }

    /**
     * Create a medical record.
     *
     * @param userid The user's id
     * @param record The object of record
     * @return error_code
     */
    public int create_record(Integer userid, Map<String, Object> record) {
        if (userid > 0 && record.containsKey("type")
                && record.containsKey("sound_file")) {

            Integer type = (Integer) record.get("type");
            String sound_path = (String) record.get("sound_path");
            String sound_file = (String) record.get("sound_file");

            ContentValues values = new ContentValues();
            values.put("userid", userid);
            values.put("type", type);
            values.put("sound_path", sound_path);
            values.put("sound_file", sound_file);

            this.db.insert("medical_records", null, values);
            return 0;
        } else {
            return -1;
        }
    }

    /**
     * Create medical record with the sound.
     *
     * @param record The object of record
     * @return error_code
     */
    public int create_person_record(Map<String, Object> record) {
        if (record.containsKey("name") && record.containsKey("gender")
                && record.containsKey("age") && record.containsKey("tel")
                && record.containsKey("type")
                && record.containsKey("sound_file")) {

            // insert the person info to person table
            Map<String, Object> new_person_info = new HashMap<String, Object>();
            new_person_info.put("name", record.get("name"));
            new_person_info.put("gender", record.get("gender"));
            new_person_info.put("age", record.get("age"));
            new_person_info.put("tel", record.get("tel"));

            Integer userid = this.create_person(new_person_info);

            // insert the record info to record table
            Map<String, Object> new_record_info = new HashMap<String, Object>();
            new_record_info.put("type", record.get("type"));
            new_record_info.put("sound_file", record.get("sound_file"));
            new_record_info.put("sound_path", record.get("sound_path"));

            this.create_record(userid, new_record_info);
        }
        return 0;
    }

    /**
     * Query the medical records list.
     *
     * @return records The list of record object.
     */
    public List<Map<String, Object>> get_medical_records() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        Cursor cursor = this.db.rawQuery(
                "SELECT _id,name,gender,age,tel FROM person ORDER BY name;",
                new String[]{});

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Integer userid = cursor.getInt(cursor.getColumnIndex("_id"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            Integer gender = cursor.getInt(cursor.getColumnIndex("gender"));
            Integer age = cursor.getInt(cursor.getColumnIndex("age"));
            String tel = cursor.getString(cursor.getColumnIndex("tel"));

            Map<String, Object> person = new HashMap<String, Object>();
            person.put("userid", userid);
            person.put("name", name);
            person.put("gender", gender);
            person.put("age", age);
            person.put("tel", tel);

            list.add(person);
        }

        return list;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.db.close();
    }

    /**
     * Get the cursor of heart sounds
     *
     * @param userid The user's id
     * @return cursor
     */
    public Cursor get_heart_sounds(Integer userid) {
        return this.db
                .rawQuery(
                        "SELECT strftime('%Y年%m月%d日 %H:%M', time, 'localtime') time,sound_file,sound_path,_id "
                                + "FROM medical_records WHERE userid=? AND type=8 ORDER BY time DESC;",
                        new String[]{String.valueOf(userid)});
    }

    /**
     * Get the cursor of breath sounds
     *
     * @param userid The user's id
     * @return cursor
     */
    public Cursor get_breath_sounds(Integer userid) {
        return this.db
                .rawQuery(
                        "SELECT strftime('%Y年%m月%d日 %H:%M', time, 'localtime') time,sound_file,sound_path,_id "
                                + "FROM medical_records WHERE userid=? AND type=2 ORDER BY time DESC;",
                        new String[]{String.valueOf(userid)});
    }

    /**
     * Get the cursor of heart sounds
     *
     * @return cursor
     */
    public Cursor get_heart_sounds() {
        return this.db
                .rawQuery(
                        "SELECT "
                                + "person.name name,person.gender gender,person.age age,"
                                + "medical_records.sound_file sound_file,medical_records.sound_path sound_path,"
                                + "strftime('%Y年%m月%d日 %H:%M', medical_records.time) time "
                                + "FROM medical_records LEFT JOIN person "
                                + "WHERE medical_records.userid=person._id and medical_records.type=8 "
                                + "ORDER BY time DESC;", null);
    }

    /**
     * Get the cursor of breath sounds
     *
     * @return cursor
     */
    public Cursor get_breath_sounds() {
        return this.db
                .rawQuery(
                        "SELECT "
                                + "person.name name,person.gender gender,person.age age,"
                                + "medical_records.sound_file sound_file,medical_records.sound_path sound_path,"
                                + "strftime('%Y年%m月%d日 %H:%M', medical_records.time) time "
                                + "FROM medical_records LEFT JOIN person "
                                + "WHERE medical_records.userid=person._id and medical_records.type=2 "
                                + "ORDER BY time DESC;", null);
    }

    /**
     * Get the cursor of specified person.
     * @param userid The person's userid.
     * @return cursor
     */
    public Cursor getPersonInfo(int userid) {
        return this.db.rawQuery(
                "SELECT _id,name,gender,age,tel,origin,married," +
                        "strftime('%Y年%m月%d日', diagnosis_time) diagnosis_time," +
                        "strftime('%Y年%m月%d日', admission_time) admission_time," +
                        "chief_complaint,HPI,past_history,personal_history " +
                        "FROM person WHERE _id=?;",
                new String[]{String.valueOf(userid)});
    }
}
