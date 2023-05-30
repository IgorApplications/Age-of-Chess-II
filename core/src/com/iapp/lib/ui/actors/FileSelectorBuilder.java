package com.iapp.lib.ui.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import com.iapp.lib.ui.screens.RdAssetManager;
import com.iapp.lib.util.OnChangeListener;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * This class allows you to browse the file system
 * and select the desired files
 * @author Igor Ivanov
 * @version 1.0
 * */
public class FileSelectorBuilder {

    public FileSelectorBuilder() {}


    /** dialog box text data */
    protected String acceptText = "",
            cancelText = "",
            title = "";
    /** reject button listeners */
    protected OnChangeListener onCancel, onHide;
    /** select button listener */
    protected Consumer<FileHandle> onSelect;
    /** current file selector position */
    protected FileHandle parent;
    /** Searches only for files that end with the specified text */
    protected String[] endFilters = new String[]{};

    /**
     * set text and listener on select button
     * @param text - accept button text
     * @param listener - accept button listener
     * */
    public FileSelectorBuilder select(String text, Consumer<FileHandle> listener) {
        acceptText = text;
        onSelect = listener;
        return this;
    }

    /**
     * set text on select button
     * @param text - accept button text
     * */
    public FileSelectorBuilder select(String text) {
        return select(text, null);
    }

    /**
     * Sets the window close listener
     * @param onHide - window close listener
     * */
    public FileSelectorBuilder onHide(OnChangeListener onHide) {
        this.onHide = onHide;
        return this;
    }

    /**
     * set text and listener on cancel button
     * @param text - cancel button text
     * @param listener - cancel button listener
     * */
    public FileSelectorBuilder cancel(String text, OnChangeListener listener) {
        cancelText = text;
        onCancel = listener;
        return this;
    }

    /**
     * set text on cancel button
     * @param text - cancel button text
     * */
    public FileSelectorBuilder cancel(String text) {
        return cancel(text, null);
    }

    /** sets the title text of the dialog box */
    public FileSelectorBuilder title(String title) {
        this.title = title;
        return this;
    }

    /** sets the initial location in the file system */
    public FileSelectorBuilder parent(FileHandle parent) {
        this.parent = parent;
        return this;
    }

    /** Sets filters for file endings, does not affect directories */
    public FileSelectorBuilder endFilters(String... endFilters) {
        this.endFilters = endFilters;
        return this;
    }

    private FileHandle[] handles;
    private RdLabel pathParent;
    private RdList<String> list;

