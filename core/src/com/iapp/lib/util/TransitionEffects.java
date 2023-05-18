package com.iapp.lib.util;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.iapp.lib.ui.screens.RdApplication;

/**
 * Basic graphic effects when transitioning between screens
 * @version 1.0
 * @author Igor Ivanov
 * */
public class TransitionEffects {

    public static void alphaHide(SequenceAction sequence, float duration) {
        sequence.addAction(Actions.alpha(0, duration));
    }

    public static void transitionBottomHide(SequenceAction sequence, Actor actor, float duration) {
        sequence.addAction(Actions.moveBy(0, -actor.getY() - actor.getHeight(), duration));
    }

    public static void transitionTopHide(SequenceAction sequence, Actor actor, float duration) {
        Stage stage = RdApplication.self().getStage();
        sequence.addAction(Actions.moveBy(0, stage.getHeight() - actor.getY(), duration));
    }

    public static void alphaShow(Actor actor, float duration) {
        actor.getColor().a = 0;
        actor.addAction(Actions.alpha(1, duration));
    }

    public static void transitionBottomShow(Actor actor, float duration) {
        Stage stage = RdApplication.self().getStage();
        actor.addAction(Actions.moveBy(0, stage.getHeight(), duration));
        actor.setY(-stage.getHeight());
    }

    public static void transitionTopShow(Actor actor, float duration) {
        Stage stage = RdApplication.self().getStage();
        actor.addAction(Actions.moveBy(0, -(stage.getHeight() - actor.getY()), duration));
        actor.setY(stage.getHeight() - actor.getY());
    }
}
