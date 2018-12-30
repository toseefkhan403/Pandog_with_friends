package com.android.toseefkhan.pandog.Home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.models.Challenge;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotificationFragment extends Fragment {


    private RecyclerView mNotificationRecyclerView;
    private ArrayList<Challenge> challengesList;
    private ProgressBar progressBar;
    private NotificationsAdapter notificationsAdapter;
    private DatabaseReference mDatabaseReference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        challengesList = new ArrayList<>();

        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        notificationsAdapter = new NotificationsAdapter(challengesList, getContext());

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

    private void addChallenge(String challengeKey) {
        mDatabaseReference.child(getString(R.string.db_challenges))
                .child(challengeKey)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Challenge challenge = dataSnapshot.getValue(Challenge.class);
                            challengesList.add(challenge);
                            notificationsAdapter.notifyItemInserted(challengesList.indexOf(challenge));
                            if (progressBar != null) {
                                if (progressBar.getVisibility() != View.GONE) {
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                        Log.d("DatabaseError", databaseError.toString());
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        mNotificationRecyclerView = view.findViewById(R.id.NotifsRecyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mNotificationRecyclerView.setLayoutManager(layoutManager);

        mNotificationRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mNotificationRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        mNotificationRecyclerView.setAdapter(notificationsAdapter);


        progressBar = view.findViewById(R.id.NotifProgressBar);
        return view;
    }
}
