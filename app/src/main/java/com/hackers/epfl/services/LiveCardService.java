package com.hackers.epfl.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;
import com.hackers.epfl.MainActivity;
import com.hackers.epfl.R;

public class LiveCardService extends Service {

	private static final String LIVE_CARD_TAG = "LiveCardDemo";

	private LiveCard mLiveCard;
	private RemoteViews mLiveCardView;

	private final Handler mHandler = new Handler();
	private static final long DELAY_MILLIS = 30000;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (mLiveCard == null) {

			// Get an instance of a live card
			mLiveCard = new LiveCard(this, LIVE_CARD_TAG);

			// Inflate a layout into a remote view
			mLiveCardView = new RemoteViews(getPackageName(), R.layout.activity_main);

			// TODO put the location id in the intent
			int locationId = 1;
			Context ctx = getApplicationContext();
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
			int nCount = prefs.getInt(MainActivity.MSG_COUNT_LOC + locationId, 0);

			// Set up initial RemoteViews values
			mLiveCardView.setTextViewText(R.id.notification_count, nCount + " message"
					+ (nCount > 1 ? "s" : ""));

			// Set up the live card's action with a pending intent
			// to show a menu when tapped
			Intent menuIntent = new Intent(this, MainActivity.class);
			menuIntent.putExtra(MainActivity.LOCATION_ID, locationId);
			menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));

			// Publish the live card
			mLiveCard.publish(PublishMode.REVEAL);
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		if (mLiveCard != null && mLiveCard.isPublished()) {
			mLiveCard.unpublish();
			mLiveCard = null;
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
