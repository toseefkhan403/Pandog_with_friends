package com.android.toseefkhan.pandog.Profile;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.android.toseefkhan.pandog.Login.LoginActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.BottomNavViewHelper;
import com.android.toseefkhan.pandog.Utils.FirebaseMethods;
import com.android.toseefkhan.pandog.Utils.GridImageAdapter;
import com.android.toseefkhan.pandog.Utils.SquareImageView;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.android.toseefkhan.pandog.models.User;
import com.android.toseefkhan.pandog.models.UserAccountSettings;
import com.android.toseefkhan.pandog.models.UserSettings;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private Context mContext=ProfileActivity.this;
    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLUMNS =3;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;


    //widgets
    private TextView  mFollowers, mFollowing, mDisplayName, mUsername, mDescription;
    private ProgressBar mProgressBar;
//    private Toolbar toolbar;
    private BottomNavigationViewEx bottomNavigationView;
    private CircleImageView mProfilePhoto;
    private TextView mEditProfile,PandaPoints;
    private int mFollowersCount=0,mFollowingCount=0,mPostsCount=0;
    private RelativeLayout relativeLayout;
    private TextView mMenu;
    private ProgressBar pb;
    private RelativeLayout profile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: started.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setupBottomNavigationView();

       // setupToolbar();
        setupActivityWidgets();
        hideWidgets();
        setBackGroundTint();
        setupFirebaseAuth();
        initImageLoader();

        getFollowersCount();
        getFollowingCount();
   //     getPostsCount();


    }

    private void setBackGroundTint() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child(getString(R.string.dbname_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByChild(getString(R.string.db_level))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: datasnapshot " + dataSnapshot);
                        Log.d(TAG, "onDataChange: datasnapshot.getValue " + dataSnapshot.getValue());

                        User user = dataSnapshot.getValue(User.class);
                        String level =user.getLevel();

                        setProfileColor(level);
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
                break;

            case "PURPLE":
                profile.setBackgroundColor(getResources().getColor(R.color.purple));
                break;

            case "BLUE":
                profile.setBackgroundColor(getResources().getColor(R.color.blue));
                break;

            case "GREEN":
                profile.setBackgroundColor(getResources().getColor(R.color.lightgreen));
                break;

            case "GREY":
                profile.setBackgroundColor(getResources().getColor(R.color.grey));
                break;

             default:
                 profile.setBackgroundColor(getResources().getColor(R.color.white));
                 break;
        }

    }

    private void getPandaPointsCount() {
        Log.d(TAG, "getPandaPointsCount: getting the count ");
        //todo fix later
        int points = mFollowersCount;
        PandaPoints.setText(String.valueOf(points));

        myRef= FirebaseDatabase.getInstance().getReference();

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.db_panda_points))
                .setValue(points);
    }

    private void hideWidgets() {
        relativeLayout.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * responsible for displaying data retrieved from firebase
     * @param userSettings
     */
    private void setProfileWidgets(UserSettings userSettings){
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database: " + userSettings.toString());
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database: " + userSettings.getSettings().getUsername());


        //User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, pb, "");

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mDescription.setText(settings.getDescription());
        mProgressBar.setVisibility(View.GONE);
    }

    private void initImageLoader() {

        UniversalImageLoader universalImageLoader=new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }



    private void setupActivityWidgets(){
        profile = findViewById(R.id.rel_profile);
        mProgressBar = (ProgressBar) findViewById(R.id.profileProgressBar);
        mProfilePhoto = findViewById(R.id.profile_photo);
        relativeLayout= findViewById(R.id.main_profile);
        mDisplayName = (TextView) findViewById(R.id.display_name);
        mUsername = (TextView) findViewById(R.id.username);
        mDescription = (TextView) findViewById(R.id.description);
      //  mPosts = (TextView) findViewById(R.id.tvPosts);
        mFollowers = (TextView) findViewById(R.id.tvFollowers);
        mFollowing = (TextView) findViewById(R.id.tvFollowing);
        PandaPoints= findViewById(R.id.pandaPoints);
//        toolbar = (Toolbar) view.findViewById(R.id.profileToolBar);
//        profileMenu = (ImageView) view.findViewById(R.id.profileMenu);
       bottomNavigationView = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        mFirebaseMethods = new FirebaseMethods(mContext);
        pb = findViewById(R.id.pb);
        mEditProfile=(TextView) findViewById(R.id.textEditProfile);
        mEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ProfileActivity.this, EditProfileActivity.class);
                startActivity(intent);
            }
        });
        mMenu = findViewById(R.id.menu);
        mMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked on the menu ");

                Intent i = new Intent(mContext, FAQs.class);
                startActivity(i);
            }
        });

    }

    private void getFollowersCount(){
        mFollowersCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_followers))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
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
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
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
//                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
//                    Log.d(TAG, "onDataChange: found post:" + singleSnapshot.getValue());
//                    mPostsCount++;
//                }
//                mPosts.setText(String.valueOf(mPostsCount));
//                getPandaPointsCount();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }

    /**
     * Responsible for setting up the profile toolbar
     */
//    private void setupToolbar(){
//        Toolbar toolbar = (Toolbar) findViewById(R.id.profileToolBar);
//        setSupportActionBar(toolbar);
//
//        ImageView profileMenu = (ImageView) findViewById(R.id.profileMenu);
//        profileMenu.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "onClick: navigating to account settings.");
//                Intent intent = new Intent(mContext, AccountSettingsActivity.class);
//                startActivity(intent);
//            }
//        });
//    }

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


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //retrieve user information from the database
                setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));

                mProgressBar.setVisibility(View.GONE);
                relativeLayout.setVisibility(View.VISIBLE);

                //retrieve images for the user in question

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
