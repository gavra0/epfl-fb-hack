package com.hackers.epfl.services;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.util.Log;

import com.google.android.glass.timeline.LiveCard;
import com.hackers.epfl.MainActivity;

public class AppTriggerService extends Service {
	private static final String TAG = AppTriggerService.class.getCanonicalName();

	public AppTriggerService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String spokenText = "";
		if (intent != null && intent.getExtras() != null) {
			ArrayList<String> voiceResults =
					intent.getExtras().getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
			if (voiceResults != null && !voiceResults.isEmpty()) {
				spokenText = voiceResults.get(0);
				Log.d(TAG, "Started the app by saying: " + spokenText);
			} else {
				Log.d(TAG, "Started the app by clicking icon.");
			}
		} else {
			Log.d(TAG, "Started the app by clicking icon.");
		}

		Intent menuIntent = new Intent(this, MainActivity.class);
		menuIntent.putExtra(MainActivity.POST_TYPE, spokenText);
		menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        getApplicationContext().startActivity(menuIntent);

		return START_STICKY;
	}
}
