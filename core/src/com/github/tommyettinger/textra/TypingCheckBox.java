/*
 * Copyright (c) 2022-2023 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.tommyettinger.textra;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.Scaling;

/**
 * A checkbox is a button that contains an image indicating the checked or unchecked state and a {@link TypingLabel}.
 *
 * @author Nathan Sweet
 */
public class TypingCheckBox extends TypingButton {
    private final Image image;
    private final Cell<?> imageCell;
    private CheckBox.CheckBoxStyle style;

    public TypingCheckBox(@Null String text, Skin skin) {
        this(text, skin.get(CheckBox.CheckBoxStyle.class));
    }

    public TypingCheckBox(@Null String text, Skin skin, String styleName) {
        this(text, skin.get(styleName, CheckBox.CheckBoxStyle.class));
    }

    public TypingCheckBox(@Null String text, CheckBox.CheckBoxStyle style) {
        this(text, style, new Font(style.font));
    }

    public TypingCheckBox(@Null String text, Skin skin, Font replacementFont) {
        this(text, skin.get(CheckBox.CheckBoxStyle.class), replacementFont);
    }

    public TypingCheckBox(@Null String text, Skin skin, String styleName, Font replacementFont) {
        this(text, skin.get(styleName, CheckBox.CheckBoxStyle.class), replacementFont);
    }

    public TypingCheckBox(@Null String text, CheckBox.CheckBoxStyle style, Font replacementFont) {
        super(text, style, replacementFont);

        TypingLabel label = (TypingLabel) getTextraLabel();
        label.setAlignment(Align.left);

        image = newImage();
        image.setDrawable(style.checkboxOff);

        addActorBefore(image, label);
        imageCell = getCell(image);
        pack();
        setSize(getPrefWidth(), getPrefHeight());
    }

    protected Image newImage() {
        return new Image(null, Scaling.none);
    }

    public void setStyle(ButtonStyle style) {
        if (!(style instanceof CheckBox.CheckBoxStyle)) throw new IllegalArgumentException("style must be a CheckBoxStyle.");
        this.style = (CheckBox.CheckBoxStyle) style;
        super.setStyle(style);
    }

    public void setStyle(ButtonStyle style, boolean makeGridGlyphs) {
        if (!(style instanceof CheckBox.CheckBoxStyle)) throw new IllegalArgumentException("style must be a CheckBoxStyle.");
        this.style = (CheckBox.CheckBoxStyle) style;
        super.setStyle(style, makeGridGlyphs);
    }

    public void setStyle(ButtonStyle style, Font font) {
        if (!(style instanceof CheckBox.CheckBoxStyle)) throw new IllegalArgumentException("style must be a CheckBoxStyle.");
        this.style = (CheckBox.CheckBoxStyle) style;
        super.setStyle(style, font);
    }

    /**
     * Returns the checkbox's style. Modifying the returned style may not have an effect until {@link #setStyle(ButtonStyle)} is
     * called.
     */
    public CheckBox.CheckBoxStyle getStyle() {
        return style;
    }

    public void draw(Batch batch, float parentAlpha) {
        Drawable checkbox = null;
        if (isDisabled()) {
            if (isChecked() && style.checkboxOnDisabled != null)
                checkbox = style.checkboxOnDisabled;
            else
                checkbox = style.checkboxOffDisabled;
        }
        if (checkbox == null) {
            boolean over = isOver() && !isDisabled();
            if (isChecked() && style.checkboxOn != null)
                checkbox = over && style.checkboxOnOver != null ? style.checkboxOnOver : style.checkboxOn;
            else if (over && style.checkboxOver != null)
                checkbox = style.checkboxOver;
            else
                checkbox = style.checkboxOff;
        }
        image.setDrawable(checkbox);
        super.draw(batch, parentAlpha);
    }

    public Image getImage() {
        return image;
    }

    public Cell<?> getImageCell() {
        return imageCell;
    }
}
