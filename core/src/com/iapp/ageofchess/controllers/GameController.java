package com.iapp.ageofchess.controllers;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.iapp.ageofchess.activity.GameActivity;
import com.iapp.ageofchess.activity.ScenariosActivity;
import com.iapp.ageofchess.services.SettingsUtil;
import com.iapp.lib.chess_engine.Color;
import com.iapp.lib.chess_engine.Move;
import com.iapp.lib.chess_engine.Result;
import com.iapp.lib.chess_engine.TypePiece;
import com.iapp.lib.ui.widgets.BoardView;
import com.iapp.ageofchess.modding.GameMode;
import com.iapp.ageofchess.modding.LocalMatch;
import com.iapp.ageofchess.modding.MatchState;
import com.iapp.ageofchess.multiplayer.TurnMode;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.ageofchess.services.Sounds;
import com.iapp.lib.ui.screens.RdApplication;
import com.iapp.lib.util.CallListener;
import com.iapp.lib.util.DisposeUtil;
import com.iapp.lib.util.Timer;

public class GameController extends EngineController {

    private BoardView boardView;
    private GameActivity activity;
    private MatchState state;

    private Timer timerByTurn, blackTimer, whiteTimer;
    private boolean modeDone;

    public GameController(GameActivity activity, MatchState state) {
        super(activity, state);
        result = state.getResult();
        modeDone = state.isMoveDone();
        this.state = state;
        initialize();
    }

    public GameController(GameActivity activity, LocalMatch localMatch) {
        super(activity, localMatch);
        initialize();
    }

    public void setBoardView(BoardView boardView) {
        this.boardView = boardView;
        if (result != Result.NONE) boardView.addBlocked();
    }

    public void setActivity(GameActivity activity) {
        this.activity = activity;

        finishGame(result);
        if (localMatch.getGameMode() != GameMode.TWO_PLAYERS
                && getMatch().getUpperColor() == getColorMove()) {
            boardView.setBlockedMove(true);
            // update condition
            activity.onMakeMove();
            makeAIMove();
        }
    }

    private boolean restart;

    public void goToScenario() {
        if (restart) return;
        restart = true;

        stop();
        resetSounds();
        if (getMatch().isFlippedPieces() && getMatch().getGameMode() == GameMode.TWO_PLAYERS) flipPieces();
        startActivity(new ScenariosActivity(),
                Actions.run(() -> DisposeUtil.dispose(localMatch.getMatchData().getAtlas())));
    }

    public void restart() {
        if (restart) return;
        restart = true;

        stop();
        if (getMatch().isFlippedPieces() && getMatch().getGameMode() == GameMode.TWO_PLAYERS) flipPieces();
        resetSounds();
        startActivity(GameActivity.newInstance(localMatch));
    }

    @SuppressWarnings("DefaultLocale")
    public String getTimeByTurn() {
        if (timerByTurn == null) return "";
        var leftSeconds = timerByTurn.getLeftMillis() / 1000;
        var leftMinutes = leftSeconds / 60;
        return String.format("%d:%02d", leftMinutes, leftSeconds % 60);
    }

    @SuppressWarnings("DefaultLocale")
    public String getBlackTime() {
        if (blackTimer == null) return "";
        var leftSeconds = blackTimer.getLeftMillis() / 1000;
        var leftMinutes = leftSeconds / 60;
        var leftHours = leftMinutes / 60;
        return String.format("%d:%02d:%02d", leftHours, leftMinutes % 60, leftSeconds % 60 % 60);
    }

    @SuppressWarnings("DefaultLocale")
    public String getWhiteTime() {
        if (whiteTimer == null) return "";
        var leftSeconds = whiteTimer.getLeftMillis() / 1000;
        var leftMinutes = leftSeconds / 60;
        var leftHours = leftMinutes / 60;
        return String.format("%d:%02d:%02d", leftHours, leftMinutes % 60, leftSeconds % 60 % 60);
    }

