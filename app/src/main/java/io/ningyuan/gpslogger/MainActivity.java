package io.ningyuan.gpslogger;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private TextView tv_latitude, tv_longitude;
    private Button btn_record, btn_stop, btn_pause;
    private LocationManager locationManager;
    private Location location;
    private Intent gpxService;

    private ServiceConnection serviceConnection;
    private GpxService myGpxService;

    private String LOG_TAG = "MainActivity";

    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_latitude = (TextView) findViewById(R.id.lat_content);
        tv_longitude = (TextView) findViewById(R.id.lng_content);
        btn_record = (Button) findViewById(R.id.btn_record);
        btn_stop = (Button) findViewById(R.id.btn_stop);
        btn_pause = (Button) findViewById(R.id.btn_pause);
        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        gpxService = new Intent(getApplicationContext(), GpxService.class);
        bindService();

        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
            }
        });
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
            }
        });

        if (!PermissionHandler.GPSisPermitted(this)) {
            PermissionHandler.askGPS(this);
        } else {
            initialiseLocation();
        }
    }

    private void bindService() {
        Log.d(LOG_TAG, "bindService");
        if (serviceConnection == null) {
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.d(LOG_TAG, "bindService: onServiceConnected");
                    GpxService.GpxServiceBinder gpxServiceBinder = (GpxService.GpxServiceBinder) service;
                    myGpxService = gpxServiceBinder.getService();

                    if (myGpxService.is_recording) {
                        btn_record.setEnabled(false);
                        btn_pause.setEnabled(false);
                        btn_stop.setEnabled(true);
                    } else {
                        btn_record.setEnabled(true);
                        btn_pause.setEnabled(false);
                        btn_stop.setEnabled(false);
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                }
            };
            Log.d(LOG_TAG, "new ServiceConnection");
        }

        bindService(gpxService, serviceConnection, Context.BIND_AUTO_CREATE);

        if (myGpxService == null) {
            Log.e(LOG_TAG, "bindService: myGpxService == null");
        } else {
            Log.d(LOG_TAG, "bindService: myGpxService != null");
        }
    }

    private void startRecording() {
        Log.v(LOG_TAG, "startRecording()");
        if (!PermissionHandler.WriteisPermitted(this)) {
            PermissionHandler.askWrite(this);
        } else {
            Log.d(LOG_TAG, "else{} in startRecording()");
            btn_record.setEnabled(false);
            btn_stop.setEnabled(true);
            btn_pause.setEnabled(true);
            // gpxFile = new GpxFile();

            gpxService.setAction(GpxService.START_SERVICE);
            startService(gpxService);
        }
    }

    private void stopRecording() {
        Log.d(LOG_TAG, "stopRecording()");
        btn_record.setEnabled(true);
        btn_stop.setEnabled(false);
        btn_pause.setEnabled(false);

        gpxService.setAction(GpxService.STOP_SERVICE);
        startService(gpxService);
    }

     void initialiseLocation () {
         locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 2, this);
         Log.d(LOG_TAG, "initialiseLocation: requestLocationUpdates");
         if (locationManager != null) {
             location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
             if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                 if (location != null) {
                    Log.d(LOG_TAG, "initialiseLocation: last if");
                    showLocationDMS(location);
                 }
             }
         }
    }

    private void showLocationDMS (Location location) {
        String lat_str = GpxUtils.getDMS(location.getLatitude(), GpxUtils.LATITUDE);
        String lon_str = GpxUtils.getDMS(location.getLongitude(), GpxUtils.LONGITUDE);
        tv_latitude.setText(lat_str);
        tv_longitude.setText(lon_str);
        Log.d(LOG_TAG, "showLocationDMS: " + lat_str + ", " + lon_str);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PermissionHandler.GPS_REQUEST_CODE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initialiseLocation();
            } else {
                Toast toast = Toast.makeText(MainActivity.this, "You must permit GPS permissions to use a GPS app!", Toast.LENGTH_LONG);
                toast.show();
            }
        }

        else if (requestCode == PermissionHandler.WRITE_REQUEST_CODE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // PERMISSION WAS GRANTED
            } else {
                Toast toast = Toast.makeText(MainActivity.this, "You must permit write permissions in order to save the data!", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        showLocationDMS(location);
        Log.d(LOG_TAG, "onLocationChanged");
    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override public void onProviderEnabled(String provider) {}
    @Override public void onProviderDisabled(String provider) {
        Toast toast = Toast.makeText(MainActivity.this, "You must enable GPS to use a GPS app!", Toast.LENGTH_LONG);
        toast.show();
    }

}
