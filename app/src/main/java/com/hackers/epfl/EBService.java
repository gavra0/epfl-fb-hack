package com.hackers.epfl;

/**
 * Created by tz on 10/05/2014.
 */

import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

//import com.google.android.glass.timeline.LiveCard;

public class EBService extends Service implements SensorEventListener {
	private Handler handler;
	private SensorManager sensorManager;
	private Sensor stepSensor;

	private BeaconManager beaconManager;

	private Region groundZero;
	private String currentStatus;

	private static final String TAG = "EBService";

	private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";

	// valid beacons
	private static final int beaconXMajor = 28945;
	private static final int beaconXMinor = 0;
	private static final int beaconYMajor = 28945;
	private static final int beaconYMinor = 1;
	private static final int beaconZMajor = 28945;
	private static final int beaconZMinor = 2;

    public static final String DEAFULT_BEACON = ESTIMOTE_PROXIMITY_UUID+"_"+beaconXMajor+"_"+beaconXMinor;

	// maximum range to beacon
	private static final double maxThreshold = 2;

	// scan length and period in seconds
	private static final int scanLength = 1;
	private static final int scanPeriod = 4;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private void runOnUiThread(Runnable runnable) {
		handler.post(runnable);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		handler = new Handler();
		currentStatus = "";

		// TODO add sensor data to stop/start beacon scanning
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
		sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);

		groundZero = new Region("groundZero", ESTIMOTE_PROXIMITY_UUID, null, null);
		beaconManager = new BeaconManager(getApplicationContext());

		beaconManager.setForegroundScanPeriod(	TimeUnit.SECONDS.toMillis(scanLength),
												TimeUnit.SECONDS.toMillis(scanPeriod));

		beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Beacon nearest = getNearestBeacon(beacons);
                        String nearestID = getBeaconID(nearest);

                        if (!nearestID.equals(currentStatus)) {
                            saveNearest(nearestID);
                            if (nearestID != "") {
                                notifyServer(nearestID);
                            }

                            currentStatus = nearestID;
                        }
                    }
                });
            }
        });
	}

	/**
	 * connect BeaconManager to service and start ranging
	 */
	private void startScanning() {
		beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
			@Override
			public void onServiceReady() {
				try {
					beaconManager.startRanging(groundZero);
				} catch (RemoteException e) {
					Log.d(TAG, "Error while starting Ranging");
				}
			}
		});
	}

	/**
	 * Stop ranging to given region
	 */
	private void stopScanning() {
		try {
			beaconManager.stopRanging(groundZero);
		} catch (RemoteException e) {
			Log.e(TAG, "Cannot stop but it does not matter now", e);
		}
	}

    private void saveNearest(String beaconID) {
        Log.i(TAG, beaconID);
        SharedPreferences.Editor editor = getSharedPreferences(Constants.COMMON_PREF, MODE_PRIVATE).edit();
        editor.putString(Constants.SHARED_PREF_BEACON, beaconID);
        editor.commit();
    }

    private void notifyServer(String beaconID) {

        new SendBeaconIDToServerAsyncTask(
                getApplicationContext(),
                beaconID,
                new SendBeaconIDToServerAsyncTask.ISendBeaconIDToServerResultHandler() {
                    @Override
                    public void onPostExecute(
                            BeaconAPIMessage.BeaconRequestResponse responseResult) {
                        //TODO create a CARD

                    }
                }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private Beacon getNearestBeacon(List<Beacon> beacons) {
        double min = maxThreshold;
        Beacon nearest = null;

        for (Beacon b : beacons) {
            if ((b.getMajor() == beaconXMajor && b.getMinor() == beaconXMinor)
                    || (b.getMajor() == beaconYMajor && b.getMinor() == beaconYMinor)
                    || (b.getMajor() == beaconZMajor && b.getMinor() == beaconZMinor)) {
                double distance = Utils.computeAccuracy(b);
                Log.i(TAG, b.getMinor() + " distance: " + distance);
                // get minimum within a maximum threshold (not too far)
                if (distance < maxThreshold && distance < min) {
                    min = distance;
                    nearest = b;
                }
            }
        }
        return nearest;
    }

    private String getBeaconID(Beacon beacon) {
        return beacon == null ? "" : beacon.getProximityUUID() + "_" + beacon.getMajor() + "_"
                + beacon.getMinor();
    }

	@Override
	public void onDestroy() {
		super.onDestroy();
		beaconManager.disconnect();
	}

	/**
	 * Start and stop scanning based on voice activated command
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		/*
		 * ArrayList<String> voiceResults =
		 * intent.getExtras().getStringArrayList(RecognizerIntent.EXTRA_RESULTS); for (String voice
		 * : voiceResults) { Log.d(TAG, "voiceResults:voice = " + voice); if
		 * (voice.contains("stop")){ Log.d(TAG,"stopScanning"); stopScanning(); }else if
		 * (voice.contains("start")){ Log.d(TAG,"startScanning"); startScanning(); } }
		 */
		startScanning();

		return START_STICKY;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub

	}
}
