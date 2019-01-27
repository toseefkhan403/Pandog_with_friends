package com.android.toseefkhan.pandog.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.toseefkhan.pandog.Home.HomeActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Share.ShareActivity;
import com.android.toseefkhan.pandog.Utils.BottomNavViewHelper;
import com.android.toseefkhan.pandog.Utils.FirebaseMethods;
import com.android.toseefkhan.pandog.Utils.GridImageAdapter;
import com.android.toseefkhan.pandog.Utils.InternetStatus;
import com.android.toseefkhan.pandog.Utils.Like;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.android.toseefkhan.pandog.models.Comment;
import com.android.toseefkhan.pandog.models.Post;
import com.android.toseefkhan.pandog.models.User;
import com.android.toseefkhan.pandog.models.UserAccountSettings;
import com.android.toseefkhan.pandog.models.UserSettings;
import com.dingmouren.layoutmanagergroup.viewpager.ViewPagerLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.koushikdutta.async.http.filter.DataRemainingException;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileActivity extends AppCompatActivity {

    private static final String TAG = "ViewProfileActivity";
    private Context mContext=ViewProfileActivity.this;
    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLUMNS =3;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    //vars
    private User mUser;

    private TextView mFollowers, mFollowing, mDisplayName, mUsername, mDescription;
    private ProgressBar mProgressBar;
    private GridView gridView;
    private BottomNavigationViewEx bottomNavigationView;
    private ImageView mProfilePhoto;
    private TextView mFollow, mUnfollow, PandaPoints;
    private int mFollowersCount=0,mFollowingCount=0,mPostsCount=0,ppcount=0;
    private TextView mMenu;
    private ProgressBar pb;
    private Toolbar profile;
    private RelativeLayout profile2;
    private Button mButtonChallenge;
    private RelativeLayout relativeLayout;
    private Button mViewChallenges;

 //   private RecyclerView mRVPosts;
    private ArrayList<Post> mPostList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: started.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_activity_profile);
        mButtonChallenge = findViewById(R.id.challenge_me);
  //      mRVPosts = findViewById(R.id.posts_recycler_view_list);
        relativeLayout = findViewById(R.id.reltohide);
        relativeLayout.setVisibility(View.INVISIBLE);
        profile2 = findViewById(R.id.rel_profile);
        profile = findViewById(R.id.profileToolBar);
        mProgressBar = (ProgressBar) findViewById(R.id.profileProgressBar);
        mProgressBar.setVisibility(View.GONE);
        mProfilePhoto = findViewById(R.id.profile_photo);
        mDisplayName = (TextView) findViewById(R.id.display_name);
        mUsername = (TextView) findViewById(R.id.username);
        mDescription = (TextView) findViewById(R.id.description);
     //   mPosts = (TextView) findViewById(R.id.);
        mFollowers = (TextView) findViewById(R.id.tvFollowers);
        mFollowing = (TextView) findViewById(R.id.tvFollowing);
        gridView = (GridView) findViewById(R.id.gridView);
        PandaPoints= findViewById(R.id.pandaPoints);
        mMenu = findViewById(R.id.menu);
        mMenu.setVisibility(View.GONE);
        bottomNavigationView = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        mFirebaseMethods = new FirebaseMethods(mContext);
        mFollow= findViewById(R.id.textFollow);
        mUnfollow= findViewById(R.id.textUnFollow);
        mViewChallenges = findViewById(R.id.button_view_challenges);
        mViewChallenges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //should take you to a fragment or activity where the posts by the user can be seen
                //currently it takes you to homeActivity
                Intent i = new Intent(mContext, HomeActivity.class);
                startActivity(i);
            }
        });

        pb = findViewById(R.id.pb);

        mButtonChallenge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to share screen ");

                if (isFollow){
                    Intent intent = new Intent(mContext, ShareActivity.class);
                    Log.d(TAG, "onClick: the user " + mUser);
                    intent.putExtra("chosen_user", mUser);
                    startActivity(intent);
                }else {
                    Snackbar.make(profile2,"You need to follow this user for a challenge" , Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        mFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: now following: " + mUser.getUsername());

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mUser.getUser_id())
                        .child(getString(R.string.field_user_id))
                        .setValue(mUser.getUser_id());

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_followers))
                        .child(mUser.getUser_id())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(getString(R.string.field_user_id))
                        .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                setFollowing();
                getFollowersCount();
                getPandaPointsCount();
            }
        });


        mUnfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: now unfollowing: " + mUser.getUsername());

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mUser.getUser_id())
                        .removeValue();

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_followers))
                        .child(mUser.getUser_id())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .removeValue();
                setUnfollowing();
                getFollowersCount();
                getPandaPointsCount();
            }
        });


        setupBottomNavigationView();
        setupFirebaseAuth();

        try{
            mUser = getUserFromBundle();
            Log.d(TAG, "onCreate: mUser" + mUser);
            init();
            checkLevel(mUser);
        }catch (NullPointerException e){
            Log.e(TAG, "onCreateView: NullPointerException: "  + e.getMessage() );
            Toast.makeText(mContext, "something went wrong", Toast.LENGTH_SHORT).show();
            getFragmentManager().popBackStack();
        }

        mViewChallenges.setText("View Posts By " + mUser.getUsername());

        isFollowing();
        getFollowingCount();
        getFollowersCount();

     //   getPostsOnProfile();

        if (!InternetStatus.getInstance(this).isOnline()) {

            Snackbar.make(getWindow().getDecorView().getRootView(),"You are not online!",Snackbar.LENGTH_LONG).show();
        }

    }