    public boolean isFewTimeByTurn() {
        return timerByTurn != null && timerByTurn.getLeftMillis() / 1000 <= 20 && timerByTurn.getLeftMillis() != 0;
    }

    public boolean isFewBlackTime() {
        return blackTimer != null && blackTimer.getLeftMillis() / 1000 <= 60 && blackTimer.getLeftMillis() != 0;
    }

    public boolean isFewWhiteTime() {
        return whiteTimer != null && whiteTimer.getLeftMillis() / 1000 <= 60 && whiteTimer.getLeftMillis() != 0;
    }

    public boolean isTurnTimeOver() {
        return timerByTurn != null && timerByTurn.isTimeOver();
    }

    public boolean isPlayerTimeOver() {
        return (blackTimer != null && blackTimer.isTimeOver())
                || (whiteTimer != null && whiteTimer.isTimeOver());
    }

    @Override
    public void makeMove(Move move, TypePiece updated) {
        // waiting for the end of time for the next move
        if (getMatch().getTurnMode() == TurnMode.ALTERNATELY && timerByTurn != null) {
            boardView.addBlocked();
            modeDone = true;
        } else if (timerByTurn != null) {
            timerByTurn.resetTime(getMatch().getTimeByTurn());
        }

        var castling = isCastleMove(move);
        var callback = (CallListener) () -> {
            if (castling) Sounds.self().playCastle();
            Sounds.self().playMove();
            var position = getCheckKing();
            if (position != null) Sounds.self().playCheck();
            boardView.setBlockedMove(false);

            if (localMatch.getMaxMoves() == getTurn()) {
                result = Result.DRAWN;
                boardView.setBlockedMove(true);
                activity.showResultDialog(Result.DRAWN);
                return;
            }

            result = defineResult();
            if (result != Result.NONE) {
                finishGame(result);
                return;
            }

            // update timers for timed play
            if (blackTimer != null && whiteTimer != null) {
                if (getColorMove() == Color.BLACK) {
                    blackTimer.resume();
                    whiteTimer.pause();
                } else {
                    blackTimer.pause();
                    whiteTimer.resume();
                }
            }

            // update condition
            activity.onMadeMove();
            if (getMatch().getGameMode() != GameMode.TWO_PLAYERS
                    && localMatch.getUpperColor() == getColorMove()) {
                boardView.setBlockedMove(true);
                // update condition
                activity.onMakeMove();
                makeAIMove();
            }
        };

        if (isUpdated(move) && !isAIMakeMove() && !boardView.isBlockedMove()) {
            activity.showSelectionDialog(userTypePiece -> {
                super.makeMove(move, updated);
                update(move, userTypePiece);
                boardView.makeMove(move, castling, true, callback);
            });
            return;
        }

        super.makeMove(move, updated);
        if (updated != null) update(move, updated);
        boardView.makeMove(move, castling, updated != null, callback);
    }

    @Override
    public void stop() {
        super.stop();
        if (timerByTurn != null) timerByTurn.stop();
        if (blackTimer != null) blackTimer.stop();
        if (whiteTimer != null) whiteTimer.stop();
        System.gc();
    }

    public void undo() {
        if (boardView.isBlockedMove() || result != Result.NONE || getMatch().isBlockedHints()) return;

        if (getMatch().getGameMode() == GameMode.TWO_PLAYERS) {
            if (lastMoves.size() < 1) return;
        } else {
            if (lastMoves.size() < 2) return;
            cancelMove();
            boardView.cancelMove();
        }
        undoMove();
        if (timerByTurn != null) timerByTurn.resetTime(getMatch().getTimeByTurn());
    }

    public void showHint() {
        if (boardView.isBlockedMove() || result != Result.NONE || getMatch().isBlockedHints()) return;
        getHint(4, (move, type) -> boardView.showHint(move));
    }

