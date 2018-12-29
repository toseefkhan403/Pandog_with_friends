package com.android.toseefkhan.pandog.Notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.toseefkhan.pandog.Home.HomeActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class PandogMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        Log.e("TokenRegistration", token);
        addTokenToDevice(token);
    }

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        final String notificationTitle = "";
        final String notificationBody = "He wants to Challenge you";
        String challengerUserUid = "";
        String status = "";

        try {
            status = remoteMessage.getData().get("status");
            if (status.equals("NOT_DECIDED")) {
                challengerUserUid = remoteMessage.getData().get("challengerUserUid");
                FirebaseDatabase.getInstance().getReference()
                        .child(getApplicationContext().getString(R.string.dbname_users))
                        .child(challengerUserUid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    User challengerUser = dataSnapshot.getValue(User.class);
                                    String userName = challengerUser.getUsername();
                                    buildNotification(remoteMessage, notificationTitle + userName, notificationBody,
                                            challengerUser);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.d("Db Error", databaseError.getMessage());
                            }
                        });
            }
        } catch (NullPointerException exception) {
            Log.e("remoteMessage", exception.getMessage());
        }

    }

    private void buildNotification
            (RemoteMessage remoteMessage, String notificationTitle, String notificationBody, User user) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                getString(R.string.default_notification_channel_id));

        Intent pendingIntent = new Intent(this, HomeActivity.class);
        pendingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pendingIntent.putExtra("ChallengerUser", user);

        PendingIntent notificationPendingIntent = PendingIntent.getActivity(
                this,
                0,
                pendingIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setSmallIcon(R.mipmap.ic_launcher)
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
