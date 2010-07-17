package com.faziklogic.scripter;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class DatabaseAdapter extends CursorAdapter {
	private LayoutInflater inflater;
	private Cursor cursor;

	public DatabaseAdapter(Context context, Cursor c) {
		super(context, c);
		this.inflater = LayoutInflater.from(context);
		this.cursor = c;
	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {
		// final int id = cursor.getInt(0);
		final int type = cursor.getInt(1);
		final String scriptName = cursor.getString(2);
		// final String script = cursor.getString(3);
		final Long lastRun = cursor.getLong(4);
		TextView scriptNameView = (TextView) view.findViewById(R.id.Name);
		TextView lastRunView = (TextView) view.findViewById(R.id.LastRun);

		if (type == DBHelper.TYPE_HISTORY) {
			scriptNameView.setText("[Long click to save]");
			scriptNameView.setVisibility(View.VISIBLE);
		} else {
			scriptNameView.setText(scriptName);
			scriptNameView.setVisibility(View.VISIBLE);
		}
		lastRunView.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date(lastRun)));
	}

	public void refrashCursor() {
		this.cursor.requery();
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return inflater.inflate(R.layout.listitem, parent, false);
	}
}
