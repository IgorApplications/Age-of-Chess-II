package com.iapp.ageofchess.controllers;

import com.badlogic.gdx.Gdx;
import com.iapp.ageofchess.activity.CreationActivity;
import com.iapp.ageofchess.activity.GameActivity;
import com.iapp.ageofchess.activity.ScenariosActivity;
import com.iapp.ageofchess.modding.LoaderMap;
import com.iapp.ageofchess.modding.LocalMatch;
import com.iapp.ageofchess.util.ChessAssetManager;
import com.iapp.ageofchess.util.ChessConstants;
import com.iapp.rodsher.actors.Spinner;
import com.iapp.rodsher.screens.Controller;
import com.iapp.rodsher.screens.RdApplication;

public class CreationController extends Controller {

    private final CreationActivity activity;

    public CreationController(CreationActivity activity) {
        super(activity);
        this.activity = activity;
    }

    public void launchGame(LocalMatch localMatch) {
        var spinner = new Spinner(strings.get("loading"), ChessAssetManager.current().getSkin());
        activity.setSpinner(spinner);
        spinner.show(RdApplication.self().getStage());
        spinner.setSize(400, 100);
        activity.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        LoaderMap.self().loadIntoRam(localMatch.getMatchData(), () ->
                startActivityAlpha(GameActivity.newInstance(localMatch),
                        ChessConstants.localData.getScreenDuration()));
    }

    public void goToScenario() {
        startActivity(new ScenariosActivity(), ChessConstants.localData.getScreenDuration());
    }
}
