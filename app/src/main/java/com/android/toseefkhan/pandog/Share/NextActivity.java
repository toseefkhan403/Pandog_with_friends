package com.android.toseefkhan.pandog.Share;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.FirebaseMethods;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NextActivity extends AppCompatActivity {

    private static final String TAG = "NextActivity";
    private Context mContext = NextActivity.this;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    //widgets
    private EditText mCaption;
    private ListView friendsListView;

    //vars
    private String mAppend = "file:/";
    private int imageCount = 0;
    private String imgUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        Log.d(TAG, "onCreate: got the chosen image: " + getIntent().getStringExtra(getString(R.string.selected_image)));
        mFirebaseMethods = new FirebaseMethods(NextActivity.this);
        mCaption = findViewById(R.id.caption);
        friendsListView = findViewById(R.id.FriendsListView);
        setupFirebaseAuth();

        setupFriendsList();
        ImageView backArrow = findViewById(R.id.ivBackArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the activity");
                finish();
            }
        });

        TextView share = findViewById(R.id.tvShare);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to the final share screen.");
                //upload the image to firebase
                Toast.makeText(mContext, "Attempting to upload new photo", Toast.LENGTH_SHORT).show();
                String caption = mCaption.getText().toString();
                mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageCount, imgUrl,null);
            }
        });

        setImage();
    }

    /**
     * gets the image url from the incoming intent and displays the chosen image
     */
    private void setImage() {
        Intent intent = getIntent();
        //Image view that is present in the NextActivity
        ImageView image = findViewById(R.id.imageShare);

        //imgUrl received as an intent extra
        imgUrl = intent.getStringExtra(getString(R.string.selected_image));
        UniversalImageLoader.setImage(imgUrl, image, null, mAppend);
    }

    //ToDo: Write codes in Next activity so that the user can choose from his friends to compete.
                                    //step1: DONE, step 2 half done
    //step1: submit photo to firebase STORAGE(not database)(database will only store an url pointing to that image).
    //step2: in nextActivity, user can see the image he has chosen, and below will be the list of all the persons whom he follows/ or the ones who follow him.

    private void setupFriendsList() {
        FriendsAdapter friendsAdapter = new FriendsAdapter(mAuth.getCurrentUser().getUid(), mContext);
        friendsListView.setAdapter(friendsAdapter);
    }
    //step3: on clicking upon his friends name, this pending post can be seen in an fragment which will be available in HomeActivity(to be created).
    //step4: Now the user's friend will get a notification about this. He can choose to either accept or ignore, and after he submits his photo, a post will
    //be created which will be available on the main feed of both the users and their respective followers.
    //step5: All the important notifications must be provided to both the users(if someone accepts their request they should get a notif)
    //step6: All the posts created by a particular user can be seen under his ProfileActivity replacing the currently placed gridView.
    //ToDo: Create the post. It will be available for 24 hours.
    //ToDo: Create mainFeedListAdapter to display posts in the HomeActivity.

    //layout for a basic post is in layout_post
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
        Log.d(TAG, "onDataChange: image count: " + imageCount);

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

                imageCount = mFirebaseMethods.getImageCount(dataSnapshot);
                Log.d(TAG, "onDataChange: image count: " + imageCount);

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
