package com.android.toseefkhan.pandog.Profile;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.toseefkhan.pandog.Home.HomeActivity;
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
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

public class ViewPostsListActivity extends AppCompatActivity {

    private static final String TAG = "ViewPostsListActivity";

    private Context mContext = ViewPostsListActivity.this;
    private RecyclerView mRVPosts;
    private ArrayList<Post> mPostList = new ArrayList<>();
    private PostsProfileRVAdapter adapter;
    private DatabaseReference myRef;


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
        myRef = FirebaseDatabase.getInstance().getReference();

        getPostKeysOnProfile();

        mRVPosts.setLayoutManager(new ViewPagerLayoutManager(mContext, OrientationHelper.VERTICAL));
        adapter = new PostsProfileRVAdapter(mContext, mPostList);
        mRVPosts.setAdapter(adapter);

    }


//    private void initRecyclerView() {
//
//        if (!mPostList.isEmpty()) {
//            Collections.reverse(mPostList);
//
//        }else{
//            Toast.makeText(mContext, "No posts yet!", Toast.LENGTH_LONG).show();
//        }
//    }


    private void getPostKeysOnProfile() {
        Log.d(TAG, "getPostsOnProfile: getting posts." + getIntent().getExtras().getString("uid"));

        myRef.child("user_posts")
                .child(getIntent().getExtras().getString("uid"))
                .orderByKey()
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        String postKey = dataSnapshot.getValue(String.class);
                        Log.d(TAG, "ChildAdded POSTS" + postKey);
                        getPostsOnProfile(postKey);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    //todo sometimes, one of the posts is null. Idk why.
    private void getPostsOnProfile(String postkey) {

        myRef.child("Posts")
                .child(postkey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot singleSnapshot) {

                        if (singleSnapshot.exists()) {
                            Post post = new Post();
                            HashMap<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                            post.setStatus(objectMap.get("status").toString());

                            if (post.getStatus().equals("INACTIVE"))
                                post.setWinner(objectMap.get("winner").toString());

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
                            post.setTimeStamp(Long.parseLong(objectMap.get("timeStamp").toString()));

                            post.setPostKey(objectMap.get("postKey").toString());
                            /*String image_url, String caption, String photo_id, String user_id, String tags,
                String image_url2, String caption2, String photo_id2, String user_id2, String tags2*/

                            List<Like> likesList = new ArrayList<Like>();
                            for (DataSnapshot dSnapshot : singleSnapshot
                                    .child("likes").getChildren()) {
                                Like like = new Like();
                                like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                                likesList.add(like);
                            }
                            post.setLikes(likesList);

                            List<Like> likesList2 = new ArrayList<Like>();
                            for (DataSnapshot dSnapshot : singleSnapshot
                                    .child("likes2").getChildren()) {
                                Like like = new Like();
                                like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                                likesList2.add(like);
                            }
                            post.setLikes2(likesList2);

                            List<Comment> comments = new ArrayList<Comment>();
                            for (DataSnapshot dSnapshot : singleSnapshot
                                    .child("comments").getChildren()) {
                                Comment comment = new Comment();
                                comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                                comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                                comments.add(comment);
                            }
                            post.setComments(comments);

                            mPostList.add(post);
                         //   Collections.reverse(mPostList);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
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
