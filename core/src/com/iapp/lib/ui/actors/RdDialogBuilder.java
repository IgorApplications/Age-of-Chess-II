package com.iapp.lib.ui.actors;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.iapp.lib.ui.screens.RdAssetManager;
import com.iapp.lib.util.OnChangeListener;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
    protected BiConsumer<RdDialog, String> onCancel, onAccept, onHide;
    /** true if the dialog is for input */
    protected boolean input;
    /** hint for input field */
    protected String message;

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
    public RdDialogBuilder accept(String text, BiConsumer<RdDialog, String> listener) {
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
    public RdDialogBuilder onHide(BiConsumer<RdDialog, String> onHide) {
        this.onHide = onHide;
        return this;
    }

    /**
     * set text and listener on cancel button
     * @param text - cancel button text
     * @param listener - cancel button listener
     * */
    public RdDialogBuilder cancel(String text, BiConsumer<RdDialog, String> listener) {
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
     * adds an input field to the dialog box after the text
     * @param message - hint for input field
     * */
    public RdDialogBuilder input(String message) {
        input = true;
        this.message = message;
        return this;
    }

    /**
     * Collect result dialog box from received data
     * @param style - the style of the dialog box and its actors
     * @return result dialog
     * */
    public RdDialog build(RdDialogBuilderStyle style) {
        var dialog = new RdDialog(title, style.rdDialogStyle);
        if (onHide != null) {
            dialog.setOnCancel(new OnChangeListener() {
                @Override
                public void onChange(Actor actor) {
                    onHide.accept(dialog, "");
                }
            });
        }
        else {
            if (onCancel != null) {
                dialog.setOnCancel(new OnChangeListener() {
                    @Override
                    public void onChange(Actor actor) {
                        onCancel.accept(dialog, "");
                    }
                });
            }
        }

        RdLabel contentLabel = new RdLabel(contentText, style.textStyle);
        contentLabel.setWrap(true);
        contentLabel.setAlignment(Align.topLeft);

        RdTextArea area = new RdTextArea("", style.textFieldStyle);
        area.setMaxLines(3);
        area.setPrefLines(3);
        area.setMessageText(message);
        RdTable contentTable = new RdTable();
        contentTable.align(Align.topLeft);
        if (input) {
            contentTable.add(contentLabel).expandX().fillX().row();
            contentTable.add(area).expandX().fillX();
        } else {
            contentTable.add(contentLabel).expand().fill();
        }

        ScrollPane scrollPane = new ScrollPane(contentTable, style.scrollStyle);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setOverscroll(false, false);
        scrollPane.setScrollingDisabled(true, false);

        RdImageTextButton cancel = new RdImageTextButton(cancelText, style.cancelStyle);
        RdImageTextButton accept = new RdImageTextButton(acceptText, style.acceptStyle);

        accept.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                if (onAccept != null) onAccept.accept(dialog, area.getText());
                else dialog.hide();
            }
        });
        cancel.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                if (onCancel != null) onCancel.accept(dialog, "");
                else dialog.hide();
            }
        });


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
        public RdTextField.RdTextFieldStyle textFieldStyle;

        public RdDialogBuilderStyle() {}

        public RdDialogBuilderStyle(RdDialog.RdDialogStyle rdDialogStyle, RdImageTextButton.RdImageTextButtonStyle acceptStyle,
                                    RdImageTextButton.RdImageTextButtonStyle cancelStyle, RdLabel.RdLabelStyle textStyle,
                                    ScrollPane.ScrollPaneStyle scrollStyle, RdTextField.RdTextFieldStyle textFieldStyle) {
            this.rdDialogStyle = rdDialogStyle;
            this.acceptStyle = acceptStyle;
            this.cancelStyle = cancelStyle;
            this.textStyle = textStyle;
            this.scrollStyle = scrollStyle;
            this.textFieldStyle = textFieldStyle;
        }

        public RdDialogBuilderStyle(RdDialogBuilderStyle style) {
            rdDialogStyle = style.rdDialogStyle;
            acceptStyle = style.acceptStyle;
            cancelStyle = style.cancelStyle;
            textStyle = style.textStyle;
            scrollStyle = style.scrollStyle;
            textFieldStyle = style.textFieldStyle;
        }
    }
}
