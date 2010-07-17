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
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class Scripter extends Activity {
	public final static String TAG = "Scripter";

	public final static int ACTIVITY_DISCLAIMER = 0;
	public final static int ACTIVITY_INIT = 1;
	public final static int ACTIVITY_SETTINGS = 2;
	public final static int ACTIVITY_SCAN = 3;
	public final static int ACTIVITY_GUIDE = 4;

	public final static int LIST_CONTECTMENU_RUN = 0;
	public final static int LIST_CONTECTMENU_REVIEW = 1;
	public final static int LIST_CONTECTMENU_EDIT = 2;

	public static DBHelper db;
	public static DatabaseAdapter allDatabaseAdapter;
	public static Cursor allCursor;
	public static LinearLayout appLayout;
	public static int backgroundChosen;
	private Button scanButton, manualButton;
	// private static ImageButton helpButton;
	private static ListView allList;
	private boolean appReady = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		backgroundChosen = getSharedPreferences(TAG, 0).getInt(
				"backgroundIndex", 0);
		Drawable backgroundDrawable = getResources().obtainTypedArray(
				R.array.backgroundsImages).getDrawable(backgroundChosen);
		setContentView(R.layout.maingui);
		appLayout = (LinearLayout) findViewById(R.id.MainLayout);
		appLayout.setBackgroundDrawable(backgroundDrawable);
		// helpButton = (ImageButton) findViewById(R.id.helpImageButton);

		if (!getSharedPreferences(TAG, 0).getBoolean("userAgreed", false))
			startActivityForResult(new Intent(this, Disclaimer.class),
					ACTIVITY_DISCLAIMER);
		else
			afterDisclaimer();
	}

	public void afterDisclaimer() {
		if (!getSharedPreferences(TAG, 0).getBoolean("applicationInitialized",
				false))
			startActivityForResult(new Intent(this, InitScripter.class),
					ACTIVITY_INIT);
		else
			afterInit();
	}

	public void afterInit() {
		appReady = true;
		db = new DBHelper(this);
		allCursor = db.getAllScripts();
		allDatabaseAdapter = new DatabaseAdapter(this, allCursor);

		scanButton = (Button) findViewById(R.id.ScanButton);
		manualButton = (Button) findViewById(R.id.ManualButton);
		allList = (ListView) findViewById(R.id.allList);
		allList.setAdapter(allDatabaseAdapter);
		allList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Cursor cursor = (Cursor) parent.getAdapter().getItem(position);
				// final int id = cursor.getInt(0);
				// final int type = cursor.getInt(1);
				// final String scriptName = cursor.getString(2);
				final String script = cursor.getString(3);
				// final Long lastRun = cursor.getLong(4);
				Builder confirmDialog = new AlertDialog.Builder(Scripter.this);
				confirmDialog.setTitle("Run script?");
				confirmDialog.setMessage(script);
				confirmDialog.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								db.updateLastRun(script);
								refrashLists();
								new RunScript(Scripter.this, null, null)
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
		allList
				.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
					@Override
					public void onCreateContextMenu(ContextMenu menu, View v,
							ContextMenuInfo menuInfo) {
						menu.setHeaderTitle("Script options");
						getMenuInflater().inflate(R.menu.script_context_menu,
								menu);
					}
				});

		// helpButton.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// startActivity(new Intent(Scripter.this, Guide.class));
		// }
		// });

		scanButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				runScan();
			}
		});
		manualButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				editScript(null, null, null, EditScript.EDIT_SCRIPT_TYPE_NEW);
			}
		});
	}

	public static void refrashLists() {
		if (allCursor.isClosed())
			allCursor = db.getAllScripts();
		allCursor.requery();
		allDatabaseAdapter.changeCursor(allCursor);
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

	public void editScript(Integer id, String name, String script, int type) {
		Intent editScriptIntent = new Intent(this, EditScript.class);
		editScriptIntent.putExtra("id", id);
		editScriptIntent.putExtra("name", name);
		editScriptIntent.putExtra("script", script);
		editScriptIntent.putExtra("type", type);
		startActivity(editScriptIntent);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case (ACTIVITY_DISCLAIMER):
			if (!getSharedPreferences(TAG, 0).getBoolean("userAgreed", false))
				finish();
			else
				afterDisclaimer();
			break;
		case (ACTIVITY_INIT):
			if (!getSharedPreferences(TAG, 0).getBoolean("hasSu", false)) {
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
			} else if (!getSharedPreferences(TAG, 0).getBoolean("hasBb", false)) {
				Builder missingBBDialog = new AlertDialog.Builder(this);
				missingBBDialog.setTitle("Cannot continue");
				missingBBDialog.setMessage(getResources().getString(
						R.string.missingBusybox));
				missingBBDialog.setPositiveButton("Search Market",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Scripter.this
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
								new RunScript(Scripter.this, null, null)
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
			} else if (resultCode == RESULT_CANCELED) {
				Log.d(TAG, "canceled, no script");
			}
			break;
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		Cursor cursor = (Cursor) allList.getAdapter().getItem(info.position);
		final int id = cursor.getInt(0);
		// final int type = cursor.getInt(1);
		final String scriptName = cursor.getString(2);
		final String script = cursor.getString(3);
		// final Long lastRun = cursor.getLong(4);
		switch (item.getItemId()) {
		case R.id.list_menu_run:
			Builder confirmDialog = new AlertDialog.Builder(Scripter.this);
			confirmDialog.setTitle("Run script?");
			confirmDialog.setMessage(script);
			confirmDialog.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							db.updateLastRun(script);
							refrashLists();
							new RunScript(Scripter.this, null, null)
									.execute(script);
						}
					});
			confirmDialog.setNegativeButton("No",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			confirmDialog.show();
			break;
		case R.id.list_menu_save:
			Builder saveDialog = new AlertDialog.Builder(Scripter.this);
			final EditText saveScriptNameEditText = new EditText(Scripter.this);
			if ((scriptName != null) && (!scriptName.equals("")))
				saveScriptNameEditText.setText(scriptName);
			saveDialog.setView(saveScriptNameEditText);
			saveDialog.setTitle("Script name");
			saveDialog.setPositiveButton("Save",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String saveScriptName = saveScriptNameEditText
									.getText().toString();
							if (!saveScriptName.equals("")
									&& (!saveScriptName.equals(scriptName))) {
								Scripter.db.saveScript(saveScriptNameEditText
										.getText().toString(), script);
								Scripter.refrashLists();
							}
							dialog.dismiss();
						}
					});
			saveDialog.show();
			break;
		case R.id.list_menu_review:
			Builder reviewDialog = new AlertDialog.Builder(Scripter.this);
			reviewDialog.setTitle("Review script");
			reviewDialog.setMessage(script);
			reviewDialog.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			reviewDialog.show();
			break;
		case R.id.list_menu_edit:
			editScript(id, scriptName, script, EditScript.EDIT_SCRIPT_TYPE_EDIT);
			break;
		case R.id.list_menu_delete:
			Builder deleteDialog = new AlertDialog.Builder(Scripter.this);
			deleteDialog.setTitle("Delete script?");
			deleteDialog.setMessage(script);
			deleteDialog.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Scripter.db.deleteByScript(script);
							Scripter.refrashLists();
							dialog.dismiss();
						}
					});
			deleteDialog.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			deleteDialog.show();
			break;
		}
		return super.onContextItemSelected(item);
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
		case R.id.menu_change_log:
			Builder changeLogDialog = new AlertDialog.Builder(this);
			changeLogDialog.setTitle("Change Log");
			changeLogDialog.setMessage(getResources().getString(
					R.string.changeLog));
			changeLogDialog.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			changeLogDialog.show();
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
	public void onPause() {
		super.onPause();
		if (appReady) {
			allCursor.close();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (appReady)
			refrashLists();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (appReady)
			if (db != null)
				db.close();
	}
}