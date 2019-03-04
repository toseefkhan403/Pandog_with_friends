package com.android.toseefkhan.pandog.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.android.toseefkhan.pandog.Utils.Like;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Post implements Parcelable{

    private String image_url;
    private String caption;
    private String user_id;
    private String tags;
    private List<Like> likes;

    private String image_url2;
    private String caption2;
    private String user_id2;
    private String tags2;
    private List<Like> likes2;
    private List<Comment> comments;

    private String postKey;

    private long timeStamp;
    private String challenge_id, status, winner;
    private HashMap<String,ArrayList<MyMention>> mention_hash_map;


    public Post() {
    }

    public Post(String image_url, String caption, String user_id, String tags,
                String image_url2, String caption2, String user_id2, String tags2) {
        this.image_url = image_url;
        this.caption = caption;
        this.user_id = user_id;
        this.tags = tags;
        this.image_url2 = image_url2;
        this.caption2 = caption2;
        this.user_id2 = user_id2;
        this.tags2 = tags2;
    }


    protected Post(Parcel in) {
        image_url = in.readString();
        caption = in.readString();
        user_id = in.readString();
        tags = in.readString();
        likes = in.createTypedArrayList(Like.CREATOR);
        image_url2 = in.readString();
        caption2 = in.readString();
        user_id2 = in.readString();
        tags2 = in.readString();
        likes2 = in.createTypedArrayList(Like.CREATOR);
        comments = in.createTypedArrayList(Comment.CREATOR);
        postKey = in.readString();
        timeStamp = in.readLong();
        challenge_id = in.readString();
        status = in.readString();
        winner = in.readString();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public HashMap<String, ArrayList<MyMention>> getMention_hash_map() {
        return mention_hash_map;
    }

    public void setMention_hash_map(HashMap<String, ArrayList<MyMention>> mention_hash_map) {
        this.mention_hash_map = mention_hash_map;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }

    public String getImage_url2() {
        return image_url2;
    }

    public void setImage_url2(String image_url2) {
        this.image_url2 = image_url2;
    }

    public String getCaption2() {
        return caption2;
    }

    public void setCaption2(String caption2) {
        this.caption2 = caption2;
    }

    public String getUser_id2() {
        return user_id2;
    }

    public void setUser_id2(String user_id2) {
        this.user_id2 = user_id2;
    }

    public String getTags2() {
        return tags2;
    }

    public void setTags2(String tags2) {
        this.tags2 = tags2;
    }

    public List<Like> getLikes2() {
        return likes2;
    }

    public void setLikes2(List<Like> likes2) {
        this.likes2 = likes2;
    }

    public String getPostKey() {
        return postKey;
    }

    public void setPostKey(String postKey) {
        this.postKey = postKey;
    }

    @Override
    public String toString() {
        return "Post{" +
                "image_url='" + image_url + '\'' +
                ", caption='" + caption + '\'' +
                ", user_id='" + user_id + '\'' +
                ", tags='" + tags + '\'' +
                ", likes=" + likes +
                ", image_url2='" + image_url2 + '\'' +
                ", caption2='" + caption2 + '\'' +
                ", user_id2='" + user_id2 + '\'' +
                ", tags2='" + tags2 + '\'' +
                ", likes2=" + likes2 +
                ", comments=" + comments +
                ", PostKey='" + postKey + '\'' +
                '}';
    }


    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getChallenge_id() {
        return challenge_id;
    }

    public void setChallenge_id(String challenge_id) {
        this.challenge_id = challenge_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(image_url);
        parcel.writeString(caption);
        parcel.writeString(user_id);
        parcel.writeString(tags);
        parcel.writeTypedList(likes);
        parcel.writeString(image_url2);
        parcel.writeString(caption2);
        parcel.writeString(user_id2);
        parcel.writeString(tags2);
        parcel.writeTypedList(likes2);
        parcel.writeTypedList(comments);
        parcel.writeString(postKey);
        parcel.writeLong(timeStamp);
        parcel.writeString(challenge_id);
        parcel.writeString(status);
        parcel.writeString(winner);
    }
}
