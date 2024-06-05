package com.iapp.lib.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/**
 * Sample texture work
 * @author Igor Ivanov
 * @version 1.0
 * */
public class TextureUtil {

    /**
     * Texture creation with specified properties
     * @param width - texture width
     * @param height - texture height
     * @param color - texture color
     * */
    public static Texture create(float width, float height, Color color) {
        if (color == null) throw new IllegalArgumentException("color == null");

        int roundedW = Math.round(width);
        int roundedH = Math.round(height);

        Pixmap pixmap = new Pixmap(roundedW, roundedH, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fillRectangle(0, 0, roundedW, roundedH);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        return texture;
    }
}
