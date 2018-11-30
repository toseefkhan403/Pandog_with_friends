package com.android.toseefkhan.pandog.Share;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;

public class NextActivity extends AppCompatActivity {

    private static final String TAG = "NextActivity";
    private Context mContext = NextActivity.this;

    private String mAppend = "file:/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        Log.d(TAG, "onCreate: got the chosen image: " + getIntent().getStringExtra(getString(R.string.selected_image)));
        setImage();
    }

    /**
     * gets the image url from the incoming intent and displays the chosen image
     */
    private void setImage() {
        Intent intent = getIntent();
        ImageView image = (ImageView) findViewById(R.id.imageShare);

        //this is how you load a image
        UniversalImageLoader.setImage(intent.getStringExtra(getString(R.string.selected_image)), image, null, mAppend);
    }

    //ToDo: Write codes in Next activity so that the user can choose from his friends to compete.
    //step1: submit photo to firebase STORAGE(not database)(database will only store an url pointing to that image).
    //step2: in nextActivity, user can see the image he has chosen, and below will be the list of all the persons whom he follows/ or the ones who follow him.
    //step3: on clicking upon his friends name, this pending post can be seen in an fragment which will be available in HomeActivity(to be created).
    //step4: Now the user's friend will get a notification about this. He can choose to either accept or ignore, and after he submits his photo, a post will
    //be created which will be available on the main feed of both the users and their respective followers.
    //step5: All the important notifications must be provided to both the users(if someone accepts their request they should get a notif)
    //step6: All the posts created by a particular user can be seen under his ProfileActivity replacing the currently placed gridView.
    //ToDo: Create the post. It will be available for 24 hours.
    //ToDo: Create mainFeedListAdapter to display posts in the HomeActivity.

    //i can do the uploading to firebase STORAGE part. Tell me if you want me to do it.
    //layout for a basic post is in layout_post

}
