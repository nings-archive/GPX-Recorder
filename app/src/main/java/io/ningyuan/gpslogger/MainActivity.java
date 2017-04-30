package io.ningyuan.gpslogger;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private TextView tv_latitude, tv_longitude;
    private Button btn_record, btn_stop;
    private LocationManager locationManager;
    private Location location;
    private Intent gpxService;
    private final Criteria criteria = new Criteria();

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

        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        gpxService = new Intent(getApplicationContext(), GpxService.class);

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

        bindService();
    }

    protected void onPause() {
        Log.d(LOG_TAG, "onPause");
        super.onPause();
        locationManager.removeUpdates(this);
    }

    protected void onResume() {
        Log.d(LOG_TAG, "onResume");
        super.onResume();
        initialiseLocation();
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
                    setButtonEnabled();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                }
            };
            Log.d(LOG_TAG, "new ServiceConnection");
        }
        bindService(gpxService, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void startRecording() {
        Log.v(LOG_TAG, "startRecording()");
        if (!PermissionHandler.WriteisPermitted(this)) {
            PermissionHandler.askWrite(this);
        }
        Log.d(LOG_TAG, "else{} in startRecording()");
        gpxService.setAction(GpxService.START_SERVICE);
        startService(gpxService);
        toggleButtonEnabled();
    }

    private void stopRecording() {
        Log.d(LOG_TAG, "stopRecording()");
        gpxService.setAction(GpxService.STOP_SERVICE);
        startService(gpxService);
        toggleButtonEnabled();
    }

    private void setButtonEnabled() {
        if (myGpxService.is_recording) {
            btn_record.setEnabled(false);
            btn_stop.setEnabled(true);
        } else {
            btn_record.setEnabled(true);
            btn_stop.setEnabled(false);
        }
    }

    private void toggleButtonEnabled() {
        if (btn_record.isEnabled()) {
            btn_record.setEnabled(false);
            btn_stop.setEnabled(true);
        } else {
            btn_record.setEnabled(true);
            btn_stop.setEnabled(false);
        }
    }

     void initialiseLocation () {
         if (!PermissionHandler.GPSisPermitted(this)) {
             PermissionHandler.askGPS(this);
         } else {
             // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 2, this);
             locationManager.requestLocationUpdates(1000, 0, criteria, this, null);
             Log.d(LOG_TAG, "initialiseLocation: requestLocationUpdates");
             if (locationManager != null) {
                 location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                 if (location != null) {
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
