package com.android.toseefkhan.pandog.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.android.toseefkhan.pandog.Utils.Like;

import java.util.ArrayList;
import java.util.List;

public class Comment implements Parcelable{

    private String comment;
    private String user_id;
    private String commentID;
    private ArrayList<MyMention> mentionArrayList;
    private ArrayList<Like> likes;

    public Comment() {
    }

    protected Comment(Parcel in) {
        comment = in.readString();
        user_id = in.readString();
        commentID = in.readString();
        likes = in.createTypedArrayList(Like.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(comment);
        dest.writeString(user_id);
        dest.writeString(commentID);
        dest.writeTypedList(likes);
    }

    @Override
    public int describeContents() {
        return 0;
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

    public String getCommentID() {
        return commentID;
    }

    public void setCommentID(String commentID) {
        this.commentID = commentID;
    }

    public ArrayList<MyMention> getMentionArrayList() {
        return mentionArrayList;
    }

    public void setMentionArrayList(ArrayList<MyMention> mentionArrayList) {
        this.mentionArrayList = mentionArrayList;
    }

    public ArrayList<Like> getLikes() {
        return likes;
    }

    public void setLikes(ArrayList<Like> likes) {
        this.likes = likes;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "comment='" + comment + '\'' +
                ", user_id='" + user_id + '\'' +
                ", commentID='" + commentID + '\'' +
                ", mentionArrayList=" + mentionArrayList +
                ", likes=" + likes +
                '}';
    }
}