    public void makeHint() {
        if (boardView.isBlocked() || boardView.isBlockedMove() || result != Result.NONE ||
                (getMatch().getGameMode() != GameMode.TWO_PLAYERS && getColorMove() == getMatch().getUpperColor())) return;
        getHint(4, (move, type) -> makeMove(move, null));
    }

    public void flipPieces() {
        var color = localMatch.getUpperColor() == Color.BLACK ? "black_" : "white_";
        getRegion(color + "pawn").flip(false, true);
        getRegion(color + "pawn").flip(false, true);
        getRegion(color + "pawn").flip(false, true);
        getRegion(color + "pawn").flip(false, true);
        getRegion(color + "pawn").flip(false, true);
        getRegion(color + "pawn").flip(false, true);
    }

    public String defineColorMove() {
        if (getColorMove() == Color.WHITE) return strings.get("[i18n]White");
        return strings.get("[i18n]Black");
    }

    public int[] getFelledPieces() {
        int blackPawn = 8, blackRook = 2, blackKnight = 2, blackBishop = 2, blackQueen = 1,
            whitePawn = 8, whiteRook = 2, whiteKnight = 2, whiteBishop = 2, whiteQueen = 1;

        var matrix = getMatrix();
        for (var line : matrix) {
            for (var typePiece : line) {
                if (isPawn(typePiece)) {
                    if (getColor(typePiece) == Color.BLACK) blackPawn--;
                    else whitePawn--;
                } else if (isRook(typePiece)) {
                    if (getColor(typePiece) == Color.BLACK) blackRook--;
                    else whiteRook--;
                } else if (isKnight(typePiece)) {
                    if (getColor(typePiece) == Color.BLACK) blackKnight--;
                    else whiteKnight--;
                } else if (isBishop(typePiece)) {
                    if (getColor(typePiece) == Color.BLACK) blackBishop--;
                    else whiteBishop--;
                } else if (isQueen(typePiece)) {
                    if (getColor(typePiece) == Color.BLACK) blackQueen--;
                    else whiteQueen--;
                }
            }
        }

        int blackScore = blackPawn + blackRook * 5 + blackBishop * 3 + blackKnight * 3 + blackQueen * 9,
            whiteScore = whitePawn + whiteRook * 5 + whiteBishop * 3 + whiteKnight * 3 + whiteQueen * 9;

        return new int[] {
                blackPawn, blackRook, blackKnight, blackBishop, blackQueen, blackScore,
                whitePawn, whiteRook, whiteKnight, whiteBishop, whiteQueen, whiteScore
        };
    }

    public String defineDefaultGameMode() {
        return SettingsUtil.defineGameMode(localMatch.getGameMode());
    }

    public void startResultSound(Result result) {
        if (result == Result.VICTORY || result == Result.BLACK_VICTORY || result == Result.WHITE_VICTORY) {
            Sounds.self().startWin();
        } else {
            Sounds.self().startLose();
        }
    }

    Timer getTimerByTurn() {
        return timerByTurn;
    }

    Timer getBlackTimer() {
        return blackTimer;
    }

    Timer getWhiteTimer() {
        return whiteTimer;
    }

    @Override
    boolean isMoveDone() {
        return modeDone;
    }

    private void initialize() {
        if (localMatch.getGameMode() == GameMode.TWO_PLAYERS) {
            if (!localMatch.isInfiniteTimeByTurn()) launchTurnTimer();
            if (!localMatch.isInfiniteTimeByGame()) launchPlayerTimer();
        }
        if (getMatch().isFlippedPieces() && getMatch().getGameMode() == GameMode.TWO_PLAYERS) flipPieces();
    }

