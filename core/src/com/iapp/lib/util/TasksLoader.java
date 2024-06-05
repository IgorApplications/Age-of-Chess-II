package com.iapp.lib.util;

import com.badlogic.gdx.Gdx;
import com.iapp.lib.ui.screens.RdApplication;
import com.iapp.lib.ui.screens.RdLogger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class TasksLoader {

    private final List<Runnable> queue = new CopyOnWriteArrayList<>();
    private CallListener onFinish;
    private long sleepTime = 100;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private boolean finishLoading;

    public TasksLoader() {}

    public void addTask(Runnable task) {
        queue.add(task);
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    public void setOnFinish(CallListener onFinish) {
        this.onFinish = onFinish;
    }


    public void load() {
        if (finishLoading) {
            throw new IllegalStateException("Already launched!");
        }
        finishLoading = true;

        Runnable runnable = () -> {
            while (running.get()) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Gdx.app.error("launchLoad", RdLogger.self().getDescription(e));
                }

                if (!queue.isEmpty()) {
                    Runnable task = queue.remove(0);
                    RdApplication.postRunnable(task);
                }
            }

            if (onFinish != null) {
                onFinish.call();
            }
        };
        RdApplication.self().execute(runnable);
    }

    public void stop() {
        running.set(false);
    }

    public void loadFinish() {
        if (finishLoading) {
            throw new IllegalStateException("Already launched!");
        }
        finishLoading = true;

        Runnable runnable = () -> {
            while (!queue.isEmpty()) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Gdx.app.error("launchLoad", RdLogger.self().getDescription(e));
                }

                Runnable task = queue.remove(0);
                RdApplication.postRunnable(task);
            }

            if (onFinish != null) {
                onFinish.call();
            }
        };
        RdApplication.self().execute(runnable);
    }
}
