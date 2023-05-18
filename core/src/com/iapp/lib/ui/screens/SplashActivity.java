package com.iapp.lib.ui.screens;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.iapp.lib.util.DisposeUtil;
import com.iapp.lib.util.TaskLoad;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * This class loads the required resources
 * in the standard graphics window.
 * @author Igor Icanov
 * @version 1.0
 * */
public class SplashActivity extends Activity {

    /** this screen will be launched after loading all resources */
    private final Supplier<Activity> nextScreen;
    /** time at the start of the start window */
    private final long startMillis;
    /**
     * the start window will be displayed at least this
     * much time if loading resources is faster
     * */
    private final long minDelayMillis;
    /** tasks to execute on the graphics thread */
    private final TaskLoad[] taskLoads;

    private final Texture textureLogo, textureTitleLogo;
    private Image logo, titleLogo;
    private boolean intent, launchLoad;

    /** @see SplashActivity#loadLibrary(Supplier, long, Texture, Texture, TaskLoad...)  */
    public static void loadLibrary(Supplier<Activity> nextScreen, String logoPath,
                                   String titleLogoPath, TaskLoad... taskLoads) {
        var screen = new SplashActivity(nextScreen, 500,
                logoPath == null ? null : new Texture(logoPath),
                titleLogoPath == null ? null : new Texture(titleLogoPath),
                taskLoads);
        RdApplication.self().setScreen(screen);
    }

    /**
     * automatic correct loading of the application with specific minimum delay
     * @param nextScreen - first screen after loading resources;
     * @param minDelayMillis - minimum resource load time
     * @param logo - application logo texture
     * @param titleLogo - app name texture
     * @param taskLoads - tasks to be loaded in the graphics thread
     *  */
    public static void loadLibrary(Supplier<Activity> nextScreen, long minDelayMillis,
                                   Texture logo, Texture titleLogo, TaskLoad... taskLoads) {
        var screen = new SplashActivity(nextScreen, minDelayMillis, logo, titleLogo, taskLoads);
        RdApplication.self().setScreen(screen);
    }

    SplashActivity(Supplier<Activity> nextScreen, long minDelayMillis,
                   Texture logo, Texture titleLogo, TaskLoad... taskLoads) {
        this.nextScreen = nextScreen;
        this.minDelayMillis = minDelayMillis;
        this.taskLoads = taskLoads;
        textureLogo = logo;
        textureTitleLogo = titleLogo;
        startMillis = System.currentTimeMillis();
    }

    @Override
    public void initActors() {
        RdApplication.self().setBackgroundColor(Color.WHITE);
        if (textureLogo != null) {
            textureLogo.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            logo = new Image(textureLogo);
        } else {
            logo = new Image();
        }
        if (textureTitleLogo != null) {
            textureTitleLogo.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            titleLogo = new Image(textureTitleLogo);
        } else {
            titleLogo = new Image();
        }
    }

    @Override
    public void initListeners() {}

    @Override
    public void show(Stage stage, Activity last) {
        RdApplication.self().getAssetManager().load();
        var content = new Table();
        content.setFillParent(true);

        content.add(logo).row();
        content.add(titleLogo);

        stage.addActor(content);
    }

    @Override
    public void render(float delta) {
        if (!intent && Gdx.app.getType() == Application.ApplicationType.WebGL) {
            RdApplication.self().getAssetManager().finishLoading();
            launchLoad();
            makeIntent();
            intent = true;
            return;
        }

        if (!RdApplication.self().getAssetManager().isFinished()) {
            RdApplication.self().getAssetManager().update();
        } else {
            if (!launchLoad) {
                RdApplication.self().getAssetManager().finishLoading();
                launchLoad();
                launchLoad = true;
            }
        }

        if (System.currentTimeMillis() - startMillis > minDelayMillis
                && !intent && isFinished()) {
            makeIntent();
            intent = true;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        DisposeUtil.dispose(textureLogo);
        DisposeUtil.dispose(textureTitleLogo);
    }

    private void launchLoad() {
        Arrays.stream(taskLoads).forEach(TaskLoad::load);
    }

    private boolean isFinished() {
        return Arrays.stream(taskLoads)
                .allMatch(TaskLoad::isFinished);
    }

    private void makeIntent() {
        try {
            RdApplication.self().setScreen(nextScreen.get());
        } finally {
            dispose();
        }
    }
}
