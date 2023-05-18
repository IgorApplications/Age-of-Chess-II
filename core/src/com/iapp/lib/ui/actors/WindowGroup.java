package com.iapp.lib.ui.actors;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.iapp.lib.ui.screens.Activity;
import com.iapp.lib.ui.screens.RdApplication;
import com.iapp.lib.ui.screens.RdAssetManager;

/**
 * Table of vertical/horizontal placement
 * of the main screen and control buttons
 * @author Igor Ivanov
 * @version 1.0
 * */
public class WindowGroup extends Table {

    private final Button[] buttons;
    private final WindowGroupStyle style;

    /** main window */
    private final RdWindow window;
    /** buttons table */
    private RdTable buttonsTable;


    public WindowGroup(String styleName, RdWindow window, Button... buttons) {
        this(RdAssetManager.current().getSkin().get(styleName, WindowGroupStyle.class),
                window, buttons);
    }

    public WindowGroup(RdWindow window, Button... buttons) {
        this(RdAssetManager.current().getSkin().get(WindowGroupStyle.class),
                window, buttons);
    }

    public WindowGroup(WindowGroupStyle style, RdWindow window, Button... buttons) {
        this.style = style;
        this.window = window;
        this.buttons = buttons;
    }

    /** returns button table */
    public RdTable getButtonsTable() {
        return buttonsTable;
    }

    /** returns main window */
    public RdWindow getWindow() {
        return window;
    }

    /**
     * updates the placement of the actors depending on the screen size,
     * must be positioned inside the method
     * @see Activity#resize(int, int)
     *  */
    public void update() {
        if (!getActions().isEmpty()) return;
        var stage = RdApplication.self().getStage();

        if (stage.getWidth() > stage.getHeight()) {
            clear();
            buttonsTable = new RdTable();
            buttonsTable.align(Align.top);

            for (var button : buttons) {
                buttonsTable.add(button).minWidth(style.buttonMinWidth).fillX().row();
            }

            add(buttonsTable).expandY().fillY().padLeft(style.padLeft).padTop(style.padTop);
            add(window).prefWidth(style.windowMinWidth).expandY().fillY().center()
                    .pad(style.padTop, 5, style.padBottom, style.padRight);


        } else if (stage.getWidth() < stage.getHeight()) {
            clear();
            buttonsTable = new RdTable();
            buttonsTable.align(Align.left);

            float sumWidth = 0;
            int j = 0;
            for (int i = 0; i < buttons.length; i++) {
                j++;
                sumWidth += Math.max(buttons[i].getPrefWidth(), style.buttonMinWidth);

                if (sumWidth > Math.max(window.getWidth() - style.padRight - 10 * j,
                    style.windowMinWidth - style.padLeft - style.padRight - 10 * j)) {

                    buttonsTable.row();
                    j = 0;
                    sumWidth = 0;
                }
                buttonsTable.add(buttons[i]).minWidth(style.buttonMinWidth)
                        .fillX().padLeft(10);
            }

            add(buttonsTable).fill().padTop(style.padTop).padRight(style.padRight).row();
            add(window).prefWidth(style.windowMinWidth).expandY().fillY().center()
                    .pad(5, style.padLeft, style.padBottom, style.padRight);

        }
    }

    public static class WindowGroupStyle {

        public float buttonMinWidth = 300, windowMinWidth = 1200;
        public float padLeft = 15, padRight = 15, padBottom = 50, padTop = 50;


        public WindowGroupStyle(float buttonMinWidth, float windowMinWidth, float padLeft,
                                float padRight, float padBottom, float padTop) {

            this.buttonMinWidth = buttonMinWidth;
            this.windowMinWidth = windowMinWidth;
            this.padLeft = padLeft;
            this.padRight = padRight;
            this.padBottom = padBottom;
            this.padTop = padTop;
        }

        public WindowGroupStyle(WindowGroupStyle style) {
            buttonMinWidth = style.buttonMinWidth;
            windowMinWidth = style.windowMinWidth;
            padLeft = style.padLeft;
            padRight = style.padRight;
            padBottom = style.padBottom;
            padTop = style.padTop;
        }

        public WindowGroupStyle() {}
    }
}
