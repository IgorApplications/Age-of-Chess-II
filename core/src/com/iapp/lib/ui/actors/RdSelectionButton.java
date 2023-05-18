package com.iapp.lib.ui.actors;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import com.iapp.lib.ui.screens.RdAssetManager;

/**
 * Choice actor among text buttons
 * @author Igor Ivanov
 * @version 1.0
 * */
public class RdSelectionButton extends RdTable {

    /** all selection buttons */
    private final ButtonGroup<RdImageTextButton> group = new ButtonGroup<>();
    /** cell inside table for buttons and lines */
    private final Array<RdCell<RdImageTextButton>> buttonCells = new Array<>();

    /**
     * Creates LibSelectionButton
     * @param style - LibSelectionButton style
     * @param textSelection - choices
     * @throws IllegalArgumentException - {@literal if choices < 2}
     * */
    public RdSelectionButton(RdSelectionButtonStyle style, String... textSelection) {
        if (textSelection.length < 2) throw new IllegalArgumentException("The select button must consist of at least two elements");

        addSelection(new RdImageTextButton(textSelection[0], style.left));
        for (int i = 1; i < textSelection.length - 1; i++) {
            addSelection(new RdImageTextButton(textSelection[i], style.center));
        }
        addSelection(new RdImageTextButton(textSelection[textSelection.length - 1], style.right));
    }

    /** @see RdSelectionButtonStyle#RdSelectionButton(Skin, String...) */
    public RdSelectionButton(Skin skin, String... textSelection) {
        this(skin.get(RdSelectionButtonStyle.class), textSelection);
    }

    /** @see RdSelectionButtonStyle#RdSelectionButton(Skin, String...) */
    public RdSelectionButton(Skin skin, String styleName, String... textSelection) {
        this(skin.get(styleName, RdSelectionButtonStyle.class), textSelection);
    }

    /** @see RdSelectionButtonStyle#RdSelectionButton(Skin, String...) */
    public RdSelectionButton(String... textSelection) {
        this(RdAssetManager.current().getSkin(), textSelection);
    }

    public ButtonGroup<RdImageTextButton> getGroup() {
        return group;
    }

    public static class RdSelectionButtonStyle {

        public RdImageTextButton.RdImageTextButtonStyle left, center, right;

        public RdSelectionButtonStyle() {}

        public RdSelectionButtonStyle(RdImageTextButton.RdImageTextButtonStyle left,
                                      RdImageTextButton.RdImageTextButtonStyle center,
                                      RdImageTextButton.RdImageTextButtonStyle right) {
            this.left = left;
            this.center = center;
            this.right = right;
        }

        public RdSelectionButtonStyle(RdSelectionButtonStyle style) {
            left = style.left;
            center = style.center;
            right = style.right;
        }
    }

    /** Changes the size of the buttons so that they are the same size */
    @Override
    protected void sizeChanged() {
        if (buttonCells.size == 0) return;

        float cellWidth = getWidth() / buttonCells.size;
        for (var cell : buttonCells) {
            cell.width(cellWidth);
        }

        super.sizeChanged();
    }

    private void addSelection(RdImageTextButton button) {
        button.getLabel().setEllipsis("...");
        group.add(button);
        var cell = add(button);
        buttonCells.add(cell);
    }
}
