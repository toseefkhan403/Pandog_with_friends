package com.android.toseefkhan.pandog.models;

import com.percolate.mentions.Mentionable;

public class Mention implements Mentionable{

    private String mentionName;
    private String mentionUid;

    int mentionLength;
    int mentionOffset;

    public Mention(String mentionName, String mentionUid) {
        this.mentionName = mentionName;
        this.mentionUid = mentionUid;
    }

    public Mention() {
    }

    public String getMentionUid() {
        return mentionUid;
    }

    public void setMentionUid(String mentionUid) {
        this.mentionUid = mentionUid;
    }

    @Override
    public int getMentionOffset() {
        return mentionOffset;
    }

    @Override
    public void setMentionOffset(int i) {
        mentionOffset = i;
    }

    @Override
    public int getMentionLength() {
        return mentionLength;
    }

    @Override
    public void setMentionLength(int i) {
        mentionLength = i;
    }

    public String getMentionName() {
        return mentionName;
    }

    public void setMentionName(String mentionName) {
        this.mentionName = mentionName;
    }

    @Override
    public String toString() {
        return "Mention{" +
                "mentionName='" + mentionName + '\'' +
                ", mentionUid='" + mentionUid + '\'' +
                '}';
    }
}