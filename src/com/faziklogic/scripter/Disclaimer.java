package com.faziklogic.scripter;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Disclaimer extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.disclaimer);

		Button acceptButton = (Button) findViewById(R.id.AcceptDisclaimerButton);
		Button cancelButton = (Button) findViewById(R.id.DeclineDisclaimerButton);
		TextView disclaimerText = (TextView) findViewById(R.id.DisclaimerText);

		disclaimerText
				.setText(getResources().getString(R.string.dislaimerText));
		acceptButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences settings = getSharedPreferences(Scripter.TAG,
						0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean("userAgreed", true);
				editor.commit();
				finish();
			}
		});
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
