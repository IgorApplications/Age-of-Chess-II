package com.iapp.lib.util;

import com.badlogic.gdx.utils.Null;
import com.iapp.lib.ui.actors.RdWindow;
import com.iapp.lib.ui.screens.RdApplication;

/**
 * Utility class for working with the Window class
 * @version 1.0
 * */
public final class WindowUtil {

    /**
     * centers the window in the center of the screen, you must call inside the resize method
     * @see com.badlogic.gdx.Screen#resize(int, int)
     * */
    public static void resizeCenter(@Null RdWindow window) {
        if (window == null) return;
        window.setPosition(
                RdApplication.self().getStage().getWidth() / 2 - window.getWidth() / 2,
                RdApplication.self().getStage().getHeight() / 2 - window.getHeight() / 2);
    }
}
