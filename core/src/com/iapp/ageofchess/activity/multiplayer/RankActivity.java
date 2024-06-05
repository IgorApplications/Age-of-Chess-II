package com.iapp.ageofchess.activity.multiplayer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.controllers.multiplayer.RankController;
import com.iapp.ageofchess.graphics.RankView;
import com.iapp.ageofchess.multiplayer.MultiplayerEngine;
import com.iapp.lib.util.TasksLoader;
import com.iapp.lib.web.RankType;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.lib.ui.actors.*;
import com.iapp.lib.ui.screens.Activity;
import com.iapp.lib.ui.screens.RdApplication;
import com.iapp.lib.util.OnChangeListener;
import com.iapp.lib.util.TransitionEffects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RankActivity extends Activity {

    private final RankController controller;
    private RdWindow window;
    private WindowGroup windowGroup;
    private RdTable topTable;
    private RdImageTextButton back;
    private final Map<RankType, List<RankView>> ratings = new HashMap<>();
    private RdLabel rankTitle;

    public RankActivity() {
        controller = new RankController(this);
    }

    @Override
    public void initActors() {
        back = new RdImageTextButton(strings.get("[i18n]Back"), "red_screen");
        back.setImage("ib_back");
    }

    @Override
    public void initListeners() {
        RdApplication.self().getLauncher().setOnFinish(controller::goToMultiplayerMenuActivity);

        back.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.goToMultiplayerMenuActivity();
            }
        });
    }

    @Override
    public void show(Stage stage, Activity last) {
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

        window = new RdWindow("","screen_window");
        window.setMovable(false);
        stage.addActor(window);

        var properties = new PropertyTable(400);
        window.add(properties).expand().fill();
        properties.setVisibleBackground(false);
        properties.add(new PropertyTable.Title(strings.get("[i18n]Rating")));

        String leaderboardKey = "[i18n]Leaderboard \"{0}\"";
        rankTitle = new RdLabel(strings.format(leaderboardKey, strings.get("bullet")));
        rankTitle.setColor(Color.GOLD);

        RdSelectBox<String> rankType = new RdSelectBox<>();
        rankType.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                if (rankType.getSelectedIndex() == 0) {
                    updateTopTable(RankType.BULLET);
                    rankTitle.setText(strings.format(leaderboardKey, strings.get("[i18n]Bullet")));
                } else if (rankType.getSelectedIndex() == 1) {
                    updateTopTable(RankType.BLITZ);
                    rankTitle.setText(strings.format(leaderboardKey, strings.get("[i18n]Blitz")));
                } else if (rankType.getSelectedIndex() == 2) {
                    updateTopTable(RankType.RAPID);
                    rankTitle.setText(strings.format(leaderboardKey, strings.get("[i18n]Rapid")));
                } else {
                    updateTopTable(RankType.LONG);
                    rankTitle.setText(strings.format(leaderboardKey, strings.get("[i18n]Long")));
                }
            }
        });
        rankType.setItems(strings.get("[i18n]Bullet"), strings.get("[i18n]Blitz"),
            strings.get("[i18n]Rapid"), strings.get("[i18n]Long"));
        topTable = new RdTable("loading");
        topTable.align(Align.topLeft);
        var scroll = new RdScrollPane(topTable);

        properties.getContent().add(rankTitle).expandX();
        properties.getContent().add(rankType).padTop(8).padRight(8).minWidth(400).row();
        properties.getContent().add(scroll).expand().fill().colspan(2).row();

        windowGroup = new WindowGroup(window, back);
        ChessApplication.self().updateTitle(windowGroup, strings.get("[i18n]Multiplayer"));

        windowGroup.setFillParent(true);
        stage.addActor(windowGroup);
        windowGroup.update();

        window.getLoading().setVisible(true);
        window.getLoading().setLoadingText("[i18n]loading...");
        requireTops();

        if (last instanceof MultiplayerGameActivity) {
            TransitionEffects.alphaShow(getStage().getRoot(), ChessConstants.localData.getScreenDuration());
        } else {
            TransitionEffects.transitionBottomShow(windowGroup, ChessConstants.localData.getScreenDuration());
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        for (List<RankView> list : ratings.values()) {
            for (RankView rankView : list) {
                rankView.dispose();
            }
        }
    }

    private void updateTopTable(RankType rankType) {
        if (topTable == null) return;
        topTable.clear();

        for (RankView rankView : ratings.get(rankType)) {
            topTable.add(rankView).expandX().fillX().left()
                .pad(8, 8, 8, 8).row();
        }
        window.getLoading().setVisible(false);
    }

    private void requireTops() {
        List<RankView> bullet = new ArrayList<>();
        List<RankView> blitz = new ArrayList<>();
        List<RankView> rapid = new ArrayList<>();
        List<RankView> longRank = new ArrayList<>();
        TasksLoader loader = new TasksLoader();

        MultiplayerEngine.self().getBulletTop(accounts -> {

            for (int i = 1; i <= (accounts.size() + 2) / 3; i++) {
                int finalI = i;
                Runnable task = () -> {
                    for (int j = (finalI - 1) * 3; j < Math.min(accounts.size(), finalI * 3); j++) {
                        bullet.add(new RankView(j + 1, accounts.get(j), RankType.BULLET));
                    }
                };
                loader.addTask(task);
            }

            loader.addTask(() -> {
                ratings.put(RankType.BULLET, bullet);
                if (ratings.size() == 4) {
                    updateTopTable(RankType.BULLET);
                    loader.stop();
                }
            });

        });

        MultiplayerEngine.self().getBlitzTop(accounts -> {

            for (int i = 1; i <= (accounts.size() + 2) / 3; i++) {
                int finalI = i;
                Runnable task = () -> {
                    for (int j = (finalI - 1) * 3; j < Math.min(accounts.size(), finalI * 3); j++) {
                        blitz.add(new RankView(j + 1, accounts.get(j), RankType.BLITZ));
                    }
                };
                loader.addTask(task);
            }

            loader.addTask(() -> {
                ratings.put(RankType.BLITZ, blitz);
                if (ratings.size() == 4) {
                    updateTopTable(RankType.BULLET);
                    loader.stop();
                }
            });

        });

        MultiplayerEngine.self().getRapidTop(accounts -> {

            for (int i = 1; i <= (accounts.size() + 2) / 3; i++) {
                int finalI = i;
                Runnable task = () -> {
                    for (int j = (finalI - 1) * 3; j < Math.min(accounts.size(), finalI * 3); j++) {
                        rapid.add(new RankView(j + 1, accounts.get(j), RankType.RAPID));
                    }
                };
                loader.addTask(task);
            }

            loader.addTask(() -> {
                ratings.put(RankType.RAPID, rapid);
                if (ratings.size() == 4) {
                    updateTopTable(RankType.BULLET);
                    loader.stop();
                }
            });

        });

        MultiplayerEngine.self().getLongTop(accounts -> {

            for (int i = 1; i <= (accounts.size() + 2) / 3; i++) {
                int finalI = i;
                Runnable task = () -> {
                    for (int j = (finalI - 1) * 3; j < Math.min(accounts.size(), finalI * 3); j++) {
                        longRank.add(new RankView(j + 1, accounts.get(j), RankType.LONG));
                    }
                };
                loader.addTask(task);
            }

            loader.addTask(() -> {
                ratings.put(RankType.LONG, longRank);
                if (ratings.size() == 4) {
                    updateTopTable(RankType.BULLET);
                    loader.stop();
                }
            });

        });

        loader.load();
    }

    @Override
    public Actor hide(SequenceAction action, Activity next) {
        TransitionEffects.transitionBottomHide(action, windowGroup, ChessConstants.localData.getScreenDuration());
        return windowGroup;
    }
}
