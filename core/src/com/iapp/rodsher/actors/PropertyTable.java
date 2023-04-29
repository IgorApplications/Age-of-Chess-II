package com.iapp.rodsher.actors;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import com.iapp.rodsher.screens.RdAssetManager;

/**
 * Property description table, property
 * group headers, and property controls
 * @author Igor Ivanov
 * @version 1.0
 * */
public class PropertyTable extends RdTable {

    /** table item store */
    private final RdTable content;
    /** content scroll area */
    private final RdScrollPane scrollArea;
    /** table for component background */
    private final RdTable background;
    /** width in the table dedicated to control actors */
    private final float actorWidth;
    private PropertyTableStyle style;

    public PropertyTable(float actorWidth, PropertyTableStyle style) {
        this.style = style;
        this.actorWidth = actorWidth;

        background = new RdTable();
        background.add(new Image(style.panel)).expand().fill();
        background.add(new Image()).width(actorWidth + 10).expandY().fillY();
        background.setFillParent(true);

        content = new RdTable();
        content.addActor(background);
        content.align(Align.topLeft);
        scrollArea = new RdScrollPane(content, style.scrollStyle);
        scrollArea.setScrollingDisabled(true, false);
        add(scrollArea).expand().fill();
    }

    public PropertyTable(float actorWidth, Skin skin) {
        this(actorWidth, skin.get(PropertyTableStyle.class));
    }

    public PropertyTable(float actorWidth, Skin skin, String styleName) {
        this(actorWidth, skin.get(styleName, PropertyTableStyle.class));
    }

    public PropertyTable(float actorWidth) {
        this(actorWidth, RdAssetManager.current().getSkin());
    }

    public PropertyTable(float actorWidth, String styleName) {
        this(actorWidth, RdAssetManager.current().getSkin(), styleName);
    }

    public void setStyle(PropertyTableStyle style) {
        this.style = style;
    }

    public PropertyTableStyle getStyle() {
        return style;
    }

    /** sets the visibility of the background, if false then the background will not be visible */
    public void setVisibleBackground(boolean visible) {
        background.setVisible(visible);
    }

    /** returns the content scroll area */
    public ScrollPane getScrollArea() {
        return scrollArea;
    }

    /** adds a title to the table */
    public void add(Title title) {
        var labelTitle = new RdLabel(title.text, style.titleStyle);
        labelTitle.setWrap(true);

        var table = new RdTable();
        if (style.panel != null) table.setBackground(style.panel);
        table.add(labelTitle).expand().fill().pad(5, 5, 5, 5);

        content.add(table).expandX().fillX().colspan(2).row();
    }

    /** adds an element to the table */
    public void add(Element element) {
        var textElement = new RdLabel(element.text, style.elementStyle);
        textElement.setWrap(true);

        var textTable = new Table();
        textTable.add(textElement).expandX().fillX();
        if (element.textActor != null) {
            textTable.add(element.textActor);
        }

        content.add(textTable).expandX().left().fillX().pad(5, 5, 5, 5);
        content.add(element.actor).left().width(actorWidth).pad(5, 5, 5, 5).fill().row();
    }

    /** returns storage of elements */
    public RdTable getContent() {
        return content;
    }

    /** Table headers */
    public static class Title {

        /** table header text */
        private final String text;

        public Title(String text) {
            this.text = text;
        }
    }

    /** table row with two cells */
    public static class Element {

        /** text description */
        private final String text;
        /** control actor */
        private final Actor actor;
        /** additional actor to the right of the text */
        private final Actor textActor;

        public Element(String text, Actor actor) {
            this.text = text;
            textActor = null;
            this.actor = actor;
        }

        public Element(String text, Actor textActor, Actor actor) {
            this.text = text;
            this.textActor = textActor;
            this.actor = actor;
        }
    }

    public static class PropertyTableStyle {

        /** first column background */
        public @Null Drawable panel;
        public RdLabel.RdLabelStyle titleStyle;
        public RdLabel.RdLabelStyle elementStyle;
        public RdScrollPane.RdScrollPaneStyle scrollStyle;

        public PropertyTableStyle() {}

        public PropertyTableStyle(Drawable panel, RdLabel.RdLabelStyle titleStyle, RdLabel.RdLabelStyle elementStyle,
                                  RdScrollPane.RdScrollPaneStyle scrollStyle) {
            this.panel = panel;
            this.titleStyle = titleStyle;
            this.elementStyle = elementStyle;
            this.scrollStyle = scrollStyle;
        }

        public PropertyTableStyle(PropertyTableStyle style) {
            panel = style.panel;
            titleStyle = style.titleStyle;
            elementStyle = style.elementStyle;
            scrollStyle = style.scrollStyle;
        }
    }
}
