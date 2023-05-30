package com.iapp.lib.web;

import java.util.Date;

public class Punishment {

    private long id;
    private long moderatorId;
    private String cause;
    private Flag type;
    private boolean active;
    private long receivedTime;
    private long removedTime = 0;

    public Punishment() {}

    public Punishment(long id, long moderatorId, String cause, Flag type, boolean active) {
        this.id = id;
        this.moderatorId = moderatorId;
        this.cause = cause;
        this.type = type;
        this.active = active;
        this.receivedTime = new Date().getTime();
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public long getModerator() {
        return moderatorId;
    }

    public void setModerator(long moderatorId) {
        this.moderatorId = moderatorId;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public Flag getType() {
        return type;
    }

    public void setType(Flag type) {
        this.type = type;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        if (active) removedTime = new Date().getTime();
        else removedTime = 0;

        this.active = active;
    }

    public long getReceivedTime() {
        return receivedTime;
    }

    public void setReceivedTime(long receivedTime) {
        this.receivedTime = receivedTime;
    }

    public long getRemovedTime() {
        return removedTime;
    }

    public void setRemovedTime(long removedTime) {
        this.removedTime = removedTime;
    }

    @Override
    public String toString() {
        return "Punishment{" +
                "moderatorId=" + moderatorId +
                ", cause='" + cause + '\'' +
                ", type=" + type +
                ", active=" + active +
                ", receivedTime=" + receivedTime +
                ", removedTime=" + removedTime +
                '}';
    }
}
