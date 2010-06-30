package com.faziklogic.scripter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class DatabaseAdapter extends CursorAdapter {
	private LayoutInflater inflater;
	private Cursor cursor;
	private ShellInterface shellInterface;
	private Context currentContext;

	public DatabaseAdapter(Context context, Cursor c,
			ShellInterface shellInterface) {
		super(context, c);
		this.inflater = LayoutInflater.from(context);
		this.cursor = c;
		this.shellInterface = shellInterface;
	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {
		final String scriptName = cursor.getString(1);
		final String script = cursor.getString(2);
		final Long lastRun = cursor.getLong(3);
		TextView scriptNameView = (TextView) view.findViewById(R.id.Name);
		TextView lastRunView = (TextView) view.findViewById(R.id.LastRun);
		ImageButton saveButton = (ImageButton) view
				.findViewById(R.id.saveButton);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Builder saveDialog = new AlertDialog.Builder(context);
				final EditText saveScriptNameEditText = new EditText(context);
				saveDialog.setView(saveScriptNameEditText);
				saveDialog.setTitle("Script name");
				saveDialog.setPositiveButton("Save",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String saveScriptName = saveScriptNameEditText
										.getText().toString();
								if (!saveScriptName.equals("")) {
									Scripter.db.saveScript(
											saveScriptNameEditText.getText()
													.toString(), script);
									Scripter.refrashLists();
								}
								dialog.dismiss();
							}
						});
				saveDialog.show();
			}
		});
		ImageButton reviewButton = (ImageButton) view
				.findViewById(R.id.reviewButton);
		reviewButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Builder reviewDialog = new AlertDialog.Builder(context);
				reviewDialog.setTitle("Review script");
				reviewDialog.setMessage(script);
				reviewDialog.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
				reviewDialog.show();
			}
		});
		ImageButton runButton = (ImageButton) view.findViewById(R.id.runButton);
		runButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Builder confirmDialog = new AlertDialog.Builder(context);
				confirmDialog.setTitle("Run script?");
				confirmDialog.setMessage(script);
				confirmDialog.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Scripter.db.updateLastRun(script);
								Scripter.refrashLists();
								new RunScript(context, shellInterface,null,null)
										.execute(script);
							}
						});
				confirmDialog.setNegativeButton("No",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
				confirmDialog.show();
			}
		});

		if (scriptName == null)
			scriptNameView.setVisibility(View.GONE);
		else {
			scriptNameView.setText(scriptName);
			saveButton.setVisibility(View.GONE);
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

	public class runScript extends AsyncTask<String, Void, Boolean> {
		ProgressDialog pd = null;

		@Override
		protected void onPreExecute() {
			pd = ProgressDialog.show(currentContext, "Running script",
					"Please wait..");
		}

		@Override
		protected Boolean doInBackground(String... params) {
			String[] allCommands = params[0].split("\n");
			ArrayList<String> commands = new ArrayList<String>(
					allCommands.length);
			for (String command : allCommands)
				commands.add(command);
			return shellInterface.runCommands(commands);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			pd.dismiss();
			Builder finishDialog = new AlertDialog.Builder(currentContext);
			finishDialog.setTitle("Script complete");
			if (result)
				finishDialog
						.setMessage("You may need to restart your device to see change"
								+ " (depensing on the script you ran)");
			else
				finishDialog
						.setMessage("Errors occur while running the script");
			finishDialog.show();
		}
	}

}
