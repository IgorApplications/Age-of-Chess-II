package com.iapp.ageofchess.chess_engine;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Backwards compatible!
 * @version 1.0
 * @author Igor Ivanov
 * */
public class BoardMatrix {

    public static final byte PAWN = 3;
    public static final byte ROOK = 4;
    public static final byte KNIGHT = 5;
    public static final byte BISHOP = 6;
    public static final byte QUEEN = 7;
    public static final byte KING = 8;

    static final byte WHITE_PAWN = -PAWN;
    static final byte WHITE_ROOK = -ROOK;
    static final byte WHITE_KNIGHT = -KNIGHT;
    static final byte WHITE_BISHOP = -BISHOP;
    static final byte WHITE_QUEEN = -QUEEN;
    static final byte WHITE_KING = -KING;

    static final byte CAGE = 1;
    static final byte WALL = 0;

    static final byte BLACK_PAWN = PAWN;
    static final byte BLACK_ROOK = ROOK;
    static final byte BLACK_KNIGHT = KNIGHT;
    static final byte BLACK_BISHOP = BISHOP;
    static final byte BLACK_QUEEN = QUEEN;
    static final byte BLACK_KING = KING;

    static final int PIECE_SIZE = 12;
    static final int ID_SIZE = 8;
    static final int INDENT_WALL = 2;

    private static final Map<Character, Byte> fenByPieces  = new HashMap<>(Map.of(
            'K', WHITE_KING,  'Q', WHITE_QUEEN, 'B', WHITE_BISHOP,
            'N', WHITE_KNIGHT, 'R', WHITE_ROOK, 'P', WHITE_PAWN,
            '1', CAGE,
            'p', BLACK_PAWN, 'r', BLACK_ROOK, 'n', BLACK_KNIGHT));

    static {
        fenByPieces.put('b', BLACK_BISHOP);
        fenByPieces.put('q', BLACK_QUEEN);
        fenByPieces.put('k', BLACK_KING);
    }

    private final byte[][] id;

    private final byte[][] matrix;
    private Color upper;
    /**
     * This is a bitmask for storing flags:
     * 0 - whiteKingMadeMove
     * 1 - blackKingMadeMove
     * 2 - leftWhiteRookMadeMove
     * 3 - rightWhiteRookMadeMove
     * 4 - leftBlackRookMadeMove
     * 5 - rightBlackRookMadeMove
     * */
    private final BitSet flags;

    BoardMatrix(Color upper, String fen) {
        flags = new BitSet();
        fen = fen.replaceAll("2", "11").replaceAll("3", "111")
                .replaceAll("4", "1111").replaceAll("5", "11111")
                .replaceAll("6", "111111").replaceAll("7", "1111111")
                .replaceAll("8", "11111111");
        var tokens = fen.split(" ");

        id = new byte[][] {
                {-1, -1, -1, -1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1, -1, -1, -1}
        };

        matrix = new byte[][] {
                {WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL},
                {WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL},
                {WALL, WALL, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, WALL, WALL},
                {WALL, WALL, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, WALL, WALL},
                {WALL, WALL, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, WALL, WALL},
                {WALL, WALL, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, WALL, WALL},
                {WALL, WALL, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, WALL, WALL},
                {WALL, WALL, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, WALL, WALL},
                {WALL, WALL, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, WALL, WALL},
                {WALL, WALL, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, WALL, WALL},
                {WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL},
                {WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL},
        };

        byte idPiece = 0;
        var lines = tokens[0].split("/");
        for (int i = 0; i < lines.length; i++) {
            var array = lines[i].toCharArray();

            for (int j = 0; j < array.length; j++) {
                var type = fenByPieces.get(array[j]);
                if (type == CAGE) id[i][j] = -1;
                else id[i][j] = idPiece++;
                matrix[i + INDENT_WALL][j + INDENT_WALL] = type;
            }
        }

        if (tokens.length > 2) {
            if (!tokens[2].contains("Q") && !tokens[2].contains("K")) {
                flags.set(0, true);

            } else {
                if (!tokens[2].contains("Q")) {
                    flags.set(2, true);
                }

                if (!tokens[2].contains("K")) {
                    flags.set(3, true);
                }
            }

            if (!tokens[2].contains("q") && !tokens[2].contains("k")) {
                flags.set(1, true);
            } else {
                if (!tokens[2].contains("q")) {
                    flags.set(4, true);
                }

                if (!tokens[2].contains("k")) {
                    flags.set(5, true);
                }
            }
        }

        if (upper == Color.WHITE) {
            flipBoard();
        }
    }

