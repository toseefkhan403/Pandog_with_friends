package com.android.toseefkhan.pandog.Profile;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.toseefkhan.pandog.Home.HomeActivity;
import com.android.toseefkhan.pandog.Utils.Heart;
import com.android.toseefkhan.pandog.Utils.ViewLikesActivity;
import com.github.tbouron.shakedetector.library.ShakeDetector;
import com.google.android.gms.common.api.Api;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.toseefkhan.pandog.R;
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
import com.nostra13.universalimageloader.core.ImageLoader;
import com.takusemba.spotlight.OnSpotlightStateChangedListener;
import com.takusemba.spotlight.OnTargetStateChangedListener;
import com.takusemba.spotlight.Spotlight;
import com.takusemba.spotlight.shape.Circle;
import com.takusemba.spotlight.target.CustomTarget;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionButton;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionHelper;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionLayout;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RFACLabelItem;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RapidFloatingActionContentLabelList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class ViewPostActivity extends AppCompatActivity implements RapidFloatingActionContentLabelList.OnRapidFloatingActionContentLabelListListener {

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
    private RelativeLayout heartHolder, heartHolder2;

    private Context mContext = ViewPostActivity.this;

    private RapidFloatingActionLayout rfaLayout;
    private RapidFloatingActionButton rfaBtn;
    private RapidFloatingActionHelper rfabHelper;

    SharedPreferences mPrefs;
    final String horizontalScreenEnabled = "horizontalScreenEnabled";
    final String showFloatingButton = "showFloatingButton";
    boolean horizontalScrollingEnabled;
    boolean isshowFloatingButton;


    @Override
    public void onRFACItemLabelClick(int position, RFACLabelItem item) {

    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    @Override
    public void onRFACItemIconClick(int position, RFACLabelItem item) {

        switch (position){

            case 0:
                Log.d(TAG, "onRFACItemIconClick: toggling horizontal off.");

                if (horizontalScrollingEnabled) {
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putBoolean(horizontalScreenEnabled, false);
                    editor.apply();
                }else{
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putBoolean(horizontalScreenEnabled, true);
                    editor.apply();
                }

                Intent i = new Intent(mContext, HomeActivity.class);
                startActivity(i);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                break;

            case 1:
                sharePost();
                break;

            case 2:
                Intent intent = new Intent(this, EditProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.pull,R.anim.push);
                break;

            case 3:
                spotlight();
                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putBoolean(showFloatingButton, false);
                editor.apply();
                rfaBtn.setVisibility(View.GONE);

                break;
        }

        rfabHelper.toggleContent();
    }

    private void spotlight() {

        View first = LayoutInflater.from(mContext).inflate(R.layout.overlay_shake_device, new FrameLayout(mContext));

        CustomTarget homeView = new CustomTarget.Builder(this)
                .setPoint(0f,0f)
                .setShape(new Circle(0f))
                .setOverlay(first)
                .setOnSpotlightStartedListener(new OnTargetStateChangedListener<CustomTarget>() {
                    @Override
                    public void onStarted(CustomTarget target) {
                        // do something
                    }
                    @Override
                    public void onEnded(CustomTarget target) {
                        // do something
                    }
                })
                .build();

        Spotlight spotlight = Spotlight.with(ViewPostActivity.this)
                .setOverlayColor(R.color.background)
                .setDuration(1000L)
                .setAnimation(new DecelerateInterpolator(2f))
                .setTargets(homeView)
                .setClosedOnTouchedOutside(true)
                .setOnSpotlightStateListener(new OnSpotlightStateChangedListener() {
                    @Override
                    public void onStarted() {

                    }

                    @Override
                    public void onEnded() {

                    }
                });
        spotlight.start();

    }

    /*
      pass to this activity either the post or the post_key as intent extra. It will show the post itself.
     */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        horizontalScrollingEnabled = mPrefs.getBoolean(horizontalScreenEnabled, true);
        isshowFloatingButton = mPrefs.getBoolean(showFloatingButton,true);
        if (horizontalScrollingEnabled) {
            //do the horizontal
            setContentView(R.layout.layout_view_post_activity);
            screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        }else{
            //do normal
            setContentView(R.layout.layout_view_post_activity_without_scroll);
            screenWidth = mContext.getResources().getDisplayMetrics().widthPixels/2;
        }

        setupWidgets();
        initImageLoader();

        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        screenHeight = mContext.getResources().getDisplayMetrics().heightPixels;

        rfaLayout = findViewById(R.id.activity_main_rfal);
        rfaBtn = findViewById(R.id.activity_main_rfab);

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

        if (isshowFloatingButton)
            setupFloatingButton();
        else
            rfaBtn.setVisibility(View.GONE);

        ShakeDetector.create(this, new ShakeDetector.OnShakeListener() {
            @Override
            public void OnShake() {
                Log.d(TAG, "OnShake: called");
                Toasty.success(getApplicationContext(), "Device shaken!", Toast.LENGTH_SHORT,false).show();

                mPrefs.getBoolean(showFloatingButton,true);
                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putBoolean(showFloatingButton, true);
                editor.apply();

                Intent i = new Intent(mContext,HomeActivity.class);
                startActivity(i);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        });
        ShakeDetector.updateConfiguration(2.0f,3);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ShakeDetector.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ShakeDetector.destroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ShakeDetector.stop();
    }

    private void setupFloatingButton() {

        RapidFloatingActionContentLabelList rfaContent = new RapidFloatingActionContentLabelList(mContext);
        rfaContent.setOnRapidFloatingActionContentLabelListListener(this);
        List<RFACLabelItem> items = new ArrayList<>();
        items.add(new RFACLabelItem<Integer>()
                .setLabel("Toggle Horizontal Scrolling")
                .setDrawable(mContext.getResources().getDrawable(R.drawable.ic_flip))
                .setIconNormalColor(0xffd84315)
                .setIconPressedColor(0xffbf360c)
                .setWrapper(0)
        );
        items.add(new RFACLabelItem<Integer>()
                .setLabel("Share this post")
                .setDrawable(mContext.getResources().getDrawable(R.drawable.ic_share))
                .setIconNormalColor(0xff4e342e)
                .setIconPressedColor(0xff3e2723)
                .setWrapper(1)
        );
        items.add(new RFACLabelItem<Integer>()
                .setLabel("Edit Your Profile")
                .setDrawable(getResources().getDrawable(R.drawable.ic_face))
                .setIconNormalColor(getResources().getColor(R.color.white))
                .setIconPressedColor(0xff0d5302)
                .setLabelColor(0xff056f00)
                .setWrapper(2)
        );
        items.add(new RFACLabelItem<Integer>()
                .setLabel("Disable this button")
                .setDrawable(getResources().getDrawable(R.drawable.ic_close))
                .setIconNormalColor(getResources().getColor(R.color.light_blue_400))
                .setIconPressedColor(0xff1a237e)
                .setLabelColor(0xff283593)
                .setWrapper(3)
        );

        rfaContent
                .setItems(items)
                .setIconShadowColor(0xff888888);

        rfabHelper = new RapidFloatingActionHelper(mContext,rfaLayout,rfaBtn,rfaContent).build();

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

                        post.setTags(objectMap.get("tags").toString());
                        post.setTags2(objectMap.get("tags2").toString());

                        post.setUser_id(objectMap.get("user_id").toString());
                        post.setUser_id2(objectMap.get("user_id2").toString());

                        post.setChallenge_id(objectMap.get("challenge_id").toString());
                        post.setStatus(objectMap.get("status").toString());

                        if (post.getStatus().equals("INACTIVE"))
                            post.setWinner(objectMap.get("winner").toString());

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

    private void sharePost(){
        Log.d(TAG, "onLongClick: attempting to share the post ");

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

                File file = saveBitMap(mContext, theWholeView);    //which view you want to pass that view as parameter
                if (file != null) {
                    scanGallery(mContext,file.getAbsolutePath());
                    Toasty.success(mContext, "Post saved to gallery", Toast.LENGTH_SHORT,true).show();
                } else {
                    Toasty.error(mContext, "Something went wrong, please try again!", Toast.LENGTH_SHORT,true).show();
                }

            }
        });

        otherApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(theWholeView,"Attempting to share the post...",Snackbar.LENGTH_LONG).show();

                try{
                    File file = saveBitMap(mContext, theWholeView);
                    MediaScannerConnection.scanFile(mContext,
                            new String[] { file.getAbsolutePath() }, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.d("onScanCompleted", uri.getPath());

                                    Intent shareIntent = new Intent();
                                    shareIntent.setAction(Intent.ACTION_SEND);
                                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Compete with your selfies using the Celfie app! \nRegister now : app link goes here");
                                    shareIntent.setType("image/jpg");
                                    mContext.startActivity(Intent.createChooser(shareIntent, "Share Celfie to..."));
                                }
                            });
                }catch (Exception e){
                    Log.d(TAG, "onClick: Exception " + e.getMessage());
                }
            }
        });

        shareImageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        shareImageDialog.show();
    }

    private void setupWidgets() {
        view = findViewById(R.id.view);
        view.setVisibility(View.INVISIBLE);
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
        heartHolder = findViewById(R.id.heart_holder);
        heartHolder2 = findViewById(R.id.heart_holder2);

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

        view.setVisibility(View.VISIBLE);
        theWholeView.setLayoutParams(new FrameLayout.LayoutParams(screenWidth*2,screenHeight));
        cardView1.setLayoutParams(new LinearLayout.LayoutParams(screenWidth,screenHeight));
        cardView2.setLayoutParams(new LinearLayout.LayoutParams(screenWidth,screenHeight));

        setTopToolbar(post);

        likesString1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigating to ViewLikesActivity");
                Intent i = new Intent(mContext, ViewLikesActivity.class);
                i.putExtra(mContext.getString(R.string.intent_post),post);
                startActivity(i);
            }
        });

        likesString2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigating to ViewLikesActivity");
                Intent i = new Intent(mContext, ViewLikesActivity.class);
                i.putExtra(mContext.getString(R.string.intent_post),post);
                i.putExtra("set_to_two",2);
                startActivity(i);
            }
        });


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

        UniversalImageLoader.setImage(post.getImage_url(),image1,null,"",child);
        UniversalImageLoader.setImage(post.getImage_url2(),image2,null,"",child2);

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

        timeRemaining.setText(String.valueOf(time) + " hr remaining");

        if (!post.getStatus().equals("INACTIVE")) {

            if (post.getStatus().equals("ACTIVE") || post.getStatus().equals("AWAITING_RESULT")) {
                if (time <= 0) {
                    timeRemaining.setText("Awaiting result");
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                    ref.child("Posts")
                            .child(post.getPostKey())
                            .child("status")
                            .setValue("AWAITING_RESULT");

                    heartHolder.setVisibility(View.GONE);
                    heartHolder2.setVisibility(View.GONE);
                } else {
                    setLikesIcons(post);
                }
            }
        }else if (post.getStatus().equals("INACTIVE")){

            heartHolder.setVisibility(View.GONE);
            heartHolder2.setVisibility(View.GONE);

            timeRemaining.setPadding(1,1,10,1);
            timeRemaining.setTextColor(getResources().getColor(R.color.black));

            image1.setAlpha(0.5f);
            image2.setAlpha(0.5f);

            RelativeLayout tvWinner = findViewById(R.id.tvWinner);
            RelativeLayout tvWinner2 = findViewById(R.id.tvWinner2);
            RelativeLayout tvLoser = findViewById(R.id.tvLoser);
            RelativeLayout tvLoser2 = findViewById(R.id.tvLoser2);

            if (post.getWinner().equals("tie")){
                timeRemaining.setText("It's a draw!!");

            }else if (post.getWinner().equals(post.getUser_id())){

                tvWinner.setVisibility(View.VISIBLE);
                tvLoser2.setVisibility(View.VISIBLE);

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                ref.child(getString(R.string.dbname_users))
                        .child(post.getUser_id())
                        .child("username")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String username = dataSnapshot.getValue(String.class);
                                timeRemaining.setText(username + " won the challenge");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

            }else if (post.getWinner().equals(post.getUser_id2())){

                tvWinner2.setVisibility(View.VISIBLE);
                tvLoser.setVisibility(View.VISIBLE);

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                ref.child(getString(R.string.dbname_users))
                        .child(post.getUser_id2())
                        .child("username")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String username = dataSnapshot.getValue(String.class);
                                timeRemaining.setText(username + " won the challenge");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
        }
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

    private File saveBitMap(Context context, View drawView){
        File pictureFileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"Handcare");
        if (!pictureFileDir.exists()) {
            boolean isDirectoryCreated = pictureFileDir.mkdirs();
            if(!isDirectoryCreated)
                Log.i("ATG", "Can't create directory to save the image");
            return null;
        }
        String filename = pictureFileDir.getPath() +File.separator+ System.currentTimeMillis()+".jpg";
        File pictureFile = new File(filename);
        Bitmap bitmap =getBitmapFromView(drawView);
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
    }

    //create bitmap from view and returns it
    private Bitmap getBitmapFromView(View view) {
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
    private void scanGallery(Context cntx, String path) {
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
