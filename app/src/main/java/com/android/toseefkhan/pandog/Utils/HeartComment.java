package com.android.toseefkhan.pandog.Utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.toseefkhan.pandog.Profile.PostsProfileRVAdapter;
import com.android.toseefkhan.pandog.Profile.ViewPostActivity;
import com.android.toseefkhan.pandog.models.Comment;
import com.android.toseefkhan.pandog.models.Post;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class HeartComment {

    private static final String TAG = "Heart";

    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();

    private ImageView heartWhite, heartRed;
    private DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
    private Context mContext;
    private String postKey;

    public HeartComment(ImageView heartWhite, ImageView heartRed,Context context,String postKey) {
        this.heartWhite = heartWhite;
        this.heartRed = heartRed;
        this.mContext = context;
        this.postKey = postKey;
    }

    public void toggleLike(CommentsRVAdapter.ViewHolder viewHolder, Comment comment){
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
            removeLike(comment);
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
            addNewLike(comment);
        }

        animationSet.start();
    }


    private void removeLike(final Comment comment){

        Query query = myRef
                .child("Posts")
                .child(postKey)
                .child("comments")
                .child(comment.getCommentID())
                .child("likes");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                    String keyID = singleSnapshot.getKey();

                    if(singleSnapshot.getValue(Like.class).getUser_id()
                            .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){

                        myRef.child("Posts")
                                .child(postKey)
                                .child("comments")
                                .child(comment.getCommentID())
                                .child("likes")
                                .child(keyID)
                                .removeValue();
                        heartWhite.setVisibility(View.VISIBLE);
                        heartRed.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    private void addNewLike(Comment comment){
        Log.d(TAG, "addNewLike: adding new like");

        String newLikeID = myRef.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        myRef.child("Posts")
                .child(postKey)
                .child("comments")
                .child(comment.getCommentID())
                .child("likes")
                .child(newLikeID)
                .setValue(like);

        heartWhite.setVisibility(View.GONE);
        heartRed.setVisibility(View.VISIBLE);
    }

}
