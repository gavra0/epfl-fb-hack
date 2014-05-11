package com.hackers.epfl;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;

import com.google.android.glass.media.CameraManager;
import com.hackers.epfl.asynctasks.SendMessageAsyncTask;
import com.hackers.epfl.asynctasks.UploadMediaAsyncTask;

/**
 * @author Ivan Gavrilovic
 */
public class GetInputActivity extends Activity {
	private static final String TAG = GetInputActivity.class.getCanonicalName();
	private static final int SPEECH_REQUEST_NOTE = 22;
	private static final int CAPTURE_REQUEST_IMAGE = 33;
	private static final int CAPTURE_REQUEST_VIDEO = 55;
	private static final int SPEECH_REQUEST_QUESTIONS = 44;

	// private static final String HELP_NOTE = ""

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String spokenText = getIntent().getStringExtra(MainActivity.POST_TYPE);

		if (spokenText == null || spokenText.toLowerCase().equals("note")
				|| spokenText.toLowerCase().equals("notes")) {
			displaySpeechRecognizer(SPEECH_REQUEST_NOTE);
		} else if (spokenText.toLowerCase().equals("question")
				|| spokenText.toLowerCase().equals("questions")) {
			displaySpeechRecognizer(SPEECH_REQUEST_QUESTIONS);
		} else if (spokenText.toLowerCase().equals("video")
				|| spokenText.toLowerCase().equals("videos")) {
			getVideo();
		} else if (spokenText.toLowerCase().equals("image")
				|| spokenText.toLowerCase().equals("images")) {
			getImage();
		}
	}

	private void displaySpeechRecognizer(int code) {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		startActivityForResult(intent, code);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "Act result: req resp int " + requestCode + " " + resultCode);
		if (requestCode != SPEECH_REQUEST_NOTE && requestCode != SPEECH_REQUEST_QUESTIONS
				&& requestCode != CAPTURE_REQUEST_IMAGE) {
			finish();
			return;
		}

		// user entered the note to save, send msg and beacon
		SharedPreferences sharedPrefs =
				this.getSharedPreferences(getString(R.string.shared_data), MODE_PRIVATE);
		final String beacon = sharedPrefs.getString(Constants.SHARED_PREF_BEACON, EBService.DEAFULT_BEACON);
		if (EBService.DEAFULT_BEACON.equals(beacon)) {
			finish();
			return;
		}

		// TEXT BASED MESSAGES
		if (requestCode == SPEECH_REQUEST_NOTE || requestCode == SPEECH_REQUEST_QUESTIONS) {
			List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			if (results == null || results.isEmpty()) {
				Log.d(TAG, "No text recognized, trying again.");
			} else {
				String spokenText = results.get(0);
				Log.d(TAG, "Left note " + spokenText);

				if (requestCode == SPEECH_REQUEST_NOTE && resultCode == RESULT_OK) {
					SendMessageAsyncTask submitNote =
							new SendMessageAsyncTask(getApplicationContext(), beacon, spokenText);
					submitNote.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

					finish();
				} else if (requestCode == SPEECH_REQUEST_QUESTIONS && resultCode == RESULT_OK) {
					SendMessageAsyncTask submitNote =
							new SendMessageAsyncTask(getApplicationContext(), beacon, spokenText);
					submitNote.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

					finish();
				}
			}
		}
		// IMAGE BASED MESSAGES
		else if (requestCode == CAPTURE_REQUEST_IMAGE) {
			String path = getIntent().getExtras().getString(CameraManager.EXTRA_PICTURE_FILE_PATH);
			FileObserver observer = new FileObserver(path) {
				@Override
				public void onEvent(int event, String path) {
					if (event == FileObserver.CREATE) {
						Log.d(TAG, "The image has been saved");
						this.stopWatching();

						UploadMediaAsyncTask uploadMediaAsyncTask =
								new UploadMediaAsyncTask(getApplicationContext(), beacon, path,
										"vid");
						uploadMediaAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						finish();
					}
				}
			};
			observer.startWatching();
		} else if (requestCode == CAPTURE_REQUEST_VIDEO) {
			String path = getIntent().getExtras().getString(CameraManager.EXTRA_VIDEO_FILE_PATH);
			FileObserver observer = new FileObserver(path) {
				@Override
				public void onEvent(int event, String path) {
					if (event == FileObserver.CREATE) {
						Log.d(TAG, "The video has been saved");
						this.stopWatching();

						UploadMediaAsyncTask uploadMediaAsyncTask =
								new UploadMediaAsyncTask(getApplicationContext(), beacon, path,
										"img");
						uploadMediaAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						finish();
					}
				}
			};
			observer.startWatching();
		}
	}

	public void getVideo(){
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,CAPTURE_REQUEST_VIDEO);
    }

	public void getImage(){
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,CAPTURE_REQUEST_IMAGE);
    }
}
