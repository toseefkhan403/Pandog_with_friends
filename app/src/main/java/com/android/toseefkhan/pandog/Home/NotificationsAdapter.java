package com.android.toseefkhan.pandog.Home;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

    @Override
    public void onBindViewHolder(@NonNull final NotificationViewHolder holder, int position) {

        final Challenge challenge = challengesList.get(position);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        UniversalImageLoader.setImage(challenge.getPhotoUrl(), holder.mCircleImageView, holder.pb, "");

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();


        Log.d(TAG, "onBindViewHolder: challenge " + challenge.getChallengerName() + challenge.getChallengedName());

        if (challenge.getChallengerUserUid().equals(uid)){
            holder.notifTextView.setText("You challenged "+challenge.getChallengedName());
            holder.status.setText("Awaiting response");
        }else if (challenge.getStatus().equals("NOT_DECIDED")){
            holder.notifTextView.setText(challenge.getChallengerName() + " challenged you!");

            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final Dialog dialog = new Dialog(mContext);
                    dialog.setContentView(R.layout.layout_response_dialog);
                    ImageView preview = dialog.findViewById(R.id.image_preview);
                    ProgressBar pb = dialog.findViewById(R.id.pb);

                    ImageLoader i = ImageLoader.getInstance();
                    i.displayImage(challenge.getPhotoUrl(),preview);
                    pb.setVisibility(View.GONE);

                    TextView username = dialog.findViewById(R.id.username);
                    username.setText(challenge.getChallengerName() + "?");

                    Button respondedYes = dialog.findViewById(R.id.respondedYes);
                    respondedYes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //todo set status of the challenge to ACCEPTED in the db and add checks at the beginning to set the holder.status of the challenge
                            //todo status can only be not_decided or rejected. if the challenge is accepted, the challenge itself has to be
                            //todo removed from the db and a new post will be created in the db under user_post and posts node.
                            getUserFromChallengerUid(challenge.getChallengerUserUid(), challenge.getChallengeKey());
        //                    holder.status.setText("You accepted the challenge");
                            dialog.dismiss();
                        }
                    });

                    Button respondedNo = dialog.findViewById(R.id.respondedNo);
                    respondedNo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            //todo set the status of the challenge to REJECTED in the db.
                            ref.child("Challenges").child(challenge.getChallengeKey()).child("status").setValue("REJECTED");
                            holder.status.setText("You rejected the challenge");
                        }
                    });

                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    if (!(holder.status.getText().equals("You rejected the challenge") || holder.status.getText().equals("You accepted the challenge")))
                        dialog.show();
                }
            });

        }

    }

    @Override
    public int getItemCount() {

        return (challengesList == null) ? 0 : challengesList.size();
    }

    private void getUserFromChallengerUid(final String uid, final String challengeKey){

        final ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(challengeKey);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child(mContext.getString(R.string.dbname_users))
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            chosen_user = dataSnapshot.getValue(User.class);
                            Log.d(TAG, "onDataChange: are you empty dude " + chosen_user);
                        if (chosen_user != null){
                            Intent intent = new Intent(mContext, ShareActivity.class);
                            intent.putExtra("chosen_user",chosen_user);
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

    public class NotificationViewHolder extends RecyclerView.ViewHolder {

        TextView notifTextView, status;
        CircleImageView mCircleImageView;
        ProgressBar pb;
        RelativeLayout container;

        public NotificationViewHolder(View itemView) {
            super(itemView);
            notifTextView = itemView.findViewById(R.id.NotifTextView);
            mCircleImageView = itemView.findViewById(R.id.NotifImageView);
            pb = itemView.findViewById(R.id.pb);
            status = itemView.findViewById(R.id.status);
            container = itemView.findViewById(R.id.container);
        }
    }
}
