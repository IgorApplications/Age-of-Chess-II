package com.iapp.ageofchess.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;
import com.iapp.ageofchess.modding.GameMode;
import com.iapp.ageofchess.modding.LocalMatch;
import com.iapp.ageofchess.modding.MatchState;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.ageofchess.services.ChessConstants;
import com.iapp.lib.chess_engine.*;
import com.iapp.lib.util.Pair;
import com.iapp.lib.util.Timer;
import com.iapp.lib.ui.screens.Activity;
import com.iapp.lib.ui.screens.Controller;
import com.iapp.lib.ui.screens.RdLogger;

import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

public abstract class EngineController extends Controller implements Chess2dController {

    private static final char[] piecesFen = {
            'K', 'Q', 'B', 'N', 'R', 'P',
            '1', '1', '1', '1', '1',
            'p', 'r', 'n', 'b', 'q', 'k',
    };

    private static final java.util.Map<Character, Integer> vertical = java.util.Map.of(
            'a', 0, 'b', 1,
            'c', 2, 'd', 3,
            'e', 4, 'f', 5,
            'g', 6, 'h', 7);

    private final Game game;
    private MatchState state;
    protected final LocalMatch localMatch;
    private ChessEngine whiteEngine, blackEngine, hintWhiteEngine, hintBlackEngine;
    private int depth;
    private boolean aiMakeMove;
    final LinkedList<Move> lastMoves = new LinkedList<>();
    Result result = Result.NONE;
    private boolean blockedHint;

    public EngineController(Activity activity, MatchState state) {
        super(activity);
        this.state = state;
        localMatch = state.getMatch();
        game = state.getGame();
        lastMoves.addAll(state.getLastMoves());
        result = state.getResult();
        initChessEngine(localMatch.getGameMode());
        whiteEngine.start();
        blackEngine.start();
        if (hintWhiteEngine != null) hintWhiteEngine.start();
        if (hintBlackEngine != null) hintBlackEngine.start();
    }

    public EngineController(Activity activity, LocalMatch localMatch) {
        super(activity);
        this.localMatch = localMatch;

        var upperColor = localMatch.getUpperColor();
        if (localMatch.isRandomColor()) {
            upperColor = getRandomColor();
        }

        game = new Game(upperColor, localMatch.getMatchData().getScenarios()[localMatch.getNumberScenario()]);
        initChessEngine(localMatch.getGameMode());
        whiteEngine.start();
        blackEngine.start();
        if (hintWhiteEngine != null) hintWhiteEngine.start();
        if (hintBlackEngine != null) hintBlackEngine.start();
    }

    public LocalMatch getMatch() {
        return localMatch;
    }

    public TextureAtlas.AtlasRegion getRegion(byte type) {
        var color = getColor(type) == Color.BLACK ? "black_" : "white_";
        if (isPawn(type)) return getRegion(color + "pawn");
        else if (isRook(type)) return getRegion(color + "rook");
        else if (isKnight(type)) return getRegion(color + "knight");
        else if (isBishop(type)) return getRegion(color + "bishop");
        else if (isQueen(type)) return getRegion(color + "queen");
        else if (isKing(type)) return getRegion(color + "king");
        throw new IllegalArgumentException("unknown type");
    }

    public TextureAtlas.AtlasRegion getRegion(String name) {
        if (localMatch.getMatchData().getAtlas() == null) return ChessAssetManager.current().findChessRegion(name);
        var region = localMatch.getMatchData().getAtlas().findRegion(name);
        if (region == null) return ChessAssetManager.current().findChessRegion(name);
        return region;
    }

    public boolean isAIMakeMove() {
        return aiMakeMove;
    }

    public Array<Move> getMoves(int pieceX, int pieceY) {
        var moves = game.getMoves(pieceX, 7 - pieceY);
        Array<Move> graphicMoves = new Array<>();
        for (Move move : moves) {
            graphicMoves.add(Move.valueOf(move.getPieceX(), 7 - move.getPieceY(),
                    move.getMoveX(), 7 - move.getMoveY()));
        }
        return graphicMoves;
    }

    public byte[][] getMatrix() {
        var gameMatrix = game.getMatrix();
        var matrix = new byte[gameMatrix.length][gameMatrix[0].length];
        matrix[0] = gameMatrix[7];
        matrix[1] = gameMatrix[6];
        matrix[2] = gameMatrix[5];
        matrix[3] = gameMatrix[4];
        matrix[4] = gameMatrix[3];
        matrix[5] = gameMatrix[2];
        matrix[6] = gameMatrix[1];
        matrix[7] = gameMatrix[0];

        return matrix;
    }

    public byte getId(int x, int y) {
        return game.getId(x, 7 - y);
    }

    public Color getColor(int x, int y) {
        return game.getColor(x, 7 - y);
    }

    public Color getColor(byte type) {
        return game.getColor(type);
    }

    public Pair<Integer, Integer> getCheckKing() {
        var pair = game.getCheckKing();
        return pair == null ? null : new Pair<>(pair.getKey(), 7 - pair.getValue());
    }

