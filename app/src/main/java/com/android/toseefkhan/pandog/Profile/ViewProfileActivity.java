package com.android.toseefkhan.pandog.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
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

import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Share.ShareActivity;
import com.android.toseefkhan.pandog.Utils.BottomNavViewHelper;
import com.android.toseefkhan.pandog.Utils.FirebaseMethods;
import com.android.toseefkhan.pandog.Utils.GridImageAdapter;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.android.toseefkhan.pandog.models.User;
import com.android.toseefkhan.pandog.models.UserAccountSettings;
import com.android.toseefkhan.pandog.models.UserSettings;
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
    private CircleImageView mProfilePhoto;
    private TextView mFollow, mUnfollow, PandaPoints;
    private int mFollowersCount=0,mFollowingCount=0,mPostsCount=0,ppcount=0;
    private TextView mMenu;
    private ProgressBar pb;
    private RelativeLayout profile;
    private Button mButtonChallenge;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: started.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_activity_profile);
        mButtonChallenge = findViewById(R.id.challenge_me);
        relativeLayout = findViewById(R.id.reltohide);
        relativeLayout.setVisibility(View.INVISIBLE);
        profile = findViewById(R.id.rel_profile);
        mProgressBar = (ProgressBar) findViewById(R.id.profileProgressBar);
        mProgressBar.setVisibility(View.VISIBLE);
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
                    Snackbar.make(profile,"You need to follow this user for a challenge" , Snackbar.LENGTH_SHORT).show();
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
       // setupToolbar();
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
     //   getPostsCount();
        tempGridSetup();
    }

    private void checkLevel(User user){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child(getString(R.string.dbname_users))
                .child(user.getUser_id())
                .orderByChild(getString(R.string.db_level))
                .addValueEventListener(new ValueEventListener() {
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
                profile.setBackgroundColor(getResources().getColor(R.color.black));
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


        //User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, pb, "");

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mDescription.setText(settings.getDescription());
        mProgressBar.setVisibility(View.GONE);
        relativeLayout.setVisibility(View.VISIBLE);
    }

    private void tempGridSetup(){
        ArrayList<String> imgURLs = new ArrayList<>();
        imgURLs.add("https://i.redd.it/9bf67ygj710z.jpg");
        imgURLs.add("https://c1.staticflickr.com/5/4276/34102458063_7be616b993_o.jpg");
        imgURLs.add("http://i.imgur.com/EwZRpvQ.jpg");
        imgURLs.add("http://i.imgur.com/JTb2pXP.jpg");
        imgURLs.add("https://i.redd.it/59kjlxxf720z.jpg");
        imgURLs.add("https://i.redd.it/pwduhknig00z.jpg");
        imgURLs.add("https://i.redd.it/clusqsm4oxzy.jpg");
        imgURLs.add("https://i.redd.it/svqvn7xs420z.jpg");
        imgURLs.add("http://i.imgur.com/j4AfH6P.jpg");
        imgURLs.add("https://i.redd.it/89cjkojkl10z.jpg");
        imgURLs.add("https://i.redd.it/aw7pv8jq4zzy.jpg");

        setupImageGrid(imgURLs);
    }

    private void setupImageGrid(ArrayList<String> imgURLs){
        GridView gridView = (GridView) findViewById(R.id.gridView);

        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth/NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);

        GridImageAdapter adapter = new GridImageAdapter(mContext, R.layout.layout_grid_image_view, "", imgURLs);
        gridView.setAdapter(adapter);
    }



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
