package com.iapp.ageofchess.controllers;

import com.iapp.ageofchess.activity.MenuActivity;
import com.iapp.ageofchess.util.ChessConstants;
import com.iapp.rodsher.screens.Activity;
import com.iapp.rodsher.screens.Controller;

public class GuideController extends Controller {

    public GuideController(Activity activity) {
        super(activity);
    }

    public void goToMenu() {
        startActivity(new MenuActivity(), ChessConstants.localData.getScreenDuration());
    }
}
