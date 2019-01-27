package com.android.toseefkhan.pandog.Home;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.database.FirebaseDatabase;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private Context mContext=HomeActivity.this;
    private static final int ACTIVITY_NUM = 0;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ViewPager mViewPager;

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
                setContentView(R.layout.progress_anim);
                SquareDrawable indicator = new BallDrawable(new int[]{getResources().getColor(R.color.deep_purple_400), getResources().getColor(R.color.light_green_400)
                        , getResources().getColor(R.color.deep_orange_400), getResources().getColor(R.color.pink_400)});

                SquareDrawable indicator2 = new PacmanDrawable(new int[]{getResources().getColor(R.color.deep_purple_400), getResources().getColor(R.color.light_green_400)
                        , getResources().getColor(R.color.teal_400), getResources().getColor(R.color.pink_400)});
                indicator.setPadding(40);
                indicator2.setPadding(20);
                View child;
                child = findViewById(R.id.progress_child);

                if (System.currentTimeMillis()%2 == 1){
                    child.setBackground(indicator);
                    final Animatable animatable = (Animatable) indicator;
                    animatable.start();
                }
                else{
                    child.setBackground(indicator2);
                    final Animatable animatable2 = (Animatable) indicator2;
                    animatable2.start();
                }
                setupFirebaseAuth();
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
        }

        if (!InternetStatus.getInstance(this).isOnline()) {

            Snackbar.make(getWindow().getDecorView().getRootView(),"You are not online!",Snackbar.LENGTH_LONG).show();
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
        BottomNavViewHelper.enableNavigation(mContext, bottomNavigationViewEx);
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

}
