package com.android.toseefkhan.pandog.models;

import java.util.HashMap;

public class Notif {

    private String mTitle;
    private String mDescription;
    private String mImgUrl;
    private HashMap<String, Object> mIntentExtra;


    public Notif(String mTitle, String mDescription, String mImgUrl, HashMap<String, Object> mIntentExtra) {
        this.mTitle = mTitle;
        this.mDescription = mDescription;
        this.mImgUrl = mImgUrl;
        this.mIntentExtra = mIntentExtra;
    }

    public Notif() {
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmDescription() {
        return mDescription;
    }

    public void setmDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getmImgUrl() {
        return mImgUrl;
    }

    public void setmImgUrl(String mImgUrl) {
        this.mImgUrl = mImgUrl;
    }

    public HashMap<String, Object> getmIntentExtra() {
        return mIntentExtra;
    }

    public void setmIntentExtra(HashMap<String, Object> mIntentExtra) {
        this.mIntentExtra = mIntentExtra;
    }

    @Override
    public String toString() {
        return "Notif{" +
                "mTitle='" + mTitle + '\'' +
                ", mDescription='" + mDescription + '\'' +
                ", mImgUrl='" + mImgUrl + '\'' +
                ", mIntentExtra=" + mIntentExtra +
                '}';
    }
}
