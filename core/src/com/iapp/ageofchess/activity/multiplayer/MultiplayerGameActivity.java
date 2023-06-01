package com.iapp.ageofchess.activity.multiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.controllers.multiplayer.GameSettingsController;
import com.iapp.ageofchess.controllers.multiplayer.MultiplayerGameController;
import com.iapp.ageofchess.graphics.*;
import com.iapp.ageofchess.modding.LocalMatch;
import com.iapp.ageofchess.multiplayer.Match;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.ageofchess.services.SettingsUtil;
import com.iapp.ageofchess.services.Sounds;
import com.iapp.lib.chess_engine.Result;
import com.iapp.lib.chess_engine.TypePiece;
import com.iapp.lib.ui.actors.RdDialog;
import com.iapp.lib.ui.actors.RdDialogBuilder;
import com.iapp.lib.ui.actors.RdImageTextButton;
import com.iapp.lib.ui.actors.RdLabel;
import com.iapp.lib.ui.screens.Activity;
import com.iapp.lib.ui.screens.GrayAssetManager;
import com.iapp.lib.ui.screens.RdApplication;
import com.iapp.lib.ui.screens.RdLogger;
import com.iapp.lib.ui.widgets.BoardView;
import com.iapp.lib.ui.widgets.ChatView;
import com.iapp.lib.util.OnChangeListener;
import com.iapp.lib.util.TransitionEffects;
import com.iapp.lib.util.WindowUtil;
import com.iapp.lib.web.Account;
import com.iapp.lib.web.RankType;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class MultiplayerGameActivity extends Activity {

    private final DecimalFormat rankFormat;
    private final GameSettingsController settingsController;
    private static final Vector3 spriteTouchPoint = new Vector3();
    static boolean verticallyMode;
    private boolean exit = true;

    private MultiplayerGameActivity oldState;
    final MultiplayerGameController controller;
    private float coefficientX, coefficientY;

    // labels
    RdLabel timeByTurnLabel, turnsLabel, whiteTime, blackTime;
    boolean fewTime;

    ControlGameView controlGame;
    // game board
    BoardView gameBoard;

    // dialog objects
    Sprite infoSprite, selectionSprite;
    MultiplayerSelectionDialog selectionDialog;
    ResultDialog resultDialog;
    RdDialog infoDialog;
    RdDialog menuDialog, statisticDialog;

    Table content;
    RdImageTextButton menu, controlMenu, settings;

    Image blackout;
    private AtomicBoolean handleInfoBlackout = new AtomicBoolean(false),
            handleSelectionBlackout = new AtomicBoolean(false);

    public static MultiplayerGameActivity newInstance(LocalMatch localMatch, Match match) {
        verticallyMode = Gdx.graphics.getWidth() <= Gdx.graphics.getHeight();
        MultiplayerGameActivity activity;
        if (verticallyMode) {
            activity = new MultiplayerGameActivityV(localMatch, match);
        } else {
            activity = new MultiplayerGameActivityH(localMatch, match);
        }

        return activity;
    }

    private static MultiplayerGameActivity newInstance(MultiplayerGameActivity oldState, MultiplayerGameController controller) {
        verticallyMode = Gdx.graphics.getWidth() <= Gdx.graphics.getHeight();
        MultiplayerGameActivity activity;
        if (verticallyMode) {
            activity = new MultiplayerGameActivityV(oldState, controller);
        } else {
            activity = new MultiplayerGameActivityH(oldState, controller);
        }

        return activity;
    }

    MultiplayerGameActivity(LocalMatch localMatch, Match match) {
        this.controller = new MultiplayerGameController(this, localMatch, match);
        initialize();
        rankFormat = new DecimalFormat("#.##");
        settingsController = new GameSettingsController();
        rankFormat.setRoundingMode(RoundingMode.CEILING);
    }

    MultiplayerGameActivity(MultiplayerGameActivity oldState, MultiplayerGameController controller) {
        this.controller = controller;
        this.oldState = oldState;
        initialize();
        rankFormat = new DecimalFormat("#.##");
        settingsController = new GameSettingsController();
        rankFormat.setRoundingMode(RoundingMode.CEILING);
    }

    @Override
    public void show(Stage stage, Activity last) {
        ChessApplication.self().getLineContent().setVisible(false);
        TransitionEffects.alphaShow(stage.getRoot(), ChessConstants.localData.getScreenDuration());
    }

    @Override
    public void show(Activity last) {
        if (!(last instanceof MultiplayerGameActivity)) {
            ChessConstants.chatView.updateMode(ChatView.Mode.GAMES);
        }
        super.show(last);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        handleBlackout();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (exit) controller.stop();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (verticallyMode != (width <= height) && width != 0 && height != 0) {
            exit = false;
            RdApplication.self().setScreen(newInstance(this, controller));
        }

        WindowUtil.resizeCenter(menuDialog);
        WindowUtil.resizeCenter(resultDialog);
        WindowUtil.resizeCenter(statisticDialog);
        WindowUtil.resizeCenter(selectionDialog);

        if (infoDialog != null) infoSprite.setBounds(infoDialog.getX(), infoDialog.getY(), infoDialog.getWidth(), infoDialog.getHeight());
        if (selectionDialog != null) selectionSprite.setBounds(selectionDialog.getX(), selectionDialog.getY(), selectionDialog.getWidth(), selectionDialog.getHeight());
    }

    @Override
    public Actor hide(SequenceAction action, Activity next) {
        if (next instanceof MultiplayerGameActivity) return null;
        TransitionEffects.alphaHide(action, ChessConstants.localData.getScreenDuration());
        return getStage().getRoot();
    }

    @Override
    public void initActors() {
        Image background = new Image(new TextureRegionDrawable(
            controller.getRegion("background")));
        background.setFillParent(true);
        getStage().addActor(background);
        background.setScaling(Scaling.fill);

        if (restoreState()) {
            return;
        }

        infoSprite = new Sprite();
        selectionSprite = new Sprite();
        gameBoard = new BoardView(controller);
        controller.setBoardView(gameBoard);

        content = new Table();
        content.setFillParent(true);
        getStage().addActor(content);

        blackout = new Image(ChessAssetManager.current().getDarkTexture());
        blackout.setVisible(false);
        blackout.setFillParent(true);

        menu = new RdImageTextButton("");
        menu.setImage("iw_menu");
        menu.getLabelCell().reset();

        settings = new RdImageTextButton("");
        settings.setImage("iw_settings");
        settings.getLabelCell().reset();

        controlMenu = new RdImageTextButton("");
        controlMenu.setImage("iw_menu");
        controlMenu.getLabelCell().reset();

        timeByTurnLabel = new RdLabel(controller.getTimeByTurn());
        turnsLabel = new RdLabel(controller.getTurn() + ". " + controller.defineColorMove());
        whiteTime = new RdLabel(controller.getWhiteTime());
        blackTime = new RdLabel(controller.getBlackTime());

        showInfoDialog();
    }

    public void update() {
        ChessConstants.chatView.updateGameMessages(controller.getCurrentMatch().getLobbyMessages());
        ChessConstants.chatView.updateGameOnline(controller.getCurrentMatch().getEntered().size());

        if (controlGame != null) {
            if (controller.getCurrentMatch().isStarted()) {
                SequenceAction sequenceAction = new SequenceAction();
                sequenceAction.addAction(Actions.moveBy(0.0f, -900, 5));
                sequenceAction.addAction(Actions.run(() -> controlGame.setVisible(false)));
                controlGame.addAction(sequenceAction);
            }
            controlGame.updateControlContent();
        }
    }

    void updateLabels() {
        timeByTurnLabel.setText(controller.getTimeByTurn());
        blackTime.setText(controller.getBlackTime());
        whiteTime.setText(controller.getWhiteTime());

        timeByTurnLabel.setColor(Color.WHITE);
        blackTime.setColor(Color.WHITE);
        whiteTime.setColor(Color.WHITE);

        // check
        if (controller.getCurrentMatch().getResult() != Result.NONE) return;
        if (!controller.getCurrentMatch().isStarted() && controller.getCurrentMatch().isRandom()) return;

        var userColor = controller.getUserColor();
        if (!userColor.isPresent() || controller.getCurrentMatch().isAlternately()) return;
        var color = userColor.get();

        if (color == com.iapp.lib.chess_engine.Color.WHITE) whiteTime.setColor(Color.GREEN);
        else blackTime.setColor(Color.GREEN);
        if (color == controller.getColorMove()) timeByTurnLabel.setColor(Color.GREEN);

        // check
        if (!controller.getCurrentMatch().isStarted()) return;

        if (controller.isFewTimeByTurn() && controller.getColorMove() == color) timeByTurnLabel.setColor(Color.RED);
        if (controller.isFewBlackTime() && color == com.iapp.lib.chess_engine.Color.BLACK) blackTime.setColor(Color.RED);
        if (controller.isFewWhiteTime() && color == com.iapp.lib.chess_engine.Color.WHITE) whiteTime.setColor(Color.RED);

        if ((controller.isFewTimeByTurn()
                || controller.isFewBlackTime() || controller.isFewWhiteTime())) {
            if (!fewTime && controller.getColorMove() == color) {
                Sounds.self().playBell();
                fewTime = true;
            }
        } else {
            fewTime = false;
        }
    }

    void resizeBoard(Cell<BoardView> cell, float rectSize) {
        if (coefficientX > coefficientY) {
            cell.width(rectSize);
            cell.height(rectSize / coefficientX);
        } else {
            cell.width(rectSize / coefficientY);
            cell.height(rectSize);
        }
    }

    private boolean restoreState() {
        if (oldState == null) return false;
        controller.setBoardView(oldState.gameBoard);

        gameBoard = oldState.gameBoard;
        infoSprite = oldState.infoSprite;
        selectionSprite = oldState.selectionSprite;
        selectionDialog = oldState.selectionDialog;
        resultDialog = oldState.resultDialog;
        infoDialog = oldState.infoDialog;
        menuDialog = oldState.menuDialog;
        blackout = oldState.blackout;
        statisticDialog = oldState.statisticDialog;
        handleInfoBlackout = oldState.handleInfoBlackout;
        handleSelectionBlackout = oldState.handleSelectionBlackout;

        timeByTurnLabel = oldState.timeByTurnLabel;
        turnsLabel = oldState.turnsLabel;
        whiteTime = oldState.whiteTime;
        blackTime = oldState.blackTime;
        fewTime = oldState.fewTime;


        menu = oldState.menu;
        settings = oldState.settings;
        controlMenu = oldState.controlMenu;

        content = new Table();
        content.setFillParent(true);
        getStage().addActor(content);

        return true;
    }

    @Override
    public void initListeners() {
        OnChangeListener onMenu = new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                blackout.setVisible(true);
                gameBoard.addBlocked();

                menuDialog = new RdDialogBuilder()
                        .title(strings.get("game_exit"))
                        .text(strings.get("game_exit_question"))
                        .cancel(strings.get("cancel"),
                            (dialog, s) -> {
                            blackout.setVisible(false);
                            gameBoard.addUnblocked();
                            menuDialog.hide();
                            menuDialog = null;
                        })
                        .accept(strings.get("exit"), (dialog, s) ->
                            controller.goToMultiplayerScenario())
                        .build(ChessAssetManager.current().getSkin(), "input");

                menuDialog.getIcon().setDrawable(new TextureRegionDrawable(
                        GrayAssetManager.current().findRegion("icon_conf")));
                menuDialog.getIcon().setScaling(Scaling.fit);
                menuDialog.show(getStage());
                menuDialog.setSize(800, 550);
                resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            }
        };
        ChessApplication.self().getLauncher().setOnFinish(() -> {
            if (menuDialog == null) onMenu.onChange(null);
        });


        if (oldState != null) {
            menu.getListeners().removeIndex(menu.getListeners().size - 1);
            controlMenu.getListeners().removeIndex(controlMenu.getListeners().size - 1);
            settings.getListeners().removeIndex(settings.getListeners().size - 1);
        }
        settings.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                blackout.setVisible(true);
                gameBoard.addBlocked();
                settingsController.showSettings(controller.getCurrentMatch(), dialog -> {
                    blackout.setVisible(false);
                    gameBoard.addUnblocked();
                    dialog.hide();
                });
            }
        });
        menu.addListener(onMenu);
        controlMenu.addListener(onMenu);
    }

    private void handleBlackout() {
        if (Gdx.input.justTouched()) {
            getStage().getViewport().getCamera()
                        .unproject(spriteTouchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));

                if (handleInfoBlackout.get() && !infoSprite.getBoundingRectangle()
                        .contains(spriteTouchPoint.x, spriteTouchPoint.y)) {
                    if (infoDialog != null) {
                        infoDialog.hide();
                        infoDialog = null;
                        gameBoard.addUnblocked();
                    }
                }

                if (handleSelectionBlackout.get() && !selectionSprite.getBoundingRectangle()
                        .contains(spriteTouchPoint.x, spriteTouchPoint.y)) {
                    if (selectionDialog != null) {
                        selectionDialog.getSelectionListener().accept(null);
                    }
                }
            }
    }

    public void showResultDialog(Result result, Account first, Account second) {
        var titlePair = SettingsUtil.defineResult(result);

        resultDialog = new ResultDialog(titlePair.getKey(), titlePair.getValue(),
                ChessAssetManager.current().getResultStyle());
        resultDialog.setOnCancel(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                if (resultDialog != null) resultDialog.hide();
                resultDialog = null;
            }
        });
        resultDialog.getContentTable().align(Align.topLeft).pad(100, 10, 0, 85);

        RdLabel label1;
        Match current = controller.getCurrentMatch();
        // -1 is infinity!
        if (current.getTimeByTurn() != -1 && current.getTimeByTurn() <= 0) {
            label1 = new RdLabel(strings.get("finish_time_by_turn"));
        // -1 is infinity!
        } else if ((current.getTimeByWhite() != -1 && current.getTimeByWhite() <= 0)
            || (current.getTimeByBlack() != -1 && current.getTimeByBlack() <= 0)) {
            label1 = new RdLabel(strings.get("finish_player_time"));
        // -1 is infinity!
        } else if (controller.getCurrentMatch().getMaxTurn() != -1 &&
            controller.getCurrentMatch().getMaxTurn() <= controller.getTurn()) {
            label1 = new RdLabel(strings.get("finish_moves"));
        } else if (result == Result.VICTORY) {
            label1 = new RdLabel(strings.get("finish_victory"));
        } else if (result == Result.DRAWN) {
            label1 = new RdLabel(strings.get("finish_drawn"));
        } else {
            label1 = new RdLabel(strings.get("finish_lose"));
        }
        label1.setColor(titlePair.getValue());
        label1.setWrap(true);

        resultDialog.getContentTable().add(label1).expandX().fillX().row();
        if (result == Result.VICTORY) showVictory(resultDialog, first, second);
        else if (result == Result.DRAWN) showDrawn(resultDialog, first, second);
        else if (result == Result.LOSE) showLose(resultDialog, first, second);
        else throw new IllegalArgumentException("unknown game mode");

        resultDialog.show(getStage());
        resultDialog.setSize(900, 900);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        controller.startResultSound(result);
    }

    public void showSelectionDialog(Consumer<TypePiece> selectionListener) {
        selectionDialog = new MultiplayerSelectionDialog(strings.get("turn_pawn"),
                ChessAssetManager.current().getSelectionStyle(), controller);

        selectionDialog.setSelectionListener(typePiece -> {
            if (selectionDialog == null) return;

            gameBoard.addUnblocked();
            selectionDialog.hide();
            blackout.setVisible(false);
            if (typePiece != null) selectionListener.accept(typePiece);
            selectionDialog = null;
            handleSelectionBlackout.set(false);
        });

        gameBoard.addBlocked();
        blackout.setVisible(true);
        selectionDialog.show(getStage());
        selectionDialog.setSize(700, 550);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        Runnable task = () -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Gdx.app.error("multiplayer selection dialog", RdLogger.self().getDescription(e));
            }
            handleSelectionBlackout.set(true);
        };
        RdApplication.self().execute(task);
    }

    private void showVictory(ResultDialog dialog, Account first, Account second) {
        RankType type = controller.getCurrentMatch().getRankType();
        String rankType = (type == RankType.UNRANKED ? "" : strings.get("rank_type"))
            + SettingsUtil.getRank(controller.getCurrentMatch().getRankType());

        var label2 = new RdLabel("[GREEN]" + rankType);
        var label3 = new RdLabel("1. [_]" + first.getFullName() + "[_]" + ": "
                + "[GREEN]+" +  getRankPlus()
                + "    [GOLD]+" + strings.format("coins", controller.getCurrentMatch().getSponsored()));
        var label4 = new RdLabel("2. [_]" + second.getFullName() + "[_]" + ": "
                + "[RED]-" + getRankMinus());

        dialog.getContentTable().add(label2).expandX().fillX().row();
        dialog.getContentTable().add(label3).expandX().fillX().row();
        dialog.getContentTable().add(label4).expandX().fillX().row();
    }

    private void showDrawn(ResultDialog dialog, Account first, Account second) {
        RankType type = controller.getCurrentMatch().getRankType();
        String rankType = (type == RankType.UNRANKED ? "" : strings.get("rank_type"))
            + SettingsUtil.getRank(controller.getCurrentMatch().getRankType());

        var label1 = new RdLabel("[GREEN]" + rankType);
        var label2 = new RdLabel("2. [_]" + first.getFullName() + "[_]" + ": "
                + "[GREEN]+" + getRankPlus()
                + "    [GOLD]+" + strings.format("coins", controller.getCurrentMatch().getSponsored()));
        var label3 = new RdLabel("2. [_]" + second.getFullName() + "[_]" + ": "
                + "[GREEN]+" + getRankMinus()
                + "    [GOLD]+" + strings.format("coins", controller.getCurrentMatch().getSponsored()));

        dialog.getContentTable().add(label1).expandX().fillX().row();
        dialog.getContentTable().add(label2).expandX().fillX().row();
        dialog.getContentTable().add(label3).expandX().fillX().row();
    }

    private void showLose(ResultDialog dialog, Account first, Account second) {
        RankType type = controller.getCurrentMatch().getRankType();
        String rankType = (type == RankType.UNRANKED ? "" : strings.get("rank_type"))
            + SettingsUtil.getRank(controller.getCurrentMatch().getRankType());

        var label1 = new RdLabel("[GREEN]" + rankType);
        var label2 = new RdLabel("1. [_]" + first.getFullName() + "[_]" + ": "
                + "[GREEN]+" + getRankPlus()
                + "    [GOLD]+" + strings.format("coins", controller.getCurrentMatch().getSponsored()));
        var label3 = new RdLabel("2. [_]" + second.getFullName() + "[_]" + ": "
                + "[RED]-" + getRankMinus());

        dialog.getContentTable().add(label1).expandX().fillX().row();
        dialog.getContentTable().add(label2).expandX().fillX().row();
        dialog.getContentTable().add(label3).expandX().fillX().row();
    }

    private String getRankPlus() {
        double rank = controller.getCurrentMatch().getRankPlus();
        if (rank < 0.01) return "0.01";
        return rankFormat.format(rank);
    }

    private String getRankMinus() {
        double rank = controller.getCurrentMatch().getRankMinus();
        if (rank < 0.01) return "0.01";
        return rankFormat.format(rank);
    }

    private void showInfoDialog() {
        if (!controller.getLocalMatch().isMatchDescription()) return;

        infoDialog = new RdDialog("", ChessAssetManager.current().getInfoStyle());
        var chessLogo = new Image(ChessAssetManager.current().findChessRegion("app_logo"));
        var description = new Table();
        description.add(new RdLabel("Age of Chess II")).row();
        description.add(new RdLabel(controller.defineDefaultGameMode()));

        infoDialog.getContentTable().align(Align.center);
        infoDialog.getContentTable().add(chessLogo);
        infoDialog.getContentTable().add(description).padRight(20);

        gameBoard.addBlocked();
        infoDialog.show(getStage());
        infoDialog.setSize(900, 200);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        Runnable task = () -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Gdx.app.error("multiplayer info 200 millis", RdLogger.self().getDescription(e));
            }
            handleInfoBlackout.set(true);
        };
        RdApplication.self().execute(task);

        // show information about the game 1.5 seconds
        Runnable timer = () -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Gdx.app.error("multiplayer info 1500 millis", RdLogger.self().getDescription(e));
            }
            handleInfoBlackout.set(false);
            RdApplication.postRunnable(() -> {
                if (infoDialog != null) {
                    infoDialog.hide();
                    infoDialog = null;
                    gameBoard.addUnblocked();
                }
            });
        };
        RdApplication.self().execute(timer);
    }

    private void initialize() {
        coefficientX = controller.getLocalMatch().getMatchData().getWidth()
            / controller.getLocalMatch().getMatchData().getHeight();
        coefficientY = controller.getLocalMatch().getMatchData().getWidth()
            / controller.getLocalMatch().getMatchData().getHeight();
    }
}
