package com.iapp.ageofchess.controllers.multiplayer;

import com.badlogic.gdx.Gdx;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.multiplayer.MultiplayerEngine;
import com.iapp.ageofchess.activity.multiplayer.MultiplayerCreationActivity;
import com.iapp.ageofchess.activity.multiplayer.MultiplayerGameActivity;
import com.iapp.ageofchess.activity.multiplayer.MultiplayerScenariosActivity;
import com.iapp.ageofchess.modding.LoaderMap;
import com.iapp.ageofchess.modding.LocalMatch;
import com.iapp.ageofchess.multiplayer.Match;
import com.iapp.ageofchess.util.ChessAssetManager;
import com.iapp.ageofchess.util.ChessConstants;
import com.iapp.rodsher.actors.Spinner;
import com.iapp.rodsher.screens.Controller;
import com.iapp.rodsher.screens.RdApplication;

import java.util.function.Consumer;

public class MultiplayerCreationController extends Controller {

    private final MultiplayerCreationActivity activity;

    public MultiplayerCreationController(MultiplayerCreationActivity activity) {
        super(activity);
        this.activity = activity;
    }

    public void launchGame(LocalMatch localMatch) {
        MultiplayerEngine.self().createMatch(localMatch,
            match -> {
                goToGame(localMatch, match);
            },
            error -> {
                RdApplication.postRunnable(() ->
                    ChessApplication.self().showError(strings.get("error_create_match") + error));
            });
    }

    public void goToGame(LocalMatch localMatch, Match match) {
        MultiplayerEngine.self().enterMatch(match.getId());

        var spinner = new Spinner(strings.get("loading"), ChessAssetManager.current().getSkin());
        activity.setSpinner(spinner);
        spinner.show(RdApplication.self().getStage());
        spinner.setSize(400, 100);
        activity.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        LoaderMap.self().loadIntoRam(localMatch.getMatchData(), () ->
                startActivity(MultiplayerGameActivity.newInstance(localMatch, match)));
    }

    public void goToScenario() {
        startActivity(new MultiplayerScenariosActivity());
    }
}
