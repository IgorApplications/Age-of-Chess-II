package com.iapp.rodsher.actors;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.iapp.rodsher.screens.RdApplication;
import com.iapp.rodsher.screens.RdAssetManager;

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
    /** title table */
    private final RdTable titleTable;
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

        titleTable = new RdTable();
    }

    /** returns top table */
    public RdTable getTitleTable() {
        return titleTable;
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
     * @see com.iapp.rodsher.screens.Activity#resize(int, int)
     *  */
    public void update() {
        var stage = RdApplication.self().getStage();

        if (stage.getWidth() > stage.getHeight()) {
            clear();
            buttonsTable = new RdTable();
            buttonsTable.align(Align.top);

            for (var button : buttons) {
                buttonsTable.add(button).minWidth(style.buttonMinWidth).fillX().row();
            }

            add();
            add(titleTable).fillX().left().padTop(style.padTop).padLeft(5).row();

            add(buttonsTable).expandY().fillY().padLeft(style.padLeft);
            add(window).prefWidth(style.windowMinWidth).expandY().fillY().center()
                    .pad(0, 5, style.padBottom, style.padRight);


        } else if (stage.getWidth() < stage.getHeight()) {
            clear();
            buttonsTable = new RdTable();
            buttonsTable.align(Align.left);
            buttonsTable.padLeft(style.padLeft).padRight(style.padRight);

            float sumWidth = 0;
            for (int i = 0; i < buttons.length; i++) {
                sumWidth += Math.max(buttons[i].getPrefWidth(), style.buttonMinWidth);
                buttonsTable.add(buttons[i]).minWidth(style.buttonMinWidth)
                        .fillX().padLeft(i > 0 ? 10 : 0);

                if (sumWidth > Math.max(window.getWidth(), style.windowMinWidth)) {
                    buttonsTable.row();
                    sumWidth = 0;
                }
            }

            add(titleTable).fillX().left().padTop(style.padTop)
                    .padLeft(style.padLeft).row();

            add(buttonsTable).fill().row();
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
