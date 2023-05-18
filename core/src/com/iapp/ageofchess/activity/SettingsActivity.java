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
import com.iapp.lib.chess_engine.Color;
import com.iapp.ageofchess.controllers.SettingsController;
import com.iapp.ageofchess.services.*;
import com.iapp.lib.ui.actors.*;
import com.iapp.lib.ui.screens.Activity;
import com.iapp.lib.util.OnChangeListener;
import com.iapp.lib.util.TransitionEffects;
import com.iapp.lib.util.WindowUtil;

public class SettingsActivity extends Activity {

    private final SettingsController controller;
    private RdImageTextButton back;
    private RdSelectBox<String> language;
    private RdCheckBox enableSounds;
    private RdCheckBox enableBackgroundMusic;
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
    private RdDialog resetDialog;
    private WindowGroup windowGroup;

    public SettingsActivity() {
        this.controller = new SettingsController(this);
    }

    @Override
    public void initActors() {
        back = new RdImageTextButton(strings.get("back"),"red_screen");
        back.setImage("ib_back");

        var infinity = strings.get("infinity");
        language = new RdSelectBox<>(ChessAssetManager.current().getSkin());
        language.setItems(ChessApplication.self().getDisplayLanguages().toArray(new String[0]));

        enableSounds = new RdCheckBox(ChessAssetManager.current().getSkin(), "check_box");
        enableBackgroundMusic = new RdCheckBox(ChessAssetManager.current().getSkin(), "check_box");

        fps = new RdSelectBox<>(ChessAssetManager.current().getSkin());
        fps.setItems("25 fps", "30 fps", "45 fps", "60 fps", "90 fps", "120 fps", strings.get("infinity"));

        enableSysProperties = new RdCheckBox(ChessAssetManager.current().getSkin(), "check_box");

        screenSpeed = new RdSelectBox<>(ChessAssetManager.current().getSkin());
        screenSpeed.setItems("0.05", "0.075", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6");

        maxBoardSize = new RdSelectBox<>(ChessAssetManager.current().getSkin());
        maxBoardSize.setItems("800x800", "900x900", "1000x1000", "1100x1100",
                "1200x1200", "1300x1300", "1400x1400", "1500x1500", "1600x1600");

        piecesSpeed = new RdSelectBox<>(ChessAssetManager.current().getSkin());
        piecesSpeed.setItems("0.001", "0.0015", "0.002", "0.0025", "0.003", "0.0035", "0.004", "0.0045", "0.005");

        reset = new RdTextButton(strings.get("reset"), ChessAssetManager.current().getSkin(), "blue");

        // -------------

        name = new RdTextField("", ChessAssetManager.current().getSkin());
        pieceColor = new RdSelectionButton(ChessAssetManager.current().getSkin(),
                new String[]{strings.get("white"), strings.get("black")});

        randomColor = new RdCheckBox("check_box");

        timeByTurn = new RdSelectBox<>();
        timeByTurn.setItems(
                infinity,
                strings.format("min_by_move", 1), strings.format("min_by_move", 2),
                strings.format("min_by_move", 3), strings.format("min_by_move", 4),
                strings.format("min_by_move", 5));

        turnMode = new RdSelectBox<>();
        turnMode.setItems(
                strings.get("concurrent"),
                strings.get("concurrent_fast"));

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
                resetDialog = controller.resetSettings();
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
            panel.add(ChessApplication.self().getAccountPanel())
                .expandX().fillX();
        }

        var window = new RdWindow("", "screen_window");
        window.setMovable(false);
        var properties = new PropertyTable(400, ChessAssetManager.current().getSkin());
        window.add(properties).expand().fill();

        var fpsHint = new RdImageTextButton("", "circle");
        fpsHint.getLabelCell().reset();
        fpsHint.setImage("ib_question");
        var fpsTool = new RdTextTooltip(strings.get("fps_hint"));
        fpsHint.addListener(fpsTool);

        var systemHint = new RdImageTextButton("", "circle");
        systemHint.getLabelCell().reset();
        systemHint.setImage("ib_question");
        var systemTool = new RdTextTooltip(strings.get("system_hint"));
        systemHint.addListener(systemTool);

        var turnModeHint = new RdImageTextButton("", "circle");
        turnModeHint.getLabelCell().reset();
        turnModeHint.setImage("ib_question");
        var turnModeTool = new RdTextTooltip(strings.get("turn_mode_hint"));
        turnModeHint.addListener(turnModeTool);

        var screenSizeHint = new RdImageTextButton("", "circle");
        screenSizeHint.getLabelCell().reset();
        screenSizeHint.setImage("ib_question");
        var screenSizeTool = new RdTextTooltip(strings.get("screen_size_hint"));
        screenSizeHint.addListener(screenSizeTool);

        properties.add(new PropertyTable.Title(strings.get("sys_settings")));

        properties.add(new PropertyTable.Element(strings.get("app_lang"), language));
        properties.add(new PropertyTable.Element(strings.get("sound_effects"), enableSounds));
        properties.add(new PropertyTable.Element(strings.get("background_music"), enableBackgroundMusic));
        properties.add(new PropertyTable.Element(strings.get("fps"), fpsHint, fps));
        properties.add(new PropertyTable.Element(strings.get("sys_properties"), systemHint, enableSysProperties));
        properties.add(new PropertyTable.Element(strings.get("speed_screen"), screenSpeed));
        properties.add(new PropertyTable.Element(strings.get("max_board_size") , maxBoardSize));
        properties.add(new PropertyTable.Element(strings.get("max_pieces_speed"), piecesSpeed));
        properties.add(new PropertyTable.Element(strings.get("reset_default"), reset));

        properties.add(new PropertyTable.Title(strings.get("general_settings")));

        properties.add(new PropertyTable.Element(strings.get("game_name"), name));
        properties.add(new PropertyTable.Element(strings.get("match_info"), matchDescription));
        properties.add(new PropertyTable.Element(strings.get("max_turns"), maxTurns));
        properties.add(new PropertyTable.Element(strings.get("random_color"), randomColor));

        properties.add(new PropertyTable.Title(strings.get("single-player") + strings.get("settings_default")));

        properties.add(new PropertyTable.Element(strings.get("upper_color"), pieceColor));
        properties.add(new PropertyTable.Element(strings.get("time_turn"), timeByTurn));
        properties.add(new PropertyTable.Element(strings.get("turn_mode"), turnModeHint, turnMode));
        properties.add(new PropertyTable.Element(strings.get("game_time"), timeByGame));
        properties.add(new PropertyTable.Element(strings.get("blocked_hints"), blockedHints));
        properties.add(new PropertyTable.Element(strings.get("flipped_pieces"), flippedPieces));

        //properties.add(new PropertyTable.Title(strings.get("multiplayer") + strings.get("settings_default")));

        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            properties.add(new PropertyTable.Title(strings.get("desktop_settings")));

            properties.add(new PropertyTable.Element(strings.get("full_screen_mode"), fullScreen));
            properties.add(new PropertyTable.Element(strings.get("save_screen_sizes"), screenSizeHint, windowSize));
        }

        windowGroup = new WindowGroup(window, back);
        ChessApplication.self().updateTitle(windowGroup, strings.get("single-player"));

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
        WindowUtil.resizeCenter(resetDialog);
        windowGroup.update();
    }

    @Override
    public void loadSettings() {
        language.setSelected(ChessConstants.localData.getLocale().getDisplayLanguage(ChessConstants.localData.getLocale()));
        enableSounds.setChecked(ChessConstants.localData.isEnableSounds());
        enableBackgroundMusic.setChecked(ChessConstants.localData.isEnableBackgroundMusic());
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
