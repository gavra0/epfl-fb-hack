package com.hackers.epfl;

/**
 * Created by tz on 10/05/2014.
 */
import java.util.List;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class EBService extends Service implements SensorEventListener{
    private Handler handler;
    private SensorManager sensorManager;
    private Sensor stepSensor;

    private BeaconManager beaconManager;

    private Region groundZero;
    private Beacon beaconX; // gray
    private Beacon beaconY; // purple
    private Beacon beaconZ; // blue

    //private LiveCard liveCard;

    private static final String TAG = "EBService";

    private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";

    private static final int beaconXMajor = 28945;
    private static final int beaconXMinor = 0;

    private static final int beaconYMajor = 28945;
    private static final int beaconYMinor = 1;

    private static final int beaconZMajor = 28945;
    private static final int beaconZMinor = 2;

    private static final double minThreshold = 3;



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



        // TODO add sensor data to stop/start beacon scanning
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);

        groundZero = new Region("epfl", ESTIMOTE_PROXIMITY_UUID, null, null);
        beaconManager = new BeaconManager(getApplicationContext());

        // runs the ranging scan every 5 seconds
        beaconManager.setForegroundScanPeriod(1000, 4000);

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Beacon nearest = getNearestBeacon(beacons);
                        if(nearest == null) {
                            showNotification("All beacons out of range");
                        } else {
                            // TODO: call task
                            showNotification(getBeaconID(nearest));
                        }
                        /*if(nearest.getMinor() == 0) {
                                showNotification("X");
                            } else if(nearest.getMinor() == 1) {
                                showNotification("Y");
                            } else if(nearest.getMinor() == 2) {
                                showNotification("Z");
                                showNotification(nearest.toString());
                            }
                        }*/
                    }
                });
            }
        });
    }

    /**
     * connect BeaconManager to service and start ranging
     */
    private void startScanning(){
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    //beaconManager.startMonitoring(houseRegion);
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
    private void stopScanning(){
        try {
            //beaconManager.stopMonitoring(houseRegion);
            beaconManager.stopRanging(groundZero);
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot stop but it does not matter now", e);
        }
    }

    private void showNotification(String msg) {
        // TODO
        Log.i(TAG, msg);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        beaconManager.disconnect();
    }


    /**
     * Start and stop scanning based on voice activated command
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
/*        ArrayList<String> voiceResults = intent.getExtras().getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
        for (String voice : voiceResults) {
            Log.d(TAG, "voiceResults:voice = " + voice);
            if (voice.contains("stop")){
                Log.d(TAG,"stopScanning");
                stopScanning();
            }else if (voice.contains("start")){
                Log.d(TAG,"startScanning");
                startScanning();
            }
        }*/
        startScanning();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub

    }

    private Beacon getNearestBeacon(List<Beacon> beacons) {
        double min = minThreshold;
        Beacon nearest = null;

        for(Beacon b: beacons) {
            double distance = Utils.computeAccuracy(b);
            // checks it's within the minimum Threshold (not too far)
            if(distance < minThreshold && distance < min) {
                min = distance;
                nearest = b;
            }
        }
        return nearest;
    }

    private String getBeaconID(Beacon beacon) {
        return beacon.getProximityUUID() + "_" + beacon.getMajor() + "_" + beacon.getMinor();
    }

}