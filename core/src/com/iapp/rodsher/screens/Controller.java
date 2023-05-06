package com.iapp.rodsher.screens;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.iapp.rodsher.util.RdI18NBundle;

/**
 * Class for managing the business logic of the current screen
 * @author Igor Ivanov
 * @version 1.0
 * */
public class Controller {

    /** current screen */
    private final Activity activity;
    /** strings (different language) */
    protected final RdI18NBundle strings;

    public Controller(Activity activity) {
        this.activity = activity;
        strings = RdApplication.self().getStrings();
    }

    /** makes a transition between screens
     * @param next - next screen
     * */
    public void startActivity(Activity next, Action... actions) {
        RdApplication.self().setScreen(next, actions);
    }
}
