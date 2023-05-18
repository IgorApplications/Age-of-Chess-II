package com.iapp.lib.util;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * class for performing actions to load resources
 * @version 1.0
 * */
public abstract class TaskLoad {

    /** true if task finished */
    private final AtomicBoolean finished = new AtomicBoolean(false);

    /** resource loading */
    public abstract void load();

    /** returns true if task finished */
    public boolean isFinished() {
        return finished.get();
    }

    /** sets the task completion flag */
    public void setFinished(boolean isDownload) {
        this.finished.set(isDownload);
    }
}
