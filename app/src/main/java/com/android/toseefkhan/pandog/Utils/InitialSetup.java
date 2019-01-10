package com.android.toseefkhan.pandog.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.toseefkhan.pandog.Home.HomeActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.models.User;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.koushikdutta.ion.Ion;
import com.squareup.leakcanary.LeakCanary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import de.hdodenhof.circleimageview.CircleImageView;

public class InitialSetup extends Application {


    public ArrayList<User> mUserList;
    public ArrayList<MarkerOptions> markerOptionsList;
    public Boolean isTaskCompleted = false;

    private static final String TAG = "InitialSetup";

    @Override
    public void onCreate() {
        super.onCreate();

        mUserList = new ArrayList<>();
        markerOptionsList = new ArrayList<>();
        MapsInitializer.initialize(getApplicationContext());
        calcUser();
    }

    private ArrayList<User> mUserList2 = new ArrayList<>();

    private void calcUser(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user list: " + singleSnapshot.getValue());  // gives the whole user objects

                    try{
                        User user= singleSnapshot.getValue(User.class);
                        mUserList2.add(user);
                    }catch (Exception e){
                        Log.d(TAG, "onDataChange: NullPointerException " + e.getMessage());
                    }
                }
                setLevels(mUserList2);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setLevels(ArrayList<User> mUserList) {

        ArrayList<User> userList1 = new ArrayList<>();
        ArrayList<User> userList2 = new ArrayList<>();
        ArrayList<User> userList3 = new ArrayList<>();
        ArrayList<User> userList4 = new ArrayList<>();
        ArrayList<User> userList5 = new ArrayList<>();

        if (mUserList != null)
            mUserList = sortList(mUserList);         //sortList(mUserList);

        int level = mUserList.size()/5;

        for (int i=0; i<mUserList.size(); i++ ){

            if (i<=level)
                userList1.add(mUserList.get(i));         //level 5
            else if (i>level && i<=2*level)
                userList2.add(mUserList.get(i));         //level 4
            else if (i>level && i<=3*level)
                userList3.add(mUserList.get(i));
            else if (i>level && i<=4*level)
                userList4.add(mUserList.get(i));
            else if (i>level && i>4*level)
                userList5.add(mUserList.get(i));
        }
        Log.d(TAG, "setMarkerswithLevels: checking the lists " + userList1 +userList2 + userList3+ userList4+ userList5);

        DatabaseReference myRef;
        myRef= FirebaseDatabase.getInstance().getReference();

        for (int i = 0 ; i< userList1.size(); i++){

            User user = userList1.get(i);

            myRef.child(getString(R.string.dbname_users))
                    .child(user.getUser_id())
                    .child(getString(R.string.db_level))
                    .setValue("BLACK");
        }

        for (int i = 0 ; i< userList2.size(); i++){

            User user = userList2.get(i);

            myRef.child(getString(R.string.dbname_users))
                    .child(user.getUser_id())
                    .child(getString(R.string.db_level))
                    .setValue("PURPLE");
        }

        for (int i = 0 ; i< userList3.size(); i++){

            User user = userList3.get(i);

            myRef.child(getString(R.string.dbname_users))
                    .child(user.getUser_id())
                    .child(getString(R.string.db_level))
                    .setValue("BLUE");
        }

        for (int i = 0 ; i< userList4.size(); i++){

            User user = userList4.get(i);

            myRef.child(getString(R.string.dbname_users))
                    .child(user.getUser_id())
                    .child(getString(R.string.db_level))
                    .setValue("GREEN");
        }

        for (int i = 0 ; i< userList5.size(); i++){

            User user = userList5.get(i);

            myRef.child(getString(R.string.dbname_users))
                    .child(user.getUser_id())
                    .child(getString(R.string.db_level))
                    .setValue("GREY");
        }

        getUsersFromArea();
    }

    private ArrayList<User> sortList(ArrayList<User> userList) {

        Collections.sort(userList, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return Integer.valueOf(o2.getPanda_points()).compareTo(Integer.valueOf(o1.getPanda_points()));
            }
        });

        return userList;
    }

    private void getUsersFromArea(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                    try{
                        User user= singleSnapshot.getValue(User.class);
                        mUserList.add(user);
                    }catch (Exception e){
                        Log.d("Error", "onDataChange: NullPointerException " + e.getMessage());
                    }
                }
                createMarkers(mUserList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }//gives the list of ALL the users of the application

    private void createMarkers(ArrayList<User> mUserList) {

        Log.d(TAG, "createMarkers: markerlist that is required " + mUserList);
        MarkerOptions markerOptions;

        for (int i=0; i<mUserList.size(); i++){
            LatLng latLng= new LatLng(mUserList.get(i).getLat_lng().getLatitude(), mUserList.get(i).getLat_lng().getLongitude());
            markerOptions=new MarkerOptions().position(latLng).
                    icon(BitmapDescriptorFactory.fromBitmap(createMarker(mUserList.get(i))))
                    .title(mUserList.get(i).getUsername())
                    .snippet("Points: " + String.valueOf(mUserList.get(i).getPanda_points()));
            markerOptionsList.add(markerOptions);
        }

        isTaskCompleted= true;
        Intent i = new Intent(this, HomeActivity.class);
        startActivity(i);

    }

    private View marker;

    @SuppressLint({"NewApi", "StaticFieldLeak"})
    private Bitmap createMarker(final User user) {

        switch (user.getLevel()){

            case "BLACK":
                marker = ((LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
                break;

            case "PURPLE":
                marker = ((LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout2, null);
                break;

            case "BLUE":
                marker = ((LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout3, null);
                break;

            case "GREEN":
                marker = ((LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout4, null);
                break;

            case "GREY":
                marker = ((LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout5, null);
                break;

        }

        final CircleImageView markerImage = marker.findViewById(R.id.user_dp);
        try {
            Bitmap bmImg =  Ion.with(getApplicationContext())
                    .load(user.getBitmap())
                    .asBitmap().get();
            markerImage.setImageBitmap(bmImg);
        } catch (Exception e) {
            Log.d("Error", "createMarker: error");
        }

        DisplayMetrics displayMetrics;
        displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
      //  ((Application)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        marker.setLayoutParams(new ViewGroup.LayoutParams(52, ViewGroup.LayoutParams.WRAP_CONTENT));
        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        marker.draw(canvas);

        return bitmap;
    }

}
