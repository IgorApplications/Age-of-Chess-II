package com.iapp.ageofchess.controllers.multiplayer;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Json;
import com.iapp.ageofchess.ChessApplication;
import com.iapp.ageofchess.activity.multiplayer.MultiplayerGameActivity;
import com.iapp.ageofchess.activity.multiplayer.MultiplayerMenuActivity;
import com.iapp.ageofchess.chess_engine.*;
import com.iapp.ageofchess.graphics.MultiplayerBoardView;
import com.iapp.ageofchess.modding.GameMode;
import com.iapp.ageofchess.modding.LocalMatch;
import com.iapp.ageofchess.multiplayer.Account;
import com.iapp.ageofchess.multiplayer.Match;
import com.iapp.ageofchess.multiplayer.MultiplayerEngine;
import com.iapp.ageofchess.util.ChessConstants;
import com.iapp.ageofchess.util.SettingsUtil;
import com.iapp.ageofchess.util.Sounds;
import com.iapp.rodsher.screens.RdApplication;
import com.iapp.rodsher.util.CallListener;
import com.iapp.rodsher.util.DisposeUtil;

import java.util.LinkedList;
import java.util.Optional;

public class MultiplayerGameController extends MultiplayerEngineController {

    private MultiplayerBoardView boardView;
    private MultiplayerGameActivity activity;
    private final long matchId;

    private Match currentMatch;
    private Account firstPlayer, secondPlayer;

    public MultiplayerGameController(MultiplayerGameActivity activity, LocalMatch localMatch, Match match) {
        super(activity, localMatch, match);
        this.matchId = match.getId();
        currentMatch = match;
    }

    public boolean isInside() {
        return currentMatch.getBlackPlayerId() == ChessConstants.account.getId()
                || currentMatch.getWhitePlayerId() == ChessConstants.account.getId();
    }

    public Account getFirstPlayer() {
        return firstPlayer;
    }

    /** never own account, either opponent or black */
    public Account getSecondPlayer() {
        return secondPlayer;
    }

    public Match getCurrentMatch() {
        return currentMatch;
    }

    public boolean isCreator() {
        return ChessConstants.account.getId() == currentMatch.getCreatorId();
    }

    public long getMatchId() {
        return matchId;
    }

    public void setBoardView(MultiplayerBoardView boardView) {
        this.boardView = boardView;

        if (currentMatch.getResult() != Result.NONE || (currentMatch.getBlackPlayerId() != ChessConstants.account.getId()
                && currentMatch.getWhitePlayerId() != ChessConstants.account.getId())) {
            boardView.setBlockedMove(true);
        }
    }

    public void setActivity(MultiplayerGameActivity activity) {
        this.activity = activity;

        updateBlockedBoard();
        activity.update();

        // activity not null!
        // Reads data from the server, can not be disabled!
        updateMatchListener();
    }

    public Optional<Color> getUserColor() {
        if (currentMatch.getBlackPlayerId() == ChessConstants.account.getId()) return Optional.of(Color.BLACK);
        else if (currentMatch.getWhitePlayerId() == ChessConstants.account.getId()) return Optional.of(Color.WHITE);
        return Optional.empty();
    }

    public void goToMultiplayerScenario() {
        stop();
        resetSounds();

        startActivityAlpha(new MultiplayerMenuActivity(), ChessConstants.localData.getScreenDuration(),
                Actions.run(() -> DisposeUtil.dispose(localMatch.getMatchData().getAtlas())));
    }

    @SuppressWarnings("DefaultLocale")
    public String getTimeByTurn() {
        if (currentMatch.getTimeByTurn() == -1) return "";
        var leftSeconds = currentMatch.getTimeByTurn() / 1000;
        var leftMinutes = leftSeconds / 60;
        return String.format("%d:%02d", leftMinutes, leftSeconds % 60);
    }

    @SuppressWarnings("DefaultLocale")
    public String getBlackTime() {
        if (currentMatch.getTimeByBlack() == -1) return "";
        var leftSeconds = currentMatch.getTimeByBlack() / 1000;
        var leftMinutes = leftSeconds / 60;
        var leftHours = leftMinutes / 60;
        return String.format("%d:%02d:%02d", leftHours, leftMinutes % 60, leftSeconds % 60 % 60);
    }

    @SuppressWarnings("DefaultLocale")
    public String getWhiteTime() {
        if (currentMatch.getTimeByWhite() == -1) return "";
        var leftSeconds = currentMatch.getTimeByWhite() / 1000;
        var leftMinutes = leftSeconds / 60;
        var leftHours = leftMinutes / 60;
        return String.format("%d:%02d:%02d", leftHours, leftMinutes % 60, leftSeconds % 60 % 60);
    }

    public boolean isFewTimeByTurn() {
        return currentMatch.getTimeByTurn() != -1 && currentMatch.getTimeByTurn() / 1000 <= 20 && currentMatch.getTimeByTurn() > 0;
    }

    public boolean isFewBlackTime() {
        return currentMatch.getTimeByBlack() != -1 && currentMatch.getTimeByBlack() / 1000 <= 60 && currentMatch.getTimeByBlack() > 0;
    }

