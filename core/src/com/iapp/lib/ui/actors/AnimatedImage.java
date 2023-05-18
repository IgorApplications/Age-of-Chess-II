package com.iapp.lib.ui.actors;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * This image performs an animated rendering, similar to the GIF format,
 * by changing images after a certain time interval
 * @author Igor Ivanov
 * @version 1.0
 * */
public class AnimatedImage extends Image {

    /** parts of an animated image */
    private final TextureRegionDrawable[] drawables;
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
    public AnimatedImage(long intervalMillis, TextureRegionDrawable... drawables) {
        this.intervalMillis = intervalMillis;
        this.drawables = copy(drawables);
        setDrawable(drawables[index]);
    }

    /**
     * makes a full copy of the state of the constructor argument
     * */
    public AnimatedImage(AnimatedImage animatedImage) {
        drawables = copy(animatedImage.drawables);
        intervalMillis = animatedImage.intervalMillis;
        lastDraw = animatedImage.lastDraw;
        index = animatedImage.index;
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

    private TextureRegionDrawable[] copy(TextureRegionDrawable[] drawables) {
        TextureRegionDrawable[] array = new TextureRegionDrawable[drawables.length];
        for (int i = 0; i < drawables.length; i++) {
            array[i] = new TextureRegionDrawable(drawables[i]);
        }
        return array;
    }
}
