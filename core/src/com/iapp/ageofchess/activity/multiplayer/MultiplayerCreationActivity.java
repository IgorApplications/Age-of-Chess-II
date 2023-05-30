package com.iapp.ageofchess.activity.multiplayer;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.activity.CreationActivity;
import com.iapp.ageofchess.controllers.multiplayer.MultiplayerCreationController;
import com.iapp.ageofchess.modding.GameMode;
import com.iapp.ageofchess.modding.LocalMatch;
import com.iapp.ageofchess.modding.MapData;
import com.iapp.ageofchess.multiplayer.MultiplayerEngine;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.ageofchess.services.SettingsUtil;
import com.iapp.lib.chess_engine.Color;
import com.iapp.lib.ui.actors.*;
import com.iapp.lib.ui.screens.Activity;
import com.iapp.lib.util.OnChangeListener;
import com.iapp.lib.util.TransitionEffects;
import com.iapp.lib.util.WindowUtil;
import com.iapp.lib.web.RankType;
import com.iapp.lib.web.RequestStatus;

import java.util.function.Consumer;

public class MultiplayerCreationActivity extends Activity {

    private final MultiplayerCreationController controller;
    private final MapData mapData;
    private final int scenario;
    private String infinity;

    private RdImageTextButton back;
    private RdWindow window;
    private PropertyTable properties;
    private RdTextArea name;
    private RdSelectBox<String> timeByGame;
    private RdSelectBox<String> maxTurns;
    private RdSelectBox<String> timeByTurn;
    private RdSelectBox<String> rankType;
    private RdCheckBox random;
    private RdSelectBox<String> sponsored;
    private RdSelectBox<String> turnMode;
    private RdTextButton create;
    private WindowGroup windowGroup;
    private Spinner spinner;

    private static final long[] bulletGame = {60_000, 90_000, 120_000, 150_000, 180_000};

    private static final long[] blitzGame = {240_000, 300_000, 360_000, 420_000, 480_000, 540_000, 600_000};
    private static final long[] blitzTurn = {30_000, 45_000, 60_000, 90_000, 120_000};

    private static final long[] rapidGame = {900_000, 1_200_000, 1_800_000, 2_400_000, 3_000_000, 3_600_000};
    private static final long[] rapidTurn = {60_000, 90_000, 120_000, 150_000, 180_000};

    private static final long[] longTurn = {28_000_000, 43_200_000, 86_400_000, 129_600_000, 172_800_000};

    private static final long[] unrankedGame = {-1, 300_000, 600_000, 1_200_000,
            1_800_000, 2_400_000, 3_000_000, 3_600_000, 7_200_000};
    private static final long[] unrankedTurn = {-1, 60_000, 120_000, 180_000, 240_000, 300_000};

    public MultiplayerCreationActivity(MapData mapData, int scenario) {
        this.controller = new MultiplayerCreationController(this);
        this.mapData = mapData;
        this.scenario = scenario;
    }

    public void setSpinner(Spinner spinner) {
        this.spinner = spinner;
    }

    @Override
    public void initActors() {
        var content = new RdTable();
        content.setFillParent(true);
        window = new RdWindow("",  "screen_window");
        window.setMovable(false);
        properties = new PropertyTable(400, ChessAssetManager.current().getSkin());
        window.add(properties).expand().fill();

        back = new RdImageTextButton(strings.get("back"), "red_screen");
        back.setImage("ib_back");

        name = new RdTextArea("", ChessAssetManager.current().getSkin());
        name.setMaxLength(15);

        infinity = strings.get("infinity");
        timeByTurn = new RdSelectBox<>(ChessAssetManager.current().getSkin());

        maxTurns = new RdSelectBox<>(ChessAssetManager.current().getSkin());
        maxTurns.setItems(
                infinity,
                strings.format("turns", 5), strings.format("turns", 10),
                strings.format("turns", 15), strings.format("turns", 20),
                strings.format("turns", 25), strings.format("turns", 30),
                strings.format("turns", 35), strings.format("turns", 40),
                strings.format("turns", 45), strings.format("turns", 50),
                strings.format("turns", 55), strings.format("turns", 60),
                strings.format("turns", 65), strings.format("turns", 70),
                strings.format("turns", 75), strings.format("turns", 80),
                strings.format("turns", 85), strings.format("turns", 90),
                strings.format("turns", 95), strings.format("turns", 100));
        timeByGame = new RdSelectBox<>(ChessAssetManager.current().getSkin());

        create = new RdTextButton(strings.get("create"), "blue");

        rankType = new RdSelectBox<>();
        rankType.setItems(strings.get("bullet"), strings.get("blitz"),
                strings.get("rapid"), strings.get("non_ranked"));
        // strings.get("long"),

        random = new RdCheckBox("check_box");

        sponsored = new RdSelectBox<>();
        sponsored.setItems("0", "5", "10", "25", "50", "100", "250", "500", "1000", "5000", "10000");

        turnMode = new RdSelectBox<>();

        loadSettings();
    }

