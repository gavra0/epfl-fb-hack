package com.hackers.epfl.services;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;
import com.hackers.epfl.Constants;
import com.hackers.epfl.MainActivity;
import com.hackers.epfl.R;

public class LiveCardService extends Service {
    private static final String TAG = LiveCardService.class.getCanonicalName();

	private static final String LIVE_CARD_TAG = "LiveCardDemo";

    public static LiveCardService instance = null;

	private LiveCard mLiveCard;
	private RemoteViews mLiveCardView;
    private LiveCard mLiveCard1;
    private RemoteViews mLiveCardView1;

	private final Handler mHandler = new Handler();
	private static final long DELAY_MILLIS = 30000;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (mLiveCard == null) {
            instance = this;
            Log.d(TAG, "On start service");

			// Get an instance of a live card
			mLiveCard = new LiveCard(this, LIVE_CARD_TAG);

			// Inflate a layout into a remote view
			mLiveCardView = new RemoteViews(getPackageName(), R.layout.activity_main);

            mLiveCard1 = new LiveCard(this, LIVE_CARD_TAG);

            // Inflate a layout into a remote view
            mLiveCardView1 = new RemoteViews(getPackageName(), R.layout.activity_main);

            Log.d(TAG, "Remote views "+mLiveCardView);

			// TODO put the location id in the intent
			int locationId = 1;
			SharedPreferences prefs = this.getSharedPreferences(Constants.COMMON_PREF, MODE_PRIVATE);
			int nCount = prefs.getInt(MainActivity.MSG_COUNT_LOC + locationId, 0);

			// Set up initial RemoteViews values
			mLiveCardView.setTextViewText(R.id.notification_count, nCount + " message"
					+ (nCount > 1 ? "s" : ""));

			// Set up the live card's action with a pending intent
			// to show a menu when tapped
            mLiveCard.setViews(mLiveCardView);
			Intent menuIntent = new Intent(this, MainActivity.class);
			menuIntent.putExtra(MainActivity.LOCATION_ID, locationId);
            menuIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
            mLiveCard.attach(this);

			// Publish the live card
			mLiveCard.publish(PublishMode.SILENT);

            // Set up initial RemoteViews values
            mLiveCardView1.setTextViewText(R.id.notification_count, nCount + " message"
                    + (nCount > 1 ? "s" : ""));

            // Set up the live card's action with a pending intent
            // to show a menu when tapped
            mLiveCard1.setViews(mLiveCardView1);
            Intent menuIntent1 = new Intent(this, MainActivity.class);
            menuIntent1.putExtra(MainActivity.LOCATION_ID, locationId);
            menuIntent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mLiveCard1.setAction(PendingIntent.getActivity(this, 0, menuIntent1, 0));
            mLiveCard1.attach(this);

            // Publish the live card
            mLiveCard1.publish(PublishMode.REVEAL);
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		if (mLiveCard != null && mLiveCard.isPublished()) {
			mLiveCard.unpublish();
			mLiveCard = null;
            instance = null;
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
