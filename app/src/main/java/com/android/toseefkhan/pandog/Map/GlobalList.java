package com.android.toseefkhan.pandog.Map;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.android.toseefkhan.pandog.Home.HomeActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.Heart;
import com.android.toseefkhan.pandog.Utils.InitialSetup;
import com.android.toseefkhan.pandog.Utils.PullToRefreshView;
import com.android.toseefkhan.pandog.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;

public class GlobalList extends Fragment{

    private static final String TAG = "GlobalList";
    private RecyclerView mUserListRecyclerView;
    private ProgressBar mProgressbar;
    private ImageButton imageButton;

    private Context mContext;
    private int currentPosition;
    private static final DecelerateInterpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_global_list, container, false);

        mUserListRecyclerView = view.findViewById(R.id.global_list);

        PullToRefreshView mPullToRefreshView = view.findViewById(R.id.pull_to_refresh);

        mPullToRefreshView.setOnRefreshListener(() -> mPullToRefreshView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPullToRefreshView.setRefreshing(false);
                Intent i = new Intent(getActivity(), MapActivity2.class);
                i.putExtra("move_to_two",2);
                startActivity(i);
                getActivity().overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                getActivity().finish();
            }
        }, 1000));

        mProgressbar = view.findViewById(R.id.progressBar);
        mProgressbar.setVisibility(View.VISIBLE);
        mContext = view.getContext();
        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: position is " + currentPosition);
                mUserListRecyclerView.smoothScrollToPosition(currentPosition);
            }
        });

        imageButton = view.findViewById(R.id.up_arrow);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUserListRecyclerView.smoothScrollToPosition(0);
            }

        });

        setupUsers();

        return view;
    }

    private void setupUsers(){

        ArrayList<User> arrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child(getString(R.string.dbname_users))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ss : dataSnapshot.getChildren()){

                            if (ss.exists()) {
                                User user = ss.getValue(User.class);

                                if (user != null) {
                                    Log.d(TAG, "onDataChange: user " + user.getUsername() + " " + user.getUser_id());
                                    if (user.getUser_id() != null)
                                        arrayList.add(user);
                                }
                            }
                        }
                        initUserListRecyclerView(sortList(arrayList));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void initUserListRecyclerView(ArrayList<User> users) {
        mProgressbar.setVisibility(View.GONE);
        getPositionForCurrentUser(users);
        MapRecyclerViewAdapter mUserRecyclerAdapter = new MapRecyclerViewAdapter(mContext, users);

        mUserRecyclerAdapter.setOnTopReachedListener(new MapRecyclerViewAdapter.OnTopReachedListener() {
            @Override
            public void onTopReached(int position) {
                //Top has reached, make button invisible.
                imageButton.setVisibility(View.GONE);
            }
        });

        mUserRecyclerAdapter.setOnScrollDownListener(new MapRecyclerViewAdapter.OnScrollDownListener() {
            @Override
            public void onScrolledDown() {

                if (imageButton.getVisibility() == View.GONE){

                    AnimatorSet animationSet =  new AnimatorSet();
                    imageButton.setScaleX(0.1f);
                    imageButton.setScaleY(0.1f);

                    ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(imageButton, "scaleY", 0.1f, 1f);
                    scaleDownY.setDuration(300);
                    scaleDownY.setInterpolator(DECELERATE_INTERPOLATOR);

                    ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(imageButton, "scaleX", 0.1f, 1f);
                    scaleDownX.setDuration(300);
                    scaleDownX.setInterpolator(DECELERATE_INTERPOLATOR);

                    imageButton.setVisibility(View.VISIBLE);

                    animationSet.playTogether(scaleDownY, scaleDownX);

                    animationSet.start();
                }
            }
        });

        AlphaInAnimationAdapter a = new AlphaInAnimationAdapter(mUserRecyclerAdapter);
        a.setDuration(1750);
        a.setInterpolator(new OvershootInterpolator());
        mUserListRecyclerView.setAdapter(a);
        LinearLayoutManager llm = new LinearLayoutManager(mContext);
        mUserListRecyclerView.setLayoutManager(llm);
    }

    private void getPositionForCurrentUser(ArrayList<User> users) {

        for (int i =0;i<users.size();i++){

            try {
                Log.d(TAG, "getPositionForCurrentUser: yeah " + users.get(i).getUser_id());
                if (users.get(i).getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    currentPosition = i;
                    break;
                }
            }catch (NullPointerException e){
                Log.d(TAG, "getPositionForCurrentUser: NullPointerException " + e.getMessage());
            }
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
    }//this should sort the list and provide the highest panda points holder

}
