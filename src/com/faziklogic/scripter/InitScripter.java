package com.faziklogic.scripter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

public class InitScripter extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!getSharedPreferences(Scripter.TAG, 0).getBoolean(
				"applicationInitialized", false)) {
			new init().execute((Void) null);
		}
	}

	public class init extends AsyncTask<Void, String, String> {
		ProgressDialog pd = null;

		@Override
		protected void onPreExecute() {
			pd = ProgressDialog.show(InitScripter.this,
					"Initialing application", "Please wait...");
		}

		@Override
		protected String doInBackground(Void... params) {
			ShellInterface shellInterface = new ShellInterface(null, null);
			if (shellInterface.getSuBinary() == null) {
				return (getResources().getString(R.string.missingSu));
			}
			if (shellInterface.getBusyboxBinary() == null) {
				return (getResources().getString(R.string.missingBusybox));
			}
			SharedPreferences settings = getSharedPreferences(Scripter.TAG, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("suBinary", shellInterface.getSuBinary());
			editor
					.putString("busyboxBinary", shellInterface
							.getBusyboxBinary());

			editor.putBoolean("applicationInitialized", true);
			editor.commit();
			return "clean";
		}

		@Override
		protected void onPostExecute(String result) {
			pd.dismiss();
			if (!result.equals("clean")) {
				Builder errorDialog = new AlertDialog.Builder(InitScripter.this);
				errorDialog.setTitle("Cannot continue");
				errorDialog.setMessage(result);
				errorDialog.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								setResult(RESULT_CANCELED);
								finish();
							}
						});
				errorDialog.show();
			} else {
				setResult(RESULT_OK);
				finish();
			}
		}
	}
}
