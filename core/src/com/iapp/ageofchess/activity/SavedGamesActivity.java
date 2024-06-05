package com.iapp.ageofchess.activity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.controllers.SavedGamesController;
import com.iapp.ageofchess.graphics.SavedGameView;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.lib.ui.actors.*;
import com.iapp.lib.ui.screens.Activity;
import com.iapp.lib.util.OnChangeListener;
import com.iapp.lib.util.TransitionEffects;
import com.iapp.lib.util.WindowUtil;

public class SavedGamesActivity extends Activity {

    private final SavedGamesController controller;
    private RdImageTextButton back;
    private RdDialog clearDialog, mapNotFound;
    private Table scrollContent;
    private WindowGroup windowGroup;

    private Spinner spinner;

    public SavedGamesActivity() {
        this.controller = new SavedGamesController(this);
    }

    public void setSpinner(Spinner spinner) {
        this.spinner = spinner;
    }

    public void setMapNotFound(RdDialog mapNotFound) {
        this.mapNotFound = mapNotFound;
    }

    @Override
    public void initActors() {
        scrollContent = new Table();
        scrollContent.align(Align.topLeft);

        back = new RdImageTextButton(strings.get("[i18n]Back"), "red_screen");
        back.setImage("ib_back");
    }

    @Override
    public void initListeners() {
        ChessApplication.self().getLauncher().setOnFinish(controller::goToScenario);
        back.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.goToScenario();
            }
        });
        updateSavedGames();
    }

    @Override
    public void show(Stage stage, Activity last) {
        Image background = new Image(new TextureRegionDrawable(
            ChessAssetManager.current().findChessRegion("menu_background")));
        background.setFillParent(true);
        background.setScaling(Scaling.fill);
        getStage().addActor(background);

        if (ChessConstants.loggingAcc != null) {
            RdTable panel = new RdTable();
            panel.align(Align.topLeft);
            panel.setFillParent(true);
            getStage().addActor(panel);
            panel.add(ChessConstants.accountPanel)
                .expandX().fillX();
        }

        var content = new Table();
        content.setFillParent(true);

        var window = new RdWindow("","screen_window");
        window.setMovable(false);
        var properties = new PropertyTable(400);
        window.add(properties).expand().fill();

        properties.setVisibleBackground(false);
        properties.add(new PropertyTable.Title(strings.get("[i18n]Saved games")));

        var scroll = new ScrollPane(scrollContent);
        scroll.setScrollingDisabled(true, false);
        properties.getContent().add(scroll).expand().fill();

        windowGroup = new WindowGroup(window, back);
        ChessApplication.self().updateTitle(windowGroup, strings.get("[i18n]Single Player"));

        windowGroup.setFillParent(true);
        stage.addActor(windowGroup);
        windowGroup.update();

        TransitionEffects.transitionBottomShow(windowGroup, ChessConstants.localData.getScreenDuration());
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        windowGroup.update();
        WindowUtil.resizeCenter(mapNotFound);
        WindowUtil.resizeCenter(clearDialog);
        WindowUtil.resizeCenter(spinner);
    }

    public void updateSavedGames() {
        scrollContent.clear();
        for (var ref : ChessConstants.localData.getReferences()) {

            var onClear = new OnChangeListener() {
                @Override
                public void onChange(Actor actor) {
                    clearDialog = controller.showClearDialog(ref);
                    resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                }
            };

            OnChangeListener onPlay = new OnChangeListener() {
                @Override
                public void onChange(Actor actor) {
                    controller.lunchGame(ref);
                }
            };

            if (ref.getMatch() == null) continue;
            scrollContent.add(new SavedGameView(ref, onClear, onPlay))
                    .expandX().left().fillX().pad(10, 10, 10, 10).row();
        }
    }

    @Override
    public Actor hide(SequenceAction action, Activity next) {
        if (next instanceof GameActivity) {
            TransitionEffects.alphaHide(action, ChessConstants.localData.getScreenDuration());
            return getStage().getRoot();
        } else {
            TransitionEffects.transitionBottomHide(action, windowGroup, ChessConstants.localData.getScreenDuration());
            return windowGroup;
        }
    }
}
