package com.hackers.epfl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// this.startService(new Intent(this, EBService.class));
		Intent intent = new Intent(getApplicationContext(), DisplayCardActivity.class);
		intent.putExtra(DisplayCardActivity.CARD_TEXT, "Text for testing");
		intent.putExtra(DisplayCardActivity.CARD_FOOTNOTE, "Footnote for testing");
		startActivity(intent);
	}
}
