package io.ningyuan.gpslogger;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by ning on 2/4/2017.
 */

public class GpxService extends Service implements LocationListener{
    private GpxFile gpxFile;
    private LocationManager locationManager;
    public boolean is_recording = false;
    final static String START_SERVICE = "io.ningyuan.gpxservice.action.start";
    final static String STOP_SERVICE = "io.ningyuan.gpxservice.action.stop";
    final int ONGOING_NOTIFICATION_ID = 2;
    final String LOG_TAG = "GpxService";

    class GpxServiceBinder extends Binder {
        public GpxService getService() {
            return GpxService.this;
        }
    }

    private IBinder mBinder = new GpxServiceBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return mBinder;
    }

    public void onCreate() {
        super.onCreate();
        is_recording = false;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(START_SERVICE)) {
            Toast toast = Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT);
            toast.show();

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 2, this);
            gpxFile = new GpxFile();
            if (locationManager == null) {
                Log.e(LOG_TAG, "locationManager == null");
            } else {
                Log.d(LOG_TAG, "locationManager != null");
            }

            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            Notification notification = new Notification.Builder(this)
                    .setContentTitle("G")
                    .setContentText("TEXT")
                    .setSmallIcon(R.drawable.small_icon)
                    .setContentIntent(pendingIntent)
                    .setTicker("TICKER")
                    .build();
            startForeground(ONGOING_NOTIFICATION_ID, notification);

            is_recording = true;
        } else if (intent.getAction().equals(STOP_SERVICE)) {
            Log.d(LOG_TAG, "Entered if equals STOP_SERVICE");
            gpxFile.save();
            locationManager.removeUpdates(this);
            Toast toast = Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT);
            toast.show();
            stopForeground(true);
            is_recording = false;
        }
        return START_STICKY;
    }

    @Override public void onLocationChanged (Location location) {
        Log.d("GpxRecorder", "onLocationChanged called");
        gpxFile.addGpsCoords(location);
    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override public void onProviderEnabled(String provider) {}
    @Override public void onProviderDisabled(String provider) {}
}
