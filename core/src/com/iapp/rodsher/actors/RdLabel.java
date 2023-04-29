package com.iapp.rodsher.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Null;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.TypingLabel;
import com.iapp.rodsher.screens.RdAssetManager;

/**
 * @version 1.0
 * */
public class RdLabel extends TypingLabel {

    public RdLabel(String text, RdLabelStyle style) {
        super(text, valueOf(style), style.font);
        skipToTheEnd();
        setDefaultToken("[%" + style.scale * 100 + "]");
    }

    public RdLabel(String text, Skin skin, String name) {
        this(text, skin.get(name, RdLabelStyle.class));
        skipToTheEnd();
    }

    /**
     * updates text if previous is not equal to new
     * NOTE: hidden text (formatting) may behave differently
     * */
    public void setSafetyText(String text) {
        if (getOriginalText().toString().equals(text)) return;
        super.setText(text);
    }

    @Override
    public void setText(String newText) {
        super.setText(newText);
        skipToTheEnd();
    }

    @Override
    protected void setText(String newText, boolean modifyOriginalText) {
        super.setText(newText, modifyOriginalText);
        skipToTheEnd();
    }

    @Override
    protected void setText(String newText, boolean modifyOriginalText, boolean restart) {
        super.setText(newText, modifyOriginalText, restart);
        skipToTheEnd();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.validate();
        super.draw(batch, parentAlpha);
    }


    public RdLabel(String text, Skin skin) {
        this(text, skin, "default");
        skipToTheEnd();
    }

    public RdLabel(String text) {
        this(text, RdAssetManager.current().getSkin());
        skipToTheEnd();
    }

    public static class RdLabelStyle {

        public Font font;
        public @Null Color color;
        public @Null Drawable background;
        public float scale = 1;

        public RdLabelStyle(Font font, @Null Color color) {
            this.font = font;
            this.color = color;
        }

        public RdLabelStyle() {}

        public RdLabelStyle(RdLabelStyle style) {
            font = style.font;
            color = style.color;
            background = style.background;
        }
    }

    private static Label.LabelStyle valueOf(RdLabelStyle labelStyle) {
        var style = new Label.LabelStyle(null, labelStyle.color);
        style.background = labelStyle.background;
        return style;
    }


}
