package com.iapp.ageofchess.controllers.multiplayer;

import com.iapp.ageofchess.activity.MenuActivity;
import com.iapp.ageofchess.activity.multiplayer.MultiplayerGamesActivity;
import com.iapp.ageofchess.activity.multiplayer.MultiplayerMenuActivity;
import com.iapp.ageofchess.activity.multiplayer.MultiplayerScenariosActivity;
import com.iapp.ageofchess.activity.multiplayer.RankActivity;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.lib.ui.screens.Controller;

public class MultiplayerMenuController extends Controller {


    private final MultiplayerMenuActivity activity;

    public MultiplayerMenuController(MultiplayerMenuActivity activity) {
        super(activity);
        this.activity = activity;

        ChessConstants.accountPanel
            .updateListeners(ChessConstants.loggingAcc,
                ChessConstants.accountController::seeAccount,
                ChessConstants.accountController::editAccount,
                ChessConstants.accountController::showGames);
    }

    public void goToMenu() {
        startActivity(new MenuActivity());
    }

    public void goToGames() {
        startActivity(new MultiplayerGamesActivity());
    }

    public void goToScenarios() {
        startActivity(new MultiplayerScenariosActivity());
    }

    public void goToRank() {
        startActivity(new RankActivity());
    }
}