    private void finishGame(Result result) {
        if (result == Result.NONE) return;
        boardView.setBlockedMove(true);
        boolean isRanked = localMatch.getMatchData().isRatingScenario(localMatch.getNumberScenario());
        activity.showResultDialog(result, isRanked);

        if ((result == Result.VICTORY || result == Result.BLACK_VICTORY || result == Result.WHITE_VICTORY)
                // if is ranked match!
                && isRanked) {
            ChessConstants.localData.getBestResultByLevel().put(localMatch.getGameMode(), getTurn());

            if (ChessConstants.localData.getUserLevel() != GameMode.GRADMASTER) {
                ChessConstants.localData.setUserLevel(
                        localMatch.getGameMode() == ChessConstants.localData.getUserLevel() ?
                                GameMode.values()[ChessConstants.localData.getUserLevel().ordinal() + 1]
                                : ChessConstants.localData.getUserLevel());
            }
        }

        if (timerByTurn != null) timerByTurn.pause();
        if (blackTimer != null) blackTimer.pause();
        if (whiteTimer != null) whiteTimer.pause();
    }

    private void undoMove() {
        var lastMove = cancelMove();

        boardView.setBlockedMove(true);
        var callback = (CallListener) () -> {
            Sounds.self().playMove();
            var position = getCheckKing();
            if (position != null) Sounds.self().playCheck();
            boardView.setBlockedMove(false);
            // update condition
            activity.onMadeMove();
        };

        boardView.undoMove(lastMove, isCastleMove(lastMove), true, callback);
    }

    private Result defineResult() {
        if (isFinish()) {
            var position = getCheckKing();
            if (position == null) return Result.DRAWN;
            var kingColor = getColor(position.getKey(), position.getValue());

            if (localMatch.getGameMode() == GameMode.TWO_PLAYERS) {
                if (kingColor == Color.BLACK) return Result.BLACK_VICTORY;
                else return Result.WHITE_VICTORY;
            } else {
                if (kingColor == localMatch.getUpperColor()) return Result.VICTORY;
                else return Result.LOSE;
            }
        }
        return Result.NONE;
    }

    private Result defineResult(Color kingColor) {
        if (localMatch.getGameMode() == GameMode.TWO_PLAYERS) {
            if (kingColor == Color.WHITE) return Result.BLACK_VICTORY;
            else return Result.WHITE_VICTORY;
        } else {
            if (kingColor == localMatch.getUpperColor()) return Result.VICTORY;
            else return Result.LOSE;
        }
    }

    private void resetSounds() {
        Sounds.self().stopWin();
        Sounds.self().stopLose();
        Sounds.self().startBackgroundMusic();
    }

    private void launchTurnTimer() {
        timerByTurn = new Timer(state != null ? state.getTimeByTurn() : getMatch().getTimeByTurn());
        timerByTurn.setOnFinish(() -> {
            if (getMatch().getTurnMode() != TurnMode.ALTERNATELY || !modeDone) {
                if (getColorMove() == Color.BLACK) result = Result.WHITE_VICTORY;
                else result = Result.BLACK_VICTORY;
                finishGame(result);
            } else {
                boardView.addUnblocked();
                timerByTurn.resetTime(getMatch().getTimeByTurn());
                modeDone = false;
            }
        });

        RdApplication.self().execute(timerByTurn);
        timerByTurn.resume();
    }

    private void launchPlayerTimer() {
        blackTimer = new Timer(state != null ? state.getTimeBlack() : getMatch().getTimeByGame());
        blackTimer.setOnFinish(() -> {
            if (getColorMove() == Color.BLACK) result = Result.WHITE_VICTORY;
            else result = Result.BLACK_VICTORY;
            finishGame(result);
        });
        whiteTimer = new Timer(state != null ? state.getTimeWhite() : getMatch().getTimeByGame());
        whiteTimer.setOnFinish(() -> {
            if (getColorMove() == Color.BLACK) result = Result.WHITE_VICTORY;
            else result = Result.BLACK_VICTORY;
            finishGame(result);
        });

        RdApplication.self().execute(blackTimer);
        RdApplication.self().execute(whiteTimer);

        if (getColorMove() == Color.BLACK) {
            blackTimer.resume();
        } else {
            whiteTimer.resume();
        }
    }
}
