package com.android.toseefkhan.pandog.Home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;

import androidx.annotation.NonNull;

import com.android.toseefkhan.pandog.Intro.Holder;
import com.android.toseefkhan.pandog.Map.MapActivity;
import com.android.toseefkhan.pandog.Profile.PostsProfileRVAdapter;
import com.android.toseefkhan.pandog.Share.ShareActivity;
import com.android.toseefkhan.pandog.models.LatLong;
import com.android.toseefkhan.pandog.models.User;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import es.dmoral.toasty.Toasty;
import kotlin.jvm.internal.Intrinsics;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.transition.Slide;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.toseefkhan.pandog.Login.LoginActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.BallDrawable;
import com.android.toseefkhan.pandog.Utils.BottomNavViewHelper;
import com.android.toseefkhan.pandog.Utils.FragmentPagerAdapter;
import com.android.toseefkhan.pandog.Utils.InitialSetup;
import com.android.toseefkhan.pandog.Utils.InternetStatus;
import com.android.toseefkhan.pandog.Utils.PacmanDrawable;
import com.android.toseefkhan.pandog.Utils.SquareDrawable;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.takusemba.spotlight.OnSpotlightStateChangedListener;
import com.takusemba.spotlight.OnTargetStateChangedListener;
import com.takusemba.spotlight.Spotlight;
import com.takusemba.spotlight.shape.Circle;
import com.takusemba.spotlight.target.CustomTarget;
import com.takusemba.spotlight.target.SimpleTarget;
import com.takusemba.spotlight.target.Target;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements PostsProfileRVAdapter.OnLoadMoreItemsListener {


    @Override
    public void onLoadMoreItems() {
        Log.d(TAG, "onLoadMoreItems: displaying more photos");
        HomeFragment fragment = (HomeFragment)getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.container + ":" + mViewPager.getCurrentItem());
        if(fragment != null){
            fragment.displayMorePhotos();
        }
    }

    private static final String TAG = "HomeActivity";
    private Context mContext=HomeActivity.this;
    private static final int ACTIVITY_NUM = 0;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ViewPager mViewPager;

    //for the welcome screen
    SharedPreferences mPrefs;
    final String tutorialScreenShownPref = "tutorialScreenShown";
    final String referralScreenShownPref = "referralScreenShown";

    private FrameLayout root;
    private LayoutInflater inflater;

    //todo optimizing map section - setting levels and creating dynamic markers, delete the map (cry emoji)
    //todo fix the trending screen in implementation(my hashtags: their posts)
    //todo clean code (delete unnecessary code from the db and app)
    //todo add the instagram crop-before-upload thing


    //todo (Aryal)
    //todo better search, the search should always take the user to the bottom
    //todo of the list so he can see all the users and not necessarily swipe up for more results.
    //todo delete stuff from user_notif node too (after a challenge is accepted or rejected)

    //todo (non-coding stuff)
    //todo get the maps api key
    //todo a thorough testing of the app and bug fixes
    //todo create dev account on google play; launch the app successfully (*happy emoji)(*another happy emoji)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!((InitialSetup)getApplicationContext()).isTaskCompleted)
        {
            Intent i = getIntent();
            if (i.hasExtra("ChallengerUser"))
            {
                setContentView(R.layout.activity_home);
                setupFirebaseAuth();
                initImageLoader();
                setupBottomNavigationView();
                setupViewPager();
                mViewPager.setCurrentItem(1);
            }else{
                setupFirebaseAuth();
                setContentView(R.layout.progress_anim);
                SquareDrawable indicator = new BallDrawable(new int[]{getResources().getColor(R.color.deep_purple_400), getResources().getColor(R.color.light_green_400)
                        , getResources().getColor(R.color.deep_orange_400), getResources().getColor(R.color.pink_400)});

                indicator.setPadding(40);
                View child;
                child = findViewById(R.id.progress_child);

                child.setBackground(indicator);
                final Animatable animatable = (Animatable) indicator;
                animatable.start();
            }
        } else {
            setContentView(R.layout.activity_home);
            setupFirebaseAuth();
            initImageLoader();
            setupBottomNavigationView();
            setupViewPager();

            Intent intent = getIntent();
            if (intent.hasExtra("ChallengerUser")) {
                mViewPager.setCurrentItem(1);
            }

            mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

            boolean referralScreenShown = mPrefs.getBoolean(referralScreenShownPref, false);

            if (!referralScreenShown) {

                startReferralScreen();
                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putBoolean(referralScreenShownPref, true);
                editor.apply(); // Very important to save the preference
            }

        }

        if (!InternetStatus.getInstance(this).isOnline()) {

            Snackbar.make(getWindow().getDecorView().getRootView(),"You are not online!",Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent i = new Intent(mContext,HomeActivity.class);
                            startActivity(i);
                        }
                    })
                    .show();
        }

    }

    private void setupViewPager() {

        FragmentPagerAdapter adapter=new FragmentPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new HomeFragment()); //index is 0
        adapter.addFragment(new NotificationFragment());  //index is 1

        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_logo);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_notification);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child(getString(R.string.dbname_users))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                            User user = singleSnapshot.getValue(User.class);
                            String uid = user.getUser_id();

                            if (uid.contains("something")){
                                ref.child(getString(R.string.dbname_users)).child(uid).removeValue();
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavViewHelper.enableNavigation(mContext, bottomNavigationViewEx,HomeActivity.this);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    @Override
    public void onBackPressed() {

    }


    /*
    ------------------------------------ Firebase ---------------------------------------------
     */

    /**
     * checks to see if the @param 'user' is logged in
     * @param user
     */
    private void checkCurrentUser(FirebaseUser user){
        Log.d(TAG, "checkCurrentUser: checking if user is logged in.");

        if(user == null){
            Intent intent = new Intent(mContext, LoginActivity.class);
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            startActivity(intent);
        }
        else{
            addTokenToDatabase();
        }
    }

    private void addTokenToDatabase() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("PandogPreference",
                Context.MODE_PRIVATE);
        String fcmToken = sharedPreferences.getString("FCMToken", null);
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (fcmToken != null) {
            FirebaseDatabase.getInstance().getReference().child("token").child(userUid).setValue(fcmToken);
        }
    }

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //check if the user is logged in
                checkCurrentUser(user);

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
        checkCurrentUser(mAuth.getCurrentUser());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void startTutorial(RelativeLayout Rview){
        Log.d(TAG, "startTutorial: spotlight tutorial started.");

        View first = inflater.inflate(R.layout.overlay_home_tutorial, root);

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

        TextView Skip = first.findViewById(R.id.close_spotlight);
        TextView Next = first.findViewById(R.id.close_target);
        TextView Recommend = first.findViewById(R.id.recommend);
        ImageView image = first.findViewById(R.id.image);

        Rview.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override public void onGlobalLayout() {
                Rview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Spotlight spotlight = Spotlight.with(HomeActivity.this)
                        .setOverlayColor(R.color.background)
                        .setDuration(1000L)
                        .setAnimation(new DecelerateInterpolator(2f))
                        .setTargets(homeView)
                        .setClosedOnTouchedOutside(false)
                        .setOnSpotlightStateListener(new OnSpotlightStateChangedListener() {
                            @Override
                            public void onStarted() {

                            }

                            @Override
                            public void onEnded() {

                            }
                        });
                spotlight.start();


                Skip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Recommend.setText("Are you sure you want to skip? \nLong click below to skip the tutorial");

                        Skip.setText("Long click here");
                        Skip.setTextSize(18.0f);

                        image.setVisibility(View.GONE);
                    }
                });

                Skip.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        spotlight.closeSpotlight();

                        return true;
                    }
                });

                Next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        spotlight.closeCurrentTarget();

                        new MaterialTapTargetPrompt.Builder(HomeActivity.this)
                                .setTarget(findViewById(R.id.homeFrag))
                                .setBackButtonDismissEnabled(false)
                                .setAutoDismiss(false)
                                .setIconDrawable(getResources().getDrawable(R.drawable.ic_logo))
                                .setBackgroundColour(getResources().getColor(R.color.background))
                                .setPrimaryText("This is where you will find your news feed")
                                .setSecondaryText("And vote for your favourite contender!")
                                .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                                    @Override
                                    public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state) {
                                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                                            new MaterialTapTargetPrompt.Builder(HomeActivity.this)
                                                    .setTarget(findViewById(R.id.pc))
                                                    .setBackButtonDismissEnabled(false)
                                                    .setAutoDismiss(true)
                                                    .setBackgroundColour(getResources().getColor(R.color.background))
                                                    .setPrimaryText("Tap here!")
                                                    .setSecondaryText("")
                                                    .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                                                        @Override
                                                        public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state) {

                                                            if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                                                                new MaterialTapTargetPrompt.Builder(HomeActivity.this)
                                                                        .setTarget(findViewById(R.id.pc))
                                                                        .setBackButtonDismissEnabled(false)
                                                                        .setAutoDismiss(true)
                                                                        .setBackgroundColour(getResources().getColor(R.color.background))
                                                                        .setPrimaryText("And here!")
                                                                        .setSecondaryText("This is how you can navigate through a post")
                                                                        .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                                                                            @Override
                                                                            public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state) {

                                                                                if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {

                                                                                    new MaterialTapTargetPrompt.Builder(HomeActivity.this)
                                                                                            .setTarget(findViewById(R.id.notifFrag))
                                                                                            .setBackgroundColour(getResources().getColor(R.color.background))
                                                                                            .setAutoDismiss(false)
                                                                                            .setBackButtonDismissEnabled(false)
                                                                                            .setIconDrawable(getResources().getDrawable(R.drawable.ic_notification))
                                                                                            .setPrimaryText("Click here to see the list of your pending challenges")
                                                                                            .setSecondaryText("")
                                                                                            .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                                                                                                @Override
                                                                                                public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state) {
                                                                                                    if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                                                                                                        new MaterialTapTargetPrompt.Builder(HomeActivity.this)
                                                                                                                .setTarget(findViewById(R.id.ic_android))
                                                                                                                .setBackgroundColour(getResources().getColor(R.color.background))
                                                                                                                .setAutoDismiss(false)
                                                                                                                .setBackButtonDismissEnabled(false)
                                                                                                                .setPrimaryText("This is your profile")
                                                                                                                .setSecondaryText("Make sure you keep it updated!")
                                                                                                                .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                                                                                                                    @Override
                                                                                                                    public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state) {
                                                                                                                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {

                                                                                                                        }
                                                                                                                    }
                                                                                                                })
                                                                                                                .show();
                                                                                                    }
                                                                                                }
                                                                                            })
                                                                                            .show();
                                                                                }
                                                                            }
                                                                        })
                                                                        .show();
                                                            }
                                                        }
                                                    })
                                                    .show();
                                        }
                                    }
                                })
                                .show();
                    }
                });
            }
        });
    }

    private void startReferralScreen() {

        setContentView(R.layout.overlay_referral);
        setupFirebaseAuth();

        EditText inputReferral = findViewById(R.id.input_referral);
        TextView closeBonus = findViewById(R.id.close_bonus);
        TextView addBonus = findViewById(R.id.add_bonus);

        closeBonus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // second argument is the default to use if the preference can't be found
                boolean welcomeScreenShown = mPrefs.getBoolean(tutorialScreenShownPref, false);

                if (!welcomeScreenShown) {

                    setContentView(R.layout.activity_home);
                    setupFirebaseAuth();
                    initImageLoader();
                    setupBottomNavigationView();
                    setupViewPager();
                    RelativeLayout Rview = findViewById(R.id.view_tutorial);
                    root = new FrameLayout(mContext);
                    inflater = LayoutInflater.from(mContext);
                    startTutorial(Rview);
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putBoolean(tutorialScreenShownPref, true);
                    editor.apply(); // Very important to save the preference
                }
            }
        });

        addBonus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: giving the points to both the users");

                String referralCode = inputReferral.getText().toString();
                setReferralPoints(referralCode);
            }
        });
    }


    private boolean isReferralCorrect = false;

    private void setReferralPoints(String referralCode) {

        Log.d(TAG, "setReferralPoints: referral code " + referralCode);

        //checking whether the code is correct or not
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child(getString(R.string.dbname_users))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: checking for match");

                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                            User user = singleSnapshot.getValue(User.class);
                            Log.d(TAG, "onDataChange: user is " + user);

                            if (user.getUsername().equals(referralCode)){
                                Log.d(TAG, "onDataChange: found a match");
                                isReferralCorrect = true;

                                ref.child(getString(R.string.dbname_users))
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child("panda_points")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                int currentPoints = dataSnapshot.getValue(Integer.class);

                                                if (currentPoints == 0) {
                                                    Log.d(TAG, "onDataChange: user is eligible, give them points");

                                                    ref.child(getString(R.string.dbname_users))
                                                            .child(user.getUser_id())
                                                            .child("panda_points")
                                                            .setValue(user.getPanda_points() + 50);

                                                    ref.child(getString(R.string.dbname_users))
                                                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                            .child("panda_points")
                                                            .setValue(50);

                                                    Toasty.success(HomeActivity.this, "Hooray! Bonus has been successfully added to your account", Toast.LENGTH_LONG,true).show();

                                                    // second argument is the default to use if the preference can't be found
                                                    boolean welcomeScreenShown = mPrefs.getBoolean(tutorialScreenShownPref, false);

                                                    if (!welcomeScreenShown) {

                                                        setContentView(R.layout.activity_home);
                                                        setupFirebaseAuth();
                                                        initImageLoader();
                                                        setupBottomNavigationView();
                                                        setupViewPager();
                                                        RelativeLayout view = findViewById(R.id.view_tutorial);
                                                        root = new FrameLayout(mContext);
                                                        inflater = LayoutInflater.from(mContext);
                                                        startTutorial(view);
                                                        SharedPreferences.Editor editor = mPrefs.edit();
                                                        editor.putBoolean(tutorialScreenShownPref, true);
                                                        editor.apply(); // Very important to save the preference
                                                    }
                                                }else
                                                    Toasty.error(HomeActivity.this, "You are not eligible for referral bonus", Toast.LENGTH_LONG,true).show();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                break;
                            }
                        }

                        if (!isReferralCorrect)
                            Toasty.error(mContext, "Incorrect referral code", Toast.LENGTH_LONG,true).show();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

}
