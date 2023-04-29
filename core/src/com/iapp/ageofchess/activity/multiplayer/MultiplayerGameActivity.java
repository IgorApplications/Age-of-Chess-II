package com.iapp.ageofchess.activity.multiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.chess_engine.Result;
import com.iapp.ageofchess.chess_engine.TypePiece;
import com.iapp.ageofchess.controllers.multiplayer.MultiplayerGameController;
import com.iapp.ageofchess.graphics.ChatView;
import com.iapp.ageofchess.graphics.MultiplayerBoardView;
import com.iapp.ageofchess.graphics.MultiplayerSelectionDialog;
import com.iapp.ageofchess.graphics.ResultDialog;
import com.iapp.ageofchess.modding.LocalMatch;
import com.iapp.ageofchess.multiplayer.Account;
import com.iapp.ageofchess.multiplayer.AccountType;
import com.iapp.ageofchess.multiplayer.Match;
import com.iapp.ageofchess.multiplayer.MultiplayerEngine;
import com.iapp.ageofchess.util.*;
import com.iapp.rodsher.actors.*;
import com.iapp.rodsher.screens.Activity;
import com.iapp.rodsher.screens.RdApplication;
import com.iapp.rodsher.screens.RdLogger;
import com.iapp.rodsher.util.OnChangeListener;
import com.iapp.rodsher.util.WindowUtil;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public abstract class MultiplayerGameActivity extends Activity {

    private static final Vector3 spriteTouchPoint = new Vector3();
    static boolean verticallyMode;

    private MultiplayerGameActivity oldState;
    final MultiplayerGameController controller;
    private ScrollPane scroll;
    private float coefficientX, coefficientY;

    // labels
    RdLabel timeByTurnLabel, turnsLabel, whiteTime, blackTime;
    boolean fewTime;

    // control buttons
    ChatView chatView;

    // game board
    MultiplayerBoardView gameBoard;

    // dialog objects
    Sprite infoSprite, selectionSprite;
    MultiplayerSelectionDialog selectionDialog;
    ResultDialog resultDialog;
    RdDialog infoDialog;
    RdDialog menuDialog, statisticDialog;

    Table content;
    RdImageTextButton menu;

    Image blackout;
    private AtomicBoolean handleInfoBlackout = new AtomicBoolean(false),
            handleSelectionBlackout = new AtomicBoolean(false);
    private RdLabel blackPawn, blackRook, blackKnight, blackBishop, blackQueen, blackScore,
        whitePawn, whiteRook, whiteKnight, whiteBishop, whiteQueen, whiteScore;

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
    }

    MultiplayerGameActivity(MultiplayerGameActivity oldState, MultiplayerGameController controller) {
        this.controller = controller;
        this.oldState = oldState;
        initialize();
    }

    @Override
    public void show(Stage stage) {}

    public void onMadeMove() {
       updateFelledPieces();
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        handleBlackout();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (verticallyMode != (width <= height) && width != 0 && height != 0) {
            RdApplication.self().setScreen(newInstance(this, controller));
            dispose();
        }

        WindowUtil.resizeCenter(menuDialog);
        WindowUtil.resizeCenter(resultDialog);
        WindowUtil.resizeCenter(statisticDialog);
        WindowUtil.resizeCenter(selectionDialog);

        if (infoDialog != null) infoSprite.setBounds(infoDialog.getX(), infoDialog.getY(), infoDialog.getWidth(), infoDialog.getHeight());
        if (selectionDialog != null) selectionSprite.setBounds(selectionDialog.getX(), selectionDialog.getY(), selectionDialog.getWidth(), selectionDialog.getHeight());
        if (scroll != null) scroll.setFadeScrollBars(true);
    }

    @Override
    public void initActors() {
        RdApplication.self().setBackground(controller.getRegion("background"));

        if (restoreState()) {
            chatView = new ChatView(550, message ->
                    MultiplayerEngine.self().sendLobbyMessage(controller.getMatchId(), message));
            return;
        }
        infoSprite = new Sprite();
        selectionSprite = new Sprite();
        gameBoard = new MultiplayerBoardView(controller, getStage());
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
        initFelledPieces();

        chatView = new ChatView(700, message ->
                MultiplayerEngine.self().sendLobbyMessage(controller.getMatchId(), message));

        timeByTurnLabel = new RdLabel(controller.getTimeByTurn());
        turnsLabel = new RdLabel(controller.getTurn() + ". " + controller.defineColorMove());
        whiteTime = new RdLabel(controller.getWhiteTime());
        blackTime = new RdLabel(controller.getBlackTime());

        showInfoDialog();
        updateFelledPieces();
    }

    private RdTable controlTable;
    private int lastSize;

    // контрольный бокс, обрезание данных с сервера на сервере, автар противника, собственный аватар над лобби,
    // смена кнопок (до начала/после начала)
    public void update() {
        boolean updateLobby = lastSize != controller.getCurrentMatch().getLobby().size();
        lastSize = controller.getCurrentMatch().getLobby().size();
        if (!updateLobby) return;

        chatView.updateMessages(controller.getCurrentMatch().getLobby(),
                "[GREEN]# " + strings.get("online") + controller.getCurrentMatch().getEntered().size());

        if (controlTable != null) {
            if (controller.getCurrentMatch().isStarted()) {
                controlTable.addAction(Actions.moveBy(0.0f, -900, 5));
            }
            updateControlContent();
        }
    }

    private RdImageTextButton join, start;
    private RdSelectBox<String> availableColors;
    private RdTable controlContent;

    void initControlTable() {
        var current = controller.getCurrentMatch();
        if ((current.getWhitePlayerId() != -1 && current.getBlackPlayerId() != -1
                && current.getWhitePlayerId() != ChessConstants.account.getId()
                && current.getBlackPlayerId() != ChessConstants.account.getId())
                || controller.getCurrentMatch().getResult() != Result.NONE || current.isStarted()) {
            return;
        }

        controlTable = new RdTable();
        controlTable.align(Align.bottom);
        controlTable.setFillParent(true);
        getStage().addActor(controlTable);

        controlContent = new RdTable();
        controlContent.align(Align.center);
        controlContent.setBackground(new NinePatchDrawable(
                new NinePatch(ChessApplication.self().getAssetManager().findChessRegion("control_bg"),
                        15,15,15,15)));
        controlContent.pad(5, 5, 5, 5);
        controlTable.add(controlContent).padBottom(20);

        join = new RdImageTextButton("[%125]" + (controller.isInside() ? strings.get("disjoin") : strings.get("join")));
        start = new RdImageTextButton("[%125]" + strings.get("start"), "blue");
        availableColors = new RdSelectBox<>();
        updateControlContent();

        join.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {

                if (!controller.isInside()) {
                    var color = SettingsUtil.defineColor(availableColors.getSelected());
                    MultiplayerEngine.self().join(controller.getMatchId(), color);
                    if (!controller.getCurrentMatch().isRandom()) {
                        controller.update(SettingsUtil.reverse(color));
                    }

                } else {
                    MultiplayerEngine.self().disjoin(controller.getMatchId());
                }

            }
        });

        start.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                int count = 0;
                if (controller.getCurrentMatch().getBlackPlayerId() != -1) count++;
                if (controller.getCurrentMatch().getWhitePlayerId() != -1) count++;

                if (count < 2) {
                    ChessApplication.self().showInfo(strings.get("not_enough_players"));
                    return;
                }
                MultiplayerEngine.self().start(controller.getMatchId());
            }
        });
    }

    private void updateControlContent() {
        controlContent.clear();

        var data = new ArrayList<String>();
        if (controller.getCurrentMatch().getWhitePlayerId() == -1) data.add("[%125]" + strings.get("white"));
        if (controller.getCurrentMatch().getBlackPlayerId() == -1) data.add("[%125]" + strings.get("black"));
        availableColors.setItems(data.toArray(new String[0]));
        join.setText("[%125]" + (controller.isInside() ? strings.get("disjoin") : strings.get("join")));

        controlContent.add(join).padRight(30).padLeft(30).minWidth(300);
        if (controller.isCreator() || ChessConstants.account.getType().ordinal() >= AccountType.MODERATOR.ordinal()) {
            controlContent.add(start).minWidth(300).padRight(30);
        }

        if (!controller.isInside() && !controller.getCurrentMatch().isRandom()) {
            controlContent.add(availableColors).padRight(30);
        }
    }

    void updateLabels() {
        timeByTurnLabel.setText(controller.getTimeByTurn());
        blackTime.setText(controller.getBlackTime());
        whiteTime.setText(controller.getWhiteTime());

        timeByTurnLabel.setColor(Color.WHITE);
        blackTime.setColor(Color.WHITE);
        whiteTime.setColor(Color.WHITE);

        var userColor = controller.getUserColor();
        if (!userColor.isPresent() || controller.getCurrentMatch().isAlternately()) return;
        var color = userColor.get();

        if (!controller.getCurrentMatch().isStarted() && controller.getCurrentMatch().isRandom()) return;
        if (color == com.iapp.ageofchess.chess_engine.Color.WHITE) whiteTime.setColor(Color.GREEN);
        else blackTime.setColor(Color.GREEN);
        if (color == controller.getColorMove()) timeByTurnLabel.setColor(Color.GREEN);

        if (!controller.getCurrentMatch().isStarted()) return;
        if (controller.isFewTimeByTurn() && controller.getColorMove() == color) timeByTurnLabel.setColor(Color.RED);
        if (controller.isFewBlackTime() && color == com.iapp.ageofchess.chess_engine.Color.BLACK) blackTime.setColor(Color.RED);
        if (controller.isFewWhiteTime() && color == com.iapp.ageofchess.chess_engine.Color.WHITE) whiteTime.setColor(Color.RED);

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

    void resizeBoard(Cell<MultiplayerBoardView> cell, float rectSize) {
        if (coefficientX > coefficientY) {
            cell.width(rectSize);
            cell.height(rectSize / coefficientX);
        } else {
            cell.width(rectSize / coefficientY);
            cell.height(rectSize);
        }
    }

    private void updateFelledPieces() {
        var data = controller.getFelledPieces();
        var blackSign = controller.getUpperColor() == com.iapp.ageofchess.chess_engine.Color.BLACK ?
                " +" : " -";
        var whiteSign = controller.getUpperColor() == com.iapp.ageofchess.chess_engine.Color.BLACK ?
                " -" : " +";

        blackPawn.setText(data[0] == 0 ? "0" : blackSign + data[0]);
        blackRook.setText(data[1] == 0 ? "0" : blackSign + data[1]);
        blackKnight.setText(data[2] == 0 ? "0" : blackSign + data[2]);
        blackBishop.setText(data[3] == 0 ? "0" : blackSign + data[3]);
        blackQueen.setText(data[4] == 0 ? "0" : blackSign + data[4]);
        blackScore.setText(strings.get("total") + " " + (data[5] == 0 ? "0" : blackSign + data[5]));

        whitePawn.setText(data[6] == 0 ? "0" : whiteSign + data[6]);
        whiteRook.setText(data[7] == 0 ? "0" : whiteSign + data[7]);
        whiteKnight.setText(data[8] == 0 ? "0" : whiteSign + data[8]);
        whiteBishop.setText(data[9] == 0 ? "0" : whiteSign + data[9]);
        whiteQueen.setText(data[10] == 0 ? "0" : whiteSign + data[10]);
        whiteScore.setText(strings.get("total") + " " + (data[11] == 0 ? "0" : whiteSign + data[11]));
    }

    private void initFelledPieces() {
        blackPawn = new RdLabel("0");
        blackRook = new RdLabel("0");
        blackKnight = new RdLabel("0");
        blackBishop = new RdLabel("0");
        blackQueen = new RdLabel("0");
        blackScore = new RdLabel(strings.get("total") + " 0");

        whitePawn = new RdLabel("0");
        whiteRook = new RdLabel("0");
        whiteKnight = new RdLabel("0");
        whiteBishop = new RdLabel("0");
        whiteQueen = new RdLabel("0");
        whiteScore = new RdLabel(strings.get("total") + " 0");

        if (controller.getUpperColor() == com.iapp.ageofchess.chess_engine.Color.BLACK) {
            blackPawn.setColor(Color.GREEN);
            blackRook.setColor(Color.GREEN);
            blackKnight.setColor(Color.GREEN);
            blackBishop.setColor(Color.GREEN);
            blackQueen.setColor(Color.GREEN);
            blackScore.setColor(Color.GREEN);

            whitePawn.setColor(Color.RED);
            whiteRook.setColor(Color.RED);
            whiteKnight.setColor(Color.RED);
            whiteBishop.setColor(Color.RED);
            whiteQueen.setColor(Color.RED);
            whiteScore.setColor(Color.RED);
        } else {
            blackPawn.setColor(Color.RED);
            blackRook.setColor(Color.RED);
            blackKnight.setColor(Color.RED);
            blackBishop.setColor(Color.RED);
            blackQueen.setColor(Color.RED);
            blackScore.setColor(Color.RED);

            whitePawn.setColor(Color.GREEN);
            whiteRook.setColor(Color.GREEN);
            whiteKnight.setColor(Color.GREEN);
            whiteBishop.setColor(Color.GREEN);
            whiteQueen.setColor(Color.GREEN);
            whiteScore.setColor(Color.GREEN);
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

        blackPawn = oldState.blackPawn;
        blackRook = oldState.blackRook;
        blackKnight = oldState.blackKnight;
        blackBishop = oldState.blackBishop;
        blackQueen = oldState.blackQueen;
        blackScore = oldState.blackScore;

        chatView = oldState.chatView;
        menu = oldState.menu;

        whitePawn = oldState.whitePawn;
        whiteRook = oldState.whiteRook;
        whiteKnight = oldState.whiteKnight;
        whiteBishop = oldState.whiteBishop;
        whiteQueen = oldState.whiteQueen;
        whiteScore = oldState.whiteScore;

        content = new Table();
        content.setFillParent(true);
        getStage().addActor(content);

        return true;
    }

    @Override
    public void initListeners() {
        var onMenu = new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                blackout.setVisible(true);
                gameBoard.addBlocked();

                menuDialog = new RdDialogBuilder()
                        .title(strings.get("game_exit"))
                        .text(strings.get("game_exit_question"))
                        .cancel(strings.get("cancel"), new OnChangeListener() {
                            @Override
                            public void onChange(Actor actor) {
                                blackout.setVisible(false);
                                gameBoard.addUnblocked();
                                menuDialog.hide();
                                menuDialog = null;
                            }
                        })
                        .accept(strings.get("exit"), new OnChangeListener() {
                            @Override
                            public void onChange(Actor actor) {
                                controller.goToMultiplayerScenario();
                            }
                        })
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
        }
        menu.addListener(onMenu);
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
        if (controller.getCurrentMatch().getTimeByTurn() <= 0) {
            label1 = new RdLabel(strings.get("finish_time_by_turn"));
        } else if (controller.getCurrentMatch().getTimeByWhite() <= 0) {
            label1 = new RdLabel(strings.get("finish_player_time"));
        } else if (controller.getCurrentMatch().getMaxTurn() <= controller.getTurn()) {
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
                Gdx.app.error("multiplayer selection dialog", RdLogger.getDescription(e));
            }
            handleSelectionBlackout.set(true);
        };
        RdApplication.self().execute(task);
    }

    private void showVictory(ResultDialog dialog, Account first, Account second) {
        var label2 = new RdLabel("[GREEN]" + strings.get("rank_type")
                + SettingsUtil.getRank(controller.getCurrentMatch().getRankType()));
        var label3 = new RdLabel("1. [_]" + first.getFullName() + "[_]" + ": "
                + "[GREEN]+" + controller.getCurrentMatch().getRankPlus()
                + "    [GOLD]+" + strings.format("coins", controller.getCurrentMatch().getSponsored()));
        var label4 = new RdLabel("2. [_]" + second.getFullName() + "[_]" + ": "
                + "[RED]-" + controller.getCurrentMatch().getRankMinus());

        dialog.getContentTable().add(label2).expandX().fillX().row();
        dialog.getContentTable().add(label3).expandX().fillX().row();
        dialog.getContentTable().add(label4).expandX().fillX().row();
    }

    private void showDrawn(ResultDialog dialog, Account first, Account second) {
        var label1 = new RdLabel("[GREEN]" + strings.get("rank_type")
                + SettingsUtil.getRank(controller.getCurrentMatch().getRankType()));
        var label2 = new RdLabel("2. [_]" + first.getFullName() + "[_]" + ": "
                + "[GREEN]+" + controller.getCurrentMatch().getRankPlus()
                + "    [GOLD]+" + strings.format("coins", controller.getCurrentMatch().getSponsored()));
        var label3 = new RdLabel("2. [_]" + second.getFullName() + "[_]" + ": "
                + "[GREEN]+" + controller.getCurrentMatch().getRankMinus()
                + "    [GOLD]+" + strings.format("coins", controller.getCurrentMatch().getSponsored()));

        dialog.getContentTable().add(label1).expandX().fillX().row();
        dialog.getContentTable().add(label2).expandX().fillX().row();
        dialog.getContentTable().add(label3).expandX().fillX().row();
    }

    private void showLose(ResultDialog dialog, Account first, Account second) {
        var label1 = new RdLabel("[GREEN]" + strings.get("rank_type")
                + SettingsUtil.getRank(controller.getCurrentMatch().getRankType()));
        var label2 = new RdLabel("1. [_]" + first.getFullName() + "[_]" + ": "
                + "[GREEN]+" + controller.getCurrentMatch().getRankPlus()
                + "    [GOLD]+" + strings.format("coins", controller.getCurrentMatch().getSponsored()));
        var label3 = new RdLabel("2. [_]" + second.getFullName() + "[_]" + ": "
                + "[RED]-" + controller.getCurrentMatch().getRankMinus());

        dialog.getContentTable().add(label1).expandX().fillX().row();
        dialog.getContentTable().add(label2).expandX().fillX().row();
        dialog.getContentTable().add(label3).expandX().fillX().row();
    }

    public void showFelledPiecesDialog() {
        statisticDialog = new RdDialog(strings.get("taken_pieces"), ChessAssetManager.current().getSkin());
        statisticDialog.setOnCancel(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                statisticDialog.hide();
                statisticDialog = null;
            }
        });
        statisticDialog.getIcon().setDrawable(new TextureRegionDrawable(
                ChessAssetManager.current().findRegion("ib_info")));
        statisticDialog.getIcon().setScaling(Scaling.fit);

        var blackPawnTxt = controller.getRegion("black_pawn");
        var blackRookTxt = controller.getRegion("black_rook");
        var blackKnightTxt = controller.getRegion("black_knight");
        var blackBishopTxt = controller.getRegion("black_bishop");
        var blackQueenTxt = controller.getRegion("black_queen");

        var whitePawnTxt = controller.getRegion("white_pawn");
        var whiteRookTxt = controller.getRegion("white_rook");
        var whiteKnightTxt = controller.getRegion("white_knight");
        var whiteBishopTxt = controller.getRegion("white_bishop");
        var whiteQueenTxt = controller.getRegion("white_queen");

        var scrollContent = new Table();
        scrollContent.align(Align.topLeft);
        scroll = new ScrollPane(scrollContent, ChessAssetManager.current().getSkin());
        scroll.setScrollingDisabled(false, true);

        statisticDialog.getContentTable().align(Align.topLeft);
        statisticDialog.getContentTable().add(scroll).expand().fillX().align(Align.topLeft);

        scrollContent.add(blackQueen);
        scrollContent.add(new Image(blackQueenTxt)).size(100, 100);
        scrollContent.add(blackBishop);
        scrollContent.add(new Image(blackBishopTxt)).size(100, 100);
        scrollContent.add(blackKnight);
        scrollContent.add(new Image(blackKnightTxt)).size(100, 100);
        scrollContent.add(blackRook);
        scrollContent.add(new Image(blackRookTxt)).size(100, 100);
        scrollContent.add(blackPawn);
        scrollContent.add(new Image(blackPawnTxt)).size(100, 100);

        scrollContent.row();
        scrollContent.add(whiteQueen);
        scrollContent.add(new Image(whiteQueenTxt)).size(100, 100);
        scrollContent.add(whiteBishop);
        scrollContent.add(new Image(whiteBishopTxt)).size(100, 100);
        scrollContent.add(whiteKnight);
        scrollContent.add(new Image(whiteKnightTxt)).size(100, 100);
        scrollContent.add(whiteRook);
        scrollContent.add(new Image(whiteRookTxt)).size(100, 100);
        scrollContent.add(whitePawn);
        scrollContent.add(new Image(whitePawnTxt)).size(100, 100);

        scrollContent.row();
        var blackScoreTable = new Table();
        blackScoreTable.add(blackScore).colspan(9);
        blackScoreTable.add(new Image(blackPawnTxt)).size(100, 100);
        scrollContent.add(blackScoreTable).left().colspan(10);

        scrollContent.row();
        var whiteScoreTable = new Table();
        whiteScoreTable.add(whiteScore).colspan(9);
        whiteScoreTable.add(new Image(whitePawnTxt)).size(100, 100);
        scrollContent.add(whiteScoreTable).left().colspan(10);

        statisticDialog.show(getStage());
        statisticDialog.setSize(800, 550);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
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
                Gdx.app.error("multiplayer info 200 millis", RdLogger.getDescription(e));
            }
            handleInfoBlackout.set(true);
        };
        RdApplication.self().execute(task);

        // show information about the game 1.5 seconds
        Runnable timer = () -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Gdx.app.error("multiplayer info 1500 millis", RdLogger.getDescription(e));
            }
            handleInfoBlackout.set(false);
            Gdx.app.postRunnable(() -> {
                if (infoDialog != null) {
                    infoDialog.hide();
                    infoDialog = null;
                    gameBoard.addUnblocked();
                }
            });
        };
        new Thread(timer).start();
    }

    private void initialize() {
        coefficientX = controller.getLocalMatch().getMatchData().getWidth() / controller.getLocalMatch().getMatchData().getHeight();
        coefficientY = controller.getLocalMatch().getMatchData().getWidth() / controller.getLocalMatch().getMatchData().getHeight();
    }
}
