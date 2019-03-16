package com.android.toseefkhan.pandog.Profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.toseefkhan.pandog.Home.HomeActivity;
import com.android.toseefkhan.pandog.Home.HomeFragment;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.BottomNavViewHelper;
import com.android.toseefkhan.pandog.Utils.Like;
import com.android.toseefkhan.pandog.Utils.PullToRefreshView;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.android.toseefkhan.pandog.models.Comment;
import com.android.toseefkhan.pandog.models.Post;
import com.android.toseefkhan.pandog.models.User;
import com.dingmouren.layoutmanagergroup.viewpager.ViewPagerLayoutManager;
import com.github.tbouron.shakedetector.library.ShakeDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;

public class ViewPostsListActivity extends AppCompatActivity implements PostsProfileRVAdapter.OnLoadMoreItemsListener , RapidFloatingActionContentLabelList.OnRapidFloatingActionContentLabelListListener {

    @Override
    public void onLoadMoreItems() {
        Log.d(TAG, "onLoadMoreItems: displaying more photos");

        this.displayMorePhotos();
    }

    private static final String TAG = "ViewPostsListActivity";

    private DatabaseReference reference;
    private ValueEventListener v1;

    private Context mContext = ViewPostsListActivity.this;
    private RecyclerView mRVPosts;
    private ArrayList<String> mPostKeysList = new ArrayList<>();
    private ArrayList<Post> mPostList = new ArrayList<>();
    private PostsProfileRVAdapter mAdapter;
    private ArrayList<Post> mPaginatedPosts;
    private int mResults;

    private RapidFloatingActionLayout rfaLayout;
    private RapidFloatingActionButton rfaBtn;
    private RapidFloatingActionHelper rfabHelper;

