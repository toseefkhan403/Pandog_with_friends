package com.android.toseefkhan.pandog.Map;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.models.LatLong;
import com.android.toseefkhan.pandog.models.User;
import com.android.toseefkhan.pandog.models.UserDistance;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AroundYouList extends Fragment {

    private static final String TAG = "AroundYouList";
    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;

    private MapRecyclerViewAdapter2 mUserRecyclerAdapter;
    private RecyclerView mUserListRecyclerView;
    private ProgressBar mProgressbar;
    private double latitude,longitude;
    private RelativeLayout permsNull;
    private DatabaseReference ref;
    private TextView gps;

    private Context mContext;
    private ArrayList<UserDistance> userList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_around_you_list, container, false);

        mUserListRecyclerView = view.findViewById(R.id.user_list_recycler_view);
        permsNull = view.findViewById(R.id.permission_null);

        mProgressbar = view.findViewById(R.id.progressBar);
        mProgressbar.setVisibility(View.VISIBLE);

        gps = view.findViewById(R.id.tvEnableGps);

        gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
            }
        });

        mContext = view.getContext();

        getCurrentUserPosition();

        return view;
    }

    private void getCurrentUserPosition() {

        ref = FirebaseDatabase.getInstance().getReference();

        ref.child(getString(R.string.dbname_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);

                        try {
                            LatLong latLong = user.getLat_lng();
                            latitude = latLong.getLatitude();
                            longitude = latLong.getLongitude();
                            getDistancesForUsers();
                        }catch(NullPointerException e){
                            Log.d(TAG, "onDataChange: the current user doesn't have a latlng");
                            permsNull.setVisibility(View.VISIBLE);
                            mProgressbar.setVisibility(View.GONE);
                            Toasty.warning(mContext,"Enable GPS and refresh the page",Toasty.LENGTH_LONG,true).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {

            }
        }
    }

    private void getDistancesForUsers() {

        ref.child(getString(R.string.dbname_users))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ss : dataSnapshot.getChildren()){

                            User user = ss.getValue(User.class);

                            if (!user.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                try {
                                    double distance = distance(latitude, longitude, user.getLat_lng().getLatitude(), user.getLat_lng().getLongitude());
                                    distance = Math.round(distance);
                                    if (distance == 0)
                                        distance = 1;
                                    userList.add(new UserDistance(user, (int) distance));
                                } catch (NullPointerException e) {
                                    Log.d(TAG, "onDataChange: User doesn't have latlng");
                                }
                            }
                        }

                        ArrayList<UserDistance> finalList = sortList(userList);

                        if (finalList.size() <= 30){
                            initUserListRecyclerView(finalList);
                        }else{

                            ArrayList<UserDistance> shortenedList = new ArrayList<>() ;

                            for (int i = 0 ; i<30 ; i++)
                                shortenedList.add(finalList.get(i));

                            initUserListRecyclerView(shortenedList);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private ArrayList<UserDistance> sortList(ArrayList<UserDistance> userList) {

        Collections.sort(userList, new Comparator<UserDistance>() {
            @Override
            public int compare(UserDistance userDistance, UserDistance t1) {
                return Integer.valueOf(userDistance.getDistance()).compareTo(Integer.valueOf(t1.getDistance()));
            }
        });

        return userList;
    }//this should sort the list and provide the highest panda points holder


    private void initUserListRecyclerView(ArrayList<UserDistance> users) {
        mProgressbar.setVisibility(View.GONE);
        mUserRecyclerAdapter = new MapRecyclerViewAdapter2(mContext , users);
        AlphaInAnimationAdapter a = new AlphaInAnimationAdapter(mUserRecyclerAdapter);
        a.setDuration(1750);
        a.setInterpolator(new OvershootInterpolator());
        mUserListRecyclerView.setAdapter(a);
        mUserListRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

}
