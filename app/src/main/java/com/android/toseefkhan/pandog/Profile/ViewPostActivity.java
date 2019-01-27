package com.android.toseefkhan.pandog.Profile;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.Heart;
import com.android.toseefkhan.pandog.Utils.InitialSetup;
import com.android.toseefkhan.pandog.Utils.InternetStatus;
import com.android.toseefkhan.pandog.Utils.Like;
import com.android.toseefkhan.pandog.Utils.PacmanDrawable;
import com.android.toseefkhan.pandog.Utils.SquareDrawable;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.android.toseefkhan.pandog.models.Comment;
import com.android.toseefkhan.pandog.models.Post;
import com.android.toseefkhan.pandog.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.koushikdutta.async.http.filter.DataRemainingException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewPostActivity extends AppCompatActivity {

    private static final String TAG = "ViewPostActivity";

    private CircleImageView dp1,dp2;
    private TextView username1,username2;
    private ImageView image1,image2;
    private TextView likesString1,likesString2;
    private boolean mLikedbyCurrentUser1 = false;
    private boolean mLikedbyCurrentUser2 = false;
    private int likesCount1 = 0;
    private int likesCount2 = 0;
    private TextView comments_list,comments_list2;
    private TextView caption1,caption2,timeRemaining;
    private ImageView heartWhite,heartWhite2,heartRed,heartRed2;
    private HorizontalScrollView horizontalScrollView;
    private LinearLayout theWholeView;
    private CardView cardView1;
    private CardView cardView2;
    private View child,child2;
    private int screenWidth,screenHeight;
    private LinearLayout view;

    private Context mContext = ViewPostActivity.this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_view_post_activity);
        setupWidgets();
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        screenHeight = mContext.getResources().getDisplayMetrics().heightPixels;

        Post post = getPostFromIntent();

        if (post != null) {
            showPost(post);
        }else if (post == null){

            if (getIntent().hasExtra("intent_post_key")){
                String postKey = getIntent().getExtras().getString("intent_post_key");
                getPostFromPostKey(postKey);
            }else {
                setContentView(R.layout.no_post_found);
                SquareDrawable indicator = new PacmanDrawable(new int[]{getResources().getColor(R.color.deep_orange_400), getResources().getColor(R.color.amber_400)
                        , getResources().getColor(R.color.lime_400), getResources().getColor(R.color.cyan_400)});
                indicator.setPadding(10);
                View child;
                child = findViewById(R.id.progress_child);
                child.setBackground(indicator);
                Animatable animatable = (Animatable) indicator;
                animatable.start();
            }
        }

        if (!InternetStatus.getInstance(this).isOnline()) {
            Snackbar.make(getWindow().getDecorView().getRootView(),"You are not online!",Snackbar.LENGTH_LONG).show();
        }
    }

    private void getPostFromPostKey(String postKey) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Posts")
                .child(postKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Post post = new Post();

                        HashMap<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();

                        post.setImage_url(objectMap.get("image_url").toString());
                        post.setImage_url2(objectMap.get("image_url2").toString());

                        post.setCaption(objectMap.get("caption").toString());
                        post.setCaption2(objectMap.get("caption2").toString());
                        post.setPhoto_id(objectMap.get("photo_id").toString());
                        post.setPhoto_id2(objectMap.get("photo_id2").toString());

                        post.setTags(objectMap.get("tags").toString());
                        post.setTags2(objectMap.get("tags2").toString());

                        post.setUser_id(objectMap.get("user_id").toString());
                        post.setUser_id2(objectMap.get("user_id2").toString());

                        post.setChallenge_id(objectMap.get("challenge_id").toString());
                        post.setStatus(objectMap.get("status").toString());
                        post.setTimeStamp(Long.parseLong(objectMap.get("timeStamp").toString()));

                        post.setPostKey(objectMap.get("postKey").toString());
                            /*String image_url, String caption, String photo_id, String user_id, String tags,
                String image_url2, String caption2, String photo_id2, String user_id2, String tags2*/

                        List<Like> likesList = new ArrayList<Like>();
                        for (DataSnapshot dSnapshot : dataSnapshot
                                .child("likes").getChildren()) {
                            Like like = new Like();
                            like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                            likesList.add(like);
                        }
                        post.setLikes(likesList);

                        List<Like> likesList2 = new ArrayList<Like>();
                        for (DataSnapshot dSnapshot : dataSnapshot
                                .child("likes2").getChildren()) {
                            Like like = new Like();
                            like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                            likesList2.add(like);
                        }
                        post.setLikes2(likesList2);

                        List<Comment> comments = new ArrayList<Comment>();
                        for (DataSnapshot dSnapshot : dataSnapshot
                                .child("comments").getChildren()) {
                            Comment comment = new Comment();
                            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                            comments.add(comment);
                        }
                        post.setComments(comments);

                        showPost(post);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void setupWidgets() {
        view = findViewById(R.id.view);
        dp1 = findViewById(R.id.profile_photo);
        dp2 = findViewById(R.id.profile_photo2);
        username1 = findViewById(R.id.username);
        username2 = findViewById(R.id.username2);
        image1 = findViewById(R.id.post_image);
        image2 = findViewById(R.id.post_image2);
        likesString1 = findViewById(R.id.image_likes);
        likesString2 = findViewById(R.id.image_likes2);
        comments_list = findViewById(R.id.comments_link);
        comments_list2 = findViewById(R.id.comments_link2);
        caption1 = findViewById(R.id.image_caption);
        caption2 = findViewById(R.id.image_caption2);
        heartWhite = findViewById(R.id.image_heart_white);
        heartWhite2 = findViewById(R.id.image_heart_white2);
        heartRed = findViewById(R.id.image_heart_red);
        heartRed2 = findViewById(R.id.image_heart_red2);
        horizontalScrollView = findViewById(R.id.horizontal_scroll_view);
        theWholeView = findViewById(R.id.theWholeView);
        cardView1 = findViewById(R.id.user1_card_view);
        cardView2 = findViewById(R.id.user2_card_view);
        timeRemaining = findViewById(R.id.timeRemaining);
        child = findViewById(R.id.progress_child);
        child2 = findViewById(R.id.progress_child2);

        heartRed.setVisibility(View.GONE);
        heartRed2.setVisibility(View.GONE);
        heartWhite.setVisibility(View.VISIBLE);
        heartWhite2.setVisibility(View.VISIBLE);
    }

    private Post getPostFromIntent() {

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            return bundle.getParcelable(getString(R.string.intent_post));
        }else{
            return null;
        }
    }

    private void showPost(Post post) {

        long timediff = System.currentTimeMillis() - post.getTimeStamp();
        int time = (int) ((86400000-timediff)/3600000);
        Log.d(TAG, "onBindViewHolder: the values of the hours " + time + " " + timediff);
        timeRemaining.setText(String.valueOf(time) + " hr remaining");

        theWholeView.setLayoutParams(new FrameLayout.LayoutParams(screenWidth*2,screenHeight));
        cardView1.setLayoutParams(new LinearLayout.LayoutParams(screenWidth,screenHeight));
        cardView2.setLayoutParams(new LinearLayout.LayoutParams(screenWidth,screenHeight));

        final ObjectAnimator animator= ObjectAnimator.ofInt(horizontalScrollView, "scrollX",screenWidth*2 );
        final ObjectAnimator animator2= ObjectAnimator.ofInt(horizontalScrollView, "scrollX",0 );
        animator.setDuration(200);
        animator2.setDuration(200);

        image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animator.start();
            }
        });
        image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animator2.start();
            }
        });

        Log.d(TAG, "onBindViewHolder: give me the post " + post);
        setTopToolbar(post);

        UniversalImageLoader.setImage(post.getImage_url(),image1,null,"",child);
        UniversalImageLoader.setImage(post.getImage_url2(),image2,null,"",child2);

        setLikesIcons(post);
        initLikesString(post);

        caption1.setText(post.getCaption());
        caption2.setText(post.getCaption2());

        comments_list.setText(String.valueOf(post.getComments().size()) + " comments");
        comments_list2.setText(String.valueOf(post.getComments().size()) + " comments");
        comments_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //takes you to ViewCommentsActivity
                Intent i = new Intent(mContext, ViewCommentsActivity.class);
                i.putExtra("post_comments",post);
                mContext.startActivity(i);
            }
        });
        comments_list2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //takes you to ViewCommentsActivity
                Intent i = new Intent(mContext,ViewCommentsActivity.class);
                i.putExtra("post_comments",post);
                mContext.startActivity(i);
            }
        });
    }

    private void initLikesString(Post post) {

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
                    likesString1.setText(String.valueOf(likesCount1)+ " Like");
                else
                    likesString1.setText(String.valueOf(likesCount1)+ " Likes");

                likesCount1 = 0;
                if (mLikedbyCurrentUser1){
                    heartRed.setVisibility(View.VISIBLE);
                    heartWhite.setVisibility(View.GONE);
                    mLikedbyCurrentUser1 = false;
                }else {
                    heartRed.setVisibility(View.GONE);
                    heartWhite.setVisibility(View.VISIBLE);
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
                    likesString2.setText(String.valueOf(likesCount2)+ " Like");
                else
                    likesString2.setText(String.valueOf(likesCount2)+ " Likes");

                likesCount2 = 0;
                if (mLikedbyCurrentUser2){
                    heartRed2.setVisibility(View.VISIBLE);
                    heartWhite2.setVisibility(View.GONE);
                    mLikedbyCurrentUser2 = false;
                }else {
                    heartRed2.setVisibility(View.GONE);
                    heartWhite2.setVisibility(View.VISIBLE);
                    mLikedbyCurrentUser2=false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setBoolean(Post post){
        ((InitialSetup)getApplicationContext()).wait = false;

        getLikesString(post);
    }

    private void getLikesString(Post post) {

        while (((InitialSetup)getApplicationContext()).wait){
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
                    likesString1.setText(String.valueOf(likesCount1)+ " Like");
                else
                    likesString1.setText(String.valueOf(likesCount1)+ " Likes");

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
                    likesString2.setText(String.valueOf(likesCount2)+ " Like");
                else
                    likesString2.setText(String.valueOf(likesCount2)+ " Likes");

                likesCount2 = 0;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ((InitialSetup)mContext.getApplicationContext()).wait = true;
    }

    private void setLikesIcons(Post post) {

        final Heart mHeart = new Heart(heartWhite,heartRed,heartWhite2,heartRed2,view,mContext,this);
        heartWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHeart.toggleLike(null,post);
                getLikesString(post);
            }
        });
        heartRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHeart.toggleLike(null,post);
                getLikesString(post);
            }
        });
        heartWhite2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHeart.toggleLike2(null,post);
                getLikesString(post);
            }
        });
        heartRed2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHeart.toggleLike2(null,post);
                getLikesString(post);
            }
        });
    }

    private void setTopToolbar(Post post) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child(mContext.getString(R.string.dbname_users))
                .child(post.getUser_id())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final User user = dataSnapshot.getValue(User.class);
                        UniversalImageLoader.setImage(Objects.requireNonNull(user).getProfile_photo(), dp1, null, "", null);
                        username1.setText(user.getUsername());

                        if (!user.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                            username1.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(mContext, ViewProfileActivity.class);
                                    i.putExtra(mContext.getString(R.string.intent_user), user);
                                    mContext.startActivity(i);
                                }
                            });
                            dp1.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(mContext, ViewProfileActivity.class);
                                    i.putExtra(mContext.getString(R.string.intent_user), user);
                                    mContext.startActivity(i);
                                }
                            });
                        } else {
                            username1.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(mContext, ProfileActivity.class);
                                    mContext.startActivity(i);
                                }
                            });
                            dp1.setOnClickListener(new View.OnClickListener() {
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
                        UniversalImageLoader.setImage(Objects.requireNonNull(user).getProfile_photo(), dp2, null, "", null);
                        username2.setText(user.getUsername());

                        if (!user.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                            username2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(mContext, ViewProfileActivity.class);
                                    i.putExtra(mContext.getString(R.string.intent_user), user);
                                    mContext.startActivity(i);
                                }
                            });
                            dp2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(mContext, ViewProfileActivity.class);
                                    i.putExtra(mContext.getString(R.string.intent_user), user);
                                    mContext.startActivity(i);
                                }
                            });

                        } else {

                            username2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(mContext, ProfileActivity.class);
                                    mContext.startActivity(i);
                                }
                            });
                            dp2.setOnClickListener(new View.OnClickListener() {
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

}
