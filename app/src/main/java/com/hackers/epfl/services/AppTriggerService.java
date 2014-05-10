package com.hackers.epfl.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.util.Log;

import com.google.android.glass.timeline.LiveCard;
import com.hackers.epfl.MainActivity;
import com.hackers.epfl.drawer.TypeChooserDrawer;

import java.util.ArrayList;

public class AppTriggerService extends Service {
    private static final String TAG=AppTriggerService.class.getCanonicalName();

    public AppTriggerService() {
    }

    private static final String LIVE_CARD_TAG = "stopwatch";

    private TypeChooserDrawer mCallback;

    private LiveCard mLiveCard;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLiveCard == null) {
            mLiveCard = new LiveCard(this, LIVE_CARD_TAG);

            // Keep track of the callback to remove it before unpublishing.
            mCallback = new TypeChooserDrawer(this);
            mLiveCard.setDirectRenderingEnabled(true).getSurfaceHolder().addCallback(mCallback);

            String spokenText = "";
            if(intent != null && intent.getExtras() != null){
                ArrayList<String> voiceResults = intent.getExtras()
                        .getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
                if (voiceResults != null && !voiceResults.isEmpty()){
                    spokenText = voiceResults.get(0);
                    Log.d(TAG, "Started the app by saying: " + spokenText);
                }
                else{
                    Log.d(TAG, "Started the app by clicking icon.");
                }
            }
            else{
                Log.d(TAG, "Started the app by clicking icon.");
            }

            Intent menuIntent = new Intent(this, MainActivity.class);
            menuIntent.putExtra(MainActivity.POST_TYPE, spokenText);
            menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
            mLiveCard.attach(this);
            mLiveCard.publish(LiveCard.PublishMode.REVEAL);
        } else {
            mLiveCard.navigate();
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
}
