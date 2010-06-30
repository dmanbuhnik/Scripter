package com.faziklogic.scripter;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class Scripter extends Activity {
	public final static String TAG = "Scripter";

	public final static int ACTIVITY_DISCLAIMER = 0;
	public final static int ACTIVITY_INIT = 1;
	public final static int ACTIVITY_SETTINGS = 2;
	public final static int ACTIVITY_SCAN = 3;

	public static DBHelper db;
	public static DatabaseAdapter savedDatabaseAdapter, historyDatabaseAdapter;
	private Button scanButton;
	private static TextView savedHeader;
	private static TextView historyHeader;
	private ListView savedList, historyList;
	private ShellInterface shellInterface;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.maingui);
		
		if (!getSharedPreferences(TAG, 0).getBoolean("userAgreed", false))
			startActivityForResult(new Intent(this, Disclaimer.class),
					ACTIVITY_DISCLAIMER);
		if (!getSharedPreferences(TAG, 0).getBoolean("applicationInitialized",
				false))
			startActivityForResult(new Intent(this, InitScripter.class),
					ACTIVITY_INIT);
		else {
			afterInit();
		}

	}

	public void afterInit() {
		shellInterface = new ShellInterface(getSharedPreferences(TAG, 0)
				.getString("suBinary", null), getSharedPreferences(TAG, 0)
				.getString("busyboxBinary", null));
		db = new DBHelper(this);
		savedDatabaseAdapter = new DatabaseAdapter(this, db.getSavedScripts(),
				shellInterface);
		historyDatabaseAdapter = new DatabaseAdapter(this, db
				.getHistoryScripts(), shellInterface);

		scanButton = (Button) findViewById(R.id.ScanButton);
		savedList = (ListView) findViewById(R.id.savedList);
		historyList = (ListView) findViewById(R.id.historyList);

		savedHeader = (TextView) findViewById(R.id.SavedListHeader);
		historyHeader = (TextView) findViewById(R.id.HistoryListHeader);
		if (!db.getSavedScripts().moveToFirst())
			savedHeader.setVisibility(View.GONE);
		if (!db.getHistoryScripts().moveToFirst())
			historyHeader.setVisibility(View.GONE);
		savedList.setAdapter(savedDatabaseAdapter);
		historyList.setAdapter(historyDatabaseAdapter);

		scanButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				runScan();
			}
		});
	}

	public static void refrashLists() {
		savedDatabaseAdapter.refrashCursor();
		if (!db.getSavedScripts().moveToFirst())
			savedHeader.setVisibility(View.GONE);
		else
			savedHeader.setVisibility(View.VISIBLE);
		if (!db.getHistoryScripts().moveToFirst())
			historyHeader.setVisibility(View.GONE);
		else
			historyHeader.setVisibility(View.VISIBLE);
		historyDatabaseAdapter.refrashCursor();
	}

	public void runScan() {
		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		this.getPackageManager();
		List<ResolveInfo> list = this.getPackageManager()
				.queryIntentActivities(intent,
						PackageManager.MATCH_DEFAULT_ONLY);
		if (list.size() > 0) {
			intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
			startActivityForResult(intent, ACTIVITY_SCAN);
		} else {
			Builder noScanDialog = new AlertDialog.Builder(this);
			noScanDialog.setTitle("No scanning application");
			noScanDialog.setMessage(getResources().getString(
					R.string.missingScanApp));
			noScanDialog.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			noScanDialog.show();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case (ACTIVITY_DISCLAIMER):
			if (!getSharedPreferences(TAG, 0).getBoolean("userAgreed", false))
				finish();
			break;
		case (ACTIVITY_INIT):
			if (resultCode == RESULT_OK) {
				afterInit();
			} else if (resultCode == RESULT_CANCELED) {
				finish();
			}
			break;
		case (ACTIVITY_SETTINGS):
			refrashLists();
			break;
		case (ACTIVITY_SCAN):
			if (resultCode == RESULT_OK) {
				String scanScript = intent.getStringExtra("SCAN_RESULT");
				String tempScript = "";
				for (String line : scanScript.split("\n")) {
					tempScript += line.trim() + "\n";
				}
				final String script = tempScript.trim();
				Builder confirmDialog = new AlertDialog.Builder(this);
				confirmDialog.setTitle("Run script?");
				confirmDialog.setMessage(script);
				confirmDialog.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								db.insert(null, script);
								refrashLists();
								new RunScript(Scripter.this, shellInterface,
										null, null).execute(script);
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
			} else if (resultCode == RESULT_CANCELED) {
				Log.d(TAG, "canceled, no script");
			}
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_about:
			Builder aboutDialog = new AlertDialog.Builder(this);
			aboutDialog.setTitle("About");
			aboutDialog.setMessage(getResources().getString(R.string.about));
			aboutDialog.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			aboutDialog.show();
			return true;
		case R.id.menu_contact:
			String version = "";
			try {
				version += " (ver. ";
				version += getPackageManager().getPackageInfo(getPackageName(),
						0).versionName;
				version += ") ";
			} catch (NameNotFoundException e) {
				e.printStackTrace();
				version = "";
			}
			final Intent emailIntent = new Intent(
					android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
					new String[] { getResources().getString(
							R.string.emailAddress) });
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, TAG
					+ version + " - contact developer");
			startActivity(Intent.createChooser(emailIntent, "Send mail..."));
			return true;
		case R.id.menu_settings:
			startActivityForResult(new Intent(this, SettingsActivity.class),
					ACTIVITY_SETTINGS);
			return true;
		case R.id.menu_quit:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		db.close();
	}
}