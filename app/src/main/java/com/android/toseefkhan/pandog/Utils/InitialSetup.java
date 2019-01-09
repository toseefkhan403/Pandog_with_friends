package com.android.toseefkhan.pandog.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import de.hdodenhof.circleimageview.CircleImageView;

public class InitialSetup extends Application {


    public ArrayList<User> mUserList;
    public ArrayList<MarkerOptions> markerOptionsList;


    @Override
    public void onCreate() {
        super.onCreate();

        mUserList = new ArrayList<>();
        markerOptionsList = new ArrayList<>();
        MapsInitializer.initialize(getApplicationContext());
        getUsersFromArea();
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

        MarkerOptions markerOptions;

        for (int i=0; i<mUserList.size(); i++){
            LatLng latLng= new LatLng(mUserList.get(i).getLat_lng().getLatitude(), mUserList.get(i).getLat_lng().getLongitude());
            markerOptions=new MarkerOptions().position(latLng).
                    icon(BitmapDescriptorFactory.fromBitmap(createMarker(getApplicationContext(),mUserList.get(i))))
                    .title(mUserList.get(i).getUsername())
                    .snippet("Points: " + String.valueOf(mUserList.get(i).getPanda_points()));
            markerOptionsList.add(markerOptions);
        }

    }

    private View marker;

    @SuppressLint({"NewApi", "StaticFieldLeak"})
    private Bitmap createMarker(Context context, final User user) {

        switch (user.getLevel()){

            case "BLACK":
                marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
                break;

            case "PURPLE":
                marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout2, null);
                break;

            case "BLUE":
                marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout3, null);
                break;

            case "GREEN":
                marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout4, null);
                break;

            case "GREY":
                marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout5, null);
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

        DisplayMetrics displayMetrics = new DisplayMetrics();
        displayMetrics =context.getResources().getDisplayMetrics();
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
