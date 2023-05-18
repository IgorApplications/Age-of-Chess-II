package com.iapp.lib.ui.actors;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.tommyettinger.textra.Font;
import com.iapp.lib.ui.screens.RdAssetManager;
import com.iapp.lib.util.OnChangeListener;

/** Displays a dialog, which is a window with a title, a content table, and a button table. Methods are provided to add a label to
 * the content table and buttons to the button table, but any widgets can be added. When a button is clicked,
 * {@link #result(Object)} is called and the dialog is removed from the stage.
 * @author Nathan Sweet
 * @author Igor Ivanov
 * */
public class RdDialog extends RdWindow {
    RdTable contentTable, buttonTable;
    private @Null Skin skin;
    ObjectMap<Actor, Object> values = new ObjectMap<>();
    boolean cancelHide;
    Actor previousKeyboardFocus, previousScrollFocus;
    FocusListener focusListener;

    /** RdDialog style */
    private RdDialogStyle rdDialogStyle;
    /** button to close the dialog */
    private @Null ImageButton closeBox;
    /** dialog icon */
    private Image icon = new Image();
    /** cell icon in the table */
    private RdCell<Image> iconRdCell;
    /** close box cell in table */
    private RdCell<ImageButton> closeRdCell;
    /** the single listener of the close box */
    private OnChangeListener onCancel = new OnChangeListener() {
        @Override
        public void onChange(Actor actor) {
            hide();
        }
    };
    /** true if the hide method was called */
    private boolean hidden;

