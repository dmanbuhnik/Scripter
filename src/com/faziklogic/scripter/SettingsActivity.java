package com.faziklogic.scripter;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		Preference clearHistory = findPreference("clearHistory");
		clearHistory.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Scripter.db.clearHistory();
				Toast.makeText(SettingsActivity.this, "History scripts deleted", Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		Preference clearSaved = findPreference("clearSaved");
		clearSaved.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Scripter.db.clearSaved();
				Toast.makeText(SettingsActivity.this, "Saved scripts deleted", Toast.LENGTH_SHORT).show();
				return true;
			}
		});
	}
}
