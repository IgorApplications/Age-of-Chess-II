package com.iapp.lib.ui.actors;

import com.badlogic.gdx.scenes.scene2d.actions.SizeByAction;

public class CenterSizeByAction extends SizeByAction {
    private float amountWidthPercent, amountHeightPercent;

    protected void updateRelative (float percentDelta) {
        float difW = target.getWidth() * percentDelta * amountWidthPercent;
        float difH = target.getHeight() * percentDelta * amountHeightPercent;

        target.setBounds(target.getX() - difW / 2, target.getY() - difH / 2,
            target.getWidth() + difW, target.getHeight() + difH);
    }

    public void setAmount (float width, float height) {
        amountWidthPercent = width;
        amountHeightPercent = height;
    }

    public float getAmountWidth () {
        return amountWidthPercent;
    }

    public void setAmountWidth (float width) {
        amountWidthPercent = width;
    }

    public float getAmountHeight () {
        return amountHeightPercent;
    }

    public void setAmountHeight (float height) {
        amountHeightPercent = height;
    }
}
