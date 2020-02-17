package com.example.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessaging extends FirebaseMessagingService {
    NotificationCompat.Builder builder = new NotificationCompat.Builder(FirebaseMessaging.this)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Payment successful")
            .setContentText("Toll payment complete")
            .setAutoCancel(true);

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        //Log.i("Notification:","From " + remoteMessage.getFrom());
        if(remoteMessage.getNotification() != null) {
            FirebaseMessaging.this.notify(builder);
            startActivity(new Intent(getApplicationContext(),LoadingActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                                                   .putExtra("Activity",6));
            //Log.i("Notification:","Title:" + remoteMessage.getNotification().getTitle());
            //Log.i("Notification:","Message:" + remoteMessage.getNotification().getBody());
        }
        //if(remoteMessage.getData().size() > 0) {
        //    Log.i("Notification:",remoteMessage.getData().get("myKey"));
        //}
    }

    @Override
    public void onNewToken(@NonNull String s) {

    }

    public void notify(NotificationCompat.Builder builder) {
        Intent intent = new Intent(FirebaseMessaging.this,CompleteActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(FirebaseMessaging.this,0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0,builder.build());
        //notificationShown = true;
        //MapsActivity.notificationShown = true;
    }
}
