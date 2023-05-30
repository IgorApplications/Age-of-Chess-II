package com.iapp.ageofchess.controllers.multiplayer;

import com.badlogic.gdx.Gdx;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.lib.ui.widgets.ChatView;
import com.iapp.ageofchess.multiplayer.MultiplayerEngine;
import com.iapp.ageofchess.activity.CreationActivity;
import com.iapp.ageofchess.activity.multiplayer.*;
import com.iapp.lib.chess_engine.Color;
import com.iapp.ageofchess.modding.GameMode;
import com.iapp.ageofchess.modding.LoaderMap;
import com.iapp.ageofchess.modding.LocalMatch;
import com.iapp.ageofchess.modding.MapData;
import com.iapp.ageofchess.multiplayer.Match;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.lib.ui.actors.Spinner;
import com.iapp.lib.ui.screens.Controller;
import com.iapp.lib.ui.screens.RdApplication;

public class MultiplayerScenariosController extends Controller {

    private final MultiplayerScenariosActivity activity;
    private final Match match;

    public MultiplayerScenariosController(MultiplayerScenariosActivity current, Match oldMatch) {
        super(current);
        activity = current;
        this.match = oldMatch;
    }

    public void goToCreation(MapData mapData, int scenario) {
        startActivity(new MultiplayerCreationActivity(mapData, scenario));
    }

    public void goToGame(MapData mapData) {
        boolean infinityTimeByTurn = match.getTimeByTurn() == -1;
        boolean infinityTimeGame = match.getTimeByWhite() == -1;
        boolean infinityTurns = match.getMaxTurn() == -1;

        LocalMatch.GameBuilder builder = new LocalMatch.GameBuilder(match.getName(), Color.BLACK, mapData)
                .flippedPieces(ChessConstants.localData.isFlippedPieces())
                .turnMode(match.getTurnMode())
                .timeByGame(Math.min(match.getTimeByWhite(), match.getTimeByBlack()))
                .timeByTurn(match.getTimeByTurn())
                .maxTurns(match.getMaxTurn())
                .infiniteTurns(infinityTurns)
                .infiniteTimeByTurn(infinityTimeByTurn)
                .infiniteTimeByGame(infinityTimeGame)
                .matchInfo(ChessConstants.localData.isMatchDescription())
                .gameMode(GameMode.MULTIPLAYER)
                .rankType(match.getRankType());
        LocalMatch localMatch = new LocalMatch(CreationActivity.generateMatchId(), builder);

        Spinner spinner = new Spinner(strings.get("loading"), ChessAssetManager.current().getSkin());
        activity.setSpinner(spinner);
        spinner.show(RdApplication.self().getStage());
        spinner.setSize(400, 100);
        activity.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        MultiplayerEngine.self().enterMatch(match.getId(),
            () -> {},
            error -> {

                spinner.hide();
                // error entry into match
                ChessApplication.self().showError(strings.format("error_enter", error));

            }
        );

        // successful entry into the match
        MultiplayerEngine.self().setOnUpdateMatch(match.getId(), updatedMatch ->
            LoaderMap.self().loadIntoRam(localMatch.getMatchData(), () -> {
                ChessConstants.chatView.updateMode(ChatView.Mode.GAMES);
                startActivity(MultiplayerGameActivity.newInstance(localMatch, updatedMatch));
            }));
    }

    public void goToMenu() {
        startActivity(new MultiplayerMenuActivity());
    }

    public void goToGames() {
        startActivity(new MultiplayerGamesActivity());
    }
}
