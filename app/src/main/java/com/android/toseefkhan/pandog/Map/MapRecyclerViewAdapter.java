package com.android.toseefkhan.pandog.Map;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.toseefkhan.pandog.Profile.EditProfileActivity;
import com.android.toseefkhan.pandog.Profile.ProfileActivity;
import com.android.toseefkhan.pandog.Profile.ViewProfileActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.android.toseefkhan.pandog.models.User;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MapRecyclerViewAdapter extends RecyclerView.Adapter<MapRecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "MapRecyclerViewAdapter";
    private Context mContext;
    private ArrayList<User> mUserList;
    private String uid;

    public MapRecyclerViewAdapter(Context mContext, ArrayList<User> mUserList) {
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

        //for pp

        UniversalImageLoader.setImage(mUserList.get(position).getProfile_photo(),holder.pp,holder.pb,"");

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (mUserList.get(position).getLevel().equals("BLACK"))
            holder.bestTag.setVisibility(View.VISIBLE);

        if (mUserList.get(position).getUser_id().equals(uid) )
            holder.userName.setText(mUserList.get(position).getUsername()+ " (You)");
        else
            holder.userName.setText(mUserList.get(position).getUsername());


        holder.email.setText(mUserList.get(position).getEmail());

        holder.main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!mUserList.get(position).getUser_id().equals(uid)){
                    Intent i= new Intent(mContext, ViewProfileActivity.class);
                    i.putExtra(mContext.getString(R.string.intent_user), mUserList.get(position));
                    mContext.startActivity(i);
                }
                else {
                    Intent i = new Intent(mContext, ProfileActivity.class);
                    mContext.startActivity(i);
                }
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

        public ViewHolder(View itemView) {
            super(itemView);

            pp= itemView.findViewById(R.id.UserProfilePictureView);
            userName= itemView.findViewById(R.id.UserNameView);
            email= itemView.findViewById(R.id.UserEmailView);
            pb = itemView.findViewById(R.id.pb);
            pb.setVisibility(View.VISIBLE);
            bestTag = itemView.findViewById(R.id.bestTag);
            main = itemView.findViewById(R.id.main);
        }
    }
}
