package com.hackers.epfl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
			getImage();
		} else if (spokenText.toLowerCase().equals("image")
				|| spokenText.toLowerCase().equals("images")) {
			getImage();
		} else {
			displaySpeechRecognizer(SPEECH_REQUEST_NOTE);
		}
	}

	private void displaySpeechRecognizer(int code) {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		startActivityForResult(intent, code);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "Act result: req resp int " + requestCode + " " + resultCode);
		if (data == null || requestCode != SPEECH_REQUEST_NOTE
				&& requestCode != SPEECH_REQUEST_QUESTIONS && requestCode != CAPTURE_REQUEST_IMAGE
				&& requestCode != CAPTURE_REQUEST_VIDEO) {
			finish();
			return;
		}

		// user entered the note to save, send msg and beacon
		SharedPreferences sharedPrefs =
				this.getSharedPreferences(Constants.COMMON_PREF, MODE_PRIVATE);
		final String beacon =
				sharedPrefs.getString(Constants.SHARED_PREF_BEACON, EBService.DEAFULT_BEACON);
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
							new SendMessageAsyncTask(getApplicationContext(), beacon, spokenText, false);
					submitNote.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

					finish();
				} else if (requestCode == SPEECH_REQUEST_QUESTIONS && resultCode == RESULT_OK) {
					SendMessageAsyncTask submitNote =
							new SendMessageAsyncTask(getApplicationContext(), beacon, spokenText, true);
					submitNote.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

					finish();
				}
			}
		}
		// IMAGE BASED MESSAGES
		else if (requestCode == CAPTURE_REQUEST_IMAGE) {
			if (resultCode == RESULT_OK) {
                String picturePath = data.getStringExtra(
                        CameraManager.EXTRA_PICTURE_FILE_PATH);
                processPictureWhenReady(picturePath, beacon);

				finish();
			} else if (resultCode == RESULT_CANCELED) {
				// User cancelled the image capture
			} else {
				// Image capture failed, advise user
			}

		}
	}

    private void getImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAPTURE_REQUEST_IMAGE);
    }

    private void processPictureWhenReady(final String picturePath, final String beacon) {
        final File pictureFile = new File(picturePath);

        if (pictureFile.exists()) {
            // The picture is ready; process it.
            UploadMediaAsyncTask uploadMediaAsyncTask =
                    new UploadMediaAsyncTask(getApplicationContext(), beacon, picturePath,
                            "img");
            uploadMediaAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            // The file does not exist yet. Before starting the file observer, you
            // can update your UI to let the user know that the application is
            // waiting for the picture (for example, by displaying the thumbnail
            // image and a progress indicator).

            final File parentDirectory = pictureFile.getParentFile();
            FileObserver observer = new FileObserver(parentDirectory.getPath(),
                    FileObserver.CLOSE_WRITE | FileObserver.MOVED_TO) {
                // Protect against additional pending events after CLOSE_WRITE
                // or MOVED_TO is handled.
                private boolean isFileWritten;

                @Override
                public void onEvent(int event, String path) {
                    if (!isFileWritten) {
                        // For safety, make sure that the file that was created in
                        // the directory is actually the one that we're expecting.
                        File affectedFile = new File(parentDirectory, path);
                        isFileWritten = affectedFile.equals(pictureFile);

                        if (isFileWritten) {
                            stopWatching();

                            // Now that the file is ready, recursively call
                            // processPictureWhenReady again (on the UI thread).
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    processPictureWhenReady(picturePath, beacon);
                                }
                            });
                        }
                    }
                }
            };
            observer.startWatching();
        }
    }
}
