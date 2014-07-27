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
	time DATETIME DEFAULT CURRENT_TIMESTAMP,
	sound TEXT,
	FOREIGN KEY(userid) REFERENCES person(_id)
);

INSERT INTO person(name, gender, age, tel) VALUES ("admin", 1, 25, "18121282862");

INSERT INTO medical_records(userid, type, sound) VALUES(1, 2, "test");

