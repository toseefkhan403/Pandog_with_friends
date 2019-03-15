package com.android.toseefkhan.pandog.Profile;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
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
import android.widget.Toast;

import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.Heart;
import com.android.toseefkhan.pandog.Utils.InitialSetup;
import com.android.toseefkhan.pandog.Utils.Like;
import com.android.toseefkhan.pandog.Utils.LottieFontViewGroupI;
import com.android.toseefkhan.pandog.Utils.LottieFontViewGroupN;
import com.android.toseefkhan.pandog.Utils.LottieFontViewGroupN2;
import com.android.toseefkhan.pandog.Utils.LottieFontViewGroupW;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class PostsProfileRVAdapter extends RecyclerView.Adapter<PostsProfileRVAdapter.ViewHolder>{

    public interface OnLoadMoreItemsListener{
        void onLoadMoreItems();
    }
    OnLoadMoreItemsListener mOnLoadMoreItemsListener;

    private static final String TAG = "PostsPRVAdapter";
    private Context mContext;
    private ArrayList<Post> mPostList;
    private boolean mLikedbyCurrentUser1 = false;
    private boolean mLikedbyCurrentUser2 = false;
    private int likesCount1 = 0;
    private int likesCount2 = 0;
    private int screenWidth;
    private int screenHeight;
    private ViewHolder mViewHolder;
    private boolean withoutScroll = false;

    public PostsProfileRVAdapter(Context mContext, ArrayList<Post> mPostList) {
        this.mContext = mContext;
        this.mPostList = mPostList;
        screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        screenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
    }

    public PostsProfileRVAdapter(Context mContext, ArrayList<Post> mPostList,boolean sth) {
        this.mContext = mContext;
        this.mPostList = mPostList;
        screenWidth = mContext.getResources().getDisplayMetrics().widthPixels/2;
        screenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
        withoutScroll = sth;
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

        if (withoutScroll){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_mainfeed_listitem2, parent, false);

            return new ViewHolder(view);
        }else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_mainfeed_listitem, parent, false);

            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder,final int position) {
        Log.d(TAG, "onBindViewHolder: called position " + position);

        final Post post = mPostList.get(position);

        long timediff = System.currentTimeMillis() - post.getTimeStamp();
        int time = (int) ((86400000 - timediff) / 3600000);

        //    int bottomHeight = 60 * (mContext.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);

        holder.theWholeView.setLayoutParams(new FrameLayout.LayoutParams(screenWidth * 2, screenHeight));
        holder.cardView1.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, screenHeight - (int)mContext.getResources().getDimension(R.dimen.bottom_view)));
        holder.cardView2.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, screenHeight - (int)mContext.getResources().getDimension(R.dimen.bottom_view)));

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
                i.putExtra("set_to_two",2);
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
                i.putExtra("post_comments",post.getPostKey());
                mContext.startActivity(i);
            }
        });
        holder.comments_list2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //takes you to ViewCommentsActivity
                Intent i = new Intent(mContext,ViewCommentsActivity.class);
                i.putExtra("post_comments",post.getPostKey());
                mContext.startActivity(i);
            }
        });

        setTimeAndCalculateResults(holder, time, post);

        if(reachedEndOfList(position)){
            loadMoreData();
        }
    }

    private void setTimeAndCalculateResults(ViewHolder holder, int time, Post post) {

        holder.timeRemaining.setVisibility(View.INVISIBLE);
        holder.timeRemaining.setText(String.valueOf(time) + " hr remaining");

        if (!post.getStatus().equals("INACTIVE")) {

            holder.heartHolder.setVisibility(View.VISIBLE);
            holder.heartHolder2.setVisibility(View.VISIBLE);

            if (post.getStatus().equals("ACTIVE") || post.getStatus().equals("AWAITING_RESULT")) {

                holder.image1.setAlpha(1f);
                holder.image2.setAlpha(1f);

                holder.tvWinner.setVisibility(View.INVISIBLE);
                holder.tvWinner2.setVisibility(View.INVISIBLE);
                holder.tvLoser.setVisibility(View.INVISIBLE);
                holder.tvLoser2.setVisibility(View.INVISIBLE);

                if (time <= 0) {

                    holder.heartHolder.setVisibility(View.GONE);
                    holder.heartHolder2.setVisibility(View.GONE);
                    holder.timeRemaining.setText("Awaiting result");
                    holder.timeRemaining.setVisibility(View.VISIBLE);

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                    ref.child("Posts")
                            .child(post.getPostKey())
                            .child("status")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    Log.d(TAG, "onBindViewHolder: i am setting the status to AWAITING_RESULT");

                                    String status = dataSnapshot.getValue(String.class);

                                    if (status.equals("ACTIVE")) {

                                        ref.child("Posts")
                                                .child(post.getPostKey())
                                                .child("status")
                                                .setValue("AWAITING_RESULT");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                } else {
                    holder.timeRemaining.setVisibility(View.VISIBLE);
                    setLikesIcons(holder,post);
                }
            }
        }else if (post.getStatus().equals("INACTIVE")) {

            holder.heartHolder.setVisibility(View.GONE);
            holder.heartHolder2.setVisibility(View.GONE);

            holder.tvWinner.setVisibility(View.INVISIBLE);
            holder.tvWinner2.setVisibility(View.INVISIBLE);
            holder.tvLoser.setVisibility(View.INVISIBLE);
            holder.tvLoser2.setVisibility(View.INVISIBLE);

            holder.image1.setAlpha(0.5f);
            holder.image2.setAlpha(0.5f);

            if (post.getWinner().equals("tie")) {
                holder.timeRemaining.setText("It's a draw!!");
                holder.timeRemaining.setVisibility(View.VISIBLE);

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
                                holder.timeRemaining.setVisibility(View.VISIBLE);
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
                                holder.timeRemaining.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
        }

    }

    private boolean reachedEndOfList(int position){
        return position == getItemCount() - 1;
    }

    private void loadMoreData(){

        try{
            mOnLoadMoreItemsListener = (OnLoadMoreItemsListener) mContext;
        }catch (ClassCastException e){
            Log.e(TAG, "loadMoreData: ClassCastException: " +e.getMessage() );
        }

        try{
            mOnLoadMoreItemsListener.onLoadMoreItems();
        }catch (Exception e){
            Log.e(TAG, "loadMoreData: ClassCastException: " +e.getMessage() );
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
                .addListenerForSingleValueEvent(new ValueEventListener() {
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
                                    i.putExtra(mContext.getString(R.string.intent_user), user);
                                    mContext.startActivity(i);
                                }
                            });
                            holder.dp1.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(mContext, ProfileActivity.class);
                                    i.putExtra(mContext.getString(R.string.intent_user), user);
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
                .addListenerForSingleValueEvent(new ValueEventListener() {
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
                                    i.putExtra(mContext.getString(R.string.intent_user), user);
                                    mContext.startActivity(i);
                                }
                            });
                            holder.dp2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(mContext, ViewProfileActivity.class);
                                    i.putExtra(mContext.getString(R.string.intent_user), user);
                                    i.putExtra(mContext.getString(R.string.intent_user), user);
                                    mContext.startActivity(i);
                                }
                            });

                        }else{

                            holder.username2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(mContext, ProfileActivity.class);
                                    i.putExtra(mContext.getString(R.string.intent_user), user);
                                    mContext.startActivity(i);
                                }
                            });
                            holder.dp2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(mContext, ProfileActivity.class);
                                    i.putExtra(mContext.getString(R.string.intent_user), user);
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
        return mPostList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

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
        RelativeLayout heartHolder, heartHolder2,tvWinner,tvWinner2
                ,tvLoser,tvLoser2,bottomView,bottomView2;

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
            bottomView = itemView.findViewById(R.id.bottomView);
            bottomView2 = itemView.findViewById(R.id.bottomView2);

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

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        mViewHolder = holder;
    }

    public void sharePost() {
        Log.d(TAG, "onLongClick: attempting to share the post ");

        if (mViewHolder != null) {
            Dialog shareImageDialog = new Dialog(mContext);
            shareImageDialog.setContentView(R.layout.layout_share_post_dialog);
            TextView saveGallery = shareImageDialog.findViewById(R.id.save_to_gallery);
            TextView otherApps = shareImageDialog.findViewById(R.id.other_apps);
            TextView cancel = shareImageDialog.findViewById(R.id.cancel);

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    shareImageDialog.dismiss();
                }
            });

            saveGallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    try {
                        File file = saveBitMap(mContext, mViewHolder.theWholeView);    //which view you want to pass that view as parameter
                        if (file != null) {
                            scanGallery(mContext, file.getAbsolutePath());
                            Toasty.success(mContext, "Post saved to gallery", Toast.LENGTH_SHORT, true).show();
                        } else {
                            Toasty.error(mContext, "Something went wrong, please try again!", Toast.LENGTH_SHORT, true).show();
                        }
                    } catch (IllegalStateException e) {
                        Log.d(TAG, "onClick: IllegalStateException" + e.getMessage());
                        Toasty.error(mContext, "Something went wrong. Please try again", Toasty.LENGTH_SHORT, true).show();
                    }

                }
            });

            otherApps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toasty.warning(mContext, "Attempting to share the post...", Toasty.LENGTH_LONG, true).show();

                    try {
                        File file = saveBitMap(mContext, mViewHolder.theWholeView);
                        MediaScannerConnection.scanFile(mContext,
                                new String[]{file.getAbsolutePath()}, null,
                                new MediaScannerConnection.OnScanCompletedListener() {
                                    public void onScanCompleted(String path, Uri uri) {
                                        Log.d("onScanCompleted", uri.getPath());

                                        Intent shareIntent = new Intent();
                                        shareIntent.setAction(Intent.ACTION_SEND);
                                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                        shareIntent.putExtra(Intent.EXTRA_TEXT, "Compete with your selfies using the Celfie app! \nRegister now : https://play.google.com/store/apps/details?id=com.android.toseefkhan.pandog \n");
                                        shareIntent.setType("image/jpg");
                                        mContext.startActivity(Intent.createChooser(shareIntent, "Share Celfie to..."));
                                    }
                                });
                    } catch (NullPointerException e) {
                        Log.d(TAG, "onClick: NullPointerException " + e.getMessage());
                        Toasty.error(mContext, "Something went wrong. Please try again", Toasty.LENGTH_SHORT, true).show();
                    } catch (IllegalStateException e) {
                        Log.d(TAG, "onClick: IllegalStateException " + e.getMessage());
                        Toasty.error(mContext, "Something went wrong. Please try again", Toasty.LENGTH_SHORT, true).show();
                    }
                }
            });

            shareImageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            shareImageDialog.show();
        }
    }

    private File saveBitMap(Context context, View drawView) throws IllegalStateException{

        try {
            File pictureFileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Handcare");
            if (!pictureFileDir.exists()) {
                boolean isDirectoryCreated = pictureFileDir.mkdirs();
                if (!isDirectoryCreated)
                    Log.d("TAG", "Can't create directory to save the image");
                return null;
            }
            String filename = pictureFileDir.getPath() + File.separator + System.currentTimeMillis() + ".jpg";
            File pictureFile = new File(filename);
            Bitmap bitmap = getBitmapFromView(drawView);
            try {
                pictureFile.createNewFile();
                FileOutputStream oStream = new FileOutputStream(pictureFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, oStream);
                oStream.flush();
                oStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("TAG", "There was an issue saving the image.");
            }

            return pictureFile;
        }catch (IllegalStateException e){
            Log.d(TAG, "saveBitMap: " + e.getMessage());
        }

        return null;
    }

    //create bitmap from view and returns it
    private Bitmap getBitmapFromView(View view) throws IllegalStateException{
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        }   else{
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    // used for scanning gallery
    private void scanGallery(Context cntx, String path) throws IllegalStateException{
        try {
            MediaScannerConnection.scanFile(cntx, new String[] { path },null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