    public boolean isCastleMove(Move move) {
        return game.isCastleMove(Move.valueOf(
                move.getPieceX(), 7 - move.getPieceY(),
                move.getMoveX(), 7 - move.getMoveY()));
    }

    public boolean isUpdated(Move move) {
        return game.isUpdated(Move.valueOf(
                move.getPieceX(), 7 - move.getPieceY(),
                move.getMoveX(), 7 - move.getMoveY()));
    }

    public boolean isFinish() {
        return game.isFinish();
    }

    public void update(Move move, TypePiece type) {
        if (type == TypePiece.QUEEN)       update(move, BoardMatrix.QUEEN);
        else if (type == TypePiece.BISHOP) update(move, BoardMatrix.BISHOP);
        else if (type == TypePiece.KNIGHT) update(move, BoardMatrix.KNIGHT);
        else if (type == TypePiece.ROOK)   update(move, BoardMatrix.ROOK);
        else throw new IllegalArgumentException("unknown piece type");
    }

    public int getTurn() {
        return game.getTurn();
    }

    public void stop() {
        whiteEngine.stop();
        blackEngine.stop();
        if (hintWhiteEngine != null) hintWhiteEngine.stop();
        if (hintBlackEngine != null) hintBlackEngine.stop();
        saveGame();
    }

    public void saveGame() {
        long timeByTurn  = getTimerByTurn() != null ? getTimerByTurn().getLeftMillis() : -1,
                blackTime = getBlackTimer() != null ? getBlackTimer().getLeftMillis() : -1,
                whiteTime = getWhiteTimer() != null ? getWhiteTimer().getLeftMillis() : -1;

        if (state != null) {
            state.setResult(result);
            state.setLastMoves(lastMoves);
            state.setTimeByTurn(timeByTurn);
            state.setTimeBlack(blackTime);
            state.setTimeWhite(whiteTime);
            return;
        }

        ChessConstants.localData.saveState(
                new MatchState(getMatch(), game, result, lastMoves,
                        blackTime, whiteTime, timeByTurn, isMoveDone()));
    }

    public boolean isCage(byte type) {
        return game.isCage(type);
    }

    @Override
    public Color getColorMove() {
        return game.getColorMove();
    }

    @Override
    public void makeMove(Move move, TypePiece updated) {
        lastMoves.add(move);
        game.makeMove(Move.valueOf(move.getPieceX(), 7 - move.getPieceY(),
            move.getMoveX(), 7 - move.getMoveY()));
    }

    @Override
    public float getPadLeft() {
        return localMatch.getMatchData().getPadLeft();
    }

    @Override
    public float getPadRight() {
        return localMatch.getMatchData().getPadRight();
    }

    @Override
    public float getPadBottom() {
        return localMatch.getMatchData().getPadBottom();
    }

    @Override
    public float getPadTop() {
        return localMatch.getMatchData().getPadTop();
    }

    @Override
    public float getWidth() {
        return localMatch.getMatchData().getWidth();
    }

    @Override
    public float getHeight() {
        return localMatch.getMatchData().getHeight();
    }

    abstract Timer getTimerByTurn();

    abstract Timer getBlackTimer();

    abstract Timer getWhiteTimer();

    abstract boolean isMoveDone();

    boolean isPawn(byte type) {
        return game.isPawn(type);
    }

    boolean isRook(byte type) {
        return game.isRook(type);
    }

    boolean isBishop(byte type) {
        return game.isBishop(type);
    }

    boolean isKnight(byte type) {
        return game.isKnight(type);
    }

    boolean isQueen(byte type) {
        return game.isQueen(type);
    }

    boolean isKing(byte type) {
        return game.isKing(type);
    }

    Move cancelMove() {
        if (lastMoves.isEmpty()) return null;
        game.cancelMove();
        return lastMoves.removeLast();
    }

    void makeAIMove() {
        aiMakeMove = true;
        getEngine().setFen(parseBoardToFen());
        getEngine().getBestMoves(depth, textMove -> {
            try {
                var pair = parsePosition(textMove);
                var move = pair.getKey();

                if (localMatch.getUpperColor() == Color.WHITE) {
                    move = Move.valueOf(move.getPieceX(), 7 - move.getPieceY(),
                            move.getMoveX(), 7 - move.getMoveY());
                }

                makeMove(move, pair.getValue());
                aiMakeMove = false;
            } catch (Throwable t) {
                Gdx.app.error("makeAIMove", RdLogger.self().getDescription(t));
                RdLogger.self().showFatalScreen(t);
            }
        }, 2000);
    }

    void getHint(int depth, OnGettingMove onGettingMove) {
        if (blockedHint) return;

        blockedHint = true;
        getHintEngine().setFen(parseBoardToFen());
        getHintEngine().getBestMoves(depth, textMove -> {
            try {
                var move = parsePosition(textMove).getKey();
                if (localMatch.getUpperColor() == Color.WHITE) {
                    move = Move.valueOf(move.getPieceX(), 7 - move.getPieceY(),
                            move.getMoveX(), 7 - move.getMoveY());
                }

                onGettingMove.onGetting(move, null);
                blockedHint = false;
            } catch (Throwable t) {
                Gdx.app.error("getHint", RdLogger.self().getDescription(t));
                RdLogger.self().showFatalScreen(t);

            }
        }, 0);
    }