    public boolean isFewWhiteTime() {
        return currentMatch.getTimeByWhite() != -1 && currentMatch.getTimeByWhite() / 1000 <= 60 && currentMatch.getTimeByWhite() > 0;
    }

    public boolean isTurnTimeOver() {
        return currentMatch.getTimeByTurn() != -1 && currentMatch.getTimeByTurn() <= 0;
    }

    public boolean isPlayerTimeOver() {
        return (currentMatch.getTimeByBlack() != -1 && currentMatch.getTimeByBlack() <= 0)
                || (currentMatch.getTimeByWhite() != -1 && currentMatch.getTimeByWhite() <= 0);
    }

    @Override
    public void makeMove(Move move, TypePiece updated, boolean self) {

        var castling = isCastleMove(move);
        var callback = (CallListener) () -> {
            if (castling) Sounds.self().playCastle();
            Sounds.self().playMove();
            var position = getCheckKing();
            if (position != null) Sounds.self().playCheck();

            boardView.setBlockedMove(self);
            if (result != Result.NONE) finishGame(result);
        };

        // If the figure needs to be updated, then we update it,
        // and then we send it to the server!
        if (isUpdated(move) && !boardView.isBlockedMove()) {
            activity.showSelectionDialog(userTypePiece -> {

                if (self) {
                    var serverMove = move;
                    if (game.getUpper() == Color.WHITE) {
                        serverMove = Move.valueOf(serverMove.getPieceX(), 7 - serverMove.getPieceY(),
                                serverMove.getMoveX(), 7 - serverMove.getMoveY());
                    }

                    MultiplayerEngine.self().makeMove(matchId, getFenMove(serverMove, userTypePiece));
                }

                super.makeMove(move, updated, self);
                update(move, userTypePiece);
                boardView.makeMove(move, castling, true, callback);

            });
            return;
        }

        if (self) {
            var serverMove = move;
            if (game.getUpper() == Color.WHITE) {
                serverMove = Move.valueOf(serverMove.getPieceX(), 7 - serverMove.getPieceY(),
                        serverMove.getMoveX(), 7 - serverMove.getMoveY());
            }

            MultiplayerEngine.self().makeMove(matchId, getFenMove(serverMove, updated));
        }

        super.makeMove(move, updated, self);
        if (updated != null) update(move, updated);
        boardView.makeMove(move, castling, updated != null, callback);
    }

    @Override
    public void stop() {
        super.stop();
        System.gc();
        ChessApplication.self().getAccountPanel().update(
                ChessApplication.self().getAccountPanel().getAvatarView());
        MultiplayerEngine.self().setOnUpdateMatch(-1, null);
    }

    private void finishGame(Result result) {
        // Avoid displaying a dialog box multiple times
        if (this.result == result) return;
        this.result = result;
        boardView.setBlockedMove(true);

        Color userColor;
        if (!getUserColor().isPresent()) {
            if (result == Result.BLACK_VICTORY) userColor = Color.BLACK;
            else userColor = Color.WHITE;
        } else {
            userColor = getUserColor().get();
        }

        Account blackPlayer, whitePlayer;
        if (firstPlayer.getId() == currentMatch.getBlackPlayerId()) {
            blackPlayer = firstPlayer;
            whitePlayer = secondPlayer;
        } else {
            blackPlayer = secondPlayer;
            whitePlayer = firstPlayer;
        }

        Account self, second;
        if (userColor == Color.BLACK) {
            self = blackPlayer;
            second = whitePlayer;
        } else {
            self = whitePlayer;
            second = blackPlayer;
        }

        if ((result == Result.BLACK_VICTORY && userColor == Color.BLACK)
                || (result == Result.WHITE_VICTORY && userColor == Color.WHITE)) {
            activity.showResultDialog(Result.VICTORY, self, second);
        } else if (result == Result.DRAWN) {
            activity.showResultDialog(Result.DRAWN, firstPlayer, secondPlayer);
        } else {
            activity.showResultDialog(Result.LOSE, second, self);
        }
    }

    private void resetSounds() {
        Sounds.self().stopWin();
        Sounds.self().stopLose();
        Sounds.self().startBackgroundMusic();
    }

    private Match last;
    private boolean flipped;

    public void update(Match last) {
        activity.update();

        if (!flipped && currentMatch.isRandom()
                  && getUserColor().isPresent() && currentMatch.isStarted()) {
            update(SettingsUtil.reverse(getUserColor().get()));
            flipped = true;
         }

        if (currentMatch.getResult() != Result.NONE
                && firstPlayer != null && secondPlayer != null) {
            finishGame(currentMatch.getResult());
        }

        // avoiding calls to the same moves
        if (this.last == last) return;

        if (!last.getFen().equals(currentMatch.getFen()) && (ChessConstants.account.getId() == currentMatch.getWhitePlayerId()
                || ChessConstants.account.getId() == currentMatch.getBlackPlayerId())) {

            Color userColor;
            if (currentMatch.getBlackPlayerId() == ChessConstants.account.getId()) userColor = Color.BLACK;
            else userColor = Color.WHITE;

            var game = new Game(Color.BLACK, currentMatch.getFen());
            lastMoves = new LinkedList<>(currentMatch.getMoves());

            if (!currentMatch.getMoves().isEmpty()) {

                var lastMove = currentMatch.getMoves().get(currentMatch.getMoves().size() - 1);
                var key = lastMove.getKey();

                if (this.game.getUpper() == Color.WHITE) {
                    key = Move.valueOf(key.getPieceX(), 7 - key.getPieceY(),
                            key.getMoveX(), 7 - key.getMoveY());
                }
                var color = game.getColorMove();

                if (color == userColor) {
                    this.last = last;
                    // unblock
                    makeMove(key, lastMove.getValue(), false);
                }

            }

        }

        updateBlockedBoard();
    }

