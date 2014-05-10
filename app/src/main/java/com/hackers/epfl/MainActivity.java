package com.hackers.epfl;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.hackers.epfl.services.LiveCardService;

public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getCanonicalName();

	public static final String POST_TYPE = "main.post.type";
    public static final String LOCATION_ID = "main.location.id";

    public static final String MSG_COUNT_LOC = "msgCount.loc";

    public static final String GNOTES = "app.gnotes";

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "On resume");

		Intent intent = getIntent();
		if (intent != null && intent.getExtras() != null) {
			String voiceResult = intent.getExtras().getString(POST_TYPE);
			if (voiceResult != null && !voiceResult.isEmpty()) {
				Log.d(TAG, "Started the app by saying: " + voiceResult);
			} else {
				Log.d(TAG, "Started the app by clicking icon.");
			}
		} else {
			Log.d(TAG, "Started the app by clicking icon.");
		}
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		openOptionsMenu();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.type_choose_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection.
		switch (item.getItemId()) {
		case R.id.menu_note:
			Log.d(TAG, "Menu note");
			displaySpeechRecognizer();
			return true;
		case R.id.menu_question:
			Log.d(TAG, "Menu question");
			return true;
		case R.id.menu_video:
			Log.d(TAG, "Menu video");
			return true;
		case R.id.menu_image:
			Log.d(TAG, "Menu image");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        // init count to 0s
        SharedPreferences sharedPrefs = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        if (!sharedPrefs.contains(MSG_COUNT_LOC+"0")){
            for (int i = 0; i < 3; i++) editor.putInt(MSG_COUNT_LOC+i, 0);
        }
        editor.commit();

		setContentView(R.layout.activity_main);

		//this.startService(new Intent(this, EBService.class));
        //Intent intent = new Intent(getApplicationContext(), DisplayCardActivity.class);
        //intent.putExtra(DisplayCardActivity.CARD_TEXT, "Text for testing");
        //intent.putExtra(DisplayCardActivity.CARD_FOOTNOTE, "Footnote for testing");
        //sstartActivity(intent);
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		// Nothing else to do, closing the Activity.
		//finish();
	}

	private static final int SPEECH_REQUEST_NOTE = 0;

	private void displaySpeechRecognizer() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		startActivityForResult(intent, SPEECH_REQUEST_NOTE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "Act result: req resp int " + requestCode + " " + resultCode);
		if (requestCode == SPEECH_REQUEST_NOTE && resultCode == RESULT_OK) {
			List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			if (results.isEmpty()) {
				Log.d(TAG, "No text recognized, trying again.");
				super.onActivityResult(requestCode, resultCode, data);

				displaySpeechRecognizer();
			}
			String spokenText = results.get(0);
			Log.d(TAG, "Left note " + spokenText);

            this.startService(new Intent(this, LiveCardService.class));
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
