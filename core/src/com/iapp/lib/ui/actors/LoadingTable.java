package com.iapp.lib.ui.actors;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Null;
import com.iapp.lib.ui.screens.RdAssetManager;

public class LoadingTable extends Table {

    private RdLabel loadingText;

    public LoadingTable(String styleName) {
        this(RdAssetManager.current().getSkin().get(styleName, LoadingStyle.class));
    }

    public LoadingTable() {
        this("default");
    }

    public LoadingTable(LoadingStyle style) {
        if (style.loadingAnim != null) {
            add(style.loadingAnim);
        }

        if (style.loadingText != null) {
            loadingText = new RdLabel("",  style.loadingText);
            add(loadingText);
        }

        setBackground(style.loadingBg);
    }

    public void setLoadingText(String text) {
        loadingText.setText(text);
    }

    public static class LoadingStyle {

        public @Null Drawable loadingBg;
        public @Null AnimatedImage loadingAnim;

        public @Null RdLabel.RdLabelStyle loadingText;

        public LoadingStyle() {}

        public LoadingStyle(Drawable loadingBg, AnimatedImage loadingAnim, RdLabel.RdLabelStyle loadingText) {
            this.loadingBg = loadingBg;
            this.loadingAnim = loadingAnim;
            this.loadingText = loadingText;
        }

        public LoadingStyle(LoadingStyle style) {
            loadingBg = style.loadingBg;
            loadingAnim = new AnimatedImage(style.loadingAnim);
            loadingText = style.loadingText;
        }
    }
}
