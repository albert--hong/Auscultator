CREATE TABLE IF NOT EXISTS person(_id INTEGER PRIMARY KEY AUTOINCREMENT, 
	name TEXT, 
	gender SMALLINT, 
	age SMALLINT, 
	tel TEXT
);

CREATE TABLE IF NOT EXISTS medical_records(
	_id INTEGER PRIMARY KEY AUTOINCREMENT, 
	userid INTEGER, 
	type SMALLINT, 
	time TIMESTAMP,
	sound_file TEXT,
	sound_path TEXT,
	FOREIGN KEY(userid) REFERENCES person(_id)
);