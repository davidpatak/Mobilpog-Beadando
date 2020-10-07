package com.example.qr_catalog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


import android.content.Context;
import android.content.SharedPreferences;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private TextView textLatLong;
    private TextView textAddress;
    private ProgressBar progressBar;


    String valid_until = "2020/10/05/17:05:00";
    int scantimes = 0;

    private String neptunkod;


    @SuppressLint("StaticFieldLeak")
    public static TextView resulttextview, neptuntext;
    Button scan_btn, result_btn;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String nkod ="BATMAN";

    String prevStarted = "prevStarted";


    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedpreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        if (!sharedpreferences.getBoolean(prevStarted, false)) {

            // Innentől az én szerencsétlenkedésem

            final EditText nk = new EditText(this);
            nk.setHint("Neptun kód pl.: KGLGRA");
            new AlertDialog.Builder(this)
                    .setTitle("NEPTUN")
                    .setMessage("Add meg a neptun kódodat!")
                    .setView(nk)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String input = nk.getText().toString();
                            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            editor.putString(nkod,nk.getText().toString());

                            editor.apply();

                            nk.setText(input);
                        }
                    })
                    .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            finish();
                            System.exit(0);
                        }
                    })
                    .show();
            // Idáig az én szerencsétlenkedésem
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putBoolean(prevStarted, Boolean.TRUE);
            //ez is
            editor.putString(neptunkod, nk.getText().toString());
            //idáig
            editor.apply();

        } else {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putBoolean(prevStarted, Boolean.TRUE);
            editor.apply();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resulttextview = findViewById(R.id.qrcodetextview);
        //Test
        //neptuntext = findViewById(R.id.neptun);
        //SharedPreferences sharedpreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        //neptuntext.setText(sharedpreferences.getString(neptunkod, "Nkod"));
        //---idáig
        scan_btn = findViewById(R.id.buttonscan);
        result_btn = findViewById(R.id.buttonresult);

        textLatLong = findViewById(R.id.textLatLong);
        textAddress = findViewById(R.id.textAddress);
        progressBar = findViewById(R.id.progressBar);



        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),ScanCodeActivity.class));
                scantimes = scantimes+1;


            }

        });



        result_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scantimes == 0) {
                    Toast.makeText(MainActivity.this, "Engedély megtagadva", Toast.LENGTH_SHORT).show();
                }else {

                String input = (String) resulttextview.getText();

                valid_until = input.substring(input.length()-19,input.length());


                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_LOCATION_PERMISSION
                    );
                } else {
                    getCurrentLocation();
                }

            }}
        });


    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Engedély megtagadva", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void getCurrentLocation() {

        final Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
        final String dateTime = simpleDateFormat.format(calendar.getTime());



        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
        Date strDate = null;
        try {
            strDate = sdf.parse(valid_until);
        } catch (ParseException e) {
            e.printStackTrace();
        }



        progressBar.setVisibility(View.VISIBLE);

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        final Date finalStrDate = strDate;
        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {

                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                .removeLocationUpdates(this);
                        if (locationResult != null && locationResult.getLocations().size() > 0){
                            int latestLocationIndex = locationResult.getLocations().size() - 1;
                            double latitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                            double longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();
                            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                            neptunkod = sharedPreferences.getString(nkod, "BATMAN");

                            //textLatLong.setText(String.format("Latitude: %s \nLongitude: %s",latitude,longitude));
                            if (latitude > 47.086044 && latitude < 47.091333 && longitude > 17.906985 && longitude < 17.911985) {
                                if (new Date().after(finalStrDate)) {
                                    textAddress.setText(
                                            String.format("Lekésted az órát!\n")+neptunkod);

                                }
                                else {
                                    textAddress.setText(
                                            String.format("Sikeresen feliratkoztál a katalógusra!\n")+neptunkod);
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference myRef = database.getReference("message");

                                    myRef.setValue(neptunkod);
                                }



                            } else {
                                if (new Date().after(finalStrDate)) {
                                    textAddress.setText(
                                            String.format("Nem vagy az egyetem területén, de már ne indulj sehova!\nAz óra végetért. ")+neptunkod);

                                }
                                else {
                                    textAddress.setText(
                                            String.format("Nem vagy az egyetem területén!\n")+neptunkod);
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference myRef = database.getReference("message");
                                    myRef.setValue(neptunkod);
                                }
                            }
                        }

                        progressBar.setVisibility(View.GONE);

                    }
                }, Looper.getMainLooper());

    }
}