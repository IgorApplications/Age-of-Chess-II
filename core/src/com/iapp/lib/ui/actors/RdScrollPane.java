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
 * */
public class RdScrollPane extends ScrollPane {

    private final RdScrollPaneStyle style;
    private final Array<Actor> actorList = new Array<>();
    private boolean isOver;
    private LoadingTable loading;

    public RdScrollPane(Actor widget) {
        this(widget, "default");
    }

    public RdScrollPane(Actor widget, String styleName) {
        this(widget, RdAssetManager.current().getSkin().get(styleName, RdScrollPane.RdScrollPaneStyle.class));
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

        if (style.loadingStyle != null) {
            loading = new LoadingTable(style.loadingStyle);
            loading.setVisible(false);
        }
    }

    public LoadingTable getLoading() {
        return loading;
    }

    public void setLoading(LoadingTable loading) {
        this.loading = loading;
    }

    /** true if the mouse cursor is over */
    public boolean isOver() {
        return isOver;
    }

    /** updates the list of actors to handle the cursor */
    @Override
    public void layout() {
        super.layout();
        Array<Actor> local = new Array<Actor>();
        local.addAll(getChildren());

        while (!local.isEmpty()) {
            Array<Actor> newLocal = new Array<Actor>();
            for (Actor child : local) {
                // TODO
                if (child instanceof Group && !(child instanceof Button)) {
                    newLocal.addAll(((Group) child).getChildren());
                } else {
                    actorList.add(child);
                }
            }
            local = newLocal;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        updateCursor();
        drawLoading(loading, batch);
    }

    protected void drawLoading(LoadingTable loading, Batch batch) {
        if (loading == null || !loading.isVisible()) return;

        Drawable background = style.background;
        float left = 0, right = 0, bottom = 0, top = 0;
        if (background != null) {
            left = background.getLeftWidth();
            right = background.getRightWidth();
            bottom = background.getBottomHeight();
            top = background.getTopHeight();
        }

        loading.setPosition(getX() + left, getY() + bottom);
        loading.setSize(getWidth() - left - right,
            getHeight() - bottom - top);
        loading.act(Gdx.graphics.getDeltaTime());
        loading.draw(batch, getColor().a);
    }

    // TODO
    /** updates the cursor state */
    private void updateCursor() {
        Cursor def = RdApplication.self().getCursor();
        boolean button = false;

        for (Actor actor : actorList) {

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
        public LoadingTable.LoadingStyle loadingStyle;

        public RdScrollPaneStyle() {}

        public RdScrollPaneStyle(Drawable background, Drawable hScroll, Drawable hScrollKnob,
                                 Drawable vScroll, Drawable vScrollKnob, Cursor cursor, boolean fadeScrollBars,
                                 boolean overscrollX, boolean overscrollY, LoadingTable.LoadingStyle loadingStyle) {
            super(background, hScroll, hScrollKnob, vScroll, vScrollKnob);
            this.cursor = cursor;
            this.fadeScrollBars = fadeScrollBars;
            this.overscrollX = overscrollX;
            this.overscrollY = overscrollY;
            this.loadingStyle = loadingStyle;
        }

        public RdScrollPaneStyle(RdScrollPaneStyle style) {
            cursor = style.cursor;
            fadeScrollBars = style.fadeScrollBars;
            overscrollX = style.overscrollX;
            overscrollY = style.overscrollY;
            loadingStyle = new LoadingTable.LoadingStyle(style.loadingStyle);
        }
    }
}
