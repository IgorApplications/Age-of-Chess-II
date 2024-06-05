package com.iapp.lib.ui.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.iapp.lib.ui.screens.RdAssetManager;

/**
 * A table with a title and a line around it, consists of three drawables
 * @author Igor Ivanov
 * @version 1.0
 * */
public class LineTable extends RdTable {

    /** actor style */
    private final LineTableStyle style;
    /** actor background */
    private RdTable background;
    /** table header */
    private RdLabel title;
    /** header text */
    private String textTitle = "";
    private RdCell<Image> left, right;

    public LineTable(String textTitle, LineTableStyle style) {
        this.style = style;
        this.textTitle = textTitle;
        initialize();
    }

    public LineTable(String styleName, String textTitle) {
        this(textTitle, RdAssetManager.current().getSkin().get(styleName, LineTable.LineTableStyle.class));
    }

    public LineTable(String textTitle) {
        this("default", textTitle);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        layout();

        background.act(Gdx.graphics.getDeltaTime());
        background.draw(batch, parentAlpha);

        super.draw(batch, parentAlpha);
    }

    @Override
    public void layout() {
        background.setBounds(getX(), getY(),
            Math.max(title.getPrefWidth() + style.part1.getMinWidth() + style.part2.getMinWidth(), getWidth()),
            Math.max(title.getPrefHeight() + style.part1.getMinHeight() + style.part3.getMinHeight(), getHeight()));

        left.padTop(title.getPrefHeight() / 2);
        right.padTop(title.getPrefHeight() / 2);

        pad(style.part1.getLeftWidth() + title.getPrefHeight(),
            style.part1.getTopHeight(),
            style.part3.getBottomHeight(),
            style.part2.getRightWidth());

        super.layout();
    }

    /** returns table header */
    public RdLabel getTitle() {
        return title;
    }

    /**
     * Designed to be overridden in child classes
     * to add or change functionality
     * */
    protected void initialize() {
        background = new RdTable();

        title = new RdLabel(textTitle, style.labelStyle);
        title.setAlignment(Align.center);

        left = background.add(new Image(style.part1)).fillY();
        background.add(title).fill().align(Align.center);
        right = background.add(new Image(style.part2)).expandX().fill();
        background.row();

        background.add(new Image(style.part3)).expand().fill().colspan(3);
        layout();
    }

    public static class LineTableStyle {

        /**
         * part1 - top left of heading text
         * part2 - top right after title text
         * part3 - bottom for content
         * */
        public Drawable part1, part2, part3;
        /** title text style */
        public RdLabel.RdLabelStyle labelStyle;

        public LineTableStyle(Drawable part1, Drawable part2,
                              Drawable part3, RdLabel.RdLabelStyle labelStyle) {
            this.part1 = part1;
            this.part2 = part2;
            this.part3 = part3;
            this.labelStyle = labelStyle;
        }

        public LineTableStyle() {}

        public LineTableStyle(LineTableStyle style) {
            part1 = style.part1;
            part2 = style.part2;
            part3 = style.part3;
            labelStyle = style.labelStyle;
        }
    }
}
