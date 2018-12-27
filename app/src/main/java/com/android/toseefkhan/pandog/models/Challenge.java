package com.android.toseefkhan.pandog.models;

public class Challenge {

    private String challengerUserUid;
    private String challengedUserUid;
    private String photoKey;
    private String status;

    public Challenge(String challengerUserUid, String challengedUserUid, String photoKey) {
        this.challengerUserUid = challengerUserUid;
        this.challengedUserUid = challengedUserUid;
        this.photoKey = photoKey;
    }

    public Challenge() {
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
