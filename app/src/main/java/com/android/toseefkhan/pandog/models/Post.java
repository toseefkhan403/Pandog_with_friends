package com.android.toseefkhan.pandog.models;

import com.android.toseefkhan.pandog.Utils.Like;

import java.util.HashMap;
import java.util.List;

public class Post {

    private String image_url;
    private String caption;
    private String photo_id;
    private String user_id;
    private String tags;
    private List<Like> likes;

    private String image_url2;
    private String caption2;
    private String photo_id2;
    private String user_id2;
    private String tags2;
    private List<Like> likes2;
    private HashMap<String,String> comments = new HashMap<>();  //<uid,comment>

    private String postKey;

    public Post() {
    }

    public Post(String image_url, String caption, String photo_id, String user_id, String tags,
                String image_url2, String caption2, String photo_id2, String user_id2, String tags2) {
        this.image_url = image_url;
        this.caption = caption;
        this.photo_id = photo_id;
        this.user_id = user_id;
        this.tags = tags;
        this.image_url2 = image_url2;
        this.caption2 = caption2;
        this.photo_id2 = photo_id2;
        this.user_id2 = user_id2;
        this.tags2 = tags2;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getPhoto_id() {
        return photo_id;
    }

    public void setPhoto_id(String photo_id) {
        this.photo_id = photo_id;
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

    public String getPhoto_id2() {
        return photo_id2;
    }

    public void setPhoto_id2(String photo_id2) {
        this.photo_id2 = photo_id2;
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

    public HashMap<String, String> getComments() {
        return comments;
    }

    public void setComments(HashMap<String, String> comments) {
        this.comments = comments;
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
                ", photo_id='" + photo_id + '\'' +
                ", user_id='" + user_id + '\'' +
                ", tags='" + tags + '\'' +
                ", likes=" + likes +
                ", image_url2='" + image_url2 + '\'' +
                ", caption2='" + caption2 + '\'' +
                ", photo_id2='" + photo_id2 + '\'' +
                ", user_id2='" + user_id2 + '\'' +
                ", tags2='" + tags2 + '\'' +
                ", likes2=" + likes2 +
                ", comments=" + comments +
                ", PostKey='" + postKey + '\'' +
                '}';
    }
}
