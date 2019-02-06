package com.android.toseefkhan.pandog.Utils;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.toseefkhan.pandog.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ViewFollowingFragment extends Fragment {

    private static final String TAG = "ViewFollowersFragment";

    private RecyclerView recyclerView;
    private List<String> userIds = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_likes, container, false);

        recyclerView = view.findViewById(R.id.likes1);

        try {

            Bundle b = getArguments();

            if (b != null) {
                String uid = b.getString(getString(R.string.intent_user_id));
                getFollowersUid(uid);
            }

        } catch (NullPointerException e) {
            Log.d(TAG, "onCreateView: NullPointer " + e.getMessage());
        }


        return view;
    }

    private void getFollowersUid(String uid) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child(getString(R.string.dbname_following))
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                            userIds.add(singleSnapshot.child("user_id").getValue(String.class));
                        }
                        initRecyclerView();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void initRecyclerView() {

        Log.d(TAG, "initRecyclerView: useridlist " + userIds);
        if (!userIds.isEmpty()) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            UserListRVAdapter adapter = new UserListRVAdapter(getActivity(), userIds);
            recyclerView.setAdapter(adapter);
        } else if (userIds.isEmpty()) {
            try {
                Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), "NO FOLLOWING FOUND!", Snackbar.LENGTH_LONG).show();
            } catch (NullPointerException e) {
                Toast.makeText(getActivity(), "NO FOLLOWING FOUND!", Toast.LENGTH_SHORT).show();
            }
        }


    }
}
