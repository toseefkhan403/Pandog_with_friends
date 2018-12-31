package com.android.toseefkhan.pandog.models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class User implements Parcelable {

    private String profile_photo;
    private String user_id;
    private String email;
    private String username;
    private LatLong lat_lng;
    private int panda_points;
    private String bitmap;

    public User() {
    }

    public User(String profile_photo, String user_id, String email, String username, LatLong lat_lng, int panda_points) {
        this.profile_photo = profile_photo;
        this.user_id = user_id;
        this.email = email;
        this.username = username;
        this.lat_lng = lat_lng;
        this.panda_points = panda_points;
    }

    public User(String profile_photo, String user_id, String email, String username) {
        this.profile_photo = profile_photo;
        this.user_id = user_id;
        this.email = email;
        this.username = username;
    }

    public String getBitmap() {
        return bitmap;
    }

    public void setBitmap(String bitmap) {
        this.bitmap = bitmap;
    }

    public int getPanda_points() {
        return panda_points;
    }

    public void setPanda_points(int panda_points) {
        this.panda_points = panda_points;
    }

    public User(String user_id, String email, String username) {
        this.user_id = user_id;
        this.email = email;
        this.username = username;
    }

    protected User(Parcel in) {
        profile_photo = in.readString();
        user_id = in.readString();
        email = in.readString();
        username = in.readString();
        lat_lng = in.readParcelable(LatLng.class.getClassLoader());
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getProfile_photo() {
        return profile_photo;
    }

    public void setProfile_photo(String profile_photo) {
        this.profile_photo = profile_photo;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getEmail() {
        return email;
    }

    public LatLong getLat_lng() {
        return lat_lng;
    }

    public void setLat_lng(LatLong lat_lng) {
        this.lat_lng = lat_lng;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "User{" +
                "user_id='" + user_id + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(profile_photo);
        dest.writeString(user_id);
        dest.writeString(email);
        dest.writeString(username);
    }
}