    public RdDialog build(FileSelectorStyle style) {
        var dialog = new RdDialog(title, style.dialogBuilderStyle.rdDialogStyle);
        if (onHide != null) dialog.setOnCancel(onHide);
        else {
            if (onCancel != null) {
                dialog.setOnCancel(onCancel);
            }
        }

        if (parent == null) parent = Gdx.files.absolute(Gdx.files.external("/").file().getAbsolutePath());
        handles = getFiles(parent);

        list = new RdList<>(style.rdListStyle);
        var scrollPane = new ScrollPane(list, style.dialogBuilderStyle.scrollStyle);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setOverscroll(false, false);
        scrollPane.setScrollingDisabled(true, false);

        updateItemIcons(style);
        list.setItems(getFileNames(handles));

        list.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (list.getSelectedIndex() == 0) parent = parent.parent();

                if (list.getSelectedIndex() != 0) parent = handles[list.getSelectedIndex() - 1];
                var res= parent.path().trim();
                pathParent.setText("[" + style.filePath.color.toString() + "] " + res);
                if (res.equals("")) pathParent.setText("/");
                if (!parent.isDirectory()) return;

                handles = getFiles(parent);
                updateItemIcons(style);
                list.setItems(getFileNames(handles));
            }
        });

        var cancel = new RdImageTextButton(cancelText, style.dialogBuilderStyle.cancelStyle);
        var accept = new RdImageTextButton(acceptText, style.dialogBuilderStyle.acceptStyle);

        pathParent = new RdLabel(" " + parent.path().trim(), style.filePath);
        pathParent.setWrap(true);

        accept.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                if (list.getSelectedIndex() == 0) return;
                if (onSelect != null) {
                    var handle = handles[list.getSelectedIndex() - 1];
                    if (handle.isDirectory()) return;
                    onSelect.accept(handle);
                }
            }
        });
        cancel.addListener(Objects.requireNonNullElseGet(onCancel, () -> new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                dialog.hide();
            }
        }));

        dialog.getContentTable().align(Align.topLeft);
        dialog.getContentTable().add(pathParent)
                .expandX().fillX().padRight(8).row();
        dialog.getContentTable().add(scrollPane).expand().fill();
        dialog.getButtonTable().add(accept).expandX().fillX();
        if (!cancelText.equals("")) {
            dialog.getButtonTable().add(cancel).expandX().fillX();
        }

        return dialog;
    }

    /** @see RdDialogBuilder#build(RdDialogBuilder.RdDialogBuilderStyle) */
    public RdDialog build(Skin skin) {
        return build(skin.get(FileSelectorStyle.class));
    }

    /** @see RdDialogBuilder#build(RdDialogBuilder.RdDialogBuilderStyle) */
    public RdDialog build(Skin skin, String nameStyle) {
        return build(skin.get(nameStyle, FileSelectorStyle.class));
    }

    public RdDialog build() {
        return build(RdAssetManager.current().getSkin());
    }

    public RdDialog build(String styleName) {
        return build(RdAssetManager.current().getSkin(), styleName);
    }

    public static class FileSelectorStyle {

        public RdDialogBuilder.RdDialogBuilderStyle dialogBuilderStyle;
        public RdList.RdListStyle rdListStyle;
        public @Null Drawable folder, file, back;
        public RdLabel.RdLabelStyle filePath;

        public FileSelectorStyle() {
            super();
        }

        public FileSelectorStyle(RdDialogBuilder.RdDialogBuilderStyle dialogBuilderStyle, RdList.RdListStyle rdListStyle,
                                 Drawable folder, Drawable file, RdLabel.RdLabelStyle filePath) {
            this.dialogBuilderStyle = dialogBuilderStyle;
            this.rdListStyle = rdListStyle;
            this.folder = folder;
            this.file = file;
            this.filePath = filePath;
        }

        public FileSelectorStyle(FileSelectorStyle style) {
            dialogBuilderStyle = style.dialogBuilderStyle;
            rdListStyle = style.rdListStyle;
            folder = style.folder;
            file = style.file;
            filePath = style.filePath;
        }
    }

    private FileHandle[] getFiles(FileHandle parent) {
        var handleList = new ArrayList<FileHandle>();

        if (endFilters.length == 0) return parent.list();

        for (var file : parent.list()) {
            for (var filter : endFilters) {
                if (file.isDirectory() || file.name().endsWith(filter)) {
                    handleList.add(file);
                    break;
                }
            }
        }

        return handleList.toArray(new FileHandle[0]);
    }

    private String[] getFileNames(FileHandle[] files) {
        var names = new String[files.length + 1];
        names[0] = " ";

        for (int i = 1; i < files.length + 1; i++) {
            names[i] = " " + getNormalStr(files[i - 1].name(), 20);
        }

        return names;
    }

    private String getNormalStr(String str, int maxLength) {
        if (str.length() > maxLength) return str.substring(0, maxLength);
        return str;
    }

    private void updateItemIcons(FileSelectorStyle style) {
        var icons = new Drawable[handles.length + 1];
        icons[0] = style.back;

        for (int i = 0; i < handles.length; i++) {
            if (handles[i].isDirectory()) {
                icons[i + 1] = style.folder;
            } else {
                icons[i + 1] = style.file;
            }
        }

        list.setIcons(icons);
    }
}