    BoardMatrix(Color upper) {
        flags = new BitSet();
        this.upper = upper;

        id = new byte[][] {
                {0, 1, 2, 3, 4, 5, 6, 7},
                {8, 9, 10, 11, 12, 13, 14, 15},
                {-1, -1, -1, -1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1, -1, -1, -1},
                {-1, -1, -1, -1, -1, -1, -1, -1},
                {16, 17, 18, 19, 20, 21, 22, 23},
                {24, 25, 26, 27, 28, 29, 30, 31},};

        matrix = new byte[][] {
                {WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL},
                {WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL},
                {WALL, WALL, BLACK_ROOK, BLACK_KNIGHT, BLACK_BISHOP, BLACK_QUEEN, BLACK_KING, BLACK_BISHOP, BLACK_KNIGHT, BLACK_ROOK, WALL, WALL},
                {WALL, WALL, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, WALL, WALL},
                {WALL, WALL, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, WALL, WALL},
                {WALL, WALL, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, WALL, WALL},
                {WALL, WALL, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, WALL, WALL},
                {WALL, WALL, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, CAGE, WALL, WALL},
                {WALL, WALL, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WALL, WALL},
                {WALL, WALL, WHITE_ROOK, WHITE_KNIGHT, WHITE_BISHOP, WHITE_QUEEN, WHITE_KING, WHITE_BISHOP, WHITE_KNIGHT, WHITE_ROOK, WALL, WALL},
                {WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL},
                {WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL},
        };

        if (upper == Color.WHITE) {
            flipBoard();
        }
    }

    boolean isWhiteKingMadeMove() {
        return flags.get(0);
    }


    boolean isBlackKingMadeMove() {
        return flags.get(1);
    }
    public boolean isLeftWhiteRookMadeMove() {
        return flags.get(2);
    }

    public boolean isRightWhiteRookMadeMove() {
        return flags.get(3);
    }

    public boolean isLeftBlackRookMadeMove() {
        return flags.get(4);
    }

    public boolean isRightBlackRookMadeMove() {
        return flags.get(5);
    }

    void updateColor(Color upper) {
        if (this.upper == upper) return;
        this.upper = upper;

        flipBoard();
    }

    byte[][] getMatrix() {
        byte[][] newMatrix = new byte[ID_SIZE][ID_SIZE];
        for (int i = INDENT_WALL; i < PIECE_SIZE - INDENT_WALL; i++) {
            System.arraycopy(matrix[i], INDENT_WALL, newMatrix[i - INDENT_WALL], 0, ID_SIZE);
        }
        return newMatrix;
    }

    BoardMatrix(Color upper, byte[][] matrix, byte[][] id, BitSet flags) {
        this.upper = upper;
        this.matrix = matrix;
        this.id = id;
        this.flags = (BitSet) flags.clone();
    }

    byte getId(int x, int y) {
        return id[y][x];
    }

    byte getPiece(int x, int y) {
        return matrix[y + INDENT_WALL][x + INDENT_WALL];
    }

    byte getPiece(Move move) {
        return getPiece(move.getPieceX(), move.getPieceY());
    }

    void setPiece(int pieceX, int pieceY, int x, int y) {
        if (getPiece(pieceX, pieceY) == WHITE_KING) flags.set(0, true);
        if (getPiece(pieceX, pieceY) == BLACK_KING) flags.set(1, true);
        if (pieceX == 0 && getPiece(pieceX, pieceY) == WHITE_ROOK) flags.set(2, true);
        if (pieceX == 7 && getPiece(pieceX, pieceY) == WHITE_ROOK) flags.set(3, true);
        if (pieceX == 0 && getPiece(pieceX, pieceY) == BLACK_ROOK) flags.set(4, true);
        if (pieceX == 7 && getPiece(pieceX, pieceY) == BLACK_ROOK) flags.set(5, true);

        id[y][x] = id[pieceY][pieceX];
        matrix[y + INDENT_WALL][x + INDENT_WALL] = matrix[pieceY + INDENT_WALL][pieceX + INDENT_WALL];
    }

    void setPiece(int pieceX, int pieceY, byte piece) {
        if (piece == CAGE) throw new IllegalArgumentException();
        matrix[pieceY + INDENT_WALL][pieceX + INDENT_WALL] = piece;
    }

    void setCage(int x, int y) {
        id[y][x] = -1;
        matrix[y + INDENT_WALL][x + INDENT_WALL] = CAGE;
    }

    BoardMatrix cloneMatrix() {
        byte[][] newMatrix = new byte[PIECE_SIZE][PIECE_SIZE];
        for (int i = 0; i < matrix.length; i++) {
            newMatrix[i] = matrix[i].clone();
        }

        byte[][] newId = new byte[ID_SIZE][ID_SIZE];
        for (int i = 0; i < id.length; i++) {
            newId[i] = id[i].clone();
        }

        return new BoardMatrix(upper, newMatrix, newId, flags);
    }

    private void flipBoard() {
        var line1 = matrix[2];
        var line2 = matrix[3];
        var line3 = matrix[4];
        var line4 = matrix[5];

        matrix[2] = matrix[9];
        matrix[3] = matrix[8];
        matrix[4] = matrix[7];
        matrix[5] = matrix[6];

        matrix[9] = line1;
        matrix[8] = line2;
        matrix[7] = line3;
        matrix[6] = line4;

        line1 = id[0];
        line2 = id[1];
        line3 = id[2];
        line4 = id[3];

        id[7] = line1;
        id[6] = line2;
        id[5] = line3;
        id[4] = line4;
    }
}
