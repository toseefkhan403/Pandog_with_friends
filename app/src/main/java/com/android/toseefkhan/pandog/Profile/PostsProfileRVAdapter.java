package com.android.toseefkhan.pandog.Profile;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Search.HorizontalRecyclerViewAdapter;
import com.android.toseefkhan.pandog.Utils.Heart;
import com.android.toseefkhan.pandog.Utils.InitialSetup;
import com.android.toseefkhan.pandog.Utils.Like;
import com.android.toseefkhan.pandog.Utils.SquareImageView;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.android.toseefkhan.pandog.models.Post;
import com.android.toseefkhan.pandog.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsProfileRVAdapter extends RecyclerView.Adapter<PostsProfileRVAdapter.ViewHolder>{

    private static final String TAG = "PostsPRVAdapter";
    private Context mContext;
    private ArrayList<Post> mPostList;
    private View view;
    private boolean mLikedbyCurrentUser1 = false;
    private boolean mLikedbyCurrentUser2 = false;
    private int likesCount1 = 0;
    private int likesCount2 = 0;


    public PostsProfileRVAdapter(Context mContext, ArrayList<Post> mPostList) {
        this.mContext = mContext;
        this.mPostList = mPostList;
    }

    public PostsProfileRVAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setBoolean(ViewHolder viewHolder, Post post){
        ((InitialSetup)mContext.getApplicationContext()).wait = false;
        Log.d(TAG, "setBoolean: making sure u r not null " + viewHolder);

        getLikesString(viewHolder,post);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_mainfeed_listitem, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        Post post = mPostList.get(position);

        Log.d(TAG, "onBindViewHolder: give me the post " + post);
        setTopToolbar(holder, post);

        UniversalImageLoader.setImage(post.getImage_url(),holder.image1,null,"");
        UniversalImageLoader.setImage(post.getImage_url2(),holder.image2,null,"");

        setLikesIcons(holder,post);
        initLikesString(holder,post);

        holder.caption1.setText(post.getCaption());
        holder.caption2.setText(post.getCaption2());

        holder.comments_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //takes you to ViewCommentsFragment

            }
        });

    }

    private void initLikesString(final ViewHolder holder, Post post) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query = ref.child("Posts")
                .child(post.getPostKey())
                .child("likes");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot singleSnap : dataSnapshot.getChildren()){

                            Like like = singleSnap.getValue(Like.class);
                            if (like.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                               mLikedbyCurrentUser1 = true;
                            }

                           likesCount1++;
                            Log.d(TAG, "onDataChange: the first post has been liked by " +  like.getUser_id());
                        }
                        if (likesCount1==1)
                            holder.likesString1.setText(String.valueOf(likesCount1)+ " Like");
                        else
                            holder.likesString1.setText(String.valueOf(likesCount1)+ " Likes");

                        likesCount1 = 0;
                        if (mLikedbyCurrentUser1){
                            holder.heartRed.setVisibility(View.VISIBLE);
                            holder.heartWhite.setVisibility(View.GONE);
                            mLikedbyCurrentUser1 = false;
                        }else {
                            holder.heartRed.setVisibility(View.GONE);
                            holder.heartWhite.setVisibility(View.VISIBLE);
                            mLikedbyCurrentUser1=false;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        //same for second photo 2
        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference();
        Query query2 = ref2.child("Posts")
                .child(post.getPostKey())
                .child("likes2");
        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnap : dataSnapshot.getChildren()){

                    Like like = singleSnap.getValue(Like.class);
                    if (like.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        mLikedbyCurrentUser2 = true;
                    }

                    likesCount2++;
                    Log.d(TAG, "onDataChange: the first post has been liked by " +  like.getUser_id());
                }
                if (likesCount2==1)
                    holder.likesString2.setText(String.valueOf(likesCount2)+ " Like");
                else
                    holder.likesString2.setText(String.valueOf(likesCount2)+ " Likes");

                likesCount2 = 0;
                if (mLikedbyCurrentUser2){
                    holder.heartRed2.setVisibility(View.VISIBLE);
                    holder.heartWhite2.setVisibility(View.GONE);
                    mLikedbyCurrentUser2 = false;
                }else {
                    holder.heartRed2.setVisibility(View.GONE);
                    holder.heartWhite2.setVisibility(View.VISIBLE);
                    mLikedbyCurrentUser2=false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getLikesString(final ViewHolder holder, Post post) {

        while (((InitialSetup)mContext.getApplicationContext()).wait){
            Log.d(TAG, "getLikesString: i am waiting for wait to get false");
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query = ref.child("Posts")
                .child(post.getPostKey())
                .child("likes");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot singleSnap : dataSnapshot.getChildren()){
                            Like like = singleSnap.getValue(Like.class);

                            likesCount1++;
                            Log.d(TAG, "onDataChange: the first post has been liked by " +  like.getUser_id());
                        }
                        if (likesCount1==1)
                            holder.likesString1.setText(String.valueOf(likesCount1)+ " Like");
                        else
                            holder.likesString1.setText(String.valueOf(likesCount1)+ " Likes");

                        likesCount1 = 0;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        //same for second photo 2
        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference();
        Query query2 = ref2.child("Posts")
                .child(post.getPostKey())
                .child("likes2");
        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnap : dataSnapshot.getChildren()){

                    Like like = singleSnap.getValue(Like.class);

                    likesCount2++;
                    Log.d(TAG, "onDataChange: the first post has been liked by " +  like.getUser_id());
                }
                if (likesCount2==1)
                    holder.likesString2.setText(String.valueOf(likesCount2)+ " Like");
                else
                    holder.likesString2.setText(String.valueOf(likesCount2)+ " Likes");

                likesCount2 = 0;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ((InitialSetup)mContext.getApplicationContext()).wait = true;
    }

    private void setLikesIcons(final ViewHolder holder, final Post post) {

        final Heart mHeart = new Heart(holder.heartWhite,holder.heartRed,holder.heartWhite2,holder.heartRed2,view,mContext);
        holder.heartWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHeart.toggleLike(holder,post);
                getLikesString(holder,post);
            }
        });
        holder.heartRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHeart.toggleLike(holder,post);
                getLikesString(holder,post);
            }
        });
        holder.heartWhite2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHeart.toggleLike2(holder,post);
                getLikesString(holder,post);
            }
        });
        holder.heartRed2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHeart.toggleLike2(holder,post);
                getLikesString(holder,post);
            }
        });
    }

    private void setTopToolbar(final ViewHolder holder, Post post){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child(mContext.getString(R.string.dbname_users))
                .child(post.getUser_id())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final User user = dataSnapshot.getValue(User.class);
                        UniversalImageLoader.setImage(user.getProfile_photo(),holder.dp1,null,"");
                        holder.username1.setText(user.getUsername());

                        holder.username1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(mContext,ViewProfileActivity.class);
                                i.putExtra(mContext.getString(R.string.intent_user),user);
                                mContext.startActivity(i);
                            }
                        });
                        holder.dp1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(mContext,ViewProfileActivity.class);
                                i.putExtra(mContext.getString(R.string.intent_user),user);
                                mContext.startActivity(i);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        ref.child(mContext.getString(R.string.dbname_users))
                .child(post.getUser_id2())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final User user = dataSnapshot.getValue(User.class);
                        UniversalImageLoader.setImage(user.getProfile_photo(),holder.dp2,null,"");
                        holder.username2.setText(user.getUsername());

                        holder.username2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(mContext,ViewProfileActivity.class);
                                i.putExtra(mContext.getString(R.string.intent_user),user);
                                mContext.startActivity(i);
                            }
                        });
                        holder.dp2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(mContext,ViewProfileActivity.class);
                                i.putExtra(mContext.getString(R.string.intent_user),user);
                                mContext.startActivity(i);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return mPostList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView dp1,dp2;
        TextView username1,username2;
        SquareImageView image1,image2;
        TextView likesString1,likesString2;
        TextView comments_list;
        TextView caption1,caption2;
        ImageView heartWhite,heartWhite2,heartRed,heartRed2;

        public ViewHolder(View itemView) {
            super(itemView);

            dp1 = itemView.findViewById(R.id.profile_photo);
            dp2 = itemView.findViewById(R.id.profile_photo2);
            username1 = itemView.findViewById(R.id.username);
            username2 = itemView.findViewById(R.id.username2);
            image1 = itemView.findViewById(R.id.post_image);
            image2 = itemView.findViewById(R.id.post_image2);
            likesString1 = itemView.findViewById(R.id.image_likes);
            likesString2 = itemView.findViewById(R.id.image_likes2);
            comments_list = itemView.findViewById(R.id.image_comments_link);
            caption1 = itemView.findViewById(R.id.image_caption);
            caption2 = itemView.findViewById(R.id.image_caption2);
            heartWhite = itemView.findViewById(R.id.image_heart_white);
            heartWhite2 = itemView.findViewById(R.id.image_heart_white2);
            heartRed = itemView.findViewById(R.id.image_heart_red);
            heartRed2 = itemView.findViewById(R.id.image_heart_red2);

            heartRed.setVisibility(View.GONE);
            heartRed2.setVisibility(View.GONE);
            heartWhite.setVisibility(View.VISIBLE);
            heartWhite2.setVisibility(View.VISIBLE);
        }
    }
}
