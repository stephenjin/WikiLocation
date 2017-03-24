package com.stephen.wikilocation.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    GoogleApiClient googleApiClient = null;
    private WikipediaClient client;
    private Data reply = null;
    private ArticleAdapter adapter;
    private RecyclerView mRecyclerView;

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


        mRecyclerView = (RecyclerView) findViewById(R.id.article_recyclerView);

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendApiRequest(String coordinates) {

        Call<Data> call = client.getArticlesNearby("query", "geosearch", 10000, coordinates, "json");
        call.enqueue(new Callback<Data>() {
            @Override
            public void onResponse(Call<Data> call, Response<Data> response) {
                reply = response.body();
                List<Article> articles = reply.getQuery().getArticles();

                //get thumbnails and add then to the articles
                new ThumbnailTask().execute(articles);
                Log.d(TAG, "onResponse: ");
            }

            @Override
            public void onFailure(Call<Data> call, Throwable t) {
                Log.d(TAG, "onFailure: ");
            }
        });
    }

    private void requestThumbnail(Article article, final int i) {
        String title = article.getTitle();
        Call<Data> call = client.getThumbnailURL("query", "pageimages", "thumbnail", "json", title);

        try {
            Data response = call.execute().body();
            Map<String, Article> map = response.getQuery().getPageid();
            //get first value from the map
            Thumbnail thumbnail = map.entrySet().iterator().next().getValue().getThumbnail();
            reply.getQuery().getArticles().get(i).setThumbnail(thumbnail);

        } catch (IOException e) {
            e.printStackTrace();
        }


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


        class ThumbnailTask extends AsyncTask<List<Article>, Void, Void> {


            @Override
            protected Void doInBackground(List<Article>... params) {
                List<Article> articles= params[0];

                for (int i = 0; i < articles.size(); i++) {
                    requestThumbnail(articles.get(i), i);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                //specify adapter for RecyclerView
                adapter = new ArticleAdapter(reply.getQuery().getArticles());
                mRecyclerView.setAdapter(adapter);
            }
        }

}
