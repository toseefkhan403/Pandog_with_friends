package com.android.toseefkhan.pandog.Share;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.android.toseefkhan.pandog.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.io.IOException;

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
    private Intent intent;
    private ImageView image;

    private FriendsAdapter mFriendsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        Log.d(TAG, "onCreate: got the chosen image: " + getIntent().getStringExtra(getString(R.string.selected_image)));
        mFirebaseMethods = new FirebaseMethods(NextActivity.this);
        mCaption = findViewById(R.id.caption);
        friendsListView = findViewById(R.id.FriendsListView);
        image = findViewById(R.id.imageShare);
        setupFirebaseAuth();

        Intent i =getIntent();
        if (i.hasExtra("challenger_user")){
            Bundle b = i.getExtras();
            if(b!=null){
               User user = b.getParcelable("challenger_user");
               Log.d(TAG, "onCreate: user " + user);
               if (user == null){
                   setupFriendsList();
               }else{
                   mFriendsAdapter = new FriendsAdapter(user,mContext);
                   friendsListView.setAdapter(mFriendsAdapter);
               }
            }
        }else{
            setupFriendsList();
        }

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
                sharePost();
            }
        });

        setImage();
    }

    private void sharePost() {
        Log.d(TAG, "onClick: navigating to the final share screen.");
        //upload the image to firebase
        int selectedUserPosition = mFriendsAdapter.getSelectedUserPosition();
        if (selectedUserPosition == -1) {
            Toast.makeText(mContext, "Select a user from list first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (image == null){
            Toast.makeText(mContext, "Please wait for the image to load", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Attempting to upload new photo");
        String caption = mCaption.getText().toString();

        User selectedUser = (User) mFriendsAdapter.getItem(selectedUserPosition);
        if (intent.hasExtra(getString(R.string.selected_image))) {
            intent = getIntent();
            String image_path = intent.getStringExtra(getString(R.string.selected_image));
            Uri myUri = Uri.parse(image_path);
            Log.d(TAG, "onClick: this is the uri from the intent " + myUri);

            long timeStamp = System.currentTimeMillis();
            String imageName = "photo" + timeStamp;

            if (getIntent().hasExtra("post_task")){
                String challengeKey = getIntent().getStringArrayListExtra("post_task").get(0);
                mFirebaseMethods.uploadNewPhoto(getString(R.string.post_photo),caption,imageName,null,myUri, selectedUser, challengeKey);
            }else{
                mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageName, null, myUri, selectedUser);
            }
        }
    }

    /**
     * gets the image url from the incoming intent and displays the chosen image
     */
    private void setImage() {
        intent = getIntent();
        //Image view that is present in the NextActivity

        if (intent.hasExtra(getString(R.string.selected_image))) {
            imgUrl = intent.getStringExtra(getString(R.string.selected_image));
            Log.d(TAG, "setImage: got new image uri: " + imgUrl);

            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(imgUrl, image);
        }
    }

    //ToDo: Write codes in Next activity so that the user can choose from his friends to compete.

    private void setupFriendsList() {
        mFriendsAdapter = new FriendsAdapter(mAuth.getCurrentUser().getUid(), mContext);
        friendsListView.setAdapter(mFriendsAdapter);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth() {
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


        myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        imageCount = mFirebaseMethods.getImageCount(dataSnapshot);
                        Log.d(TAG, "onDataChange: image count: " + imageCount);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.i("Firebase", databaseError.toString());
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
