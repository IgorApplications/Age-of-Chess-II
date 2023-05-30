package com.iapp.lib.web;

import java.util.Date;
import java.util.Objects;

public class Message {

    private long id;
    private boolean pinned;
    private long senderId;
    private long time;
    private String text;

    public Message() {}

    public Message(long id, long senderId, String text) {
        this.id = id;
        this.senderId = senderId;
        time = new Date().getTime();
        this.text = text;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return id == message.id && pinned == message.pinned && senderId == message.senderId && time == message.time && Objects.equals(text, message.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, pinned, senderId, time, text);
    }
}
