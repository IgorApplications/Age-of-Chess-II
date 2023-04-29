package com.iapp.rodsher.util;

import com.badlogic.gdx.Gdx;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Timer with control methods, updated only once per second and
 * running in another thread, but completely thread-safe for the user!
 * @author Igor Ivanov
 * @version 1.0
 * */
public class Timer implements Runnable {

    /** stop flag */
    private final AtomicBoolean stop = new AtomicBoolean(false);
    /** time in millis */
    private final AtomicLong time;
    /** state working */
    private final AtomicBoolean completing = new AtomicBoolean(false);
    /** listener on events */
    private volatile CallListener onSecond, onFinish;

    /**
     * Creates a timer with a constant time that is restartable.
     * For start, call resume.
     * @param timeMillis - time scale timer
     * */
    public Timer(long timeMillis) {
        time = new AtomicLong(timeMillis);
    }

    /** temporarily stop the timer */
    public void pause() {
        completing.set(false);
    }

    /** first timer start or unpause */
    public void resume() {
        completing.set(true);
    }

    /** set listener to pass 1 second, calls will be called on the graphics thread */
    public void setOnSecond(CallListener onSecond) {
        this.onSecond = onSecond;
    }

    /** set a listener for completion time, will be called on the graphics thread */
    public void setOnFinish(CallListener onFinish) {
        this.onFinish = onFinish;
    }

    /** returns true if time is over */
    public boolean isTimeOver() {
        return time.get() <= 0;
    }

    /** returns left millis of time */
    public long getLeftMillis() {
        return time.get();
    }

    /** stops the timer during 1 second,
     * but it won't make listener callbacks anymore.
     * If you don't call this method when the time is up, it will sleep.
     * */
    public void stop() {
        stop.set(true);
    }

    /**
     * restarts the timer if it has not been stopped,
     * also, it does not unpause the timer!
     * */
    public void resetTime(long timeMillis) {
        time.set(timeMillis);
    }

    @Override
    public void run() {
        while (!stop.get() && !Thread.currentThread().isInterrupted()) {
            Thread.yield();
            try {
                Thread.sleep(980);
            } catch (InterruptedException e) {
                e.printStackTrace(System.out);
                return;
            }
            if (stop.get()) return;

            if (!completing.get() || isTimeOver()) continue;
            time.set(time.get() - 1000);

            if (onSecond != null) {
                Gdx.app.postRunnable(onSecond::call);
            }
            if (isTimeOver() && onFinish != null) {
                Gdx.app.postRunnable(onFinish::call);
            }
        }
    }
}
