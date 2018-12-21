package com.android.toseefkhan.pandog.Notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.toseefkhan.pandog.Home.HomeActivity;
import com.android.toseefkhan.pandog.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class PandogMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        Log.e("TokenRegistration", token);
        addTokenToDevice(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String notificationType = "", notificationTitle = "", notificationBody = "";
        try {
            notificationType = remoteMessage.getData().get("notificationType");
            notificationBody = remoteMessage.getNotification().getBody();
            Log.e("Body", notificationBody);
            notificationTitle = remoteMessage.getNotification().getTitle();
        } catch (NullPointerException exception) {
            Log.e("remoteMessage", exception.getMessage());
        }
        buildNotification(remoteMessage, notificationType, notificationTitle, notificationBody);
    }

    private void buildNotification
            (RemoteMessage remoteMessage, String notificationType, String notificationTitle, String notificationBody) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                getString(R.string.default_notification_channel_id));

        Intent pendingIntent = new Intent(this, HomeActivity.class);
        pendingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent notificationPendingIntent = PendingIntent.getActivity(
                this,
                0,
                pendingIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setSmallIcon(R.drawable.ic_notifications)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(notificationTitle)
                .setColor(Color.RED)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentText(notificationBody);

        builder.setContentIntent(notificationPendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    private void addTokenToDevice(String token) {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("PandogPreference",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("FCMToken", token);
        editor.apply();
    }
}
