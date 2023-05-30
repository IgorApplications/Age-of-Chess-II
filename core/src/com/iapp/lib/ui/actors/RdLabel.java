package com.iapp.lib.ui.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Null;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.TypingLabel;
import com.iapp.lib.ui.screens.GrayAssetManager;
import com.iapp.lib.ui.screens.RdAssetManager;

/**
 * @version 1.0
 * */
public class RdLabel extends TypingLabel {

    private String lastText;

    public RdLabel(String text, RdLabelStyle style) {
        super(text, valueOf(style), style.font);
        skipToTheEnd();
        setDefaultToken("[%" + style.scale * 100 + "]");
    }

    public RdLabel(String text, RdLabelStyle style, boolean markup) {
        super(text, valueOf(style), style.font, markup);
        skipToTheEnd();
        replaceScale(style.scale);
    }

    public RdLabel(String text, boolean markup) {
        this(text, RdAssetManager.current().getSkin().get("default", RdLabelStyle.class), markup);
    }

    public RdLabel(String text, Skin skin, String name) {
        this(text, skin.get(name, RdLabelStyle.class));
    }

    public RdLabel(String text, Skin skin) {
        this(text, skin, "default");
        skipToTheEnd();
    }

    public RdLabel(String text) {
        this(text, RdAssetManager.current().getSkin());
        skipToTheEnd();
    }

    @Override
    public void setText(String newText) {
        if (newText.equals(lastText)) return;
        lastText = newText;
        super.setText(newText);
        skipToTheEnd();
    }

    public String getText() {
        return getOriginalText().toString();
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
