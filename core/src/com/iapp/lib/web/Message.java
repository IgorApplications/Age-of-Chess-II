package com.iapp.lib.web;

public class Message {

    private long id;
    private boolean pinned;
    private long senderId;
    private long time;
    private String text;

    public Message() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", pinned=" + pinned +
                ", senderId=" + senderId +
                ", time=" + time +
                ", text='" + text + '\'' +
                '}';
    }
}
