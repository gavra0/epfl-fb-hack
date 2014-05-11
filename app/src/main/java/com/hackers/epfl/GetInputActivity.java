package com.hackers.epfl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;

import com.hackers.epfl.services.LiveCardService;

import java.util.List;

/**
 * @author Ivan Gavrilovic
 */
public class GetInputActivity extends Activity{
    private static final String TAG = GetInputActivity.class.getCanonicalName();
    public static final int GET_POST_TYPE = 1;
    private static final int SPEECH_REQUEST_NOTE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String spokenText = getIntent().getStringExtra(MainActivity.POST_TYPE);

        if (spokenText.toLowerCase().equals("note")
                || spokenText.toLowerCase().equals("notes")) {
            displaySpeechRecognizer(SPEECH_REQUEST_NOTE);
        } else if (spokenText.toLowerCase().equals("question")
                || spokenText.toLowerCase().equals("questions")) {
            // TODO input question
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
        if (requestCode != SPEECH_REQUEST_NOTE && data == null){
            return;
        }

        List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if (results == null || results.isEmpty()) {
            Log.d(TAG, "No text recognized, trying again.");
        } else {
            String spokenText = results.get(0);
            Log.d(TAG, "Left note " + spokenText);
            if (requestCode == SPEECH_REQUEST_NOTE && resultCode == RESULT_OK) {
                // user entered the note to save
                // TODO invoke service to publish this, now just show the thing
                finish();
                this.startService(new Intent(getApplicationContext(), LiveCardService.class));
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
