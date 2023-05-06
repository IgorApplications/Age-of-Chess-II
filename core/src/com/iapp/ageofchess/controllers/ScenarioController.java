package com.iapp.ageofchess.controllers;

import com.iapp.ageofchess.activity.MenuActivity;
import com.iapp.ageofchess.modding.MapData;
import com.iapp.ageofchess.activity.CreationActivity;
import com.iapp.ageofchess.activity.SavedGamesActivity;
import com.iapp.ageofchess.util.ChessConstants;
import com.iapp.rodsher.screens.Activity;
import com.iapp.rodsher.screens.Controller;

public class ScenarioController extends Controller {

    public ScenarioController(Activity current) {
        super(current);
    }

    public void goToCreation(MapData mapData, int scenario) {
        startActivity(new CreationActivity(mapData, scenario));
    }

    public void goToMenu() {
        startActivity(new MenuActivity());
    }

    public void goToSavedGames() {
        startActivity(new SavedGamesActivity());
    }
}
