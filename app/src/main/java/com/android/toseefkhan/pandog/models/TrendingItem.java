package com.android.toseefkhan.pandog.models;

import java.util.ArrayList;

public class TrendingItem {

   private String title;
   private ArrayList<String> post_keys_list;

    public TrendingItem(String title, ArrayList<String> post_keys_list) {
        this.title = title;
        this.post_keys_list = post_keys_list;
    }

    public TrendingItem() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<String> getPost_keys_list() {
        return post_keys_list;
    }

    public void setPost_keys_list(ArrayList<String> post_keys_list) {
        this.post_keys_list = post_keys_list;
    }

    @Override
    public String toString() {
        return "TrendingItem{" +
                "title='" + title + '\'' +
                ", post_keys_list=" + post_keys_list +
                '}';
    }
}


