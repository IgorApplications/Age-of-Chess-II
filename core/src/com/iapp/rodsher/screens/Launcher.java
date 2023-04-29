package com.iapp.rodsher.screens;

import com.iapp.rodsher.util.CallListener;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public interface Launcher {

    void initPool(int threads);

    void execute(Runnable task);

    default long currentMillis() {
        return System.currentTimeMillis();
    }

    default <T> List<T> copyOnWriteArrayList() {
        return new CopyOnWriteArrayList<>();
    }

    default <K, V> Map<K, V> concurrentHashMap() {
        return new ConcurrentHashMap<>();
    }

    default <E> Set<E> concurrentSkipListSet() {
        return new ConcurrentSkipListSet<>();
    }

    default <E> Queue<E> concurrentLinkedQueue() {
        return new ConcurrentLinkedQueue<>();
    }

    default void setOnFinish(CallListener onFinish) {}

    default void verifyStoragePermissions(Consumer<Boolean> listener) {}

    default boolean checkStoragePermissions() {
        return false;
    }
}
