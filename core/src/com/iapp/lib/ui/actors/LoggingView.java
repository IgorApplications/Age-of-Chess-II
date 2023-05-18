package com.iapp.lib.ui.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.tommyettinger.textra.Font;
import com.iapp.lib.ui.screens.RdAssetManager;
import com.iapp.lib.ui.screens.RdLogger;

/**
 * Actor for logging to screen RAM and FPS
 * @author Igor Ivanov
 * @version 1.0
 * */
public class LoggingView extends RdLabel {

    /** Text to the right of FPS */
    private String endFPS = "";
    /** Text to the right of RAM */
    private String endRAM = "Mb";
    private final LoggingViewStyle style;

    public LoggingView() {
        this(RdAssetManager.current().getSkin());
    }

    public LoggingView(Skin skin) {
        this(skin.get(LoggingViewStyle.class));
    }

    public LoggingView(Skin skin, String styleName) {
        this(skin.get(styleName, LoggingViewStyle.class));
    }

    public LoggingView(LoggingViewStyle style) {
        super("", style);
        this.style = style;
    }

    /** returns text to the right of FPS */
    public String getEndFPS() {
        return endFPS;
    }

    /** sets text to the right of FPS */
    public void setEndFPS(String endFPS) {
        this.endFPS = endFPS;
    }

    /** returns  text to the right of RAM */
    public String getEndRAM() {
        return endRAM;
    }

    /** sets text to the right of RAM */
    public void setEndRAM(String endRAM) {
        this.endRAM = endRAM;
    }

    private long last;

    @SuppressWarnings("DefaultLocale")
    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        setWrap(false);
        if (System.currentTimeMillis() - last > 100) {
            setText(String.format("[%s]%d%s [%s]%d%s",
                    getHex(style.colorFPS), RdLogger.getFPS(), endFPS,
                    getHex(style.colorRAM), RdLogger.getRAM(), endRAM));
            last = System.currentTimeMillis();
        }
    }

    public static class LoggingViewStyle extends RdLabel.RdLabelStyle {
        public Color colorFPS, colorRAM;

        public LoggingViewStyle() {
            super();
        }

        public LoggingViewStyle(Font font, Color fontColor, Color colorFPS, Color colorRAM) {
            super(font, fontColor);
            this.colorFPS = colorFPS;
            this.colorRAM = colorRAM;
        }

        public LoggingViewStyle(LoggingViewStyle style) {
            super(style);
            colorFPS = style.colorFPS;
            colorRAM = style.colorRAM;
        }
    }

    private String getHex(Color color) {
        return "#" + color.toString();
    }
}
