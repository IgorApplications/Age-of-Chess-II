package com.iapp.ageofchess.controllers;

import com.iapp.ageofchess.activity.MenuActivity;
import com.iapp.lib.ui.screens.Activity;
import com.iapp.lib.ui.screens.Controller;

public class GuideController extends Controller {

    public GuideController(Activity activity) {
        super(activity);
    }

    public void goToMenu() {
        startActivity(new MenuActivity());
    }
}
