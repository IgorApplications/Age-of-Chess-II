package com.iapp.rodsher.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.github.tommyettinger.textra.Font;
import com.iapp.rodsher.screens.RdApplication;
import com.iapp.rodsher.screens.RdAssetManager;

/**
 * Load dialog actor
 * @author Igor Ivanov
 * @version 1.0
 * */
public class Spinner extends RdDialog {

    /** load information */
    private final String info;
    /** spinner style */
    private SpinnerStyle style;
    /** label load information */
    private RdLabel infoLabel;

    /**
     * Load dialog created
     * @param info - load information
     * @param style - spinner style
     * */
    public Spinner(String info, SpinnerStyle style) {
        super("", style);
        this.style = style;
        this.info = info;
        init();
    }

    /** @see Spinner#Spinner(String, SpinnerStyle) */
    public Spinner(String info, Skin skin) {
        this(info, skin.get(SpinnerStyle.class));
    }

    /** @see Spinner#Spinner(String, SpinnerStyle) */
    public Spinner(String info, Skin skin, String styleName) {
        this(info, skin.get(styleName, SpinnerStyle.class));
    }

    public Spinner(String info) {
        this(info, RdAssetManager.current().getSkin());
    }

    public Spinner(String info, String styleName) {
        this(info, RdAssetManager.current().getSkin(), styleName);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        updateCursor();
    }

    protected void updateCursor() {

        if (isHidden()) {

            var def = RdApplication.self().getCursor();
            if (style.cursor != null) {
                Gdx.graphics.setCursor(def);
            }

        } else {

            if (style.cursor != null) {
                Gdx.graphics.setCursor(style.cursor);
            }

        }

    }

    public void setStyle(SpinnerStyle style) {
        super.setStyle(style);
        this.style = style;
    }

    @Override
    public SpinnerStyle getStyle() {
        return style;
    }

    /** returns label load information */
    public RdLabel getInfoLabel() {
        return infoLabel;
    }

    /** Packing actors into a table, meant to be overridden */
    protected void init() {

        style.image.setAlign(Align.right);
        style.image.setScaling(Scaling.fit);
        infoLabel = new RdLabel(info, new RdLabel.RdLabelStyle(style.titleFont, style.titleFontColor));
        infoLabel.setWrap(true);
        infoLabel.setAlignment(Align.center);
        padLeft(0);

        getContentTable().add(style.image).expandY().fillY().right();
        getContentTable().add(infoLabel).expand().fill();
    }

    public static class SpinnerStyle extends RdDialogStyle {

        public AnimatedImage image;
        public Cursor cursor;

        public SpinnerStyle() {
            super();
        }

        public SpinnerStyle(AnimatedImage image, Font titleFont, Color titleFontColor, Drawable background) {
            super(titleFont, titleFontColor, background, null, null);
            this.image = image;
        }

        public SpinnerStyle(AnimatedImage image, RdDialogStyle style) {
            super(style);
            this.image = image;
        }
    }
}
