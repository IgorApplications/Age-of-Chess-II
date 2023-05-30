package com.iapp.lib.web;

import java.util.Objects;

public class LobbyMessage {

    private long id;
    private long senderId;
    private String text;

    public LobbyMessage(long id, long senderId, String text) {
        this.id = id;
        this.senderId = senderId;
        this.text = text;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "LobbyMessage{" +
            "id=" + id +
            ", senderId=" + senderId +
            ", text='" + text + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LobbyMessage message = (LobbyMessage) o;
        return id == message.id && senderId == message.senderId && Objects.equals(text, message.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, senderId, text);
    }
}
