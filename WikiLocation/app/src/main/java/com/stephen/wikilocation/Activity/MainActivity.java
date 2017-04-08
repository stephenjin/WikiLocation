package com.stephen.wikilocation.Activity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.stephen.wikilocation.Model.Article;
import com.stephen.wikilocation.Model.Data;
import com.stephen.wikilocation.Model.Thumbnail;
import com.stephen.wikilocation.R;
import com.stephen.wikilocation.REST.ServiceGenerator;
import com.stephen.wikilocation.REST.WikipediaClient;
import com.stephen.wikilocation.View.ArticleAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    GoogleApiClient googleApiClient = null;
    List<Geofence> mGeofenceList = null;
    PendingIntent pendingIntent;
    private WikipediaClient client;
    private Data reply = null;
    private ArticleAdapter adapter;
    private RecyclerView mRecyclerView;
    SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.article_recyclerView);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //create the API client
        client = ServiceGenerator.createService(WikipediaClient.class);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d(TAG, "Connected to GoogleApiClient");
                        updateLocation();
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




        //using a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);


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
        if (response != ConnectionResult.SUCCESS) {
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_refresh) {
            updateLocation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //make call to get nearby wikipedia articles
    private void sendApiRequest(String coordinates) {

        if(reply != null)
            stopGeofenceMonitoring(); //remove old geofences

        int num = preferences.getInt("NumResults", 10);
        Call<Data> call = client.getArticlesNearby("query", "geosearch", 10000, coordinates, num, "json");
        call.enqueue(new Callback<Data>() {
            @Override
            public void onResponse(Call<Data> call, Response<Data> response) {
                reply = response.body();
                List<Article> articles = reply.getQuery().getArticles();

                //get thumbnails and add then to the articles
                requestThumbnail(articles);

                    if(preferences.getBoolean("NotificationToggle", false)) {
                        //add articles to geofence list and start monitoring
                        startGeofenceMonitoring(articles);
                    }

            }

            @Override
            public void onFailure(Call<Data> call, Throwable t) {
                Log.d(TAG, "onFailure: ");
            }
        });
    }

    //get thumbnails for a list of articles
    private void requestThumbnail(final List<Article> articles) {
        int size = articles.size();
        String title = "";

        //prepare title query
        for(int i=0; i < size; i++){
            if(i != size-1)
                title += articles.get(i).getTitle() + "|";
            else
                title += articles.get(i).getTitle();
        }

        Call<Data> call = client.getThumbnailURL("query", "pageimages", "thumbnail", "json", 100, size, title);

          call.enqueue(new Callback<Data>() {
              @Override
              public void onResponse(Call<Data> call, Response<Data> response) {
                  Data d = response.body();
                  Map<String, Article> map = d.getQuery().getPageid();

                  for(Article curr : articles){
                      Thumbnail t = map.get(Integer.toString(curr.getPageid())).getThumbnail();
                      curr.setThumbnail(t);
                  }

                  //specify adapter for RecyclerView
                  adapter = new ArticleAdapter(reply.getQuery().getArticles());
                  mRecyclerView.setAdapter(adapter);

              }

              @Override
              public void onFailure(Call<Data> call, Throwable t) {

              }
          });


    }

    private void updateLocation() {
        //need to request permission at runtime for android sdk 23 and above
        if (Build.VERSION.SDK_INT >= 23){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET}, 10);
            return;
             }
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location != null){
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            String coord = lat+"|"+lon;
            sendApiRequest(coord);
        }

    }


    private void startLocationMonitoring(){
        try{
            LocationRequest locationRequest = LocationRequest.create()
                    .setInterval(600000)
                    .setFastestInterval(60000)
                    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, new com.google.android.gms.location.LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                }
            });
        }catch(SecurityException e){
            Log.d(TAG, "SecurityException: " + e.getMessage());
        }
    }

    private void startGeofenceMonitoring(List<Article> articles){

        try {
            mGeofenceList = new ArrayList<Geofence>();
            //create a geofence for every item in the list
            for (Article article : articles) {
                Geofence geofence = new Geofence.Builder()
                        .setRequestId(article.getTitle())
                        .setCircularRegion(article.getLat(), article.getLon(), 100)
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setNotificationResponsiveness(1000)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build();

                mGeofenceList.add(geofence);
            }
            //create request to trigger upon entering geofence radius
            GeofencingRequest geofenceRequest = new GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofences(mGeofenceList).build();

            Intent intent = new Intent(this, GeofenceService.class);
            pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            if(googleApiClient.isConnected()){
                LocationServices.GeofencingApi.addGeofences(googleApiClient, geofenceRequest, pendingIntent)
                        .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                if(status.isSuccess()){
                                    Log.d(TAG, "Added Geofence");
                                }
                                else{
                                    Log.d(TAG, "Failed to add geofence - "+status.getStatus());
                                }
                            }
                        });
            }
        }catch (SecurityException e){
                Log.d(TAG,"SecurityException -" + e.getMessage());
        }

    }

    private void stopGeofenceMonitoring(){

        List<String> ids = new ArrayList<String>();
        for(Article article: reply.getQuery().getArticles()){
            ids.add(article.getTitle());
        }

        LocationServices.GeofencingApi.removeGeofences(googleApiClient, ids)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        Log.d(TAG, "Geofences stopped");
                    }
                });
    }



}
