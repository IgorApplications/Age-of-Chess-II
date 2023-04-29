package com.iapp.rodsher.actors;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.iapp.rodsher.screens.RdAssetManager;
import com.iapp.rodsher.util.OnChangeListener;

import java.util.Objects;

/**
 * Builder of standard dialog boxes from text and buttons
 * @author Igor Ivanov
 * @version 1.0
 * */
public class RdDialogBuilder {

    /** dialog box text data */
    protected String contentText = "",
            acceptText = "",
            cancelText = "",
            title = "";
    /** accept and reject button listeners */
    protected OnChangeListener onCancel, onAccept, onHide;

    /**
     * set the main text of the dialog box
     * */
    public RdDialogBuilder text(String text) {
        contentText = text;
        return this;
    }

    /**
     * set text and listener on accept button
     * @param text - accept button text
     * @param listener - accept button listener
     * */
    public RdDialogBuilder accept(String text, OnChangeListener listener) {
        acceptText = text;
        onAccept = listener;
        return this;
    }

    /**
     * set text on accept button
     * @param text - accept button text
     * */
    public RdDialogBuilder accept(String text) {
        return accept(text, null);
    }

    /**
     * Sets the window close listener
     * @param onHide - window close listener
     * */
    public RdDialogBuilder onHide(OnChangeListener onHide) {
        this.onHide = onHide;
        return this;
    }

    /**
     * set text and listener on cancel button
     * @param text - cancel button text
     * @param listener - cancel button listener
     * */
    public RdDialogBuilder cancel(String text, OnChangeListener listener) {
        cancelText = text;
        onCancel = listener;
        return this;
    }

    /**
     * set text on cancel button
     * @param text - cancel button text
     * */
    public RdDialogBuilder cancel(String text) {
        return cancel(text, null);
    }

    /** sets the title text of the dialog box */
    public RdDialogBuilder title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Collect result dialog box from received data
     * @param style - the style of the dialog box and its actors
     * @return result dialog
     * */
    public RdDialog build(RdDialogBuilderStyle style) {
        var dialog = new RdDialog(title, style.rdDialogStyle);
        if (onHide != null) dialog.setOnCancel(onHide);
        else {
            if (onCancel != null) {
                dialog.setOnCancel(onCancel);
            }
        }

        var contentLabel = new RdLabel(contentText, style.textStyle);
        contentLabel.setWrap(true);
        contentLabel.setAlignment(Align.topLeft);

        var contentTable = new RdTable();
        contentTable.add(contentLabel).expand().fill();

        var scrollPane = new ScrollPane(contentTable, style.scrollStyle);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setOverscroll(false, false);
        scrollPane.setScrollingDisabled(true, false);

        var cancel = new RdImageTextButton(cancelText, style.cancelStyle);
        var accept = new RdImageTextButton(acceptText, style.acceptStyle);

        accept.addListener(Objects.requireNonNullElseGet(onAccept, () -> new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                dialog.hide();
            }
        }));
        cancel.addListener(Objects.requireNonNullElseGet(onCancel, () -> new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                dialog.hide();
            }
        }));

        dialog.getContentTable().align(Align.topLeft);
        dialog.getContentTable().add(scrollPane).expand().fill();
        dialog.getButtonTable().add(accept).expandX().fillX();
        if (!cancelText.equals("")) {
            dialog.getButtonTable().add(cancel).expandX().fillX();
        }

        return dialog;
    }

    /** @see RdDialogBuilder#build(RdDialogBuilderStyle) */
    public RdDialog build(Skin skin) {
         return build(skin.get(RdDialogBuilderStyle.class));
    }

    /** @see RdDialogBuilder#build(RdDialogBuilderStyle) */
    public RdDialog build(Skin skin, String nameStyle) {
        return build(skin.get(nameStyle, RdDialogBuilderStyle.class));
    }

    public RdDialog build() {
        return build(RdAssetManager.current().getSkin());
    }

    public RdDialog build(String styleName) {
        return build(RdAssetManager.current().getSkin(), styleName);
    }

    public static class RdDialogBuilderStyle {
        public RdDialog.RdDialogStyle rdDialogStyle;
        public RdImageTextButton.RdImageTextButtonStyle acceptStyle, cancelStyle;
        public RdLabel.RdLabelStyle textStyle;
        public ScrollPane.ScrollPaneStyle scrollStyle;

        public RdDialogBuilderStyle() {}

        public RdDialogBuilderStyle(RdDialog.RdDialogStyle rdDialogStyle, RdImageTextButton.RdImageTextButtonStyle acceptStyle,
                                    RdImageTextButton.RdImageTextButtonStyle cancelStyle, RdLabel.RdLabelStyle textStyle, ScrollPane.ScrollPaneStyle scrollStyle) {
            this.rdDialogStyle = rdDialogStyle;
            this.acceptStyle = acceptStyle;
            this.cancelStyle = cancelStyle;
            this.textStyle = textStyle;
            this.scrollStyle = scrollStyle;
        }

        public RdDialogBuilderStyle(RdDialogBuilderStyle style) {
            rdDialogStyle = style.rdDialogStyle;
            acceptStyle = style.acceptStyle;
            cancelStyle = style.cancelStyle;
            textStyle = style.textStyle;
            scrollStyle = style.scrollStyle;
        }
    }
}
