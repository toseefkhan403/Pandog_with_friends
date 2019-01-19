package com.android.toseefkhan.pandog.models;

public class Challenge {
    private String challengerUserUid;
    private String challengedUserUid;
    private String photoKey;
    private String status;
    private String challengerName;
    private String challengedName;
    private String photoUrl;
    private String challengeKey;
    private String caption;
    private String tags;

    public Challenge(String challengerUserUid, String challengedUserUid, String photoKey, String photoUrl,String caption,String tags) {
        this.challengerUserUid = challengerUserUid;
        this.challengedUserUid = challengedUserUid;
        this.photoKey = photoKey;
        this.photoUrl = photoUrl;
        this.caption = caption;
        this.tags = tags;
    }

    public Challenge() {
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getChallengeKey() {
        return challengeKey;
    }

    public void setChallengeKey(String challengeKey) {
        this.challengeKey = challengeKey;
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

    public void setChallengerName(String challengerName) {
        this.challengerName = challengerName;
    }

    public String getChallengedName() {
        return challengedName;
    }

    public void setChallengedName(String challengedName) {
        this.challengedName = challengedName;
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
