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
import com.iapp.ageofchess.controllers.multiplayer.MultiplayerMenuController;
import com.iapp.ageofchess.controllers.multiplayer.RankController;
import com.iapp.ageofchess.graphics.RankView;
import com.iapp.ageofchess.multiplayer.MultiplayerEngine;
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
    private final MultiplayerMenuController menuController;
    private RdWindow window;
    private WindowGroup windowGroup;
    private RdTable topTable;
    private RdImageTextButton back;
    private final Map<RankType, List<RankView>> ratings = new HashMap<>();
    private RdLabel rankTitle;

    public RankActivity() {
        controller = new RankController(this);
        menuController = new MultiplayerMenuController(null);
    }

    @Override
    public void initActors() {
        back = new RdImageTextButton(strings.get("back"), "red_screen");
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
        panel.add(ChessApplication.self().getAccountPanel())
            .expandX().fillX();

        window = new RdWindow("","screen_window");
        window.setMovable(false);
        stage.addActor(window);

        var properties = new PropertyTable(400, ChessAssetManager.current().getSkin());
        window.add(properties).expand().fill();
        properties.setVisibleBackground(false);
        properties.add(new PropertyTable.Title(strings.get("rating")));

        rankTitle = new RdLabel(strings.format("leaderboard", strings.get("bullet")));
        rankTitle.setColor(Color.GOLD);

        RdSelectBox<String> rankType = new RdSelectBox<>();
        rankType.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                if (rankType.getSelectedIndex() == 0) {
                    updateTopTable(RankType.BULLET);
                    rankTitle.setText(strings.format("leaderboard", strings.get("bullet")));
                } else if (rankType.getSelectedIndex() == 1) {
                    updateTopTable(RankType.BLITZ);
                    rankTitle.setText(strings.format("leaderboard", strings.get("blitz")));
                } else if (rankType.getSelectedIndex() == 2) {
                    updateTopTable(RankType.RAPID);
                    rankTitle.setText(strings.format("leaderboard", strings.get("rapid")));
                } else {
                    updateTopTable(RankType.LONG);
                    rankTitle.setText(strings.format("leaderboard", strings.get("long")));
                }
            }
        });
        rankType.setItems(strings.get("bullet"), strings.get("blitz"), strings.get("rapid"), strings.get("long"));
        topTable = new RdTable("loading");
        topTable.align(Align.topLeft);
        var scroll = new RdScrollPane(topTable);

        properties.getContent().add(rankTitle).expandX();
        properties.getContent().add(rankType).padTop(8).padRight(8).minWidth(400).row();
        properties.getContent().add(scroll).expand().fill().colspan(2).row();

        windowGroup = new WindowGroup(window, back);
        ChessApplication.self().updateTitle(windowGroup, strings.get("multiplayer"));

        windowGroup.setFillParent(true);
        stage.addActor(windowGroup);
        windowGroup.update();

        window.getLoading().setVisible(true);
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

        ratings.put(RankType.BULLET, bullet);
        ratings.put(RankType.BLITZ, blitz);
        ratings.put(RankType.RAPID, rapid);
        ratings.put(RankType.LONG, longRank);

        MultiplayerEngine.self().getBulletTop(accounts -> {
            for (int i = 0; i < accounts.size(); i++) {
                bullet.add(new RankView(i + 1, accounts.get(i), RankType.BULLET, menuController));
            }

            if (ratings.size() == 4) updateTopTable(RankType.BULLET);
        });

        MultiplayerEngine.self().getBlitzTop(accounts -> {
            for (int i = 0; i < accounts.size(); i++) {
                blitz.add(new RankView(i + 1, accounts.get(i), RankType.BLITZ, menuController));
            }

            if (ratings.size() == 4) updateTopTable(RankType.BULLET);
        });

        MultiplayerEngine.self().getRapidTop(accounts -> {
            for (int i = 0; i < accounts.size(); i++) {
                rapid.add(new RankView(i + 1, accounts.get(i), RankType.RAPID, menuController));
            }

            if (ratings.size() == 4) updateTopTable(RankType.BULLET);
        });

        MultiplayerEngine.self().getLongTop(accounts -> {
            for (int i = 0; i < accounts.size(); i++) {
                longRank.add(new RankView(i + 1, accounts.get(i), RankType.LONG, menuController));
            }

            if (ratings.size() == 4) updateTopTable(RankType.BULLET);
        });
    }

    @Override
    public Actor hide(SequenceAction action, Activity next) {
        TransitionEffects.transitionBottomHide(action, windowGroup, ChessConstants.localData.getScreenDuration());
        return windowGroup;
    }
}
