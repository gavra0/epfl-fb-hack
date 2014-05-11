package com.hackers.epfl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.hackers.epfl.services.AppTriggerService;

public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getCanonicalName();

	public static final String POST_TYPE = "main.post.type";
	public static final String LOCATION_ID = "main.location.id";

	public static final String MSG_COUNT_LOC = "msgCount.loc";

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "On resume");
		AppTriggerService.mainAct = this;

		Intent intent = getIntent();
		if (intent != null && intent.getExtras() != null) {
			String voiceResult = intent.getExtras().getString(POST_TYPE);
			if (voiceResult != null && !voiceResult.isEmpty()) {
				Log.d(TAG, "Started the app by saying: " + voiceResult);
				getInputForType(voiceResult);
			} else {
				Log.d(TAG, "Started the app by clicking icon. Show menu.");
                Intent typeIntent = new Intent(this, GetTypeActivity.class);
                finish();
                this.startActivity(typeIntent);
			}
		} else {
			Log.d(TAG, "Started the app by clicking icon. Show menu.");
            Intent typeIntent = new Intent(this, GetTypeActivity.class);
            finish();
            this.startActivity(typeIntent);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        // starts beacon service
        this.startService(new Intent(this, EBService.class));

		// init count to 0s
		SharedPreferences sharedPrefs = this.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		if (!sharedPrefs.contains(MSG_COUNT_LOC + "0")) {
			for (int i = 0; i < 3; i++)
				editor.putInt(MSG_COUNT_LOC + i, 0);
		}
		editor.commit();

		// this.startService(new Intent(this, EBService.class));
		// Intent intent = new Intent(getApplicationContext(), DisplayCardActivity.class);
		// intent.putExtra(DisplayCardActivity.CARD_TEXT, "Text for testing");
		// intent.putExtra(DisplayCardActivity.CARD_FOOTNOTE, "Footnote for testing");
		// sstartActivity(intent);
	}

	public void getInputForType(String spokenText) {
		Intent intent = new Intent(this, GetInputActivity.class);
		intent.putExtra(POST_TYPE, spokenText);
		finish();
		this.startActivity(intent);
	}
}
