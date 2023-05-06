package com.iapp.ageofchess.controllers.multiplayer;

import com.badlogic.gdx.Gdx;
import com.iapp.ageofchess.multiplayer.MultiplayerEngine;
import com.iapp.ageofchess.activity.CreationActivity;
import com.iapp.ageofchess.activity.multiplayer.*;
import com.iapp.ageofchess.chess_engine.Color;
import com.iapp.ageofchess.modding.GameMode;
import com.iapp.ageofchess.modding.LoaderMap;
import com.iapp.ageofchess.modding.LocalMatch;
import com.iapp.ageofchess.modding.MapData;
import com.iapp.ageofchess.multiplayer.Match;
import com.iapp.ageofchess.util.ChessAssetManager;
import com.iapp.ageofchess.util.ChessConstants;
import com.iapp.rodsher.actors.Spinner;
import com.iapp.rodsher.screens.Controller;
import com.iapp.rodsher.screens.RdApplication;

public class MultiplayerScenariosController extends Controller {

    private final MultiplayerScenariosActivity activity;
    private final Match match;

    public MultiplayerScenariosController(MultiplayerScenariosActivity current, Match match) {
        super(current);
        activity = current;
        this.match = match;
    }

    public void goToCreation(MapData mapData, int scenario) {
        startActivity(new MultiplayerCreationActivity(mapData, scenario));
    }

    public void goToGame(MapData mapData) {
        MultiplayerEngine.self().enterMatch(match.getId());

        boolean infinityTimeByTurn = match.getTimeByTurn() == -1;
        boolean infinityTimeGame = match.getTimeByWhite() == -1;
        boolean infinityTurns = match.getMaxTurn() == -1;

        var builder = new LocalMatch.GameBuilder(match.getName(), Color.BLACK, mapData)
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
        var localMatch = new LocalMatch(CreationActivity.generateMatchId(), builder);

        var spinner = new Spinner(strings.get("loading"), ChessAssetManager.current().getSkin());
        activity.setSpinner(spinner);
        spinner.show(RdApplication.self().getStage());
        spinner.setSize(400, 100);
        activity.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        LoaderMap.self().loadIntoRam(localMatch.getMatchData(), () ->
                startActivity(MultiplayerGameActivity.newInstance(localMatch, match)));
    }

    public void goToMenu() {
        startActivity(new MultiplayerMenuActivity());
    }

    public void goToGames() {
        startActivity(new MultiplayerGamesActivity());
    }
}
