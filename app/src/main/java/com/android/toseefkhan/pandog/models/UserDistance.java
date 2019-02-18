package com.android.toseefkhan.pandog.models;

public class UserDistance {

    private User user;
    private int distance;

    public UserDistance(User user, int distance) {
        this.user = user;
        this.distance = distance;
    }

    public UserDistance() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "UserDistance{" +
                "user=" + user +
                ", distance=" + distance +
                '}';
    }
}