    @Override
    public void initListeners() {
        back.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.goToScenario();
            }
        });

        rankType.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                update();
            }
        });

        timeByTurn.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {}
        });

        maxTurns.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {}
        });

        timeByGame.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {}
        });

        sponsored.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {}
        });

        turnMode.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {}
        });

        random.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {}
        });

        create.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                launchGame();
            }
        });
    }

    @Override
    public void show(Stage stage, Activity last) {
        ChessApplication.self().getLauncher().setOnFinish(controller::goToScenario);

        Image background = new Image(new TextureRegionDrawable(
            ChessAssetManager.current().findChessRegion("menu_background")));
        background.setFillParent(true);
        background.setScaling(Scaling.fill);
        getStage().addActor(background);

        windowGroup = new WindowGroup(window, back);
        ChessApplication.self().updateTitle(windowGroup, strings.get("multiplayer"));

        windowGroup.setFillParent(true);
        stage.addActor(windowGroup);
        windowGroup.update();

        var turnModeHint = new RdImageTextButton("", "circle");
        turnModeHint.getLabelCell().reset();
        turnModeHint.setImage("ib_question");
        var turnModeTool = new RdTextTooltip(strings.get("turn_mode_hint"));
        turnModeHint.addListener(turnModeTool);

        update();
        properties.add(new PropertyTable.Title(strings.get("game_creation")));

        properties.add(new PropertyTable.Element(strings.get("game_name"), name));
        properties.add(new PropertyTable.Element(strings.get("rank_type"), rankType));
        properties.add(new PropertyTable.Element(strings.get("random_color"), random));
        properties.add(new PropertyTable.Element(strings.get("game_time"), timeByGame));
        properties.add(new PropertyTable.Element(strings.get("time_turn"), timeByTurn));
        properties.add(new PropertyTable.Element(strings.get("turn_mode"), turnModeHint, turnMode));
        properties.add(new PropertyTable.Element(strings.get("sponsored_coins"), sponsored));
        properties.add(new PropertyTable.Element(strings.get("max_turns"), maxTurns));

        properties.add(new PropertyTable.Element("", create));

        TransitionEffects.transitionBottomShow(windowGroup, ChessConstants.localData.getScreenDuration());
    }

    private void update() {
        var type = SettingsUtil.getRankType(rankType.getSelected());

        if (type == RankType.BULLET) {

            timeByTurn.setItems();

            timeByGame.setItems(
                    strings.format("minutes", 1),
                    strings.format("minutes", 1.5),
                    strings.format("minutes", 2),
                    strings.format("minutes", 2.5),
                    strings.format("minutes", 3));

            turnMode.setItems(strings.get("concurrent_fast"));

        } else if (type == RankType.BLITZ) {

            timeByTurn.setItems(
                    strings.format("sec_by_move", 30), strings.format("sec_by_move", 45),
                    strings.format("min_by_move", 1), strings.format("min_by_move", 1.5),
                    strings.format("min_by_move", 2));

            timeByGame.setItems(
                    strings.format("minutes", 4),
                    strings.format("minutes", 5),
                    strings.format("minutes", 6),
                    strings.format("minutes", 7),
                    strings.format("minutes", 8),
                    strings.format("minutes", 9),
                    strings.format("minutes", 10));

            turnMode.setItems(strings.get("concurrent_fast"));

        } else if (type == RankType.RAPID) {

            timeByTurn.setItems(
                    strings.format("min_by_move", 1), strings.format("min_by_move", 1.5),
                    strings.format("min_by_move", 2), strings.format("min_by_move", 2.5),
                    strings.format("min_by_move", 3));

            timeByGame.setItems(
                    strings.format("minutes", 15),
                    strings.format("minutes", 20),
                    strings.format("minutes", 30),
                    strings.format("minutes", 40),
                    strings.format("minutes", 50),
                    strings.format("hours", 1));

            turnMode.setItems(strings.get("concurrent_fast"), strings.get("concurrent"));

        } else if (type == RankType.LONG) {

            timeByTurn.setItems(
                    strings.format("hours_by_move", 8), strings.format("hours_by_move", 12),
                    strings.format("hours_by_move", 24), strings.format("hours_by_move", 36),
                    strings.format("hours_by_move", 48));

            timeByGame.setItems();
            turnMode.setItems(strings.get("concurrent_fast"), strings.get("concurrent"));

        } else {

            timeByTurn.setItems(
                    infinity,
                    strings.format("min_by_move", 1), strings.format("min_by_move", 2),
                    strings.format("min_by_move", 3), strings.format("min_by_move", 4),
                    strings.format("min_by_move", 5));

            timeByGame.setItems(
                    infinity,
                    strings.format("minutes", 5),
                    strings.format("minutes", 10),
                    strings.format("minutes", 20),
                    strings.format("minutes", 30),
                    strings.format("minutes", 40),
                    strings.format("minutes", 50),
                    strings.format("hours", 1),
                    strings.format("hours", 2));

            turnMode.setItems(strings.get("concurrent_fast"), strings.get("concurrent"));
        }


    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        windowGroup.update();
        WindowUtil.resizeCenter(spinner);
    }

    private void launchGame() {
        var turns = SettingsUtil.defineMaxTurns(maxTurns.getSelected());
        var rank = SettingsUtil.getRankType(rankType.getSelected());
        var timeTurn = getTimeByTurn(rank);
        var timePlayer = getTimeByPlayer(rank);
        var mode = SettingsUtil.defineTurnMode(turnMode.getSelected());
        var coins = Long.parseLong(sponsored.getSelected());

        var builder = new LocalMatch.GameBuilder(name.getText(), Color.BLACK, mapData)
                .flippedPieces(ChessConstants.localData.isFlippedPieces())
                .matchInfo(ChessConstants.localData.isMatchDescription())
                .turnMode(mode)
                .timeByGame(timePlayer)
                .timeByTurn(timeTurn)
                .maxTurns(turns.getValue())
                .infiniteTurns(turns.getKey())
                .gameMode(GameMode.MULTIPLAYER)
                .numberScenario(scenario)
                .rankType(rank)
                .randomColor(random.isChecked())
                .sponsored(coins);

        var localMatch = new LocalMatch(CreationActivity.generateMatchId(), builder);
        controller.launchGame(localMatch);
    }

    @Override
    public void loadSettings() {
        name.setText(ChessConstants.localData.getGameName());
        maxTurns.setSelected(String.valueOf(ChessConstants.localData.getMaxTurns()));
    }

    private long getTimeByTurn(RankType rankType) {
        if (rankType == RankType.BULLET) {
            return -1;
        } else if (rankType == RankType.BLITZ) {
            return blitzTurn[timeByTurn.getSelectedIndex()];
        } else if (rankType == RankType.RAPID) {
            return rapidTurn[timeByTurn.getSelectedIndex()];
        } else if (rankType == RankType.LONG) {
            return longTurn[timeByTurn.getSelectedIndex()];
        }
        return unrankedTurn[timeByTurn.getSelectedIndex()];
    }

    private long getTimeByPlayer(RankType rankType) {
        if (rankType == RankType.BULLET) {
            return bulletGame[timeByGame.getSelectedIndex()];
        } else if (rankType == RankType.BLITZ) {
            return blitzGame[timeByGame.getSelectedIndex()];
        } else if (rankType == RankType.RAPID) {
            return rapidGame[timeByGame.getSelectedIndex()];
        } else if (rankType == RankType.LONG) {
            return -1;
        }
        return unrankedGame[timeByGame.getSelectedIndex()];
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
