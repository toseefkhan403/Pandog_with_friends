package com.android.toseefkhan.pandog.models;

public class UserAccountSettings {

    private String description;
    private String display_name;
    private String profile_photo;
    private String username;
    private String instagram_username;

    public UserAccountSettings() {
    }

    public UserAccountSettings(String description, String display_name, String profile_photo, String username,String instagram_username) {
        this.description = description;
        this.display_name = display_name;
        this.profile_photo = profile_photo;
        this.username = username;
        this.instagram_username = instagram_username;
    }

    public String getInstagram_username() {
        return instagram_username;
    }

    public void setInstagram_username(String instagram_username) {
        this.instagram_username = instagram_username;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getProfile_photo() {
        return profile_photo;
    }

    public void setProfile_photo(String profile_photo) {
        this.profile_photo = profile_photo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "UserAccountSettings{" +
                "description='" + description + '\'' +
                ", display_name='" + display_name + '\'' +
                ", profile_photo='" + profile_photo + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
