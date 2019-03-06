package com.android.toseefkhan.pandog.Home;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.PullToRefreshView;
import com.android.toseefkhan.pandog.models.Challenge;
import com.dingmouren.layoutmanagergroup.echelon.EchelonLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotificationFragment extends Fragment {

    private static final String TAG = "NotificationFragment";

    private RecyclerView mNotificationRecyclerView;
    private ArrayList<Challenge> challengesList = new ArrayList<>();
    private ProgressBar progressBar;
    private NotificationsAdapter notificationsAdapter;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference ref1;
    private DatabaseReference ref2;
    private RelativeLayout rel;
    private ValueEventListener v1;
    private ChildEventListener v2;


    @Override
    public void onResume() {
        super.onResume();

//        notificationsAdapter = new NotificationsAdapter(challengesList, getContext());
    }

    @Override
    public void onPause() {

//        mNotificationRecyclerView.setAdapter(null);
        if (ref1 != null && v1 != null)
            ref1.removeEventListener(v1);

        if (ref2 != null && v2 != null)
            ref2.removeEventListener(v2);

        if (mNotificationRecyclerView != null){
            mNotificationRecyclerView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    // no-op
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    if (mNotificationRecyclerView != null)
                        mNotificationRecyclerView.setAdapter(null);
                }
            });
        }

        super.onPause();
    }

    @Override
    public void onDestroy() {

        if (ref1 != null && v1 != null)
            ref1.removeEventListener(v1);

        if (ref2 != null && v2 != null)
            ref2.removeEventListener(v2);

        if (mNotificationRecyclerView != null){
            mNotificationRecyclerView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    // no-op
                }

                @Override
                public void onViewDetachedFromWindow(View v) {

                    if (mNotificationRecyclerView != null)
                        mNotificationRecyclerView.setAdapter(null);
                }
            });
        }

        super.onDestroy();
    }

    @Override
    public void onStop() {

        if (ref1 != null && v1 != null)
            ref1.removeEventListener(v1);

        if (ref2 != null && v2 != null)
            ref2.removeEventListener(v2);

        if (mNotificationRecyclerView != null){
            mNotificationRecyclerView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    // no-op
                }

                @Override
                public void onViewDetachedFromWindow(View v) {

                    if (mNotificationRecyclerView != null)
                        mNotificationRecyclerView.setAdapter(null);
                }
            });
        }

        super.onStop();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        notificationsAdapter = new NotificationsAdapter(challengesList, getContext());

        String userUid = "something";
        try {
            userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } catch (NullPointerException e) {
            Log.d(TAG, "onCreateView: " + e.getMessage());
        }

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        ref2 = mDatabaseReference.child(getString(R.string.db_user_challenges))
                .child(userUid);
        v2 = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    String ChallengeKey = dataSnapshot.getValue(String.class);
                    Log.d("String", "ChildAdded" + s);
                    addChallenge(ChallengeKey);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Db ERROR", databaseError.toString());
            }
        };

        ref2.orderByKey().addChildEventListener(v2);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        Log.d(TAG, "onCreateView: called");
        mNotificationRecyclerView = view.findViewById(R.id.NotifsRecyclerView);

        PullToRefreshView mPullToRefreshView = view.findViewById(R.id.pull_to_refresh);

        mPullToRefreshView.setOnRefreshListener(() -> mPullToRefreshView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPullToRefreshView.setRefreshing(false);
                Intent i = new Intent(getActivity(),HomeActivity.class);
                i.putExtra("ChallengerUser",2);
                startActivity(i);
                getActivity().overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        }, 1000));

        progressBar = view.findViewById(R.id.NotifProgressBar);
        rel = view.findViewById(R.id.noNotifs);
        progressBar.setVisibility(View.GONE);

        initUserListRecyclerView();

        return view;
    }

    private void addChallenge(final String challengeKey) {
        if (isAdded()) {
            Log.d(TAG, "addChallenge: challengekey " + challengeKey);

            v1 = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Challenge challenge = dataSnapshot.getValue(Challenge.class);
                        if (challenge.getStatus().equals("NOT_DECIDED")) {

                            if (notificationsAdapter.doesChallengeExist(challenge.getChallengeKey())) {
                                int challlengeIndex = notificationsAdapter.getIndexOfChallenge(challenge.getChallengeKey());
                                challengesList.set(challlengeIndex, challenge);
                                notificationsAdapter.updateList(challengesList,challenge);
                            } else {
                                challengesList.add(challenge);
                                notificationsAdapter.changeList(challengesList,challenge);
                                if (progressBar != null) {
                                    if (progressBar.getVisibility() != View.GONE) {
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            }
                        }
                    }
                    if (challengesList.isEmpty()) {
                        if (progressBar != null)
                            progressBar.setVisibility(View.GONE);
                        rel.setVisibility(View.VISIBLE);
                    } else {
                        rel.setVisibility(View.GONE);
                    }

                    mNotificationRecyclerView.smoothScrollToPosition(0);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            ref1 = mDatabaseReference.child(getString(R.string.db_challenges))
                    .child(challengeKey);

            ref1.addValueEventListener(v1);
        }
    }

    private void initUserListRecyclerView() {

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mNotificationRecyclerView.setLayoutManager(mLayoutManager);

        notificationsAdapter = new NotificationsAdapter(challengesList, getContext());
        AlphaInAnimationAdapter a = new AlphaInAnimationAdapter(notificationsAdapter);
        Log.d(TAG, "initUserListRecyclerView: empty " + challengesList);
        mNotificationRecyclerView.setAdapter(a);
    }

    @Override
    public void onDestroyView() {

        mNotificationRecyclerView = null;

        super.onDestroyView();
    }

}
