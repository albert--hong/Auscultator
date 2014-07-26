package com.auscultator.data;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by HongYan on 2014/6/29.
 */
public class DataAdapter {
	private final static String TAG = "DataAdapter";
	/**
	 * The the instance for singleton model
	 */
	private static DataAdapter mInstance;

	private SQLiteDatabase db;

	private DataAdapter(SQLiteDatabase sqliteDB) {
		this.db = sqliteDB;
		this.create_db();
	}

	public static DataAdapter getInstance(SQLiteDatabase sqliteDB) {
		if (mInstance == null) {
			mInstance = new DataAdapter(sqliteDB);
		}
		return mInstance;
	}

	/**
	 * Create the tables for database.
	 * 
	 * @return
	 */
	private int create_db() {
		// 性别： 男的为1，女的为0；
		// create person table
		String sql_create_person_table = "CREATE TABLE IF NOT EXISTS person("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, " + "name TEXT, "
				+ "gender SMALLINT, " + "age SMALLINT, " + "tel TEXT" + ");";

		Log.d(TAG, sql_create_person_table);
		// 声音类型：breath为2，heart为8
		// create records table
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

	private boolean if_db_exist() {
		return false;
	}

	/**
	 * Create one person info.
	 * 
	 * @param person_info
	 * @return person_id
	 */
	public int create_person(Map<String, Object> person_info) {
		int person_id = -1;
		if (person_info.containsKey("name")
				&& person_info.containsKey("gender")
				&& person_info.containsKey("age")
				&& person_info.containsKey("tel")) {
			String name = (String) person_info.get("name");
			Integer gender = (Integer) person_info.get("gender");
			Integer age = (Integer) person_info.get("age");
			String tel = (String) person_info.get("tel");

			ContentValues values = new ContentValues();
			values.put("name", name);
			values.put("gender", gender);
			values.put("age", age);
			values.put("tel", tel);

			long rowid = this.db.insert("person", null, values);

			Cursor cursor = this.db.rawQuery(
					"SELECT _id FROM person WHERE rowid=?;",
					new String[] { String.valueOf(rowid) });

			if (cursor.moveToFirst() != false) {
				person_id = cursor.getInt(cursor.getColumnIndex("_id"));
			}
		}
		return person_id;
	}

	/**
	 * Create a medical record.
	 * 
	 * @param userid
	 * @param record
	 * @return
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
	 * @param record
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
	 * @return
	 */
	public List<Map<String, Object>> get_medical_records() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		Cursor cursor = this.db.rawQuery(
				"SELECT _id,name,gender,age,tel FROM person ORDER BY name;",
				new String[] {});

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
	 * @param userid
	 * @return cursor
	 */
	public Cursor get_heart_sounds(Integer userid) {
		 return this.db
		 .rawQuery(
		 "SELECT strftime('%Y年%m月%d日 %H:%M', time) time,sound_file,sound_path,_id "
		 +
		 "FROM medical_records WHERE userid=? AND type=8 ORDER BY time DESC;",
		 new String[] { String.valueOf(userid) });
	}

	/**
	 * Get the cursor of breath sounds
	 * 
	 * @param userid
	 * @return
	 */
	public Cursor get_breath_sounds(Integer userid) {
		return this.db
				.rawQuery(
						"SELECT strftime('%Y年%m月%d日 %H:%M', time) time,sound_file,sound_path,_id "
								+ "FROM medical_records WHERE userid=? AND type=2 ORDER BY time DESC;",
						new String[] { String.valueOf(userid) });
	}
}
