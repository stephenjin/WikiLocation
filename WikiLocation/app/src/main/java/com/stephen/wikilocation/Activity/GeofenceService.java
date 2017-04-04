package com.stephen.wikilocation.Activity;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;


public class GeofenceService extends IntentService {


    public GeofenceService() {
        super("GeofenceService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);

        if(event.hasError()){

        }
        else{
            int transition = event.getGeofenceTransition();
            List<Geofence> geofences = event.getTriggeringGeofences();
            Geofence geofence = geofences.get(0);

            if(transition == Geofence.GEOFENCE_TRANSITION_ENTER){

            }
        }
    }
}
