package com.android.toseefkhan.pandog.Home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class InitActivity extends AppCompatActivity {

    private static final String TAG = "InitActivity";
    private Context mContext = InitActivity.this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.progress_anim);
        calcUser();

        long time = System.currentTimeMillis()+12000;
        long time2 = System.currentTimeMillis();

        while(time2 < time){
            time2 = System.currentTimeMillis();
        }

        if (time2 >= time) {
            Intent i = new Intent(this, HomeActivity.class);
            startActivity(i);
        }
    }

    private ArrayList<User> mUserList = new ArrayList<>();

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
                        mUserList.add(user);
                    }catch (Exception e){
                        Log.d(TAG, "onDataChange: NullPointerException " + e.getMessage());
                    }
                }
                setLevels(mUserList);
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

            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(user.getUser_id())
                    .child(mContext.getString(R.string.db_level))
                    .setValue("BLACK");
        }

        for (int i = 0 ; i< userList2.size(); i++){

            User user = userList2.get(i);

            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(user.getUser_id())
                    .child(mContext.getString(R.string.db_level))
                    .setValue("PURPLE");
        }

        for (int i = 0 ; i< userList3.size(); i++){

            User user = userList3.get(i);

            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(user.getUser_id())
                    .child(mContext.getString(R.string.db_level))
                    .setValue("BLUE");
        }

        for (int i = 0 ; i< userList4.size(); i++){

            User user = userList4.get(i);

            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(user.getUser_id())
                    .child(mContext.getString(R.string.db_level))
                    .setValue("GREEN");
        }

        for (int i = 0 ; i< userList5.size(); i++){

            User user = userList5.get(i);

            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(user.getUser_id())
                    .child(mContext.getString(R.string.db_level))
                    .setValue("GREY");
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
    }
}