    private void update(Move move, byte type) {
        game.updatePawn(move.getMoveX(), 7 - move.getMoveY(), type);
    }

    private Pair<Move, TypePiece> parsePosition(String position) {
        var main = position.split(" ")[0];

        int x = vertical.get(main.charAt(0));
        int y = Integer.parseInt(String.valueOf(main.charAt(1))) - 1;
        int moveX = vertical.get(main.charAt(2));
        int moveY = Integer.parseInt(String.valueOf(main.charAt(3))) - 1;

        TypePiece typePiece = null;
        if (main.length() == 5) typePiece = defineTypePiece(main.charAt(4));

        // The graphical screen is always reversed,
        // but the artificial intelligence does not flip the board.
        // Therefore, a double flip of coordinates!!!
        boolean isKing;
        if (localMatch.getUpperColor() == Color.WHITE) isKing = game.isKing(x, y);
        else isKing = game.isKing(x,  7 - y);

        // Castling transformation from Carballo Engine to Game Api
        if (isKing && (y == 0 || y == 7)) {
            if (moveX == 7 && x == 3) moveX -= 2;
            else if (moveX == 7 && x == 4) moveX--;
            else if (moveX == 0 && x == 4) moveX += 2;
            else if (moveX == 0 && x == 3) moveX++;
        }

        return new Pair<>(Move.valueOf(x, y, moveX, moveY), typePiece);
    }

    private String parseBoardToFen() {
        var fen = new StringBuilder();
        var matrix = game.getMatrix();
        if (localMatch.getUpperColor() == Color.WHITE) flip(matrix);

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

    private void initChessEngine(GameMode gameMode) {

        switch (gameMode) {
            case TWO_PLAYERS:
                whiteEngine = new CarballoChessEngine();
                blackEngine = new CarballoChessEngine();
                return;
            case NOVICE:
                whiteEngine = new IgorChessEngineAdapter();
                blackEngine = new IgorChessEngineAdapter();
                hintWhiteEngine = new CarballoChessEngine();
                hintBlackEngine = new CarballoChessEngine();
                depth = 1;
                return;
            case EASY:
                whiteEngine = new IgorChessEngineAdapter();
                blackEngine = new IgorChessEngineAdapter();
                hintWhiteEngine = new CarballoChessEngine();
                hintBlackEngine = new CarballoChessEngine();
                depth = 2;
                return;
            case AVERAGE:
                whiteEngine = new CarballoChessEngine();
                blackEngine = new CarballoChessEngine();
                depth = 1;
                return;
            case HARD:
                whiteEngine = new CarballoChessEngine();
                blackEngine = new CarballoChessEngine();
                depth = 2;
                return;
            case EPIC:
                whiteEngine = new CarballoChessEngine();
                blackEngine = new CarballoChessEngine();
                depth = 3;
                return;
            case MASTER_CANDIDATE:
                whiteEngine = new CarballoChessEngine();
                blackEngine = new CarballoChessEngine();
                depth = 4;
                return;
            case MASTER:
                whiteEngine = new CarballoChessEngine();
                blackEngine = new CarballoChessEngine();
                depth = 5;
                return;
            case GRADMASTER:
                whiteEngine = new CarballoChessEngine();
                blackEngine = new CarballoChessEngine();
                depth = 8;
                return;
            default:
                throw new IllegalArgumentException("Unknown game mode");
        }
    }

    private void flip(byte[][] matrix) {
        var line1 = matrix[0];
        var line2 = matrix[1];
        var line3 = matrix[2];
        var line4 = matrix[3];
        var line5 = matrix[4];
        var line6 = matrix[5];
        var line7 = matrix[6];
        var line8 = matrix[7];

        matrix[0] = line8;
        matrix[1] = line7;
        matrix[2] = line6;
        matrix[3] = line5;
        matrix[4] = line4;
        matrix[5] = line3;
        matrix[6] = line2;
        matrix[7] = line1;
    }

    private TypePiece defineTypePiece(char pieceFen) {
        switch (Character.toLowerCase(pieceFen)) {
            case 'q': return TypePiece.QUEEN;
            case 'b': return TypePiece.BISHOP;
            case 'n': return TypePiece.KNIGHT;
            case 'r': return TypePiece.ROOK;
        }
        throw new IllegalArgumentException("unknown fen piece");
    }

    private ChessEngine getEngine() {
        if (getColorMove() == Color.WHITE) return whiteEngine;
        return blackEngine;
    }

    private ChessEngine getHintEngine() {
        if (hintWhiteEngine == null && hintBlackEngine == null) return getEngine();
        if (getColorMove() == Color.WHITE) return hintWhiteEngine;
        return hintBlackEngine;
    }

    private Color getRandomColor() {
        var rand = ThreadLocalRandom.current();
        if (rand.nextInt(2) == 0) return Color.WHITE;
        return Color.BLACK;
    }
}
