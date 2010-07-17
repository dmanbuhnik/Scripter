package com.faziklogic.scripter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

public class RunDownloadedScript extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!getSharedPreferences(Scripter.TAG, 0).getBoolean("userAgreed",
				false))
			startActivityForResult(new Intent(this, Disclaimer.class),
					Scripter.ACTIVITY_DISCLAIMER);
		else
			afterDisclaimer();
	}

	public void afterDisclaimer() {
		if (!getSharedPreferences(Scripter.TAG, 0).getBoolean(
				"applicationInitialized", false))
			startActivityForResult(new Intent(this, InitScripter.class),
					Scripter.ACTIVITY_INIT);
		else
			afterInit();
	}

	private void afterInit() {
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
									finishActivityHandler,
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
			else
				afterDisclaimer();
			break;
		case (Scripter.ACTIVITY_INIT):
			if (!getSharedPreferences(Scripter.TAG, 0).getBoolean("hasSu",
					false)) {
				Builder missingSUDialog = new AlertDialog.Builder(this);
				missingSUDialog.setTitle("Cannot continue");
				missingSUDialog.setMessage(getResources().getString(
						R.string.missingSu));
				missingSUDialog.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}
						});
				missingSUDialog.show();
			} else if (!getSharedPreferences(Scripter.TAG, 0).getBoolean(
					"hasBb", false)) {
				Builder missingBBDialog = new AlertDialog.Builder(this);
				missingBBDialog.setTitle("Cannot continue");
				missingBBDialog.setMessage(getResources().getString(
						R.string.missingBusybox));
				missingBBDialog.setPositiveButton("Search Market",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								RunDownloadedScript.this
										.startActivity(new Intent(
												Intent.ACTION_VIEW,
												Uri
														.parse("market://search?q=busybox")));
								setResult(RESULT_CANCELED);
								finish();
							}
						});
				missingBBDialog.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								setResult(RESULT_CANCELED);
								finish();
							}
						});
				missingBBDialog.show();
			} else {
				afterInit();
			}
			break;
		}
	}
}