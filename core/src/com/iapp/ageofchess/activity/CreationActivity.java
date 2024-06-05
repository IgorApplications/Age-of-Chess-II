package com.iapp.ageofchess.activity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.controllers.CreationController;
import com.iapp.ageofchess.modding.GameMode;
import com.iapp.ageofchess.modding.LocalMatch;
import com.iapp.ageofchess.modding.MapData;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.ageofchess.services.SettingsUtil;
import com.iapp.lib.chess_engine.Color;
import com.iapp.lib.ui.actors.*;
import com.iapp.lib.ui.screens.Activity;
import com.iapp.lib.util.OnChangeListener;
import com.iapp.lib.util.TransitionEffects;
import com.iapp.lib.util.WindowUtil;

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
        RdTable content = new RdTable();
        content.setFillParent(true);
        window = new RdWindow("",  "screen_window");
        window.setMovable(false);
        properties = new PropertyTable(400);
        window.add(properties).expand().fill();

        back = new RdImageTextButton(strings.get("[i18n]Back"), "red_screen");
        back.setImage("ib_back");

        name = new RdTextArea("", ChessAssetManager.current().getSkin());
        name.setMaxLength(15);
        pieceColor = new RdSelectionButton(
                ChessAssetManager.current().getSkin(),
                new String[]{strings.get("[i18n]White"), strings.get("[i18n]Black")});

        flippedPieces = new RdCheckBox(ChessAssetManager.current().getSkin(), "check_box");
        matchDescription = new RdCheckBox(ChessAssetManager.current().getSkin(), "check_box");
        blockedHints = new RdCheckBox(ChessAssetManager.current().getSkin(), "check_box");

        String infinity = strings.get("[i18n]infinity");
        String minByMoveKey = "[i18n]{0,choice,1#1 minute|1<{0,number} minutes}/move";
        String turnsKey = "[i18n]{0,choice,1#1 turn|1<{0,number,integer} turns}";
        String minutesKey = "[i18n]{0,choice,1#1 minute|1<{0,number} minutes}";
        String hoursKey = "[i18n]{0,choice,1#1 hour|1<{0,number} hours}";

        timeByTurn = new RdSelectBox<>(ChessAssetManager.current().getSkin());
        timeByTurn.setItems(
                infinity,
                strings.format(minByMoveKey, 1), strings.format(minByMoveKey, 2),
                strings.format(minByMoveKey, 3), strings.format(minByMoveKey, 4),
                strings.format(minByMoveKey, 5));

        maxTurns = new RdSelectBox<>(ChessAssetManager.current().getSkin());
        maxTurns.setItems(
                infinity,
                strings.format(turnsKey, 5), strings.format(turnsKey, 10),
                strings.format(turnsKey, 15), strings.format(turnsKey, 20),
                strings.format(turnsKey, 25), strings.format(turnsKey, 30),
                strings.format(turnsKey, 35), strings.format(turnsKey, 40),
                strings.format(turnsKey, 45), strings.format(turnsKey, 50),
                strings.format(turnsKey, 55), strings.format(turnsKey, 60),
                strings.format(turnsKey, 65), strings.format(turnsKey, 70),
                strings.format(turnsKey, 75), strings.format(turnsKey, 80),
                strings.format(turnsKey, 85), strings.format(turnsKey, 90),
                strings.format(turnsKey, 95), strings.format(turnsKey, 100));

        timeByGame = new RdSelectBox<>(ChessAssetManager.current().getSkin());
        timeByGame.setItems(
                infinity,
                strings.format(minutesKey, 5),
                strings.format(minutesKey, 10),
                strings.format(minutesKey, 20),
                strings.format(minutesKey, 30),
                strings.format(minutesKey, 40),
                strings.format(minutesKey, 50),
                strings.format(hoursKey, 1),
                strings.format(hoursKey, 2));

        turnModeSelection = new RdSelectBox<>(ChessAssetManager.current().getSkin());
        turnModeSelection.setItems(
                strings.get("[i18n]Alternately"),
                strings.get("[i18n]Alternately/Fast"));

        create = new RdTextButton(strings.get("[i18n]create"), "blue");

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
    public void show(Stage stage, Activity last) {
        ChessApplication.self().getLauncher().setOnFinish(controller::goToScenario);

        Image background = new Image(new TextureRegionDrawable(
            ChessAssetManager.current().findChessRegion("menu_background")));
        background.setFillParent(true);
        getStage().addActor(background);
        background.setScaling(Scaling.fill);

        var turnModeHint = new RdImageTextButton("", "circle");
        turnModeHint.getLabelCell().reset();
        turnModeHint.setImage("ib_question");
        var turnModeTool = new RdTextTooltip(strings.get("[i18n]In alternately mode, you always have to wait for the end of the time per turn"));
        turnModeHint.addListener(turnModeTool);

        properties.add(new PropertyTable.Title(strings.get("[i18n]Game creation")));
        properties.add(new PropertyTable.Element(strings.get("[i18n]Game name"), name));
        properties.add(new PropertyTable.Element(strings.get("[i18n]Shape color"), pieceColor));
        properties.add(new PropertyTable.Element(strings.get("[i18n]Random color"), randomColor));
        properties.add(new PropertyTable.Element(strings.get("[i18n]Display match info on enter"), matchDescription));
        properties.add(new PropertyTable.Element(strings.get("[i18n]Block hints"), blockedHints));
        properties.add(new PropertyTable.Element(strings.get("[i18n]Max turns"), maxTurns));

        // Two players mode
        if (ChessConstants.localData.getGameMode() == GameMode.TWO_PLAYERS) {
            properties.add(new PropertyTable.Element(strings.get("[i18n]Flipped pieces"), flippedPieces));
            properties.add(new PropertyTable.Element(strings.get("[i18n]Game mode"), turnModeHint, turnModeSelection));
            properties.add(new PropertyTable.Element(strings.get("[i18n]Time for the whole game for one person"), timeByGame));
            properties.add(new PropertyTable.Element(strings.get("[i18n]Time to turn"), timeByTurn));
        } else {
            flippedPieces.setChecked(false);
            timeByGame.setSelected(strings.get("[i18n]infinity"));
            timeByTurn.setSelected(strings.get("[i18n]infinity"));
        }
        properties.add(new PropertyTable.Element("", create));

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
