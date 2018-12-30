package com.android.toseefkhan.pandog.models;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Challenge {

    private String challengerUserUid;
    private String challengedUserUid;
    private String photoKey;
    private String status;
    private String challengerName;
    private String challengedName;
    private String photoUrl;

    public Challenge(String challengerUserUid, String challengedUserUid, String photoKey, String photoUrl) {
        this.challengerUserUid = challengerUserUid;
        this.challengedUserUid = challengedUserUid;
        this.photoKey = photoKey;
        this.photoUrl = photoUrl;
        getNames();
    }

    public Challenge() {
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getChallengerName() {
        return challengerName;
    }

    public String getChallengedName() {
        return challengedName;
    }

    private void getNames() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(challengedUserUid).child("username").
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            challengedName = dataSnapshot.getValue(String.class);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        databaseReference.child("users").child(challengerUserUid).child("username").
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            challengerName = dataSnapshot.getValue(String.class);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public String getChallengerUserUid() {
        return challengerUserUid;
    }

    public void setChallengerUserUid(String challengerUserUid) {
        this.challengerUserUid = challengerUserUid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getChallengedUserUid() {
        return challengedUserUid;

    }

    public void setChallengedUserUid(String challengedUserUid) {
        this.challengedUserUid = challengedUserUid;
    }

    public String getPhotoKey() {
        return photoKey;
    }

    public void setPhotoKey(String photoKey) {
        this.photoKey = photoKey;
    }

}
