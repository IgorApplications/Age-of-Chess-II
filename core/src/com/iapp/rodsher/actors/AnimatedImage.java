package com.iapp.rodsher.actors;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * This image performs an animated rendering, similar to the GIF format,
 * by changing images after a certain time interval
 * @author Igor Ivanov
 * @version 1.0
 * */
public class AnimatedImage extends Image {

    /** parts of an animated image */
    private final Drawable[] drawables;
    /** animation speed */
    private final long intervalMillis;
    /** time of the last animation image change */
    private long lastDraw;
    /** pointer to current image */
    private int index;

    /**
     * Creates an animation object
     * @param intervalMillis - animation speed
     * @param drawables - animation frames
     * */
    public AnimatedImage(long intervalMillis, Drawable... drawables) {
        this.intervalMillis = intervalMillis;
        this.drawables = drawables;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (System.currentTimeMillis() - lastDraw >= intervalMillis) {
            if (index == drawables.length - 1) index = 0;
            else index++;

            setDrawable(drawables[index]);
            lastDraw = System.currentTimeMillis();
        }
    }

    /** returns current drawable */
    @Override
    public Drawable getDrawable() {
        return drawables[index];
    }
}
