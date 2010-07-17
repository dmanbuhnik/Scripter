package com.faziklogic.scripter;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class Guide extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.guidegui);
		setTitle("Scripter Guide");
		TextView textGuide = (TextView) findViewById(R.id.TextGuide);
		textGuide.setText(getResources().getString(R.string.guideString));
	}
}