package com.iapp.ageofchess.activity;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.chess_engine.Color;
import com.iapp.ageofchess.controllers.CreationController;
import com.iapp.ageofchess.modding.GameMode;
import com.iapp.ageofchess.modding.LocalMatch;
import com.iapp.ageofchess.modding.MapData;
import com.iapp.ageofchess.util.ChessAssetManager;
import com.iapp.ageofchess.util.ChessConstants;
import com.iapp.ageofchess.util.SettingsUtil;
import com.iapp.rodsher.actors.*;
import com.iapp.rodsher.screens.Activity;
import com.iapp.rodsher.util.OnChangeListener;
import com.iapp.rodsher.util.WindowUtil;

public class CreationActivity extends Activity {

    private final CreationController controller;
    private final MapData mapData;
    private final int scenario;

    private RdImageTextButton back;
    private RdWindow window;
    private PropertyTable properties;
    private RdTextArea name;
    private RdSelectionButton pieceColor;
    private RdCheckBox flippedPieces;
    private RdSelectBox<String> timeByGame;
    private RdCheckBox blockedHints;
    private RdCheckBox matchDescription;
    private RdCheckBox randomColor;
    private RdSelectBox<String> maxTurns;
    private RdSelectBox<String> turnModeSelection;
    private RdSelectBox<String> timeByTurn;
    private RdTextButton create;
    private WindowGroup windowGroup;

    private Spinner spinner;

    public CreationActivity(MapData mapData, int scenario) {
        this.controller = new CreationController(this);
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

        name = new RdTextArea("");
        name.setPrefLines(1);
        pieceColor = new RdSelectionButton(
                ChessAssetManager.current().getSkin(),
                new String[]{strings.get("white"), strings.get("black")});

        flippedPieces = new RdCheckBox(ChessAssetManager.current().getSkin(), "check_box");
        matchDescription = new RdCheckBox(ChessAssetManager.current().getSkin(), "check_box");
        blockedHints = new RdCheckBox(ChessAssetManager.current().getSkin(), "check_box");

        var infinity = strings.get("infinity");
        timeByTurn = new RdSelectBox<>(ChessAssetManager.current().getSkin());
        timeByTurn.setItems(
                infinity,
                strings.format("min_by_move", 1), strings.format("min_by_move", 2),
                strings.format("min_by_move", 3), strings.format("min_by_move", 4),
                strings.format("min_by_move", 5));

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

        turnModeSelection = new RdSelectBox<>(ChessAssetManager.current().getSkin());
        turnModeSelection.setItems(
                strings.get("concurrent"),
                strings.get("concurrent_fast"));

        create = new RdTextButton(strings.get("create"), "blue");

        randomColor = new RdCheckBox("check_box");

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
        pieceColor.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {}
        });

        randomColor.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {}
        });

        flippedPieces.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {}
        });

        matchDescription.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {}
        });

        blockedHints.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {}
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

        turnModeSelection.addListener(new OnChangeListener() {
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
    public void show(Stage stage) {
        ChessApplication.self().getLauncher().setOnFinish(controller::goToScenario);

        var turnModeHint = new RdImageTextButton("", "circle");
        turnModeHint.getLabelCell().reset();
        turnModeHint.setImage("ib_question");
        var turnModeTool = new RdTextTooltip(strings.get("turn_mode_hint"));
        turnModeHint.addListener(turnModeTool);

        properties.add(new PropertyTable.Title(strings.get("game_creation")));
        properties.add(new PropertyTable.Element(strings.get("game_name"), name));
        properties.add(new PropertyTable.Element(strings.get("upper_color"), pieceColor));
        properties.add(new PropertyTable.Element(strings.get("random_color"), randomColor));
        properties.add(new PropertyTable.Element(strings.get("match_info"), matchDescription));
        properties.add(new PropertyTable.Element(strings.get("blocked_hints"), blockedHints));
        properties.add(new PropertyTable.Element(strings.get("max_turns"), maxTurns));

        // Two players mode
        if (ChessConstants.localData.getGameMode() == GameMode.TWO_PLAYERS) {
            properties.add(new PropertyTable.Element(strings.get("flipped_pieces"), flippedPieces));
            properties.add(new PropertyTable.Element(strings.get("game_mode"), turnModeHint, turnModeSelection));
            properties.add(new PropertyTable.Element(strings.get("game_time"), timeByGame));
            properties.add(new PropertyTable.Element(strings.get("time_turn"), timeByTurn));
        } else {
            flippedPieces.setChecked(false);
            timeByGame.setSelected(strings.get("infinity"));
            timeByTurn.setSelected(strings.get("infinity"));
        }
        properties.add(new PropertyTable.Element("", create));

        windowGroup = new WindowGroup(window, back);
        ChessApplication.self().updateTitle(windowGroup, strings.get("single-player"));

        windowGroup.setFillParent(true);
        stage.addActor(windowGroup);
        windowGroup.update();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        windowGroup.update();
        WindowUtil.resizeCenter(spinner);
    }

    private void launchGame() {
        var color = SettingsUtil.reverse(
                SettingsUtil.defineColor(String.valueOf(pieceColor.getGroup().getChecked().getText())));
        var turns = SettingsUtil.defineMaxTurns(maxTurns.getSelected());
        var time = SettingsUtil.defineTimeByTurn(timeByTurn.getSelected());
        var matchInfoFlag = matchDescription.isChecked();
        var blockedHintsFlag = blockedHints.isChecked();
        var timeGame = SettingsUtil.defineTimeByGame(timeByGame.getSelected());
        var turnMode = SettingsUtil.defineTurnMode(turnModeSelection.getSelected());

        var builder = new LocalMatch.GameBuilder(name.getText(), color, mapData)
                .gameMode(ChessConstants.localData.getGameMode())
                .flippedPieces(flippedPieces.isChecked())
                .infiniteTurns(turns.getKey())
                .maxTurns(turns.getValue())
                .infiniteTimeByTurn(time.getKey())
                .timeByTurn(time.getValue())
                .matchInfo(matchInfoFlag)
                .blockedHints(blockedHintsFlag)
                .infiniteTimeByGame(timeGame.getKey())
                .timeByGame(timeGame.getValue())
                .numberScenario(scenario)
                .randomColor(randomColor.isChecked())
                .turnMode(turnMode);
        controller.launchGame(new LocalMatch(generateMatchId(), builder));
    }

    @Override
    public void loadSettings() {
        name.setText(ChessConstants.localData.getGameName());
        turnModeSelection.setSelected(SettingsUtil.defineTurnMode());
        timeByTurn.setSelected(SettingsUtil.defineTimeByTurn());
        randomColor.setChecked(ChessConstants.localData.isRandomColor());
        maxTurns.setSelected(SettingsUtil.defineMaxTurns());
        timeByGame.setSelected(SettingsUtil.defineTimeByGame());
        if (ChessConstants.localData.getPieceColor() == Color.BLACK) pieceColor.getGroup().getButtons().get(1).setChecked(true);
        else pieceColor.getGroup().getButtons().get(0).setChecked(true);
        flippedPieces.setChecked(ChessConstants.localData.isFlippedPieces());
        matchDescription.setChecked(ChessConstants.localData.isMatchDescription());
        blockedHints.setChecked(ChessConstants.localData.isBlockedHints());
    }

    public static long generateMatchId() {
        for (long id = 0; id < Long.MAX_VALUE - 100; id++) {
            boolean contains = false;
            for (var ref : ChessConstants.localData.getReferences()) {
                if (ref.getMatch() == null) continue;

                if (ref.getMatch().getId() == id) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                return id;
            }
        }
        return -1;
    }
}
