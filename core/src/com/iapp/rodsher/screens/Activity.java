package com.iapp.rodsher.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.iapp.rodsher.util.RdI18NBundle;

/**
 * Standard screen of the Rodsher library. Supports background,
 * graphic logging and strings (different languages)
 * @version 1.0
 * */
public abstract class Activity implements Screen {

    /** strings (different language) */
    protected RdI18NBundle strings;

    public Activity() {}

    /** initializes the actors */
    public abstract void initActors();

    /** Loading settings */
    public void loadSettings() {}

    /** adds listeners to actors */
    public abstract void initListeners();

    /**
     * called when the screen is ready to be drawn
     * @param stage - actor drawing object
     **/
    public abstract void show(Stage stage);

    /** @return an object for drawing actors */
    public Stage getStage() {
        return RdApplication.self().getStage();
    }

    /** Creates a scene and sets up an input listener */
    @Override
    public void show() {
        strings = RdApplication.self().getStrings();
        var defCursor = RdApplication.self().getCursor();
        if (defCursor != null) {
            Gdx.graphics.setCursor(defCursor);
        }

        initActors();
        initListeners();
        loadSettings();
        show(RdApplication.self().getStage());
    }

    /** the stage draws the actors */
    @Override
    public void render(float delta) {}

    /** clears the object for drawing actors */
    @Override
    public void dispose() {}

    /** Called when the screen is resized */
    @Override
    public void resize(int width, int height) {}

    /** Called when the application goes into the background */
    @Override
    public void pause() {}

    /** Called when the application exits the background */
    @Override
    public void resume() {}

    /** Called when the application window is closed */
    @Override
    public void hide() {}
}
