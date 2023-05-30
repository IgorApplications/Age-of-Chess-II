package com.iapp.lib.ui.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.github.tommyettinger.textra.Font;
import com.iapp.lib.ui.screens.RdAssetManager;
import com.iapp.lib.ui.screens.RdLogger;

/**
 * Actor for logging to screen RAM and FPS
 * @author Igor Ivanov
 * @version 1.0
 * */
public class LoggingView extends Table {

    private final LoggingViewStyle style;
    private RdLabel fps, ram;

    public LoggingView() {
        this(RdAssetManager.current().getSkin());
    }

    public LoggingView(Skin skin) {
        this(skin.get(LoggingViewStyle.class));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        fps.setText(String.valueOf(RdLogger.self().getFPS()));
        ram.setText(RdLogger.self().getRAM() + "Mb");
    }

    public LoggingView(Skin skin, String styleName) {
        this(skin.get(styleName, LoggingViewStyle.class));
    }

    public LoggingView(LoggingViewStyle style) {
        this.style = style;
        init();
    }

    private void init() {
        align(Align.topLeft);
        fps = new RdLabel("");
        fps.setDefaultToken("[%75]");
        fps.setColor(style.colorFPS);
        ram = new RdLabel("");
        ram.setDefaultToken("[%75]");
        ram.setColor(style.colorRAM);

        String strVersion = String.valueOf(RdLogger.self().getVersion());
        RdLabel version = new RdLabel(strVersion.charAt(0) + "." + strVersion.substring(1, 3)
            + "." + strVersion.substring(3));
        version.setDefaultToken("[%75]");
        RdLabel time = new RdLabel(RdLogger.self().getTime());
        time.setDefaultToken("[%75]");

        RdTable column1 = new RdTable();
        column1.add(fps).padRight(10);
        column1.add(ram);
        RdTable column2 = new RdTable();
        column2.add(version).padRight(10);
        column2.add(time);

        add(column1).expand().align(Align.topLeft).row();
        add(column2).expandX().align(Align.topLeft);
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
