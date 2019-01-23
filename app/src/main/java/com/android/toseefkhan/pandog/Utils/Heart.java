package com.android.toseefkhan.pandog.Utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.toseefkhan.pandog.Profile.PostsProfileRVAdapter;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.models.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class Heart {

        private static final String TAG = "Heart";

        private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
        private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();

        private ImageView heartWhite, heartRed,heartWhite2, heartRed2;
        private LinearLayout view;
        private DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        private Context mContext;
        private PostsProfileRVAdapter adapter;
        private PostsProfileRVAdapter.ViewHolder mViewHolder;

    public Heart(ImageView heartWhite, ImageView heartRed, ImageView heartWhite2, ImageView heartRed2,LinearLayout view,Context context) {
        this.heartWhite = heartWhite;
        this.heartRed = heartRed;
        this.heartWhite2 = heartWhite2;
        this.heartRed2 = heartRed2;
        this.view = view;
        this.mContext = context;
        adapter = new PostsProfileRVAdapter(mContext);
    }

    public void toggleLike(PostsProfileRVAdapter.ViewHolder viewHolder, Post post){
        mViewHolder = viewHolder;
        Log.d(TAG, "toggleLike: toggling heart.");

            AnimatorSet animationSet =  new AnimatorSet();

            if(heartRed.getVisibility() == View.VISIBLE){
                Log.d(TAG, "toggleLike: toggling red heart off.");
                heartRed.setScaleX(0.1f);
                heartRed.setScaleY(0.1f);

                ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(heartRed, "scaleY", 1f, 0f);
                scaleDownY.setDuration(300);
                scaleDownY.setInterpolator(ACCELERATE_INTERPOLATOR);

                ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(heartRed, "scaleX", 1f, 0f);
                scaleDownX.setDuration(300);
                scaleDownX.setInterpolator(ACCELERATE_INTERPOLATOR);

                heartRed.setVisibility(View.GONE);
                heartWhite.setVisibility(View.VISIBLE);

                animationSet.playTogether(scaleDownY, scaleDownX);
                removeLike1(post);
            }

            else if(heartRed.getVisibility() == View.GONE){
                Log.d(TAG, "toggleLike: toggling red heart on.");
                heartRed.setScaleX(0.1f);
                heartRed.setScaleY(0.1f);

                ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(heartRed, "scaleY", 0.1f, 1f);
                scaleDownY.setDuration(300);
                scaleDownY.setInterpolator(DECCELERATE_INTERPOLATOR);

                ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(heartRed, "scaleX", 0.1f, 1f);
                scaleDownX.setDuration(300);
                scaleDownX.setInterpolator(DECCELERATE_INTERPOLATOR);

                heartRed.setVisibility(View.VISIBLE);
                heartWhite.setVisibility(View.GONE);

                animationSet.playTogether(scaleDownY, scaleDownX);
                addNewLike1(post);
            }

            animationSet.start();
            checkLikes(post);
    }

    private void checkLikes(Post post) {

        if ((heartRed.getVisibility() == View.VISIBLE) && (heartRed2.getVisibility() == View.VISIBLE)){
                Snackbar.make(view,"You can vote for any ONE user at a time!", Snackbar.LENGTH_SHORT).show();
                heartRed.setVisibility(View.GONE);
                heartRed2.setVisibility(View.GONE);
                heartWhite.setVisibility(View.VISIBLE);
                heartWhite2.setVisibility(View.VISIBLE);
                removeLike1(post);
                removeLike2(post);
        }
    }

    public void toggleLike2(PostsProfileRVAdapter.ViewHolder viewHolder,Post post){
        mViewHolder = viewHolder;

            AnimatorSet animationSet2 =  new AnimatorSet();
            //for the second pair
            if(heartRed2.getVisibility() == View.VISIBLE){
                Log.d(TAG, "toggleLike: toggling red heart off.");
                heartRed2.setScaleX(0.1f);
                heartRed2.setScaleY(0.1f);

                ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(heartRed2, "scaleY", 1f, 0f);
                scaleDownY.setDuration(300);
                scaleDownY.setInterpolator(ACCELERATE_INTERPOLATOR);

                ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(heartRed2, "scaleX", 1f, 0f);
                scaleDownX.setDuration(300);
                scaleDownX.setInterpolator(ACCELERATE_INTERPOLATOR);

                heartRed2.setVisibility(View.GONE);
                heartWhite2.setVisibility(View.VISIBLE);

                animationSet2.playTogether(scaleDownY, scaleDownX);
                removeLike2(post);
            }

            else if(heartRed2.getVisibility() == View.GONE){
                Log.d(TAG, "toggleLike: toggling red heart on.");
                heartRed2.setScaleX(0.1f);
                heartRed2.setScaleY(0.1f);

                ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(heartRed2, "scaleY", 0.1f, 1f);
                scaleDownY.setDuration(300);
                scaleDownY.setInterpolator(DECCELERATE_INTERPOLATOR);

                ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(heartRed2, "scaleX", 0.1f, 1f);
                scaleDownX.setDuration(300);
                scaleDownX.setInterpolator(DECCELERATE_INTERPOLATOR);

                heartRed2.setVisibility(View.VISIBLE);
                heartWhite2.setVisibility(View.GONE);

                animationSet2.playTogether(scaleDownY, scaleDownX);
                addNewLike2(post);
            }
            animationSet2.start();
            checkLikes(post);
    }

    private void removeLike1(final Post post){

        Query query = myRef
                .child("Posts")
                .child(post.getPostKey())
                .child("likes");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: hey the loop is going on");
                    String keyID = singleSnapshot.getKey();

                    if(singleSnapshot.getValue(Like.class).getUser_id()
                            .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){

                        myRef.child("Posts")
                                .child(post.getPostKey())
                                .child("likes")
                                .child(keyID)
                                .removeValue();
                        heartWhite.setVisibility(View.VISIBLE);
                        heartRed.setVisibility(View.GONE);
                        adapter.setBoolean(mViewHolder,post);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ((InitialSetup)mContext.getApplicationContext()).wait = false;
    }

    private void removeLike2(final Post post){

        Query query = myRef
                .child("Posts")
                .child(post.getPostKey())
                .child("likes2");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: hey the loop is going on");
                    String keyID = singleSnapshot.getKey();

                    if(singleSnapshot.getValue(Like.class).getUser_id()
                            .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){

                        myRef.child("Posts")
                                .child(post.getPostKey())
                                .child("likes2")
                                .child(keyID)
                                .removeValue();
                        heartWhite2.setVisibility(View.VISIBLE);
                        heartRed2.setVisibility(View.GONE);
                        adapter.setBoolean(mViewHolder,post);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ((InitialSetup)mContext.getApplicationContext()).wait = false;
    }

    private void addNewLike1(Post post){
        Log.d(TAG, "addNewLike: adding new like");

        String newLikeID = myRef.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        myRef.child("Posts")
                .child(post.getPostKey())
                .child("likes")
                .child(newLikeID)
                .setValue(like);
        heartWhite.setVisibility(View.GONE);
        heartRed.setVisibility(View.VISIBLE);

        ((InitialSetup)mContext.getApplicationContext()).wait = false;
    }

    private void addNewLike2(Post post){
        Log.d(TAG, "addNewLike: adding new like");

        String newLikeID = myRef.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        myRef.child("Posts")
                .child(post.getPostKey())
                .child("likes2")
                .child(newLikeID)
                .setValue(like);
        heartWhite2.setVisibility(View.GONE);
        heartRed2.setVisibility(View.VISIBLE);

        ((InitialSetup)mContext.getApplicationContext()).wait = false;

    }


}
