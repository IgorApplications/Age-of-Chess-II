package com.iapp.lib.chess_engine;

import com.badlogic.gdx.utils.Array;
import com.iapp.lib.util.Pair;

import java.util.*;

/**
 * Backwards compatible!
 * @version 1.0
 * @author Igor Ivanov
 * */
public class Game {

    private static final boolean[] pawns = {false, false, false, false, false, true, false, false, false, false, false, true, false, false, false, false, false};
    private static final boolean[] rooks = {false, false, false, false, true, false, false, false, false, false, false, false, true, false, false, false, false};
    private static final boolean[] knights = {false, false, false, true, false, false, false, false, false, false, false, false, false, true, false, false, false};
    private static final boolean[] bishops = {false, false, true, false, false, false, false, false, false, false, false, false, false, false, true, false, false};
    private static final boolean[] queens = {false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false};
    private static final boolean[] kings = {true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true};
    private static final boolean[] cages = {false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, false};

    private Color upper;
    private Color lower;

    private Color colorMove = Color.WHITE;
    private BoardMatrix current;
    private final LinkedList<BoardMatrix> matrices;

    public Game(Color upper) {
        this.upper = upper;
        lower = reverse(upper);

        current = new BoardMatrix(upper);
        matrices = new LinkedList<>();
    }

    public Game(Color upper, String fen) {
        this.upper = upper;
        lower = reverse(upper);

        current = new BoardMatrix(upper, fen);
        colorMove = fen.split(" ")[1].equals("w") ? Color.WHITE : Color.BLACK;
        matrices = new LinkedList<>();
    }

    Game(Color colorMove, Color upper, Color lower, BoardMatrix current,
                 LinkedList<BoardMatrix> matrices) {
        this.colorMove = colorMove;
        this.upper = upper;
        this.lower = lower;
        this.current = current;
        this.matrices = matrices;
    }

    public static boolean isValidFEN(String fen) {
        String[] parts = fen.split(" ");

        if (parts.length > 3) return false;
        if (parts.length == 3 && (!checkRightPart(parts[1]) || !checkRightPart(parts[2]))) {
            return false;
        }
        if (parts.length == 2 && !checkRightPart(parts[1])) {
            return false;
        }

        parts[0] = parts[0].replaceAll("8", "11111111")
                .replaceAll("7", "1111111")
                .replaceAll("6", "111111")
                .replaceAll("5", "11111")
                .replaceAll("4", "1111")
                .replaceAll("3", "111")
                .replaceAll("2", "11");

        List<Character> chars = new ArrayList<>(Arrays.asList('r', 'n', 'b', 'q', 'k', 'p', '1', 'R', 'N', 'B', 'Q', 'K', 'P'));
        String[] lines = parts[0].split("/");
        if (lines.length != 8) return false;

        for (String line : lines) {
            if (line.length() != 8) return false;

            for (char c : line.toCharArray()) {
                if (!chars.contains(c)) return false;
            }
        }
        return true;
    }

    private static boolean checkRightPart(String part) {
        if (part.matches("(b)|(w)")) return true;
        int k = 0, K = 0, q = 0, Q = 0, another = 0;

        for (char c : part.toCharArray()) {
            if (c == 'k') k++;
            else if (c == 'K') K++;
            else if (c == 'q') q++;
            else if (c == 'Q') Q++;
            else another++;
        }
        return k <= 1 && K <= 1 && q <= 1 && Q <= 1 && another == 0;
    }

    public byte[][] getMatrix() {
        return current.getMatrix();
    }

    public byte[][] getLastMatrix(int depth) {
        return matrices.get(matrices.size() - depth).getMatrix();
    }

    public Color getUpper() {
        return upper;
    }

    public Color getLower() {
        return lower;
    }

    public Color getColorMove() {
        return colorMove;
    }

    void setColorMove(Color colorMove) {
        this.colorMove = colorMove;
    }

    public void updateUpperColor(Color upper) {
        if (this.upper == upper) return;

        this.upper = upper;
        lower = reverse(upper);
        if (!matrices.isEmpty()) {
            colorMove = reverse(colorMove);
        }

        for (BoardMatrix matrix : matrices) {
            matrix.updateColor(upper);
        }
        current.updateColor(upper);
    }

