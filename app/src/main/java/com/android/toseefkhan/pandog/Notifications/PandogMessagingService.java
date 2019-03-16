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

    private static int NOTIFICATION_ID = 0;

    @Override
    public void onNewToken(String token) {
        Log.d("TokenRegistration", token);
        addTokenToDevice(token);
    }

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived: remoteMessage " + remoteMessage);

        String notificationType = remoteMessage.getData().get("type");
        switch (notificationType) {
            case "Challenge": {
                String challengerUserUid = "";
                String status = "";
                final String notificationTitle = "";
                try {
                    status = remoteMessage.getData().get("status");
                    switch (status) {
                        case "NOT_DECIDED": {
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
                            break;
                        }
                        case "ACCEPTED": {
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
                            break;
                        }
                        case "REJECTED": {
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
                            break;
                        }
                    }
                } catch (NullPointerException exception) {
                    Log.d(TAG, "onMessageReceived: NullPointerException " + exception.getMessage());
                }
                break;
            }
            case "Following": {
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
                break;
            }
            case "RESULTS": {
                String notificationTitle;
                String notificationBody;
                String status = remoteMessage.getData().get("status");
                switch (status) {
                    case "win":
                        notificationTitle = "Congratulations";
                        notificationBody = "You won your challenge with ";
                        String loserUid = remoteMessage.getData().get("loser");
                        FirebaseDatabase.getInstance().getReference()
                                .child(getApplicationContext().getString(R.string.dbname_users))
                                .child(loserUid)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            User user = dataSnapshot.getValue(User.class);
                                            String username = user.getUsername();
                                            buildNotification(
                                                    remoteMessage, notificationTitle,
                                                    notificationBody + username, user
                                            );
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.d("DB ERROR", databaseError.toString());
                                    }
                                });
                        break;
                    case "lose":
                        notificationTitle = "Better luck next Time";
                        notificationBody = "You lost your challenge with ";
                        String winnerUid = remoteMessage.getData().get("winner");
                        FirebaseDatabase.getInstance().getReference()
                                .child(getApplicationContext().getString(R.string.dbname_users))
                                .child(winnerUid)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            User user = dataSnapshot.getValue(User.class);
                                            String username = user.getUsername();
                                            buildNotification(
                                                    remoteMessage, notificationTitle,
                                                    notificationBody + username, user
                                            );
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.d("DB ERROR", databaseError.toString());
                                    }
                                });
                        break;
                    case "draw":
                        notificationTitle = "It's a draw";
                        notificationBody = "Your challenge with # ended in a draw";
                        String userUid = remoteMessage.getData().get("user2");
                        FirebaseDatabase.getInstance().getReference()
                                .child(getApplicationContext().getString(R.string.dbname_users))
                                .child(userUid)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            User user = dataSnapshot.getValue(User.class);
                                            String username = user.getUsername();
                                            buildNotification(remoteMessage, notificationTitle
                                                    , notificationBody.replace("#", username), user);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.d("DB ERROR", databaseError.toString());
                                    }
                                });
                        break;
                }
                break;
            }
            case "mention": {
                String notificationTitle = "Mentioned";
                String notificationBody = "you were mentioned in a # by @";
                String mentionedPlace = remoteMessage.getData().get("mentionedPlace");
                String mentioningUserUid = remoteMessage.getData().get("userUid");
                FirebaseDatabase.getInstance().getReference()
                        .child(getApplicationContext().getString(R.string.dbname_users))
                        .child(mentioningUserUid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    User user = dataSnapshot.getValue(User.class);
                                    String userName = user.getUsername();
                                    buildNotification(remoteMessage, notificationTitle
                                            , notificationBody.replace("#", mentionedPlace).replace("@", userName), user);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.d("DB ERROR", databaseError.toString());
                            }
                        });
                break;
            }
            case "comment": {
                String NotificationTitle = "Comment";
                String NotificationBody = "@ commented on your post";
                String uid = remoteMessage.getData().get("userUid");
                FirebaseDatabase.getInstance().getReference()
                        .child(getApplicationContext().getString(R.string.dbname_users))
                        .child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    User user = dataSnapshot.getValue(User.class);
                                    String userName = user.getUsername();
                                    buildNotification(remoteMessage, NotificationTitle,
                                            NotificationBody.replace("@", userName), user);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                break;
            }
            case "topPost": {
                String NotificationTitle = "Celfie of the day";
                String NotificationBody = "Click here to see Today's best Celfie";

                buildNotification(remoteMessage, NotificationTitle, NotificationBody, null);
                break;
            }
        }
    }

    private void buildNotification
            (RemoteMessage remoteMessage, String notificationTitle, String notificationBody, User user) {
        Log.d(TAG, "buildNotification: " + remoteMessage + notificationTitle + notificationBody + user);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                getString(R.string.default_notification_channel_id));

        Intent notifIntent = null;
        switch (remoteMessage.getData().get("type")) {
            case "Challenge":
                NOTIFICATION_ID = 1;
                switch (remoteMessage.getData().get("status")) {
                    case "NOT_DECIDED":
                        notifIntent = new Intent(this, HomeActivity.class);
                        notifIntent.putExtra("ChallengerUser", user);
                        break;
                    case "ACCEPTED":
                        notifIntent = new Intent(this, ViewPostActivity.class);
                        notifIntent.putExtra("ChallengedUser", user);
                        String postKey = remoteMessage.getData().get("postKey");
                        notifIntent.putExtra("intent_post_key", postKey);
                        break;
                    case "REJECTED":
                        notifIntent = new Intent(this, HomeActivity.class);
                        break;
                }
                break;
            case "Following":
                NOTIFICATION_ID = 2;
                notifIntent = new Intent(this, ViewProfileActivity.class);
                notifIntent.putExtra(getResources().getString(R.string.intent_user), user);
                break;
            case "RESULTS": {
                NOTIFICATION_ID = (int) System.currentTimeMillis();
                notifIntent = new Intent(this, ViewPostActivity.class);
                String postKey = remoteMessage.getData().get("postKey");
                notifIntent.putExtra("intent_post_key", postKey);
                break;
            }
            case "mention": {
                NOTIFICATION_ID = 4;
                notifIntent = new Intent(this, ViewPostActivity.class);
                String postKey = remoteMessage.getData().get("postKey");
                notifIntent.putExtra("intent_post_key", postKey);
                if (remoteMessage.getData().get("mentionedPlace").equals("comment")) {
                    notifIntent.putExtra("post_comments", postKey);
                }
                break;
            }
            case "comment": {
                NOTIFICATION_ID = 5;
                notifIntent = new Intent(this, ViewPostActivity.class);
                String postKey = remoteMessage.getData().get("postKey");
                Log.d(TAG, "buildNotification: getting the postkey " + postKey);
                notifIntent.putExtra("intent_post_key", postKey);
                notifIntent.putExtra("post_comments", postKey);
                break;
            }
            case "topPost": {
                NOTIFICATION_ID = 6;
                notifIntent = new Intent(this, ViewPostActivity.class);
                String postKey = remoteMessage.getData().get("postKey");
                notifIntent.putExtra("intent_post_key", postKey);
            }
        }
        notifIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int iUniqueId = (int) (System.currentTimeMillis() & 0xfffffff);

        PendingIntent notificationPendingIntent;
        notificationPendingIntent = PendingIntent.getActivity(
                this,
                iUniqueId,
                notifIntent,
                0
        );

        builder.setSmallIcon(R.mipmap.ic_logo_celfie)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(notificationTitle)
                .setColor(Color.RED)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentText(notificationBody)
                .setContentIntent(notificationPendingIntent)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);

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
            //   mNotificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            Log.d(TAG, "buildNotification: create notif " + mNotificationChannel);
            notificationManager.createNotificationChannel(mNotificationChannel);
        }

        builder.setChannelId(NOTIFICATION_CHANNEL_ID);          //very important to set channel id
        notificationManager.notify(NOTIFICATION_ID, builder.build());
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