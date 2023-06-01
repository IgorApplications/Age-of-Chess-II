package com.iapp.lib.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.iapp.lib.ui.actors.RdSelectBox;
import com.iapp.lib.ui.actors.RdSelectionButton;
import com.iapp.lib.ui.screens.RdLogger;

/**
 * Processing interface when pressing (release pressing) on the actor.
 * Protects from crashing the application when processing a click.
 * @author Igor Ivanov
 * @version 1.0
 * */
public abstract class OnChangeListener extends InputListener {

    /** button click sound listener */
    private static CallListener buttonClick;
    private boolean enable = true;

    /** sets button click sound listener */
    public static void setButtonClick(CallListener click) {
        buttonClick = click;
    }

    public void setSoundEnable(boolean enable) {
        this.enable = enable;
    }

    /**
     * Called when the actor is released
     * @param actor - the actor that was clicked
     * */
    public abstract void onChange(Actor actor);

    @Override
    public boolean handle(Event event) {
        boolean result = super.handle(event);
        if (event instanceof ChangeListener.ChangeEvent) {
            try {
                onChange(event.getListenerActor());
            } catch (Throwable t) {
                Gdx.app.error("handle", RdLogger.self().getDescription(t));
                RdLogger.self().showFatalScreen(t);
            }
        }
        return result;
    }

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        super.touchDown(event, x, y, pointer, button);
        return true;
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

        super.touchUp(event, x, y, pointer, button);
    }
}
