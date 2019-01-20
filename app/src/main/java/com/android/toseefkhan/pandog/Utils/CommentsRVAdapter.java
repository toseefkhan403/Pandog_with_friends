package com.android.toseefkhan.pandog.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.toseefkhan.pandog.Profile.EditProfileActivity;
import com.android.toseefkhan.pandog.Profile.ProfileActivity;
import com.android.toseefkhan.pandog.Profile.ViewProfileActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Search.HorizontalRecyclerViewAdapter;
import com.android.toseefkhan.pandog.models.Comment;
import com.android.toseefkhan.pandog.models.Post;
import com.android.toseefkhan.pandog.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsRVAdapter extends RecyclerView.Adapter<CommentsRVAdapter.ViewHolder>{

    private static final String TAG = "CommentsRVAdapter";
    private Context mContext;
    private ArrayList<Comment> mCommentsList;

    public CommentsRVAdapter(Context mContext, ArrayList<Comment> mCommentsList) {
        this.mContext = mContext;
        this.mCommentsList = mCommentsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Comment comment = mCommentsList.get(position);

        setUserDetails(holder,comment);

        holder.comment.setText(comment.getComment());

    }

    private void setUserDetails(final ViewHolder viewHolder, Comment comment) {

        final String uid = comment.getUser_id();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child(mContext.getString(R.string.dbname_users))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: datasnapshot.getValue" + dataSnapshot.getValue());

                        for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){

                            final User user = singleSnapshot.getValue(User.class);

                            if (user.getUser_id().equals(uid)){

                                Log.d(TAG, "onDataChange: found the user " + user);

                                viewHolder.comment_username.setText(user.getUsername());
                                UniversalImageLoader.setImage(user.getProfile_photo(), viewHolder.profile_image,null,"");

                                if (!uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                    viewHolder.comment_username.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            Intent i = new Intent(mContext, ViewProfileActivity.class);
                                            i.putExtra(mContext.getString(R.string.intent_user),user);
                                            mContext.startActivity(i);
                                        }
                                    });

                                    viewHolder.profile_image.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            Intent i = new Intent(mContext, ViewProfileActivity.class);
                                            i.putExtra(mContext.getString(R.string.intent_user),user);
                                            mContext.startActivity(i);
                                        }
                                    });
                                }
                                else{
                                    viewHolder.comment_username.setOnClickListener(new View.OnClickListener() {
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
                                }

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return mCommentsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView profile_image;
        TextView comment_username;
        TextView comment;


        public ViewHolder(View itemView) {
            super(itemView);

            profile_image = itemView.findViewById(R.id.comment_profile_image);
            comment_username = itemView.findViewById(R.id.comment_username);
            comment = itemView.findViewById(R.id.comment);
        }
    }
}
