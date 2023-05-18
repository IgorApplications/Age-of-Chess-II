package com.iapp.ageofchess.controllers;

import com.badlogic.gdx.Gdx;
import com.iapp.ageofchess.activity.CreationActivity;
import com.iapp.ageofchess.activity.GameActivity;
import com.iapp.ageofchess.activity.ScenariosActivity;
import com.iapp.ageofchess.modding.LoaderMap;
import com.iapp.ageofchess.modding.LocalMatch;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.lib.ui.actors.Spinner;
import com.iapp.lib.ui.screens.Controller;
import com.iapp.lib.ui.screens.RdApplication;

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
                startActivity(GameActivity.newInstance(localMatch)));
    }

    public void goToScenario() {
        startActivity(new ScenariosActivity());
    }
}
