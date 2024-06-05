package com.iapp.ageofchess.activity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.controllers.ScenariosController;
import com.iapp.ageofchess.graphics.MapScenariosView;
import com.iapp.lib.ui.widgets.ChatView;
import com.iapp.ageofchess.graphics.LevelView;
import com.iapp.ageofchess.graphics.ScenarioView;
import com.iapp.ageofchess.modding.GameMode;
import com.iapp.ageofchess.modding.MapData;
import com.iapp.lib.ui.actors.*;
import com.iapp.ageofchess.services.*;
import com.iapp.lib.ui.screens.Activity;
import com.iapp.lib.ui.screens.GrayAssetManager;
import com.iapp.lib.ui.screens.RdLogger;
import com.iapp.lib.util.OnChangeListener;
import com.iapp.lib.util.StreamUtil;
import com.iapp.lib.util.TransitionEffects;
import com.iapp.lib.util.WindowUtil;

import java.util.HashMap;
import java.util.List;

public class ScenariosActivity extends Activity {

    private final ScenariosController controller;
    private final Array<RdDialog> badScenarios = new Array<>();
    private RdImageTextButton back, savedGames;
    private WindowGroup windowGroup;
    private RdDialog scenarios;

    public ScenariosActivity() {
        this.controller = new ScenariosController(this);
    }

    @Override
    public void initActors() {
        if (ChessConstants.chatView != null) ChessConstants.chatView.updateMode(ChatView.Mode.LOBBY);
        back = new RdImageTextButton(strings.get("[i18n]Back"), "red_screen");
        back.setImage("ib_back");

        savedGames = new RdImageTextButton(strings.get("[i18n]Games"), "white_screen");
        savedGames.setImage("ib_games");
    }

    @Override
    public void initListeners() {
        ChessApplication.self().getLauncher().setOnFinish(controller::goToMenu);
        back.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.goToMenu();
            }
        });
        savedGames.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.goToSavedGames();
            }
        });
    }

    @Override
    public void show(Stage stage, Activity last) {
        ChessApplication.self().getLineContent().setVisible(true);
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

        var window = new RdWindow("","screen_window");
        window.setMovable(false);
        var properties = new PropertyTable(400);
        window.add(properties).expand().fill();

        properties.setVisibleBackground(false);
        properties.add(new PropertyTable.Title(strings.get("[i18n]Scene selection")));

        properties.getContent().add(getLevels()).height(250).fillX().pad(10, 10, 10, 10).row();
        addMapInfo(properties.getContent(), ChessAssetManager.current().getDataMaps());

        windowGroup = new WindowGroup(window, back, savedGames);
        windowGroup.setFillParent(true);
        ChessApplication.self().updateTitle(windowGroup, strings.get("[i18n]Single Player"));

        stage.addActor(windowGroup);
        windowGroup.update();

        if (last instanceof GameActivity) {
            TransitionEffects.alphaShow(getStage().getRoot(), ChessConstants.localData.getScreenDuration());
        } else {
            TransitionEffects.transitionBottomShow(windowGroup, ChessConstants.localData.getScreenDuration());
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        windowGroup.update();
        WindowUtil.resizeCenter(scenarios);
        StreamUtil.streamOf(badScenarios)
            .forEach(WindowUtil::resizeCenter);
    }

    private void addMapInfo(RdTable content, List<MapData> maps) {
        for (int i = 0; i < maps.size(); i++) {
            int finalI = i;
            content.add(new MapScenariosView(maps.get(i), new OnChangeListener() {
                @Override
                public void onChange(Actor actor) {
                    showScenarios(maps.get(finalI));
                }
            })).expandX().fillX().pad(10, 10, 10, 10).row();
        }
    }

    private void showScenarios(MapData mapData) {
        if (mapData.getScenarios().length == 1) {
            controller.goToCreation(mapData, 0);
            return;
        }

        scenarios = new RdDialog(strings.get("[i18n]Scenarios"), ChessAssetManager.current().getSkin());

        var content = new RdTable();
        content.align(Align.topLeft);
        var scrollPane = new RdScrollPane(content);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setOverscroll(false, false);

        for (int i = 0; i < mapData.getScenarios().length; i++) {
            int finalI = i;

            try {
                content.add(new ScenarioView(mapData, i, new OnChangeListener() {
                            @Override
                            public void onChange(Actor actor) {
                                scenarios.afterHide(Actions.run(() ->
                                    controller.goToCreation(mapData, finalI)));

                            }
                        }))
                        .expandX().fillX().left().padBottom(5).row();

            } catch (Throwable t) {
                Gdx.app.error("showScenario", RdLogger.self().getDescription(t));

                int index = badScenarios.size;
                var badScenario = new RdDialogBuilder()
                        .title(strings.get("[i18n]Error"))
                        .text(RdLogger.self().getDescription(t))
                        .accept(strings.get("[i18n]accept"), (dialog, s) -> {
                            badScenarios.get(index).hide();
                            badScenarios.removeValue(badScenarios.get(index), true);
                        })
                        .build("input");

                badScenarios.add(badScenario);
                badScenario.getIcon().setDrawable(new TextureRegionDrawable(
                        GrayAssetManager.current().findRegion("ib_error")));
                badScenario.getIcon().setScaling(Scaling.fit);
                badScenario.show(getStage());
                badScenario.setSize(800, 550);
                resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            }
        }

        if (content.getChildren().isEmpty()) return;
        scenarios.getContentTable().add(scrollPane).expand().fill();

        scenarios.getIcon().setDrawable(new TextureRegionDrawable(
                GrayAssetManager.current().findRegion("icon_conf")));
        scenarios.getIcon().setScaling(Scaling.fit);
        scenarios.show(getStage());
        scenarios.setSize(800, 600);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private ScrollPane getLevels() {
        var levelsContent = new RdTable();
        levelsContent.align(Align.bottomLeft);
        var levels = new RdScrollPane(levelsContent);
        levels.setOverscroll(false, false);
        levels.setFadeScrollBars(false);

        var buttons = new ButtonGroup<ImageButton>();
        var map = new HashMap<GameMode, ImageButton>();

        for (int i = 0; i < GameMode.values().length; i++) {
            if (GameMode.values()[i] == GameMode.MULTIPLAYER) continue;
            if (GameMode.values()[i] == null
                    || ChessConstants.localData.getBestResultByLevel().get(GameMode.values()[i]) == null) continue;

            int bestResult = ChessConstants.localData.getBestResultByLevel().get(GameMode.values()[i]);
            var levelView = new LevelView(SettingsUtil.defineGameMode(GameMode.values()[i]), bestResult < 100,
                    bestResult < 50, bestResult < 25, bestResult);
            levelsContent.add(levelView).size(450, 200).padLeft(10).expandY().center();
            buttons.add(levelView);
            map.put(GameMode.values()[i], levelView);
        }
        map.get(ChessConstants.localData.getGameMode()).setChecked(true);

        for (int i = 0; i < buttons.getButtons().size; i++) {
            int finalI = i;
            var listener = new OnChangeListener() {
                @Override
                public void onChange(Actor actor) {
                    ChessConstants.localData.setGameMode(GameMode.values()[finalI]);
                }
            };
            listener.setSoundEnable(false);
            buttons.getButtons().get(i).addListener(listener);
        }

        return levels;
    }

    @Override
    public Actor hide(SequenceAction action, Activity next) {
        TransitionEffects.transitionBottomHide(action, windowGroup, ChessConstants.localData.getScreenDuration());
        return windowGroup;
    }
}
