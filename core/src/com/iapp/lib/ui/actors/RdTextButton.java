package com.iapp.lib.ui.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import com.github.tommyettinger.textra.Font;
import com.iapp.lib.ui.screens.RdAssetManager;

/** A button with a child {@link Label} to display text.
 * @author Nathan Sweet */
public class RdTextButton extends Button {

    private RdLabel label;
    private RdTextButtonStyle style;

    public RdTextButton(@Null String text, Skin skin) {
        this(text, skin.get(RdTextButtonStyle.class));
        setSkin(skin);
    }

    public RdTextButton(@Null String text, Skin skin, String styleName) {
        this(text, skin.get(styleName, RdTextButtonStyle.class));
        setSkin(skin);
    }

    public RdTextButton(@Null String text, RdTextButtonStyle style) {
        super();
        setStyle(style);
        label = newLabel(text, new RdLabel.RdLabelStyle(style.font, style.fontColor));
        label.setAlignment(Align.center);
        align(Align.center);
        add(label).expand().fill().center();
        setSize(getPrefWidth(), getPrefHeight());

        if (style.padLeft != -1) padLeft(style.padLeft);
        if (style.padRight != -1) padRight(style.padRight);
        if (style.padBottom != -1) padBottom(style.padBottom);
        if (style.padTop != -1) padTop(style.padTop);
    }

    public RdTextButton(@Null String text, String styleName) {
        this(text, RdAssetManager.current().getSkin(), styleName);
    }

    public RdTextButton(@Null String text) {
        this(text, RdAssetManager.current().getSkin());
    }

    protected RdLabel newLabel (String text, RdLabel.RdLabelStyle style) {
        style.scale = this.style.scaleText;
        return new RdLabel(text, style);
    }

    public void setStyle (ButtonStyle style) {
        if (style == null) throw new NullPointerException("style cannot be null");
        if (!(style instanceof RdTextButtonStyle)) throw new IllegalArgumentException("style must be a TextButtonStyle.");
        this.style = (RdTextButtonStyle) style;
        super.setStyle(style);

        if (label != null) {
            RdTextButtonStyle textButtonStyle = (RdTextButtonStyle) style;
            label.setFont(textButtonStyle.font);
            label.setColor(textButtonStyle.fontColor);
        }
    }

    public RdTextButtonStyle getStyle () {
        return style;
    }

    /** Returns the appropriate label font color from the style based on the current button state. */
    protected @Null Color getFontColor () {
        if (isDisabled() && style.disabledFontColor != null) return style.disabledFontColor;
        if (isPressed()) {
            if (isChecked() && style.checkedDownFontColor != null) return style.checkedDownFontColor;
            if (style.downFontColor != null) return style.downFontColor;
        }
        if (isOver()) {
            if (isChecked()) {
                if (style.checkedOverFontColor != null) return style.checkedOverFontColor;
            } else {
                if (style.overFontColor != null) return style.overFontColor;
            }
        }
        boolean focused = hasKeyboardFocus();
        if (isChecked()) {
            if (focused && style.checkedFocusedFontColor != null) return style.checkedFocusedFontColor;
            if (style.checkedFontColor != null) return style.checkedFontColor;
            if (isOver() && style.overFontColor != null) return style.overFontColor;
        }
        if (focused && style.focusedFontColor != null) return style.focusedFontColor;
        return style.fontColor;
    }

    public void draw (Batch batch, float parentAlpha) {
        label.setColor(getFontColor());
        super.draw(batch, parentAlpha);
    }

    public void setLabel (RdLabel label) {
        if (label == null) throw new IllegalArgumentException("label cannot be null.");
        getLabelCell().setActor(label);
        this.label = label;
    }

    public RdLabel getLabel () {
        return label;
    }

    public Cell<RdLabel> getLabelCell () {
        return getCell(label);
    }

    public void setText (@Null String text) {
        label.setText(text);
    }

    public String getText () {
        return label.getText();
    }

    public String toString () {
        String name = getName();
        if (name != null) return name;
        String className = getClass().getName();
        int dotIndex = className.lastIndexOf('.');
        if (dotIndex != -1) className = className.substring(dotIndex + 1);
        return (className.indexOf('$') != -1 ? "TextButton " : "") + className + ": " + getText();
    }

    /** The style for a text button, see {@link com.badlogic.gdx.scenes.scene2d.ui.TextButton}.
     * @author Nathan Sweet */
    static public class RdTextButtonStyle extends ButtonStyle {

        public Font font;
        public @Null Color fontColor, downFontColor, overFontColor, focusedFontColor, disabledFontColor;
        public @Null Color checkedFontColor, checkedDownFontColor, checkedOverFontColor, checkedFocusedFontColor;
        public float padLeft = -1, padRight = -1, padBottom = -1, padTop = -1;
        public float scaleText = 1;

        public RdTextButtonStyle() {
        }

        public RdTextButtonStyle(@Null Drawable up, @Null Drawable down, @Null Drawable checked, @Null Font font) {
            super(up, down, checked);
            this.font = font;
        }

        public RdTextButtonStyle(RdTextButtonStyle style) {
            super(style);
            font = style.font;
            scaleText = style.scaleText;

            if (style.fontColor != null) fontColor = new Color(style.fontColor);
            if (style.downFontColor != null) downFontColor = new Color(style.downFontColor);
            if (style.overFontColor != null) overFontColor = new Color(style.overFontColor);
            if (style.focusedFontColor != null) focusedFontColor = new Color(style.focusedFontColor);
            if (style.disabledFontColor != null) disabledFontColor = new Color(style.disabledFontColor);

            if (style.checkedFontColor != null) checkedFontColor = new Color(style.checkedFontColor);
            if (style.checkedDownFontColor != null) checkedDownFontColor = new Color(style.checkedDownFontColor);
            if (style.checkedOverFontColor != null) checkedOverFontColor = new Color(style.checkedOverFontColor);
            if (style.checkedFocusedFontColor != null) checkedFocusedFontColor = new Color(style.checkedFocusedFontColor);
        }
    }
}
