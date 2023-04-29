package com.iapp.rodsher.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.iapp.rodsher.util.RdI18NBundle;

import java.util.Arrays;

/**
 * Class for managing the business logic of the current screen
 * @author Igor Ivanov
 * @version 1.0
 * */
public class Controller {

    /** current screen */
    private final Activity activity;
    /** strings (different language) */
    protected final RdI18NBundle strings;

    public Controller(Activity activity) {
        this.activity = activity;
        strings = RdApplication.self().getStrings();
    }

    /** makes a transition between screens with the effect of moving the stage
     * and automatically clearing past activity
     * @param next - next screen
     * @param duration - effect duration for each screen
     * */
    public void startActivity(Activity next, float duration) {
        var stage = RdApplication.self().getStage();
        Gdx.input.setOnscreenKeyboardVisible(false);

        var sequence = new SequenceAction();
        sequence.addAction(Actions.moveBy(0, -stage.getHeight(), duration));
        sequence.addAction(Actions.run(() -> {
            // TODO
            activity.dispose();
            RdApplication.self().setScreen(next);

            stage.getRoot().setY(-stage.getHeight());
            stage.addAction(Actions.moveBy(0, stage.getHeight(), duration));

        }));
        stage.addAction(sequence);
    }

    /** @see Controller#startActivityAlpha(Activity, float, Action...) */
    public void startActivityAlpha(Activity next, float duration) {
        var stage = RdApplication.self().getStage();
        var sequence = new SequenceAction();
        Gdx.input.setOnscreenKeyboardVisible(false);

        sequence.addAction(Actions.alpha(0, duration));
        sequence.addAction(Actions.run(() -> {
            activity.dispose();
            RdApplication.self().setScreen(next);
            stage.addAction(Actions.alpha(0));
            stage.addAction(Actions.alpha(1, duration));
        }));
        stage.addAction(sequence);
    }

    /**
     * makes a transition between screens with the effect of alpha the stage
     * and automatically clearing past activity
     * @param next - next screen
     * @param duration - effect duration for each screen
     * @param actions - additional actions
     * */
    public void startActivityAlpha(Activity next, float duration, Action... actions) {
        var stage = RdApplication.self().getStage();
        var sequence = new SequenceAction();
        Gdx.input.setOnscreenKeyboardVisible(false);

        sequence.addAction(Actions.alpha(0, duration));
        Arrays.stream(actions).forEach(sequence::addAction);
        sequence.addAction(Actions.run(() -> {
            activity.dispose();
            RdApplication.self().setScreen(next);
            stage.addAction(Actions.alpha(0));
            stage.addAction(Actions.alpha(1, duration));
        }));
        stage.addAction(sequence);
    }
}
