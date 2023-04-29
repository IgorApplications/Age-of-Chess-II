package com.iapp.rodsher.actors;

import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.iapp.rodsher.screens.RdAssetManager;

/**
 * Checkbox supports sound effect on click
 * and same width and height (width == height)
 * @author Igor Ivanov
 * @version 1.0
 * */
public class RdCheckBox extends ImageButton {

    /** creating a custom button with the "default" style */
    public RdCheckBox(Skin skin) {
        super(skin);
    }

    /** creating a custom button with the specified style */
    public RdCheckBox(Skin skin, String styleName) {
        super(skin, styleName);
    }

    /** creating a custom button */
    public RdCheckBox(ImageButtonStyle style) {
        super(style);
    }

    /** @see RdCheckBox#RdCheckBox(Drawable, Drawable, Drawable)  */
    public RdCheckBox(Drawable imageUp) {
        super(imageUp);
    }

    /** @see RdCheckBox#RdCheckBox(Drawable, Drawable, Drawable)  */
    public RdCheckBox(Drawable imageUp, Drawable imageDown) {
        super(imageUp, imageDown);
    }

    public RdCheckBox() {
        this(RdAssetManager.current().getSkin());
    }

    public RdCheckBox(String styleName) {
        this(RdAssetManager.current().getSkin(), styleName);
    }

    /**
     * creates a custom button with three images without a background
     * @param imageUp - normal state
     * @param imageDown - pressed the button
     * @param imageChecked - the button has already been pressed
     * */
    public RdCheckBox(Drawable imageUp, Drawable imageDown, Drawable imageChecked) {
        super(imageUp, imageDown, imageChecked);
    }

    /**
     * resizes the actor to have the same
     * width and height (width == height)
     * */
    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        var size = Math.min(getWidth(), getHeight());
        setSize(size, size);
    }
}

