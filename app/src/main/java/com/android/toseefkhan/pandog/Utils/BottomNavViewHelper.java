package com.android.toseefkhan.pandog.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.android.toseefkhan.pandog.Map.MapActivity2;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;

import com.android.toseefkhan.pandog.Home.HomeActivity;
import com.android.toseefkhan.pandog.Map.MapActivity;
import com.android.toseefkhan.pandog.Profile.ProfileActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Search.SearchActivity;
import com.android.toseefkhan.pandog.Share.ShareActivity;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;



public class BottomNavViewHelper {

    private static final String TAG = "BottomNavigationViewHel";

    public static void setupBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx){
        try {
            Log.d(TAG, "setupBottomNavigationView: Setting up BottomNavigationView");
            bottomNavigationViewEx.enableAnimation(true);
            bottomNavigationViewEx.enableItemShiftingMode(false);
            bottomNavigationViewEx.enableShiftingMode(false);
            bottomNavigationViewEx.setTextVisibility(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void enableNavigation(final Context context, BottomNavigationViewEx view, Activity activity){
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){

                    case R.id.ic_house:
                        Intent intent1 = new Intent(context, HomeActivity.class);//ACTIVITY_NUM = 0
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        context.startActivity(intent1);
                        activity.overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                        ((Activity)context).finish();
                        break;

                    case R.id.ic_cloud:
                        Intent intent2  = new Intent(context, MapActivity2.class);//ACTIVITY_NUM = 1
                        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        context.startActivity(intent2);
                        activity.overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                        break;

                    case R.id.ic_circle:
                        Intent intent3 = new Intent(context, ShareActivity.class);//ACTIVITY_NUM = 2
                        context.startActivity(intent3);
                        activity.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                        break;

                    case R.id.ic_search:
                        Intent intent4 = new Intent(context, SearchActivity.class);//ACTIVITY_NUM = 3
                        intent4.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        context.startActivity(intent4);
                        activity.overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                        break;

                    case R.id.ic_android:
                        Intent intent5 = new Intent(context, ProfileActivity.class);//ACTIVITY_NUM = 4
                        intent5.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        context.startActivity(intent5);
                        activity.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                        break;
                }

                return false;
            }
        });
    }
}