package com.android.toseefkhan.pandog.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Comment implements Parcelable{

    private String comment;
    private String user_id;
    private ArrayList<MyMention> mentionArrayList;

    public ArrayList<MyMention> getMentionArrayList() {
        return mentionArrayList;
    }

    public void setMentionArrayList(ArrayList<MyMention> mentionArrayList) {
        this.mentionArrayList = mentionArrayList;
    }

    public Comment(String comment, String user_id) {
        this.comment = comment;
        this.user_id = user_id;
    }

    public Comment() {
    }

    protected Comment(Parcel in) {
        comment = in.readString();
        user_id = in.readString();
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "comment='" + comment + '\'' +
                ", user_id='" + user_id + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(comment);
        dest.writeString(user_id);
    }
}
