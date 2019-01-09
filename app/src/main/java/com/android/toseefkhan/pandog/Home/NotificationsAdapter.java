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

        UniversalImageLoader.setImage(challenge.getPhotoUrl(), holder.mCircleImageView, holder.pb, "");

        Log.d(TAG, "onBindViewHolder: the challenge details " + challenge.getChallengedName() + challenge.getChallengerName());
        holder.notifTextView.setText(challenge.getChallengerName());
    }

    @Override
    public int getItemCount() {

        return (challengesList == null) ? 0 : challengesList.size();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {

        TextView notifTextView;
        CircleImageView mCircleImageView;
        ProgressBar pb;

        public NotificationViewHolder(View itemView) {
            super(itemView);
            notifTextView = itemView.findViewById(R.id.NotifTextView);
            mCircleImageView = itemView.findViewById(R.id.NotifImageView);
            pb = itemView.findViewById(R.id.pb);
        }
    }
}
