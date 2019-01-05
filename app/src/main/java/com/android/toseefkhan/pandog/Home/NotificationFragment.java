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
import com.android.toseefkhan.pandog.Utils.RecyclerViewAdapter;
import com.android.toseefkhan.pandog.models.Challenge;
import com.android.toseefkhan.pandog.models.User;
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
    private ArrayList<Challenge> challengesList= new ArrayList<>();
    private ProgressBar progressBar;
    private NotificationsAdapter notificationsAdapter;
    private DatabaseReference mDatabaseReference;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        Log.d(TAG, "onCreateView: called");
        mNotificationRecyclerView = view.findViewById(R.id.NotifsRecyclerView);

        progressBar = view.findViewById(R.id.NotifProgressBar);
        progressBar.setVisibility(View.VISIBLE);

        notificationsAdapter = new NotificationsAdapter(challengesList, getContext());

        String userUid = "something";
        try{
            userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }catch (NullPointerException e){
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

        return view;
    }

    private void addChallenge(String challengeKey) {

        Log.d(TAG, "addChallenge: challengekey " + challengeKey);
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
                        Log.d(TAG, "onDataChange: are you empty " + challengesList);
                        initUserListRecyclerView();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                        Log.d("DatabaseError", databaseError.toString());
                    }
                });

    }

    private void initUserListRecyclerView() {

        mNotificationRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mNotificationRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mNotificationRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        NotificationsAdapter notificationAdapter = new NotificationsAdapter(challengesList, getContext());
        mNotificationRecyclerView.setAdapter(notificationAdapter);
    }

}