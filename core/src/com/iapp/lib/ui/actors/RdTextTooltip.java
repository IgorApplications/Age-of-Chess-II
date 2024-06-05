package com.iapp.lib.ui.actors;

import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import com.github.tommyettinger.textra.Font;
import com.iapp.lib.ui.screens.RdAssetManager;

public class RdTextTooltip extends RdTooltip<RdLabel> {

    public RdTextTooltip(@Null String text, Skin skin) {
        this(text, getDefaultManager(), skin.get(RdTextTooltipStyle.class));
    }

    public RdTextTooltip(@Null String text, Skin skin, String styleName) {
        this(text, getDefaultManager(), skin.get(styleName, RdTextTooltipStyle.class));
    }

    public RdTextTooltip(@Null String text, RdTextTooltipStyle style) {
        this(text, getDefaultManager(), style);
    }

    public RdTextTooltip(@Null String text, RdTooltipManager manager, Skin skin) {
        this(text, manager, skin.get(RdTextTooltipStyle.class));
    }

    public RdTextTooltip(@Null String text, RdTooltipManager manager, Skin skin, String styleName) {
        this(text, manager, skin.get(styleName, RdTextTooltipStyle.class));
    }

    public RdTextTooltip(@Null String text) {
        this(text, RdAssetManager.current().getSkin());
    }

    public RdTextTooltip(@Null String text, String styleName) {
        this(text, RdAssetManager.current().getSkin(), styleName);
    }

    public RdTextTooltip(@Null String text, RdTooltipManager manager, RdTextTooltipStyle style) {
        super(null, manager);

        RdLabel label = new RdLabel(text, style.labelStyle);
        label.setAlignment(Align.center);
        label.setWrap(true);
        label.layout.setTargetWidth(style.wrapWidth);
        getContainer().setActor(label);
        getContainer().width(style.wrapWidth);
        setStyle(style);
        label.setText(text);
    }

    public void setStyle(RdTextTooltipStyle style) {
        if (style == null) throw new NullPointerException("style cannot be null");
        Font font = style.labelStyle.font;

        Container<RdLabel> container = getContainer();
        container.getActor().setFont(font, false);
        container.getActor().layout.setTargetWidth(style.wrapWidth);
        if (style.labelStyle.color != null) container.getActor().setColor(style.labelStyle.color);
        font.regenerateLayout(container.getActor().layout);
        font.calculateSize(container.getActor().layout);
        container.getActor().setWidth(container.getActor().layout.getWidth());
        container.setBackground(style.background);
        container.maxWidth(style.wrapWidth);
        setAlwaysTop(style.alwaysTop);
    }

    public static class RdTextTooltipStyle {

        public RdLabel.RdLabelStyle labelStyle;
        public @Null Drawable background;
        public boolean alwaysTop;
        /** 0 means don't wrap. */
        public float wrapWidth;

        public RdTextTooltipStyle() {}

        public RdTextTooltipStyle(RdLabel.RdLabelStyle labelStyle, @Null Drawable background) {
            this.labelStyle = labelStyle;
            this.background = background;
        }

        public RdTextTooltipStyle(RdTextTooltipStyle style) {
            labelStyle = new RdLabel.RdLabelStyle(style.labelStyle);
            background = style.background;
            wrapWidth = style.wrapWidth;
        }
    }

    private static RdTooltipManager getDefaultManager() {
        RdTooltipManager tooltipManager = new RdTooltipManager();
        tooltipManager.instant();
        return tooltipManager;
    }
}