//    private void initRecyclerView() {
//
//        Collections.reverse(mPostList);
//        mRVPosts.setLayoutManager(new ViewPagerLayoutManager(mContext, OrientationHelper.VERTICAL));
//        PostsProfileRVAdapter adapter = new PostsProfileRVAdapter(mContext, mPostList);
//        mRVPosts.setAdapter(adapter);
//    }

    private void getPostsOnProfile(){

        Log.d(TAG, "getPostsOnProfile: getting posts.");

        //todo currently its retrieving all the posts. get only those which the user is related to
        myRef.child("Posts")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: .getValue " + dataSnapshot.getValue());
                        mPostList.clear();

                        for (DataSnapshot singleSnapshot: dataSnapshot.getChildren() ) {

                            Post post = new Post();
                            HashMap<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

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

                            post.setPostKey(objectMap.get("postKey").toString());
                            /*String image_url, String caption, String photo_id, String user_id, String tags,
                String image_url2, String caption2, String photo_id2, String user_id2, String tags2*/

                            List<Like> likesList = new ArrayList<Like>();
                            for (DataSnapshot dSnapshot : singleSnapshot
                                    .child("likes").getChildren()){
                                Like like = new Like();
                                like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                                likesList.add(like);
                            }
                            post.setLikes(likesList);

                            List<Like> likesList2 = new ArrayList<Like>();
                            for (DataSnapshot dSnapshot : singleSnapshot
                                    .child("likes2").getChildren()){
                                Like like = new Like();
                                like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                                likesList2.add(like);
                            }
                            post.setLikes2(likesList2);

                            List<Comment> comments = new ArrayList<Comment>();
                            for (DataSnapshot dSnapshot : singleSnapshot
                                    .child("comments").getChildren()){
                                Comment comment = new Comment();
                                comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                                comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                                comments.add(comment);
                            }
                            post.setComments(comments);

                            mPostList.add(post);
                            Log.d(TAG, "onDataChange: singlesnapshot.getValue " + post);
                        }

                        //initRecyclerView();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }


    private void checkLevel(User user){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child(getString(R.string.dbname_users))
                .child(user.getUser_id())
                .orderByChild(getString(R.string.db_level))
                .addValueEventListener(new ValueEventListener() {       //todo this is giving a memory leak
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User currentUser = dataSnapshot.getValue(User.class);
                        setProfileColor(currentUser.getLevel());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void setProfileColor(String level){

        Log.d(TAG, "setTint: the level of the current user is " + level);

        switch (level){

            case "BLACK":
                profile.setBackgroundColor(getResources().getColor(R.color.black));
                mUsername.setTextColor(getResources().getColor(R.color.white));
                break;

            case "PURPLE":
                profile.setBackgroundColor(getResources().getColor(R.color.purple));
                mUsername.setTextColor(getResources().getColor(R.color.white));
                break;

            case "BLUE":
                profile.setBackgroundColor(getResources().getColor(R.color.lightblue));
                mUsername.setTextColor(getResources().getColor(R.color.white));
                break;

            case "GREEN":
                profile.setBackgroundColor(getResources().getColor(R.color.lightgreen));
                mUsername.setTextColor(getResources().getColor(R.color.black));
                break;

            case "GREY":
                profile.setBackgroundColor(getResources().getColor(R.color.grey));
                mUsername.setTextColor(getResources().getColor(R.color.black));
                break;

            default:
                profile.setBackgroundColor(getResources().getColor(R.color.white));
                mUsername.setTextColor(getResources().getColor(R.color.black));
                break;
        }

    }


    private void getPandaPointsCount() {
        ppcount=0;
        Log.d(TAG, "getPandaPointsCount: getting the count ");

        // todo later
      //  ppcount= mFollowersCount+ mPostsCount;
        ppcount= mFollowersCount;
        PandaPoints.setText(String.valueOf(ppcount));

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(mUser.getUser_id())
                .child(mContext.getString(R.string.db_panda_points))
                .setValue(ppcount);
    }

    private void init() {

        //set the profile widgets
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference();
        reference1.child(getString(R.string.dbname_user_account_settings)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot childData : dataSnapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: found user:" + childData.getValue(UserAccountSettings.class).toString());

                        if (childData.getValue(UserAccountSettings.class).getUsername().equals(mUser.getUsername())){
                            UserAccountSettings userAccountSettings = childData.getValue(UserAccountSettings.class);
                            Log.d(TAG, "onDataChange: this is testing " + userAccountSettings);
                            UserSettings settings = new UserSettings();
                            settings.setUser(mUser);
                            settings.setSettings(childData.getValue(UserAccountSettings.class));
                            setProfileWidgets(settings);
                        }

                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //todo set the posts that the user has competed in Ever and display them under his profile

    }

    private boolean isFollow = false;

    private void isFollowing() {
        Log.d(TAG, "isFollowing: checking if following this users.");
        setUnfollowing();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByChild(getString(R.string.field_user_id)).equalTo(mUser.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user:" + singleSnapshot.getValue());

                    setFollowing();
                    isFollow= true;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getFollowersCount(){
        mFollowersCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_followers))
                .child(mUser.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found follower:" + singleSnapshot.getValue());
                    mFollowersCount++;
                }
                mFollowers.setText(String.valueOf(mFollowersCount));
                getPandaPointsCount();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getFollowingCount(){
        mFollowingCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_following))
                .child(mUser.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found following user:" + singleSnapshot.getValue());
                    mFollowingCount++;
                }
                mFollowing.setText(String.valueOf(mFollowingCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

//    private void getPostsCount(){
//        mPostsCount = 0;
//
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
//        Query query = reference.child(getString(R.string.dbname_user_photos))
//                .child(mUser.getUser_id());
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
//                    Log.d(TAG, "onDataChange: found post:" + singleSnapshot.getValue());
//                    mPostsCount++;
//                }
//                mPosts.setText(String.valueOf(mPostsCount));
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }

        private void setFollowing(){
        Log.d(TAG, "setFollowing: updating UI for following this user");
        mFollow.setVisibility(View.GONE);
        mUnfollow.setVisibility(View.VISIBLE);
        isFollow = true;
    }

    private void setUnfollowing(){
        Log.d(TAG, "setFollowing: updating UI for unfollowing this user");
        mFollow.setVisibility(View.VISIBLE);
        mUnfollow.setVisibility(View.GONE);
        isFollow = false;
    }


    private User getUserFromBundle(){
        Log.d(TAG, "getUserFromBundle: " + getIntent().getExtras());

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            return bundle.getParcelable(getString(R.string.intent_user));
        }else{
            return null;
        }
    }

    /**
     * responsible for displaying data retrieved from firebase
     * @param userSettings
     */
    private void setProfileWidgets(UserSettings userSettings){
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database: " + userSettings.toString());
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database: " + userSettings.getSettings().getUsername());

        View child = findViewById(R.id.progress_child);

        //User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "", child);

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mDescription.setText(settings.getDescription());
        mProgressBar.setVisibility(View.GONE);
        relativeLayout.setVisibility(View.VISIBLE);
    }

      /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavViewHelper.enableNavigation(mContext, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

     /*
    ------------------------------------ Firebase ---------------------------------------------
     */

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();


                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
