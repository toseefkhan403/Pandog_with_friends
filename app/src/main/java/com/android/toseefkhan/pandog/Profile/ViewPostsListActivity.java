package com.android.toseefkhan.pandog.Profile;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
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
import com.android.toseefkhan.pandog.models.Comment;
import com.android.toseefkhan.pandog.models.Post;
import com.android.toseefkhan.pandog.models.User;
import com.dingmouren.layoutmanagergroup.viewpager.ViewPagerLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.takusemba.spotlight.OnSpotlightStateChangedListener;
import com.takusemba.spotlight.OnTargetStateChangedListener;
import com.takusemba.spotlight.Spotlight;
import com.takusemba.spotlight.shape.Circle;
import com.takusemba.spotlight.target.CustomTarget;

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

public class ViewPostsListActivity extends AppCompatActivity implements PostsProfileRVAdapter.OnLoadMoreItemsListener {

    @Override
    public void onLoadMoreItems() {
        Log.d(TAG, "onLoadMoreItems: displaying more photos");

        this.displayMorePhotos();
    }

    private static final String TAG = "ViewPostsListActivity";

    private Context mContext = ViewPostsListActivity.this;
    private RecyclerView mRVPosts;
    private ArrayList<String> mPostKeysList = new ArrayList<>();
    private ArrayList<Post> mPostList = new ArrayList<>();
    private PostsProfileRVAdapter mAdapter;
    private ArrayList<Post> mPaginatedPosts;
    private int mResults;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_profile_posts_list);
        setupBottomNavigationView();

        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mRVPosts = findViewById(R.id.posts_recycler_view_list);
        mRVPosts.setItemViewCacheSize(20);
        mRVPosts.setDrawingCacheEnabled(true);
        mRVPosts.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        mRVPosts.setLayoutManager(new ViewPagerLayoutManager(mContext, OrientationHelper.VERTICAL));

        if (getIntent().hasExtra("post_keys_list")){

            startSpotlight();
            mPostKeysList = getIntent().getStringArrayListExtra("post_keys_list");
            getPhotos();
        }else{
            getPostKeysOnProfile();
        }

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

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child("user_posts")
                .child(getIntent().getExtras().getString("uid"));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found keys: " +
                            singleSnapshot.getValue(String.class));

                    mPostKeysList.add(singleSnapshot.getValue(String.class));
                }
                //get the photos
                getPhotos();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getPhotos(){
        Log.d(TAG, "getPhotos: getting photos");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
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
                            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
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

                mAdapter = new PostsProfileRVAdapter(mContext, mPaginatedPosts);
                mRVPosts.setAdapter(mAdapter);
                Log.d(TAG, "displayPhotos: i am making it this far and let's see what paginated posts has " + mPaginatedPosts.size());

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
                mAdapter.notifyDataSetChanged();
            }
        }catch (NullPointerException e){
            Log.e(TAG, "displayPhotos: NullPointerException: " + e.getMessage() );
        }catch (IndexOutOfBoundsException e){
            Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage() );
        }
    }


    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavViewHelper.enableNavigation(mContext, bottomNavigationViewEx, ViewPostsListActivity.this);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(4);
        menuItem.setChecked(true);
    }

}
