package com.iapp.ageofchess.multiplayer;

import java.util.Date;

public class Punishment {

    private String moderator;
    private String cause;
    private PunishmentType type;
    private boolean active;
    private long receivedTime;
    private long removedTime = 0;

    public Punishment(String moderator, String cause, PunishmentType type, boolean active) {
        this.moderator = moderator;
        this.cause = cause;
        this.type = type;
        this.active = active;
        this.receivedTime = new Date().getTime();
    }

    public String getModerator() {
        return moderator;
    }

    public void setModerator(String moderator) {
        this.moderator = moderator;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public PunishmentType getType() {
        return type;
    }

    public void setType(PunishmentType type) {
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
                "moderator='" + moderator + '\'' +
                ", cause='" + cause + '\'' +
                ", type=" + type +
                ", active=" + active +
                ", receivedTime=" + receivedTime +
                ", removedTime=" + removedTime +
                '}';
    }
}
