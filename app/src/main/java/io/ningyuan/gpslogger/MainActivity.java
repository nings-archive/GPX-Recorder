package io.ningyuan.gpslogger;

import android.app.Service;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private GpsPermissionHandler GpsPermissions = new GpsPermissionHandler();

    private TextView tv_latitude, tv_longitude;
    private Button btn_record, btn_stop, btn_pause;
    private LocationManager locationManager;
    private Location location;
    private Boolean is_recording = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_latitude = (TextView) findViewById(R.id.lat_content);
        tv_longitude = (TextView) findViewById(R.id.lng_content);
        btn_record = (Button) findViewById(R.id.btn_record);
        btn_stop = (Button) findViewById(R.id.btn_stop);
        btn_pause = (Button) findViewById(R.id.btn_pause);
        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);

        btn_pause.setEnabled(false);
        btn_stop.setEnabled(false);
        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
                Toast toast = Toast.makeText(MainActivity.this, "I'M WORKING ON IT", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
            }
        });

        if (!GpsPermissions.isPermitted(this)) {
            GpsPermissions.ask(this);
        } else {
            initialiseLocation();
        }
    }

    private void startRecording() {
        is_recording = true;
        btn_record.setEnabled(false);
        btn_stop.setEnabled(true);
        btn_pause.setEnabled(true);
    }

    private void stopRecording() {
        is_recording = false;
        btn_record.setEnabled(true);
        btn_stop.setEnabled(false);
        btn_pause.setEnabled(false);
    }

    private void initialiseLocation () {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 2, this);
        if (locationManager != null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (location != null) {
                    showLocationDMS(location);
                }
            }
        }
    }

    private void showLocationDMS (Location location) {
        tv_latitude.setText(GpxUtils.getDMS(location.getLatitude(), GpxUtils.LATITUDE));
        tv_longitude.setText(GpxUtils.getDMS(location.getLongitude(), GpxUtils.LONGITUDE));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == GpsPermissions.REQUEST_CODE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initialiseLocation();
            } else {
                Toast toast = Toast.makeText(MainActivity.this, "You must permit GPS permissions to use a GPS app!", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        showLocationDMS(location);
    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override public void onProviderEnabled(String provider) {}
    @Override public void onProviderDisabled(String provider) {
        Toast toast = Toast.makeText(MainActivity.this, "You must enable GPS to use a GPS app!", Toast.LENGTH_LONG);
        toast.show();
    }

}
