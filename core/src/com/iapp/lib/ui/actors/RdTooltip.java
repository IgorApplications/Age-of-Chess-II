package com.iapp.lib.ui.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager;
import com.badlogic.gdx.utils.Null;

/** A listener that shows a tooltip actor when the mouse is over another actor.
 * @author Nathan Sweet
 * @author Igor Ivanov
 * */
public class RdTooltip<T extends Actor> extends InputListener {
    static Vector2 tmp = new Vector2();

    private final RdTooltipManager manager;
    final Container<T> container;
    boolean instant, always, touchIndependent, alwaysTop;
    Actor targetActor;

    /** @param contents May be null. */
    public RdTooltip(@Null T contents) {
        this(contents, RdTooltipManager.getInstance());
    }

    /** @param contents May be null. */
    public RdTooltip(@Null T contents, RdTooltipManager manager) {
        this.manager = manager;

        container = new Container<>(contents) {
            public void act(float delta) {
                super.act(delta);
                if (targetActor != null && targetActor.getStage() == null) remove();
            }
        };
        container.setTouchable(Touchable.disabled);
    }

    public RdTooltipManager getManager () {
        return manager;
    }

    public Container<T> getContainer () {
        return container;
    }

    public void setActor (@Null T contents) {
        container.setActor(contents);
    }

    public @Null T getActor () {
        return container.getActor();
    }

    public void setAlwaysTop(boolean alwaysTop) {
        this.alwaysTop = alwaysTop;
    }

    public boolean isAlwaysTop() {
        return alwaysTop;
    }

    /** If true, this tooltip is shown without delay when hovered. */
    public void setInstant (boolean instant) {
        this.instant = instant;
    }

    /** If true, this tooltip is shown even when tooltips are not {@link TooltipManager#enabled}. */
    public void setAlways (boolean always) {
        this.always = always;
    }

    /** If true, this tooltip will be shown even when screen is touched simultaneously with entering tooltip's targetActor */
    public void setTouchIndependent (boolean touchIndependent) {
        this.touchIndependent = touchIndependent;
    }

    public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
        super.touchDown(event, x, y, pointer, button);
        // restart
        manager.instant();

        if (touchIndependent && Gdx.input.isTouched()) return false;
        var actor = event.getListenerActor();

        setContainerPosition(actor, x, y);
        manager.touchDown(this);

        return true;
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        super.touchDown(event, x, y, pointer, button);
        hide();
    }

    public boolean mouseMoved (InputEvent event, float x, float y) {
        if (container.hasParent()) return false;
        setContainerPosition(event.getListenerActor(), x, y);
        return true;
    }

    private void setContainerPosition (Actor actor, float x, float y) {
        this.targetActor = actor;
        var stage = actor.getStage();
        if (stage == null) return;

        container.setSize(manager.maxWidth, Integer.MAX_VALUE);
        container.validate();
        container.width(container.getActor().getWidth());
        container.pack();

        float offsetX = manager.offsetX, offsetY = manager.offsetY, dist = manager.edgeDistance;
        Vector2 point = actor.localToStageCoordinates(tmp.set(x + offsetX, y - offsetY - container.getHeight()));

        if (point.x < dist) point.x = dist;
        if (point.x + container.getWidth() > stage.getWidth() - dist)
            point.x = stage.getWidth() - dist - container.getWidth();
        if (!alwaysTop) {
            if (point.y < dist) point = actor.localToStageCoordinates(tmp.set(x + offsetX, y + offsetY));
            if (point.y + container.getHeight() > stage.getHeight() - dist)
                point.y = stage.getHeight() - dist - container.getHeight();
        } else {
            point.y = point.y + dist + container.getHeight();
        }
        container.setPosition(point.x, point.y);

        point = actor.localToStageCoordinates(tmp.set(actor.getWidth() / 2, actor.getHeight() / 2));
        point.sub(container.getX(), container.getY());
        container.setOrigin(point.x, point.y);
    }

    public void hide () {
        manager.hide(this);
    }
}

