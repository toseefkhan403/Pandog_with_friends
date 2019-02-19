package com.android.toseefkhan.pandog.Home;

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
    private RelativeLayout rel;

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

        mDatabaseReference.child(getString(R.string.db_user_challenges))
                .child(userUid)
                .orderByKey()
                .addChildEventListener(new ChildEventListener() {
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
                });

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        Log.d(TAG, "onCreateView: called");
        mNotificationRecyclerView = view.findViewById(R.id.NotifsRecyclerView);

        progressBar = view.findViewById(R.id.NotifProgressBar);
        rel = view.findViewById(R.id.noNotifs);
        progressBar.setVisibility(View.GONE);

        initUserListRecyclerView();

        return view;
    }

    private void addChallenge(final String challengeKey) {
        if (isAdded()) {
            Log.d(TAG, "addChallenge: challengekey " + challengeKey);
            mDatabaseReference.child(getString(R.string.db_challenges))
                    .child(challengeKey)
                    .addValueEventListener(new ValueEventListener() {
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
                                progressBar.setVisibility(View.GONE);
                                rel.setVisibility(View.VISIBLE);
                            } else {
                                rel.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
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

}