    public Game cloneGame() {
        LinkedList<BoardMatrix> cloneMatrices = new LinkedList<>();
        for (BoardMatrix boardMatrix : matrices) {
            cloneMatrices.add(boardMatrix.cloneMatrix());
        }

        return new Game(colorMove, upper, lower, current.cloneMatrix(), cloneMatrices);
    }

    public void makeMove(Move move) {
        matrices.offer(current.cloneMatrix());
        if (isTakeOnPass(move)) {
            current.setCage(move.getMoveX(), move.getPieceY());
        }

        if (isCastleMove(move)) {
            if (move.getMoveX() < move.getPieceX()) {
                current.setPiece(0, move.getPieceY(), move.getMoveX() + 1, move.getPieceY());
                current.setCage(0, move.getPieceY());
            } else {
                current.setPiece(7, move.getPieceY(), move.getMoveX() - 1, move.getPieceY());
                current.setCage(7, move.getPieceY());
            }
        }

        current.setPiece(move.getPieceX(), move.getPieceY(), move.getMoveX(), move.getMoveY());
        current.setCage(move.getPieceX(),move.getPieceY());
        // we pass the move to another color after all the actions!
        colorMove = reverse(colorMove);
    }

    public boolean isBlackKingMadeMove() {
        return current.isBlackKingMadeMove();
    }

    public boolean isWhiteKingMadeMove() {
        return current.isWhiteKingMadeMove();
    }

    public boolean isLeftWhiteRookMadeMove() {
        return current.isLeftWhiteRookMadeMove();
    }

    public boolean isRightWhiteRookMadeMove() {
        return current.isRightWhiteRookMadeMove();
    }

    public boolean isLeftBlackRookMadeMove() {
        return current.isLeftBlackRookMadeMove();
    }

    public boolean isRightBlackRookMadeMove() {
        return current.isRightBlackRookMadeMove();
    }

    public Array<Move> getMoves(int x, int y) {
        return getMovesToSaveKing(getPieceMoves(x, y));
    }

    public boolean isPawn(int x, int y) {
        return pawns[current.getPiece(x, y) + 8];
    }

    public boolean isRook(int x, int y) {
        return rooks[(current.getPiece(x, y)) + 8];
    }

    public boolean isKnight(int x, int y) {
        return knights[current.getPiece(x, y) + 8];
    }

    public boolean isBishop(int x, int y) {
        return bishops[current.getPiece(x, y) + 8];
    }

    public boolean isQueen(int x, int y) {
        return queens[current.getPiece(x, y) + 8];
    }

    public boolean isKing(int x, int y) {
        return kings[current.getPiece(x, y) + 8];
    }

    public boolean isCage(int x, int y) {
        return cages[current.getPiece(x, y) + 8];
    }

    public boolean isPawn(byte type) {
        return pawns[type + 8];
    }

    public boolean isRook(byte type) {
        return rooks[type + 8];
    }

    public boolean isKnight(byte type) {
        return knights[type + 8];
    }

    public boolean isBishop(byte type) {
        return bishops[type + 8];
    }

    public boolean isQueen(byte type) {
        return queens[type + 8];
    }

    public boolean isKing(byte type) {
        return kings[type + 8];
    }

    public boolean isCage(byte type) {
        return cages[type + 8];
    }

    public byte getPiece(int x, int y) {
        return current.getPiece(x, y);
    }

    public Color getColor(byte type) {
        if (type < -2) return Color.WHITE;
        else if (type > 2) return Color.BLACK;
        return null;
    }

    public Color getColor(int x, int y) {
        if (current.getPiece(x, y) < -2) return Color.WHITE;
        else if (current.getPiece(x, y) > 2) return Color.BLACK;
        return null;
    }

    public int getTurn() {
        return matrices.size() / 2 + 1;
    }

    public int getMove() {
        return matrices.size();
    }

    public byte getId(int x, int y) {
        return current.getId(x, y);
    }

    public void cancelMove() {
        if (matrices.isEmpty()) return;

        current = matrices.removeLast();
        colorMove = reverse(colorMove);
    }

    public boolean isUpdated(Move move) {
        return isPawn(move.getPieceX(), move.getPieceY()) && (move.getMoveY() == 0 || move.getMoveY() == 7);
    }

    public void updatePawn(int pawnX, int pawnY, byte type) {
        int sign = current.getPiece(pawnX, pawnY) > 0 ? 1 : -1;
        current.updatePiece(pawnX, pawnY, (byte) (type * sign));
    }

