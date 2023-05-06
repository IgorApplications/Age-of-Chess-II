package com.iapp.ageofchess.controllers.multiplayer;

import com.iapp.ageofchess.activity.multiplayer.MultiplayerGamesActivity;
import com.iapp.ageofchess.activity.multiplayer.MultiplayerMenuActivity;
import com.iapp.ageofchess.activity.multiplayer.MultiplayerScenariosActivity;
import com.iapp.ageofchess.multiplayer.Match;
import com.iapp.ageofchess.util.ChessConstants;
import com.iapp.rodsher.screens.Controller;

public class MultiplayerGamesController extends Controller {

    private final MultiplayerGamesActivity activity;

    public MultiplayerGamesController(MultiplayerGamesActivity activity) {
        super(activity);
        this.activity = activity;
    }

    public void goToMultiplayerMenuActivity() {
        startActivity(new MultiplayerMenuActivity());
    }

    public void goToMultiplayerScenariosActivity(Match match) {
        startActivity(new MultiplayerScenariosActivity(match));
    }
}
