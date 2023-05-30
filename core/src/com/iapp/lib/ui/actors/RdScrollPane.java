package com.iapp.lib.ui.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.iapp.lib.ui.screens.RdApplication;
import com.iapp.lib.ui.screens.RdAssetManager;

/**
 * Scroll bar with cursor support and additional default settings
 * @author Igor Ivanov
 * @version 1.0
 * */
public class RdScrollPane extends ScrollPane {

    private RdScrollPaneStyle style;
    private boolean isOver;
    private final Array<Actor> actorList = new Array<>();

    public RdScrollPane(Actor widget) {
        this(widget, RdAssetManager.current().getSkin());
    }

    public RdScrollPane(Actor widget, Skin skin) {
        this(widget, skin, "default");
    }

    public RdScrollPane(Actor widget, Skin skin, String styleName) {
        this(widget, skin.get(styleName, RdScrollPaneStyle.class));
    }

    /** true if the mouse cursor is over */
    public boolean isOver() {
        return isOver;
    }

    public void setStyle(RdScrollPaneStyle style) {
        super.setStyle(style);
        this.style = style;
    }

    @Override
    public RdScrollPaneStyle getStyle() {
        return style;
    }

    /** updates the list of actors to handle the cursor */
    @Override
    public void layout() {
        super.layout();
        var local = new Array<Actor>();
        local.addAll(getChildren());

        while (!local.isEmpty()) {
            var newLocal = new Array<Actor>();
            for (var child : local) {
                if (child instanceof Group && !(child instanceof Button)) {
                    newLocal.addAll(((Group) child).getChildren());
                } else {
                    actorList.add(child);
                }
            }
            local = newLocal;
        }
    }

    public RdScrollPane(Actor widget, RdScrollPaneStyle style) {
        super(widget, style);
        this.style = style;

        // checks if the mouse is hovered
        addListener(new InputListener() {

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                isOver = true;
                super.enter(event, x, y, pointer, fromActor);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                isOver = false;
                super.exit(event, x, y, pointer, toActor);
            }
        });

        setFadeScrollBars(style.fadeScrollBars);
        setOverscroll(style.overscrollX, style.overscrollY);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        updateCursor();
    }

    /** updates the cursor state */
    private void updateCursor() {
        var def = RdApplication.self().getCursor();
        boolean button = false;

        for (var actor : actorList) {

            if (actor instanceof Button) {

                if (((Button) actor).isOver()) {
                    button = true;
                    break;
                }
            }

            if (actor instanceof  RdTextField) {
                if (((RdTextField) actor).isOver()) {
                   button = true;
                    break;
                }
            }
        }

        if ((Gdx.input.isButtonPressed(Input.Buttons.LEFT)
                || Gdx.input.isButtonPressed(Input.Buttons.RIGHT))
                && isOver && !button) {

            if (style.cursor != null) {
                Gdx.graphics.setCursor(style.cursor);
            }

        } else {

            if (def != null) {
                Gdx.graphics.setCursor(def);
            }

        }

    }

    public static class RdScrollPaneStyle extends ScrollPane.ScrollPaneStyle {

        public Cursor cursor;
        public boolean fadeScrollBars = true;
        public boolean overscrollX = true, overscrollY = true;

        public RdScrollPaneStyle() {
            super();
        }

        public RdScrollPaneStyle(Drawable background, Drawable hScroll, Drawable hScrollKnob, Drawable vScroll, Drawable vScrollKnob) {
            super(background, hScroll, hScrollKnob, vScroll, vScrollKnob);
        }

        public RdScrollPaneStyle(ScrollPaneStyle style) {
            super(style);
        }
    }
}
