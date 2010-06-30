package com.faziklogic.scripter;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class RunScript extends AsyncTask<String, Void, Boolean> {
	Context context;
	ShellInterface shellInterface;
	ProgressDialog progressDialog;
	Handler handler;
	Runnable runnable;

	public RunScript(Context context, ShellInterface shellInterface,
			Handler handler, Runnable runnable) {
		this.context = context;
		this.shellInterface = shellInterface;
		this.handler = handler;
		this.runnable = runnable;
	}

	@Override
	protected void onPreExecute() {
		progressDialog = ProgressDialog.show(this.context, "Running script",
				"Please wait..");
	}

	@Override
	protected Boolean doInBackground(String... params) {
		Log.d("BLA",params[0]);
		Log.d("BLA",shellInterface.toString());
		String[] allCommands = params[0].split("\n");
		ArrayList<String> commands = new ArrayList<String>(allCommands.length);
		for (String command : allCommands) {
			commands.add(command);
		}
		return shellInterface.runCommands(commands);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		progressDialog.dismiss();
		Builder finishDialog = new AlertDialog.Builder(this.context);
		finishDialog.setTitle("Script complete");
		if (result)
			finishDialog.setMessage("Script completed cleanly");
		else
			finishDialog.setMessage("Error occurred while running the script");
		finishDialog.setPositiveButton("Ok",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						if (handler != null)
							if (runnable != null)
								handler.post(runnable);
					}
				});
		finishDialog.show();
	}
}
