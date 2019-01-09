package com.android.toseefkhan.pandog.Home;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.android.toseefkhan.pandog.models.Challenge;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private static final String TAG = "NotificationsAdapter";
    private ArrayList<Challenge> challengesList;
    private Context mContext;

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
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {

        Challenge challenge = challengesList.get(position);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        UniversalImageLoader.setImage(challenge.getPhotoUrl(), holder.mCircleImageView, holder.pb, "");

        holder.notifTextView.setText(challenge.getChallengerName() + " challenged you!");

        Log.d(TAG, "onBindViewHolder: challenge " + challenge.getChallengerName() + challenge.getChallengedName());

        if (challenge.getChallengerUserUid().equals(uid)){
            holder.notifTextView.setText("You challenged "+challenge.getChallengedName());
            holder.status.setText("Awaiting response");
        }

        holder.status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo pop up a dialog
            }
        });
    }

    @Override
    public int getItemCount() {

        return (challengesList == null) ? 0 : challengesList.size();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {

        TextView notifTextView, status;
        CircleImageView mCircleImageView;
        ProgressBar pb;

        public NotificationViewHolder(View itemView) {
            super(itemView);
            notifTextView = itemView.findViewById(R.id.NotifTextView);
            mCircleImageView = itemView.findViewById(R.id.NotifImageView);
            pb = itemView.findViewById(R.id.pb);
            status = itemView.findViewById(R.id.status);
        }
    }
}
