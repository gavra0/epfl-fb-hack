package com.hackers.epfl;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;

import com.hackers.epfl.asynctasks.SendMessageAsyncTask;

/**
 * @author Ivan Gavrilovic
 */
public class GetInputActivity extends Activity {
	private static final String TAG = GetInputActivity.class.getCanonicalName();
	private static final int SPEECH_REQUEST_NOTE =22;
	private static final int SPEECH_REQUEST_QUESTIONS = 44;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String spokenText = getIntent().getStringExtra(MainActivity.POST_TYPE);

		if (spokenText.toLowerCase().equals("note") || spokenText.toLowerCase().equals("notes")) {
			displaySpeechRecognizer(SPEECH_REQUEST_NOTE);
		} else if (spokenText.toLowerCase().equals("question")
				|| spokenText.toLowerCase().equals("questions")) {
			displaySpeechRecognizer(SPEECH_REQUEST_QUESTIONS);
		} else if (spokenText.toLowerCase().equals("video")
				|| spokenText.toLowerCase().equals("videos")) {
			// TODO input video
		} else if (spokenText.toLowerCase().equals("image")
				|| spokenText.toLowerCase().equals("images")) {
			// TODO input image
		}
	}

	private void displaySpeechRecognizer(int code) {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		startActivityForResult(intent, code);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "Act result: req resp int " + requestCode + " " + resultCode);
		if (requestCode != SPEECH_REQUEST_NOTE && requestCode != SPEECH_REQUEST_QUESTIONS) {
            finish();
			return;
		}

		List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
		if (results == null || results.isEmpty()) {
			Log.d(TAG, "No text recognized, trying again.");
		} else {
			String spokenText = results.get(0);
			Log.d(TAG, "Left note " + spokenText);
            // user entered the note to save, send msg and beacon
            SharedPreferences sharedPrefs =
                    this.getSharedPreferences(getString(R.string.shared_data), MODE_PRIVATE);
            String beacon = sharedPrefs.getString(EBService.NEAREST, EBService.DEAFULT_BEACON);
            if(EBService.DEAFULT_BEACON.equals(beacon)){
                finish();
                return;
            }

			if (requestCode == SPEECH_REQUEST_NOTE && resultCode == RESULT_OK) {
				SendMessageAsyncTask submitNote =
						new SendMessageAsyncTask(getApplicationContext(), beacon, spokenText);
				submitNote.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

				finish();
				// this.startService(new Intent(getApplicationContext(), LiveCardService.class));
				return;
			} else if (requestCode == SPEECH_REQUEST_QUESTIONS && resultCode == RESULT_OK) {
				SendMessageAsyncTask submitNote =
						new SendMessageAsyncTask(getApplicationContext(), beacon, spokenText);
				submitNote.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

				finish();
				// this.startService(new Intent(getApplicationContext(), LiveCardService.class));
				return;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
