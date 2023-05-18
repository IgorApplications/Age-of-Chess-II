package com.iapp.ageofchess.controllers.multiplayer;

import com.iapp.ageofchess.activity.multiplayer.MultiplayerMenuActivity;
import com.iapp.lib.ui.screens.Activity;
import com.iapp.lib.ui.screens.Controller;

public class RankController extends Controller {

    public RankController(Activity activity) {
        super(activity);
    }

    public void goToMultiplayerMenuActivity() {
        startActivity(new MultiplayerMenuActivity());
    }
}