    SharedPreferences mPrefs;
    final String horizontalScreenEnabled = "horizontalScreenEnabled";
    final String showFloatingButton = "showFloatingButton";
    boolean horizontalScrollingEnabled;
    boolean isshowFloatingButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_profile_posts_list);
        setupBottomNavigationView();
        initImageLoader();

        PullToRefreshView mPullToRefreshView = findViewById(R.id.pull_to_refresh);

        mPullToRefreshView.setOnRefreshListener(() -> mPullToRefreshView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPullToRefreshView.setRefreshing(false);

                if (getIntent().hasExtra("post_keys_list")){

                    Intent i = new Intent(mContext,ViewPostsListActivity.class);
                    i.putExtra("post_keys_list",getIntent().getStringArrayListExtra("post_keys_list"));
                    i.putExtra("title",getIntent().getStringExtra("title"));
                    startActivity(i);
                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                    finish();

                }else if (getIntent().hasExtra("uid")){

                    Intent i = new Intent(mContext,ViewPostsListActivity.class);
                    i.putExtra("uid",getIntent().getExtras().getString("uid"));
                    startActivity(i);
                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                    finish();

                }

            }
        }, 1000));


        reference = FirebaseDatabase.getInstance().getReference();
        v1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found keys: " +
                            singleSnapshot.getValue(String.class));

                    mPostKeysList.add(singleSnapshot.getValue(String.class));
                }

                if (mPostKeysList.isEmpty()){
                    findViewById(R.id.no_posts).setVisibility(View.VISIBLE);
                }else{
                    //get the photos
                    getPhotos();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mRVPosts = findViewById(R.id.posts_recycler_view_list);
        mRVPosts.setLayoutManager(new ViewPagerLayoutManager(mContext, OrientationHelper.VERTICAL));
        mRVPosts.setVisibility(View.GONE);
        rfaLayout = findViewById(R.id.activity_main_rfal);
        rfaBtn = findViewById(R.id.activity_main_rfab);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        horizontalScrollingEnabled = mPrefs.getBoolean(horizontalScreenEnabled, true);
        isshowFloatingButton = mPrefs.getBoolean(showFloatingButton,true);

        if (getIntent().hasExtra("post_keys_list")){

            startSpotlight();
            mPostKeysList = getIntent().getStringArrayListExtra("post_keys_list");
            getPhotos();
        }else{
            getPostKeysOnProfile();
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

                if (getIntent().hasExtra("post_keys_list")){

                    Intent i = new Intent(mContext,ViewPostsListActivity.class);
                    i.putExtra("post_keys_list",getIntent().getStringArrayListExtra("post_keys_list"));
                    i.putExtra("title",getIntent().getStringExtra("title"));
                    startActivity(i);
                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                    finish();

                }else if (getIntent().hasExtra("uid")){

                    Intent i = new Intent(mContext,ViewPostsListActivity.class);
                    i.putExtra("uid",getIntent().getExtras().getString("uid"));
                    startActivity(i);
                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                    finish();

                }
            }
        });
        ShakeDetector.updateConfiguration(2.0f,3);
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
                .setLabel("Scroll to top")
                .setDrawable(getResources().getDrawable(R.drawable.ic_up_arrow))
                .setIconNormalColor(getResources().getColor(R.color.pink_400))
                .setIconPressedColor(0xff1a237e)
                .setLabelColor(0xff283593)
                .setWrapper(3)
        );
        items.add(new RFACLabelItem<Integer>()
                .setLabel("Disable this button")
                .setDrawable(getResources().getDrawable(R.drawable.ic_close))
                .setIconNormalColor(getResources().getColor(R.color.light_blue_400))
                .setIconPressedColor(0xff1a237e)
                .setLabelColor(0xff283593)
                .setWrapper(4)
        );


        rfaContent
                .setItems(items)
                .setIconShadowColor(0xff888888);

        rfabHelper = new RapidFloatingActionHelper(mContext,rfaLayout,rfaBtn,rfaContent).build();

    }

    private void startSpotlight() {

        FrameLayout root = new FrameLayout(mContext);
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View first = inflater.inflate(R.layout.overlay_trending, root);

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

        TextView trending = first.findViewById(R.id.custom_text);
        TextView title = first.findViewById(R.id.title);
        trending.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/Cursive.ttf"));
        title.setText(getIntent().getStringExtra("title"));

        mRVPosts.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mRVPosts.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Spotlight spotlight = Spotlight.with(ViewPostsListActivity.this)
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
        });

    }

    private void getPostKeysOnProfile() {
        Log.d(TAG, "getPostsOnProfile: getting posts." + getIntent().getExtras().getString("uid"));

        Query query = reference
                .child("user_posts")
                .child(getIntent().getExtras().getString("uid"));
        query.addListenerForSingleValueEvent(v1);
    }

    private void getPhotos(){
        Log.d(TAG, "getPhotos: getting photos");

        for(int i = 0; i < mPostKeysList.size(); i++){
            final int count = i;
            Query query = reference
                    .child("Posts")
                    .child(mPostKeysList.get(i));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    Post post = new Post();
                    if (dataSnapshot.exists()) {
                        HashMap<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();
                        post.setStatus(objectMap.get("status").toString());

                        if (post.getStatus().equals("INACTIVE"))
                            post.setWinner(objectMap.get("winner").toString());

                        post.setImage_url(objectMap.get("image_url").toString());
                        post.setImage_url2(objectMap.get("image_url2").toString());

                        post.setCaption(objectMap.get("caption").toString());
                        post.setCaption2(objectMap.get("caption2").toString());

                        post.setTags(objectMap.get("tags").toString());
                        post.setTags2(objectMap.get("tags2").toString());

                        post.setUser_id(objectMap.get("user_id").toString());
                        post.setUser_id2(objectMap.get("user_id2").toString());

                        post.setChallenge_id(objectMap.get("challenge_id").toString());
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
                            HashMap<String, Object> objectHashMap = (HashMap<String, Object>) dSnapshot.getValue();

                            comment.setUser_id(objectHashMap.get("user_id").toString());
                            comment.setComment(objectHashMap.get("comment").toString());
                            comment.setCommentID(objectHashMap.get("commentID").toString());

                            ArrayList<Like> co = new ArrayList<>();
                            for (DataSnapshot dSnapshot2 : dSnapshot
                                    .child("likes").getChildren()) {
                                Like like = new Like();
                                like.setUser_id(dSnapshot2.getValue(Like.class).getUser_id());
                                co.add(like);
                            }
                            comment.setLikes(co);

                            comments.add(comment);
                        }
                        post.setComments(comments);


                        mPostList.add(post);
                        Log.d(TAG, "onDataChange: contents of the postlist " + mPostList.size());
                    }

                    if(count >= mPostKeysList.size() -1){
                        //display our photos
                        displayPhotos();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void displayPhotos(){
        mPaginatedPosts = new ArrayList<>();
        mRVPosts.setVisibility(View.VISIBLE);

        if(!mPostList.isEmpty()){
            try{
                Collections.sort(mPostList, new Comparator<Post>() {
                    @Override
                    public int compare(Post post, Post t1) {
                        return Long.compare(t1.getTimeStamp(),post.getTimeStamp());
                    }
                });

                Log.d(TAG, "displayPhotos: postslist now" + mPostList);

                int iterations = mPostList.size();

                if(iterations > 10){
                    iterations = 10;
                }

                mResults = 10;
                for(int i = 0; i < iterations; i++){
                    Log.d(TAG, "displayPhotos: adding posts to paginated posts" + mPostList.get(i).getPostKey());
                    mPaginatedPosts.add(mPostList.get(i));
                }

                if (horizontalScrollingEnabled) {
                    mAdapter = new PostsProfileRVAdapter(mContext, mPaginatedPosts);
                    mRVPosts.setAdapter(mAdapter);
                }else{
                    mAdapter = new PostsProfileRVAdapter(mContext, mPaginatedPosts, true);
                    mRVPosts.setAdapter(mAdapter);
                }

            }catch (NullPointerException e){
                Log.e(TAG, "displayPhotos: NullPointerException: " + e.getMessage() );
            }catch (IndexOutOfBoundsException e){
                Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage() );
            }
        }
    }

    public void displayMorePhotos(){
        Log.d(TAG, "displayMorePhotos: displaying more photos");

        try{

            if(mPostList.size() > mResults && mPostList.size() > 0){

                int iterations;
                if(mPostList.size() > (mResults + 10)){
                    Log.d(TAG, "displayMorePhotos: there are greater than 10 more photos");
                    iterations = 10;
                }else{
                    Log.d(TAG, "displayMorePhotos: there is less than 10 more photos");
                    iterations = mPostList.size() - mResults;
                }

                //add the new photos to the paginated results
                for(int i = mResults; i < mResults + iterations; i++){
                    mPaginatedPosts.add(mPostList.get(i));
                }
                mResults = mResults + iterations;

                mRVPosts.post(new Runnable()
                {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });

            }
        }catch (NullPointerException e){
            Log.e(TAG, "displayPhotos: NullPointerException: " + e.getMessage() );
        }catch (IndexOutOfBoundsException e){
            Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage() );
        }
    }


    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavViewHelper.enableNavigation(mContext, bottomNavigationViewEx, ViewPostsListActivity.this);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(4);
        menuItem.setChecked(true);
        menuItem.setEnabled(false);
    }


    /*
     ---------------------------------LIFECYCLE METHODS-----------------------------------------
     */

    @Override
    protected void onStart() {
        super.onStart();

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

        reference.removeEventListener(v1);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ShakeDetector.stop();

        reference.removeEventListener(v1);
    }

    @Override
    protected void onPause() {
        super.onPause();

        reference.removeEventListener(v1);
    }



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

                if (getIntent().hasExtra("post_keys_list")){

                    Intent i = new Intent(mContext,ViewPostsListActivity.class);
                    i.putExtra("post_keys_list",getIntent().getStringArrayListExtra("post_keys_list"));
                    i.putExtra("title",getIntent().getStringExtra("title"));
                    startActivity(i);
                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                    finish();

                }else if (getIntent().hasExtra("uid")){

                    Intent i = new Intent(mContext,ViewPostsListActivity.class);
                    i.putExtra("uid",getIntent().getExtras().getString("uid"));
                    startActivity(i);
                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                    finish();
                }

                break;

            case 1:
                if (mAdapter != null) {
                    mAdapter.sharePost();
                }

                break;

            case 2:
                Intent intent = new Intent(this, EditProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.pull,R.anim.push);
                break;

            case 3:
                mRVPosts.smoothScrollToPosition(0);
                break;

            case 4:
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

        Spotlight spotlight = Spotlight.with(ViewPostsListActivity.this)
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





}
