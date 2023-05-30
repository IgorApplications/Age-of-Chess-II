package com.iapp.ageofchess.controllers.multiplayer;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;
import com.iapp.ageofchess.modding.LocalMatch;
import com.iapp.ageofchess.multiplayer.Match;
import com.iapp.ageofchess.services.ChessAssetManager;
import com.iapp.lib.chess_engine.*;
import com.iapp.lib.ui.screens.Activity;
import com.iapp.lib.ui.screens.Controller;
import com.iapp.lib.util.Pair;

import java.util.LinkedList;

public abstract class MultiplayerEngineController extends Controller implements Chess2dController {

    private static final java.util.Map<Integer, Character> verticalInt = java.util.Map.of(
            0, 'a', 1, 'b',
            2,'c',  3,'d',
            4,'e',  5,'f',
            6,'g',  7, 'h');

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

    Game game;
    protected final LocalMatch localMatch;
    LinkedList<Pair<Move, TypePiece>> lastMoves;
    Result result = Result.NONE;

    public MultiplayerEngineController(Activity activity, LocalMatch localMatch, Match match) {
        super(activity);
        this.localMatch = localMatch;
        game = new Game(Color.BLACK, match.getFen());
        lastMoves = new LinkedList<>(match.getMoves());
    }

    public LocalMatch getLocalMatch() {
        return localMatch;
    }

    public Color getUpperColor() {
        return game.getUpper();
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

    public Array<Move> getMoves(int pieceX, int pieceY) {
        var moves = game.getMoves(pieceX, 7 - pieceY);
        Array<Move> graphicMoves = new Array<>();
        for (Move move : moves) {
            graphicMoves.add(Move.valueOf(move.getPieceX(), 7 - move.getPieceY(),
                    move.getMoveX(), 7 - move.getMoveY()));
        }
        return graphicMoves;
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

    public Color getColorMove() {
        return game.getColorMove();
    }

    public void stop() {}

    public boolean isCage(byte type) {
        return game.isCage(type);
    }

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

    void makeMove(Move move, TypePiece updated, boolean self) {
        game.makeMove(Move.valueOf(move.getPieceX(), 7 - move.getPieceY(),
                move.getMoveX(), 7 - move.getMoveY()));
    }

    Pair<Move, TypePiece> cancelMove() {
        if (lastMoves.isEmpty()) return null;
        game.cancelMove();
        return lastMoves.removeLast();
    }

    void makeAIMove() {}

    void getHint(int depth, OnGettingMove onGettingMove) {}

    String getFenMove(Move move, TypePiece typePiece) {
        String transition = "";

        transition += verticalInt.get((int) move.getPieceX());
        transition += String.valueOf(move.getPieceY() + 1);
        transition += verticalInt.get((int) move.getMoveX());
        transition += String.valueOf(move.getMoveY() + 1);

        if (typePiece != null ) {
            transition += " " + getFen(typePiece);
        }

        return transition;
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

    private char getFen(TypePiece typePiece) {
        char fen;

        if (typePiece == TypePiece.KNIGHT) {
            fen = 'n';
        } else if (typePiece == TypePiece.ROOK) {
            fen = 'r';
        } else if (typePiece == TypePiece.BISHOP) {
            fen = 'b';
        } else if (typePiece == TypePiece.QUEEN) {
            fen = 'q';
        } else {
            throw new IllegalArgumentException("unknown type piece");
        }

        if (game.getColorMove() == Color.WHITE) {
            fen = Character.toUpperCase(fen);
        }
        return fen;
    }
}
