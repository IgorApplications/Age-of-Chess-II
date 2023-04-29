package com.iapp.ageofchess.lwjgl3;

import com.iapp.rodsher.screens.Launcher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RdLwjgl3Launcher implements Launcher {

    private ExecutorService executorService;

    @Override
    public void initPool(int threads) {
        executorService = Executors.newFixedThreadPool(threads);
    }

    @Override
    public void execute(Runnable task) {
        executorService.execute(task);
    }
}
