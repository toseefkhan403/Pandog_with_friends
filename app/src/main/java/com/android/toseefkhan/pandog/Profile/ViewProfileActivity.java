package com.android.toseefkhan.pandog.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.toseefkhan.pandog.Utils.ViewFollowersActivity;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.toseefkhan.pandog.Home.HomeActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Share.ShareActivity;
import com.android.toseefkhan.pandog.Utils.BottomNavViewHelper;
import com.android.toseefkhan.pandog.Utils.FirebaseMethods;
import com.android.toseefkhan.pandog.Utils.InternetStatus;
import com.android.toseefkhan.pandog.Utils.Like;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.android.toseefkhan.pandog.models.Comment;
import com.android.toseefkhan.pandog.models.Post;
import com.android.toseefkhan.pandog.models.User;
import com.android.toseefkhan.pandog.models.UserAccountSettings;
import com.android.toseefkhan.pandog.models.UserSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ViewProfileActivity extends AppCompatActivity {

    private static final String TAG = "ViewProfileActivity";
    private Context mContext=ViewProfileActivity.this;
    private static final int ACTIVITY_NUM = 4;

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

    private ArrayList<Post> mPostList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: started.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_activity_profile);
        mButtonChallenge = findViewById(R.id.challenge_me);
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
        mFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mContext, ViewFollowersActivity.class);
                i.putExtra(getString(R.string.intent_user_id), mUser.getUser_id());
                startActivity(i);
            }
        });

        mFollowing = (TextView) findViewById(R.id.tvFollowing);
        mFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mContext, ViewFollowersActivity.class);
                i.putExtra(getString(R.string.intent_user_id), mUser.getUser_id());
                i.putExtra("set_to_two",2);
                startActivity(i);
            }
        });

        gridView = (GridView) findViewById(R.id.gridView);
        PandaPoints= findViewById(R.id.pandaPoints);
        mMenu = findViewById(R.id.menu);
        mMenu.setVisibility(View.GONE);
        LinearLayout fonts = findViewById(R.id.fonts);
        fonts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mContext,ViewPostsListActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.pull,R.anim.push);
            }
        });
        bottomNavigationView = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        mFirebaseMethods = new FirebaseMethods(mContext);
        mFollow= findViewById(R.id.textFollow);
        mUnfollow= findViewById(R.id.textUnFollow);

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

        isFollowing();
        getFollowingCount();
        getFollowersCount();
        getPandaPointsCount();


        if (!InternetStatus.getInstance(this).isOnline()) {

            Snackbar.make(getWindow().getDecorView().getRootView(),"You are not online!",Snackbar.LENGTH_LONG).show();
        }

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

        myRef.child(getString(R.string.dbname_users))
                .child(mUser.getUser_id())
                .child("panda_points")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Long l = dataSnapshot.getValue(Long.class);
                        PandaPoints.setText(String.valueOf(l));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
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
        BottomNavViewHelper.enableNavigation(mContext, bottomNavigationViewEx,ViewProfileActivity.this);
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
