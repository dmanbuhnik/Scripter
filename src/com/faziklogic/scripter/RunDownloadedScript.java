package com.faziklogic.scripter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class RunDownloadedScript extends Activity {
	ShellInterface shellInterface;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!getSharedPreferences(Scripter.TAG, 0).getBoolean("userAgreed",
				false))
			startActivityForResult(new Intent(this, Disclaimer.class),
					Scripter.ACTIVITY_DISCLAIMER);
		if (!getSharedPreferences(Scripter.TAG, 0).getBoolean(
				"applicationInitialized", false))
			startActivityForResult(new Intent(this, InitScripter.class),
					Scripter.ACTIVITY_INIT);
		else
			shellInterface = new ShellInterface(getSharedPreferences(
					Scripter.TAG, 0).getString("suBinary", null),
					getSharedPreferences(Scripter.TAG, 0).getString(
							"busyboxBinary", null));
		Intent intent = getIntent();

		final String script = Utils.readFile(intent.getData().getEncodedPath());
		if ((script == null) || (script.equals(""))) {
			Builder emptyScriptDialog = new AlertDialog.Builder(this);
			emptyScriptDialog.setTitle("Warning");
			emptyScriptDialog.setMessage("The script you selected is empty.");
			emptyScriptDialog.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							finish();
						}
					});
			emptyScriptDialog.setCancelable(false);
			emptyScriptDialog.show();
		} else {
			Builder confirmDialog = new AlertDialog.Builder(this);
			confirmDialog.setTitle("Run script?");
			confirmDialog.setMessage(script);
			confirmDialog.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (Scripter.db != null) {
								Scripter.db.insert(null, script);
								Scripter.refrashLists();
							} else {
								DBHelper db = new DBHelper(
										RunDownloadedScript.this);
								db.insert(null, script);
								db.close();
							}
							final Handler finishActivityHandler = new Handler();
							final Runnable finishActivityRunnable = new Runnable() {
								@Override
								public void run() {
									finish();
								}
							};
							new RunScript(RunDownloadedScript.this,
									shellInterface, finishActivityHandler,
									finishActivityRunnable).execute(script);
							dialog.dismiss();
						}
					});
			confirmDialog.setNegativeButton("No",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							finish();
						}
					});
			confirmDialog.show();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case (Scripter.ACTIVITY_DISCLAIMER):
			if (!getSharedPreferences(Scripter.TAG, 0).getBoolean("userAgreed",
					false))
				finish();
			break;
		case (Scripter.ACTIVITY_INIT):
			if (resultCode == RESULT_OK) {
				shellInterface = new ShellInterface(getSharedPreferences(
						Scripter.TAG, 0).getString("suBinary", null),
						getSharedPreferences(Scripter.TAG, 0).getString(
								"busyboxBinary", null));
			} else if (resultCode == RESULT_CANCELED) {
				finish();
			}
			break;
		}
	}
}