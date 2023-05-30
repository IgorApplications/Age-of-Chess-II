package com.iapp.ageofchess.activity.multiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.activity.GameActivity;
import com.iapp.ageofchess.controllers.multiplayer.MultiplayerScenariosController;
import com.iapp.ageofchess.graphics.MapDataView;
import com.iapp.ageofchess.graphics.ScenarioView;
import com.iapp.ageofchess.modding.MapData;
import com.iapp.ageofchess.multiplayer.Match;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.lib.ui.actors.*;
import com.iapp.lib.ui.screens.Activity;
import com.iapp.lib.ui.screens.RdLogger;
import com.iapp.lib.util.*;

import java.util.List;
import java.util.function.BiConsumer;

public class MultiplayerScenariosActivity extends Activity {

    private Match match;
    private final MultiplayerScenariosController controller;
    private final Array<RdDialog> badScenarios = new Array<>();
    private RdImageTextButton back;
    private WindowGroup windowGroup;
    private RdDialog scenarios;
    private Spinner spinner;

    public MultiplayerScenariosActivity(Match match) {
        this.match = match;
        controller = new MultiplayerScenariosController(this, match);
    }

    public MultiplayerScenariosActivity() {
        controller = new MultiplayerScenariosController(this, null);
    }

    public void setSpinner(Spinner spinner) {
        this.spinner = spinner;
    }

    @Override
    public void initActors() {
        back = new RdImageTextButton(strings.get("back"), "red_screen");
        back.setImage("ib_back");
    }

    @Override
    public void initListeners() {
        back.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                if (match != null) {
                    controller.goToGames();
                } else {
                    controller.goToMenu();
                }
            }
        });
    }

    @Override
    public void show(Stage stage, Activity last) {
        if (last instanceof MultiplayerCreationActivity || last instanceof MultiplayerMenuActivity) {
            ChessApplication.self().getLauncher().setOnFinish(controller::goToMenu);
        } else {
            ChessApplication.self().getLauncher().setOnFinish(controller::goToGames);
        }

        Image background = new Image(new TextureRegionDrawable(
            ChessAssetManager.current().findChessRegion("menu_background")));
        background.setFillParent(true);
        getStage().addActor(background);
        background.setScaling(Scaling.fill);

        RdTable panel = new RdTable();
        panel.align(Align.topLeft);
        panel.setFillParent(true);
        getStage().addActor(panel);
        panel.add(ChessConstants.accountPanel)
            .expandX().fillX();

        var window = new RdWindow("","screen_window");
        window.setMovable(false);
        var properties = new PropertyTable(400, ChessAssetManager.current().getSkin());
        window.add(properties).expand().fill();

        properties.setVisibleBackground(false);

        properties.add(new PropertyTable.Title(strings.get("scenario_selection")));
        addMapInfo(properties.getContent(), ChessAssetManager.current().getDataMaps());

        windowGroup = new WindowGroup(window, back);
        ChessApplication.self().updateTitle(windowGroup, strings.get("multiplayer"));

        windowGroup.setFillParent(true);
        stage.addActor(windowGroup);
        windowGroup.update();

        if (last instanceof MultiplayerGameActivity || last instanceof GameActivity) {
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
        WindowUtil.resizeCenter(spinner);
    }

    private void addMapInfo(RdTable content, List<MapData> maps) {
        for (int i = 0; i < maps.size(); i++) {
            int finalI = i;

            content.add(new MapDataView(maps.get(i), new OnChangeListener() {
                @Override
                public void onChange(Actor actor) {
                    if (match != null) {
                        controller.goToGame(maps.get(finalI));
                    } else {
                        showScenarios(maps.get(finalI));
                    }
                }
            })).expandX().fillX().pad(10, 10, 10, 10).row();
        }
    }

    private void showScenarios(MapData mapData) {
        if (mapData.getScenarios().length == 1) {
            controller.goToCreation(mapData, 0);
            return;
        }

        scenarios = new RdDialog(strings.get("scenarios"), ChessAssetManager.current().getSkin());

        var content = new RdTable();
        content.align(Align.topLeft);
        var scrollPane = new RdScrollPane(content, ChessAssetManager.current().getSkin());
        scrollPane.setScrollingDisabled(true, false);

        for (int i = 0; i < mapData.getScenarios().length; i++) {
            int finalI = i;

            try {
                content.add(new ScenarioView(mapData, i, new OnChangeListener() {
                            @Override
                            public void onChange(Actor actor) {
                                scenarios.hide(Actions.run(() ->
                                    controller.goToCreation(mapData, finalI)));

                            }
                        }))
                        .expandX().fillX().left().padBottom(5).row();

            } catch (Throwable t) {
                Gdx.app.error("showScenario", RdLogger.self().getDescription(t));

                int index = badScenarios.size;
                var badScenario = new RdDialogBuilder()
                        .title(strings.get("error"))
                        .text(RdLogger.self().getDescription(t))
                        .accept(strings.get("accept"),
                            (dialog, s) -> {
                                badScenarios.get(index).hide();
                                badScenarios.removeValue(badScenarios.get(index), true);
                            })
                        .build(ChessAssetManager.current().getSkin(), "input");

                badScenarios.add(badScenario);
                badScenario.getIcon().setDrawable(new TextureRegionDrawable(
                        ChessAssetManager.current().findRegion("ib_error")));
                badScenario.getIcon().setScaling(Scaling.fit);
                badScenario.show(getStage());
                badScenario.setSize(800, 550);
                resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            }
        }

        if (content.getChildren().isEmpty()) return;
        scenarios.getContentTable().add(scrollPane).expand().fill();

        scenarios.getIcon().setDrawable(new TextureRegionDrawable(
                ChessAssetManager.current().findRegion("icon_conf")));
        scenarios.getIcon().setScaling(Scaling.fit);
        scenarios.show(getStage());
        scenarios.setSize(800, 600);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public Actor hide(SequenceAction action, Activity next) {
        if (next instanceof MultiplayerGameActivity) {
            TransitionEffects.alphaHide(action, ChessConstants.localData.getScreenDuration());
            return getStage().getRoot();
        }
        TransitionEffects.transitionBottomHide(action, windowGroup, ChessConstants.localData.getScreenDuration());
        return windowGroup;
    }
}
