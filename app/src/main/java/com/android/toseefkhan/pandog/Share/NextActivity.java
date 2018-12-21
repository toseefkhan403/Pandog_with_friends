package com.android.toseefkhan.pandog.Share;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import com.android.toseefkhan.pandog.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;

import javax.net.ssl.HttpsURLConnection;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

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

    private String uriString = "firebaseHostingUrl";
    private Disposable mDisposable;
    private FriendsAdapter mFriendsAdapter;

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
                sharePost();
            }
        });

        setImage();
    }

    private void sharePost() {
        Callable<String> mCallable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                Log.d(TAG, "onClick: navigating to the final share screen.");
                //upload the image to firebase
                int selectedUserPosition = mFriendsAdapter.getSelectedUserPosition();
                if (selectedUserPosition == -1) {
                    return "Select a user from list first";
                }

                Log.d(TAG, "Attempting to upload new photo");
                String caption = mCaption.getText().toString();

                if (intent.hasExtra(getString(R.string.selected_image))) {
                    intent = getIntent();
                    String image_path = intent.getStringExtra(getString(R.string.selected_image));
                    Uri myUri = Uri.parse(image_path);
                    Log.d(TAG, "onClick: this is the uri from the intent " + myUri);

                    long timeStamp = System.currentTimeMillis();
                    String imageName = "photo" + timeStamp;
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageName, null, myUri);
                }
                User selectedUser = (User) mFriendsAdapter.getItem(selectedUserPosition);
                String uid = selectedUser.getUser_id();
                String httpUrlString = parseURL(uid);

                URL httpUrl = new URL(httpUrlString);

                return getResponsefromUrl(httpUrl);
            }

        };


        Observer<String> mObserver = new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                mDisposable = d;
            }

            @Override
            public void onNext(String result) {
                Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.getMessage());
                Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() {
                Log.i(TAG, "Operation Completed");
            }
        };

        Observable.fromCallable(mCallable)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mObserver);
    }

    private String getResponsefromUrl(URL httpUrl) throws IOException {
        String response = "";
        HttpsURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        try {
            httpURLConnection = (HttpsURLConnection) httpUrl.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setReadTimeout(15000);
            httpURLConnection.setConnectTimeout(10000);
            httpURLConnection.connect();

            if (httpURLConnection.getResponseCode() == 200) {
                StringBuilder responseBuilder = new StringBuilder();
                inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line = bufferedReader.readLine();
                while (line != null) {
                    responseBuilder.append(line);
                    line = bufferedReader.readLine();
                }
                response = responseBuilder.toString();
            } else {
                response = "Operation Failed";
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return response;
    }

    private String parseURL(String uid) {

        Uri baseUri = Uri.parse(uriString);
        Uri.Builder builder = baseUri.buildUpon();

        builder.appendQueryParameter("user", uid);
        return builder.toString();
    }

    /**
     * gets the image url from the incoming intent and displays the chosen image
     */
    private void setImage() {
        intent = getIntent();
        //Image view that is present in the NextActivity
        ImageView image = findViewById(R.id.imageShare);

        if (intent.hasExtra(getString(R.string.selected_image))) {
            imgUrl = intent.getStringExtra(getString(R.string.selected_image));
            Log.d(TAG, "setImage: got new image uri: " + imgUrl);

            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(imgUrl.toString(), image);
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
        mDisposable.dispose();
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
