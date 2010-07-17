package com.faziklogic.scripter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;

import com.faziklogic.scripter.ShellCommand.CommandResult;

public class RunScript extends AsyncTask<String, Void, CommandResult> {
	Context context;
	ProgressDialog progressDialog;
	Handler handler;
	Runnable runnable;

	public RunScript(Context context, Handler handler, Runnable runnable) {
		this.context = context;
		this.handler = handler;
		this.runnable = runnable;
	}

	@Override
	protected void onPreExecute() {
		progressDialog = ProgressDialog.show(this.context, "Running script",
				"Please wait..");
	}

	@Override
	protected CommandResult doInBackground(String... params) {
		ShellCommand cmd = new ShellCommand();
		CommandResult r = cmd.su.runWaitFor(params[0]);
		return r;
	}

	@Override
	protected void onPostExecute(final CommandResult result) {
		progressDialog.dismiss();
		Builder finishDialog = new AlertDialog.Builder(this.context);
		finishDialog.setTitle("Script complete");
		if (result.success())
			finishDialog.setMessage("Script completed cleanly");
		else {
			finishDialog.setMessage("Error occurred while running the script\n"
					+ "It may or may not effect the outcome of the script\n"
					+ "Use 'More info' for detail error log");
		}
		finishDialog.setPositiveButton("More info",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Builder moreInfoDialog = new AlertDialog.Builder(
								RunScript.this.context);
						moreInfoDialog.setTitle("Output information");
						String output = "stdout:\n";
						if (result.stdout == null)
							output += "[empty]";
						else
							output += result.stdout;
						output += "\nstderr:\n";
						if (result.stderr == null)
							output += "[empty]";
						else
							output += result.stderr;
						moreInfoDialog.setMessage(output);
						moreInfoDialog.setPositiveButton("Close",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
										if (handler != null)
											if (runnable != null)
												handler.post(runnable);
									}
								});
						moreInfoDialog.show();
					}
				});
		finishDialog.setNegativeButton("Close",
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
