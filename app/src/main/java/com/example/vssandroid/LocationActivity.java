package com.example.vssandroid;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.vssandroid.Constants;
import com.example.vssandroid.R;

import mumayank.com.airlocationlibrary.AirLocation;

public class LocationActivity extends AppCompatActivity /*implements LocationListener */{
    private Button locationButton;

    private double longitude;
    private double latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        locationButton = (Button) findViewById(R.id.button);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLocation();
            }
        });
    }

    // Checks if the location permission is present. If not, it requests the permission
    //  from the user.
    private boolean isPermissionPresent() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission")
                        .setMessage("Your location lets us know whether you're in the correct area to use this app.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(LocationActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }

            return false;
        } else {
            return true;
        }
    }


    // This checks if the user is within the correct location bounds to use the app.
    private void checkLocation() {
        if (isPermissionPresent()) {

            // AirLocation is used because it is more accurate than Androids Location Listener
            AirLocation airLocation = new AirLocation(this, true,
                    true, new AirLocation.Callbacks() {
                @Override
                public void onSuccess(Location location) {
                    // Gets the longitude and latitude of the location
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();

                    // Checks if the longitude and latitude are within 0.0005 degrees (100 m)
                    //  of the desired location

                    //SEPT 22. For testing purposes, making the location redundant with >= 0
                    if ((Math.abs(latitude - Constants.LATITUDE) >= 0 && Math.abs(longitude - Constants.LONGITUDE) >= 0)){
                        System.out.println("Latitude: " + latitude + "\nLongitude: " + longitude);

                        // Starts new activity if within bounds
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    }

                    else {
                        // Shows toast message if not in bounds
                        Toast.makeText(getApplicationContext(), "Not in bounds", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailed(AirLocation.LocationFailedEnum locationFailedEnum) {
                    System.out.println("failed");
                }
            });
        }
    }
}