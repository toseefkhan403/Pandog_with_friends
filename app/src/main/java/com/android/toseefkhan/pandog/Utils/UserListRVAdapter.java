package com.android.toseefkhan.pandog.Utils;

import android.app.Activity;
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
import com.android.toseefkhan.pandog.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserListRVAdapter extends RecyclerView.Adapter<UserListRVAdapter.ViewHolder>{

    private static final String TAG = "UserListRVAdapter";
    private Context mContext;
    private List<String> mUserIdList;
    private ProgressBar mProgressBar;

    public UserListRVAdapter(Context mContext, List<String> mUserIdList, ProgressBar pb) {
        this.mContext = mContext;
        this.mUserIdList = mUserIdList;
        this.mProgressBar = pb;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_item_with_follow, parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.whole.setVisibility(View.INVISIBLE);
        holder.setIsRecyclable(false);

        String userId = mUserIdList.get(position);
        Log.d(TAG, "onBindViewHolder: the user Ids who liked the photo " + userId);

        setUserDetails(holder,userId);

        holder.follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase.getInstance().getReference()
                        .child(mContext.getString(R.string.dbname_following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(userId)
                        .child(mContext.getString(R.string.field_user_id))
                        .setValue(userId);

                FirebaseDatabase.getInstance().getReference()
                        .child(mContext.getString(R.string.dbname_followers))
                        .child(userId)
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mContext.getString(R.string.field_user_id))
                        .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                setFollowing(holder);
            }
        });


        holder.unfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase.getInstance().getReference()
                        .child(mContext.getString(R.string.dbname_following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(userId)
                        .removeValue();

                FirebaseDatabase.getInstance().getReference()
                        .child(mContext.getString(R.string.dbname_followers))
                        .child(userId)
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .removeValue();
                setUnfollowing(holder);
            }
        });

    }

    private void setUserDetails(final ViewHolder viewHolder, String uid) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child(mContext.getString(R.string.dbname_users))
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: datasnapshot.getValue" + dataSnapshot.getValue());

                        if (dataSnapshot.exists()) {
                            final User user = dataSnapshot.getValue(User.class);
                            Log.d(TAG, "onDataChange: found the user " + user);

                            viewHolder.username.setText(user.getUsername());
                            viewHolder.email.setText(user.getEmail());
                            UniversalImageLoader.setImage(user.getProfile_photo(), viewHolder.profile_image, null, "", null);

                            if (!uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                viewHolder.username.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        Intent i = new Intent(mContext, ViewProfileActivity.class);
                                        i.putExtra(mContext.getString(R.string.intent_user), user);
                                        mContext.startActivity(i);
                                        ((Activity) mContext).finish();
                                    }
                                });

                                viewHolder.profile_image.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        Intent i = new Intent(mContext, ViewProfileActivity.class);
                                        i.putExtra(mContext.getString(R.string.intent_user), user);
                                        mContext.startActivity(i);
                                        ((Activity) mContext).finish();
                                    }
                                });

                                isFollowing(viewHolder, user);
                            } else {
                                viewHolder.username.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        Intent i = new Intent(mContext, ProfileActivity.class);
                                        mContext.startActivity(i);
                                    }
                                });

                                viewHolder.profile_image.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        Intent i = new Intent(mContext, ProfileActivity.class);
                                        mContext.startActivity(i);
                                    }
                                });

                                viewHolder.follow.setVisibility(View.GONE);
                                viewHolder.unfollow.setVisibility(View.GONE);
                                viewHolder.whole.setVisibility(View.VISIBLE);
                                mProgressBar.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void isFollowing(ViewHolder viewHolder, User user) {
        Log.d(TAG, "isFollowing: checking if following this users.");
        setUnfollowing(viewHolder);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(mContext.getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByChild(mContext.getString(R.string.field_user_id)).equalTo(user.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user:" + singleSnapshot.getValue());

                    setFollowing(viewHolder);
                }
                viewHolder.whole.setVisibility(View.VISIBLE);
                if (mProgressBar.getVisibility() == View.VISIBLE)
                    mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setFollowing(ViewHolder holder){
        Log.d(TAG, "setFollowing: updating UI for following this user");
        holder.follow.setVisibility(View.GONE);
        holder.unfollow.setVisibility(View.VISIBLE);
    }

    private void setUnfollowing(ViewHolder holder){
        Log.d(TAG, "setFollowing: updating UI for unfollowing this user");
        holder.follow.setVisibility(View.VISIBLE);
        holder.unfollow.setVisibility(View.GONE);
    }


    @Override
    public int getItemCount() {
        return mUserIdList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView profile_image;
        TextView username;
        TextView email;
        TextView follow, unfollow;
        RelativeLayout whole;

        public ViewHolder(View itemView) {
            super(itemView);

            profile_image = itemView.findViewById(R.id.UserProfilePictureView);
            username = itemView.findViewById(R.id.UserNameView);
            email = itemView.findViewById(R.id.UserEmailView);
            follow = itemView.findViewById(R.id.follow);
            unfollow = itemView.findViewById(R.id.unfollow);
            whole = itemView.findViewById(R.id.whole);

        }
    }
}
