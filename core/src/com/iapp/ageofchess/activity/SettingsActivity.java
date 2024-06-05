package com.iapp.ageofchess.activity;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.multiplayer.MultiplayerEngine;
import com.iapp.lib.chess_engine.Color;
import com.iapp.ageofchess.controllers.SettingsController;
import com.iapp.ageofchess.services.*;
import com.iapp.lib.ui.actors.*;
import com.iapp.lib.ui.screens.Activity;
import com.iapp.lib.ui.screens.RdApplication;
import com.iapp.lib.util.ArraysUtil;
import com.iapp.lib.util.OnChangeListener;
import com.iapp.lib.util.TransitionEffects;
import com.iapp.lib.util.WindowUtil;

import java.io.*;
import java.util.Arrays;
import java.util.function.Consumer;

public class SettingsActivity extends Activity {

    private final SettingsController controller;
    private RdImageTextButton back;
    private RdSelectBox<String> language;
    private RdCheckBox enableSounds;
    private RdCheckBox enableBackgroundMusic;
    private RdSelectBox<String> volumeEffects;
    private RdSelectBox<String> volumeMusic;
    private RdSelectBox<String> fps;
    private RdCheckBox enableSysProperties;
    private RdSelectBox<String> screenSpeed;
    private RdSelectBox<String> maxBoardSize;
    private RdSelectBox<String> piecesSpeed;
    private RdTextButton reset;

    private RdTextField name;
    private RdSelectionButton pieceColor;
    private RdCheckBox randomColor;
    private RdSelectBox<String> timeByTurn;
    private RdSelectBox<String> turnMode;
    private RdSelectBox<String> maxTurns;
    private RdSelectBox<String> timeByGame;
    private RdCheckBox flippedPieces;
    private RdCheckBox matchDescription;
    private RdCheckBox blockedHints;

    private RdCheckBox fullScreen;
    private RdCheckBox windowSize;
    private WindowGroup windowGroup;

    public SettingsActivity() {
        this.controller = new SettingsController(this);
    }

