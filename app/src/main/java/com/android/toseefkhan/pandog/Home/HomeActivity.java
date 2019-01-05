package com.android.toseefkhan.pandog.Home;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.toseefkhan.pandog.Login.LoginActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.BottomNavViewHelper;
import com.android.toseefkhan.pandog.Utils.FragmentPagerAdapter;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.android.toseefkhan.pandog.models.User;
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
        setContentView(R.layout.activity_home);

        setupFirebaseAuth();

        initImageLoader();
        setupBottomNavigationView();
        setupViewPager();

        Intent intent = getIntent();
        if (intent.hasExtra("ChallengerUser")) {
            mViewPager.setCurrentItem(1);
        }

        calcUser();
    }

    private ArrayList<User> mUserList = new ArrayList<>();

    private void calcUser(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user list: " + singleSnapshot.getValue());  // gives the whole user objects

                    try{
                        User user= singleSnapshot.getValue(User.class);
                        mUserList.add(user);
                    }catch (Exception e){
                        Log.d(TAG, "onDataChange: NullPointerException " + e.getMessage());
                    }
                }
                setLevels(mUserList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setLevels(ArrayList<User> mUserList) {

        ArrayList<User> userList1 = new ArrayList<>();
        ArrayList<User> userList2 = new ArrayList<>();
        ArrayList<User> userList3 = new ArrayList<>();
        ArrayList<User> userList4 = new ArrayList<>();
        ArrayList<User> userList5 = new ArrayList<>();

        if (mUserList != null)
            mUserList = sortList(mUserList);         //sortList(mUserList);

        int level = mUserList.size()/5;

        for (int i=0; i<mUserList.size(); i++ ){

            if (i<=level)
                userList1.add(mUserList.get(i));         //level 5
            else if (i>level && i<=2*level)
                userList2.add(mUserList.get(i));         //level 4
            else if (i>level && i<=3*level)
                userList3.add(mUserList.get(i));
            else if (i>level && i<=4*level)
                userList4.add(mUserList.get(i));
            else if (i>level && i>4*level)
                userList5.add(mUserList.get(i));
        }
        Log.d(TAG, "setMarkerswithLevels: checking the lists " + userList1 +userList2 + userList3+ userList4+ userList5);

        DatabaseReference myRef;
        myRef= FirebaseDatabase.getInstance().getReference();

        for (int i = 0 ; i< userList1.size(); i++){

            User user = userList1.get(i);

            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(user.getUser_id())
                    .child(mContext.getString(R.string.db_level))
                    .setValue("BLACK");
        }

        for (int i = 0 ; i< userList2.size(); i++){

            User user = userList2.get(i);

            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(user.getUser_id())
                    .child(mContext.getString(R.string.db_level))
                    .setValue("PURPLE");
        }

        for (int i = 0 ; i< userList3.size(); i++){

            User user = userList3.get(i);

            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(user.getUser_id())
                    .child(mContext.getString(R.string.db_level))
                    .setValue("BLUE");
        }

        for (int i = 0 ; i< userList4.size(); i++){

            User user = userList4.get(i);

            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(user.getUser_id())
                    .child(mContext.getString(R.string.db_level))
                    .setValue("GREEN");
        }

        for (int i = 0 ; i< userList5.size(); i++){

            User user = userList5.get(i);

            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(user.getUser_id())
                    .child(mContext.getString(R.string.db_level))
                    .setValue("GREY");
        }

    }

    private ArrayList<User> sortList(ArrayList<User> userList) {

        Collections.sort(userList, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return Integer.valueOf(o2.getPanda_points()).compareTo(Integer.valueOf(o1.getPanda_points()));
            }
        });

        return userList;
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