    private void updateBlockedBoard() {

        if (currentMatch.isStarted() && currentMatch.getResult() == Result.NONE) {
            boardView.setBlockedMove((getColorMove() != Color.BLACK || currentMatch.getBlackPlayerId() != ChessConstants.account.getId())
                    && (getColorMove() != Color.WHITE || currentMatch.getWhitePlayerId() != ChessConstants.account.getId()));
        } else {
            boardView.setBlockedMove(true);
        }

        if (currentMatch.getResult() == Result.NONE && currentMatch.isAlternately()) {
            boardView.setBlockedMove(true);
        }

    }

    public void update(Color upper) {
        game.updateUpperColor(upper);
        boardView.update();
    }

    // --------------------------------------------------------------------------------------------------------------

    private void updateMatchListener() {
        MultiplayerEngine.self().setOnUpdateMatch(matchId, newMatch -> {
            var last = currentMatch;

            if (newMatch != null) currentMatch = newMatch;
            update(last);

            long firstId, secondId;
            if (ChessConstants.account.getId() != currentMatch.getBlackPlayerId()) {
                firstId = currentMatch.getWhitePlayerId();
                secondId = currentMatch.getBlackPlayerId();
            } else {
                firstId = currentMatch.getBlackPlayerId();
                secondId = currentMatch.getWhitePlayerId();
            }

            updateAccounts(firstId, secondId, last);
        });
    }

    private void updateAccounts(long first, long second, Match last) {


        if (first != -1) {
            MultiplayerEngine.self().getAccount(first, account ->
                    RdApplication.postRunnable(() -> {
                        firstPlayer = account;
                        update(last);
                    }));
        } else {
            firstPlayer = null;
        }

        if (second != -1) {
            MultiplayerEngine.self().getAccount(second, account ->
                    RdApplication.postRunnable(() -> {
                        secondPlayer = account;
                        update(last);
                    }));
        } else  {
            secondPlayer = null;
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
























    public void undo() {
        if (boardView.isBlockedMove() || result != Result.NONE || localMatch.isBlockedHints()) return;

        if (localMatch.getGameMode() == GameMode.TWO_PLAYERS) {
            if (lastMoves.size() < 1) return;
        } else {
            if (lastMoves.size() < 2) return;
            cancelMove();
            boardView.cancelMove();
        }
        undoMove();
    }

    public void showHint() {
        if (boardView.isBlockedMove() || result != Result.NONE || localMatch.isBlockedHints()) return;
        getHint(4, (move, type) -> boardView.showHint(move));
    }

    public void makeHint() {
        if (boardView.isBlocked() || boardView.isBlockedMove() || result != Result.NONE ||
                (localMatch.getGameMode() != GameMode.TWO_PLAYERS && getColorMove() == localMatch.getUpperColor())) return;
        getHint(4, (move, type) -> makeMove(move, null, false));
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
        if (getColorMove() == Color.WHITE) return strings.get("white_upper");
        return strings.get("black_upper");
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
        if (localMatch.getGameMode() == GameMode.TWO_PLAYERS) return strings.get("two_players");
        else if (localMatch.getGameMode() == GameMode.NOVICE) return strings.get("novice");
        else if (localMatch.getGameMode() == GameMode.EASY) return strings.get("easy");
        else if (localMatch.getGameMode() == GameMode.AVERAGE) return strings.get("average");
        else if (localMatch.getGameMode() == GameMode.HARD) return strings.get("hard");
        else if (localMatch.getGameMode() == GameMode.EPIC) return strings.get("epic");
        else if (localMatch.getGameMode() == GameMode.MASTER_CANDIDATE) return strings.get("candidate_master");
        else if (localMatch.getGameMode() == GameMode.MASTER) return strings.get("master");
        else if (localMatch.getGameMode() == GameMode.GRADMASTER) return strings.get("grandmaster");
        else if (localMatch.getGameMode() == GameMode.MULTIPLAYER) return strings.get("multiplayer");
        else throw new IllegalArgumentException("unknown game mode");
    }

    public void startResultSound(Result result) {
        if (result == Result.VICTORY || result == Result.BLACK_VICTORY || result == Result.WHITE_VICTORY) {
            Sounds.self().startWin();
        } else {
            Sounds.self().startLose();
        }
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

        boardView.undoMove(lastMove.getKey(), isCastleMove(lastMove.getKey()), true, callback);
    }
}