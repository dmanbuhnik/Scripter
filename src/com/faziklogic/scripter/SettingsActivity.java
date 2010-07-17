package com.faziklogic.scripter;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {
	private final String exportImportFile = Environment
			.getExternalStorageDirectory()
			+ "/scripter.json";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		Preference clearHistory = findPreference("clearHistory");
		clearHistory
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Scripter.db.clearHistory();
						Toast.makeText(SettingsActivity.this,
								"History scripts deleted", Toast.LENGTH_SHORT)
								.show();
						return true;
					}
				});
		Preference clearSaved = findPreference("clearSaved");
		clearSaved
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Scripter.db.clearSaved();
						Toast.makeText(SettingsActivity.this,
								"Saved scripts deleted", Toast.LENGTH_SHORT)
								.show();
						return true;
					}
				});
		Preference exportScripts = findPreference("exportScripts");
		exportScripts
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						if (!Environment.getExternalStorageState().equals(
								Environment.MEDIA_MOUNTED)) {
							Builder noSdcardDialog = new AlertDialog.Builder(
									SettingsActivity.this);
							noSdcardDialog.setTitle("Error");
							noSdcardDialog.setMessage(getResources().getString(
									R.string.noSdcard));
							noSdcardDialog.setPositiveButton("Ok",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
										}
									});
							noSdcardDialog.show();
							return false;
						} else {
							File checkExists = new File(exportImportFile);
							if (checkExists.exists()) {
								Builder fileExistsDialog = new AlertDialog.Builder(
										SettingsActivity.this);
								fileExistsDialog.setTitle("Override file?");
								fileExistsDialog.setMessage(getResources()
										.getString(R.string.fileExists));
								fileExistsDialog.setPositiveButton("Yes",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												dialog.dismiss();
												exportScripts();
											}
										});
								fileExistsDialog.setNegativeButton("No",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												dialog.dismiss();
											}
										});
								fileExistsDialog.show();
							} else {
								exportScripts();
							}
							return true;
						}
					}
				});
		Preference importScripts = findPreference("importScripts");
		importScripts
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						String sdFileContent = Utils.readFile(exportImportFile);
						if ((sdFileContent == null)
								|| (sdFileContent.equals("{}"))
								|| (sdFileContent.equals("{}\n"))
								|| (sdFileContent.equals(""))) {
							Toast.makeText(SettingsActivity.this,
									"No file or empty file", Toast.LENGTH_LONG)
									.show();
							return false;
						}
						try {
							JSONObject fileContent = new JSONObject(
									sdFileContent);
							JSONArray jsonScripts = fileContent
									.getJSONArray("scripts");
							for (int i = 0; i < jsonScripts.length(); i++) {
								String name = null;
								if (jsonScripts.getJSONObject(i).has("name"))
									name = jsonScripts.getJSONObject(i)
											.getString("name");
								Scripter.db.insert(jsonScripts.getJSONObject(i)
										.getInt("id"), name, jsonScripts
										.getJSONObject(i).getString("script"),
										jsonScripts.getJSONObject(i).getLong(
												"last_run"));
							}
						} catch (JSONException e) {
							e.printStackTrace();
							Toast.makeText(SettingsActivity.this,
									"JSON error, curropted file maybe",
									Toast.LENGTH_LONG).show();
							return false;
						}
						Toast.makeText(SettingsActivity.this,
								"Import completed", Toast.LENGTH_LONG).show();
						return true;
					}
				});
		Preference changeBackground = findPreference("changeBackground");
		changeBackground
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						TypedArray namesArray = getResources()
								.obtainTypedArray(R.array.backgroundsNames);
						final String[] names = new String[namesArray.length()];
						for (int i = 0; i < namesArray.length(); i++) {
							names[i] = namesArray.getString(i);
						}
						Builder backgroundDialog = new AlertDialog.Builder(
								SettingsActivity.this);
						backgroundDialog.setTitle("Pick background");
						backgroundDialog.setSingleChoiceItems(names, Scripter.backgroundChosen,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
										Drawable backgroundDrawable = getResources()
												.obtainTypedArray(
														R.array.backgroundsImages)
												.getDrawable(which);
										Scripter.appLayout
												.setBackgroundDrawable(backgroundDrawable);
										SharedPreferences settings = getSharedPreferences(
												Scripter.TAG, 0);
										SharedPreferences.Editor editor = settings
												.edit();
										editor.putInt("backgroundIndex", which);
										editor.commit();
										Scripter.backgroundChosen = which;
										Toast.makeText(
												SettingsActivity.this,
												names[which]
														+ " set as background",
												Toast.LENGTH_SHORT).show();
									}
								});
						backgroundDialog.show();
						return true;
					}
				});
	}

	private boolean exportScripts() {
		Cursor allScripts = Scripter.db.getAllScripts();
		JSONArray jsonScriptsArray = new JSONArray();
		while (allScripts.moveToNext()) {
			JSONObject jsonScript = new JSONObject();
			try {
				jsonScript.put("id", allScripts.getInt(0));
				jsonScript.put("type", allScripts.getInt(1));
				jsonScript.put("name", allScripts.getString(2));
				jsonScript.put("script", allScripts.getString(3));
				jsonScript.put("last_run", allScripts.getLong(4));
				jsonScript.put("root", true);
				jsonScript.put("after_boot", false);
				jsonScriptsArray.put(jsonScript);
			} catch (JSONException e) {
				e.printStackTrace();
				Toast.makeText(SettingsActivity.this,
						"Error while gathering scripts", Toast.LENGTH_LONG)
						.show();
				return false;
			}
		}
		JSONObject jsonScripts = new JSONObject();
		try {
			jsonScripts.put("scripts", jsonScriptsArray);
		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(SettingsActivity.this,
					"Error while gathering scripts", Toast.LENGTH_LONG).show();
			return false;
		}
		allScripts.close();

		if (!Utils.writeFile(exportImportFile, jsonScripts.toString())) {
			Toast.makeText(SettingsActivity.this, "Error while writing file",
					Toast.LENGTH_LONG).show();
			return false;
		}
		Toast.makeText(SettingsActivity.this, "Export completed",
				Toast.LENGTH_LONG).show();
		return true;
	}
}
