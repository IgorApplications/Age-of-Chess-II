package com.iapp.ageofchess.activity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.chess_engine.Result;
import com.iapp.ageofchess.chess_engine.TypePiece;
import com.iapp.ageofchess.controllers.GameController;
import com.iapp.ageofchess.graphics.BoardView;
import com.iapp.ageofchess.graphics.ResultDialog;
import com.iapp.ageofchess.graphics.SelectionDialog;
import com.iapp.ageofchess.modding.GameMode;
import com.iapp.ageofchess.modding.LocalMatch;
import com.iapp.ageofchess.modding.MatchState;
import com.iapp.ageofchess.util.*;
import com.iapp.rodsher.actors.*;
import com.iapp.rodsher.screens.Activity;
import com.iapp.rodsher.screens.RdApplication;
import com.iapp.rodsher.screens.RdLogger;
import com.iapp.rodsher.util.OnChangeListener;
import com.iapp.rodsher.util.WindowUtil;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public abstract class GameActivity extends Activity {

    private static final Vector3 spriteTouchPoint = new Vector3();
    static boolean verticallyMode;

    private GameActivity oldState;
    final GameController controller;
    private ScrollPane scroll;
    private float coefX, coefY;

    BoardView gameBoard;

    // dialog objects
    Sprite infoSprite, selectionSprite;
    SelectionDialog selectionDialog;
    ResultDialog resultDialog;
    RdDialog infoDialog;
    RdDialog menuDialog, replayDialog, statisticDialog;

    Table content;
    RdImageTextButton menu, replay, undo, hint, info;

    Image blackout;
    private AtomicBoolean handleInfoBlackout = new AtomicBoolean(false),
            handleSelectionBlackout = new AtomicBoolean(false);
    private RdLabel blackPawn, blackRook, blackKnight, blackBishop, blackQueen, blackScore,
        whitePawn, whiteRook, whiteKnight, whiteBishop, whiteQueen, whiteScore;

    public static GameActivity newInstance(MatchState state) {
        verticallyMode = Gdx.graphics.getWidth() <= Gdx.graphics.getHeight();
        GameActivity activity;
        if (verticallyMode) {
            activity = new GameActivityV(state);
        } else {
            activity = new GameActivityH(state);
        }


        return activity;
    }

    public static GameActivity newInstance(LocalMatch localMatch) {
        verticallyMode = Gdx.graphics.getWidth() <= Gdx.graphics.getHeight();
        GameActivity activity;
        if (verticallyMode) {
            activity = new GameActivityV(localMatch);
        } else {
            activity = new GameActivityH(localMatch);
        }

        return activity;
    }

    private static GameActivity newInstance(GameActivity oldState, GameController controller) {
        verticallyMode = Gdx.graphics.getWidth() <= Gdx.graphics.getHeight();
        GameActivity activity;
        if (verticallyMode) {
            activity = new GameActivityV(oldState, controller);
        } else {
            activity = new GameActivityH(oldState, controller);
        }

        return activity;
    }

    GameActivity(LocalMatch localMatch) {
        this.controller = new GameController(this, localMatch);
        initialize();
    }

    GameActivity(MatchState state) {
        this.controller = new GameController(this, state);
        initialize();
    }

    GameActivity(GameActivity oldState, GameController controller) {
        this.controller = controller;
        this.oldState = oldState;
        initialize();
    }

    @Override
    public void show(Stage stage) {}

    public void onMadeMove() {
       updateFelledPieces();
    }

    public abstract void onMakeMove();

    @Override
    public void render(float delta) {
        super.render(delta);
        handleBlackout();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (verticallyMode != (width <= height) && width != 0 && height != 0) {
            RdApplication.self().setScreen(newInstance(this, controller));
            dispose();
        }

        WindowUtil.resizeCenter(menuDialog);
        WindowUtil.resizeCenter(replayDialog);
        WindowUtil.resizeCenter(resultDialog);
        WindowUtil.resizeCenter(statisticDialog);
        WindowUtil.resizeCenter(selectionDialog);

        if (infoDialog != null) infoSprite.setBounds(infoDialog.getX(), infoDialog.getY(), infoDialog.getWidth(), infoDialog.getHeight());
        if (selectionDialog != null) selectionSprite.setBounds(selectionDialog.getX(), selectionDialog.getY(), selectionDialog.getWidth(), selectionDialog.getHeight());
        if (scroll != null) scroll.setFadeScrollBars(true);
    }

    @Override
    public void pause() {
        super.pause();
        controller.saveGame();
    }

    @Override
    public void initActors() {
        RdApplication.self().setBackground(controller.getRegion("background"));

        if (restoreState()) return;
        infoSprite = new Sprite();
        selectionSprite = new Sprite();
        gameBoard = new BoardView(controller, getStage());
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
        replay = new RdImageTextButton("");
        replay.setImage("iw_replay");
        replay.getLabelCell().reset();
        undo = new RdImageTextButton("");
        undo.setImage("iw_undo");
        undo.getLabelCell().reset();
        hint = new RdImageTextButton("");
        hint.setImage("iw_hint");
        hint.getLabelCell().reset();
        info = new RdImageTextButton("");
        info.setImage("iw_help");
        info.getLabelCell().reset();
        initFelledPieces();

        showInfoDialog();
        updateFelledPieces();
    }

    void resizeBoard(Cell<BoardView> cell, float rectSize) {
        if (coefX > coefY) {
            cell.width(rectSize);
            cell.height(rectSize / coefX);
        } else {
            cell.width(rectSize / coefY);
            cell.height(rectSize);
        }
    }

    private void updateFelledPieces() {

        var data = controller.getFelledPieces();
        var blackSign = controller.getMatch().getUpperColor() == com.iapp.ageofchess.chess_engine.Color.BLACK ?
                " +" : " -";
        var whiteSign = controller.getMatch().getUpperColor() == com.iapp.ageofchess.chess_engine.Color.BLACK ?
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

        if (controller.getMatch().getUpperColor() == com.iapp.ageofchess.chess_engine.Color.BLACK) {
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
        replayDialog = oldState.replayDialog;
        blackout = oldState.blackout;
        statisticDialog = oldState.statisticDialog;
        handleInfoBlackout = oldState.handleInfoBlackout;
        handleSelectionBlackout = oldState.handleSelectionBlackout;

        blackPawn = oldState.blackPawn;
        blackRook = oldState.blackRook;
        blackKnight = oldState.blackKnight;
        blackBishop = oldState.blackBishop;
        blackQueen = oldState.blackQueen;
        blackScore = oldState.blackScore;

        whitePawn = oldState.whitePawn;
        whiteRook = oldState.whiteRook;
        whiteKnight = oldState.whiteKnight;
        whiteBishop = oldState.whiteBishop;
        whiteQueen = oldState.whiteQueen;
        whiteScore = oldState.whiteScore;

        content = new Table();
        content.setFillParent(true);
        getStage().addActor(content);

        menu = oldState.menu;
        replay = oldState.replay;
        undo = oldState.undo;
        hint = oldState.hint;
        info = oldState.info;

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
                                controller.goToScenario();
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
            replay.getListeners().removeIndex(replay.getListeners().size - 1);
            info.getListeners().removeIndex(info.getListeners().size - 1);
            hint.getListeners().removeIndex(hint.getListeners().size - 1);
            undo.getListeners().removeIndex(undo.getListeners().size - 1);
        }
        menu.addListener(onMenu);

        replay.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                blackout.setVisible(true);
                gameBoard.addBlocked();

                replayDialog = new RdDialogBuilder()
                        .title(strings.get("start_game_again"))
                        .text(strings.get("restart_game_question"))
                        .cancel(strings.get("cancel"), new OnChangeListener() {
                            @Override
                            public void onChange(Actor actor) {
                                blackout.setVisible(false);
                                gameBoard.addUnblocked();
                                replayDialog.hide();
                                replayDialog = null;
                            }
                        })
                        .accept(strings.get("replay"), new OnChangeListener() {
                            @Override
                            public void onChange(Actor actor) {
                                controller.restart();
                            }
                        })
                        .build(ChessAssetManager.current().getSkin(), "input");

                replayDialog.getIcon().setDrawable(new TextureRegionDrawable(
                        GrayAssetManager.current().findRegion("icon_conf")));
                replayDialog.getIcon().setScaling(Scaling.fit);
                replayDialog.show(getStage());
                replayDialog.setSize(800, 550);
                resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            }
        });

        info.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                showFelledPiecesDialog();
            }
        });
        hint.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.showHint();
            }
        });
        undo.addListener(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                controller.undo();
            }
        });

        // Developer functions
        if (ChessApplication.self().getCheats() == Cheats.DEVELOPER)
            getStage().addListener(new InputListener() {
                @Override
                public boolean keyUp(InputEvent event, int keycode) {
                    if (Input.Keys.SPACE == keycode) controller.makeHint();
                    return super.keyUp(event, keycode);
                }
            });
    }

    private void handleBlackout() {
        if (Gdx.input.justTouched()) {
            getStage().getViewport().getCamera()
                        .unproject(spriteTouchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));

                if (handleInfoBlackout.get() && !infoSprite.getBoundingRectangle().contains(spriteTouchPoint.x, spriteTouchPoint.y)) {
                    if (infoDialog != null) {
                        infoDialog.hide();
                        infoDialog = null;
                        gameBoard.addUnblocked();
                    }
                }

                if (handleSelectionBlackout.get() && !selectionSprite.getBoundingRectangle().contains(spriteTouchPoint.x, spriteTouchPoint.y)) {
                    if (selectionDialog != null) {
                        selectionDialog.getSelectionListener().accept(null);
                    }
                }
            }
    }

    public void showResultDialog(Result result) {
        var titlePair = SettingsUtil.defineResult(result);

        resultDialog = new ResultDialog(titlePair.getKey(), titlePair.getValue(),
                ChessAssetManager.current().getResultStyle());
        resultDialog.setOnCancel(new OnChangeListener() {
            @Override
            public void onChange(Actor actor) {
                resultDialog.hide();
                resultDialog = null;
            }
        });
        resultDialog.getContentTable().align(Align.topLeft).pad(150, 10, 0, 85);

        if (result == Result.VICTORY || result == Result.BLACK_VICTORY || result == Result.WHITE_VICTORY) showVictory(resultDialog);
        else if (result == Result.DRAWN) showDrawn(resultDialog);
        else if (result == Result.LOSE) showLose(resultDialog);
        else throw new IllegalArgumentException("unknown game mode");

        resultDialog.show(getStage());
        resultDialog.setSize(900, 900);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        controller.startResultSound(result);
    }

    public void showSelectionDialog(Consumer<TypePiece> selectionListener) {
        selectionDialog = new SelectionDialog(strings.get("turn_pawn"),
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
                Gdx.app.error("selection dialog", RdLogger.getDescription(e));
            }
            handleSelectionBlackout.set(true);
        };
        RdApplication.self().execute(task);
    }

    private void showVictory(ResultDialog dialog) {
        var bestResult = ChessConstants.localData.getBestResultByLevel()
                .get(controller.getMatch().getGameMode());
        var bestResultLabel = new RdLabel("", ChessAssetManager.current().getSkin());
        bestResultLabel.setWrap(true);
        bestResultLabel.setColor(Color.GOLD);

        if (bestResult != Integer.MAX_VALUE && bestResult < controller.getTurn()) {
            bestResultLabel.setText(strings.get("best_result")
                    + " " + strings.format("turns", bestResult));
        } else {
            bestResultLabel.setText(strings.get("your_best_result"));
        }
        Image star1, star2, star3;
        if (controller.getTurn() <= 100) star1 = new Image(ChessAssetManager.current().findChessRegion("star"));
        else star1 = new Image(ChessAssetManager.current().findChessRegion("empty_star"));
        if (controller.getTurn() <= 50) star2 = new Image(ChessAssetManager.current().findChessRegion("star"));
        else star2 = new Image(ChessAssetManager.current().findChessRegion("empty_star"));
        if (controller.getTurn() <= 25) star3 = new Image(ChessAssetManager.current().findChessRegion("star"));
        else star3 = new Image(ChessAssetManager.current().findChessRegion("empty_star"));

        var starTable = new Table();
        starTable.add(star1).padLeft(3).padRight(3);
        starTable.add(star2).padRight(3);
        starTable.add(star3);

        var turnsLabel = new RdLabel(strings.get("passed_level")
                + " " + strings.format("turns", controller.getTurn()));
        turnsLabel.setWrap(true);
        var gameModeLabel = new RdLabel("", ChessAssetManager.current().getSkin());
        gameModeLabel.setWrap(true);
        if (controller.getMatch().getGameMode() == GameMode.TWO_PLAYERS) {
            gameModeLabel.setText(strings.get("win_mode") + " [GOLD]" + controller.defineDefaultGameMode());
        } else {
            gameModeLabel.setText(strings.get("win_ai") + " [GOLD]" + controller.defineDefaultGameMode());
        }

        var reason = new RdLabel(strings.get("enemy_time_over"),
                ChessAssetManager.current().getSkin());
        reason.setWrap(true);

        dialog.getContentTable().add(bestResultLabel).expandX().fillX().row();
        if (controller.isTurnTimeOver() || controller.isPlayerTimeOver()) dialog.getContentTable().add(reason).expandX().fillX().row();
        dialog.getContentTable().add(starTable).left().row();
        dialog.getContentTable().add(turnsLabel).expandX().fillX().row();
        dialog.getContentTable().add(gameModeLabel).expandX().fillX().row();
    }

    private void showDrawn(ResultDialog dialog) {
        var bestResult = ChessConstants.localData.getBestResultByLevel()
                .get(controller.getMatch().getGameMode());

        RdLabel info;
        if (controller.getTurn() == controller.getMatch().getMaxMoves()) info = new RdLabel(strings.get("drawn_max_turns"));
        else info = new RdLabel(strings.get("drawn_info"));
        info.setWrap(true);
        info.setColor(Color.GREEN);

        var bestResultLabel = new RdLabel("");
        bestResultLabel.setWrap(true);
        if (bestResult != Integer.MAX_VALUE) {
            bestResultLabel.setText(strings.get("best_passed_level")
                    +  " " + strings.format("turns", bestResult));
        } else {
            bestResultLabel.setText(strings.get("not_passed_level"));
        }

        var gameModeLabel = new RdLabel("");
        gameModeLabel.setWrap(true);
        if (controller.getMatch().getGameMode() == GameMode.TWO_PLAYERS) {
            gameModeLabel.setText(strings.get("play_mode") + " [GREEN]" + controller.defineDefaultGameMode());
        } else {
            gameModeLabel.setText(strings.get("play_ai") + " [GREEN]" + controller.defineDefaultGameMode());
        }

        dialog.getContentTable().add(info).expandX().fillX().left().row();
        dialog.getContentTable().add(bestResultLabel).expandX().fillX().left().row();
        dialog.getContentTable().add(gameModeLabel).expandX().fillX().left();
    }

    private void showLose(ResultDialog dialog) {
        var bestResult = ChessConstants.localData.getBestResultByLevel()
                .get(controller.getMatch().getGameMode());

        var info = new RdLabel(strings.get("lose_info"),
                ChessAssetManager.current().getSkin());
        info.setWrap(true);
        info.setColor(Color.RED);

        var bestResultLabel = new RdLabel("",
                ChessAssetManager.current().getSkin());
        bestResultLabel.setWrap(true);
        if (bestResult != Integer.MAX_VALUE) {
            bestResultLabel.setText(strings.get("best_passed_level")
                    +  " " + strings.format("turns", bestResult));
        } else {
            bestResultLabel.setText(strings.get("not_passed_level"));
        }

        var gameModeLabel = new RdLabel("", ChessAssetManager.current().getSkin());
        gameModeLabel.setWrap(true);
        if (controller.getMatch().getGameMode() == GameMode.TWO_PLAYERS) {
            gameModeLabel.setText(strings.get("play_mode") + " [RED]" + controller.defineDefaultGameMode());
        } else {
            gameModeLabel.setText(strings.get("play_ai") + " [RED]" + controller.defineDefaultGameMode());
        }

        dialog.getContentTable().add(info).expandX().fillX().left().row();
        dialog.getContentTable().add(bestResultLabel).expandX().fillX().left().row();
        dialog.getContentTable().add(gameModeLabel).expandX().fillX().left();
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
                ChessAssetManager.current().findRegion("icon_info")));
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
        scroll = new RdScrollPane(scrollContent, ChessAssetManager.current().getSkin());
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
        if (!controller.getMatch().isMatchDescription()) return;

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
                Gdx.app.error("info dialog 200 millis", RdLogger.getDescription(e));
            }
            handleInfoBlackout.set(true);
        };
        RdApplication.self().execute(task);

        // show information about the game 1.5 seconds
        Runnable timer = () -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Gdx.app.error("info dialog 1500 millis", RdLogger.getDescription(e));
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
        coefX = controller.getMatch().getMatchData().getWidth()
                / controller.getMatch().getMatchData().getHeight();
        coefY = controller.getMatch().getMatchData().getHeight()
                / controller.getMatch().getMatchData().getWidth();
    }
}
