package com.faziklogic.scripter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper {

	private static final String DATABASE_NAME = "scripts.db";
	private static final int DATABASE_VERSION = 2;
	private static final String TABLE_NAME = "scripts";

	private Context context;
	private SQLiteDatabase db;

	public DBHelper(Context context) {
		this.context = context;
		DBOpenHelper dbOpenHelper = new DBOpenHelper(this.context);
		this.db = dbOpenHelper.getWritableDatabase();
	}

	public void insert(String name, String script) {
		ContentValues values = new ContentValues();

		if (!db.isOpen()) {
			DBOpenHelper dbOpenHelper = new DBOpenHelper(this.context);
			this.db = dbOpenHelper.getWritableDatabase();
		}
		Cursor checkCursor = this.db.query(TABLE_NAME,
				new String[] { "script" }, "script=?", new String[] { script },
				null, null, null);

		if (checkCursor.moveToFirst()) {
			values.put("last_run", System.currentTimeMillis());
			this.db.update(TABLE_NAME, values, "script=?",
					new String[] { script });
		} else {
			values.put("last_run", System.currentTimeMillis());
			if (name != null)
				values.put("name", name);
			values.put("script", script);
			this.db.insert(TABLE_NAME, null, values);
		}
	}

	public void saveScript(String name, String script) {
		ContentValues values = new ContentValues();
		values.put("name", name);
		this.db.update(TABLE_NAME, values, "script=?", new String[] { script });
	}

	public Cursor getAllScripts() {
		return this.db
				.query(TABLE_NAME, new String[] { "_id", "name", "script",
						"last_run" }, null, null, null, null, "last_run DESC");
	}

	public Cursor getSavedScripts() {
		return this.db.query(TABLE_NAME, new String[] { "_id", "name",
				"script", "last_run" }, "name is not null", null, null, null,
				"name ASC");
	}

	public Cursor getHistoryScripts() {
		return this.db.query(TABLE_NAME, new String[] { "_id", "name",
				"script", "last_run" }, "name is null", null, null, null,
				"last_run DESC");
	}

	public void updateLastRun(String script) {
		ContentValues values = new ContentValues();
		values.put("last_run", System.currentTimeMillis());
		this.db.update(TABLE_NAME, values, "script=?", new String[] { script });
	}

	public void deleteById(int id) {
		this.db.delete(TABLE_NAME, "_id=?",
				new String[] { Integer.toString(id) });
	}

	public void clearHistory() {
		this.db.delete(TABLE_NAME, "name is null", null);
	}

	public void clearSaved() {
		this.db.delete(TABLE_NAME, "name is not null", null);
	}

	public void clearDB() {
		this.db.delete(TABLE_NAME, null, null);
	}

	public void close() {
		if (this.db.isOpen()) {
			this.db.close();
		}
	}

	private static class DBOpenHelper extends SQLiteOpenHelper {
		private static final String CREATE_STATEMENT = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME
				+ " (_id INTEGER, name VARCHAR(40), script TEXT, last_run INTEGER, "
				+ "PRIMARY KEY (_id), UNIQUE (name,script));";

		DBOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_STATEMENT);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (oldVersion == 1 && newVersion == 2) {
				ContentValues values = new ContentValues();

				db.execSQL("ALTER TABLE " + TABLE_NAME + " RENAME TO hold");
				db.execSQL(CREATE_STATEMENT);

				Cursor c = db.query("hold", null, null, null, null, null, null);
				while (c.moveToNext()) {
					values.put("_id", c.getInt(0));
					values.put("name", c.getString(1));
					values.put("script", c.getString(2));
					values.put("last_run", c.getLong(3));
					db.insert(TABLE_NAME, null, values);
					values.clear();
				}
				db.execSQL("DROP TABLE IF EXISTS hold");
				c.close();
			}
		}
	}
}