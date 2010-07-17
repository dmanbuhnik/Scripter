package com.faziklogic.scripter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper {

	private static final String DATABASE_NAME = "scripts.db";
	private static final int DATABASE_VERSION = 3;
	private static final String TABLE_NAME = "scripts";
	private static final String ORDER_BY = "type ASC, name ASC, last_run DESC";

	public static final int TYPE_SAVED = 10;
	public static final int TYPE_HISTORY = 20;

	private Context context;
	private SQLiteDatabase db;

	public DBHelper(Context context) {
		this.context = context;
		DBOpenHelper dbOpenHelper = new DBOpenHelper(this.context);
		this.db = dbOpenHelper.getWritableDatabase();
		Cursor dbVersionCheck = db.query(TABLE_NAME, null, null, null, null,
				null, null);
		int version = 2;
		for (String column : dbVersionCheck.getColumnNames()) {
			if (column.equals("type"))
				version = 3;
		}
		db.setVersion(version);
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
			values.put("root", true);
			values.put("after_boot", false);
			values.put("last_run", System.currentTimeMillis());
			if (name == null) {
				values.put("type", TYPE_HISTORY);
			} else {
				values.put("type", TYPE_SAVED);
				values.put("name", name);
			}
			values.put("script", script);
			this.db.insert(TABLE_NAME, null, values);
		}
		checkCursor.close();
	}

	public void insert(Integer id, String name, String script, Long lastRun) {
		ContentValues values = new ContentValues();

		if (!db.isOpen()) {
			DBOpenHelper dbOpenHelper = new DBOpenHelper(this.context);
			this.db = dbOpenHelper.getWritableDatabase();
		}
		values.put("_id", id);
		values.put("root", true);
		values.put("after_boot", false);
		if (name == null) {
			values.put("type", TYPE_HISTORY);
		} else {
			values.put("type", TYPE_SAVED);
			values.put("name", name);
		}
		values.put("script", script);
		values.put("last_run", lastRun);
		this.db.insert(TABLE_NAME, null, values);
	}

	public void updateScript(Integer id, String name, String script) {
		ContentValues values = new ContentValues();

		boolean openedHere = false;
		if (!db.isOpen()) {
			DBOpenHelper dbOpenHelper = new DBOpenHelper(this.context);
			this.db = dbOpenHelper.getWritableDatabase();
			openedHere = true;
		}
		Cursor checkCursor = this.db.query(TABLE_NAME, new String[] { "_id" },
				"_id=?", new String[] { id.toString() }, null, null, null);
		if (checkCursor.moveToFirst()) {
			if (name == null) {
				values.put("type", TYPE_HISTORY);
			} else {
				values.put("type", TYPE_SAVED);
			}
			values.put("name", name);
			values.put("script", script);
			this.db.update(TABLE_NAME, values, "_id=?", new String[] { id
					.toString() });
			if (openedHere)
				checkCursor.close();
		}
	}

	public void saveScript(String name, String script) {
		ContentValues values = new ContentValues();
		values.put("type", TYPE_SAVED);
		values.put("name", name);
		this.db.update(TABLE_NAME, values, "script=?", new String[] { script });
	}

	public Cursor getAllScripts() {
		return this.db.query(TABLE_NAME, new String[] { "_id", "type", "name",
				"script", "last_run" }, null, null, null, null, ORDER_BY);
	}

	public Cursor getSavedScripts() {
		return this.db.query(TABLE_NAME, new String[] { "_id", "type", "name",
				"script", "last_run" }, "name is not null", null, null, null,
				ORDER_BY);
	}

	public Cursor getHistoryScripts() {
		return this.db.query(TABLE_NAME, new String[] { "_id", "type", "name",
				"script", "last_run" }, "name is null", null, null, null,
				ORDER_BY);
	}

	public void updateLastRun(String script) {
		ContentValues values = new ContentValues();
		values.put("last_run", System.currentTimeMillis());
		this.db.update(TABLE_NAME, values, "script=?", new String[] { script });
	}

	public void deleteByScript(String script) {
		this.db.delete(TABLE_NAME, "script=?", new String[] { script });
	}

	public void deleteById(int id) {
		this.db.delete(TABLE_NAME, "_id=?",
				new String[] { Integer.toString(id) });
	}

	public void clearHistory() {
		this.db.delete(TABLE_NAME, "type='" + Integer.toString(TYPE_HISTORY)
				+ "'", null);
	}

	public void clearSaved() {
		this.db.delete(TABLE_NAME, "type='" + Integer.toString(TYPE_SAVED)
				+ "'", null);
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
				+ " (_id INTEGER, type INTEGER, name VARCHAR(40), script TEXT, last_run INTEGER, root BOOLEAN, after_boot BOOLEAN, "
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
			if (oldVersion == 2 && newVersion == 3) {
				Log.i("DBHelper", "scripts.db needs upgrade");
				ContentValues values = new ContentValues();

				db.execSQL("ALTER TABLE " + TABLE_NAME + " RENAME TO hold");
				db.execSQL(CREATE_STATEMENT);

				Cursor c = db.query("hold", null, null, null, null, null, null);
				while (c.moveToNext()) {
					values.put("_id", c.getInt(0));
					String name = c.getString(1);
					if ((name == null) || (name.equals("")))
						values.put("type", TYPE_HISTORY);
					else
						values.put("type", TYPE_SAVED);
					values.put("root", true);
					values.put("after_boot", false);
					values.put("name", name);
					values.put("script", c.getString(2));
					values.put("last_run", c.getLong(3));
					db.insert(TABLE_NAME, null, values);
					values.clear();
				}
				db.execSQL("DROP TABLE IF EXISTS hold");
				c.close();
				Log.i("DBHelper", "upgrade on scripts.db completed");
			}
		}
	}
}