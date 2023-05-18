package com.iapp.lib.ui.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.Scaling;
import com.github.tommyettinger.textra.Font;
import com.iapp.lib.ui.screens.RdAssetManager;

/** A button with a child {@link Image} and {@link Label}.
 * @see ImageButton
 * @see RdTextButton
 * @see Button
 * @author Nathan Sweet */
public class RdImageTextButton extends Button {

    private final Image image;
    private RdLabel label;
    private RdImageTextButtonStyle style;

    public RdImageTextButton(@Null String text, Skin skin) {
        this(text, skin.get(RdImageTextButtonStyle.class));
        setSkin(skin);
    }

    public RdImageTextButton(@Null String text, Skin skin, String styleName) {
        this(text, skin.get(styleName, RdImageTextButtonStyle.class));
        setSkin(skin);
    }

    public RdImageTextButton(@Null String text, RdImageTextButtonStyle style) {
        super(style);
        this.style = style;

        defaults().space(3);

        image = newImage();

        label = newLabel(text, new RdLabel.RdLabelStyle(style.font, style.fontColor));
        label.setAlignment(Align.center);

        add(image);
        add(label).expand().center();

        setStyle(style);
        setSize(getPrefWidth(), getPrefHeight());

        if (style.padLeft != -1) padLeft(style.padLeft);
        if (style.padRight != -1) padRight(style.padRight);
        if (style.padBottom != -1) padBottom(style.padBottom);
        if (style.padTop != -1) padTop(style.padTop);
    }

    /** sets icon for all states */
    public void setImage(Drawable image) {
        style = new RdImageTextButtonStyle(style);
        style.imageUp = image;
        updateImage();
    }

    /** loads an icon from the skin by the specified name for all states */
    public void setImage(String name) {
        style = new RdImageTextButtonStyle(style);
        style.imageUp = new TextureRegionDrawable(RdAssetManager.current().findRegion(name));
        updateImage();
    }

    public RdImageTextButton(@Null String text, String styleName) {
        this(text, RdAssetManager.current().getSkin(), styleName);
    }

    public RdImageTextButton(@Null String text) {
        this(text, RdAssetManager.current().getSkin());
    }

    protected Image newImage () {
        return new Image(null, Scaling.fit);
    }

    protected RdLabel newLabel (String text, RdLabel.RdLabelStyle style) {
        style.scale = this.style.scaleText;
        return new RdLabel(text, style);
    }

    public void setStyle (ButtonStyle style) {
        if (!(style instanceof RdImageTextButtonStyle)) throw new IllegalArgumentException("style must be a ImageTextButtonStyle.");
        this.style = (RdImageTextButtonStyle) style;
        super.setStyle(style);

        if (image != null) updateImage();

        if (label != null) {
            RdImageTextButtonStyle textButtonStyle = (RdImageTextButtonStyle) style;
            label.setFont(textButtonStyle.font);
            label.setColor(textButtonStyle.fontColor);
        }
    }

    public RdImageTextButtonStyle getStyle () {
        return style;
    }

    /** Returns the appropriate image drawable from the style based on the current button state. */
    protected @Null Drawable getImageDrawable () {
        if (isDisabled() && style.imageDisabled != null) return style.imageDisabled;
        if (isPressed()) {
            if (isChecked() && style.imageCheckedDown != null) return style.imageCheckedDown;
            if (style.imageDown != null) return style.imageDown;
        }
        if (isOver()) {
            if (isChecked()) {
                if (style.imageCheckedOver != null) return style.imageCheckedOver;
            } else {
                if (style.imageOver != null) return style.imageOver;
            }
        }
        if (isChecked()) {
            if (style.imageChecked != null) return style.imageChecked;
            if (isOver() && style.imageOver != null) return style.imageOver;
        }
        return style.imageUp;
    }

    /** Sets the image drawable based on the current button state. The default implementation sets the image drawable using
     * {@link #getImageDrawable()}. */
    protected void updateImage () {
        image.setDrawable(getImageDrawable());
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

    @Override
    public void draw (Batch batch, float parentAlpha) {
        updateImage();
        label.setColor(getFontColor());
        super.draw(batch, parentAlpha);
    }

    public Image getImage () {
        return image;
    }

    public Cell getImageCell () {
        return getCell(image);
    }

    public void setLabel (RdLabel label) {
        getLabelCell().setActor(label);
        this.label = label;
    }

    public RdLabel getLabel () {
        return label;
    }

    public Cell getLabelCell () {
        return getCell(label);
    }

    public void setText (String text) {
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
        return (className.indexOf('$') != -1 ? "ImageTextButton " : "") + className + ": " + image.getDrawable() + " "
                + getText();
    }

    /** The style for an image text button, see {@link com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton}.
     * @author Nathan Sweet */
    static public class RdImageTextButtonStyle extends RdTextButton.RdTextButtonStyle {

        public @Null Drawable imageUp, imageDown, imageOver, imageDisabled;
        public @Null Drawable imageChecked, imageCheckedDown, imageCheckedOver;
        public float padLeft = -1, padRight = -1, padBottom = -1, padTop = -1;
        public float scaleText = 1;

        public RdImageTextButtonStyle() {
        }

        public RdImageTextButtonStyle(@Null Drawable up, @Null Drawable down, @Null Drawable checked, Font font) {
            super(up, down, checked, font);
        }

        public RdImageTextButtonStyle(RdImageTextButtonStyle style) {
            super(style);
            imageUp = style.imageUp;
            imageDown = style.imageDown;
            imageOver = style.imageOver;
            imageDisabled = style.imageDisabled;

            imageChecked = style.imageChecked;
            imageCheckedDown = style.imageCheckedDown;
            imageCheckedOver = style.imageCheckedOver;
            scaleText = style.scaleText;
        }

        public RdImageTextButtonStyle(RdTextButton.RdTextButtonStyle style) {
            super(style);
        }
    }
}