    protected InputListener ignoreTouchDown = new InputListener() {
        public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
            event.cancel();
            return false;
        }
    };

    public RdDialog(String title, Skin skin) {
        super(title, skin.get(RdDialogStyle.class));
        setSkin(skin);
        this.skin = skin;
        rdDialogStyle = skin.get(RdDialogStyle.class);

        initialize();
    }

    public RdDialog(String title, Skin skin, String windowStyleName) {
        super(title, skin.get(windowStyleName, RdDialogStyle.class));
        setSkin(skin);
        this.skin = skin;
        rdDialogStyle = skin.get(windowStyleName, RdDialogStyle.class);

        initialize();
    }

    public RdDialog(String title, RdDialogStyle rdDialogStyle) {
        super(title, rdDialogStyle);
        this.rdDialogStyle = rdDialogStyle;

        initialize();
    }

    public RdDialog(String title) {
        this(title, RdAssetManager.current().getSkin());
    }

    public RdDialog(String title, String styleName) {
        this(title, RdAssetManager.current().getSkin().get(styleName, RdDialogStyle.class));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }

    /** returns true if the hide method was called */
    public boolean isHidden() {
        return hidden;
    }

    /** returns the button to close the dialog */
    public @Null ImageButton getCloseBox() {
        return closeBox;
    }

    /** returns the single listener of the close box */
    public OnChangeListener getOnCancel() {
        return onCancel;
    }

    /** returns cell icon in the table */
    public RdCell<Image> getIconCell() {
        return iconRdCell;
    }

    /** returns close box cell in table */
    public RdCell<ImageButton> getCloseCell() {
        return closeRdCell;
    }

    /**
     * sets the single listener of the close box.
     * Replaces the old listener.
     * */
    public void setOnCancel(OnChangeListener onCancel) {
        closeBox.removeListener(this.onCancel);
        this.onCancel = onCancel;
        closeBox.addListener(onCancel);
    }

    /** returns the dialog icon */
    public Image getIcon() {
        return icon;
    }

    /** Called to draw the background, before clipping is applied (if enabled). Default implementation draws the background
     * drawable. */
    @Override
    protected void drawBackground(Batch batch, float parentAlpha, float x, float y) {
        if (getBackground() instanceof TwoNinePath) {
            var bg = ((TwoNinePath) getBackground());

            bg.setSecondHeight(Math.max(
                    getButtonTable().getPrefHeight(), 10));
        }
        super.drawBackground(batch, parentAlpha, x, y);
    }

    @Override
    protected void drawLoading(RdTable loading, Batch batch) {
        if (!loading.isVisible()) return;
        loading.setPosition(getX() + rdDialogStyle.padLeftC,
                getY() + rdDialogStyle.padBottomB);
        loading.setSize(getWidth() - rdDialogStyle.padLeftC - rdDialogStyle.padRightC,
                getHeight() - rdDialogStyle.padBottomB - rdDialogStyle.padTopT - rdDialogStyle.padTopC);
        loading.act(Gdx.graphics.getDeltaTime());
        loading.draw(batch, getColor().a);
    }

    /**
     * With this method, when extending the class,
     * you can change the default behavior of the actor
     **/
    protected void initialize () {
        setModal(true);

        defaults().space(6);
        add(contentTable = new RdTable(skin)).expand().fill();
        row();
        add(buttonTable = new RdTable(skin)).fillX();

        contentTable.defaults().space(6);
        buttonTable.defaults().space(6);

        buttonTable.addListener(new ChangeListener() {
            public void changed (ChangeEvent event, Actor actor) {
                if (!values.containsKey(actor)) return;
                while (actor.getParent() != buttonTable)
                    actor = actor.getParent();
                result(values.get(actor));
                if (!cancelHide) hide();
                cancelHide = false;
            }
        });

        focusListener = new FocusListener() {
            public void keyboardFocusChanged (FocusEvent event, Actor actor, boolean focused) {
                if (!focused) focusChanged(event);
            }

            public void scrollFocusChanged (FocusEvent event, Actor actor, boolean focused) {
                if (!focused) focusChanged(event);
            }

            private void focusChanged (FocusEvent event) {
                Stage stage = getStage();
                if (isModal && stage != null && stage.getRoot().getChildren().size > 0
                        && stage.getRoot().getChildren().peek() == RdDialog.this) { // Dialog is top most actor.
                    Actor newFocusedActor = event.getRelatedActor();
                    if (newFocusedActor != null && !newFocusedActor.isDescendantOf(RdDialog.this)
                            && !(newFocusedActor.equals(previousKeyboardFocus) || newFocusedActor.equals(previousScrollFocus)))
                        event.cancel();
                }
            }
        };

        getTitleTable().clear();
        getTitleLabel().setColor(rdDialogStyle.titleFontColor);

        pad(rdDialogStyle.padTopT, 0, 0, 0);
        getTitleTable().pad(0, rdDialogStyle.padLeftT, rdDialogStyle.padBottomT, rdDialogStyle.padRightT);
        getButtonTable().pad(rdDialogStyle.padTopB, rdDialogStyle.padLeftB, rdDialogStyle.padBottomB, rdDialogStyle.padRightB);
        getContentTable().pad(rdDialogStyle.padTopC, rdDialogStyle.padLeftC, rdDialogStyle.padBottomC, rdDialogStyle.padRightC);

        if (rdDialogStyle.drawableIcon != null) {
            icon.setDrawable(rdDialogStyle.drawableIcon);
        }

        getTitleTable().add(icon)
                .pad(rdDialogStyle.padTopIcon, rdDialogStyle.padLeftIcon,
                        rdDialogStyle.padBottomIcon, rdDialogStyle.padRightIcon);

        getTitleTable().add(getTitleLabel()).expandX().fillX()
                .pad(rdDialogStyle.padTopTitle, rdDialogStyle.padLeftTitle,
                        rdDialogStyle.padBottomTitle, rdDialogStyle.padRightTitle);

        if (rdDialogStyle.closeBoxStyle != null) {
            closeBox = new ImageButton(rdDialogStyle.closeBoxStyle);
            getTitleTable().add(closeBox)
                    .pad(rdDialogStyle.padTopBox, rdDialogStyle.padLeftBox,
                            rdDialogStyle.padBottomBox, rdDialogStyle.padRightBox);
            closeBox.addListener(onCancel);
        }
    }

    protected void setStage (Stage stage) {
        if (stage == null)
            addListener(focusListener);
        else
            removeListener(focusListener);
        super.setStage(stage);
    }

    public RdTable getContentTable () {
        return contentTable;
    }

    public RdTable getButtonTable () {
        return buttonTable;
    }

    /** Adds a label to the content table. The dialog must have been constructed with a skin to use this method. */
    public RdDialog text (@Null String text) {
        if (skin == null)
            throw new IllegalStateException("This method may only be used if the dialog was constructed with a Skin.");
        return text(text, skin.get(LabelStyle.class));
    }

    /** Adds a label to the content table. */
    public RdDialog text (@Null String text, LabelStyle labelStyle) {
        return text(new Label(text, labelStyle));
    }

    /** Adds the given Label to the content table */
    public RdDialog text (Label label) {
        contentTable.add(label);
        return this;
    }

    /** Adds a text button to the button table. Null will be passed to {@link #result(Object)} if this button is clicked. The
     * dialog must have been constructed with a skin to use this method. */
    public RdDialog button (@Null String text) {
        return button(text, null);
    }

    /** Adds a text button to the button table. The dialog must have been constructed with a skin to use this method.
     * @param object The object that will be passed to {@link #result(Object)} if this button is clicked. May be null. */
    public RdDialog button (@Null String text, @Null Object object) {
        if (skin == null)
            throw new IllegalStateException("This method may only be used if the dialog was constructed with a Skin.");
        return button(text, object, skin.get(TextButtonStyle.class));
    }

    /** Adds a text button to the button table.
     * @param object The object that will be passed to {@link #result(Object)} if this button is clicked. May be null. */
    public RdDialog button (@Null String text, @Null Object object, TextButtonStyle buttonStyle) {
        return button(new TextButton(text, buttonStyle), object);
    }

    /** Adds the given button to the button table. */
    public RdDialog button (Button button) {
        return button(button, null);
    }

    /** Adds the given button to the button table.
     * @param object The object that will be passed to {@link #result(Object)} if this button is clicked. May be null. */
    public RdDialog button (Button button, @Null Object object) {
        buttonTable.add(button);
        setObject(button, object);
        return this;
    }

    /** {@link #pack() Packs} the dialog (but doesn't set the position), adds it to the stage, sets it as the keyboard and scroll
     * focus, clears any actions on the dialog, and adds the specified action to it. The previous keyboard and scroll focus are
     * remembered, so they can be restored when the dialog is hidden.
     * @param action May be null. */
    public RdDialog show (Stage stage, @Null Action action) {
        clearActions();
        removeCaptureListener(ignoreTouchDown);

        previousKeyboardFocus = null;
        Actor actor = stage.getKeyboardFocus();
        if (actor != null && !actor.isDescendantOf(this)) previousKeyboardFocus = actor;

        previousScrollFocus = null;
        actor = stage.getScrollFocus();
        if (actor != null && !actor.isDescendantOf(this)) previousScrollFocus = actor;


        pack();
        stage.cancelTouchFocus();
        stage.setKeyboardFocus(this);
        stage.setScrollFocus(this);
        if (action != null) addAction(action);
        stage.addActor(this);

        return this;
    }

    /** Centers the dialog in the stage and calls {@link #show(Stage, Action)} with a {@link Actions#fadeIn(float, Interpolation)}
     * action. */
    public RdDialog show (Stage stage) {
        show(stage, sequence(Actions.alpha(0), Actions.fadeIn(0.4f, Interpolation.fade)));
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2), Math.round((stage.getHeight() - getHeight()) / 2));
        return this;
    }

    /** Removes the dialog from the stage, restoring the previous keyboard and scroll focus, and adds the specified action to the
     * dialog.
     * @param action If null, the dialog is removed immediately. Otherwise, the dialog is removed when the action completes. The
     *           dialog will not respond to touch down events during the action. */
    public void hide (@Null Action action) {
        hidden = true;
        Stage stage = getStage();
        if (stage != null) {
            removeListener(focusListener);
            if (previousKeyboardFocus != null && previousKeyboardFocus.getStage() == null) previousKeyboardFocus = null;
            Actor actor = stage.getKeyboardFocus();
            if (actor == null || actor.isDescendantOf(this)) stage.setKeyboardFocus(previousKeyboardFocus);

            if (previousScrollFocus != null && previousScrollFocus.getStage() == null) previousScrollFocus = null;
            actor = stage.getScrollFocus();
            if (actor == null || actor.isDescendantOf(this)) stage.setScrollFocus(previousScrollFocus);
        }
        if (action != null) {
            addCaptureListener(ignoreTouchDown);
            addAction(
                    sequence(action,
                            Actions.removeListener(ignoreTouchDown, true),
                            Actions.removeActor())
            );
        } else
            remove();
    }

    /**
     * Hides the dialog. Called automatically when a close button is clicked.
     * The default implementation fades out the dialog over 400 milliseconds.
     * */
    public void hide () {
        hide(fadeOut(0.4f, Interpolation.fade));
    }

    /**
     *
     * Hides the dialog. Called automatically when a close button is clicked.
     * The default implementation fades out the dialog over 400 milliseconds.
     * @param action - additional action after closing the dialog box
     * */
    public void afterHide(Action action) {
        SequenceAction sequence = new SequenceAction();
        sequence.addAction(fadeOut(0.4f, Interpolation.fade));
        sequence.addAction(action);
        hide(sequence);
    }

    public void setObject (Actor actor, @Null Object object) {
        values.put(actor, object);
    }

    /** If this key is pressed, {@link #result(Object)} is called with the specified object.
     * @see Keys */
    public RdDialog key (final int keycode, final @Null Object object) {
        addListener(new InputListener() {
            public boolean keyDown (InputEvent event, int keycode2) {
                if (keycode == keycode2) {
                    // Delay a frame to eat the keyTyped event.
                    Gdx.app.postRunnable(() -> {
                        result(object);
                        if (!cancelHide) hide();
                        cancelHide = false;
                    });
                }
                return false;
            }
        });
        return this;
    }

    /** Called when a button is clicked. The dialog will be hidden after this method returns unless {@link #cancel()} is called.
     * @param object The object specified when the button was added. */
    protected void result (@Null Object object) {}

    public void cancel () {
        cancelHide = true;
    }

    public static class RdDialogStyle extends RdWindowStyle {

        public @Null ImageButton.ImageButtonStyle closeBoxStyle;
        public @Null Drawable drawableIcon;
        public int padBottomT = 0, padLeftT = 0, padRightT = 0, padTopT = 0;
        public int padBottomB = 0, padLeftB = 0, padRightB = 0, padTopB = 0;
        public int padBottomC = 0, padLeftC = 0, padRightC = 0, padTopC = 0;
        public int padBottomBox = 0, padLeftBox = 0, padRightBox = 0, padTopBox = 0;
        public int padBottomTitle = 0, padLeftTitle = 0, padRightTitle = 0, padTopTitle = 0;
        public int padBottomIcon = 0, padLeftIcon = 0, padRightIcon = 0, padTopIcon = 0;

        public RdDialogStyle() {
            super();
        }

        public RdDialogStyle(Font titleFont, Color titleFontColor, Drawable background,
                             ImageButton.ImageButtonStyle closeBoxStyle, Drawable drawableIcon) {
            super(titleFont, titleFontColor, background);
            this.closeBoxStyle = closeBoxStyle;
            this.drawableIcon = drawableIcon;
        }

        public RdDialogStyle(RdDialogStyle style) {
            super(style);
            closeBoxStyle = style.closeBoxStyle;
            drawableIcon = style.drawableIcon;
        }
    }
}
