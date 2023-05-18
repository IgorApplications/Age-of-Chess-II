package com.iapp.lib.ui.actors;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

/**
 * Vertically combines two patches and adapts as one drawable
 * @author Igor Ivanov
 * @version 1.0
 * */
public class TwoNinePath implements Drawable {

    private final NinePatchDrawable first;
    private final NinePatchDrawable second;
    private float secondHeight = 0;

    /** Creates a common drawable, where the first is drawn from above, the second from below */
    public TwoNinePath(NinePatchDrawable first, NinePatchDrawable second) {
        this.first = first;
        this.second = second;
    }

    /** sets the height of the second drawable, the height of the first is calculated as height - secondHeight */
    public void setSecondHeight(float secondHeight) {
        this.secondHeight = secondHeight;
    }

    /** returns the upper patch */
    public NinePatchDrawable getFirst() {
        return first;
    }

    /** returns the lower patch */
    public NinePatchDrawable getSecond() {
        return second;
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        height -= secondHeight;
        second.draw(batch, x, y, width, secondHeight);
        first.draw(batch, x, y + secondHeight, width, height);
    }

    @Override
    public float getLeftWidth() {
        return Math.max(first.getLeftWidth(), second.getRightWidth());
    }

    @Override
    public void setLeftWidth(float leftWidth) {
        first.setLeftWidth(leftWidth);
        second.setLeftWidth(leftWidth);
    }

    @Override
    public float getRightWidth() {
        return Math.max(first.getRightWidth(), second.getRightWidth());
    }

    @Override
    public void setRightWidth(float rightWidth) {
        first.setRightWidth(rightWidth);
        second.setRightWidth(rightWidth);
    }

    @Override
    public float getTopHeight() {
        return Math.max(first.getTopHeight(), second.getTopHeight());
    }

    @Override
    public void setTopHeight(float topHeight) {
        first.setTopHeight(topHeight);
        second.setTopHeight(topHeight);
    }

    @Override
    public float getBottomHeight() {
        return Math.max(first.getBottomHeight(), second.getBottomHeight());
    }

    @Override
    public void setBottomHeight(float bottomHeight) {
        first.setBottomHeight(bottomHeight);
        second.setBottomHeight(bottomHeight);
    }

    @Override
    public float getMinWidth() {
        return Math.min(first.getMinWidth(), second.getRightWidth());
    }

    @Override
    public void setMinWidth(float minWidth) {
        first.setMinWidth(minWidth);
        second.setMinWidth(minWidth);
    }

    @Override
    public float getMinHeight() {
        return Math.min(first.getMinHeight(), second.getMinHeight());
    }

    @Override
    public void setMinHeight(float minHeight) {
        first.setMinHeight(minHeight);
        second.setMinHeight(minHeight);
    }
}
