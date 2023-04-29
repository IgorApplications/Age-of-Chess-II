package com.iapp.ageofchess.ios;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.iapp.rodsher.screens.Launcher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class RdIOSLauncher extends IOSApplication.Delegate implements Launcher {

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
