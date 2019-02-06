package com.android.toseefkhan.pandog.Profile;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.Heart;
import com.android.toseefkhan.pandog.Utils.InitialSetup;
import com.android.toseefkhan.pandog.Utils.Like;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.android.toseefkhan.pandog.Utils.ViewLikesActivity;
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

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class PostsProfileRVAdapter extends RecyclerView.Adapter<PostsProfileRVAdapter.ViewHolder>{

    private static final String TAG = "PostsPRVAdapter";
    private Context mContext;
    private ArrayList<Post> mPostList;
    private boolean mLikedbyCurrentUser1 = false;
    private boolean mLikedbyCurrentUser2 = false;
    private int likesCount1 = 0;
    private int likesCount2 = 0;

    private int screenWidth;
    private int screenHeight;

    public PostsProfileRVAdapter(Context mContext, ArrayList<Post> mPostList) {
        this.mContext = mContext;
        this.mPostList = mPostList;
        screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        screenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
    }

    public PostsProfileRVAdapter(Context mContext) {
        this.mContext = mContext;
        screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        screenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
    }

    public void setBoolean(ViewHolder viewHolder, Post post){
        ((InitialSetup)mContext.getApplicationContext()).wait = false;
        Log.d(TAG, "setBoolean: making sure u r not null " + viewHolder);

        getLikesString(viewHolder,post);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_mainfeed_listitem, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder,final int position) {

        final Post post = mPostList.get(position);
        holder.setIsRecyclable(false);
        Log.d(TAG, "onBindViewHolder: postLikes " + post.getLikes());
        Log.d(TAG, "onBindViewHolder: post " + post.getWinner());

        long timediff = System.currentTimeMillis() - post.getTimeStamp();
        int time = (int) ((86400000 - timediff) / 3600000);

        int bottomHeight = 60 * (mContext.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);

        holder.theWholeView.setLayoutParams(new FrameLayout.LayoutParams(screenWidth * 2, screenHeight - bottomHeight));
        holder.cardView1.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, screenHeight - bottomHeight));
        holder.cardView2.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, screenHeight - bottomHeight));

        final ObjectAnimator animator = ObjectAnimator.ofInt(holder.horizontalScrollView, "scrollX", screenWidth * 2);
        final ObjectAnimator animator2 = ObjectAnimator.ofInt(holder.horizontalScrollView, "scrollX", 0);
        animator.setDuration(800);
        animator2.setDuration(800);

        holder.image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animator.start();
            }
        });
        holder.image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animator2.start();
            }
        });

        setTopToolbar(holder, post);

        UniversalImageLoader.setImage(post.getImage_url(), holder.image1, null, "", holder.child);
        UniversalImageLoader.setImage(post.getImage_url2(), holder.image2, null, "", holder.child2);

        initLikesString(holder, post);

        holder.likesString1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigating to ViewLikesActivity");
                Intent i = new Intent(mContext, ViewLikesActivity.class);
                i.putExtra(mContext.getString(R.string.intent_post),post);
                mContext.startActivity(i);
            }
        });

        holder.likesString2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigating to ViewLikesActivity");
                Intent i = new Intent(mContext, ViewLikesActivity.class);
                i.putExtra(mContext.getString(R.string.intent_post),post);
                mContext.startActivity(i);
            }
        });

        if (!post.getCaption().isEmpty()){
            holder.caption1.setText(post.getCaption());
        }else
            holder.caption1.setVisibility(View.GONE);

        if (!post.getCaption2().isEmpty()){
            holder.caption2.setText(post.getCaption2());
        }else
            holder.caption2.setVisibility(View.GONE);

        holder.comments_list.setText(String.valueOf(post.getComments().size()) + " comments");
        holder.comments_list2.setText(String.valueOf(post.getComments().size()) + " comments");
        holder.comments_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //takes you to ViewCommentsActivity
                Intent i = new Intent(mContext, ViewCommentsActivity.class);
                i.putExtra("post_comments",post);
                mContext.startActivity(i);
            }
        });
        holder.comments_list2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //takes you to ViewCommentsActivity
                Intent i = new Intent(mContext,ViewCommentsActivity.class);
                i.putExtra("post_comments",post);
                mContext.startActivity(i);
            }
        });

        holder.timeRemaining.setText(String.valueOf(time) + " hr remaining");

        if (!post.getStatus().equals("INACTIVE")) {

            if (post.getStatus().equals("ACTIVE") || post.getStatus().equals("AWAITING_RESULT")) {
                if (time <= 0) {
                    holder.timeRemaining.setText("Awaiting result");
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                    ref.child("Posts")
                            .child(post.getPostKey())
                            .child("status")
                            .setValue("AWAITING_RESULT");

                    holder.heartHolder.setVisibility(View.GONE);
                    holder.heartHolder2.setVisibility(View.GONE);
                } else {
                    setLikesIcons(holder,post);
                }
            }
        }else if (post.getStatus().equals("INACTIVE")) {

            holder.heartHolder.setVisibility(View.GONE);
            holder.heartHolder2.setVisibility(View.GONE);

            holder.image1.setAlpha(0.5f);
            holder.image2.setAlpha(0.5f);

            if (post.getWinner().equals("tie")) {
                holder.timeRemaining.setText("It's a draw!!");

            } else if (post.getWinner().equals(post.getUser_id())) {

                holder.tvWinner.setVisibility(View.VISIBLE);
                holder.tvLoser2.setVisibility(View.VISIBLE);

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                ref.child(mContext.getString(R.string.dbname_users))
                        .child(post.getUser_id())
                        .child("username")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String username = dataSnapshot.getValue(String.class);
                                holder.timeRemaining.setText(username + " won the challenge");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

            } else if (post.getWinner().equals(post.getUser_id2())) {

                holder.tvWinner2.setVisibility(View.VISIBLE);
                holder.tvLoser.setVisibility(View.VISIBLE);

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                ref.child(mContext.getString(R.string.dbname_users))
                        .child(post.getUser_id2())
                        .child("username")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String username = dataSnapshot.getValue(String.class);
                                holder.timeRemaining.setText(username + " won the challenge");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }

        }
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

        final Heart mHeart = new Heart(holder.heartWhite,holder.heartRed,holder.heartWhite2,holder.heartRed2,holder.theWholeView,mContext,null);
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
                        UniversalImageLoader.setImage(user.getProfile_photo(),holder.dp1,null,"",null);
                        holder.username1.setText(user.getUsername());

                        if (!user.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                            holder.username1.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(mContext, ViewProfileActivity.class);
                                    i.putExtra(mContext.getString(R.string.intent_user), user);
                                    mContext.startActivity(i);
                                }
                            });
                            holder.dp1.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(mContext, ViewProfileActivity.class);
                                    i.putExtra(mContext.getString(R.string.intent_user), user);
                                    mContext.startActivity(i);
                                }
                            });
                        }else{
                            holder.username1.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(mContext, ProfileActivity.class);
                                    mContext.startActivity(i);
                                }
                            });
                            holder.dp1.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
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

        ref.child(mContext.getString(R.string.dbname_users))
                .child(post.getUser_id2())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final User user = dataSnapshot.getValue(User.class);
                        UniversalImageLoader.setImage(user.getProfile_photo(),holder.dp2,null,"",null);
                        holder.username2.setText(user.getUsername());

                        if (!user.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                            holder.username2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(mContext, ViewProfileActivity.class);
                                    i.putExtra(mContext.getString(R.string.intent_user), user);
                                    mContext.startActivity(i);
                                }
                            });
                            holder.dp2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(mContext, ViewProfileActivity.class);
                                    i.putExtra(mContext.getString(R.string.intent_user), user);
                                    mContext.startActivity(i);
                                }
                            });

                        }else{

                            holder.username2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(mContext, ProfileActivity.class);
                                    mContext.startActivity(i);
                                }
                            });
                            holder.dp2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
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
        return mPostList.size() ;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView dp1,dp2;
        TextView username1,username2;
        ImageView image1,image2;
        TextView likesString1,likesString2;
        TextView comments_list,comments_list2;
        TextView caption1,caption2,timeRemaining;
        ImageView heartWhite,heartWhite2,heartRed,heartRed2;
        HorizontalScrollView horizontalScrollView;
        LinearLayout theWholeView;
        CardView cardView1;
        CardView cardView2;
        View child,child2;
        RelativeLayout heartHolder, heartHolder2,tvWinner,tvWinner2,tvLoser,tvLoser2;

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
            comments_list = itemView.findViewById(R.id.comments_link);
            comments_list2 = itemView.findViewById(R.id.comments_link2);
            caption1 = itemView.findViewById(R.id.image_caption);
            caption2 = itemView.findViewById(R.id.image_caption2);
            heartWhite = itemView.findViewById(R.id.image_heart_white);
            heartWhite2 = itemView.findViewById(R.id.image_heart_white2);
            heartRed = itemView.findViewById(R.id.image_heart_red);
            heartRed2 = itemView.findViewById(R.id.image_heart_red2);
            horizontalScrollView = itemView.findViewById(R.id.horizontal_scroll_view);
            theWholeView = itemView.findViewById(R.id.theWholeView);
            cardView1 = itemView.findViewById(R.id.user1_card_view);
            cardView2 = itemView.findViewById(R.id.user2_card_view);
            timeRemaining = itemView.findViewById(R.id.timeRemaining);
            child = itemView.findViewById(R.id.progress_child);
            child2 = itemView.findViewById(R.id.progress_child2);


            heartRed.setVisibility(View.GONE);
            heartRed2.setVisibility(View.GONE);
            heartWhite.setVisibility(View.VISIBLE);
            heartWhite2.setVisibility(View.VISIBLE);
            heartHolder = itemView.findViewById(R.id.heart_holder);
            heartHolder2 = itemView.findViewById(R.id.heart_holder2);

            tvWinner = itemView.findViewById(R.id.tvWinner);
            tvWinner2 = itemView.findViewById(R.id.tvWinner2);
            tvLoser = itemView.findViewById(R.id.tvLoser);
            tvLoser2 = itemView.findViewById(R.id.tvLoser2);
        }
    }
}
