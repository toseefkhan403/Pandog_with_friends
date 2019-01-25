package com.android.toseefkhan.pandog.Home;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.toseefkhan.pandog.Profile.ProfileActivity;
import com.android.toseefkhan.pandog.Profile.ViewProfileActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Share.ShareActivity;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.android.toseefkhan.pandog.models.Challenge;
import com.android.toseefkhan.pandog.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private static final String TAG = "NotificationsAdapter";
    private ArrayList<Challenge> challengesList;
    private Context mContext;
    private User chosen_user;

    public NotificationsAdapter(ArrayList<Challenge> challenges, Context ctx) {
        this.challengesList = challenges;
        this.mContext = ctx;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_notif_item, parent, false);

        return new NotificationViewHolder(itemView);
    }

    public int getIndexOfChallenge(String challengeKey) {
        for (Challenge challenge : challengesList) {
            if (challenge.getChallengeKey().equals(challengeKey)) {
                return challengesList.indexOf(challenge);
            }
        }
        return -1;
    }

    public boolean doesChallengeExist(String challengeKey) {
        for (Challenge challenge : challengesList) {
            if (challenge.getChallengeKey().equals(challengeKey)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void onBindViewHolder(@NonNull final NotificationViewHolder holder, int position) {

        final Challenge challenge = challengesList.get(position);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        //setting the constant widgets
        UniversalImageLoader.setImage(challenge.getPhotoUrl(), holder.notif_image_preview, null, "");

        //todo note: this list is meant for pending posts only. as soon as the user accepts or rejects the challenge, that challenge should go away from the list
        if (challenge.getStatus().equals("NOT_DECIDED")) {
            if (challenge.getChallengerUserUid().equals(uid)) {
                setPpChallenger(holder,challenge.getChallengedUserUid());
                holder.challenged_who.setText("You challenged this user");
                holder.button_holder.setVisibility(View.GONE);

                switch (challenge.getStatus()) {
                    case "NOT_DECIDED":
                        holder.status.setText("Awaiting response");
                        break;
                    case "ACCEPTED":
                        holder.status.setText("Your post is up for voting!");
                        //todo this case should not be in the list.
                        break;
                    case "REJECTED":
                        holder.status.setText("Your challenge was rejected");
                        //todo this case should be deleted as well - it should be not in the list.
                        break;
                }

            } else if (!challenge.getChallengerUserUid().equals(uid)) {
                setPpChallenger(holder,challenge.getChallengerUserUid());
                holder.challenged_who.setText("Challenged you");
                holder.button_holder.setVisibility(View.VISIBLE);
                holder.status.setVisibility(View.GONE);
                //todo as soon as the user accepts or rejects the challenge, it should go away. Immediately in the case of rejection,
                //todo and only when the user fully accepts the challenge by uploading the second photo.
                holder.respondedYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getUserFromChallengerUid(challenge.getChallengerUserUid(), challenge.getChallengeKey());
                    }
                });
                holder.respondedNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ref.child("Challenges").child(challenge.getChallengeKey()).child("status").setValue("REJECTED");
                        Intent i = new Intent(mContext,HomeActivity.class);
                        mContext.startActivity(i);
                    }
                });
            }
        }

        //tod
    }

    private void setPpChallenger(final NotificationViewHolder holder, String challengerUserUid) {

        Log.d(TAG, "setPpChallenger: setting the pp of the challenger");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child(mContext.getString(R.string.dbname_users))
                .child(challengerUserUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final User user = dataSnapshot.getValue(User.class);
                        Log.d(TAG, "onDataChange: user " + user);
                        UniversalImageLoader.setImage(user.getProfile_photo(),holder.pp,null,"");
                        holder.username.setText(user.getUsername());

                        if (!user.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            holder.pp.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent i = new Intent(mContext, ViewProfileActivity.class);
                                    i.putExtra(mContext.getString(R.string.intent_user), user);
                                    mContext.startActivity(i);
                                }
                            });

                            holder.username.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent i = new Intent(mContext, ViewProfileActivity.class);
                                    i.putExtra(mContext.getString(R.string.intent_user), user);
                                    mContext.startActivity(i);
                                }
                            });
                        }else if (user.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){

                            holder.pp.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent i = new Intent(mContext, ProfileActivity.class);
                                    mContext.startActivity(i);
                                }
                            });

                            holder.username.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent i = new Intent(mContext, ProfileActivity.class);
                                    mContext.startActivity(i);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    @Override
    public int getItemCount() {

        return (challengesList == null) ? 0 : challengesList.size();
    }

    private void getUserFromChallengerUid(final String uid, final String challengeKey) {

        final ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(challengeKey);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child(mContext.getString(R.string.dbname_users))
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            chosen_user = dataSnapshot.getValue(User.class);
                            Log.d(TAG, "onDataChange: are you empty dude " + chosen_user);
                            if (chosen_user != null) {
                                Intent intent = new Intent(mContext, ShareActivity.class);
                                intent.putExtra("chosen_user", chosen_user);
                                intent.putStringArrayListExtra("post_task", arrayList);
                                mContext.startActivity(intent);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(mContext, "An error occurred.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void changeList(ArrayList<Challenge> challengesList) {
        this.challengesList = challengesList;
        Log.d(TAG, "listChanged");
        Collections.reverse(this.challengesList);
        notifyDataSetChanged();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {

        CircleImageView pp;
        TextView username, challenged_who;
        LinearLayout button_holder;
        Button respondedYes, respondedNo;
        TextView status;
        ImageView notif_image_preview;


        public NotificationViewHolder(View itemView) {
            super(itemView);
            pp = itemView.findViewById(R.id.userppNotif);
            username = itemView.findViewById(R.id.usernameNotif);
            challenged_who = itemView.findViewById(R.id.challenged_who);
            button_holder = itemView.findViewById(R.id.button_holder);
            respondedYes = itemView.findViewById(R.id.respondedYes);
            respondedNo = itemView.findViewById(R.id.respondedNo);
            status = itemView.findViewById(R.id.status);
            notif_image_preview = itemView.findViewById(R.id.NotifImagePreView);
        }
    }
}
