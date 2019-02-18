package com.android.toseefkhan.pandog.Map;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.toseefkhan.pandog.Profile.ProfileActivity;
import com.android.toseefkhan.pandog.Profile.ViewProfileActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.android.toseefkhan.pandog.models.User;
import com.android.toseefkhan.pandog.models.UserDistance;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MapRecyclerViewAdapter2 extends RecyclerView.Adapter<MapRecyclerViewAdapter2.ViewHolder>{

    private static final String TAG = "MapRecyclerViewAdapter2";
    private Context mContext;
    private ArrayList<UserDistance> mUserList;

    public MapRecyclerViewAdapter2(Context mContext, ArrayList<UserDistance> mUserList) {
        this.mContext = mContext;
        this.mUserList = mUserList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        Log.d(TAG, "onBindViewHolder: received user: " + mUserList.get(position));

        holder.setIsRecyclable(false);

        UniversalImageLoader.setImage(mUserList.get(position).getUser().getProfile_photo(),holder.pp,null,"",holder.child);

        if (mUserList.get(position).getUser().getLevel().equals("BLACK"))
            holder.bestTag.setVisibility(View.VISIBLE);

        holder.userName.setText(mUserList.get(position).getUser().getUsername());

        holder.email.setText(mUserList.get(position).getUser().getEmail());

        holder.userDistance.setText(String.valueOf(mUserList.get(position).getDistance()) + " km");

        holder.main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i= new Intent(mContext, ViewProfileActivity.class);
                i.putExtra(mContext.getString(R.string.intent_user), mUserList.get(position).getUser());
                mContext.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        RelativeLayout main;
        CircleImageView pp,bestTag;
        TextView userName, email;
        ProgressBar pb;
        View child;
        TextView userDistance;

        public ViewHolder(View itemView) {
            super(itemView);

            pp= itemView.findViewById(R.id.UserProfilePictureView);
            userName= itemView.findViewById(R.id.UserNameView);
            email= itemView.findViewById(R.id.UserEmailView);
            pb = itemView.findViewById(R.id.pb);
            pb.setVisibility(View.GONE);
            bestTag = itemView.findViewById(R.id.bestTag);
            main = itemView.findViewById(R.id.main);
            child = itemView.findViewById(R.id.progress_child);
            userDistance = itemView.findViewById(R.id.UserDistanceView);
            userDistance.setVisibility(View.VISIBLE);
        }
    }
}
