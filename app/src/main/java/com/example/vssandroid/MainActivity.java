package com.example.vssandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.vssandroid.Constants;
import com.example.vssandroid.NetworkUtils;
import com.example.vssandroid.R;
import com.instacart.library.truetime.TrueTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private ImageView mCapture;
    private ProgressBar mProgressBar;
    private Spinner mSpinner;
    private Date date; // ntpDate;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeVariables();
        new Thread(new Runnable() {
            public void run() {
                try {
                    TrueTime.build()
                            .withNtpHost(Constants.NTP_IP) // This is also time.google.com
                            .initialize();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        mCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                glideImage();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            // Gets the IP address when an option is selected.
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final String url = mSpinner.getSelectedItem().toString();
                String newUrl;
                if (url == "Camera 1") {
                    newUrl = Constants.PORT_5000;
                } else {
                    newUrl = Constants.PORT_5000;
                }

                final String finalUrl = newUrl;

                // Needs to be run on different thread or else app freezes for a little bit
                new Thread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    public void run() {
                        NetworkUtils.url = finalUrl;
                        NetworkUtils.uuid = NetworkUtils.getCamera();
                    }
                }).start();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void initializeVariables() {
        mCapture = (ImageView) findViewById(R.id.captureButton);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mSpinner = (Spinner) findViewById(R.id.spinner);
        mProgressBar.setVisibility(View.INVISIBLE);
        setSpinner();
    }

    // Sets the spinner to the two IP address options
    private void setSpinner() {
        ArrayList<String> portList = new ArrayList<>();
        portList.add("Camera 1");
        portList.add("Camera 2");
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, portList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(spinnerAdapter);
    }

    // This function retrieves the six high-res images
    private void glideImage() {
        if (NetworkUtils.uuid == "null") {
            Toast.makeText(getApplicationContext(), "No cameras available", Toast.LENGTH_SHORT).show();
            return;
        }

        // This sets the progress bar to visible so the user knows that the app is getting the photo
        mProgressBar.setVisibility(View.VISIBLE);

        date = TrueTime.now();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                final String url = NetworkUtils.buildUrl(date, 3);

                // This gets the high-res images
                Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(url)
                        .listener(new RequestListener<Bitmap>() {

                            // If the load fails, a toast message appears
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap>
                                    target, boolean isFirstResource) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressBar.setVisibility(View.INVISIBLE);
                                        Toast.makeText(getApplicationContext(), "Cannot capture image",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return false;
                            }

                            // If the load succeeds, the next activity is started
                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target,
                                                           DataSource dataSource, boolean isFirstResource) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressBar.setVisibility(View.INVISIBLE);
                                    }
                                });

                                //creating the arrayList of image urls for the gallery
                                ArrayList<String> urlList = NetworkUtils.createUrlList(date);
                                Collections.reverse(urlList);
                                nextActivity(urlList);
                                return false;
                            }
                        })
                        .submit();
            }
        }, 3000);
    }

    // A function for the next activity to start. The url list is sent
    public void nextActivity(ArrayList<String> url) {
        Intent intent = new Intent(this, GalleryActivity.class);
        intent.putStringArrayListExtra("urlList", url);
        intent.putExtra("url", url);
        startActivity(intent);
    }
}
