package io.ningyuan.gpslogger;

import android.Manifest;
import android.app.Service;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private TextView tv_latitude;
    private TextView tv_longitude;
    private LocationManager locationManager;
    private Location location;
    private final int REQUEST_LOCATION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_latitude = (TextView) findViewById(R.id.lat_content);
        tv_longitude = (TextView) findViewById(R.id.lng_content);
        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 2, this);
            if (locationManager != null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        }

        initialiseLocation();
    }

    private void initialiseLocation () {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (location != null) {
                tv_latitude.setText(String.valueOf(location.getLatitude()));
                tv_longitude.setText(String.valueOf(location.getLongitude()));
            } else {
                // TODO(1): showGpsDisabledAlert()
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 2, this);
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    initialiseLocation();
                } else {
                    // TODO(2): showAskForGps()
                }
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        tv_latitude.setText(String.valueOf(location.getLatitude()));
        tv_longitude.setText(String.valueOf(location.getLongitude()));
    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override public void onProviderEnabled(String provider) {}
    @Override public void onProviderDisabled(String provider) {/*TODO(2): showGPSDisabled...();))*/}

}
