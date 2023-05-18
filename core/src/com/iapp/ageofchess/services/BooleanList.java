package com.iapp.ageofchess.services;

import com.iapp.lib.ui.screens.RdApplication;

import java.util.List;

public final class BooleanList {

    private final List<Boolean> booleanLinkedList = RdApplication.self().getLauncher().copyOnWriteArrayList();

    public BooleanList() {}

    public boolean get() {
        return !booleanLinkedList.isEmpty();
    }

    public void addFalse() {
        if (booleanLinkedList.isEmpty()) return;
        booleanLinkedList.remove(booleanLinkedList.size() - 1);
    }

    public void addTrue() {
        booleanLinkedList.add(true);
    }

    public void add(boolean value) {
        if (value) addTrue();
        else addFalse();
    }
}
