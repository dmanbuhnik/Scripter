package com.faziklogic.scripter;

import android.app.Activity;
import android.app.ProgressDialog;
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

	public class init extends AsyncTask<Void, String, Void> {
		ProgressDialog pd = null;

		@Override
		protected void onPreExecute() {
			pd = ProgressDialog.show(InitScripter.this,
					"Initialing application", "Please wait...");
		}

		@Override
		protected Void doInBackground(Void... params) {
			SharedPreferences settings = getSharedPreferences(Scripter.TAG, 0);
			SharedPreferences.Editor editor = settings.edit();
			ShellCommand cmd = new ShellCommand();
			editor.putBoolean("hasSu", cmd.canSU());
			editor.putBoolean("hasBb", cmd.hasBB());
			editor.putBoolean("applicationInitialized", true);
			editor.commit();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			pd.dismiss();
			finish();
		}
	}
}
