package com.iapp.ageofchess.teavm;

import com.badlogic.gdx.Gdx;
import com.iapp.lib.ui.screens.Launcher;

import java.util.*;

public class RdTeaVmLauncher implements Launcher {

    @Override
    public void initPool(int threads) {}

    @Override
    public void execute(Runnable task) {
        Gdx.app.postRunnable(task);
    }

    @Override
    public <T> List<T> copyOnWriteArrayList() {
        return new ArrayList<>();
    }

    @Override
    public <K, V> Map<K, V> concurrentHashMap() {
        return new HashMap<>();
    }

    @Override
    public <E> Set<E> concurrentSkipListSet() {
        return new HashSet<>();
    }

    @Override
    public <E> Queue<E> concurrentLinkedQueue() {
        return new LinkedList<>();
    }
}
