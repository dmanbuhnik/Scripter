package com.faziklogic.scripter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditScript extends Activity {
	public final static int EDIT_SCRIPT_TYPE_EDIT = 0;
	public final static int EDIT_SCRIPT_TYPE_NEW = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editscriptgui);
		Intent intent = getIntent();
		final int id = intent.getExtras().getInt("id");
		final String name = intent.getExtras().getString("name");
		final String script = intent.getExtras().getString("script");
		final int type = intent.getExtras().getInt("type");
		final EditText scriptNameManualEditText = (EditText) findViewById(R.id.ScriptNameEditText);
		if (name != null)
			scriptNameManualEditText.setText(name);
		final EditText scriptCommandsManualEditText = (EditText) findViewById(R.id.CommandsEditText);
		if (script != null)
			scriptCommandsManualEditText.setText(script);
		Button saveManualButton = (Button) findViewById(R.id.SaveManualButton);
		saveManualButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String name = scriptNameManualEditText.getText().toString();
				String script = scriptCommandsManualEditText.getText()
						.toString();
				switch (type) {
				case EDIT_SCRIPT_TYPE_EDIT:
					 Scripter.db.updateScript(id, name, script);
					 Scripter.refrashLists();
					 finish();
					break;
				case EDIT_SCRIPT_TYPE_NEW:
					if ((script == null) || (script.equals("")))
						finish();
					if ((name == null) || (name.equals("")))
						Scripter.db.insert(null, script);
					else
						Scripter.db.insert(name, script);
					Scripter.refrashLists();
					finish();
					break;
				}
				//Error! now one of the above options
				finish();
			}
		});
		Button cancelManualButton = (Button) findViewById(R.id.CancelManualButton);
		cancelManualButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
}
