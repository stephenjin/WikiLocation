package com.stephen.wikilocation.Activity;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.stephen.wikilocation.R;

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
                NotificationCompat.Builder builder= new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_wikipedia)
                        .setContentTitle("Nearby Location:")
                        .setContentText(geofence.getRequestId());

                //set action when notification is clicked
                String url = this.getString(R.string.baseURL) + geofence.getRequestId();
                Intent resultIntent = new Intent(this, WebViewActivity.class);
                //pass url to be loaded to webview activity
                resultIntent.putExtra("url", url);

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                stackBuilder.addParentStack(WebViewActivity.class);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(resultPendingIntent);

                Notification notification = builder.build();
                NotificationManagerCompat.from(this).notify(0, notification);

            }
        }
    }
}
