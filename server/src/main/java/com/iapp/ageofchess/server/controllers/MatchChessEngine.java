package com.iapp.ageofchess.server.controllers;

import com.iapp.lib.web.Account;
import com.iapp.ageofchess.multiplayer.Match;
import com.iapp.lib.web.RankType;
import com.iapp.ageofchess.multiplayer.TurnMode;
import com.iapp.lib.web.RequestStatus;
import com.iapp.ageofchess.server.dao.AccountDAO;
import com.iapp.lib.chess_engine.*;
import com.iapp.lib.util.ELOCalculator;
import com.iapp.lib.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatchChessEngine {

    private static final Logger matchChessEngineLogger = LoggerFactory.getLogger(MatchChessEngine.class);

    private static final java.util.Map<Character, Integer> vertical = java.util.Map.of(
            'a', 0, 'b', 1,
            'c', 2, 'd', 3,
            'e', 4, 'f', 5,
            'g', 6, 'h', 7);

    private static final char[] piecesFen = {
            'K', 'Q', 'B', 'N', 'R', 'P',
            '1', '1', '1', '1', '1',
            'p', 'r', 'n', 'b', 'q', 'k',
    };

    private final AccountDAO accountDAO;
    private final Match match;
    private final Game game;
    private final long defTime, defTimeByTurn;

    private long lastUpdateTime, lastTurnUpdateTime;

    public MatchChessEngine(Match match, AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
        this.match = match;
        game = new Game(Color.BLACK, match.getFen());
        // should be equal
        defTime = Math.min(match.getTimeByBlack(), match.getTimeByWhite());
        defTimeByTurn = match.getTimeByTurn();
    }

    public Match getMatch() {
        synchronized (match) {
            return match;
        }
    }

    // thread1
    public void start() {
        synchronized (match) {
            if (match.isStarted()) return;

            match.setStarted(true);
            lastUpdateTime = System.currentTimeMillis();
            lastTurnUpdateTime = System.currentTimeMillis();
        }
    }

    // waiting for the end of the time of each turn
    private boolean alternately;

    // thread1
    public RequestStatus makeMove(String fenMove) {
        synchronized (match) {

            if (match.getResult() != Result.NONE || (defTimeByTurn != -1 && alternately))
                return RequestStatus.DENIED;
            match.setTurn(game.getTurn());

            Pair<Move, TypePiece> pair;
            try {
                pair = getMove(fenMove);
            } catch (Throwable t) {
                t.printStackTrace();
                return RequestStatus.DENIED;
            }

            if (defTimeByTurn != -1 && match.getTurnMode() == TurnMode.ALTERNATELY) alternately = true;

            Move move = pair.getKey();
            var normalMove = Move.valueOf(move.getPieceX(), 7 - move.getPieceY(),
                    move.getMoveX(), 7 - move.getMoveY());
            var updated = game.isUpdated(normalMove);

            if (!game.getMoves(move.getPieceX(), 7 - move.getPieceY())
                    .contains(normalMove, false) || updated == (pair.getValue() == null)) {
                return RequestStatus.DENIED;
            }

            match.getMoves().add(pair);

            game.makeMove(Move.valueOf(move.getPieceX(), 7 - move.getPieceY(),
                    move.getMoveX(), 7 - move.getMoveY()));
            if (updated) {
                game.updatePawn(pair.getKey().getMoveX(), pair.getKey().getMoveY(),
                        typeToByte(pair.getValue()));
            }

            if (pair.getValue() != null) update(move, pair.getValue());

            if (match.getResult() == Result.NONE) {
                // automatically calls finish game
                match.setResult(defineResult());
            }

            lastUpdateTime = System.currentTimeMillis();
            lastTurnUpdateTime = System.currentTimeMillis();

            // for client
            if (!alternately) match.setTimeByTurn(defTimeByTurn);
            match.setFen(parseBoardToFen());
            match.setAlternately(alternately);
            matchChessEngineLogger.info("make move " + pair + " in match - " + match);

            return RequestStatus.DONE;
        }

    }

    // thread2
    public void updateTimer() {
        synchronized (match) {

            if (!match.isStarted() || match.getResult() != Result.NONE) return;

            if (defTime != -1) {
                if (game.getColorMove() == Color.BLACK) {
                    match.setTimeByBlack(match.getTimeByBlack() - (System.currentTimeMillis() - lastUpdateTime));
                    lastUpdateTime = System.currentTimeMillis();

                    if (match.getTimeByBlack() <= 0) {
                        match.setResult(Result.WHITE_VICTORY);
                        finishGame(match.getWhitePlayerId(), match.getBlackPlayerId());
                    }

                } else {
                    match.setTimeByWhite(match.getTimeByWhite() - (System.currentTimeMillis() - lastUpdateTime));
                    lastUpdateTime = System.currentTimeMillis();

                    if (match.getTimeByWhite() <= 0) {
                        match.setResult(Result.WHITE_VICTORY);
                        finishGame(match.getWhitePlayerId(), match.getBlackPlayerId());
                    }

                }
            }

            if (defTimeByTurn != -1) {
                match.setTimeByTurn(match.getTimeByTurn() - (System.currentTimeMillis() - lastTurnUpdateTime));
                lastTurnUpdateTime = System.currentTimeMillis();

                if (match.getTimeByTurn() <= 0) {

                    if (match.getTurnMode() == TurnMode.ALTERNATELY && alternately) {
                        // The move is made and the time is up, restart the timer
                        alternately = false;
                        // for client
                        match.setAlternately(false);
                        match.setTimeByTurn(defTimeByTurn);
                    } else {
                        if (game.getColorMove() == Color.BLACK) {
                            match.setResult(Result.WHITE_VICTORY);
                            finishGame(match.getWhitePlayerId(), match.getBlackPlayerId());
                        } else {
                            match.setResult(Result.BLACK_VICTORY);
                            finishGame(match.getBlackPlayerId(), match.getWhitePlayerId());
                        }
                    }

                }

            }

        }
    }

    private void update(Move move, TypePiece type) {
        if (type == TypePiece.QUEEN)       update(move, BoardMatrix.QUEEN);
        else if (type == TypePiece.BISHOP) update(move, BoardMatrix.BISHOP);
        else if (type == TypePiece.KNIGHT) update(move, BoardMatrix.KNIGHT);
        else if (type == TypePiece.ROOK)   update(move, BoardMatrix.ROOK);
        else throw new IllegalArgumentException("unknown piece type");
    }

    private void update(Move move, byte type) {
        game.updatePawn(move.getMoveX(), 7 - move.getMoveY(), type);
    }

    private Pair<Move, TypePiece> getMove(String fenMove) {
        var arr = fenMove.split(" ");
        var main = arr[0];

        int x = vertical.get(main.charAt(0));
        int y = Integer.parseInt(String.valueOf(main.charAt(1))) - 1;
        int moveX = vertical.get(main.charAt(2));
        int moveY = Integer.parseInt(String.valueOf(main.charAt(3))) - 1;

        TypePiece typePiece = null;
        if (arr.length > 1 && arr[1].length() >= 1) {
            typePiece = defineTypePiece(Character.toLowerCase(arr[1].charAt(0)));
        }

        return new Pair<>(Move.valueOf(x, y, moveX, moveY), typePiece);
    }

    private TypePiece defineTypePiece(char pieceFen) {
        switch (Character.toLowerCase(pieceFen)) {
            case 'q': return TypePiece.QUEEN;
            case 'b': return TypePiece.BISHOP;
            case 'n': return TypePiece.KNIGHT;
            case 'r': return TypePiece.ROOK;
        }

        matchChessEngineLogger.error("unknown fen piece");
        return null;
    }

    private byte typeToByte(TypePiece typePiece) {
        switch (typePiece) {
            case ROOK: return BoardMatrix.ROOK;
            case BISHOP: return BoardMatrix.BISHOP;
            case KNIGHT: return BoardMatrix.KNIGHT;
            case QUEEN: return BoardMatrix.QUEEN;
        }

        matchChessEngineLogger.error("unknown byte piece");
        return 0;
    }

    private Result defineResult() {
        // match.getMaxTurn() == -1 - its infinity
        if (match.getMaxTurn() != -1 && game.getTurn() >= match.getMaxTurn()) {
            finishGame();
            return Result.DRAWN;
        }

        if (game.isFinish()) {
            var position = game.getCheckKing();
            if (position == null) {
                finishGame();
                return Result.DRAWN;
            }
            var kingColor = game.getColor(position.getKey(), position.getValue());

            if (kingColor != Color.BLACK) {
                finishGame(match.getBlackPlayerId(), match.getWhitePlayerId());
                return Result.BLACK_VICTORY;
            } else {
                finishGame(match.getWhitePlayerId(), match.getBlackPlayerId());
                return Result.WHITE_VICTORY;
            }
        }

        return Result.NONE;
    }

    private void finishGame(long winnerId, long secondPlayerId) {
        // result already sets in makeMove!

        Pair<RequestStatus, Account> winner = accountDAO.getAccount(winnerId);
        Pair<RequestStatus, Account> secondPlayer = accountDAO.getAccount(secondPlayerId);
        ELOCalculator calculator = ELOCalculator.getInstance();

        if (winner.getKey() != RequestStatus.DONE) {
            matchChessEngineLogger.error("Wrong winner player, status - " + winner.getKey());
            return;
        }

        if (secondPlayer.getKey() != RequestStatus.DONE) {
            matchChessEngineLogger.error("Wrong second player, status - " + secondPlayer.getKey());
            return;
        }
        Account singleWinner = winner.getValue();
        Account singlePlayer = secondPlayer.getValue();

        singleWinner.setCoins(singleWinner.getCoins() + match.getSponsored());
        double rankPlus = 0, rankMinus = 0;
        switch (match.getRankType()) {
            case BULLET: {
                Pair<Double,Double> newRank = calculator.calculateELO(
                        singleWinner.getBullet(), singlePlayer.getBullet());
                rankPlus = newRank.getKey() - singleWinner.getBullet();
                rankMinus = newRank.getValue() - singlePlayer.getBullet();
                singleWinner.setBullet(newRank.getKey());
                singlePlayer.setBullet(newRank.getValue());
                break;
            }
            case BLITZ: {
                Pair<Double,Double> newRank = calculator.calculateELO(
                        singleWinner.getBlitz(), singlePlayer.getBlitz());
                rankPlus = newRank.getKey() - singleWinner.getBlitz();
                rankMinus = newRank.getValue() - singlePlayer.getBlitz();
                singleWinner.setBlitz(newRank.getKey());
                singlePlayer.setBlitz(newRank.getValue());
                break;
            }
            case RAPID: {
                Pair<Double,Double> newRank = calculator.calculateELO(
                        singleWinner.getRapid(), singlePlayer.getRapid());
                rankPlus = newRank.getKey() - singleWinner.getRapid();
                rankMinus = newRank.getValue() - singlePlayer.getRapid();
                singleWinner.setRapid(newRank.getKey());
                singlePlayer.setRapid(newRank.getValue());
                break;
            }
            case LONG: {
                Pair<Double,Double> newRank = calculator.calculateELO(
                        singleWinner.getLongRank(), singlePlayer.getLongRank());
                rankPlus = newRank.getKey() - singleWinner.getLongRank();
                rankMinus = newRank.getValue() - singlePlayer.getLongRank();
                singleWinner.setLongRank(newRank.getKey());
                singlePlayer.setLongRank(newRank.getValue());
                break;
            }
        }

        if (match.getRankType() != RankType.UNRANKED) {
            match.setRankPlus(Math.abs(rankPlus));
            match.setRankMinus(Math.abs(rankMinus));
        }
        accountDAO.updateServerAccount(singleWinner);
        accountDAO.updateServerAccount(singlePlayer);
        matchChessEngineLogger.info("Match id = " + match.getId() + " finished!");
    }

    private void finishGame() {
        // result already sets in makeMove!

        Pair<RequestStatus, Account> firstPlayer = accountDAO.getAccount(match.getWhitePlayerId());

        Pair<RequestStatus, Account> secondPlayer = accountDAO.getAccount(match.getBlackPlayerId());

        if (firstPlayer.getKey() != RequestStatus.DONE) {
            matchChessEngineLogger.error("Wrong first player, status - " + firstPlayer.getKey());
            return;
        }

        if (secondPlayer.getKey() != RequestStatus.DONE) {
            matchChessEngineLogger.error("Wrong second player, status - " + secondPlayer.getKey());
            return;
        }

        firstPlayer.getValue().setCoins(firstPlayer.getValue().getCoins() + match.getSponsored() / 2);
        secondPlayer.getValue().setCoins(secondPlayer.getValue().getCoins() + match.getSponsored() / 2);

        accountDAO.updateServerAccount(firstPlayer.getValue());
        accountDAO.updateServerAccount(secondPlayer.getValue());
        match.setFinishTime(System.currentTimeMillis());
        matchChessEngineLogger.info("Match id = " + match.getId() + " finished!");
    }

    private String parseBoardToFen() {
        StringBuilder fen = new StringBuilder();
        byte[][] matrix = game.getMatrix();

        for (byte[] line : matrix) {
            for (byte pieceType : line) {
                fen.append(piecesFen[pieceType + 8]);
            }
            fen.append("/");
        }

        if (game.getColorMove() == Color.WHITE) fen.append(" w");
        else fen.append(" b");

        fen.append(" ");
        if (!game.isWhiteKingMadeMove()) {
            if (!game.isLeftWhiteRookMadeMove()) {
                fen.append("Q");
            }
            if (!game.isRightWhiteRookMadeMove()) {
                fen.append("K");
            }
        }
        if (!game.isBlackKingMadeMove()) {
            if (!game.isLeftBlackRookMadeMove()) {
                fen.append("q");
            }
            if (!game.isRightBlackRookMadeMove()) {
                fen.append("k");
            }
        }

        return fen.toString();
    }
}
