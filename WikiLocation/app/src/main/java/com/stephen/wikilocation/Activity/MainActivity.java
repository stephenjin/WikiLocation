package com.stephen.wikilocation.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.stephen.wikilocation.Model.Data;
import com.stephen.wikilocation.R;
import com.stephen.wikilocation.REST.ServiceGenerator;
import com.stephen.wikilocation.REST.WikipediaClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

        private static final String TAG = "MainActivity";
        GoogleApiClient googleApiClient = null;
        private WikipediaClient client;
        private Data reply = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //create the API client
        client = ServiceGenerator.createService(WikipediaClient.class);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d(TAG, "Connected to GoogleApiClient");
                        startLocationMonitoring();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d(TAG, "Suspended connection to GoogleApiClient");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d(TAG, "Suspended connection to GoogleApiClient");
                    }
                })
                .build();
        

    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();

        int response = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if(response != ConnectionResult.SUCCESS){
            //Google Play Services not available - show dialog to ask user to download it
            GoogleApiAvailability.getInstance().getErrorDialog(this, response, 1).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendApiRequest(String coordinates){

        Call<Data> call = client.getArticlesNearby("query","geosearch",10000,coordinates,"json");

        call.enqueue(new Callback<Data>() {
            @Override
            public void onResponse(Call<Data> call, Response<Data> response) {
                reply = response.body();
                Log.d(TAG, "onResponse: ");
            }

            @Override
            public void onFailure(Call<Data> call, Throwable t) {
                Log.d(TAG, "onFailure: ");
            }
        });
    }

    private void startLocationMonitoring(){
        try{
            LocationRequest locationRequest = LocationRequest.create()
                    .setInterval(10000)
                    .setInterval(5000)
                    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, new com.google.android.gms.location.LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    String coord = lat+"|"+lon;
                    sendApiRequest(coord);
                }
            });
        }catch(SecurityException e){
            Log.d(TAG, "SecurityException: " + e.getMessage());
        }
    }
}
