package com.hackers.epfl.services;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.util.Log;

import com.hackers.epfl.GetTypeActivity;
import com.hackers.epfl.MainActivity;

public class AppTriggerService extends Service {
	private static final String TAG = AppTriggerService.class.getCanonicalName();

    public static Activity mainAct;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Intent menuIntent = new Intent(this, GetTypeActivity.class);
        menuIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplicationContext().startActivity(menuIntent);

		return START_STICKY;
	}

    @Override
    public void onDestroy() {
        Log.d(TAG, "App trigger on destroy");
        super.onDestroy();
    }
}