    @Override
    public void initActors() {

        back = new RdImageTextButton(strings.get("[i18n]Back"),"red_screen");
        back.setImage("ib_back");

        var infinity = strings.get("[i18n]infinity");
        language = new RdSelectBox<>(ChessAssetManager.current().getSkin());
        language.setItems(ChessApplication.self().getDisplayLanguages());

        enableSounds = new RdCheckBox(ChessAssetManager.current().getSkin(), "check_box");
        enableBackgroundMusic = new RdCheckBox(ChessAssetManager.current().getSkin(), "check_box");

        volumeEffects = new RdSelectBox<>();
        volumeEffects.setItems("20%", "40%", "60%", "80%", "100%");
        volumeMusic = new RdSelectBox<>();
        volumeMusic.setItems("20%", "40%", "60%", "80%", "100%");

        fps = new RdSelectBox<>(ChessAssetManager.current().getSkin());
        fps.setItems("25 fps", "30 fps", "45 fps", "60 fps", "90 fps", "120 fps", strings.get("[i18n]infinity"));

        enableSysProperties = new RdCheckBox(ChessAssetManager.current().getSkin(), "check_box");

        screenSpeed = new RdSelectBox<>(ChessAssetManager.current().getSkin());
        screenSpeed.setItems("0.05", "0.075", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6");

        maxBoardSize = new RdSelectBox<>(ChessAssetManager.current().getSkin());
        maxBoardSize.setItems("800x800", "900x900", "1000x1000", "1100x1100",
                "1200x1200", "1300x1300", "1400x1400", "1500x1500", "1600x1600");

        piecesSpeed = new RdSelectBox<>(ChessAssetManager.current().getSkin());
        piecesSpeed.setItems("0.001", "0.0015", "0.002", "0.0025", "0.003", "0.0035", "0.004", "0.0045", "0.005");

        reset = new RdTextButton(strings.get("[i18n]Reset"), ChessAssetManager.current().getSkin(), "blue");

        // ---------------------------------------------------------------------------------------------------------

        name = new RdTextField("", ChessAssetManager.current().getSkin());
        name.setMaxLength(15);
        pieceColor = new RdSelectionButton(ChessAssetManager.current().getSkin(),
                new String[]{strings.get("[i18n]white"), strings.get("[i18n]Black")});

        randomColor = new RdCheckBox("check_box");

        String minByMoveKey = "[i18n]{0,choice,1#1 minute|1<{0,number} minutes}/move";

        timeByTurn = new RdSelectBox<>();
        timeByTurn.setItems(
                infinity,
                strings.format(minByMoveKey, 1), strings.format(minByMoveKey, 2),
                strings.format(minByMoveKey, 3), strings.format(minByMoveKey, 4),
                strings.format(minByMoveKey, 5));

        turnMode = new RdSelectBox<>();
        turnMode.setItems(
                strings.get("[i18n]Alternately"),
                strings.get("[i18n]Alternately/Fast"));

        maxTurns = new RdSelectBox<>();
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

        flippedPieces = new RdCheckBox(ChessAssetManager.current().getSkin(), "check_box");
        matchDescription = new RdCheckBox(ChessAssetManager.current().getSkin(), "check_box");
        blockedHints = new RdCheckBox(ChessAssetManager.current().getSkin(), "check_box");

        fullScreen = new RdCheckBox(ChessAssetManager.current().getSkin(), "check_box");
        windowSize = new RdCheckBox(ChessAssetManager.current().getSkin(), "check_box");

        loadSettings();
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

        language.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.updateLang(language.getSelectedIndex());
            }
        });

        enableBackgroundMusic.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.updateBackgroundMusic(enableBackgroundMusic.isChecked());
            }
        });

        enableSounds.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.updateSounds(enableSounds.isChecked());
            }
        });

        volumeEffects.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.updateVolumeEffects(volumeEffects.getSelected());
            }
        });

        volumeMusic.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.updateVolumeMusic(volumeMusic.getSelected());
            }
        });

        fps.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.updateFPS(fps.getSelected());
            }
        });

        enableSysProperties.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.updateSysProperties(enableSysProperties.isChecked());
            }
        });

        screenSpeed.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                ChessConstants.localData.setScreenSpeed(Float.parseFloat(screenSpeed.getSelected()));
            }
        });

        name.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                ChessConstants.localData.setName(name.getText());
            }
        });

        pieceColor.getGroup().getButtons().get(0)
                .addListener(new OnChangeListener() {
                    @Override
                    public void onChange(Actor actor) {
                        ChessConstants.localData.setPieceColor(Color.WHITE);
                    }
                });
        pieceColor.getGroup().getButtons().get(1)
                .addListener(new OnChangeListener() {
                    @Override
                    public void onChange(Actor actor) {
                        ChessConstants.localData.setPieceColor(Color.BLACK);
                    }
                });

        timeByTurn.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                var pair = SettingsUtil.defineTimeByTurn(timeByTurn.getSelected());
                ChessConstants.localData.setInfinityByTurn(pair.getKey());
                ChessConstants.localData.setTimeByTurn(pair.getValue());
            }
        });
        turnMode.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                ChessConstants.localData.setTurnMode(SettingsUtil.defineTurnMode(turnMode.getSelected()));
            }
        });
        maxTurns.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                var pair = SettingsUtil.defineMaxTurns(maxTurns.getSelected());
                ChessConstants.localData.setInfinityTurns(pair.getKey());
                ChessConstants.localData.setMaxTurns(pair.getValue());
            }
        });
        timeByGame.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                var pair = SettingsUtil.defineTimeByGame(timeByGame.getSelected());
                ChessConstants.localData.setInfinityTimeGame(pair.getKey());
                ChessConstants.localData.setTimeByGame(pair.getValue());
            }
        });
        flippedPieces.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                ChessConstants.localData.setFlippedPieces(flippedPieces.isChecked());
            }
        });

        matchDescription.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                ChessConstants.localData.setMatchDescription(matchDescription.isChecked());
            }
        });
        blockedHints.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                ChessConstants.localData.setBlockedHints(blockedHints.isChecked());
            }
        });

        maxBoardSize.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                ChessConstants.localData.setBoardMaxSize(
                        Float.parseFloat(maxBoardSize.getSelected().split("x")[0]));
            }
        });

        piecesSpeed.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                ChessConstants.localData.setPiecesSpeed(
                        Float.parseFloat(piecesSpeed.getSelected()));
            }
        });

        randomColor.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                ChessConstants.localData.setRandomColor(randomColor.isChecked());
            }
        });

        reset.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.resetSettings();
                resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            }
        });

        // ------

        fullScreen.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.updateFullScreen(fullScreen.isChecked());
            }
        });

        windowSize.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.updateWindowSize(windowSize.isChecked());
            }
        });
    }

    @Override
    public void show(Stage stage, Activity last) {
        Image background = new Image(new TextureRegionDrawable(
            ChessAssetManager.current().findChessRegion("menu_background")));
        background.setScaling(Scaling.fill);
        background.setFillParent(true);
        getStage().addActor(background);

        if (ChessConstants.loggingAcc != null) {
            RdTable panel = new RdTable();
            panel.align(Align.topLeft);
            panel.setFillParent(true);
            getStage().addActor(panel);
            panel.add(ChessConstants.accountPanel)
                .expandX().fillX();
        }

        var window = new RdWindow("", "screen_window");
        window.setMovable(false);
        var properties = new PropertyTable(400);
        window.add(properties).expand().fill();

        var fpsHint = new RdImageTextButton("", "circle");
        fpsHint.getLabelCell().reset();
        fpsHint.setImage("ib_question");
        var fpsTool = new RdTextTooltip(strings.get("[i18n]Sets limits on the maximum number of frames per second"));
        fpsHint.addListener(fpsTool);

        var systemHint = new RdImageTextButton("", "circle");
        systemHint.getLabelCell().reset();
        systemHint.setImage("ib_question");
        var systemTool = new RdTextTooltip(strings.get("[i18n]Shows frames per second and memory used"));
        systemHint.addListener(systemTool);

        var turnModeHint = new RdImageTextButton("", "circle");
        turnModeHint.getLabelCell().reset();
        turnModeHint.setImage("ib_question");
        var turnModeTool = new RdTextTooltip(strings.get("[i18n]In alternately mode, you always have to wait for the end of the time per turn"));
        turnModeHint.addListener(turnModeTool);

        var screenSizeHint = new RdImageTextButton("", "circle");
        screenSizeHint.getLabelCell().reset();
        screenSizeHint.setImage("ib_question");
        var screenSizeTool = new RdTextTooltip(strings.get("[i18n]saves application window size on exit"));
        screenSizeHint.addListener(screenSizeTool);

        properties.add(new PropertyTable.Title(strings.get("[i18n]System settings")));

        properties.add(new PropertyTable.Element(strings.get("[i18n]App language"), language));
        properties.add(new PropertyTable.Element(strings.get("[i18n]Sound effects"), enableSounds));
        properties.add(new PropertyTable.Element(strings.get("[i18n]Background music"), enableBackgroundMusic));
        properties.add(new PropertyTable.Element(strings.get("[i18n]Effects Volume"), volumeEffects));
        properties.add(new PropertyTable.Element(strings.get("[i18n]Music volume"), volumeMusic));
        properties.add(new PropertyTable.Element(strings.get("[i18n]Frames per second"), fpsHint, fps));
        properties.add(new PropertyTable.Element(strings.get("[i18n]System properties"), systemHint, enableSysProperties));
        properties.add(new PropertyTable.Element(strings.get("[i18n]Screen transition speed"), screenSpeed));
        properties.add(new PropertyTable.Element(strings.get("[i18n]Maximum board size") , maxBoardSize));
        properties.add(new PropertyTable.Element(strings.get("[i18n]Speed of movement of pieces"), piecesSpeed));
        properties.add(new PropertyTable.Element(strings.get("[i18n]Reset settings"), reset));

        properties.add(new PropertyTable.Title(strings.get("[i18n]General settings")));

        properties.add(new PropertyTable.Element(strings.get("[i18nGame name"), name));
        properties.add(new PropertyTable.Element(strings.get("[i18n]Display match info on enter"), matchDescription));
        properties.add(new PropertyTable.Element(strings.get("[i18n]Max turns"), maxTurns));
        properties.add(new PropertyTable.Element(strings.get("[i18n]Random color"), randomColor));

        properties.add(new PropertyTable.Title(strings.get("[i18n]Single-player") + strings.get("[i18n] (default)")));

        properties.add(new PropertyTable.Element(strings.get("[i18n]Shape color"), pieceColor));
        properties.add(new PropertyTable.Element(strings.get("[i18n]Time to turn"), timeByTurn));
        properties.add(new PropertyTable.Element(strings.get("[i18n]Turn Mode"), turnModeHint, turnMode));
        properties.add(new PropertyTable.Element(strings.get("[i18n]Time for the whole game for one person"), timeByGame));
        properties.add(new PropertyTable.Element(strings.get("[i18n]Block hints"), blockedHints));
        properties.add(new PropertyTable.Element(strings.get("[i18n]Flipped pieces"), flippedPieces));

        //properties.add(new PropertyTable.Title(strings.get("multiplayer") + strings.get("settings_default")));

        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            properties.add(new PropertyTable.Title(strings.get("[i18n]Desktop settings")));

            properties.add(new PropertyTable.Element(strings.get("[i18n]Full screen mode"), fullScreen));
            properties.add(new PropertyTable.Element(strings.get("[i18n]Save screen sizes"), screenSizeHint, windowSize));
        }

        windowGroup = new WindowGroup(window, back);
        ChessApplication.self().updateTitle(windowGroup, strings.get("[i18n]Single Player"));

        windowGroup.setFillParent(true);
        stage.addActor(windowGroup);
        windowGroup.update();

        if (last instanceof SettingsActivity) {
            TransitionEffects.alphaShow(getStage().getRoot(), ChessConstants.localData.getScreenDuration());
        } else {
            TransitionEffects.transitionBottomShow(windowGroup, ChessConstants.localData.getScreenDuration());
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        windowGroup.update();
    }

    @Override
    public void loadSettings() {
        language.setSelected(RdApplication.self().getDisplayLanguages()
            [ArraysUtil.indexOf(RdApplication.self().getLanguageCodes(), ChessConstants.localData.getLangCode())]);
        enableSounds.setChecked(ChessConstants.localData.isEnableSounds());
        enableBackgroundMusic.setChecked(ChessConstants.localData.isEnableBackgroundMusic());
        volumeEffects.setSelected(((int) ChessConstants.localData.getEffectsVolume() * 100) + "%");
        volumeMusic.setSelected(((int) ChessConstants.localData.getMusicVolume() * 100) + "%");

        fps.setSelected(SettingsUtil.defineFPS());
        enableSysProperties.setChecked(ChessConstants.localData.isEnableSysProperties());
        screenSpeed.setSelected(String.valueOf(ChessConstants.localData.getScreenDuration()));
        maxBoardSize.setSelected(((int) ChessConstants.localData.getMaxBoardSize())
                + "x" + ((int) ChessConstants.localData.getMaxBoardSize()));
        piecesSpeed.setSelected(String.valueOf(ChessConstants.localData.getPiecesSpeed()));

        name.setText(ChessConstants.localData.getGameName());
        if (ChessConstants.localData.getPieceColor() == Color.BLACK) pieceColor.getGroup().getButtons().get(1).setChecked(true);
        else pieceColor.getGroup().getButtons().get(0).setChecked(true);
        randomColor.setChecked(ChessConstants.localData.isRandomColor());
        timeByTurn.setSelected(SettingsUtil.defineTimeByTurn());
        turnMode.setSelected(SettingsUtil.defineTurnMode());
        maxTurns.setSelected(SettingsUtil.defineMaxTurns());
        timeByGame.setSelected(SettingsUtil.defineTimeByGame());
        flippedPieces.setChecked(ChessConstants.localData.isFlippedPieces());
        matchDescription.setChecked(ChessConstants.localData.isMatchDescription());
        blockedHints.setChecked(ChessConstants.localData.isBlockedHints());

        windowSize.setChecked(ChessConstants.localData.isSaveWindowSize());
        fullScreen.setChecked(ChessConstants.localData.isFullScreen());
    }

    @Override
    public Actor hide(SequenceAction action, Activity next) {
        if (next instanceof SettingsActivity) {
            TransitionEffects.alphaHide(action, ChessConstants.localData.getScreenDuration());
            return getStage().getRoot();
        } else {
            TransitionEffects.transitionBottomHide(action, windowGroup, ChessConstants.localData.getScreenDuration());
            return windowGroup;
        }
    }
}
