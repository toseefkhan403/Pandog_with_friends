package com.android.toseefkhan.pandog.Profile;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.android.toseefkhan.pandog.Home.HomeActivity;
import com.android.toseefkhan.pandog.Share.NextActivity;
import com.android.toseefkhan.pandog.Utils.ViewFollowersActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.toseefkhan.pandog.R;
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
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private Context mContext = ProfileActivity.this;
    private static final int ACTIVITY_NUM = 4;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private ValueEventListener v1;

    //widgets
    private TextView  mFollowers, mFollowing, mDisplayName, mUsername, mDescription, mInstagramUsername;
    private TextView thought;
    private ProgressBar mProgressBar;
//    private Toolbar toolbar;
    private BottomNavigationViewEx bottomNavigationView;
    private ImageView mProfilePhoto;
    private TextView mEditProfile,PandaPoints;
    private int mFollowersCount=0,mFollowingCount=0;
    private RelativeLayout relativeLayout;
    private LinearLayout fonts;
    private TextView mMenu;
    private ProgressBar pb;
    private Toolbar profile;
    private FloatingActionButton fab;

    //for the welcome screen
    SharedPreferences mPrefs;
    final String tutorialScreenShownPrefProfile = "tutorialScreenShownProfile";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: started.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setupBottomNavigationView();

        setupActivityWidgets();

        hideWidgets();
        setBackGroundTint();
        setupFirebaseAuth();
        initImageLoader();
        setTheThought();

        getFollowersCount();
        getFollowingCount();
        getPandaPointsCount();


        if (!InternetStatus.getInstance(this).isOnline()) {

            Snackbar.make(getWindow().getDecorView().getRootView(),"You are not online!",Snackbar.LENGTH_LONG).show();
        }
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
        Log.d(TAG, "getPandaPointsCount: getting the count ");

        myRef.child(getString(R.string.dbname_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("panda_points")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Long pandaPoints = dataSnapshot.getValue(Long.class);
                        PandaPoints.setText(String.valueOf(pandaPoints));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
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

        View child = findViewById(R.id.progress_child);

        //User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "", child);

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mDescription.setText(settings.getDescription());

        if (settings.getInstagram_username().equals("")){
            mInstagramUsername.setVisibility(View.GONE);
        }else {
            mInstagramUsername.setVisibility(View.VISIBLE);
            SpannableString ss = new SpannableString("Follow me on Instagram : " + settings.getInstagram_username());
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    //set link
                    Log.d(TAG, "onClick: navigating to Instagram");
                    String profileLink = settings.getInstagram_username();
                    Uri uri = Uri.parse("http://instagram.com/_u/" + profileLink);
                    Intent insta = new Intent(Intent.ACTION_VIEW, uri);
                    insta.setPackage("com.instagram.android");

                    if (isIntentAvailable(mContext, insta)) {
                        startActivity(insta);
                    } else {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/" + profileLink)));
                    }
                }
                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };
            ss.setSpan(clickableSpan, 25, 25+settings.getInstagram_username().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            mInstagramUsername.setText(ss);
            mInstagramUsername.setMovementMethod(LinkMovementMethod.getInstance());
            mInstagramUsername.setHighlightColor(Color.BLUE);
        }

        mProgressBar.setVisibility(View.GONE);
    }

    private void initImageLoader() {

        UniversalImageLoader universalImageLoader=new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void setupActivityWidgets(){

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //do something
                Intent i = new Intent(mContext, NotificationsActivity.class);
                startActivity(i);
            }
        });
        profile = findViewById(R.id.profileToolBar);
        mProgressBar = findViewById(R.id.profileProgressBar);
        mProfilePhoto = findViewById(R.id.profile_photo);
        relativeLayout= findViewById(R.id.main_profile);
        thought = findViewById(R.id.thought);
        mDisplayName = findViewById(R.id.display_name);
        mUsername = findViewById(R.id.username);
        mDescription = findViewById(R.id.description);
        mInstagramUsername = findViewById(R.id.instagram_username);
        mFollowers = findViewById(R.id.tvFollowers);
        mFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mContext, ViewFollowersActivity.class);
                i.putExtra(getString(R.string.intent_user_id), FirebaseAuth.getInstance().getCurrentUser().getUid());
                startActivity(i);
            }
        });

        mFollowing = (TextView) findViewById(R.id.tvFollowing);
        mFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mContext, ViewFollowersActivity.class);
                i.putExtra(getString(R.string.intent_user_id), FirebaseAuth.getInstance().getCurrentUser().getUid());
                i.putExtra("set_to_two",2);
                startActivity(i);
            }
        });

        PandaPoints= findViewById(R.id.pandaPoints);
       bottomNavigationView = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        mFirebaseMethods = new FirebaseMethods(mContext);
        pb = findViewById(R.id.pb);
        mEditProfile=(TextView) findViewById(R.id.textEditProfile);
        mEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ProfileActivity.this, EditProfileActivity.class);
                ActivityOptions options = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    options = ActivityOptions
                            .makeSceneTransitionAnimation((Activity) mContext,mProfilePhoto, "user_pp");
                }
                startActivity(intent,options.toBundle());
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
        fonts = findViewById(R.id.fonts);
        fonts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mContext,ViewPostsListActivity.class);
                i.putExtra("uid",FirebaseAuth.getInstance().getCurrentUser().getUid());
                startActivity(i);
                overridePendingTransition(R.anim.pull,R.anim.push);
            }
        });

    }

    private void setTheThought() {

        myRef.child("thoughts")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        try {
                            ArrayList<String> myThoughts = (ArrayList<String>) dataSnapshot.getValue();
                            int randomNumber = new Random().nextInt(myThoughts.size());
                            thought.setText(myThoughts.get(randomNumber));

                        }catch (NullPointerException e){
                            Log.d(TAG, "onDataChange: NullPointerException " + e.getMessage());
                        }catch (IndexOutOfBoundsException e){
                            Log.d(TAG, "onDataChange: IndexOutOfBoundsException " + e.getMessage());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private boolean isIntentAvailable(Context ctx, Intent intent) {
        final PackageManager packageManager = ctx.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
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

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavViewHelper.enableNavigation(mContext, bottomNavigationViewEx, ProfileActivity.this);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
        menuItem.setEnabled(false);
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

        v1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //retrieve user information from the database
                setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));

                mProgressBar.setVisibility(View.GONE);
                relativeLayout.setVisibility(View.VISIBLE);

                mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                // second argument is the default to use if the preference can't be found
                boolean welcomeScreenShown = mPrefs.getBoolean(tutorialScreenShownPrefProfile, false);

                if (!welcomeScreenShown) {

                    startTutorial();
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putBoolean(tutorialScreenShownPrefProfile, true);
                    editor.apply(); // Very important to save the preference
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        myRef.addValueEventListener(v1);
    }

    private void startTutorial() {

        Log.d(TAG, "startTutorial: starting the tutorial");

        new MaterialTapTargetPrompt.Builder(ProfileActivity.this)
                .setTarget(findViewById(R.id.menu))
                .setBackgroundColour(getResources().getColor(R.color.background))
                .setAutoDismiss(false)
                .setBackButtonDismissEnabled(false)
                .setPrimaryText("Click here to know more about the app")
                .setSecondaryText("")
                .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                    @Override
                    public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state) {
                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                            new MaterialTapTargetPrompt.Builder(ProfileActivity.this)
                                    .setTarget(findViewById(R.id.ic_search))
                                    .setBackgroundColour(getResources().getColor(R.color.background))
                                    .setAutoDismiss(false)
                                    .setBackButtonDismissEnabled(false)
                                    .setPrimaryText("Search")
                                    .setSecondaryText("Here you can find the latest trends!")
                                    .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                                        @Override
                                        public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state) {
                                            if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {

                                            }
                                        }
                                    }).show();

                        }
                    }
                }).show();

    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

        if (myRef != null && v1 != null)
        myRef.addValueEventListener(v1);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (myRef != null && v1 != null)
        myRef.addValueEventListener(v1);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }

        if (myRef != null && v1 != null)
            myRef.removeEventListener(v1);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (myRef != null && v1 != null)
            myRef.removeEventListener(v1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (myRef != null && v1 != null)
            myRef.removeEventListener(v1);
    }

}