    public Pair<Integer, Integer> getCheckKing() {
        byte[][] matrix = getMatrix();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (isCheckKing(j, i)) {
                    return new Pair<>(j, i);
                }
            }
        }
        return null;
    }

    public boolean isCheckKing(int kingX, int kingY) {
        if (!isKing(kingX, kingY)) return false;
        byte[][] matrix = getMatrix();

        Color saved = colorMove;
        colorMove = reverse(getColor(kingX, kingY));

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                Array<Move> moves;

                if (isKing(j, i)) {
                    moves = getKingMoves(j, i);
                } else {
                    moves = getPieceMoves(j, i);
                }

                for (Move move : moves) {
                    if (move.getMoveX() == kingX && move.getMoveY() == kingY) {
                        colorMove = saved;
                        return true;
                    }
                }
            }
        }

        colorMove = saved;
        return false;
    }

    public boolean isFinish() {
        byte[][] matrix = current.getMatrix();

        int count = 0;
        boolean finish = true;

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (!isCage(j, i)) count++;

                if (!getMoves(j, i).isEmpty()) {
                    finish = false;
                }
            }
        }

        return finish || count == 2;
    }

    public boolean isCastleMove(Move move) {
        return getCastleMoves(move.getPieceX(), move.getPieceY())
                .contains(move, false);
    }

    public Color reverse(Color first) {
        return first == Color.BLACK ? Color.WHITE : Color.BLACK;
    }

    private boolean isKingMadeMove() {
        if (colorMove == Color.BLACK) return current.isBlackKingMadeMove();
        return current.isWhiteKingMadeMove();
    }

    private Array<Move> getPieceMoves(int x, int y) {
        if (getColor(x, y) != colorMove) return new Array<>(0);

        Array<Move> moves;
        if (isPawn(x, y)) {
            moves = getPawnMoves(x, y);
        }
        else if (isRook(x, y)) {
            moves = getRookMoves(x, y);
        }
        else if (isKnight(x, y)) {
            moves = getKnightMoves(x, y);
        }
        else if (isBishop(x, y)) {
            moves =  getBishopMoves(x, y);
        }
        else if (isQueen(x, y)) {
            moves = getQueenMoves(x, y);
        }
        else if (isKing(x, y)) {
            moves = getKingMoves(x, y);

            if (!isCheckKing(x, y)) {
                moves.addAll(getCastleMoves(x, y));
            }
        }
        else {
            moves = new Array<>(0);
        }

        return moves;
    }

    private Array<Move> getQueenMoves(int x, int y) {
        Array<Move> queenMoves = getRookMoves(x, y);
        queenMoves.addAll(getBishopMoves(x, y));

        return queenMoves;
    }

    private Array<Move> getKnightMoves(int x, int y) {
        Array<Move> knightMoves = new Array<>();

        addValidKnightMove(knightMoves, x, y, x + 1, y + 2);
        addValidKnightMove(knightMoves, x, y, x - 1, y + 2);

        addValidKnightMove(knightMoves, x, y, x + 1, y - 2);
        addValidKnightMove(knightMoves, x, y, x - 1, y - 2);

        addValidKnightMove(knightMoves, x, y, x + 2, y + 1);
        addValidKnightMove(knightMoves, x, y, x + 2, y - 1);

        addValidKnightMove(knightMoves, x, y, x - 2, y + 1);
        addValidKnightMove(knightMoves, x, y, x - 2, y - 1);

        return knightMoves;
    }

    private void addValidKnightMove(Array<Move> knightMoves, int x, int y, int moveX, int moveY) {
        Color color = getColor(moveX, moveY);
        if ((color != null && color != colorMove) || isCage(moveX, moveY)) {
            knightMoves.add(Move.valueOf(x, y, moveX, moveY));
        }
    }

    private Array<Move> getBishopMoves(int x, int y) {
        Array<Move> bishopMoves = new Array<>();
        int j = x + 1, i = y + 1;

        while (j < 8 && i < 8) {
            if (getColor(j, i) != colorMove) {
                bishopMoves.add(Move.valueOf(x, y, j, i));
            }

            if (!isCage(j, i)) break;

            j++;
            i++;
        }

        j = x + 1;
        i = y - 1;
        while (j < 8 && i >= 0) {
            if (getColor(j, i) != colorMove) {
                bishopMoves.add(Move.valueOf(x, y, j, i));
            }

            if (!isCage(j, i)) break;

            j++;
            i--;
        }

        j = x - 1;
        i = y + 1;
        while (j >= 0 && i < 8) {
            if (getColor(j, i) != colorMove) {
                bishopMoves.add(Move.valueOf(x, y, j, i));
            }

            if (!isCage(j, i)) break;

            j--;
            i++;
        }

        j = x - 1;
        i = y - 1;
        while (j >= 0 && i >= 0 ) {
            if (getColor(j, i) != colorMove) {
                bishopMoves.add(Move.valueOf(x, y, j, i));
            }

            if (!isCage(j, i)) break;

            j--;
            i--;
        }

        return bishopMoves;
    }

    private Array<Move> getRookMoves(int x, int y) {
        Array<Move> rookMoves = new Array<>();

        for (int i = y - 1; i >= 0; i--) {
            if (getColor(x, i) != colorMove) {
                rookMoves.add(Move.valueOf(x, y, x, i));
            }

            if (!isCage(x, i)) break;
        }

        for (int i = y + 1; i < 8; i++) {
            if (getColor(x, i) != colorMove) {
                rookMoves.add(Move.valueOf(x, y, x, i));
            }

            if (!isCage(x, i)) break;
        }

        for (int i = x - 1; i >= 0; i--) {
            if (getColor(i, y) != colorMove) {
                rookMoves.add(Move.valueOf(x, y, i, y));
            }

            if (!isCage(i, y)) break;
        }

        for (int i = x + 1; i < 8; i++) {
            if (getColor(i, y) != colorMove) {
                rookMoves.add(Move.valueOf(x, y, i, y));
            }

            if (!isCage(i, y)) break;
        }

        return rookMoves;
    }

    private Array<Move> getKingMoves(int x, int y) {
        Array<Move> kingMoves = new Array<>();
        int sign = colorMove == Color.BLACK ? -1 : 1;

        addKingMove(kingMoves, x, y, x - 1, y, sign);
        addKingMove(kingMoves, x, y, x - 1, y - 1, sign);
        addKingMove(kingMoves, x, y, x, y - 1, sign);
        addKingMove(kingMoves, x, y, x + 1, y - 1, sign);
        addKingMove(kingMoves, x, y, x + 1, y, sign);
        addKingMove(kingMoves, x, y, x + 1, y + 1, sign);
        addKingMove(kingMoves, x, y, x, y + 1, sign);
        addKingMove(kingMoves, x, y, x - 1, y + 1, sign);

        return kingMoves;
    }

    private Array<Move> getPawnMoves(int x, int y) {
        Array<Move> pawnMoves = new Array<>();

        int direction = isUpperColor(x, y) ? 1 : -1;
        Color figureColor = getColor(x, y);

        if (isCage(x,y + direction)) {
            pawnMoves.add(Move.valueOf(x, y, x, y + direction));

            if ((y == 1 || y == 6) && isCage(x,y + direction * 2)) {
                pawnMoves.add(Move.valueOf(x, y, x, y + direction * 2));
            }
        }

        Color left = getColor(x - 1, y + direction);
        if (left != null && left != colorMove) {
            pawnMoves.add(Move.valueOf(x, y, x - 1, y + direction));
        }

        Color right = getColor(x + 1, y + direction);
        if (right != null && right != colorMove) {
            pawnMoves.add(Move.valueOf(x, y, x + 1, y + direction));
        }

        // take on the pass
        if (((y == 3 && figureColor == lower) || (y == 4 && figureColor == upper))
                && ((getColor(x - 1, y)) != colorMove && getColor(x + 1, y) != colorMove)) {

            Move move = findChange();

            // only pawn
            if (isPawn(move.getMoveX(), move.getMoveY()) && (move.getPieceY() + -direction * 2) == move.getMoveY()) {

                if (x - 1 == move.getMoveX() && y == move.getMoveY()) {
                    pawnMoves.add(Move.valueOf(x, y, x - 1, y + direction));
                }

                if (x + 1 == move.getMoveX() && y == move.getMoveY()) {
                    pawnMoves.add(Move.valueOf(x, y, x + 1, y + direction));
                }
            }
        }

        return pawnMoves;
    }

    private byte[] getKingPosition(Color color) {
        byte[][] matrix = current.getMatrix();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (isKing(j, i) && getColor(j, i) == color) {
                    return new byte[]{(byte) j, (byte) i};
                }
            }
        }
        return new byte[]{-1, -1};
    }

    private void addKingMove(Array<Move> moves, int figureX, int figureY, int x, int y, int sign) {
        if (current.getPiece(x, y) * sign >= 1 || isCage(x, y)) {
            moves.add(Move.valueOf(figureX, figureY, x, y));
        }
    }

    private Array<Move> getCastleMoves(int pieceX, int pieceY) {
        if (!isKing(pieceX, pieceY) || isKingMadeMove()) return new Array<>(0);

        Color kingColor = getColor(pieceX, pieceY);
        Array<Move> castleMoves = new Array<>();

        if ((kingColor == upper && pieceY == 0) || (kingColor == lower && pieceY == 7)) {
            if (pieceX == 3) {
                if (checkTreePosition(pieceX, pieceY, -1) && checkQueenCastleByRook(kingColor)) {
                    castleMoves.add(Move.valueOf(pieceX, pieceY, pieceX - 2, pieceY));
                }

                if (checkFourPosition(pieceX, pieceY, 1)  && checkKingCastleByRook(kingColor)) {
                    castleMoves.add(Move.valueOf(pieceX, pieceY, pieceX + 2, pieceY));
                }
            } else if (pieceX == 4) {
                if (checkFourPosition(pieceX, pieceY, -1) && checkQueenCastleByRook(kingColor)) {
                    castleMoves.add(Move.valueOf(pieceX, pieceY, pieceX - 2, pieceY));
                }

                if (checkTreePosition(pieceX, pieceY, 1) && checkKingCastleByRook(kingColor)) {
                    castleMoves.add(Move.valueOf(pieceX, pieceY, pieceX + 2, pieceY));
                }
            }
        }

        return castleMoves;
    }

    private boolean checkKingCastleByRook(Color color) {
        if (color == Color.BLACK) return !current.isRightBlackRookMadeMove();
        return !current.isRightWhiteRookMadeMove();
    }

    private boolean checkQueenCastleByRook(Color color) {
        if (color == Color.BLACK) return !current.isLeftBlackRookMadeMove();
        return !current.isLeftWhiteRookMadeMove();
    }


    private boolean checkFourPosition(int figureX, int figureY, int sign) {
        return isCage(figureX + sign, figureY) && isCage(figureX + sign * 2, figureY)
                && isCage(figureX + sign * 3, figureY) && isRook(figureX + sign * 4, figureY);
    }

    private boolean checkTreePosition(int figureX, int figureY, int sign) {
        return isCage(figureX + sign, figureY) && isCage(figureX + sign * 2, figureY)
                && isRook(figureX + sign * 3, figureY);
    }

    private Move findChange() {
        if (matrices.size() == 1 || matrices.isEmpty()) return Move.valueOf(-1, -1, -1, -1);

        byte[][] current = getMatrix();
        byte[][] last = getLastMatrix(1);

        int figureX = -1, figureY = -1, moveX = -1, moveY = -1;

        for (int i = 0; i < current.length; i++) {
            for (int j = 0; j < current[i].length; j++) {
                if (current[i][j] != last[i][j]) {
                    if (!isCage(j, i)) {
                        moveX = j;
                        moveY = i;
                    } else {
                        figureX = j;
                        figureY = i;
                    }
                }
            }
        }
        return Move.valueOf(figureX, figureY, moveX, moveY);
    }

    private Array<Move> getMovesToSaveKing(Array<Move> moves) {
        Array<Move> saving = new Array<>();

        for (Move move : moves) {
            makeMove(move);
            byte[] position = getKingPosition(reverse(colorMove));
            if (!isCheckKing(position[0], position[1])) saving.add(move);
            cancelMove();
        }

        return saving;
    }

    private boolean isUpperColor(int x, int y) {
        return getColor(x, y) == upper;
    }

    private boolean isTakeOnPass(Move move) {
        if (!isPawn(move.getPieceX(), move.getPieceY()) || !isPawn(move.getMoveX(), move.getPieceY())) return false;
        return move.getMoveX() != move.getPieceX() && isCage(move.getMoveX(), move.getMoveY());
    }
}
