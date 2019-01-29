package com.android.toseefkhan.pandog.Notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import com.android.toseefkhan.pandog.Home.HomeActivity;
import com.android.toseefkhan.pandog.Profile.ViewPostActivity;
import com.android.toseefkhan.pandog.Profile.ViewProfileActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;


public class PandogMessagingService extends FirebaseMessagingService {

    private static final String TAG = "PandogMessagingService";
    private static final String NOTIFICATION_CHANNEL_ID = "1001010101";

    @Override
    public void onNewToken(String token) {
        Log.d("TokenRegistration", token);
        addTokenToDevice(token);
    }

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived: remoteMessage " + remoteMessage);
        String notificationType = remoteMessage.getData().get("type");
        if (notificationType.equals("Challenge")) {
            String challengerUserUid = "";
            String status = "";
            final String notificationTitle = "";
            try {
                status = remoteMessage.getData().get("status");
                if (status.equals("NOT_DECIDED")) {
                    Log.d(TAG, "not decided notif");
                    final String notificationBody = "challenged you!";
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
                } else if (status.equals("ACCEPTED")) {
                    Log.d(TAG, "accepted notif");
                    final String notificationBody = "accepted your challenge";
                    String challengedUserUid = remoteMessage.getData().get("challengedUserUid");
                    FirebaseDatabase.getInstance().getReference()
                            .child(getApplicationContext().getString(R.string.dbname_users))
                            .child(challengedUserUid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        User challengedUser = dataSnapshot.getValue(User.class);
                                        String username = challengedUser.getUsername();
                                        buildNotification(remoteMessage, notificationTitle + username,
                                                notificationBody, challengedUser);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.d("Db Error", databaseError.getMessage());
                                }
                            });
                } else if (status.equals("REJECTED")) {
                    Log.d(TAG, "rejected notif");
                    final String notificationBody = "rejected your challenge";
                    String challengedUserUid = remoteMessage.getData().get("challengedUserUid");
                    FirebaseDatabase.getInstance().getReference()
                            .child(getApplicationContext().getString(R.string.dbname_users))
                            .child(challengedUserUid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        User challengedUser = dataSnapshot.getValue(User.class);
                                        String username = challengedUser.getUsername();
                                        buildNotification(remoteMessage, notificationTitle + username,
                                                notificationBody, challengedUser);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.d("Db Error", databaseError.getMessage());
                                }
                            });
                }
            } catch (NullPointerException exception) {
                Log.d(TAG, "onMessageReceived: NullPointerException " + exception.getMessage());
            }
        } else if (notificationType.equals("Following")) {
            Log.d(TAG, "following notif");
            final String notificationTitle = "You received a new follower";
            final String notificationBody = "";
            String followerUserUid = "";
            followerUserUid = remoteMessage.getData().get("followerUserUid");
            FirebaseDatabase.getInstance().getReference()
                    .child(getApplicationContext().getString(R.string.dbname_users))
                    .child(followerUserUid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                User followerUser = dataSnapshot.getValue(User.class);
                                String followerName = followerUser.getUsername();
                                buildNotification(remoteMessage, notificationTitle, notificationBody + followerName, followerUser);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.d("Db Error", databaseError.getMessage());
                        }
                    });
        }

    }

    private void buildNotification
            (RemoteMessage remoteMessage, String notificationTitle, String notificationBody, User user) {

        Log.d(TAG, "buildNotification: " + remoteMessage + notificationTitle + notificationBody + user);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                getString(R.string.default_notification_channel_id));

        Intent notifIntent = null;
        if (remoteMessage.getData().get("type").equals("Challenge")) {
            if (remoteMessage.getData().get("status").equals("NOT_DECIDED")) {
                notifIntent = new Intent(this, HomeActivity.class);
                notifIntent.putExtra("ChallengerUser", user);
            } else if (remoteMessage.getData().get("status").equals("ACCEPTED")) {
                notifIntent = new Intent(this, ViewPostActivity.class);
                notifIntent.putExtra("ChallengedUser", user);
                String postKey = remoteMessage.getData().get("postKey");
                notifIntent.putExtra("intent_postKey", postKey);
            } else if (remoteMessage.getData().get("status").equals("REJECTED")) {
                notifIntent = new Intent(this, HomeActivity.class);
            }
        } else if (remoteMessage.getData().get("type").equals("Following")) {
            notifIntent = new Intent(this, ViewProfileActivity.class);
            notifIntent.putExtra(getResources().getString(R.string.intent_user), user);
        }
        notifIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent notificationPendingIntent = null;
        notificationPendingIntent = PendingIntent.getActivity(
                this,
                0,
                notifIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        builder.setSmallIcon(R.drawable.ic_logo)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(notificationTitle)
                .setColor(Color.RED)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentText(notificationBody)
                .setContentIntent(notificationPendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mNotificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    getResources().getString(R.string.default_notification_channel_id),
                    importance);
            mNotificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);

            mNotificationChannel.enableLights(true);
            mNotificationChannel.setLightColor(Color.YELLOW);

            mNotificationChannel.canShowBadge();

            mNotificationChannel.enableVibration(true);
            mNotificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            Log.d(TAG, "buildNotification: create notif " + mNotificationChannel);
            notificationManager.createNotificationChannel(mNotificationChannel);
        }

        builder.setChannelId(NOTIFICATION_CHANNEL_ID);          //very important to set channel id
        notificationManager.notify(0, builder.build());
    }

    private void addTokenToDevice(String token) {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("PandogPreference",
                Context.MODE_PRIVATE);
        Log.d(TAG, "addTokenToDevice: The new token that is being added to the device " + token);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("FCMToken", token);
        editor.apply();
    }
}