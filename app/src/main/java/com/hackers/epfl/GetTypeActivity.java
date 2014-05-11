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
public class GetTypeActivity extends Activity{
    private static final String TAG = GetTypeActivity.class.getCanonicalName();
    public static final int GET_POST_TYPE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        displaySpeechRecognizer();
    }

    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        startActivityForResult(intent, GET_POST_TYPE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "Act result: req resp int " + requestCode + " " + resultCode);
        if (requestCode != GET_POST_TYPE && data == null){
            return;
        }

        List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if (results == null || results.isEmpty()) {
            Log.d(TAG, "No text recognized, trying again.");
        } else {
            String spokenText = results.get(0);
            Log.d(TAG, "Post type " + spokenText);
            if (requestCode == GET_POST_TYPE && resultCode == RESULT_OK) {
                startInput(spokenText);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void startInput(String type){
        Intent intent = new Intent(this, GetInputActivity.class);
        intent.putExtra(MainActivity.POST_TYPE, type);
        finish();
        this.startActivity(intent);
    }
}
