package com.iapp.lib.util;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.AbsoluteFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.ClasspathFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.ExternalFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.iapp.lib.ui.screens.RdApplication;
import com.iapp.lib.ui.screens.RdLogger;
import com.iapp.lib.util.CallListener;

import java.util.concurrent.atomic.AtomicBoolean;

public class AssetsLoader {

    private final AssetManager assetManager;
    private long sleepTime = 100;
    private int loadTime = 15;
    private CallListener onFinish = () -> {};
    private final AtomicBoolean fatal = new AtomicBoolean(false);

    public AssetsLoader(Files.FileType type) {
        if (type == Files.FileType.External) {
            assetManager = new AssetManager(new ExternalFileHandleResolver());
        } else if (type == Files.FileType.Absolute) {
            assetManager = new AssetManager(new AbsoluteFileHandleResolver());
        } else if (type == Files.FileType.Classpath) {
            assetManager = new AssetManager(new ClasspathFileHandleResolver());
        } else if (type == Files.FileType.Local) {
            assetManager = new AssetManager(new LocalFileHandleResolver());
        } else {
            assetManager = new AssetManager();
        }
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public long getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    public long getLoadTime() {
        return loadTime;
    }

    public void setLodTime(int loadTime) {
        this.loadTime = loadTime;
    }

    public CallListener getOnFinish() {
        return onFinish;
    }

    public void setOnFinish(CallListener onFinish) {
        this.onFinish = onFinish;
    }

    public void launchLoad() {
        Runnable task = () -> {
            while (!assetManager.isFinished() && !fatal.get()) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Gdx.app.error("launchLoad", RdLogger.self().getDescription(e));
                }

                RdApplication.postRunnable(() -> {
                    try {
                        assetManager.update(loadTime);
                    } catch (Throwable t) {
                        Gdx.app.error("launchLoad", RdLogger.self().getDescription(t));
                        RdLogger.self().showFatalScreen(t);
                        fatal.set(true);
                    }
                });
            }

            RdApplication.postRunnable(() -> {
                try {
                    assetManager.finishLoading();
                    onFinish.call();
                } catch (Throwable t) {
                    Gdx.app.error("launchLoad", RdLogger.self().getDescription(t));
                    if (fatal.get()) return;
                    RdLogger.self().showFatalScreen(t);
                }
            });
        };
        RdApplication.self().execute(task);
    }
}
